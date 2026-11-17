package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.alarm.AlarmSoundPlayer
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.data.localization.toLocalizedDigits
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmSound
import com.example.data.model.AlarmType
import com.example.data.model.TrackingMode
import com.example.ui.components.AlarmConfigSection
import com.example.ui.components.NivoButton
import com.example.ui.components.SkeuoDetentChip
import com.example.ui.components.SkeuoIconKeyButton
import com.example.ui.components.SkeuoPanelCard
import com.example.ui.components.SkeuoRecessedWell
import com.example.ui.components.SkeuoSwitch
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SecondaryText
import com.example.ui.viewmodel.NivoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateGoalScreen(
    viewModel: NivoViewModel,
    goalIdToEdit: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        AlarmSoundPlayer.stopPreview()
        onNavigateBack()
    }

    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val goalToEdit = remember(goalIdToEdit, goals) {
        goals.find { it.id == goalIdToEdit }
    }
    val existingHabitAlarm = remember(goalToEdit, alarms) {
        if (goalToEdit != null) {
            alarms.find { it.type == AlarmType.HABIT && it.habitId == goalToEdit.id }
        } else null
    }

    val isEditMode = goalToEdit != null

    var title by remember(goalToEdit) { mutableStateOf(goalToEdit?.title ?: "") }
    var durationInput by remember(goalToEdit) {
        mutableStateOf((goalToEdit?.durationDays ?: 30).toString())
    }
    var startDateMillis by remember(goalToEdit) {
        mutableLongStateOf(goalToEdit?.startDateMillis ?: System.currentTimeMillis())
    }
    var reason by remember(goalToEdit) { mutableStateOf(goalToEdit?.reason ?: "") }
    var trackingMode by remember(goalToEdit) {
        mutableStateOf(goalToEdit?.trackingMode ?: TrackingMode.MANUAL)
    }

    var alarmEnabled by remember(goalToEdit?.id, existingHabitAlarm?.id) {
        mutableStateOf(existingHabitAlarm?.isEnabled ?: false)
    }
    var alarmHour24 by remember(goalToEdit?.id, existingHabitAlarm?.id) {
        mutableIntStateOf(existingHabitAlarm?.hour24 ?: 8)
    }
    var alarmMinute by remember(goalToEdit?.id, existingHabitAlarm?.id) {
        mutableIntStateOf(existingHabitAlarm?.minute ?: 0)
    }
    var alarmUse24Hour by remember(goalToEdit?.id, existingHabitAlarm?.id) {
        mutableStateOf(existingHabitAlarm?.use24HourFormat ?: false)
    }
    var alarmRepeatDays by remember(goalToEdit?.id, existingHabitAlarm?.id) {
        mutableStateOf(existingHabitAlarm?.repeatDays ?: AlarmItem.ALL_DAYS)
    }
    var alarmSnoozeMinutes by remember(goalToEdit?.id, existingHabitAlarm?.id) {
        mutableIntStateOf(existingHabitAlarm?.snoozeMinutes ?: 5)
    }
    var alarmSound by remember(goalToEdit?.id, existingHabitAlarm?.id) {
        mutableStateOf(existingHabitAlarm?.sound ?: AlarmSound.CLASSIC_ALARM)
    }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        AlarmSoundPlayer.stopPreview()
        onNavigateBack()
    }

    var titleError by remember { mutableStateOf<String?>(null) }
    var durationError by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.US) }

    val parsedDuration = durationInput.toIntOrNull()
    val isValidDuration = parsedDuration != null && parsedDuration in 2..365

    val expectedCompletionMillis = remember(startDateMillis, parsedDuration) {
        if (parsedDuration != null && parsedDuration in 2..365) {
            startDateMillis + (parsedDuration.toLong() * 24L * 60L * 60L * 1000L)
        } else null
    }

    val sampleDurations = listOf(2, 3, 7, 15, 30, 90, 180, 365)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("create_goal_screen")
    ) {
        // Hardware Top Header
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
                testTag = "create_goal_back_button"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEditMode) NivoStrings.editGoal(language) else NivoStrings.newGoal(language),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Goal Title Input
        Text(
            text = NivoStrings.goalTitleLabel(language),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                if (it.isNotBlank()) titleError = null
            },
            placeholder = {
                Text(NivoStrings.goalTitlePlaceholder(language), color = SecondaryText)
            },
            isError = titleError != null,
            supportingText = {
                if (titleError != null) {
                    Text(text = titleError!!, color = AccentRed, fontSize = 12.sp)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
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
                .testTag("goal_title_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Tracking Mode (Option A: Manual vs Option B: Automatic)
        Text(
            text = NivoStrings.trackingModeLabel(language),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))

        val isManual = trackingMode == TrackingMode.MANUAL
        SkeuoPanelCard(
            modifier = Modifier.testTag("option_manual_tracking"),
            isHighlighted = isManual,
            cornerRadius = 14.dp,
            contentPadding = PaddingValues(14.dp),
            onClick = { trackingMode = TrackingMode.MANUAL }
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = if (isManual) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isManual) AccentRed else SecondaryText,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = NivoStrings.optionAManual(language),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isManual) AccentRed else PrimaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = NivoStrings.manualModeDesc(language),
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val isAuto = trackingMode == TrackingMode.AUTOMATIC
        SkeuoPanelCard(
            modifier = Modifier.testTag("option_automatic_tracking"),
            isHighlighted = isAuto,
            cornerRadius = 14.dp,
            contentPadding = PaddingValues(14.dp),
            onClick = { trackingMode = TrackingMode.AUTOMATIC }
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = if (isAuto) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isAuto) AccentRed else SecondaryText,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = NivoStrings.optionBAutomatic(language),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAuto) AccentRed else PrimaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = NivoStrings.autoModeDesc(language),
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val isDailyReset = trackingMode == TrackingMode.DAILY_RESET
        SkeuoPanelCard(
            modifier = Modifier.testTag("option_daily_reset_tracking"),
            isHighlighted = isDailyReset,
            cornerRadius = 14.dp,
            contentPadding = PaddingValues(14.dp),
            onClick = { trackingMode = TrackingMode.DAILY_RESET }
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = if (isDailyReset) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isDailyReset) AccentRed else SecondaryText,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = NivoStrings.optionCDailyReset(language),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDailyReset) AccentRed else PrimaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = NivoStrings.dailyResetModeDesc(language),
                        fontSize = 12.sp,
                        color = SecondaryText,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Goal Duration (2 to 365 Days)
        Text(
            text = NivoStrings.goalDurationLabel(language),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeuoIconKeyButton(
                icon = Icons.Filled.Remove,
                contentDescription = "Decrement",
                onClick = {
                    val current = durationInput.toIntOrNull() ?: 30
                    val newVal = (current - 1).coerceIn(2, 365)
                    durationInput = newVal.toString()
                    durationError = null
                },
                keySize = 48.dp,
                testTag = "decrement_duration_button"
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = durationInput,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    durationInput = filtered
                    val num = filtered.toIntOrNull()
                    durationError = when {
                        filtered.isEmpty() -> NivoStrings.errorDurationInvalid(language)
                        num == null -> NivoStrings.errorDurationInvalid(language)
                        num < 2 -> NivoStrings.errorDurationTooSmall(language)
                        num > 365 -> NivoStrings.errorDurationTooLarge(language)
                        else -> null
                    }
                },
                placeholder = { Text("30", color = SecondaryText) },
                singleLine = true,
                isError = durationError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryText,
                    fontFeatureSettings = "tnum"
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
                    .weight(1f)
                    .testTag("duration_number_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            SkeuoIconKeyButton(
                icon = Icons.Filled.Add,
                contentDescription = "Increment",
                onClick = {
                    val current = durationInput.toIntOrNull() ?: 30
                    val newVal = (current + 1).coerceIn(2, 365)
                    durationInput = newVal.toString()
                    durationError = null
                },
                keySize = 48.dp,
                testTag = "increment_duration_button"
            )
        }

        if (durationError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = durationError!!,
                color = AccentRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sampleDurations) { days ->
                val isSelected = parsedDuration == days
                SkeuoDetentChip(
                    label = "${days.toLocalizedDigits(language)} ${if (language == AppLanguage.BANGLA) "দিন" else "d"}",
                    selected = isSelected,
                    onClick = {
                        durationInput = days.toString()
                        durationError = null
                    },
                    testTag = "preset_duration_${days}"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Start & Target Date Readout
        SkeuoRecessedWell(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${NivoStrings.startDateLabel(language)}: ${dateFormat.format(Date(startDateMillis))}",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }

                if (expectedCompletionMillis != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = null,
                            tint = AccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${NivoStrings.expectedCompletionLabel(language)}: ${dateFormat.format(Date(expectedCompletionMillis))}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Optional Personal Reason
        Text(
            text = NivoStrings.personalReasonLabel(language),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            placeholder = {
                Text(NivoStrings.personalReasonPlaceholder(language), color = SecondaryText)
            },
            minLines = 3,
            maxLines = 4,
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
                .testTag("goal_reason_input")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Optional Habit Reminder / Alarm Console
        SkeuoPanelCard(
            modifier = Modifier.testTag("habit_alarm_section"),
            isHighlighted = alarmEnabled,
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Alarm,
                            contentDescription = null,
                            tint = AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = NivoStrings.habitReminderAlarmSection(language),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = NivoStrings.habitAlarmOptionalDesc(language),
                                fontSize = 11.sp,
                                color = SecondaryText,
                                lineHeight = 15.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SkeuoSwitch(
                        checked = alarmEnabled,
                        onCheckedChange = { alarmEnabled = it },
                        testTag = "habit_alarm_toggle"
                    )
                }

                AnimatedVisibility(visible = alarmEnabled || existingHabitAlarm != null) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        AlarmConfigSection(
                            hour24 = alarmHour24,
                            minute = alarmMinute,
                            use24HourFormat = alarmUse24Hour,
                            repeatDays = alarmRepeatDays,
                            snoozeMinutes = alarmSnoozeMinutes,
                            selectedSound = alarmSound,
                            isHabitAlarm = true,
                            language = language,
                            onTimeChange = { h, m ->
                                alarmHour24 = h
                                alarmMinute = m
                            },
                            onFormatChange = { use24 -> alarmUse24Hour = use24 },
                            onRepeatDaysChange = { days -> alarmRepeatDays = days },
                            onSnoozeChange = { min -> alarmSnoozeMinutes = min },
                            onSoundChange = { sound -> alarmSound = sound }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NivoButton(
            text = if (isEditMode) NivoStrings.updateGoalButton(language) else NivoStrings.saveGoalButton(language),
            onClick = {
                var hasError = false
                if (title.isBlank()) {
                    titleError = NivoStrings.errorTitleRequired(language)
                    hasError = true
                }
                val duration = durationInput.toIntOrNull()
                if (duration == null || duration !in 2..365) {
                    durationError = when {
                        duration == null -> NivoStrings.errorDurationInvalid(language)
                        duration < 2 -> NivoStrings.errorDurationTooSmall(language)
                        else -> NivoStrings.errorDurationTooLarge(language)
                    }
                    hasError = true
                }

                if (!hasError && duration != null) {
                    AlarmSoundPlayer.stopPreview()
                    val finishNavigation = {
                        if (alarmEnabled &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onNavigateBack()
                        }
                    }

                    if (goalToEdit != null) {
                        viewModel.updateGoal(
                            id = goalToEdit.id,
                            title = title,
                            durationDays = duration,
                            startDateMillis = startDateMillis,
                            reason = reason,
                            trackingMode = trackingMode
                        ) { updatedGoal ->
                            if (alarmEnabled || existingHabitAlarm != null) {
                                viewModel.saveHabitAlarm(
                                    habitId = updatedGoal.id,
                                    habitTitle = updatedGoal.title,
                                    hour24 = alarmHour24,
                                    minute = alarmMinute,
                                    repeatDays = alarmRepeatDays,
                                    isEnabled = alarmEnabled,
                                    snoozeMinutes = alarmSnoozeMinutes,
                                    sound = alarmSound,
                                    use24HourFormat = alarmUse24Hour
                                )
                            }
                            finishNavigation()
                        }
                    } else {
                        viewModel.createGoal(
                            title = title,
                            durationDays = duration,
                            startDateMillis = startDateMillis,
                            reason = reason,
                            trackingMode = trackingMode
                        ) { createdGoal ->
                            if (alarmEnabled) {
                                viewModel.saveHabitAlarm(
                                    habitId = createdGoal.id,
                                    habitTitle = createdGoal.title,
                                    hour24 = alarmHour24,
                                    minute = alarmMinute,
                                    repeatDays = alarmRepeatDays,
                                    isEnabled = true,
                                    snoozeMinutes = alarmSnoozeMinutes,
                                    sound = alarmSound,
                                    use24HourFormat = alarmUse24Hour
                                )
                            }
                            finishNavigation()
                        }
                    }
                }
            },
            icon = Icons.Filled.Check,
            testTag = "save_goal_button",
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
                    if (isLight) Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.White, RecessedWell))
                    else Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                )
                .border(1.dp, if (isLight) OutlineGrey else BevelHighlight, RoundedCornerShape(14.dp))
                .clickable {
                    AlarmSoundPlayer.stopPreview()
                    onNavigateBack()
                }
                .testTag("cancel_goal_button"),
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
