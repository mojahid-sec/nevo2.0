package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmSound
import com.example.data.model.AlarmType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AlarmRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _alarms = MutableStateFlow<List<AlarmItem>>(emptyList())
    val alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()

    init {
        loadAlarms()
    }

    @Synchronized
    fun reloadFromPrefs() {
        loadAlarms()
    }

    @Synchronized
    private fun loadAlarms() {
        val rawJson = prefs.getString(KEY_ALARMS_JSON, null)
        val loaded = mutableListOf<AlarmItem>()
        if (!rawJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(rawJson)
                val seenIds = HashSet<String>()
                val seenHabitIds = HashSet<String>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.getString("id")
                    if (!seenIds.add(id)) continue

                    val type = AlarmType.fromString(obj.optString("type", "STANDALONE"))
                    val habitId = if (obj.has("habitId") && !obj.isNull("habitId")) {
                        obj.getString("habitId").takeIf { it.isNotBlank() }
                    } else null

                    // Ensure no duplicate Habit alarms exist for the same habitId
                    if (type == AlarmType.HABIT && habitId != null && !seenHabitIds.add(habitId)) {
                        continue
                    }

                    val repeatSet = mutableSetOf<Int>()
                    val daysArr = obj.optJSONArray("repeatDays")
                    if (daysArr != null) {
                        for (d in 0 until daysArr.length()) {
                            val dayVal = daysArr.optInt(d, -1)
                            if (dayVal in 1..7) {
                                repeatSet.add(dayVal)
                            }
                        }
                    }

                    loaded.add(
                        AlarmItem(
                            id = id,
                            title = obj.optString("title", "Alarm"),
                            hour24 = obj.optInt("hour24", 8).coerceIn(0, 23),
                            minute = obj.optInt("minute", 0).coerceIn(0, 59),
                            repeatDays = repeatSet,
                            isEnabled = obj.optBoolean("isEnabled", true),
                            snoozeMinutes = obj.optInt("snoozeMinutes", 5).let {
                                if (it in AlarmItem.SNOOZE_OPTIONS) it else 5
                            },
                            sound = AlarmSound.fromId(obj.optString("sound", AlarmSound.CLASSIC_ALARM.id)),
                            type = type,
                            habitId = habitId,
                            use24HourFormat = obj.optBoolean("use24HourFormat", false),
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _alarms.value = loaded.sortedWith(compareBy<AlarmItem> { it.hour24 }.thenBy { it.minute }.thenBy { it.createdAtMillis })
    }

    @Synchronized
    private fun saveAlarms(list: List<AlarmItem>) {
        val sorted = list.sortedWith(compareBy<AlarmItem> { it.hour24 }.thenBy { it.minute }.thenBy { it.createdAtMillis })
        _alarms.value = sorted
        val array = JSONArray()
        sorted.forEach { alarm ->
            val obj = JSONObject().apply {
                put("id", alarm.id)
                put("title", alarm.title)
                put("hour24", alarm.hour24)
                put("minute", alarm.minute)
                val daysArray = JSONArray()
                alarm.repeatDays.sorted().forEach { daysArray.put(it) }
                put("repeatDays", daysArray)
                put("isEnabled", alarm.isEnabled)
                put("snoozeMinutes", alarm.snoozeMinutes)
                put("sound", alarm.sound.id)
                put("type", alarm.type.name)
                if (alarm.habitId != null) {
                    put("habitId", alarm.habitId)
                }
                put("use24HourFormat", alarm.use24HourFormat)
                put("createdAtMillis", alarm.createdAtMillis)
            }
            array.put(obj)
        }
        // Use commit() so BroadcastReceivers in background/boot see immediate durable writes
        prefs.edit().putString(KEY_ALARMS_JSON, array.toString()).commit()
    }

    fun getAlarmById(id: String): AlarmItem? {
        return _alarms.value.find { it.id == id }
    }

    fun getAlarmForHabit(habitId: String): AlarmItem? {
        return _alarms.value.find { it.type == AlarmType.HABIT && it.habitId == habitId }
    }

    fun getAllEnabledAlarms(): List<AlarmItem> {
        return _alarms.value.filter { it.isEnabled }
    }

    @Synchronized
    fun saveStandaloneAlarm(alarm: AlarmItem): AlarmItem {
        val clean = alarm.copy(
            type = AlarmType.STANDALONE,
            habitId = null,
            title = alarm.title.trim().ifEmpty { "Alarm" },
            snoozeMinutes = if (alarm.snoozeMinutes in AlarmItem.SNOOZE_OPTIONS) alarm.snoozeMinutes else 5
        )
        val current = _alarms.value
        val updated = if (current.any { it.id == clean.id }) {
            current.map { if (it.id == clean.id) clean else it }
        } else {
            current + clean
        }
        saveAlarms(updated)
        return clean
    }

    /**
     * Inserts or updates the single Habit Alarm associated with [habitId].
     * Guarantees no duplicate Habit Alarms are created for the same Habit.
     */
    @Synchronized
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
    ): AlarmItem {
        val existing = getAlarmForHabit(habitId)
        val normalizedDays = if (repeatDays.isEmpty()) AlarmItem.ALL_DAYS else repeatDays
        val updatedAlarm = if (existing != null) {
            existing.copy(
                title = habitTitle.trim().ifEmpty { existing.title },
                hour24 = hour24.coerceIn(0, 23),
                minute = minute.coerceIn(0, 59),
                repeatDays = normalizedDays,
                isEnabled = isEnabled,
                snoozeMinutes = if (snoozeMinutes in AlarmItem.SNOOZE_OPTIONS) snoozeMinutes else 5,
                sound = sound,
                use24HourFormat = use24HourFormat
            )
        } else {
            AlarmItem(
                title = habitTitle.trim().ifEmpty { "Habit Reminder" },
                hour24 = hour24.coerceIn(0, 23),
                minute = minute.coerceIn(0, 59),
                repeatDays = normalizedDays,
                isEnabled = isEnabled,
                snoozeMinutes = if (snoozeMinutes in AlarmItem.SNOOZE_OPTIONS) snoozeMinutes else 5,
                sound = sound,
                type = AlarmType.HABIT,
                habitId = habitId,
                use24HourFormat = use24HourFormat
            )
        }

        val current = _alarms.value.filterNot { it.type == AlarmType.HABIT && it.habitId == habitId && it.id != updatedAlarm.id }
        val nextList = if (current.any { it.id == updatedAlarm.id }) {
            current.map { if (it.id == updatedAlarm.id) updatedAlarm else it }
        } else {
            current + updatedAlarm
        }
        saveAlarms(nextList)
        return updatedAlarm
    }

    /**
     * When a Habit's title is edited without modifying its alarm settings,
     * preserves all alarm settings and updates only the synced title.
     */
    @Synchronized
    fun syncHabitTitle(habitId: String, newHabitTitle: String): AlarmItem? {
        val existing = getAlarmForHabit(habitId) ?: return null
        val updated = existing.copy(title = newHabitTitle.trim().ifEmpty { existing.title })
        val nextList = _alarms.value.map { if (it.id == updated.id) updated else it }
        saveAlarms(nextList)
        return updated
    }

    @Synchronized
    fun setAlarmEnabled(alarmId: String, enabled: Boolean): AlarmItem? {
        val existing = getAlarmById(alarmId) ?: return null
        val updated = existing.copy(isEnabled = enabled)
        val nextList = _alarms.value.map { if (it.id == alarmId) updated else it }
        saveAlarms(nextList)
        return updated
    }

    @Synchronized
    fun deleteAlarm(alarmId: String): AlarmItem? {
        val existing = getAlarmById(alarmId) ?: return null
        saveAlarms(_alarms.value.filterNot { it.id == alarmId })
        return existing
    }

    @Synchronized
    fun deleteAlarmsForHabit(habitId: String): List<AlarmItem> {
        val removed = _alarms.value.filter { it.type == AlarmType.HABIT && it.habitId == habitId }
        if (removed.isNotEmpty()) {
            saveAlarms(_alarms.value.filterNot { it.type == AlarmType.HABIT && it.habitId == habitId })
        }
        return removed
    }

    @Synchronized
    fun clearAllAlarms(): List<AlarmItem> {
        val all = _alarms.value
        saveAlarms(emptyList())
        return all
    }

    companion object {
        private const val PREFS_NAME = "nivo_alarms_prefs"
        private const val KEY_ALARMS_JSON = "alarms_json"

        @Volatile
        private var INSTANCE: AlarmRepository? = null

        fun getInstance(context: Context): AlarmRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AlarmRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
