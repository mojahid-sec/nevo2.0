package com.example.data.model

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

enum class TrackingMode {
    MANUAL,
    AUTOMATIC,
    DAILY_RESET;

    companion object {
        fun fromString(value: String?): TrackingMode {
            return when (value?.uppercase()) {
                "AUTOMATIC", "AUTO" -> AUTOMATIC
                "DAILY_RESET", "DAILY", "DAILY RESET" -> DAILY_RESET
                else -> MANUAL
            }
        }
    }
}

data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val durationDays: Int = 30, // 2 to 365
    val startDateMillis: Long = System.currentTimeMillis(),
    val reason: String = "",
    val completedDaysCount: Int = 0,
    val lastCheckInTimestampMillis: Long? = null,
    val lastCheckInDateKey: String? = null,
    val isSelected: Boolean = false,
    val isCompleted: Boolean = false,
    val completedDateMillis: Long? = null,
    val trackingMode: TrackingMode = TrackingMode.MANUAL
) {
    // ------------------------------------------------------------------------
    // Circle 1: Manual Progress (Increases ONLY via valid manual Check-In)
    // ------------------------------------------------------------------------
    val manualCompletedDays: Int
        get() = completedDaysCount

    val manualRemainingDays: Int
        get() = (durationDays - completedDaysCount).coerceAtLeast(0)

    val manualProgressFraction: Float
        get() = if (durationDays > 0) {
            (completedDaysCount.toFloat() / durationDays.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val manualProgressPercentage: Int
        get() = (manualProgressFraction * 100f).toInt()

    // ------------------------------------------------------------------------
    // Circle 2: Automatic Progress (Increases ONLY via elapsed 24-hour time)
    // ------------------------------------------------------------------------
    fun getAutomaticCompletedDays(nowMillis: Long = System.currentTimeMillis()): Int {
        val elapsedMillis = (nowMillis - startDateMillis).coerceAtLeast(0L)
        val full24HourDays = (elapsedMillis / EXACT_24_HOURS_MILLIS).toInt()
        return full24HourDays.coerceIn(0, durationDays)
    }

    fun getAutomaticRemainingDays(nowMillis: Long = System.currentTimeMillis()): Int {
        return (durationDays - getAutomaticCompletedDays(nowMillis)).coerceAtLeast(0)
    }

    fun getAutomaticProgressFraction(nowMillis: Long = System.currentTimeMillis()): Float {
        return if (durationDays > 0) {
            (getAutomaticCompletedDays(nowMillis).toFloat() / durationDays.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    fun getAutomaticProgressPercentage(nowMillis: Long = System.currentTimeMillis()): Int {
        return (getAutomaticProgressFraction(nowMillis) * 100f).toInt()
    }

    fun isAutomaticCompleted(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return getAutomaticCompletedDays(nowMillis) >= durationDays
    }

    fun getRemainingMillisUntilNextAutoDay(nowMillis: Long = System.currentTimeMillis()): Long {
        if (isAutomaticCompleted(nowMillis)) return 0L
        val elapsedMillis = (nowMillis - startDateMillis).coerceAtLeast(0L)
        val remainderMillis = elapsedMillis % EXACT_24_HOURS_MILLIS
        return (EXACT_24_HOURS_MILLIS - remainderMillis).coerceAtLeast(0L)
    }

    // ------------------------------------------------------------------------
    // Unified Single Progress Properties (Depends strictly on trackingMode)
    // ------------------------------------------------------------------------
    fun getEffectiveCompletedDays(nowMillis: Long = System.currentTimeMillis()): Int {
        return when (trackingMode) {
            TrackingMode.AUTOMATIC -> getAutomaticCompletedDays(nowMillis)
            TrackingMode.MANUAL, TrackingMode.DAILY_RESET -> completedDaysCount
        }
    }

    fun getEffectiveRemainingDays(nowMillis: Long = System.currentTimeMillis()): Int {
        return (durationDays - getEffectiveCompletedDays(nowMillis)).coerceAtLeast(0)
    }

    fun getEffectiveProgressFraction(nowMillis: Long = System.currentTimeMillis()): Float {
        return if (durationDays > 0) {
            (getEffectiveCompletedDays(nowMillis).toFloat() / durationDays.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    fun getEffectiveProgressPercentage(nowMillis: Long = System.currentTimeMillis()): Int {
        return (getEffectiveProgressFraction(nowMillis) * 100f).toInt()
    }

    val remainingDays: Int
        get() = getEffectiveRemainingDays()

    val progressFraction: Float
        get() = getEffectiveProgressFraction()

    val progressPercentage: Int
        get() = getEffectiveProgressPercentage()

    val expectedCompletionDateMillis: Long
        get() = startDateMillis + (durationDays.toLong() * 24L * 60L * 60L * 1000L)

    companion object {
        const val EXACT_24_HOURS_MILLIS: Long = 24L * 60L * 60L * 1000L // 86,400,000 ms

        fun getLocalDateKey(
            millis: Long = System.currentTimeMillis(),
            timeZone: TimeZone = TimeZone.getDefault()
        ): String {
            val cal = Calendar.getInstance(timeZone).apply {
                timeInMillis = millis
            }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)
            return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        }

        fun getRemainingMillisUntilNextMidnight(
            nowMillis: Long = System.currentTimeMillis(),
            timeZone: TimeZone = TimeZone.getDefault()
        ): Long {
            val cal = Calendar.getInstance(timeZone).apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return (cal.timeInMillis - nowMillis).coerceAtLeast(0L)
        }
    }

    /**
     * Checks check-in eligibility according to trackingMode:
     * - MANUAL: exact 24 hours elapsed since previous check-in.
     * - DAILY_RESET: once per local calendar day (resets at 00:00 local midnight).
     * - AUTOMATIC: automatic progress only.
     */
    fun isCheckInEligible(
        nowMillis: Long = System.currentTimeMillis(),
        recordedDateKeys: Set<String> = emptySet(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Boolean {
        if (isCompleted) return false
        return when (trackingMode) {
            TrackingMode.MANUAL -> {
                val lastTimestamp = lastCheckInTimestampMillis ?: return true
                nowMillis >= (lastTimestamp + EXACT_24_HOURS_MILLIS)
            }
            TrackingMode.DAILY_RESET -> {
                val todayKey = getLocalDateKey(nowMillis, timeZone)
                if (todayKey in recordedDateKeys) return false
                if (lastCheckInDateKey != null && lastCheckInDateKey == todayKey) return false
                val lastTimestamp = lastCheckInTimestampMillis ?: return true
                getLocalDateKey(lastTimestamp, timeZone) != todayKey
            }
            TrackingMode.AUTOMATIC -> false
        }
    }

    /**
     * Returns remaining countdown milliseconds until next check-in is eligible.
     */
    fun getRemainingCooldownMillis(
        nowMillis: Long = System.currentTimeMillis(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): Long {
        if (isCompleted) return 0L
        return when (trackingMode) {
            TrackingMode.MANUAL -> {
                val lastTimestamp = lastCheckInTimestampMillis ?: return 0L
                val nextEligible = lastTimestamp + EXACT_24_HOURS_MILLIS
                (nextEligible - nowMillis).coerceAtLeast(0L)
            }
            TrackingMode.DAILY_RESET -> {
                getRemainingMillisUntilNextMidnight(nowMillis, timeZone)
            }
            TrackingMode.AUTOMATIC -> 0L
        }
    }
}

data class CheckInRecord(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val dateKey: String, // Format: YYYY-MM-DD
    val timestampMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)

