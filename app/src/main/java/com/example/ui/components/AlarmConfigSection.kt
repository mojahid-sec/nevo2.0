package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.alarm.AlarmSoundPlayer
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.data.localization.toLocalizedDigits
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmSound
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardElevated
import com.example.ui.theme.DarkRed
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmConfigSection(
    hour24: Int,
    minute: Int,
    use24HourFormat: Boolean,
    repeatDays: Set<Int>,
    snoozeMinutes: Int,
    selectedSound: AlarmSound,
    isHabitAlarm: Boolean,
    language: AppLanguage,
    onTimeChange: (Int, Int) -> Unit,
    onFormatChange: (Boolean) -> Unit,
    onRepeatDaysChange: (Set<Int>) -> Unit,
    onSnoozeChange: (Int) -> Unit,
    onSoundChange: (AlarmSound) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var playingPreviewSound by remember { mutableStateOf<AlarmSound?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                AlarmSoundPlayer.stopPreview()
                playingPreviewSound = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            AlarmSoundPlayer.stopPreview()
            playingPreviewSound = null
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Time & Format Selector (12h AM/PM & 24h)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = NivoStrings.alarmTimeLabel(language),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            SkeuoRecessedWell(
                cornerRadius = 10.dp,
                contentPadding = PaddingValues(3.dp)
            ) {
                Row {
                    FormatToggleChip(
                        label = NivoStrings.format12Hour(language),
                        selected = !use24HourFormat,
                        onClick = { onFormatChange(false) },
                        testTag = "format_12h_chip"
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    FormatToggleChip(
                        label = NivoStrings.format24Hour(language),
                        selected = use24HourFormat,
                        onClick = { onFormatChange(true) },
                        testTag = "format_24h_chip"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val displayTime = remember(hour24, minute, use24HourFormat, language) {
            AlarmItem(
                title = "",
                hour24 = hour24,
                minute = minute,
                use24HourFormat = use24HourFormat
            ).formattedTime(language, force24Hour = use24HourFormat)
        }

        SkeuoRecessedWell(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePickerDialog = true }
                .testTag("alarm_time_picker_row"),
            cornerRadius = 16.dp,
            accentGlow = true,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = displayTime,
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryText,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom)))
                        .border(1.dp, BevelHighlight, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BANGLA) "পরিবর্তন" else "Edit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Repeat Days Selector
        Text(
            text = NivoStrings.repeatDaysLabel(language),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PresetRepeatChip(
                label = NivoStrings.everyDayLabel(language),
                selected = repeatDays == AlarmItem.ALL_DAYS,
                onClick = { onRepeatDaysChange(AlarmItem.ALL_DAYS) }
            )
            PresetRepeatChip(
                label = NivoStrings.weekdaysLabel(language),
                selected = repeatDays == AlarmItem.WEEKDAYS,
                onClick = { onRepeatDaysChange(AlarmItem.WEEKDAYS) }
            )
            PresetRepeatChip(
                label = NivoStrings.weekendsLabel(language),
                selected = repeatDays == AlarmItem.WEEKENDS,
                onClick = { onRepeatDaysChange(AlarmItem.WEEKENDS) }
            )
            if (!isHabitAlarm) {
                PresetRepeatChip(
                    label = NivoStrings.oneTimeLabel(language),
                    selected = repeatDays.isEmpty(),
                    onClick = { onRepeatDaysChange(emptySet()) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 7 Raised Day Keycaps
        val isLight = LocalNivoColors.current.isLight
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AlarmItem.ORDERED_DAYS.forEach { day ->
                val isSelected = day in repeatDays
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isLight && isSelected -> Brush.verticalGradient(listOf(PrimaryRed, PrimaryRed))
                                isLight -> Brush.verticalGradient(listOf(Color.White, RecessedWell))
                                isSelected -> Brush.verticalGradient(listOf(AccentRed, PrimaryRed, DarkRed))
                                else -> Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                            }
                        )
                        .border(
                            width = 1.dp,
                            brush = if (isLight) {
                                val c = if (isSelected) PrimaryRed else OutlineGrey
                                Brush.verticalGradient(listOf(c, c))
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        if (isSelected) Color(0xFFFF8894) else BevelHighlight,
                                        BevelShadow
                                    )
                                )
                            },
                            shape = CircleShape
                        )
                        .clickable {
                            val next = if (isSelected) {
                                val removed = repeatDays - day
                                if (isHabitAlarm && removed.isEmpty()) repeatDays else removed
                            } else {
                                repeatDays + day
                            }
                            onRepeatDaysChange(next)
                        }
                        .testTag("repeat_day_$day"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = NivoStrings.dayShortLabel(day, language),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isSelected -> Color.White
                            else -> SecondaryText
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Snooze Duration Options
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Snooze,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = NivoStrings.snoozeDurationLabel(language),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AlarmItem.SNOOZE_OPTIONS.forEach { minutes ->
                val isSelected = snoozeMinutes == minutes
                SkeuoDetentChip(
                    label = if (language == AppLanguage.BANGLA) {
                        "${minutes.toLocalizedDigits(language)} মিনিট"
                    } else {
                        "$minutes min"
                    },
                    selected = isSelected,
                    onClick = { onSnoozeChange(minutes) },
                    modifier = Modifier.weight(1f),
                    testTag = "snooze_option_$minutes"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Selectable Alarm Sound Console (10 Offline Sounds + Play/Stop Preview)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = NivoStrings.alarmSoundLabel(language),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        SkeuoRecessedWell(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp,
            contentPadding = PaddingValues(6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                AlarmSound.values().forEach { sound ->
                    val isSelected = selectedSound == sound
                    val isPreviewing = playingPreviewSound == sound

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isLight && isSelected -> Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color(0xFFD6F3DF)))
                                    isLight -> Brush.verticalGradient(listOf(Color.White, Color.White))
                                    isSelected -> Brush.verticalGradient(listOf(Color(0xFF33181C), Color(0xFF211114)))
                                    else -> Brush.verticalGradient(listOf(MetalTop.copy(alpha = 0.55f), CardBackground))
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryRed.copy(alpha = 0.7f) else OutlineGrey.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSoundChange(sound) }
                            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            .testTag("alarm_sound_${sound.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeuoLedIndicator(isOn = isSelected || isPreviewing, size = 7.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sound.getLocalizedName(language),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryText else SilverAccent.copy(alpha = 0.85f),
                            modifier = Modifier.weight(1f)
                        )
                        SkeuoIconKeyButton(
                            icon = if (isPreviewing) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (isPreviewing) {
                                NivoStrings.stopPreview(language)
                            } else {
                                NivoStrings.previewSound(language)
                            },
                            isAccent = isPreviewing,
                            tint = if (isSelected) AccentRed else SilverAccent,
                            keySize = 36.dp,
                            onClick = {
                                if (isPreviewing) {
                                    AlarmSoundPlayer.stopPreview()
                                    playingPreviewSound = null
                                } else {
                                    playingPreviewSound = sound
                                    AlarmSoundPlayer.startPreview(
                                        context = context,
                                        sound = sound,
                                        onCompleted = {
                                            if (playingPreviewSound == sound) {
                                                playingPreviewSound = null
                                            }
                                        },
                                        onError = { _ ->
                                            if (playingPreviewSound == sound) {
                                                playingPreviewSound = null
                                            }
                                            Toast.makeText(
                                                context,
                                                if (language == AppLanguage.BANGLA) {
                                                    "অ্যালার্ম সাউন্ড প্রিভিউ চালু করা যায়নি"
                                                } else {
                                                    "Unable to play sound preview"
                                                },
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            },
                            testTag = "preview_sound_${sound.id}"
                        )
                    }
                }
            }
        }
    }

    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour24,
            initialMinute = minute,
            is24Hour = use24HourFormat
        )
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            containerColor = CardBackground,
            title = {
                Text(
                    text = NivoStrings.alarmTimeLabel(language),
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = RecessedWell,
                            selectorColor = PrimaryRed,
                            containerColor = CardBackground,
                            periodSelectorBorderColor = OutlineGrey,
                            periodSelectorSelectedContainerColor = PrimaryRed.copy(alpha = 0.25f),
                            periodSelectorUnselectedContainerColor = CardElevated,
                            periodSelectorSelectedContentColor = AccentRed,
                            periodSelectorUnselectedContentColor = SecondaryText,
                            timeSelectorSelectedContainerColor = PrimaryRed.copy(alpha = 0.25f),
                            timeSelectorUnselectedContainerColor = CardElevated,
                            timeSelectorSelectedContentColor = AccentRed,
                            timeSelectorUnselectedContentColor = PrimaryText,
                            clockDialSelectedContentColor = PrimaryText,
                            clockDialUnselectedContentColor = SecondaryText
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange(timePickerState.hour, timePickerState.minute)
                        showTimePickerDialog = false
                    }
                ) {
                    Text(
                        text = NivoStrings.doneButton(language),
                        color = AccentRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text(
                        text = NivoStrings.cancelButton(language),
                        color = SecondaryText
                    )
                }
            }
        )
    }
}

@Composable
private fun FormatToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val isLight = LocalNivoColors.current.isLight
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isLight && selected -> Brush.verticalGradient(listOf(PrimaryRed, PrimaryRed))
                    selected -> Brush.verticalGradient(listOf(AccentRed, PrimaryRed, DarkRed))
                    else -> Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 5.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else SecondaryText
        )
    }
}

@Composable
private fun PresetRepeatChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isLight = LocalNivoColors.current.isLight
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isLight && selected -> Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color(0xFFD6F3DF)))
                    isLight -> Brush.verticalGradient(listOf(Color.White, RecessedWell))
                    selected -> Brush.verticalGradient(listOf(Color(0xFF3E191E), Color(0xFF230E12)))
                    else -> Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                }
            )
            .border(
                width = 1.dp,
                color = if (selected) AccentRed.copy(alpha = 0.7f) else OutlineGrey,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = when {
                isLight && selected -> DarkRed
                selected -> PrimaryText
                else -> SecondaryText
            }
        )
    }
}
