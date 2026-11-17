package com.example.alarm

object AlarmConstants {
    const val ACTION_TRIGGER_ALARM = "com.example.nivo.alarm.ACTION_TRIGGER_ALARM"
    const val ACTION_DISMISS_ALARM = "com.example.nivo.alarm.ACTION_DISMISS_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.example.nivo.alarm.ACTION_SNOOZE_ALARM"

    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    const val EXTRA_OPEN_ALARMS_TAB = "extra_open_alarms_tab"

    const val CHANNEL_ID_ALARM = "nivo_alarm_ring_channel_v1"
    const val BASE_NOTIFICATION_ID = 42000
}
