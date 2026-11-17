package com.example.data.model

import com.example.R
import com.example.data.localization.AppLanguage
import com.example.data.localization.toLocalizedDigits
import java.util.Calendar
import java.util.UUID

enum class AlarmType {
    STANDALONE,
    HABIT;

    companion object {
        fun fromString(value: String?): AlarmType {
            return when (value?.uppercase()) {
                "HABIT" -> HABIT
                else -> STANDALONE
            }
        }
    }
}

enum class AlarmSound(
    val id: String,
    val rawResId: Int,
    val englishName: String,
    val banglaName: String
) {
    CLASSIC_ALARM("classic_alarm", R.raw.alarm_classic, "Classic Alarm", "ক্লাসিক অ্যালার্ম"),
    DIGITAL_BEEP("digital_beep", R.raw.alarm_digital_beep, "Digital Beep", "ডিজিটাল বিপ"),
    MORNING_RISE("morning_rise", R.raw.alarm_morning_rise, "Morning Rise", "মর্নিং রাইজ"),
    GENTLE_WAKE("gentle_wake", R.raw.alarm_gentle_wake, "Gentle Wake", "জেন্টল ওয়েক"),
    DEEP_ALARM("deep_alarm", R.raw.alarm_deep, "Deep Alarm", "ডিপ অ্যালার্ম"),
    ELECTRONIC_PULSE("electronic_pulse", R.raw.alarm_electronic_pulse, "Electronic Pulse", "ইলেকট্রনিক পালস"),
    BELL_ALARM("bell_alarm", R.raw.alarm_bell, "Bell Alarm", "বেল অ্যালার্ম"),
    RADAR_ALERT("radar_alert", R.raw.alarm_radar, "Radar Alert", "রাডার অ্যালার্ট"),
    NATURE_MORNING("nature_morning", R.raw.alarm_nature_morning, "Nature Morning", "নেচার মর্নিং"),
    URGENT_ALARM("urgent_alarm", R.raw.alarm_urgent, "Urgent Alarm", "আর্জেন্ট অ্যালার্ম");

    fun getLocalizedName(language: AppLanguage): String {
        return if (language == AppLanguage.BANGLA) banglaName else englishName
    }

    companion object {
        fun fromId(id: String?): AlarmSound {
            return values().find {
                it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true)
            } ?: CLASSIC_ALARM
        }
    }
}

data class AlarmItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val hour24: Int, // 0..23
    val minute: Int, // 0..59
    val repeatDays: Set<Int> = emptySet(), // Calendar.SUNDAY (1)..Calendar.SATURDAY (7)
    val isEnabled: Boolean = true,
    val snoozeMinutes: Int = 5, // 5, 10, 15
    val sound: AlarmSound = AlarmSound.CLASSIC_ALARM,
    val type: AlarmType = AlarmType.STANDALONE,
    val habitId: String? = null,
    val use24HourFormat: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    /**
     * Habit alarms always repeat on selected days (or daily if no subset is chosen)
     * and continue ringing even after the Habit is completed until manually disabled or deleted.
     */
    fun isRepeating(): Boolean {
        return repeatDays.isNotEmpty() || type == AlarmType.HABIT
    }

    fun effectiveRepeatDays(): Set<Int> {
        return if (type == AlarmType.HABIT && repeatDays.isEmpty()) ALL_DAYS else repeatDays
    }

    fun mainRequestCode(): Int {
        return (id.hashCode() and 0x1FFFFFFF) + 1000
    }

    fun snoozeRequestCode(): Int {
        return ((id.hashCode() and 0x1FFFFFFF) + 1000) or 0x20000000
    }

    fun computeNextTriggerMillis(nowMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour24.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
        }

        val activeDays = effectiveRepeatDays()
        if (activeDays.isEmpty()) {
            // One-time standalone alarm
            if (cal.timeInMillis <= nowMillis) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        }

        // Repeating alarm: find earliest day in activeDays whose trigger time is strictly > nowMillis
        for (dayOffset in 0..7) {
            val candidate = (cal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val dow = candidate.get(Calendar.DAY_OF_WEEK)
            if (dow in activeDays && candidate.timeInMillis > nowMillis) {
                return candidate.timeInMillis
            }
        }

        // Fallback (should not happen since activeDays is non-empty)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }

    fun formattedTime(language: AppLanguage, force24Hour: Boolean = use24HourFormat): String {
        val h = hour24.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        return if (force24Hour) {
            val hh = h.toString().padStart(2, '0')
            val mm = m.toString().padStart(2, '0')
            if (language == AppLanguage.BANGLA) {
                "${hh}:${mm}".toLocalizedDigits(language)
            } else {
                "$hh:$mm"
            }
        } else {
            val isPm = h >= 12
            val h12 = when {
                h == 0 -> 12
                h > 12 -> h - 12
                else -> h
            }
            val hh = h12.toString().padStart(2, '0')
            val mm = m.toString().padStart(2, '0')
            val suffix = if (language == AppLanguage.BANGLA) {
                if (isPm) "PM" else "AM"
            } else {
                if (isPm) "PM" else "AM"
            }
            val timePart = if (language == AppLanguage.BANGLA) {
                "$hh:$mm".toLocalizedDigits(language)
            } else {
                "$hh:$mm"
            }
            "$timePart $suffix"
        }
    }

    companion object {
        val ALL_DAYS: Set<Int> = setOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        )

        val WEEKDAYS: Set<Int> = setOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY
        )

        val WEEKENDS: Set<Int> = setOf(
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )

        val ORDERED_DAYS: List<Int> = listOf(
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
        )

        val SNOOZE_OPTIONS: List<Int> = listOf(5, 10, 15)
    }
}
