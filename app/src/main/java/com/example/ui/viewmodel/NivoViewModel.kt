package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.localization.AppLanguage
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmSound
import com.example.data.model.AlarmType
import com.example.data.model.CheckInRecord
import com.example.data.model.Goal
import com.example.data.repository.AlarmRepository
import com.example.data.repository.GoalRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NivoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GoalRepository.getInstance(application)
    private val alarmRepository = AlarmRepository.getInstance(application)
    val pinManager = com.example.data.security.PinManager.getInstance(application)
    private val checkInMutex = Mutex()

    private val _isPinEnabled = MutableStateFlow(pinManager.isPinEnabled())
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    fun refreshPinStatus() {
        _isPinEnabled.value = pinManager.isPinEnabled()
    }

    val goals: StateFlow<List<Goal>> = repository.goals
    val checkIns: StateFlow<List<CheckInRecord>> = repository.checkIns
    val userName: StateFlow<String> = repository.userName
    val language: StateFlow<AppLanguage> = repository.selectedLanguage
    val selectedTheme: StateFlow<com.example.ui.theme.NivoThemeStyle> = repository.selectedTheme
    val notificationsEnabled: StateFlow<Boolean> = repository.notificationsEnabled
    val sessionQuoteIndex: StateFlow<Int> = repository.sessionQuoteIndex

    // Alarm StateFlows (Mode 1: Standalone, Mode 2: Habit-linked)
    val alarms: StateFlow<List<AlarmItem>> = alarmRepository.alarms
    val standaloneAlarms: StateFlow<List<AlarmItem>> = alarmRepository.alarms
        .map { list -> list.filter { it.type == AlarmType.STANDALONE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val habitAlarms: StateFlow<List<AlarmItem>> = alarmRepository.alarms
        .map { list -> list.filter { it.type == AlarmType.HABIT } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedGoal: StateFlow<Goal?> = repository.goals.combine(repository.goals) { list, _ ->
        list.find { it.isSelected } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current time ticker for precise 24-hour countdowns (updates every second)
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    val goalCompletedEvent = MutableStateFlow<Goal?>(null)

    init {
        // Ensure all persisted enabled alarms are scheduled cleanly on app start
        AlarmScheduler.rescheduleAllEnabledAlarms(application)

        // High-precision 1-second ticker for exact 24-hour timer countdown and automatic completions
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _currentTimeMillis.value = now
                val autoCompleted = repository.checkAndCompleteAutomaticGoals(now)
                autoCompleted.firstOrNull()?.let { goal ->
                    goalCompletedEvent.value = goal
                }
                delay(1000L)
            }
        }
    }

    /**
     * Immediately refreshes the internal clock and evaluates time-dependent state
     * (e.g. midnight calendar-day reset for Daily Reset habits and automatic completions).
     */
    fun refreshClockAndCheckIns(nowMillis: Long = System.currentTimeMillis()) {
        _currentTimeMillis.value = nowMillis
        viewModelScope.launch {
            val autoCompleted = repository.checkAndCompleteAutomaticGoals(nowMillis)
            autoCompleted.firstOrNull()?.let { goal ->
                goalCompletedEvent.value = goal
            }
        }
    }

    /**
     * Checks if selected goal is currently eligible for check-in according to its tracking mode.
     */
    fun isGoalEligibleForCheckIn(goal: Goal?): Boolean {
        if (goal == null) return false
        val recordedDateKeys = checkIns.value
            .asSequence()
            .filter { it.goalId == goal.id }
            .map { it.dateKey }
            .toSet()
        return goal.isCheckInEligible(_currentTimeMillis.value, recordedDateKeys)
    }

    /**
     * Returns remaining countdown in hours, minutes, seconds for check-in cooldown / midnight reset.
     */
    fun getCountdownForGoal(goal: Goal?): Triple<Long, Long, Long> {
        if (goal == null) return Triple(0L, 0L, 0L)
        val remainingMillis = goal.getRemainingCooldownMillis(_currentTimeMillis.value)
        val totalSeconds = remainingMillis / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return Triple(hours, minutes, seconds)
    }

    /**
     * Returns remaining countdown in hours, minutes, seconds until next automatic 24h day completes.
     */
    fun getAutomaticCountdownForGoal(goal: Goal?): Triple<Long, Long, Long> {
        if (goal == null) return Triple(0L, 0L, 0L)
        val remainingMillis = goal.getRemainingMillisUntilNextAutoDay(_currentTimeMillis.value)
        val totalSeconds = remainingMillis / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return Triple(hours, minutes, seconds)
    }

    /**
     * Executes check-in with mutex protection against duplicate taps.
     */
    fun checkInSelectedGoal(onGoalCompleted: ((Goal) -> Unit)? = null) {
        val currentGoal = selectedGoal.value ?: return
        viewModelScope.launch {
            checkInMutex.withLock {
                val now = System.currentTimeMillis()
                val latestGoal = repository.goals.value.find { it.id == currentGoal.id } ?: currentGoal
                val recordedDateKeys = repository.checkIns.value
                    .asSequence()
                    .filter { it.goalId == latestGoal.id }
                    .map { it.dateKey }
                    .toSet()
                if (!latestGoal.isCheckInEligible(now, recordedDateKeys)) {
                    return@launch
                }
                val resultGoal = repository.checkInGoal(latestGoal.id, nowMillis = now)
                if (resultGoal != null) {
                    _currentTimeMillis.value = now
                    if (resultGoal.isCompleted) {
                        goalCompletedEvent.value = resultGoal
                        onGoalCompleted?.invoke(resultGoal)
                    }
                }
            }
        }
    }

    fun createGoal(
        title: String,
        durationDays: Int,
        startDateMillis: Long,
        reason: String,
        trackingMode: com.example.data.model.TrackingMode = com.example.data.model.TrackingMode.MANUAL,
        onSuccess: (Goal) -> Unit
    ) {
        viewModelScope.launch {
            val newGoal = repository.createGoal(
                title = title,
                durationDays = durationDays,
                startDateMillis = startDateMillis,
                reason = reason,
                trackingMode = trackingMode
            )
            onSuccess(newGoal)
        }
    }

    fun updateGoal(
        id: String,
        title: String,
        durationDays: Int,
        startDateMillis: Long,
        reason: String,
        trackingMode: com.example.data.model.TrackingMode = com.example.data.model.TrackingMode.MANUAL,
        onSuccess: (Goal) -> Unit
    ) {
        viewModelScope.launch {
            val updated = repository.updateGoal(
                id = id,
                title = title,
                durationDays = durationDays,
                startDateMillis = startDateMillis,
                reason = reason,
                trackingMode = trackingMode
            )
            if (updated != null) {
                alarmRepository.syncHabitTitle(id, updated.title)
                onSuccess(updated)
            }
        }
    }

    fun selectGoal(goalId: String) {
        viewModelScope.launch {
            repository.selectGoal(goalId)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
            val removedAlarms = alarmRepository.deleteAlarmsForHabit(goalId)
            removedAlarms.forEach { alarm ->
                AlarmScheduler.cancelAlarm(getApplication(), alarm)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Alarm Management Operations (Standalone & Habit-linked)
    // ------------------------------------------------------------------------

    fun getAlarmById(alarmId: String): AlarmItem? = alarmRepository.getAlarmById(alarmId)

    fun getAlarmForHabit(habitId: String): AlarmItem? = alarmRepository.getAlarmForHabit(habitId)

    fun reloadAlarms() {
        alarmRepository.reloadFromPrefs()
    }

    fun saveStandaloneAlarm(alarm: AlarmItem) {
        val saved = alarmRepository.saveStandaloneAlarm(alarm)
        if (saved.isEnabled) {
            AlarmScheduler.scheduleAlarm(getApplication(), saved)
        } else {
            AlarmScheduler.cancelAlarm(getApplication(), saved)
        }
    }

    fun saveHabitAlarm(
        habitId: String,
        habitTitle: String,
        hour24: Int,
        minute: Int,
        repeatDays: Set<Int>,
        isEnabled: Boolean,
        snoozeMinutes: Int,
        sound: AlarmSound,
        use24HourFormat: Boolean
    ) {
        val saved = alarmRepository.saveHabitAlarm(
            habitId = habitId,
            habitTitle = habitTitle,
            hour24 = hour24,
            minute = minute,
            repeatDays = repeatDays,
            isEnabled = isEnabled,
            snoozeMinutes = snoozeMinutes,
            sound = sound,
            use24HourFormat = use24HourFormat
        )
        if (saved.isEnabled) {
            AlarmScheduler.scheduleAlarm(getApplication(), saved)
        } else {
            AlarmScheduler.cancelAlarm(getApplication(), saved)
        }
    }

    fun toggleAlarmEnabled(alarmId: String, enabled: Boolean) {
        val updated = alarmRepository.setAlarmEnabled(alarmId, enabled) ?: return
        if (updated.isEnabled) {
            AlarmScheduler.scheduleAlarm(getApplication(), updated)
        } else {
            AlarmScheduler.cancelAlarm(getApplication(), updated)
        }
    }

    fun deleteAlarm(alarmId: String) {
        val removed = alarmRepository.deleteAlarm(alarmId) ?: return
        AlarmScheduler.cancelAlarm(getApplication(), removed)
    }

    fun rescheduleAllEnabledAlarms() {
        AlarmScheduler.rescheduleAllEnabledAlarms(getApplication())
    }

    fun setLanguage(newLanguage: AppLanguage) {
        viewModelScope.launch {
            repository.updateLanguage(newLanguage)
        }
    }

    fun setTheme(theme: com.example.ui.theme.NivoThemeStyle) {
        viewModelScope.launch {
            repository.updateTheme(theme)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateNotificationsEnabled(enabled)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            repository.updateUserName(name)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetData()
            val cleared = alarmRepository.clearAllAlarms()
            cleared.forEach { alarm ->
                AlarmScheduler.cancelAlarm(getApplication(), alarm)
            }
        }
    }

    fun clearGoalCompletedEvent() {
        goalCompletedEvent.value = null
    }

    fun exportBackupJson(): String = repository.exportBackupJson()

    fun importBackupJson(json: String): Boolean = repository.importBackupJson(json)
}
