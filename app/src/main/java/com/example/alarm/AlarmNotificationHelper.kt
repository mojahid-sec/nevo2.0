package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmType
import com.example.ui.screens.AlarmRingActivity

object AlarmNotificationHelper {

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val existing = nm.getNotificationChannel(AlarmConstants.CHANNEL_ID_ALARM)
            if (existing == null) {
                val channel = NotificationChannel(
                    AlarmConstants.CHANNEL_ID_ALARM,
                    "NIVO Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "High-priority notifications for NIVO Standalone and Habit Alarms"
                    enableVibration(false) // AlarmSoundPlayer manages alarm vibration pattern
                    setSound(null, null)   // AlarmSoundPlayer plays selected alarm audio on USAGE_ALARM
                    setBypassDnd(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    fun canUseFullScreenIntent(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        return nm.canUseFullScreenIntent()
    }

    fun buildAlarmNotification(
        context: Context,
        alarm: AlarmItem,
        language: AppLanguage
    ): Notification {
        ensureNotificationChannel(context)

        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarm.mainRequestCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_DISMISS_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.mainRequestCode() + 10,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_SNOOZE_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, alarm.snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.mainRequestCode() + 20,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val typeLabel = if (alarm.type == AlarmType.HABIT) {
            NivoStrings.habitAlarmLabel(language)
        } else {
            NivoStrings.standaloneAlarmLabel(language)
        }

        val snoozeLabel = NivoStrings.snoozeMinutesButton(language, alarm.snoozeMinutes)
        val dismissLabel = NivoStrings.dismissAlarmButton(language)

        return NotificationCompat.Builder(context, AlarmConstants.CHANNEL_ID_ALARM)
            .setSmallIcon(R.drawable.ic_nivo_ribbon)
            .setContentTitle(alarm.title)
            .setContentText("$typeLabel • ${alarm.formattedTime(language)}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, snoozeLabel, snoozePendingIntent)
            .addAction(0, dismissLabel, dismissPendingIntent)
            .build()
    }
}
