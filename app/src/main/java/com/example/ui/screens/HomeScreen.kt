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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.MotivationalQuotes
import com.example.data.localization.NivoStrings
import com.example.data.localization.toLocalizedDigits
import com.example.data.model.AlarmType
import com.example.data.model.TrackingMode
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.MotivationalCard
import com.example.ui.components.NivoButton
import com.example.ui.components.NivoRibbonSymbol
import com.example.ui.components.SkeuoCarvedDivider
import com.example.ui.components.SkeuoDetentChip
import com.example.ui.components.SkeuoLedIndicator
import com.example.ui.components.SkeuoPanelCard
import com.example.ui.components.SkeuoRecessedWell
import com.example.ui.components.SkeuoSectionHeader
import com.example.ui.components.SkeuoTelemetryCell
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.CardBackground
import com.example.ui.theme.DarkBackground
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
import com.example.ui.viewmodel.NivoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: NivoViewModel,
    onCreateGoalClick: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToAlarms: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onGoalCompleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedGoal by viewModel.selectedGoal.collectAsState()
    val allGoals by viewModel.goals.collectAsState()
    val checkIns by viewModel.checkIns.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val language by viewModel.language.collectAsState()
    val completedEvent by viewModel.goalCompletedEvent.collectAsState()
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()
    val sessionQuoteIndex by viewModel.sessionQuoteIndex.collectAsState()
    val isLight = LocalNivoColors.current.isLight

    var showGoalSwitchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(completedEvent) {
        completedEvent?.let { goal ->
            onGoalCompleted(goal.id)
            viewModel.clearGoalCompletedEvent()
        }
    }

    val isEligible = remember(selectedGoal, checkIns, currentTimeMillis) {
        viewModel.isGoalEligibleForCheckIn(selectedGoal)
    }
    val (hours, minutes, seconds) = remember(selectedGoal, currentTimeMillis) {
        viewModel.getCountdownForGoal(selectedGoal)
    }

    val locale = remember(language) {
        if (language == AppLanguage.BANGLA) Locale.forLanguageTag("bn-BD") else Locale.US
    }
    val dayDateFormat = remember(locale) { SimpleDateFormat("EEEE • MMM d, yyyy", locale) }
    val clockTimeFormat = remember { SimpleDateFormat("hh:mm:ss", Locale.US) }
    val clockAmPmFormat = remember { SimpleDateFormat("a", Locale.US) }

    val nowDate = Date(currentTimeMillis)
    val formattedDayDate = dayDateFormat.format(nowDate)
    val formattedClockDigits = clockTimeFormat.format(nowDate).toLocalizedDigits(language)
    val formattedAmPm = clockAmPmFormat.format(nowDate)

    // Summary counts across all Habits
    val totalHabitsCount = allGoals.size
    val completedHabitsCount = allGoals.count { goal ->
        goal.isCompleted || goal.getEffectiveCompletedDays(currentTimeMillis) >= goal.durationDays
    }
    val remainingHabitsCount = (totalHabitsCount - completedHabitsCount).coerceAtLeast(0)

    val upcomingAlarms = remember(alarms) {
        alarms.filter { it.isEnabled }.sortedWith(compareBy({ it.hour24 }, { it.minute })).take(3)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Hardware Brand Strip + Date & Realistic Digital Clock Module
        SkeuoPanelCard(
            cornerRadius = 20.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NivoRibbonSymbol(size = 40.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "N I V O",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp,
                                color = PrimaryText
                            )
                            Text(
                                text = NivoStrings.appTagline(language),
                                fontSize = 11.sp,
                                color = SilverAccent.copy(alpha = 0.78f)
                            )
                        }
                    }

                    // Active LED pill badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLight) Color(0xFFE2F8E9) else Color(0xFF0A0B0E))
                            .border(1.dp, if (isLight) PrimaryRed.copy(alpha = 0.35f) else OutlineGrey, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeuoLedIndicator(isOn = true, size = 6.dp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (language == AppLanguage.BANGLA) "অফলাইন" else "OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = SilverAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recessed LCD Digital Clock & Date Bay
                SkeuoRecessedWell(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_digital_clock_card"),
                    cornerRadius = 14.dp,
                    accentGlow = true,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = NivoStrings.greeting(language, userName),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("home_greeting_text")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedDayDate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SilverAccent.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Realistic LCD Digital Time
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formattedClockDigits,
                                style = TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentRed,
                                    fontFeatureSettings = "tnum",
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedAmPm,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SilverAccent,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Daily Progress Overview Console (Active / Completed / Remaining)
        SkeuoPanelCard(
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SkeuoSectionHeader(
                    title = NivoStrings.dailyOverviewTitle(language),
                    trailing = NivoStrings.homeSubtitle(language)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeuoTelemetryCell(
                        label = NivoStrings.totalHabitsLabel(language),
                        value = totalHabitsCount.toLocalizedDigits(language),
                        modifier = Modifier.weight(1f)
                    )
                    SkeuoTelemetryCell(
                        label = NivoStrings.completedHabitsLabel(language),
                        value = completedHabitsCount.toLocalizedDigits(language),
                        highlightAccent = completedHabitsCount > 0,
                        modifier = Modifier.weight(1f)
                    )
                    SkeuoTelemetryCell(
                        label = NivoStrings.remainingHabitsLabel(language),
                        value = remainingHabitsCount.toLocalizedDigits(language),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Access Tactile Keycaps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeuoDetentChip(
                        label = NivoStrings.newGoal(language),
                        selected = true,
                        icon = Icons.Filled.Add,
                        onClick = onCreateGoalClick,
                        modifier = Modifier.weight(1f)
                    )
                    SkeuoDetentChip(
                        label = NivoStrings.navGoals(language),
                        selected = false,
                        icon = Icons.Filled.CheckCircle,
                        onClick = onNavigateToGoals,
                        modifier = Modifier.weight(1f)
                    )
                    SkeuoDetentChip(
                        label = NivoStrings.navAlarms(language),
                        selected = false,
                        icon = Icons.Filled.Alarm,
                        onClick = onNavigateToAlarms,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedGoal == null) {
            HomeEmptyState(
                language = language,
                onCreateGoalClick = onCreateGoalClick
            )
        } else {
            val goal = selectedGoal!!
            val (autoHours, autoMinutes, autoSeconds) = viewModel.getAutomaticCountdownForGoal(goal)
            val isAutoMode = goal.trackingMode == TrackingMode.AUTOMATIC
            val isDailyResetMode = goal.trackingMode == TrackingMode.DAILY_RESET
            val isHighlightedMode = isAutoMode || isDailyResetMode

            // 3. Selected Goal Switcher Plate + Mode Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isLight) Brush.verticalGradient(listOf(Color.White, CardBackground))
                            else Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                        )
                        .border(1.dp, if (isLight) OutlineGrey else BevelHighlight, RoundedCornerShape(14.dp))
                        .clickable {
                            if (allGoals.size > 1) {
                                showGoalSwitchDialog = true
                            } else {
                                onNavigateToGoals()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (allGoals.size > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = NivoStrings.switchGoalTitle(language),
                            tint = AccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mode Instrument Badge
                val modeBadgeText = when (goal.trackingMode) {
                    TrackingMode.AUTOMATIC -> NivoStrings.autoModeShort(language)
                    TrackingMode.DAILY_RESET -> NivoStrings.dailyResetModeShort(language)
                    TrackingMode.MANUAL -> NivoStrings.manualModeShort(language)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isLight && isHighlightedMode -> Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color(0xFFD6F3DF)))
                                isLight -> Brush.verticalGradient(listOf(Color.White, RecessedWell))
                                isHighlightedMode -> Brush.verticalGradient(listOf(Color(0xFF3E181D), Color(0xFF210D10)))
                                else -> Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
                            }
                        )
                        .border(
                            1.dp,
                            if (isHighlightedMode) AccentRed.copy(alpha = 0.6f) else OutlineGrey,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeuoLedIndicator(isOn = isHighlightedMode, size = 6.dp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = modeBadgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlightedMode) (if (isLight) DarkRed else PrimaryText) else SilverAccent
                    )
                }
            }

            if (showGoalSwitchDialog) {
                AlertDialog(
                    onDismissRequest = { showGoalSwitchDialog = false },
                    containerColor = CardBackground,
                    title = {
                        Text(
                            text = NivoStrings.switchGoalTitle(language),
                            color = PrimaryText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column {
                            allGoals.forEach { g ->
                                val isCur = g.id == goal.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCur) OutlineGrey.copy(alpha = 0.35f) else CardBackground)
                                        .clickable {
                                            viewModel.selectGoal(g.id)
                                            showGoalSwitchDialog = false
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isCur) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isCur) AccentRed else SecondaryText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = g.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = if (isCur) AccentRed else PrimaryText
                                        )
                                        Text(
                                            text = "${NivoStrings.daysUnit(language, g.durationDays)} • ${NivoStrings.daysRemaining(language, g.remainingDays)}",
                                            fontSize = 12.sp,
                                            color = SecondaryText
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showGoalSwitchDialog = false }) {
                            Text(NivoStrings.doneButton(language), color = AccentRed, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. EXACTLY ONE CIRCULAR PROGRESS RING PER GOAL (Skeuomorphic Instrument Dial)
            val effectiveCompleted = goal.getEffectiveCompletedDays(currentTimeMillis)
            val effectiveRemaining = goal.getEffectiveRemainingDays(currentTimeMillis)
            val effectiveFraction = goal.getEffectiveProgressFraction(currentTimeMillis)

            SkeuoPanelCard(
                modifier = Modifier.testTag("single_goal_progress_card"),
                isHighlighted = !isLight,
                cornerRadius = 22.dp,
                contentPadding = PaddingValues(vertical = 22.dp, horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressRing(
                        progressFraction = effectiveFraction,
                        completedDays = effectiveCompleted,
                        remainingDays = effectiveRemaining,
                        totalDays = goal.durationDays,
                        size = 216.dp,
                        strokeWidth = 16.dp,
                        primaryColor = PrimaryRed,
                        secondaryColor = DarkRed,
                        testTag = "single_goal_progress_ring",
                        language = language
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Action Console (Manual Check-In OR Automatic Countdown)
            if (isAutoMode) {
                SkeuoPanelCard(
                    modifier = Modifier.testTag("auto_tracking_info_card"),
                    cornerRadius = 16.dp,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Brush.verticalGradient(listOf(PrimaryRed.copy(alpha = 0.35f), DarkRed.copy(alpha = 0.25f))))
                                    .border(1.dp, BevelShadow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = AccentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = NivoStrings.autoModeShort(language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = NivoStrings.nextAutoDayCountdown(language, autoHours, autoMinutes, autoSeconds),
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentRed,
                                        fontFeatureSettings = "tnum"
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = NivoStrings.autoModeDesc(language),
                            fontSize = 12.sp,
                            color = SecondaryText,
                            lineHeight = 17.sp
                        )
                    }
                }
            } else {
                if (!isEligible) {
                    NivoButton(
                        text = NivoStrings.checkedInForToday(language),
                        onClick = { },
                        enabled = false,
                        icon = Icons.Filled.Check,
                        testTag = "check_in_button",
                        containerColor = CardBackground,
                        contentColor = SecondaryText
                    )
                } else {
                    NivoButton(
                        text = NivoStrings.checkInButton(language),
                        onClick = {
                            viewModel.checkInSelectedGoal { completedGoal ->
                                onGoalCompleted(completedGoal.id)
                            }
                        },
                        enabled = true,
                        icon = Icons.Filled.Check,
                        testTag = "check_in_button",
                        containerColor = PrimaryRed,
                        contentColor = if (isLight) Color.White else PrimaryText
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SkeuoRecessedWell(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 12.dp,
                    accentGlow = !isEligible,
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = if (!isEligible) AccentRed else SilverAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val countdownText = when {
                            !isEligible && isDailyResetMode -> NivoStrings.nextMidnightResetIn(language, hours, minutes, seconds)
                            !isEligible -> NivoStrings.nextCheckInIn(language, hours, minutes, seconds)
                            else -> NivoStrings.checkInReady(language)
                        }
                        Text(
                            text = countdownText,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = if (!isEligible) PrimaryText else AccentRed,
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "tnum"
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Upcoming Reminders / Alarms Console
            SkeuoPanelCard(
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(14.dp),
                onClick = onNavigateToAlarms
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SkeuoSectionHeader(
                            title = NivoStrings.upcomingRemindersTitle(language),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = NivoStrings.manageAlarmsAction(language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (upcomingAlarms.isEmpty()) {
                        SkeuoRecessedWell(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 12.dp,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Alarm,
                                    contentDescription = null,
                                    tint = SecondaryText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = NivoStrings.noUpcomingAlarms(language),
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }
                        }
                    } else {
                        SkeuoRecessedWell(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 12.dp,
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                upcomingAlarms.forEachIndexed { idx, alarm ->
                                    if (idx > 0) {
                                        SkeuoCarvedDivider()
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            SkeuoLedIndicator(isOn = true, size = 6.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = alarm.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = PrimaryText,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = NivoStrings.formatRepeatDaysSummary(
                                                        alarm.repeatDays,
                                                        alarm.type == AlarmType.HABIT,
                                                        language
                                                    ),
                                                    fontSize = 11.sp,
                                                    color = SecondaryText
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = alarm.formattedTime(language),
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = AccentRed,
                                                fontFeatureSettings = "tnum"
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Motivational Quote Nameplate (Fresh per app session, stable across navigation/resume, localized live)
            val (quoteText, quoteAuthor) = remember(sessionQuoteIndex, language) {
                MotivationalQuotes.getQuoteByIndex(sessionQuoteIndex, language)
            }
            MotivationalCard(
                quote = quoteText,
                author = quoteAuthor
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeEmptyState(
    language: AppLanguage,
    onCreateGoalClick: () -> Unit
) {
    SkeuoPanelCard(
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(vertical = 32.dp, horizontal = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom)))
                    .border(1.2.dp, BevelHighlight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Flag,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = NivoStrings.noActiveGoal(language),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = NivoStrings.noActiveGoalDesc(language),
                fontSize = 13.sp,
                color = SecondaryText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            NivoButton(
                text = NivoStrings.createFirstGoal(language),
                onClick = onCreateGoalClick,
                icon = Icons.Filled.Add,
                testTag = "create_first_goal_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
