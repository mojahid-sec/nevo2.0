package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.alarm.AlarmScheduler
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.data.localization.toLocalizedDigits
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmType
import com.example.ui.components.SkeuoCarvedDivider
import com.example.ui.components.SkeuoIconKeyButton
import com.example.ui.components.SkeuoLedIndicator
import com.example.ui.components.SkeuoPanelCard
import com.example.ui.components.SkeuoRecessedWell
import com.example.ui.components.SkeuoSwitch
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardElevated
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkRed
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverAccent
import com.example.ui.viewmodel.NivoViewModel

@Composable
fun AlarmsScreen(
    viewModel: NivoViewModel,
    onCreateStandaloneAlarm: () -> Unit,
    onEditAlarm: (String) -> Unit,
    onConfigureHabitAlarm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val language by viewModel.language.collectAsState()
    val standaloneAlarms by viewModel.standaloneAlarms.collectAsState()
    val habitAlarms by viewModel.habitAlarms.collectAsState()
    val goals by viewModel.goals.collectAsState()

    var alarmToDelete by remember { mutableStateOf<AlarmItem?>(null) }
    var showPickHabitDialog by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasExactAlarmPermission by remember {
        mutableStateOf(AlarmScheduler.canScheduleExactAlarms(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                hasExactAlarmPermission = AlarmScheduler.canScheduleExactAlarms(context)
                viewModel.reloadAlarms()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    val habitsWithoutAlarm = remember(goals, habitAlarms) {
        val linkedIds = habitAlarms.mapNotNull { it.habitId }.toSet()
        goals.filter { it.id !in linkedIds }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("alarms_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = NivoStrings.alarmsTitle(language),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                    Text(
                        text = NivoStrings.alarmsSubtitle(language),
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }

                val isLight = LocalNivoColors.current.isLight
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.verticalGradient(listOf(AccentRed, PrimaryRed, DarkRed)))
                        .border(
                            width = 1.dp,
                            brush = if (isLight) {
                                Brush.verticalGradient(listOf(PrimaryRed, DarkRed))
                            } else {
                                Brush.verticalGradient(listOf(Color(0xFFFF8894), BevelShadow))
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onCreateStandaloneAlarm() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("header_add_alarm_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = NivoStrings.newAlarmButton(language),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = NivoStrings.newAlarmButton(language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    item {
                        PermissionBanner(
                            message = NivoStrings.notificationPermissionNeeded(language),
                            buttonText = NivoStrings.grantPermissionButton(language),
                            onClick = {
                                notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        )
                    }
                }

                if (!hasExactAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    item {
                        PermissionBanner(
                            message = NivoStrings.exactAlarmPermissionNeeded(language),
                            buttonText = NivoStrings.grantPermissionButton(language),
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        )
                    }
                }

                // SECTION 1: MY ALARMS (Standalone Alarms)
                item {
                    AlarmSectionHeader(
                        title = NivoStrings.myAlarmsSection(language),
                        subtitle = NivoStrings.myAlarmsSubtitle(language),
                        count = standaloneAlarms.size,
                        language = language,
                        testTag = "section_my_alarms"
                    )
                }

                if (standaloneAlarms.isEmpty()) {
                    item {
                        EmptyAlarmSectionCard(
                            message = NivoStrings.noStandaloneAlarms(language),
                            actionLabel = NivoStrings.newAlarmButton(language),
                            onAction = onCreateStandaloneAlarm,
                            testTag = "empty_standalone_alarms"
                        )
                    }
                } else {
                    items(standaloneAlarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            linkedHabitCompleted = false,
                            language = language,
                            onToggle = { enabled -> viewModel.toggleAlarmEnabled(alarm.id, enabled) },
                            onEdit = { onEditAlarm(alarm.id) },
                            onDelete = { alarmToDelete = alarm }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // SECTION 2: HABIT ALARMS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AlarmSectionHeader(
                            title = NivoStrings.habitAlarmsSection(language),
                            subtitle = NivoStrings.habitAlarmsSubtitle(language),
                            count = habitAlarms.size,
                            language = language,
                            testTag = "section_habit_alarms",
                            modifier = Modifier.weight(1f)
                        )
                        if (habitsWithoutAlarm.isNotEmpty()) {
                            val isLightBtn = LocalNivoColors.current.isLight
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isLightBtn) Brush.verticalGradient(listOf(Color.White, CardElevated))
                                        else Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                                    )
                                    .border(
                                        1.dp,
                                        if (isLightBtn) PrimaryRed.copy(alpha = 0.55f) else AccentRed.copy(alpha = 0.65f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { showPickHabitDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                    .testTag("link_habit_alarm_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = AccentRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = NivoStrings.selectHabitForAlarm(language),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentRed
                                    )
                                }
                            }
                        }
                    }
                }

                if (habitAlarms.isEmpty()) {
                    item {
                        EmptyAlarmSectionCard(
                            message = NivoStrings.noHabitAlarms(language),
                            actionLabel = if (habitsWithoutAlarm.isNotEmpty()) NivoStrings.selectHabitForAlarm(language) else null,
                            onAction = { showPickHabitDialog = true },
                            testTag = "empty_habit_alarms"
                        )
                    }
                } else {
                    items(habitAlarms, key = { it.id }) { alarm ->
                        val linkedGoal = goals.find { it.id == alarm.habitId }
                        AlarmCard(
                            alarm = alarm,
                            linkedHabitCompleted = linkedGoal?.isCompleted == true,
                            language = language,
                            onToggle = { enabled -> viewModel.toggleAlarmEnabled(alarm.id, enabled) },
                            onEdit = { onEditAlarm(alarm.id) },
                            onDelete = { alarmToDelete = alarm }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }

        if (alarmToDelete != null) {
            val target = alarmToDelete!!
            AlertDialog(
                onDismissRequest = { alarmToDelete = null },
                containerColor = CardBackground,
                title = {
                    Text(
                        text = NivoStrings.deleteAlarmConfirmTitle(language),
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = NivoStrings.deleteAlarmConfirmMessage(language, target.title),
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteAlarm(target.id)
                            alarmToDelete = null
                        }
                    ) {
                        Text(
                            text = NivoStrings.deleteButton(language),
                            color = AccentRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { alarmToDelete = null }) {
                        Text(text = NivoStrings.cancelButton(language), color = SecondaryText)
                    }
                }
            )
        }

        if (showPickHabitDialog) {
            AlertDialog(
                onDismissRequest = { showPickHabitDialog = false },
                containerColor = CardBackground,
                title = {
                    Text(
                        text = NivoStrings.selectHabitForAlarm(language),
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        habitsWithoutAlarm.forEach { habit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardElevated)
                                    .clickable {
                                        showPickHabitDialog = false
                                        onConfigureHabitAlarm(habit.id)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Flag,
                                    contentDescription = null,
                                    tint = AccentRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = habit.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPickHabitDialog = false }) {
                        Text(NivoStrings.cancelButton(language), color = SecondaryText)
                    }
                }
            )
        }
    }
}

@Composable
private fun PermissionBanner(
    message: String,
    buttonText: String,
    onClick: () -> Unit
) {
    SkeuoPanelCard(
        isHighlighted = true,
        cornerRadius = 14.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = PrimaryText,
                modifier = Modifier.weight(1f),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.verticalGradient(listOf(AccentRed, PrimaryRed, DarkRed)))
                    .clickable { onClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun AlarmSectionHeader(
    title: String,
    subtitle: String,
    count: Int,
    language: AppLanguage,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.testTag(testTag)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeuoLedIndicator(isOn = count > 0, size = 6.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SilverAccent,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(${count.toLocalizedDigits(language)})",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentRed
            )
        }
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = SecondaryText
        )
    }
}

@Composable
private fun EmptyAlarmSectionCard(
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    testTag: String
) {
    SkeuoPanelCard(
        modifier = Modifier.testTag(testTag),
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Alarm,
                contentDescription = null,
                tint = SecondaryText,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = SecondaryText
            )
            if (actionLabel != null) {
                val isLightAction = LocalNivoColors.current.isLight
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isLightAction) Brush.verticalGradient(listOf(Color.White, CardElevated))
                            else Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                        )
                        .border(
                            1.dp,
                            if (isLightAction) PrimaryRed.copy(alpha = 0.55f) else AccentRed.copy(alpha = 0.7f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onAction() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = actionLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmItem,
    linkedHabitCompleted: Boolean,
    language: AppLanguage,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isHabit = alarm.type == AlarmType.HABIT
    SkeuoPanelCard(
        modifier = Modifier.testTag("alarm_card_${alarm.id}"),
        isHighlighted = alarm.isEnabled,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(14.dp),
        onClick = onEdit
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeuoRecessedWell(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 12.dp,
                    accentGlow = alarm.isEnabled,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = alarm.formattedTime(language),
                            style = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (alarm.isEnabled) PrimaryText else SecondaryText,
                                fontFeatureSettings = "tnum"
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = alarm.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (alarm.isEnabled) SilverAccent else SecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                SkeuoSwitch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    testTag = "alarm_switch_${alarm.id}"
                )
            }

            if (isHabit && linkedHabitCompleted && alarm.isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryRed.copy(alpha = 0.2f))
                        .border(0.8.dp, AccentRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = NivoStrings.habitCompletedStillActiveBadge(language),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            SkeuoCarvedDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = NivoStrings.formatRepeatDaysSummary(alarm.repeatDays, isHabit, language),
                        fontSize = 12.sp,
                        color = if (alarm.isEnabled) AccentRed else SecondaryText,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = SilverAccent.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${alarm.sound.getLocalizedName(language)} • ${alarm.snoozeMinutes.toLocalizedDigits(language)}m",
                            fontSize = 11.sp,
                            color = SecondaryText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeuoIconKeyButton(
                        icon = Icons.Filled.Edit,
                        contentDescription = NivoStrings.editAlarmTitle(language),
                        onClick = onEdit,
                        keySize = 34.dp,
                        testTag = "edit_alarm_${alarm.id}"
                    )
                    SkeuoIconKeyButton(
                        icon = Icons.Filled.Delete,
                        contentDescription = NivoStrings.deleteButton(language),
                        onClick = onDelete,
                        tint = AccentRed,
                        keySize = 34.dp,
                        testTag = "delete_alarm_${alarm.id}"
                    )
                }
            }
        }
    }
}
