package com.example

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.AlarmConstants
import com.example.alarm.AlarmPlaybackService
import com.example.alarm.AlarmReceiver
import com.example.alarm.AlarmScheduler
import com.example.alarm.AlarmSoundPlayer
import com.example.alarm.BootReceiver
import com.example.data.localization.AppLanguage
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmSound
import com.example.data.model.AlarmType
import com.example.data.model.TrackingMode
import com.example.data.repository.AlarmRepository
import com.example.data.repository.GoalRepository
import com.example.ui.components.AlarmConfigSection
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NivoViewModel
import org.junit.Rule
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.util.DataSource
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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NivoAlarmTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Application
    private lateinit var alarmRepository: AlarmRepository
    private lateinit var goalRepository: GoalRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("nivo_alarms_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("nivo_offline_prefs", Context.MODE_PRIVATE).edit().clear().commit()

        AlarmSoundPlayer.stopPreview()
        AlarmSoundPlayer.stopAlarmPlayback()

        alarmRepository = AlarmRepository.getInstance(context)
        alarmRepository.clearAllAlarms()

        goalRepository = GoalRepository.getInstance(context)
        goalRepository.resetData()
    }

    @After
    fun tearDown() {
        AlarmSoundPlayer.stopPreview()
        AlarmSoundPlayer.stopAlarmPlayback()
        alarmRepository.clearAllAlarms()
        Dispatchers.resetMain()
    }

    @Test
    fun testTenOfflineAlarmSoundsExistAndAreValidResources() {
        val sounds = AlarmSound.values()
        assertEquals(10, sounds.size)
        sounds.forEach { sound ->
            assertTrue(sound.englishName.isNotBlank())
            assertTrue(sound.banglaName.isNotBlank())
            assertEquals(sound, AlarmSound.fromId(sound.id))
            val afd = context.resources.openRawResourceFd(sound.rawResId)
            assertNotNull("Raw resource missing for ${sound.englishName}", afd)
            assertTrue("Raw resource empty for ${sound.englishName}", afd.length > 0)
            afd.close()
        }
    }

    @Test
    fun testStandaloneAlarmCreationTogglePersistenceAndFormatting() {
        val alarm = AlarmItem(
            id = "standalone-1",
            title = "Morning Run",
            hour24 = 18,
            minute = 30,
            repeatDays = emptySet(),
            isEnabled = true,
            snoozeMinutes = 10,
            sound = AlarmSound.MORNING_RISE,
            type = AlarmType.STANDALONE,
            use24HourFormat = false
        )
        assertEquals("06:30 PM", alarm.formattedTime(AppLanguage.ENGLISH, force24Hour = false))
        assertEquals("18:30", alarm.formattedTime(AppLanguage.ENGLISH, force24Hour = true))

        val saved = alarmRepository.saveStandaloneAlarm(alarm)
        assertTrue(saved.isEnabled)
        AlarmScheduler.scheduleAlarm(context, saved)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertEquals(1, shadowAlarmManager.scheduledAlarms.size)

        // Disable alarm -> must cancel scheduled alarm
        val disabled = alarmRepository.setAlarmEnabled(saved.id, false)!!
        assertFalse(disabled.isEnabled)
        AlarmScheduler.cancelAlarm(context, disabled)
        assertEquals(0, shadowAlarmManager.scheduledAlarms.size)

        // Reload from SharedPreferences -> persists disabled state & settings
        alarmRepository.reloadFromPrefs()
        val reloaded = alarmRepository.getAlarmById("standalone-1")
        assertNotNull(reloaded)
        assertFalse(reloaded!!.isEnabled)
        assertEquals(10, reloaded.snoozeMinutes)
        assertEquals(AlarmSound.MORNING_RISE, reloaded.sound)
    }

    @Test
    fun testRepeatDaysCalculationAndSnoozeScheduling() {
        // Base time: Wednesday 2026-11-11 08:00:00
        val baseCal = Calendar.getInstance().apply {
            set(2026, Calendar.NOVEMBER, 11, 8, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val alarm = AlarmItem(
            id = "repeat-1",
            title = "Weekday Focus",
            hour24 = 7,
            minute = 30,
            repeatDays = setOf(Calendar.MONDAY, Calendar.FRIDAY),
            isEnabled = true,
            snoozeMinutes = 15,
            sound = AlarmSound.BELL_ALARM
        )
        val nextTrigger = alarm.computeNextTriggerMillis(baseCal.timeInMillis)
        val nextCal = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(Calendar.FRIDAY, nextCal.get(Calendar.DAY_OF_WEEK))
        assertEquals(7, nextCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, nextCal.get(Calendar.MINUTE))

        // Verify snooze scheduling for 5, 10, 15 minutes
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        listOf(5, 10, 15).forEach { snoozeMin ->
            val snoozeAt = AlarmScheduler.scheduleSnooze(context, alarm, snoozeMin, baseCal.timeInMillis)
            assertEquals(baseCal.timeInMillis + snoozeMin * 60_000L, snoozeAt)
        }
        // Snooze uses a single snoozeRequestCode per alarm, so no duplicates accumulate
        assertEquals(1, shadowAlarmManager.scheduledAlarms.size)
    }

    @Test
    fun testHabitCreationWithAndWithoutAlarmAndNoDuplicates() {
        val viewModel = NivoViewModel(context)

        // 1. Habit created WITHOUT alarm
        var habitWithoutAlarmId: String? = null
        viewModel.createGoal(
            title = "Read Books",
            durationDays = 21,
            startDateMillis = System.currentTimeMillis(),
            reason = "Knowledge",
            trackingMode = TrackingMode.MANUAL
        ) { goal ->
            habitWithoutAlarmId = goal.id
        }
        assertNotNull(habitWithoutAlarmId)
        assertNull(viewModel.getAlarmForHabit(habitWithoutAlarmId!!))

        // 2. Habit created WITH alarm
        var habitWithAlarmId: String? = null
        viewModel.createGoal(
            title = "Morning Meditation",
            durationDays = 30,
            startDateMillis = System.currentTimeMillis(),
            reason = "Clarity",
            trackingMode = TrackingMode.MANUAL
        ) { goal ->
            habitWithAlarmId = goal.id
            viewModel.saveHabitAlarm(
                habitId = goal.id,
                habitTitle = goal.title,
                hour24 = 6,
                minute = 15,
                repeatDays = AlarmItem.ALL_DAYS,
                isEnabled = true,
                snoozeMinutes = 5,
                sound = AlarmSound.GENTLE_WAKE,
                use24HourFormat = false
            )
        }

        val habitAlarm = viewModel.getAlarmForHabit(habitWithAlarmId!!)
        assertNotNull(habitAlarm)
        assertTrue(habitAlarm!!.isEnabled)
        assertEquals(6, habitAlarm.hour24)
        assertEquals(15, habitAlarm.minute)
        assertEquals(AlarmSound.GENTLE_WAKE, habitAlarm.sound)

        // Updating the same habit alarm must NOT create duplicate Habit Alarms
        viewModel.saveHabitAlarm(
            habitId = habitWithAlarmId!!,
            habitTitle = "Morning Meditation",
            hour24 = 6,
            minute = 45,
            repeatDays = AlarmItem.WEEKDAYS,
            isEnabled = true,
            snoozeMinutes = 10,
            sound = AlarmSound.NATURE_MORNING,
            use24HourFormat = true
        )
        assertEquals(1, alarmRepository.alarms.value.count { it.habitId == habitWithAlarmId })
        val updatedAlarm = viewModel.getAlarmForHabit(habitWithAlarmId!!)!!
        assertEquals(6, updatedAlarm.hour24)
        assertEquals(45, updatedAlarm.minute)
        assertEquals(AlarmItem.WEEKDAYS, updatedAlarm.repeatDays)
        assertEquals(AlarmSound.NATURE_MORNING, updatedAlarm.sound)
        assertEquals(10, updatedAlarm.snoozeMinutes)
        assertTrue(updatedAlarm.use24HourFormat)
    }

    @Test
    fun testHabitCompletionDoesNotDisableHabitAlarmAndDeletionCancelsOnlyItsAlarm() {
        val viewModel = NivoViewModel(context)

        // Create a 2-day habit with an enabled alarm
        val habit = goalRepository.createGoal(
            title = "2-Day Sprint",
            durationDays = 2,
            startDateMillis = System.currentTimeMillis() - (48L * 60 * 60 * 1000L),
            reason = "Test",
            trackingMode = TrackingMode.AUTOMATIC
        )
        viewModel.saveHabitAlarm(
            habitId = habit.id,
            habitTitle = habit.title,
            hour24 = 9,
            minute = 0,
            repeatDays = AlarmItem.ALL_DAYS,
            isEnabled = true,
            snoozeMinutes = 5,
            sound = AlarmSound.CLASSIC_ALARM,
            use24HourFormat = false
        )

        // Also create an independent standalone alarm
        val standalone = alarmRepository.saveStandaloneAlarm(
            AlarmItem(
                id = "sa-keep",
                title = "Independent Alarm",
                hour24 = 21,
                minute = 0,
                repeatDays = AlarmItem.ALL_DAYS,
                isEnabled = true
            )
        )
        AlarmScheduler.scheduleAlarm(context, standalone)

        // Complete the habit
        val completedList = goalRepository.checkAndCompleteAutomaticGoals(System.currentTimeMillis())
        assertEquals(1, completedList.size)
        assertTrue(completedList.first().isCompleted)

        // Rule: Completing a Habit MUST NOT disable its alarm!
        val alarmAfterCompletion = alarmRepository.getAlarmForHabit(habit.id)
        assertNotNull(alarmAfterCompletion)
        assertTrue("Habit alarm must remain enabled after Habit completion", alarmAfterCompletion!!.isEnabled)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertEquals(2, shadowAlarmManager.scheduledAlarms.size)

        // Rule: Deleting the Habit cancels ONLY its associated alarm
        viewModel.deleteGoal(habit.id)
        assertNull(alarmRepository.getAlarmForHabit(habit.id))
        assertNotNull(alarmRepository.getAlarmById("sa-keep"))
        assertTrue(alarmRepository.getAlarmById("sa-keep")!!.isEnabled)
        assertEquals(1, shadowAlarmManager.scheduledAlarms.size)
    }

    @Test
    fun testMultipleAlarmsIndependentToggleAndBootRestore() {
        val a1 = alarmRepository.saveStandaloneAlarm(
            AlarmItem(id = "a1", title = "A1", hour24 = 6, minute = 0, repeatDays = AlarmItem.ALL_DAYS, isEnabled = true)
        )
        val a2 = alarmRepository.saveStandaloneAlarm(
            AlarmItem(id = "a2", title = "A2", hour24 = 7, minute = 0, repeatDays = AlarmItem.ALL_DAYS, isEnabled = true)
        )
        val h1 = alarmRepository.saveHabitAlarm(
            habitId = "habit-1",
            habitTitle = "H1",
            hour24 = 8,
            minute = 0,
            repeatDays = AlarmItem.ALL_DAYS,
            isEnabled = true,
            snoozeMinutes = 5,
            sound = AlarmSound.DIGITAL_BEEP,
            use24HourFormat = false
        )

        AlarmScheduler.rescheduleAllEnabledAlarms(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)
        assertEquals(3, shadowAlarmManager.scheduledAlarms.size)

        // Disable only a1 -> a2 and h1 stay enabled & scheduled
        alarmRepository.setAlarmEnabled(a1.id, false)
        AlarmScheduler.cancelAlarm(context, a1)
        assertEquals(2, shadowAlarmManager.scheduledAlarms.size)
        assertTrue(alarmRepository.getAlarmById(a2.id)!!.isEnabled)
        assertTrue(alarmRepository.getAlarmById(h1.id)!!.isEnabled)

        // Simulate device reboot (OS clears scheduled alarms from AlarmManager, then fires BOOT_COMPLETED)
        AlarmScheduler.cancelAlarm(context, a2)
        AlarmScheduler.cancelAlarm(context, h1)
        assertEquals(0, shadowAlarmManager.scheduledAlarms.size)

        val bootReceiver = BootReceiver()
        bootReceiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
        // Enabled alarms (a2 & h1) must be restored; disabled alarm (a1) stays unscheduled
        assertEquals(2, shadowAlarmManager.scheduledAlarms.size)
    }

    @Test
    fun testAlarmReceiverOneTimeVsRepeatingBehaviorWhenAppClosed() {
        val oneTime = alarmRepository.saveStandaloneAlarm(
            AlarmItem(id = "once-1", title = "Once", hour24 = 10, minute = 0, repeatDays = emptySet(), isEnabled = true)
        )
        val repeating = alarmRepository.saveStandaloneAlarm(
            AlarmItem(id = "rep-1", title = "Repeat", hour24 = 11, minute = 0, repeatDays = AlarmItem.ALL_DAYS, isEnabled = true)
        )

        val receiver = AlarmReceiver()

        // Trigger one-time alarm -> auto-disables after trigger
        receiver.onReceive(
            context,
            Intent(AlarmConstants.ACTION_TRIGGER_ALARM).apply {
                putExtra(AlarmConstants.EXTRA_ALARM_ID, oneTime.id)
                putExtra(AlarmConstants.EXTRA_IS_SNOOZE, false)
            }
        )
        assertFalse(alarmRepository.getAlarmById(oneTime.id)!!.isEnabled)

        // Trigger repeating alarm -> stays enabled and reschedules next occurrence
        receiver.onReceive(
            context,
            Intent(AlarmConstants.ACTION_TRIGGER_ALARM).apply {
                putExtra(AlarmConstants.EXTRA_ALARM_ID, repeating.id)
                putExtra(AlarmConstants.EXTRA_IS_SNOOZE, false)
            }
        )
        assertTrue(alarmRepository.getAlarmById(repeating.id)!!.isEnabled)
    }

    private fun registerShadowMediaInfos() {
        AlarmSound.values().forEach { sound ->
            val uri = Uri.parse("android.resource://${context.packageName}/${sound.rawResId}")
            val ds = DataSource.toDataSource(context, uri)
            ShadowMediaPlayer.addMediaInfo(ds, ShadowMediaPlayer.MediaInfo(60_000, 0))
        }
    }

    @Test
    fun testAllTenAlarmSoundPreviewsStartSwitchStopAndStayIsolatedFromScheduledAlarms() {
        registerShadowMediaInfos()
        var createCount = 0
        ShadowMediaPlayer.setCreateListener { _, _ ->
            createCount++
        }

        val scheduled = alarmRepository.saveStandaloneAlarm(
            AlarmItem(
                id = "keep-scheduled",
                title = "Scheduled",
                hour24 = 7,
                minute = 0,
                repeatDays = AlarmItem.ALL_DAYS,
                isEnabled = true
            )
        )
        AlarmScheduler.scheduleAlarm(context, scheduled)
        val shadowAlarmManager = shadowOf(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
        assertEquals(1, shadowAlarmManager.scheduledAlarms.size)

        // Play all 10 sounds sequentially; each one should release the previous preview player
        AlarmSound.values().forEach { sound ->
            var errorMessage: String? = null
            AlarmSoundPlayer.startPreview(
                context = context,
                sound = sound,
                onError = { errorMessage = it }
            )
            assertNull("Preview failed for ${sound.englishName}: $errorMessage", errorMessage)
            assertTrue("Expected preview playing for ${sound.englishName}", AlarmSoundPlayer.isPreviewPlaying())
        }

        // Stop preview explicitly
        AlarmSoundPlayer.stopPreview()
        assertFalse(AlarmSoundPlayer.isPreviewPlaying())

        // Scheduled alarm in AlarmManager must be untouched
        assertEquals(1, shadowAlarmManager.scheduledAlarms.size)
        assertEquals(10, createCount)
    }

    @Test
    fun testAlarmConfigSectionPreviewButtonTogglesAndStopsOnDispose() {
        registerShadowMediaInfos()

        var showConfig by mutableStateOf(true)
        var selectedSound by mutableStateOf(AlarmSound.CLASSIC_ALARM)

        composeTestRule.setContent {
            MyApplicationTheme {
                if (showConfig) {
                    androidx.compose.foundation.layout.Column(
                        modifier = androidx.compose.ui.Modifier.verticalScroll(
                            androidx.compose.foundation.rememberScrollState()
                        )
                    ) {
                        AlarmConfigSection(
                            hour24 = 7,
                            minute = 30,
                            use24HourFormat = false,
                            repeatDays = AlarmItem.ALL_DAYS,
                            snoozeMinutes = 5,
                            selectedSound = selectedSound,
                            isHabitAlarm = false,
                            language = AppLanguage.ENGLISH,
                            onTimeChange = { _, _ -> },
                            onFormatChange = {},
                            onRepeatDaysChange = {},
                            onSnoozeChange = {},
                            onSoundChange = { selectedSound = it }
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        val firstSound = AlarmSound.BELL_ALARM
        composeTestRule.onNodeWithTag("preview_sound_${firstSound.id}")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(AlarmSoundPlayer.isPreviewPlaying())
        composeTestRule.onNodeWithContentDescription("Stop").assertIsDisplayed()

        // Clicking same preview button again stops playback
        composeTestRule.onNodeWithTag("preview_sound_${firstSound.id}").performClick()
        composeTestRule.waitForIdle()
        assertFalse(AlarmSoundPlayer.isPreviewPlaying())

        // Start another sound preview, then remove screen -> onDispose must stop playback
        composeTestRule.onNodeWithTag("preview_sound_${AlarmSound.URGENT_ALARM.id}")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(AlarmSoundPlayer.isPreviewPlaying())

        showConfig = false
        composeTestRule.waitForIdle()
        assertFalse(AlarmSoundPlayer.isPreviewPlaying())
    }

    @Test
    fun testAllTenNormalizedAudioFilesHaveConsistentLoudnessAndZeroClipping() {
        val activeRmsDbList = mutableListOf<Double>()

        AlarmSound.values().forEach { sound ->
            context.resources.openRawResource(sound.rawResId).use { stream ->
                val bytes = stream.readBytes()
                // Standard 44-byte WAV header + 16-bit PCM little-endian samples
                assertTrue("Invalid WAV size for ${sound.englishName}", bytes.size > 44)
                val header = String(bytes, 0, 4)
                assertEquals("RIFF", header)

                val sampleCount = (bytes.size - 44) / 2
                val samples = ShortArray(sampleCount)
                for (i in 0 until sampleCount) {
                    val lo = bytes[44 + i * 2].toInt() and 0xFF
                    val hi = bytes[44 + i * 2 + 1].toInt()
                    samples[i] = ((hi shl 8) or lo).toShort()
                }

                // Verify zero hard-clipped samples (>= 32767 or <= -32767)
                var maxAbs = 0
                for (s in samples) {
                    val absVal = kotlin.math.abs(s.toInt())
                    if (absVal > maxAbs) maxAbs = absVal
                }
                assertTrue("Sample clipped in ${sound.englishName}: peak=$maxAbs", maxAbs < 32767)
                assertTrue("Peak too quiet in ${sound.englishName}: peak=$maxAbs", maxAbs >= 20000)

                // Compute 20ms active-window RMS
                val win = 441 // 20ms at 22050Hz
                val winRms = mutableListOf<Double>()
                var idx = 0
                while (idx < sampleCount) {
                    val end = minOf(sampleCount, idx + win)
                    var sumSq = 0.0
                    for (j in idx until end) {
                        val v = samples[j].toDouble()
                        sumSq += v * v
                    }
                    winRms.add(kotlin.math.sqrt(sumSq / (end - idx)))
                    idx += win
                }
                val maxWinRms = winRms.maxOrNull() ?: 0.0
                val active = winRms.filter { it > maxWinRms * 0.2 }
                val activeRms = kotlin.math.sqrt(active.sumOf { it * it } / active.size)
                val activeDb = 20.0 * kotlin.math.log10(activeRms / 32767.0)
                activeRmsDbList.add(activeDb)
                assertTrue("Active loudness too low for ${sound.englishName}: ${activeDb}dBFS", activeDb >= -10.0)
            }
        }

        val spreadDb = (activeRmsDbList.maxOrNull() ?: 0.0) - (activeRmsDbList.minOrNull() ?: 0.0)
        assertTrue("Loudness spread across sounds too wide: ${spreadDb}dB", spreadDb <= 3.5)
    }

    @Test
    fun testActualAlarmPlaybackUsesUsageAlarmUnityVolumeAndServiceLifecycle() {
        registerShadowMediaInfos()

        // Verify distinct stream attributes: USAGE_ALARM for alarms, USAGE_MEDIA for previews
        assertEquals(AudioAttributes.USAGE_ALARM, AlarmSoundPlayer.alarmAudioAttributes.usage)
        assertEquals(AudioAttributes.CONTENT_TYPE_SONIFICATION, AlarmSoundPlayer.alarmAudioAttributes.contentType)
        assertEquals(AudioAttributes.USAGE_MEDIA, AlarmSoundPlayer.previewAudioAttributes.usage)
        assertEquals(AudioAttributes.CONTENT_TYPE_MUSIC, AlarmSoundPlayer.previewAudioAttributes.contentType)

        val capturedPlayers = mutableListOf<Pair<android.media.MediaPlayer, ShadowMediaPlayer>>()
        ShadowMediaPlayer.setCreateListener { player, shadow ->
            capturedPlayers.add(player to shadow)
        }

        // Verify all 10 sounds play cleanly through startAlarmPlayback at unity volume (1.0f, 1.0f)
        AlarmSound.values().forEach { sound ->
            AlarmSoundPlayer.startAlarmPlayback(context, sound)
            assertTrue("Expected alarm playing for ${sound.englishName}", AlarmSoundPlayer.isAlarmPlaying())
            val (lastPlayer, lastShadow) = capturedPlayers.last()
            assertEquals(1.0f, lastShadow.leftVolume, 0.001f)
            assertEquals(1.0f, lastShadow.rightVolume, 0.001f)
            assertTrue("Expected looping enabled for ${sound.englishName}", lastPlayer.isLooping)
        }
        AlarmSoundPlayer.stopAlarmPlayback()
        assertFalse(AlarmSoundPlayer.isAlarmPlaying())

        // Verify scheduled alarm service start/stop respects playback lifecycle without altering system alarm volume
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val initialAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        val alarm = alarmRepository.saveStandaloneAlarm(
            AlarmItem(
                id = "service-ring-1",
                title = "Wake Up",
                hour24 = 6,
                minute = 30,
                repeatDays = AlarmItem.ALL_DAYS,
                isEnabled = true,
                sound = AlarmSound.MORNING_RISE
            )
        )

        val controller = org.robolectric.Robolectric.buildService(
            AlarmPlaybackService::class.java,
            Intent(context, AlarmPlaybackService::class.java).apply {
                putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
            }
        ).create().startCommand(0, 0)

        assertTrue(AlarmSoundPlayer.isAlarmPlaying())
        assertEquals(initialAlarmVolume, audioManager.getStreamVolume(AudioManager.STREAM_ALARM))

        controller.destroy()
        assertFalse(AlarmSoundPlayer.isAlarmPlaying())
    }
}
