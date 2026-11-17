package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.alarm.AlarmSoundPlayer
import com.example.data.localization.NivoStrings
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmSound
import com.example.data.model.AlarmType
import com.example.ui.components.AlarmConfigSection
import com.example.ui.components.NivoButton
import com.example.ui.components.SkeuoIconKeyButton
import com.example.ui.components.SkeuoPanelCard
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SecondaryText
import com.example.ui.viewmodel.NivoViewModel

@Composable
fun CreateEditAlarmScreen(
    viewModel: NivoViewModel,
    alarmIdToEdit: String? = null,
    habitIdToConfigure: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        AlarmSoundPlayer.stopPreview()
        onNavigateBack()
    }

    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val goals by viewModel.goals.collectAsState()

    val existingAlarm = remember(alarmIdToEdit, habitIdToConfigure, alarms) {
        when {
            alarmIdToEdit != null -> alarms.find { it.id == alarmIdToEdit }
            habitIdToConfigure != null -> alarms.find { it.type == AlarmType.HABIT && it.habitId == habitIdToConfigure }
            else -> null
        }
    }

    val linkedHabit = remember(existingAlarm, habitIdToConfigure, goals) {
        val targetHabitId = existingAlarm?.habitId ?: habitIdToConfigure
        if (targetHabitId != null) goals.find { it.id == targetHabitId } else null
    }

    val isHabitAlarm = (existingAlarm?.type == AlarmType.HABIT) || (habitIdToConfigure != null)
    val isEditMode = existingAlarm != null

    var title by remember(existingAlarm, linkedHabit) {
        mutableStateOf(
            existingAlarm?.title ?: linkedHabit?.title ?: ""
        )
    }
    var hour24 by remember(existingAlarm) { mutableIntStateOf(existingAlarm?.hour24 ?: 7) }
    var minute by remember(existingAlarm) { mutableIntStateOf(existingAlarm?.minute ?: 0) }
    var use24HourFormat by remember(existingAlarm) {
        mutableStateOf(existingAlarm?.use24HourFormat ?: false)
    }
    var repeatDays by remember(existingAlarm, isHabitAlarm) {
        mutableStateOf(
            existingAlarm?.repeatDays ?: if (isHabitAlarm) AlarmItem.ALL_DAYS else emptySet()
        )
    }
    var snoozeMinutes by remember(existingAlarm) {
        mutableIntStateOf(existingAlarm?.snoozeMinutes ?: 5)
    }
    var selectedSound by remember(existingAlarm) {
        mutableStateOf(existingAlarm?.sound ?: AlarmSound.CLASSIC_ALARM)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        AlarmSoundPlayer.stopPreview()
        onNavigateBack()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("create_edit_alarm_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeuoIconKeyButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = NivoStrings.cancelButton(language),
                onClick = {
                    AlarmSoundPlayer.stopPreview()
                    onNavigateBack()
                },
                testTag = "alarm_back_button"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (isEditMode) NivoStrings.editAlarmTitle(language) else NivoStrings.newAlarmButton(language),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText
                )
                Text(
                    text = if (isHabitAlarm) NivoStrings.habitAlarmLabel(language) else NivoStrings.standaloneAlarmLabel(language),
                    fontSize = 12.sp,
                    color = AccentRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (!isHabitAlarm) {
            Text(
                text = NivoStrings.alarmNameLabel(language),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(NivoStrings.alarmNamePlaceholder(language), color = SecondaryText)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    focusedContainerColor = RecessedWell,
                    unfocusedContainerColor = RecessedWell,
                    focusedBorderColor = AccentRed,
                    unfocusedBorderColor = OutlineGrey,
                    cursorColor = AccentRed
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_name_input")
            )
            Spacer(modifier = Modifier.height(18.dp))
        } else if (linkedHabit != null) {
            SkeuoPanelCard(
                isHighlighted = true,
                cornerRadius = 14.dp,
                contentPadding = PaddingValues(14.dp)
            ) {
                Column {
                    Text(
                        text = NivoStrings.habitAlarmLabel(language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = linkedHabit.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        AlarmConfigSection(
            hour24 = hour24,
            minute = minute,
            use24HourFormat = use24HourFormat,
            repeatDays = repeatDays,
            snoozeMinutes = snoozeMinutes,
            selectedSound = selectedSound,
            isHabitAlarm = isHabitAlarm,
            language = language,
            onTimeChange = { h, m ->
                hour24 = h
                minute = m
            },
            onFormatChange = { use24 -> use24HourFormat = use24 },
            onRepeatDaysChange = { days -> repeatDays = days },
            onSnoozeChange = { min -> snoozeMinutes = min },
            onSoundChange = { sound -> selectedSound = sound }
        )

        Spacer(modifier = Modifier.height(24.dp))

        NivoButton(
            text = NivoStrings.saveAlarmButton(language),
            onClick = {
                AlarmSoundPlayer.stopPreview()
                if (isHabitAlarm) {
                    val hId = existingAlarm?.habitId ?: habitIdToConfigure
                    if (hId != null) {
                        viewModel.saveHabitAlarm(
                            habitId = hId,
                            habitTitle = linkedHabit?.title ?: title.ifBlank { "Habit Alarm" },
                            hour24 = hour24,
                            minute = minute,
                            repeatDays = repeatDays,
                            isEnabled = true,
                            snoozeMinutes = snoozeMinutes,
                            sound = selectedSound,
                            use24HourFormat = use24HourFormat
                        )
                    }
                } else {
                    val item = (existingAlarm ?: AlarmItem(
                        title = title,
                        hour24 = hour24,
                        minute = minute
                    )).copy(
                        title = title.trim().ifEmpty { "Alarm" },
                        hour24 = hour24,
                        minute = minute,
                        repeatDays = repeatDays,
                        isEnabled = true,
                        snoozeMinutes = snoozeMinutes,
                        sound = selectedSound,
                        type = AlarmType.STANDALONE,
                        habitId = null,
                        use24HourFormat = use24HourFormat
                    )
                    viewModel.saveStandaloneAlarm(item)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onNavigateBack()
                }
            },
            icon = Icons.Filled.Check,
            testTag = "save_alarm_button",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        val isLight = LocalNivoColors.current.isLight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isLight) Brush.verticalGradient(listOf(Color.White, RecessedWell))
                    else Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                )
                .border(1.dp, if (isLight) OutlineGrey else BevelHighlight, RoundedCornerShape(14.dp))
                .clickable {
                    AlarmSoundPlayer.stopPreview()
                    onNavigateBack()
                }
                .testTag("cancel_alarm_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = NivoStrings.cancelButton(language),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
