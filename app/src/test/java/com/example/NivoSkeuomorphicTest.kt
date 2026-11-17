package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.localization.AppLanguage
import com.example.data.model.Goal
import com.example.data.model.TrackingMode
import com.example.data.repository.AlarmRepository
import com.example.data.repository.GoalRepository
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CreateEditAlarmScreen
import com.example.ui.screens.CreateGoalScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NivoThemeStyle
import com.example.ui.viewmodel.NivoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class NivoSkeuomorphicTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Application
    private lateinit var goalRepository: GoalRepository
    private lateinit var alarmRepository: AlarmRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("nivo_offline_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("nivo_alarms_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        GoalRepository.resetInstanceForTests()
        goalRepository = GoalRepository.getInstance(context)
        alarmRepository = AlarmRepository.getInstance(context)
        alarmRepository.clearAllAlarms()
    }

    @After
    fun tearDown() {
        alarmRepository.clearAllAlarms()
        GoalRepository.resetInstanceForTests()
        Dispatchers.resetMain()
    }

    @Test
    fun testNotificationsSettingAndBackupRestorePersistence() {
        goalRepository.updateUserName("Arif")
        goalRepository.updateLanguage(AppLanguage.BANGLA)
        goalRepository.updateNotificationsEnabled(false)
        val created = goalRepository.createGoal(
            title = "Morning Meditation",
            durationDays = 21,
            startDateMillis = 1_700_000_000_000L,
            reason = "Calm mind",
            trackingMode = TrackingMode.MANUAL
        )

        // Fresh repository instance reads persisted notification preference from SharedPreferences
        val reloadedRepo = GoalRepository(context)
        assertFalse(reloadedRepo.notificationsEnabled.value)
        assertEquals("Arif", reloadedRepo.userName.value)
        assertEquals(AppLanguage.BANGLA, reloadedRepo.selectedLanguage.value)
        assertTrue(reloadedRepo.goals.value.any { it.id == created.id && it.title == "Morning Meditation" })

        // Export JSON -> reset -> restore JSON
        val exportedJson = reloadedRepo.exportBackupJson()
        reloadedRepo.resetData()
        assertTrue(reloadedRepo.goals.value.isEmpty())
        assertTrue(reloadedRepo.notificationsEnabled.value)

        val restored = reloadedRepo.importBackupJson(exportedJson)
        assertTrue(restored)
        assertFalse(reloadedRepo.notificationsEnabled.value)
        assertEquals("Arif", reloadedRepo.userName.value)
        assertEquals(AppLanguage.BANGLA, reloadedRepo.selectedLanguage.value)
        assertTrue(reloadedRepo.goals.value.any { it.id == created.id && it.title == "Morning Meditation" })

        // Backward compatibility: legacy backup JSON containing "selectedTheme" restores cleanly
        val legacyJson = """
            {
              "version": 1,
              "userName": "LegacyUser",
              "language": "en",
              "selectedTheme": "ocean",
              "goals": [],
              "checkIns": []
            }
        """.trimIndent()
        assertTrue(reloadedRepo.importBackupJson(legacyJson))
        assertEquals("LegacyUser", reloadedRepo.userName.value)
        assertEquals(AppLanguage.ENGLISH, reloadedRepo.selectedLanguage.value)
    }

    @Test
    fun testSettingsScreenThemeSelectorAndSupportsDataRestoreDialog() {
        val viewModel = NivoViewModel(context)

        composeTestRule.setContent {
            val selectedTheme by viewModel.selectedTheme.collectAsState()
            MyApplicationTheme(theme = selectedTheme) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAboutDeveloper = {},
                    onNavigateToAlarms = {},
                    onRequestSetPin = {},
                    onRequestDisablePin = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_notifications_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_data_backup_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_data_restore_row").performScrollTo().assertIsDisplayed()

        // Ensure Theme Selector UI is present
        composeTestRule.onAllNodesWithText("Select App Theme", substring = true, ignoreCase = true).assertCountEquals(1)

        val payload = """{"version":2,"userName":"RestoredFromUI","language":"en","notificationsEnabled":false,"goals":[],"checkIns":[]}"""
        assertTrue(viewModel.importBackupJson(payload))

        assertEquals("RestoredFromUI", viewModel.userName.value)
        assertFalse(viewModel.notificationsEnabled.value)
    }

    @Test
    fun testHomeScreenRendersSingleProgressRingAndDigitalClock() {
        val viewModel = NivoViewModel(context)
        goalRepository.createGoal(
            title = "Daily Reading",
            durationDays = 30,
            startDateMillis = System.currentTimeMillis(),
            reason = "Stay sharp",
            trackingMode = TrackingMode.MANUAL
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onCreateGoalClick = {},
                    onNavigateToGoals = {},
                    onNavigateToAlarms = {},
                    onNavigateToCalendar = {},
                    onGoalCompleted = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_digital_clock_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_greeting_text").assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("single_goal_progress_ring").assertCountEquals(1)
    }

    @Test
    fun testGoalsScreenRendersGoalCardsCleanly() {
        val viewModel = NivoViewModel(context)
        val createdGoal = goalRepository.createGoal(
            title = "Evening Run",
            durationDays = 21,
            startDateMillis = System.currentTimeMillis(),
            reason = "Endurance",
            trackingMode = TrackingMode.AUTOMATIC
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                GoalsScreen(
                    viewModel = viewModel,
                    onCreateGoalClick = {},
                    onEditGoalClick = {},
                    onConfigureHabitAlarm = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("goals_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("header_add_goal_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("goal_item_card_${createdGoal.id}").assertIsDisplayed()
    }

    @Test
    fun testDailyResetRepositoryLifecyclePersistenceRebootAndDateChangeGuard() {
        fun localMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
            return java.util.Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        val day1Morning8am = localMillis(2026, java.util.Calendar.NOVEMBER, 16, 8, 0)
        val day1Morning10am = localMillis(2026, java.util.Calendar.NOVEMBER, 16, 10, 0)
        val day2Midnight = localMillis(2026, java.util.Calendar.NOVEMBER, 17, 0, 0)
        val day2Morning8am = localMillis(2026, java.util.Calendar.NOVEMBER, 17, 8, 0)

        val goal = goalRepository.createGoal(
            title = "Daily Reset Habit",
            durationDays = 14,
            startDateMillis = day1Morning8am,
            reason = "Daily consistency",
            trackingMode = TrackingMode.DAILY_RESET
        )

        // 1. User checks in at 8:00 AM
        val firstCheckIn = goalRepository.checkInGoal(goal.id, nowMillis = day1Morning8am)
        assertNotNull(firstCheckIn)
        assertEquals(1, firstCheckIn!!.completedDaysCount)
        assertEquals(Goal.getLocalDateKey(day1Morning8am), firstCheckIn.lastCheckInDateKey)

        // 2. Attempt check-in at 10:00 AM same day -> blocked, remaining < 24h
        val sameDayAttempt = goalRepository.checkInGoal(goal.id, nowMillis = day1Morning10am)
        assertNull(sameDayAttempt)
        assertEquals(1, goalRepository.goals.value.first { it.id == goal.id }.completedDaysCount)
        assertTrue(firstCheckIn.getRemainingCooldownMillis(day1Morning10am) < Goal.EXACT_24_HOURS_MILLIS)

        // 5 & 6. App close/reopen & simulated device reboot before midnight -> still disabled for day 1
        GoalRepository.resetInstanceForTests()
        val repoAfterRebootSameDay = GoalRepository.getInstance(context)
        val reloadedSameDayGoal = repoAfterRebootSameDay.goals.value.first { it.id == goal.id }
        val keysSameDay = repoAfterRebootSameDay.checkIns.value.filter { it.goalId == goal.id }.map { it.dateKey }.toSet()
        assertFalse(reloadedSameDayGoal.isCheckInEligible(day1Morning10am, keysSameDay))

        // 3. Midnight (00:00) on next calendar day -> immediately eligible
        assertTrue(reloadedSameDayGoal.isCheckInEligible(day2Midnight, keysSameDay))

        // 4. Next morning check-in -> succeeds & preserves previous day's check-in history
        val secondCheckIn = repoAfterRebootSameDay.checkInGoal(goal.id, nowMillis = day2Morning8am)
        assertNotNull(secondCheckIn)
        assertEquals(2, secondCheckIn!!.completedDaysCount)
        val allHistoryForGoal = repoAfterRebootSameDay.checkIns.value.filter { it.goalId == goal.id }
        assertEquals(2, allHistoryForGoal.size)
        assertTrue(allHistoryForGoal.any { it.dateKey == Goal.getLocalDateKey(day1Morning8am) })
        assertTrue(allHistoryForGoal.any { it.dateKey == Goal.getLocalDateKey(day2Morning8am) })

        // 7. Device date changed back to Day 1 -> blocked by stored dateKey history
        val backdatedAttempt = repoAfterRebootSameDay.checkInGoal(goal.id, nowMillis = day1Morning10am)
        assertNull(backdatedAttempt)
        assertEquals(2, repoAfterRebootSameDay.goals.value.first { it.id == goal.id }.completedDaysCount)
    }

    @Test
    fun testCreateGoalScreenSelectsDailyResetMode() {
        val viewModel = NivoViewModel(context)
        var navigatedBack = false

        composeTestRule.setContent {
            MyApplicationTheme(theme = NivoThemeStyle.LIGHT) {
                CreateGoalScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navigatedBack = true }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("option_daily_reset_tracking").performScrollTo().assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("goal_title_input").performScrollTo().performTextInput("Daily Gratitude")
        composeTestRule.onNodeWithTag("save_goal_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertTrue(navigatedBack)
        val saved = goalRepository.goals.value.firstOrNull { it.title == "Daily Gratitude" }
        assertNotNull(saved)
        assertEquals(TrackingMode.DAILY_RESET, saved!!.trackingMode)
    }

    @Test
    fun testHomeScreenDailyResetDisablesCheckInButtonImmediatelyInBothThemes() {
        val viewModel = NivoViewModel(context)
        val dailyGoal = goalRepository.createGoal(
            title = "Daily Stretching",
            durationDays = 21,
            startDateMillis = System.currentTimeMillis() - 1000L,
            reason = "Mobility",
            trackingMode = TrackingMode.DAILY_RESET
        )
        goalRepository.selectGoal(dailyGoal.id)
        viewModel.setTheme(NivoThemeStyle.LIGHT)

        composeTestRule.setContent {
            val theme by viewModel.selectedTheme.collectAsState()
            MyApplicationTheme(theme = theme) {
                HomeScreen(
                    viewModel = viewModel,
                    onCreateGoalClick = {},
                    onNavigateToGoals = {},
                    onNavigateToAlarms = {},
                    onNavigateToCalendar = {},
                    onGoalCompleted = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag("single_goal_progress_ring").assertCountEquals(1)
        composeTestRule.onNodeWithTag("check_in_button").performScrollTo().assertIsEnabled().performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, goalRepository.goals.value.first { it.id == dailyGoal.id }.completedDaysCount)
        composeTestRule.onNodeWithTag("check_in_button").performScrollTo().assertIsNotEnabled()

        // Switch live to Dark Red theme and ensure screen & state stay intact
        viewModel.setTheme(NivoThemeStyle.DARK_RED)
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag("single_goal_progress_ring").assertCountEquals(1)
        composeTestRule.onNodeWithTag("check_in_button").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun testLightThemePersistsAndRendersAllPrimaryScreens() {
        val viewModel = NivoViewModel(context)
        viewModel.setTheme(NivoThemeStyle.LIGHT)

        // Fresh repository confirms persistence of Light Theme
        GoalRepository.resetInstanceForTests()
        val freshRepo = GoalRepository.getInstance(context)
        assertEquals(NivoThemeStyle.LIGHT, freshRepo.selectedTheme.value)

        val g = freshRepo.createGoal(
            title = "Read Books",
            durationDays = 10,
            startDateMillis = System.currentTimeMillis(),
            reason = "Focus",
            trackingMode = TrackingMode.DAILY_RESET
        )
        val freshViewModel = NivoViewModel(context)

        composeTestRule.setContent {
            MyApplicationTheme(theme = NivoThemeStyle.LIGHT) {
                GoalsScreen(
                    viewModel = freshViewModel,
                    onCreateGoalClick = {},
                    onEditGoalClick = {},
                    onConfigureHabitAlarm = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("goal_item_card_${g.id}").assertIsDisplayed()
    }

    @Test
    fun testLightThemeCalendarAndAlarmScreensRenderCleanly() {
        val viewModel = NivoViewModel(context)
        goalRepository.createGoal(
            title = "Workout Session",
            durationDays = 15,
            startDateMillis = System.currentTimeMillis(),
            reason = "Strength",
            trackingMode = TrackingMode.MANUAL
        )

        composeTestRule.setContent {
            MyApplicationTheme(theme = NivoThemeStyle.LIGHT) {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                        CalendarScreen(viewModel = viewModel)
                    }
                    androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                        AlarmsScreen(
                            viewModel = viewModel,
                            onCreateStandaloneAlarm = {},
                            onEditAlarm = {},
                            onConfigureHabitAlarm = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("calendar_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("alarms_screen").assertIsDisplayed()
    }

    @Test
    fun testMotivationalQuotesDeckCyclesAllWithoutRepeatingPrevious() {
        val prefs = context.getSharedPreferences("test_quotes_deck_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val total = com.example.data.localization.MotivationalQuotes.quotes.size
        assertTrue(total >= 50)

        // Cycle 1: must draw every single quote once before any repetition
        val cycle1 = (0 until total).map {
            com.example.data.localization.MotivationalQuotes.selectNextSessionQuoteIndex(
                prefs = prefs,
                random = kotlin.random.Random(12345)
            )
        }
        assertEquals(total, cycle1.distinct().size)

        // Cycle 2: must draw every quote again and first of cycle 2 != last of cycle 1
        val cycle2 = (0 until total).map {
            com.example.data.localization.MotivationalQuotes.selectNextSessionQuoteIndex(
                prefs = prefs,
                random = kotlin.random.Random(12345)
            )
        }
        assertEquals(total, cycle2.distinct().size)
        assertTrue("Immediately adjacent cycles must not repeat quote", cycle1.last() != cycle2.first())

        // Verify both English & Bangla translations exist and differ for every quote
        for (i in 0 until total) {
            val en = com.example.data.localization.MotivationalQuotes.getQuoteByIndex(i, AppLanguage.ENGLISH)
            val bn = com.example.data.localization.MotivationalQuotes.getQuoteByIndex(i, AppLanguage.BANGLA)
            assertTrue(en.first.isNotBlank() && en.second.isNotBlank())
            assertTrue(bn.first.isNotBlank() && bn.second.isNotBlank())
            assertTrue(en.first != bn.first)
        }
    }

    @Test
    fun testMotivationalQuoteFreshPerAppLaunchAndStableWithinSession() {
        // Launch 1
        val launch1Vm = NivoViewModel(context)
        val session1Index = launch1Vm.sessionQuoteIndex.value
        val secondGoal = goalRepository.createGoal(
            title = "Second Focus Habit",
            durationDays = 14,
            startDateMillis = System.currentTimeMillis(),
            reason = "Discipline",
            trackingMode = TrackingMode.MANUAL
        )

        val (quoteEn, _) = com.example.data.localization.MotivationalQuotes.getQuoteByIndex(session1Index, AppLanguage.ENGLISH)
        val (quoteBn, _) = com.example.data.localization.MotivationalQuotes.getQuoteByIndex(session1Index, AppLanguage.BANGLA)

        composeTestRule.setContent {
            val theme by launch1Vm.selectedTheme.collectAsState()
            MyApplicationTheme(theme = theme) {
                HomeScreen(
                    viewModel = launch1Vm,
                    onCreateGoalClick = {},
                    onNavigateToGoals = {},
                    onNavigateToAlarms = {},
                    onNavigateToCalendar = {},
                    onGoalCompleted = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("motivational_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithText(quoteEn, substring = true).assertCountEquals(1)

        // Switching selected goal inside same session must NOT change motivational quote index
        launch1Vm.selectGoal(secondGoal.id)
        composeTestRule.waitForIdle()
        assertEquals(session1Index, launch1Vm.sessionQuoteIndex.value)
        composeTestRule.onAllNodesWithText(quoteEn, substring = true).assertCountEquals(1)

        // Switching language inside same session translates the SAME session quote to Bangla
        launch1Vm.setLanguage(AppLanguage.BANGLA)
        composeTestRule.waitForIdle()
        assertEquals(session1Index, launch1Vm.sessionQuoteIndex.value)
        composeTestRule.onAllNodesWithText(quoteBn, substring = true).assertCountEquals(1)

        // Simulating a fresh app launch -> must pick a different quote index
        GoalRepository.resetInstanceForTests()
        val launch2Repo = GoalRepository.getInstance(context)
        assertTrue(
            "Fresh app launch must pick a new quote index instead of repeating $session1Index",
            launch2Repo.sessionQuoteIndex.value != session1Index
        )
    }

    @Test
    fun testHabitEditDeletePinLockAndLanguageSwitchPersistence() {
        val viewModel = NivoViewModel(context)

        // Habit Create -> Edit -> Sync Alarm Title -> Delete
        val created = goalRepository.createGoal(
            title = "Morning Journal",
            durationDays = 21,
            startDateMillis = 1_700_000_000_000L,
            reason = "Clarity",
            trackingMode = TrackingMode.MANUAL
        )
        viewModel.saveHabitAlarm(
            habitId = created.id,
            habitTitle = created.title,
            hour24 = 7,
            minute = 0,
            repeatDays = com.example.data.model.AlarmItem.ALL_DAYS,
            isEnabled = true,
            snoozeMinutes = 5,
            sound = com.example.data.model.AlarmSound.MORNING_RISE,
            use24HourFormat = false
        )

        var updatedTitle = ""
        viewModel.updateGoal(
            id = created.id,
            title = "Evening Journal",
            durationDays = 30,
            startDateMillis = 1_700_000_000_000L,
            reason = "Reflection",
            trackingMode = TrackingMode.DAILY_RESET
        ) { updated ->
            updatedTitle = updated.title
        }
        assertEquals("Evening Journal", updatedTitle)
        val syncedGoal = goalRepository.goals.value.first { it.id == created.id }
        assertEquals(30, syncedGoal.durationDays)
        assertEquals(TrackingMode.DAILY_RESET, syncedGoal.trackingMode)
        assertEquals("Evening Journal", alarmRepository.getAlarmForHabit(created.id)?.title)

        viewModel.deleteGoal(created.id)
        assertFalse(goalRepository.goals.value.any { it.id == created.id })
        assertNull(alarmRepository.getAlarmForHabit(created.id))

        // PIN Lock setup, verify, mismatch, disable
        val pinPrefs = context.getSharedPreferences("nivo_security_prefs", Context.MODE_PRIVATE)
        pinPrefs.edit().clear().commit()
        val pinManager = com.example.data.security.PinManager(context)
        assertFalse(pinManager.isPinEnabled())
        pinManager.setPin("4829")
        assertTrue(pinManager.isPinEnabled())
        assertTrue(pinManager.verifyPin("4829"))
        assertFalse(pinManager.verifyPin("0000"))
        pinManager.disablePin()
        assertFalse(pinManager.isPinEnabled())
        assertFalse(pinManager.verifyPin("4829"))

        // Language switch persistence across fresh repository instances
        viewModel.setLanguage(AppLanguage.BANGLA)
        GoalRepository.resetInstanceForTests()
        val reloaded = GoalRepository.getInstance(context)
        assertEquals(AppLanguage.BANGLA, reloaded.selectedLanguage.value)
    }
}

