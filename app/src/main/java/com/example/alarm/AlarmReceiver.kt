package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.example.data.repository.AlarmRepository

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContext = context.applicationContext
        val alarmId = intent.getStringExtra(AlarmConstants.EXTRA_ALARM_ID) ?: return
        val action = intent.action ?: return

        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "NIVO:AlarmReceiverWakeLock"
        )
        wakeLock?.acquire(15_000L)

        try {
            val repository = AlarmRepository.getInstance(appContext)
            repository.reloadFromPrefs()

            when (action) {
                AlarmConstants.ACTION_TRIGGER_ALARM -> {
                    val alarm = repository.getAlarmById(alarmId) ?: return
                    val isSnooze = intent.getBooleanExtra(AlarmConstants.EXTRA_IS_SNOOZE, false)

                    if (!isSnooze) {
                        if (alarm.isRepeating()) {
                            // Keep enabled and immediately schedule the next repeating occurrence
                            AlarmScheduler.scheduleAlarm(appContext, alarm)
                        } else {
                            // One-time standalone alarm turns off after ringing
                            repository.setAlarmEnabled(alarm.id, false)
                        }
                    }

                    val serviceIntent = Intent(appContext, AlarmPlaybackService::class.java).apply {
                        this.action = AlarmConstants.ACTION_TRIGGER_ALARM
                        putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
                    }
                    ContextCompat.startForegroundService(appContext, serviceIntent)
                }

                AlarmConstants.ACTION_SNOOZE_ALARM -> {
                    val alarm = repository.getAlarmById(alarmId)
                    val snoozeMinutes = intent.getIntExtra(
                        AlarmConstants.EXTRA_SNOOZE_MINUTES,
                        alarm?.snoozeMinutes ?: 5
                    )
                    appContext.stopService(Intent(appContext, AlarmPlaybackService::class.java))
                    if (alarm != null) {
                        AlarmScheduler.scheduleSnooze(appContext, alarm, snoozeMinutes)
                    }
                    sendDismissUiBroadcast(appContext, alarmId)
                }

                AlarmConstants.ACTION_DISMISS_ALARM -> {
                    appContext.stopService(Intent(appContext, AlarmPlaybackService::class.java))
                    sendDismissUiBroadcast(appContext, alarmId)
                }
            }
        } finally {
            if (wakeLock?.isHeld == true) {
                wakeLock.release()
            }
        }
    }

    private fun sendDismissUiBroadcast(context: Context, alarmId: String) {
        val closeIntent = Intent(AlarmConstants.ACTION_DISMISS_ALARM).apply {
            setPackage(context.packageName)
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
        }
        context.sendBroadcast(closeIntent)
    }
}
