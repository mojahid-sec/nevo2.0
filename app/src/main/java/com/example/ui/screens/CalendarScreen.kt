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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.data.model.TrackingMode
import com.example.ui.components.SkeuoCarvedDivider
import com.example.ui.components.SkeuoIconKeyButton
import com.example.ui.components.SkeuoLedIndicator
import com.example.ui.components.SkeuoLinearGauge
import com.example.ui.components.SkeuoPanelCard
import com.example.ui.components.SkeuoRecessedWell
import com.example.ui.components.SkeuoTelemetryCell
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
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
import com.example.ui.theme.SilverAccent
import com.example.ui.theme.SubtleGrey
import com.example.ui.viewmodel.NivoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: NivoViewModel,
    modifier: Modifier = Modifier
) {
    val checkIns by viewModel.checkIns.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val selectedGoal by viewModel.selectedGoal.collectAsState()
    val language by viewModel.language.collectAsState()

    var displayedCal by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayKey by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time))
    }

    val locale = remember(language) {
        if (language == AppLanguage.BANGLA) Locale.forLanguageTag("bn-BD") else Locale.US
    }
    val monthFormat = remember(locale) { SimpleDateFormat("MMMM yyyy", locale) }
    val dayFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val timeFormat = remember(locale) { SimpleDateFormat("hh:mm a", locale) }
    val fullDateFormat = remember(locale) { SimpleDateFormat("EEEE, MMMM d, yyyy", locale) }

    val todayCalendar = remember { Calendar.getInstance() }
    val todayKey = remember { dayFormat.format(todayCalendar.time) }

    val year = displayedCal.get(Calendar.YEAR)
    val month = displayedCal.get(Calendar.MONTH)

    val monthStartCal = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = monthStartCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = monthStartCal.get(Calendar.DAY_OF_WEEK)
    val offset = firstDayOfWeek - 1

    val checkInsThisMonth = remember(checkIns, year, month) {
        checkIns.filter { record ->
            val recCal = Calendar.getInstance().apply { timeInMillis = record.timestampMillis }
            recCal.get(Calendar.YEAR) == year && recCal.get(Calendar.MONTH) == month
        }
    }

    val selectedDayRecords = remember(checkIns, selectedDayKey) {
        checkIns.filter { it.dateKey == selectedDayKey }
    }
    val selectedRecord = selectedDayRecords.firstOrNull()

    val parsedSelectedCal = remember(selectedDayKey) {
        Calendar.getInstance().apply {
            try {
                val date = dayFormat.parse(selectedDayKey)
                if (date != null) time = date
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val selectedDayEndMillis = remember(parsedSelectedCal) {
        (parsedSelectedCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }

    // Completed vs Incomplete Habits breakdown for selected date
    val habitDayStatuses = remember(goals, selectedDayRecords, selectedDayKey, selectedDayEndMillis) {
        goals.map { goal ->
            val isCompletedOnDate = when (goal.trackingMode) {
                TrackingMode.MANUAL,
                TrackingMode.DAILY_RESET -> selectedDayRecords.any { it.goalId == goal.id }
                TrackingMode.AUTOMATIC -> {
                    val startKey = dayFormat.format(goal.startDateMillis)
                    val endKey = dayFormat.format(goal.expectedCompletionDateMillis)
                    selectedDayKey >= startKey && selectedDayKey <= endKey && selectedDayKey <= todayKey
                }
            }
            goal to isCompletedOnDate
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("calendar_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = NivoStrings.progressCalendar(language),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
        Text(
            text = NivoStrings.calendarSubtitle(language),
            fontSize = 13.sp,
            color = SecondaryText,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Calendar Deck
        SkeuoPanelCard(
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SkeuoIconKeyButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        onClick = {
                            val prev = (displayedCal.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                            displayedCal = prev
                        },
                        keySize = 36.dp
                    )

                    Text(
                        text = monthFormat.format(displayedCal.time),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryText
                    )

                    SkeuoIconKeyButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        onClick = {
                            val next = (displayedCal.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                            displayedCal = next
                        },
                        keySize = 36.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val weekdays = if (language == AppLanguage.BANGLA) {
                    listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
                } else {
                    listOf("S", "M", "T", "W", "T", "F", "S")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SilverAccent.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val totalSlots = offset + daysInMonth
                val totalRows = (totalSlots + 6) / 7

                for (rowIndex in 0 until totalRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (colIndex in 0 until 7) {
                            val slot = rowIndex * 7 + colIndex
                            val dayNumber = slot - offset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val dateCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                }
                                val dateKey = dayFormat.format(dateCal.time)
                                val isSelected = dateKey == selectedDayKey
                                val isToday = dateKey == todayKey
                                val isCheckedIn = checkIns.any { it.dateKey == dateKey }
                                val isPastDay = dateCal.before(todayCalendar) && !isToday

                                DayCell(
                                    day = dayNumber,
                                    language = language,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    isCheckedIn = isCheckedIn,
                                    isPastDay = isPastDay,
                                    onClick = { selectedDayKey = dateKey },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Day Inspection Bay (Check-in details + Completed / Incomplete Habit Info)
        SkeuoPanelCard(
            modifier = Modifier.testTag("selected_day_details_card"),
            isHighlighted = selectedRecord != null,
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fullDateFormat.format(parsedSelectedCal.time),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val badgeText = when {
                        selectedRecord != null -> NivoStrings.completedStatus(language)
                        selectedDayKey == todayKey -> NivoStrings.todayStatus(language)
                        parsedSelectedCal.before(todayCalendar) -> NivoStrings.missedStatus(language)
                        else -> NivoStrings.upcomingStatus(language)
                    }
                    val badgeAccent = selectedRecord != null

                    val isLight = LocalNivoColors.current.isLight
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isLight && badgeAccent -> Color(0xFFE2F8E9)
                                    isLight -> RecessedWell
                                    badgeAccent -> PrimaryRed.copy(alpha = 0.25f)
                                    else -> Color(0xFF0A0B0E)
                                }
                            )
                            .border(
                                1.dp,
                                if (badgeAccent) AccentRed else OutlineGrey,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badgeAccent) AccentRed else SecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SkeuoRecessedWell(
                    cornerRadius = 12.dp,
                    accentGlow = selectedRecord != null,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Column {
                        if (selectedRecord != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = AccentRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.BANGLA) {
                                        "চেক-ইন সম্পন্ন: ${timeFormat.format(selectedRecord.timestampMillis)}"
                                    } else {
                                        "Checked in at ${timeFormat.format(selectedRecord.timestampMillis)}"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryText
                                )
                            }

                            if (selectedRecord.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "\"${selectedRecord.note}\"",
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }
                        } else {
                            Text(
                                text = if (selectedDayKey == todayKey) {
                                    NivoStrings.checkInReady(language)
                                } else if (parsedSelectedCal.before(todayCalendar)) {
                                    if (language == AppLanguage.BANGLA) "এই দিনে কোনো চেক-ইন রেকর্ড করা হয়নি।" else "No check-in was logged for this day."
                                } else {
                                    if (language == AppLanguage.BANGLA) "আসন্ন দিন, আপনার লক্ষ্যের অপেক্ষায়।" else "Future milestone awaiting your commitment."
                                },
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }

                        if (habitDayStatuses.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            SkeuoCarvedDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = NivoStrings.habitsStatusForDay(language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SilverAccent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            habitDayStatuses.forEach { (habit, done) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        SkeuoLedIndicator(isOn = done, size = 6.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = habit.title,
                                            fontSize = 12.sp,
                                            color = if (done) PrimaryText else SecondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (done) NivoStrings.completedHabitsLabel(language) else NivoStrings.incompleteHabitsLabel(language),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (done) AccentRed else SecondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Summary Console
        val consistencyPct = ((checkInsThisMonth.size.toFloat() / daysInMonth) * 100).toInt().coerceIn(0, 100)
        SkeuoPanelCard(
            modifier = Modifier.testTag("monthly_summary_card"),
            cornerRadius = 18.dp,
            contentPadding = PaddingValues(16.dp)
        ) {
            Column {
                Text(
                    text = NivoStrings.monthlySummary(language),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                Spacer(modifier = Modifier.height(10.dp))

                SkeuoLinearGauge(
                    progressFraction = consistencyPct / 100f,
                    height = 10.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkeuoTelemetryCell(
                        label = NivoStrings.totalCheckInsLabel(language),
                        value = "${checkInsThisMonth.size.toLocalizedDigits(language)} ${if (language == AppLanguage.BANGLA) "দিন" else "d"}",
                        highlightAccent = checkInsThisMonth.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                    SkeuoTelemetryCell(
                        label = NivoStrings.consistencyRate(language),
                        value = "${consistencyPct.toLocalizedDigits(language)}%",
                        highlightAccent = consistencyPct >= 50,
                        modifier = Modifier.weight(1f)
                    )
                    SkeuoTelemetryCell(
                        label = NivoStrings.expectedCompletionLabel(language),
                        value = selectedGoal?.title?.take(9) ?: "—",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DayCell(
    day: Int,
    language: AppLanguage,
    isSelected: Boolean,
    isToday: Boolean,
    isCheckedIn: Boolean,
    isPastDay: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = LocalNivoColors.current.isLight
    val shape = RoundedCornerShape(10.dp)
    val bgBrush = when {
        isLight && isSelected -> Brush.verticalGradient(listOf(PrimaryRed, PrimaryRed))
        isLight && isToday -> Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color(0xFFE2F8E9)))
        isLight && isCheckedIn -> Brush.verticalGradient(listOf(Color(0xFFF0FBF4), Color(0xFFF0FBF4)))
        isLight -> Brush.verticalGradient(listOf(RecessedWell, RecessedWell))
        isSelected -> Brush.verticalGradient(listOf(Color(0xFF3E191E), Color(0xFF210D10)))
        isToday -> Brush.verticalGradient(listOf(MetalTop, CardBackground, MetalBottom))
        else -> Brush.verticalGradient(listOf(Color(0xFF14151A), Color(0xFF0C0D10)))
    }
    val borderColor = when {
        isSelected -> AccentRed
        isLight && isToday -> PrimaryRed
        isLight -> OutlineGrey
        isToday -> BevelHighlight
        else -> BevelShadow
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(shape)
            .background(bgBrush)
            .border(width = if (isSelected) 1.4.dp else 0.8.dp, color = borderColor, shape = shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toLocalizedDigits(language),
                fontSize = 11.sp,
                fontWeight = if (isToday || isSelected || isCheckedIn) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isLight && isSelected -> Color.White
                    isSelected -> PrimaryText
                    isCheckedIn -> PrimaryText
                    isPastDay -> SubtleGrey
                    else -> SecondaryText
                }
            )

            Spacer(modifier = Modifier.height(1.dp))

            if (isCheckedIn) {
                SkeuoLedIndicator(isOn = true, size = 4.dp)
            } else {
                Spacer(modifier = Modifier.size(6.dp))
            }
        }
    }
}
