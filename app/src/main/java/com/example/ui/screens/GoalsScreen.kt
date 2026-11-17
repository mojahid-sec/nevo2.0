package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.data.localization.toLocalizedDigits
import com.example.data.model.AlarmItem
import com.example.data.model.AlarmType
import com.example.data.model.Goal
import com.example.data.model.TrackingMode
import com.example.ui.components.NivoButton
import com.example.ui.components.SkeuoCarvedDivider
import com.example.ui.components.SkeuoIconKeyButton
import com.example.ui.components.SkeuoLedIndicator
import com.example.ui.components.SkeuoLinearGauge
import com.example.ui.components.SkeuoPanelCard
import com.example.ui.components.SkeuoRecessedWell
import com.example.ui.components.SkeuoSwitch
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkRed
import com.example.ui.theme.LightHabitPastels
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverAccent
import com.example.ui.viewmodel.NivoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GoalsScreen(
    viewModel: NivoViewModel,
    onCreateGoalClick: () -> Unit,
    onEditGoalClick: (String) -> Unit,
    onConfigureHabitAlarm: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val goals by viewModel.goals.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()
    val language by viewModel.language.collectAsState()
    var goalToDelete by remember { mutableStateOf<Goal?>(null) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("goals_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = NivoStrings.myGoals(language),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )
                    Text(
                        text = "${goals.size.toLocalizedDigits(language)} ${if (language == AppLanguage.BANGLA) "টি লক্ষ্য" else "Active Goals"}",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }

                val isLight = LocalNivoColors.current.isLight
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isLight) Brush.verticalGradient(listOf(Color(0xFF3CB866), PrimaryRed))
                            else Brush.verticalGradient(listOf(AccentRed, PrimaryRed, DarkRed))
                        )
                        .border(
                            1.dp,
                            if (isLight) Brush.verticalGradient(listOf(Color(0xFF28964E), Color(0xFF28964E)))
                            else Brush.verticalGradient(listOf(Color(0xFFFF8894), BevelShadow)),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onCreateGoalClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("header_add_goal_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = NivoStrings.newGoal(language),
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = NivoStrings.newGoal(language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (goals.isEmpty()) {
                GoalsEmptyState(
                    language = language,
                    onCreateGoalClick = onCreateGoalClick
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        val linkedAlarm = alarms.find { it.type == AlarmType.HABIT && it.habitId == goal.id }
                        GoalCard(
                            goal = goal,
                            linkedAlarm = linkedAlarm,
                            currentTimeMillis = currentTimeMillis,
                            language = language,
                            dateFormat = dateFormat,
                            onSelect = { viewModel.selectGoal(goal.id) },
                            onEdit = { onEditGoalClick(goal.id) },
                            onDelete = { goalToDelete = goal },
                            onToggleAlarm = { enabled ->
                                if (linkedAlarm != null) {
                                    viewModel.toggleAlarmEnabled(linkedAlarm.id, enabled)
                                }
                            },
                            onConfigureAlarm = { onConfigureHabitAlarm(goal.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }

        if (goalToDelete != null) {
            val target = goalToDelete!!
            AlertDialog(
                onDismissRequest = { goalToDelete = null },
                containerColor = CardBackground,
                title = {
                    Text(
                        text = NivoStrings.deleteGoalConfirmTitle(language),
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = NivoStrings.deleteGoalConfirmMessage(language, target.title),
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteGoal(target.id)
                            goalToDelete = null
                        }
                    ) {
                        Text(
                            NivoStrings.deleteButton(language),
                            color = AccentRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { goalToDelete = null }) {
                        Text(NivoStrings.cancelButton(language), color = SecondaryText)
                    }
                }
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    linkedAlarm: AlarmItem?,
    currentTimeMillis: Long,
    language: AppLanguage,
    dateFormat: SimpleDateFormat,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAlarm: (Boolean) -> Unit,
    onConfigureAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = LocalNivoColors.current.isLight
    val isSelected = goal.isSelected
    val isAuto = goal.trackingMode == TrackingMode.AUTOMATIC
    val isDailyReset = goal.trackingMode == TrackingMode.DAILY_RESET
    val pastelPalette = remember(goal.title) { LightHabitPastels.forTitle(goal.title) }
    val cardAccentColor = if (isLight) pastelPalette.accent else AccentRed

    val effectiveCompleted = goal.getEffectiveCompletedDays(currentTimeMillis)
    val effectiveRemaining = goal.getEffectiveRemainingDays(currentTimeMillis)
    val effectiveProgressFraction = goal.getEffectiveProgressFraction(currentTimeMillis)

    SkeuoPanelCard(
        modifier = modifier.testTag("goal_item_card_${goal.id}"),
        isHighlighted = isSelected,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(16.dp),
        containerColorOverride = if (isLight) pastelPalette.container else null,
        borderColorOverride = if (isLight && isSelected) cardAccentColor else if (isLight) OutlineGrey else null,
        onClick = onSelect
    ) {
        Column {
            // Header Row: Selection Radio + Title + Edit & Delete tactile keys
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
                        imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = if (isSelected) "Active Goal" else "Select Goal",
                        tint = if (isSelected) cardAccentColor else SecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = goal.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        val modeLabel = when (goal.trackingMode) {
                            TrackingMode.AUTOMATIC -> NivoStrings.autoModeShort(language)
                            TrackingMode.DAILY_RESET -> NivoStrings.dailyResetModeShort(language)
                            TrackingMode.MANUAL -> NivoStrings.manualModeShort(language)
                        }
                        val isSpecialMode = isAuto || isDailyReset
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            isLight -> Color.White.copy(alpha = 0.78f)
                                            isSpecialMode -> PrimaryRed.copy(alpha = 0.22f)
                                            else -> OutlineGrey.copy(alpha = 0.35f)
                                        }
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isLight -> cardAccentColor
                                        isSpecialMode -> AccentRed
                                        else -> SilverAccent
                                    }
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${NivoStrings.activeOnHome(language)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardAccentColor
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkeuoIconKeyButton(
                        icon = Icons.Filled.Edit,
                        contentDescription = NivoStrings.editGoal(language),
                        onClick = onEdit,
                        keySize = 34.dp,
                        testTag = "edit_goal_${goal.id}"
                    )
                    SkeuoIconKeyButton(
                        icon = Icons.Filled.Delete,
                        contentDescription = NivoStrings.deleteButton(language),
                        onClick = onDelete,
                        tint = if (isLight) Color(0xFFDC2626) else AccentRed,
                        keySize = 34.dp,
                        testTag = "delete_goal_${goal.id}"
                    )
                }
            }

            if (goal.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${goal.reason}\"",
                    fontSize = 12.sp,
                    color = if (isLight) PrimaryText.copy(alpha = 0.72f) else SecondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // EXACTLY ONE PROGRESS BAR PER HABIT
            SkeuoLinearGauge(
                progressFraction = effectiveProgressFraction,
                height = 10.dp,
                fillColorOverride = if (isLight) cardAccentColor else null
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recessed Telemetry Grid: Duration, Completed, Remaining, Target Date
            SkeuoRecessedWell(
                cornerRadius = 12.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GoalMetricItem(
                        label = NivoStrings.durationLabel(language),
                        value = NivoStrings.daysUnit(language, goal.durationDays)
                    )
                    GoalMetricItem(
                        label = NivoStrings.completedLabel(language),
                        value = "${effectiveCompleted.toLocalizedDigits(language)}/${goal.durationDays.toLocalizedDigits(language)}"
                    )
                    GoalMetricItem(
                        label = NivoStrings.remainingLabel(language),
                        value = "${effectiveRemaining.toLocalizedDigits(language)} ${if (language == AppLanguage.BANGLA) "দিন" else "d"}"
                    )
                    GoalMetricItem(
                        label = NivoStrings.expectedCompletionLabel(language),
                        value = dateFormat.format(Date(goal.expectedCompletionDateMillis))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            SkeuoCarvedDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Alarm Status & Physical Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onConfigureAlarm() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeuoLedIndicator(isOn = linkedAlarm?.isEnabled == true, size = 7.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Alarm,
                        contentDescription = null,
                        tint = if (linkedAlarm?.isEnabled == true) AccentRed else SecondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (linkedAlarm != null) {
                                "${linkedAlarm.formattedTime(language)} • ${
                                    if (linkedAlarm.isEnabled) NivoStrings.alarmOnLabel(language)
                                    else NivoStrings.alarmOffLabel(language)
                                }"
                            } else {
                                NivoStrings.noAlarmSetLabel(language)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (linkedAlarm?.isEnabled == true) PrimaryText else SecondaryText
                        )
                        if (linkedAlarm != null) {
                            Text(
                                text = NivoStrings.formatRepeatDaysSummary(linkedAlarm.repeatDays, true, language),
                                fontSize = 10.sp,
                                color = SecondaryText
                            )
                        }
                    }
                }

                if (linkedAlarm != null) {
                    SkeuoSwitch(
                        checked = linkedAlarm.isEnabled,
                        onCheckedChange = onToggleAlarm,
                        testTag = "goal_alarm_switch_${goal.id}"
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isLight) Brush.verticalGradient(listOf(Color.White, RecessedWell))
                                else Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                            )
                            .border(1.dp, if (isLight) OutlineGrey else BevelHighlight, RoundedCornerShape(10.dp))
                            .clickable { onConfigureAlarm() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = NivoStrings.addAlarmQuickLabel(language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalMetricItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = SecondaryText
        )
    }
}

@Composable
private fun GoalsEmptyState(
    language: AppLanguage,
    onCreateGoalClick: () -> Unit
) {
    SkeuoPanelCard(
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom)))
                    .border(1.2.dp, BevelHighlight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = NivoStrings.noActiveGoal(language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = NivoStrings.noActiveGoalDesc(language),
                fontSize = 13.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            NivoButton(
                text = NivoStrings.createFirstGoal(language),
                onClick = onCreateGoalClick,
                icon = Icons.Filled.Add,
                testTag = "empty_create_goal_button",
                modifier = Modifier.fillMaxWidth(0.88f)
            )
        }
    }
}
