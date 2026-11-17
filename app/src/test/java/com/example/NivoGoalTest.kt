package com.example

import com.example.data.localization.AppLanguage
import com.example.data.localization.MotivationalQuotes
import com.example.data.localization.NivoStrings
import com.example.data.localization.toLocalizedDigits
import com.example.data.model.Goal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NivoGoalTest {

    @Test
    fun testGoalCalculationsAndBoundaries() {
        // Boundary: 2 days
        val minGoal = Goal(
            title = "Two Day Sprint",
            durationDays = 2,
            completedDaysCount = 1
        )
        assertEquals(2, minGoal.durationDays)
        assertEquals(1, minGoal.remainingDays)
        assertEquals(0.5f, minGoal.progressFraction, 0.001f)
        assertEquals(50, minGoal.progressPercentage)

        // Intermediate: 15 days
        val midGoal15 = Goal(
            title = "Two Week Challenge",
            durationDays = 15,
            completedDaysCount = 3
        )
        assertEquals(15, midGoal15.durationDays)
        assertEquals(12, midGoal15.remainingDays)
        assertEquals(0.2f, midGoal15.progressFraction, 0.001f)
        assertEquals(20, midGoal15.progressPercentage)

        // Intermediate: 180 days
        val midGoal180 = Goal(
            title = "Half Year Transformation",
            durationDays = 180,
            completedDaysCount = 90
        )
        assertEquals(180, midGoal180.durationDays)
        assertEquals(90, midGoal180.remainingDays)
        assertEquals(0.5f, midGoal180.progressFraction, 0.001f)
        assertEquals(50, midGoal180.progressPercentage)

        // Boundary: 365 days
        val maxGoal = Goal(
            title = "Full Year Discipline",
            durationDays = 365,
            completedDaysCount = 365
        )
        assertEquals(365, maxGoal.durationDays)
        assertEquals(0, maxGoal.remainingDays)
        assertEquals(1.0f, maxGoal.progressFraction, 0.001f)
        assertEquals(100, maxGoal.progressPercentage)
        assertTrue(maxGoal.remainingDays == 0)
    }

    @Test
    fun testExact24HourRule() {
        val checkInTime = 1_000_000_000_000L // 1 Trillion ms base epoch
        val exact24Hours = 24L * 60L * 60L * 1000L // 86,400,000 ms

        val goalWithCheckIn = Goal(
            title = "24 Hour Test Goal",
            durationDays = 30,
            completedDaysCount = 1,
            lastCheckInTimestampMillis = checkInTime
        )

        // At 10 hours after check-in: Ineligible
        val tenHoursLater = checkInTime + (10L * 60L * 60L * 1000L)
        assertFalse(goalWithCheckIn.isCheckInEligible(tenHoursLater))
        assertEquals(14L * 60L * 60L * 1000L, goalWithCheckIn.getRemainingCooldownMillis(tenHoursLater))

        // At 23 hours 59 minutes 59 seconds: Ineligible
        val almost24Hours = checkInTime + exact24Hours - 1000L
        assertFalse(goalWithCheckIn.isCheckInEligible(almost24Hours))
        assertEquals(1000L, goalWithCheckIn.getRemainingCooldownMillis(almost24Hours))

        // At exactly 24 hours: Eligible!
        val exactly24HoursLater = checkInTime + exact24Hours
        assertTrue(goalWithCheckIn.isCheckInEligible(exactly24HoursLater))
        assertEquals(0L, goalWithCheckIn.getRemainingCooldownMillis(exactly24HoursLater))

        // At 25 hours after check-in: Eligible!
        val twentyFiveHoursLater = checkInTime + (25L * 60L * 60L * 1000L)
        assertTrue(goalWithCheckIn.isCheckInEligible(twentyFiveHoursLater))
        assertEquals(0L, goalWithCheckIn.getRemainingCooldownMillis(twentyFiveHoursLater))
    }

    @Test
    fun testFirstCheckInAlwaysEligible() {
        val brandNewGoal = Goal(
            title = "Brand New Goal",
            durationDays = 30,
            completedDaysCount = 0,
            lastCheckInTimestampMillis = null // No check-in has happened yet
        )

        assertTrue(brandNewGoal.isCheckInEligible())
        assertEquals(0L, brandNewGoal.getRemainingCooldownMillis())
    }

    @Test
    fun testBanglaDigitLocalization() {
        assertEquals("০", 0.toLocalizedDigits(AppLanguage.BANGLA))
        assertEquals("১", 1.toLocalizedDigits(AppLanguage.BANGLA))
        assertEquals("২", 2.toLocalizedDigits(AppLanguage.BANGLA))
        assertEquals("১৫", 15.toLocalizedDigits(AppLanguage.BANGLA))
        assertEquals("১৮০", 180.toLocalizedDigits(AppLanguage.BANGLA))
        assertEquals("৩৬৫", 365.toLocalizedDigits(AppLanguage.BANGLA))

        // English remains Arabic digits
        assertEquals("365", 365.toLocalizedDigits(AppLanguage.ENGLISH))
    }

    @Test
    fun testMotivationalQuotesCount() {
        // Must have at least 50 unique quotes
        assertTrue("Expected at least 50 quotes, found: ${MotivationalQuotes.quotes.size}",
            MotivationalQuotes.quotes.size >= 50)
    }

    @Test
    fun testExpectedCompletionDateCalculation() {
        val start = 1_700_000_000_000L
        val goal30 = Goal(
            title = "30 Days",
            durationDays = 30,
            startDateMillis = start
        )

        val expected = start + (30L * 24L * 60L * 60L * 1000L)
        assertEquals(expected, goal30.expectedCompletionDateMillis)
    }

    @Test
    fun testPermanentSkeuomorphicPaletteConfigured() {
        val colors = com.example.ui.theme.SkeuoColors
        // Permanent Skeuomorphic dark chassis palette (never light)
        assertFalse(colors.isLight)
        // High-contrast light text on dark charcoal/black background
        assertTrue(colors.textPrimary.red > 0.85f && colors.textPrimary.green > 0.85f && colors.textPrimary.blue > 0.85f)
        assertTrue(colors.background.red < 0.1f && colors.background.green < 0.1f && colors.background.blue < 0.1f)
        // Deep red crimson primary & glowing accent
        assertTrue(colors.primaryAccent.red > 0.7f && colors.primaryAccent.green < 0.3f)
        assertTrue(colors.accent.red > 0.9f)
        // Metallic silver & brushed bevel highlights
        assertTrue(colors.silverAccent.blue > colors.card.blue)
        assertTrue(colors.bevelHighlight.red > colors.bevelShadow.red)
    }

    @Test
    fun testTrackingModesDefaultAndParsing() {
        val defaultGoal = Goal(title = "Default Mode Test")
        assertEquals(com.example.data.model.TrackingMode.MANUAL, defaultGoal.trackingMode)

        assertEquals(com.example.data.model.TrackingMode.MANUAL, com.example.data.model.TrackingMode.fromString("MANUAL"))
        assertEquals(com.example.data.model.TrackingMode.AUTOMATIC, com.example.data.model.TrackingMode.fromString("AUTOMATIC"))
        assertEquals(com.example.data.model.TrackingMode.AUTOMATIC, com.example.data.model.TrackingMode.fromString("auto"))
        assertEquals(com.example.data.model.TrackingMode.DAILY_RESET, com.example.data.model.TrackingMode.fromString("DAILY_RESET"))
        assertEquals(com.example.data.model.TrackingMode.DAILY_RESET, com.example.data.model.TrackingMode.fromString("daily"))
        assertEquals(com.example.data.model.TrackingMode.DAILY_RESET, com.example.data.model.TrackingMode.fromString("daily reset"))
        assertEquals(com.example.data.model.TrackingMode.MANUAL, com.example.data.model.TrackingMode.fromString(null))
        assertEquals(com.example.data.model.TrackingMode.MANUAL, com.example.data.model.TrackingMode.fromString("unknown"))
    }

    @Test
    fun testAutomaticProgressIncrementsPer24Hours() {
        val start = 1_000_000_000_000L
        val oneDayMillis = Goal.EXACT_24_HOURS_MILLIS
        val autoGoal = Goal(
            title = "30-Day Auto Habit",
            durationDays = 30,
            startDateMillis = start,
            trackingMode = com.example.data.model.TrackingMode.AUTOMATIC
        )

        // At start time: 0 days
        assertEquals(0, autoGoal.getAutomaticCompletedDays(start))
        assertEquals(30, autoGoal.getAutomaticRemainingDays(start))
        assertEquals(0f, autoGoal.getAutomaticProgressFraction(start), 0.001f)
        assertFalse(autoGoal.isAutomaticCompleted(start))

        // At 12 hours: Still 0 full days
        val halfDay = start + (12L * 60 * 60 * 1000L)
        assertEquals(0, autoGoal.getAutomaticCompletedDays(halfDay))

        // At 23 hours 59 minutes: Still 0 full days
        val almostOneDay = start + oneDayMillis - 1000L
        assertEquals(0, autoGoal.getAutomaticCompletedDays(almostOneDay))

        // At exactly 24 hours: Exactly 1 day!
        val exactlyOneDay = start + oneDayMillis
        assertEquals(1, autoGoal.getAutomaticCompletedDays(exactlyOneDay))
        assertEquals(29, autoGoal.getAutomaticRemainingDays(exactlyOneDay))

        // At 18 days: Exactly 18 days (matching prompt example 18/30 days)
        val eighteenDays = start + (18L * oneDayMillis)
        assertEquals(18, autoGoal.getAutomaticCompletedDays(eighteenDays))
        assertEquals(12, autoGoal.getAutomaticRemainingDays(eighteenDays))
        assertEquals(0.6f, autoGoal.getAutomaticProgressFraction(eighteenDays), 0.001f)
        assertEquals(60, autoGoal.getAutomaticProgressPercentage(eighteenDays))
        assertFalse(autoGoal.isAutomaticCompleted(eighteenDays))

        // At 30 days: Target duration reached -> Completed!
        val thirtyDays = start + (30L * oneDayMillis)
        assertEquals(30, autoGoal.getAutomaticCompletedDays(thirtyDays))
        assertEquals(0, autoGoal.getAutomaticRemainingDays(thirtyDays))
        assertEquals(1.0f, autoGoal.getAutomaticProgressFraction(thirtyDays), 0.001f)
        assertEquals(100, autoGoal.getAutomaticProgressPercentage(thirtyDays))
        assertTrue(autoGoal.isAutomaticCompleted(thirtyDays))

        // Beyond 30 days: Coerced to durationDays
        val fortyDays = start + (40L * oneDayMillis)
        assertEquals(30, autoGoal.getAutomaticCompletedDays(fortyDays))
        assertEquals(0, autoGoal.getAutomaticRemainingDays(fortyDays))
    }

    @Test
    fun testProgressIndependenceManualVersusAutomatic() {
        val start = 1_000_000_000_000L
        val oneDayMillis = Goal.EXACT_24_HOURS_MILLIS

        // Example scenario: 12 manual check-ins, but 18 days elapsed
        val goal = Goal(
            title = "Dual Progress Habit",
            durationDays = 30,
            startDateMillis = start,
            completedDaysCount = 12, // 12 manual check-ins
            trackingMode = com.example.data.model.TrackingMode.MANUAL
        )

        val eighteenDaysLater = start + (18L * oneDayMillis)

        // Circle 1: Manual Progress must reflect ONLY the 12 manual check-ins
        assertEquals(12, goal.manualCompletedDays)
        assertEquals(18, goal.manualRemainingDays)
        assertEquals(0.4f, goal.manualProgressFraction, 0.001f)
        assertEquals(40, goal.manualProgressPercentage)

        // Circle 2: Automatic Progress must reflect ONLY the 18 days elapsed
        assertEquals(18, goal.getAutomaticCompletedDays(eighteenDaysLater))
        assertEquals(12, goal.getAutomaticRemainingDays(eighteenDaysLater))
        assertEquals(0.6f, goal.getAutomaticProgressFraction(eighteenDaysLater), 0.001f)
        assertEquals(60, goal.getAutomaticProgressPercentage(eighteenDaysLater))

        // Progress Independence Guarantee:
        // 1. Elapsed time does NOT change manualCompletedDays
        val thirtyDaysLater = start + (30L * oneDayMillis)
        assertEquals(12, goal.manualCompletedDays) // STILL 12!

        // 2. Incrementing manual completed count does NOT change automatic completed days
        val checkedInGoal = goal.copy(completedDaysCount = 13)
        assertEquals(13, checkedInGoal.manualCompletedDays)
        assertEquals(18, checkedInGoal.getAutomaticCompletedDays(eighteenDaysLater)) // STILL 18!
    }

    @Test
    fun testSingleProgressBarReflectsModeSemantics() {
        val start = 1_000_000_000_000L
        val oneDayMillis = Goal.EXACT_24_HOURS_MILLIS

        // 1. Manual Goal: Single progress bar MUST reflect valid manual check-ins
        val manualGoal = Goal(
            title = "Manual Tracking Goal",
            durationDays = 30,
            startDateMillis = start,
            completedDaysCount = 10,
            trackingMode = com.example.data.model.TrackingMode.MANUAL
        )

        // Elapsed time (say, 20 days later) must NOT alter manual progress bar
        val twentyDaysLater = start + (20L * oneDayMillis)
        assertEquals(10, manualGoal.getEffectiveCompletedDays(twentyDaysLater))
        assertEquals(20, manualGoal.getEffectiveRemainingDays(twentyDaysLater))
        assertEquals(10f / 30f, manualGoal.getEffectiveProgressFraction(twentyDaysLater), 0.001f)
        assertEquals(33, manualGoal.getEffectiveProgressPercentage(twentyDaysLater))

        // 2. Automatic Goal: Single progress bar MUST reflect elapsed 24-hour periods
        val autoGoal = Goal(
            title = "Automatic Tracking Goal",
            durationDays = 30,
            startDateMillis = start,
            completedDaysCount = 0, // No manual check-in ever done
            trackingMode = com.example.data.model.TrackingMode.AUTOMATIC
        )

        // At 12 days elapsed: Exactly 12/30 completed, 18 remaining
        val twelveDaysLater = start + (12L * oneDayMillis)
        assertEquals(12, autoGoal.getEffectiveCompletedDays(twelveDaysLater))
        assertEquals(18, autoGoal.getEffectiveRemainingDays(twelveDaysLater))
        assertEquals(12f / 30f, autoGoal.getEffectiveProgressFraction(twelveDaysLater), 0.001f)
        assertEquals(40, autoGoal.getEffectiveProgressPercentage(twelveDaysLater))
        assertFalse(autoGoal.isAutomaticCompleted(twelveDaysLater))

        // At 30 days elapsed: Target reached -> 30/30 completed, 0 remaining, 100%
        val thirtyDaysLater = start + (30L * oneDayMillis)
        assertEquals(30, autoGoal.getEffectiveCompletedDays(thirtyDaysLater))
        assertEquals(0, autoGoal.getEffectiveRemainingDays(thirtyDaysLater))
        assertEquals(1.0f, autoGoal.getEffectiveProgressFraction(thirtyDaysLater), 0.001f)
        assertEquals(100, autoGoal.getEffectiveProgressPercentage(thirtyDaysLater))
        assertTrue(autoGoal.isAutomaticCompleted(thirtyDaysLater))

        // 3. Daily Reset Goal: Single progress bar reflects completedDaysCount (check-ins only)
        val dailyGoal = Goal(
            title = "Daily Reset Goal",
            durationDays = 10,
            startDateMillis = start,
            completedDaysCount = 4,
            trackingMode = com.example.data.model.TrackingMode.DAILY_RESET
        )
        assertEquals(4, dailyGoal.getEffectiveCompletedDays(twentyDaysLater))
        assertEquals(6, dailyGoal.getEffectiveRemainingDays(twentyDaysLater))
        assertEquals(0.4f, dailyGoal.getEffectiveProgressFraction(twentyDaysLater), 0.001f)
        assertEquals(40, dailyGoal.getEffectiveProgressPercentage(twentyDaysLater))
    }

    @Test
    fun testDailyResetMidnightBoundaryAndDuplicateDateGuard() {
        val tz = java.util.TimeZone.getTimeZone("Asia/Dhaka")
        fun localMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
            return java.util.Calendar.getInstance(tz).apply {
                set(year, month, day, hour, minute, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        // Monday 08:00 AM -> check-in
        val mon8am = localMillis(2026, java.util.Calendar.NOVEMBER, 16, 8, 0)
        val monKey = Goal.getLocalDateKey(mon8am, tz)
        assertEquals("2026-11-16", monKey)

        val goal = Goal(
            title = "Daily Reset Check",
            durationDays = 30,
            completedDaysCount = 1,
            lastCheckInTimestampMillis = mon8am,
            lastCheckInDateKey = monKey,
            trackingMode = com.example.data.model.TrackingMode.DAILY_RESET
        )

        // Monday 10:00 AM same day -> disabled, remaining cooldown = 14 hours until midnight (NOT 24h)
        val mon10am = localMillis(2026, java.util.Calendar.NOVEMBER, 16, 10, 0)
        assertFalse(goal.isCheckInEligible(mon10am, setOf(monKey), tz))
        assertEquals(14L * 60L * 60L * 1000L, goal.getRemainingCooldownMillis(mon10am, tz))

        // Monday 11:59 PM -> still disabled, 1 minute left
        val mon1159pm = localMillis(2026, java.util.Calendar.NOVEMBER, 16, 23, 59)
        assertFalse(goal.isCheckInEligible(mon1159pm, setOf(monKey), tz))
        assertEquals(60L * 1000L, goal.getRemainingCooldownMillis(mon1159pm, tz))

        // Tuesday 12:00 AM (00:00) -> immediately enabled even though only 16h elapsed!
        val tueMidnight = localMillis(2026, java.util.Calendar.NOVEMBER, 17, 0, 0)
        assertTrue(goal.isCheckInEligible(tueMidnight, setOf(monKey), tz))

        // Late-night case: Tuesday 10:00 PM check-in -> Wednesday 12:00 AM (only 2h elapsed!) -> enabled!
        val tue10pm = localMillis(2026, java.util.Calendar.NOVEMBER, 17, 22, 0)
        val tueKey = Goal.getLocalDateKey(tue10pm, tz)
        val tueCheckedGoal = goal.copy(
            completedDaysCount = 2,
            lastCheckInTimestampMillis = tue10pm,
            lastCheckInDateKey = tueKey
        )
        val wedMidnight = localMillis(2026, java.util.Calendar.NOVEMBER, 18, 0, 0)
        assertFalse(tueCheckedGoal.isCheckInEligible(tue10pm, setOf(monKey, tueKey), tz))
        assertTrue(tueCheckedGoal.isCheckInEligible(wedMidnight, setOf(monKey, tueKey), tz))

        // Changing device date backwards to Monday -> duplicate dateKey blocked!
        assertFalse(tueCheckedGoal.isCheckInEligible(mon8am, setOf(monKey, tueKey), tz))
    }

    @Test
    fun testLightThemePaletteAndPastelPaletteConfigured() {
        val light = com.example.ui.theme.LightSkeuoColors
        assertTrue(light.isLight)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFF7F8FA), light.background)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFFFFF), light.card)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF202B40), light.textPrimary)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF8996A8), light.textSecondary)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF32A85A), light.primaryAccent)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFE4E8ED), light.outline)

        // Balanced 5 pastel colors for Light Theme habit cards
        assertEquals(5, com.example.ui.theme.LightHabitPastels.palette.size)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFDDF3FF), com.example.ui.theme.LightHabitPastels.forTitle("Reading").container)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFE2F8E9), com.example.ui.theme.LightHabitPastels.forTitle("Workout").container)
    }
}
