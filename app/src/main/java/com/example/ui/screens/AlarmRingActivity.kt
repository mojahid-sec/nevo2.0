package com.example.ui.screens

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.alarm.AlarmConstants
import com.example.alarm.AlarmReceiver
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmType
import com.example.data.repository.AlarmRepository
import com.example.ui.components.NivoButton
import com.example.ui.components.NivoLogo
import com.example.ui.components.SkeuoRecessedWell
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkRed
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NivoThemeStyle
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverAccent

class AlarmRingActivity : ComponentActivity() {

    private var currentAlarmId: String? = null
    private val dismissReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val targetId = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_ID)
            if (targetId == null || targetId == currentAlarmId) {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_ALARM
        enableEdgeToEdge()
        configureLockScreenBehavior()

        currentAlarmId = intent.getStringExtra(AlarmConstants.EXTRA_ALARM_ID)
        val alarmRepo = AlarmRepository.getInstance(applicationContext)
        alarmRepo.reloadFromPrefs()
        val alarm = currentAlarmId?.let { alarmRepo.getAlarmById(it) }

        val prefs = getSharedPreferences("nivo_offline_prefs", Context.MODE_PRIVATE)
        val language = AppLanguage.fromCode(prefs.getString("app_language", "en") ?: "en")
        val themeStyle = NivoThemeStyle.fromId(prefs.getString("app_theme_style", "dark_red"))

        ContextCompat.registerReceiver(
            this,
            dismissReceiver,
            IntentFilter(AlarmConstants.ACTION_DISMISS_ALARM),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        setContent {
            MyApplicationTheme(theme = themeStyle) {
                AlarmRingScreen(
                    alarm = alarm,
                    language = language,
                    onDismiss = {
                        sendAlarmAction(AlarmConstants.ACTION_DISMISS_ALARM, 0)
                        finish()
                    },
                    onSnooze = { snoozeMinutes ->
                        sendAlarmAction(AlarmConstants.ACTION_SNOOZE_ALARM, snoozeMinutes)
                        finish()
                    }
                )
            }
        }
    }

    private fun configureLockScreenBehavior() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun sendAlarmAction(actionName: String, snoozeMinutes: Int) {
        val alarmId = currentAlarmId ?: return
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = actionName
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(dismissReceiver)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }
}

@Composable
private fun AlarmRingScreen(
    alarm: AlarmItem?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSnooze: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alarm_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val isHabit = alarm?.type == AlarmType.HABIT
    val defaultSnooze = alarm?.snoozeMinutes ?: 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        NivoLogo(
            iconSize = 42.dp,
            showTagline = false,
            language = language
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val isLight = LocalNivoColors.current.isLight
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isLight) Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color.White))
                        else Brush.verticalGradient(listOf(Color(0xFF3E181D), Color(0xFF210D10)))
                    )
                    .border(1.dp, AccentRed.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isHabit) NivoStrings.habitAlarmLabel(language) else NivoStrings.standaloneAlarmLabel(language),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentRed
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .size(116.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLight) {
                            Brush.radialGradient(listOf(Color(0xFFE2F8E9), Color.White, RecessedWell))
                        } else {
                            Brush.radialGradient(listOf(PrimaryRed.copy(alpha = 0.35f), DarkRed.copy(alpha = 0.25f), Color.Black))
                        }
                    )
                    .border(2.dp, AccentRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SkeuoRecessedWell(
                cornerRadius = 18.dp,
                accentGlow = true,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = alarm?.formattedTime(language) ?: "--:--",
                    style = TextStyle(
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText,
                        fontFeatureSettings = "tnum"
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = alarm?.title ?: NivoStrings.alarmsTitle(language),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                textAlign = TextAlign.Center
            )

            if (alarm != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alarm.sound.getLocalizedName(language),
                    fontSize = 13.sp,
                    color = SilverAccent.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = NivoStrings.snoozeDurationLabel(language),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isLightSnooze = LocalNivoColors.current.isLight
                AlarmItem.SNOOZE_OPTIONS.forEach { minutes ->
                    val isDefault = minutes == defaultSnooze
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isLightSnooze && isDefault -> Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color.White))
                                    isLightSnooze -> Brush.verticalGradient(listOf(Color.White, RecessedWell))
                                    isDefault -> Brush.verticalGradient(listOf(Color(0xFF3E191E), Color(0xFF230E12)))
                                    else -> Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                                }
                            )
                            .border(
                                width = if (isDefault) 1.5.dp else 1.dp,
                                color = when {
                                    isDefault -> AccentRed
                                    isLightSnooze -> OutlineGrey
                                    else -> BevelHighlight
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSnooze(minutes) }
                            .testTag("ring_snooze_$minutes"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Snooze,
                                contentDescription = null,
                                tint = if (isDefault) AccentRed else SilverAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = NivoStrings.snoozeMinutesButton(language, minutes),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDefault) AccentRed else PrimaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NivoButton(
                text = NivoStrings.dismissAlarmButton(language),
                onClick = onDismiss,
                icon = Icons.Filled.AlarmOff,
                testTag = "ring_dismiss_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
