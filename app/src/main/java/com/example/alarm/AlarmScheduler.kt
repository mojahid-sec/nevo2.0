package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.MainActivity
import com.example.data.model.AlarmItem
import com.example.data.repository.AlarmRepository

object AlarmScheduler {

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        return alarmManager.canScheduleExactAlarms()
    }

    /**
     * Schedules the next trigger for [alarm] using AlarmManager.
     * Cancels any existing pending alarm for the same ID first to prevent duplicates.
     * Returns the scheduled epoch milliseconds, or null if alarm is disabled.
     */
    fun scheduleAlarm(
        context: Context,
        alarm: AlarmItem,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        val appContext = context.applicationContext
        // Cancel existing pending intent first to prevent duplicates
        cancelMainPendingIntent(appContext, alarm.mainRequestCode(), alarm.id)

        if (!alarm.isEnabled) {
            return null
        }

        val triggerAtMillis = alarm.computeNextTriggerMillis(nowMillis)
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return null

        val triggerPendingIntent = buildTriggerPendingIntent(
            context = appContext,
            alarmId = alarm.id,
            requestCode = alarm.mainRequestCode(),
            isSnooze = false
        )

        scheduleExactInternal(appContext, alarmManager, triggerAtMillis, triggerPendingIntent)
        return triggerAtMillis
    }

    /**
     * Schedules a snooze trigger [snoozeMinutes] from [nowMillis].
     */
    fun scheduleSnooze(
        context: Context,
        alarm: AlarmItem,
        snoozeMinutes: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val appContext = context.applicationContext
        val safeMinutes = snoozeMinutes.coerceIn(1, 60)
        val triggerAtMillis = nowMillis + (safeMinutes * 60_000L)
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            ?: return triggerAtMillis

        val snoozePendingIntent = buildTriggerPendingIntent(
            context = appContext,
            alarmId = alarm.id,
            requestCode = alarm.snoozeRequestCode(),
            isSnooze = true
        )

        scheduleExactInternal(appContext, alarmManager, triggerAtMillis, snoozePendingIntent)
        return triggerAtMillis
    }

    private fun scheduleExactInternal(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        operation: PendingIntent
    ) {
        try {
            if (canScheduleExactAlarms(context)) {
                val showIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(AlarmConstants.EXTRA_OPEN_ALARMS_TAB, true)
                }
                val showPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                // setAlarmClock wakes Doze mode reliably and displays system alarm indicator
                val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
                alarmManager.setAlarmClock(info, operation)
            } else {
                // Fallback when exact alarm permission is not yet granted on Android 12+
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    operation
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                operation
            )
        }
    }

    fun cancelAlarm(context: Context, alarm: AlarmItem) {
        val appContext = context.applicationContext
        cancelMainPendingIntent(appContext, alarm.mainRequestCode(), alarm.id)
        cancelSnoozePendingIntent(appContext, alarm.snoozeRequestCode(), alarm.id)
    }

    private fun cancelMainPendingIntent(context: Context, requestCode: Int, alarmId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_TRIGGER_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmConstants.EXTRA_IS_SNOOZE, false)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    private fun cancelSnoozePendingIntent(context: Context, requestCode: Int, alarmId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_TRIGGER_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmConstants.EXTRA_IS_SNOOZE, true)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pi != null) {
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    private fun buildTriggerPendingIntent(
        context: Context,
        alarmId: String,
        requestCode: Int,
        isSnooze: Boolean
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_TRIGGER_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmConstants.EXTRA_IS_SNOOZE, isSnooze)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Restores all enabled Standalone and Habit alarms (called on BOOT_COMPLETED,
     * time/timezone changes, app updates, or app launch).
     */
    fun rescheduleAllEnabledAlarms(
        context: Context,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        val appContext = context.applicationContext
        val repository = AlarmRepository.getInstance(appContext)
        repository.reloadFromPrefs()
        val enabled = repository.getAllEnabledAlarms()
        enabled.forEach { alarm ->
            scheduleAlarm(appContext, alarm, nowMillis)
        }
    }
}
