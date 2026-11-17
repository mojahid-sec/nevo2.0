package com.example.alarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ServiceCompat
import com.example.data.localization.AppLanguage
import com.example.data.repository.AlarmRepository

class AlarmPlaybackService : Service() {

    private val timeoutHandler = Handler(Looper.getMainLooper())
    private val autoStopRunnable = Runnable {
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == AlarmConstants.ACTION_DISMISS_ALARM) {
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_ID)
        if (alarmId.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val repository = AlarmRepository.getInstance(applicationContext)
        repository.reloadFromPrefs()
        val alarm = repository.getAlarmById(alarmId)
        if (alarm == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val prefs = applicationContext.getSharedPreferences("nivo_offline_prefs", Context.MODE_PRIVATE)
        val language = AppLanguage.fromCode(prefs.getString("app_language", "en") ?: "en")

        val notification = AlarmNotificationHelper.buildAlarmNotification(
            context = applicationContext,
            alarm = alarm,
            language = language
        )

        val notificationId = AlarmConstants.BASE_NOTIFICATION_ID + (alarm.mainRequestCode() % 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(notificationId, notification)
        }

        AlarmSoundPlayer.startAlarmPlayback(applicationContext, alarm.sound)

        // Safety auto-stop after 5 minutes if unacknowledged
        timeoutHandler.removeCallbacks(autoStopRunnable)
        timeoutHandler.postDelayed(autoStopRunnable, AUTO_TIMEOUT_MILLIS)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        timeoutHandler.removeCallbacks(autoStopRunnable)
        AlarmSoundPlayer.stopAlarmPlayback()
        super.onDestroy()
    }

    companion object {
        private const val AUTO_TIMEOUT_MILLIS = 5 * 60_000L
    }
}
