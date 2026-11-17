package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarm.AlarmConstants
import com.example.alarm.AlarmNotificationHelper
import com.example.data.localization.NivoStrings
import com.example.ui.navigation.BottomNavDestination
import com.example.ui.navigation.Screen
import com.example.ui.screens.AboutDeveloperScreen
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CreateEditAlarmScreen
import com.example.ui.screens.CreateGoalScreen
import com.example.ui.screens.GoalCompletionScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.PinMode
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BevelHighlight
import com.example.ui.theme.BevelShadow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.LocalNivoColors
import com.example.ui.theme.MetalBottom
import com.example.ui.theme.MetalTop
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.PureBlack
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverAccent
import com.example.ui.viewmodel.NivoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AlarmNotificationHelper.ensureNotificationChannel(this)
        val openAlarmsTab = intent?.getBooleanExtra(AlarmConstants.EXTRA_OPEN_ALARMS_TAB, false) == true
        setContent {
            val viewModel: NivoViewModel = viewModel()
            val selectedTheme by viewModel.selectedTheme.collectAsState()
            MyApplicationTheme(theme = selectedTheme) {
                NivoApp(
                    viewModel = viewModel,
                    initialTab = if (openAlarmsTab) BottomNavDestination.ALARMS else BottomNavDestination.HOME
                )
            }
        }
    }
}

@Composable
fun NivoApp(
    viewModel: NivoViewModel = viewModel(),
    initialTab: BottomNavDestination = BottomNavDestination.HOME
) {
    var currentTab by remember { mutableStateOf(initialTab) }
    var currentSubScreen by remember { mutableStateOf<Screen?>(null) }
    val language by viewModel.language.collectAsState()
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh clock on foreground resume & device date/time/timezone changes for instant midnight resets
    DisposableEffect(lifecycleOwner, context) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshClockAndCheckIns()
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        val dateChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                viewModel.refreshClockAndCheckIns()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, dateChangeReceiver, filter, ContextCompat.RECEIVER_EXPORTED)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            try {
                context.unregisterReceiver(dateChangeReceiver)
            } catch (_: Exception) {
            }
        }
    }

    var isUnlocked by remember { mutableStateOf(!viewModel.pinManager.isPinEnabled()) }

    if (isPinEnabled && !isUnlocked) {
        PinLockScreen(
            mode = PinMode.AUTHENTICATE,
            pinManager = viewModel.pinManager,
            language = language,
            onSuccess = { isUnlocked = true }
        )
        return
    }

    BackHandler(enabled = currentSubScreen != null || currentTab != BottomNavDestination.HOME) {
        if (currentSubScreen != null) {
            currentSubScreen = null
        } else if (currentTab != BottomNavDestination.HOME) {
            currentTab = BottomNavDestination.HOME
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = DarkBackground,
        bottomBar = {
            if (currentSubScreen == null) {
                SkeuoBottomDock(
                    currentTab = currentTab,
                    language = language,
                    onSelectTab = { currentTab = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val sub = currentSubScreen) {
                is Screen.CreateGoal -> {
                    CreateGoalScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentSubScreen = null }
                    )
                }

                is Screen.EditGoal -> {
                    CreateGoalScreen(
                        viewModel = viewModel,
                        goalIdToEdit = sub.goalId,
                        onNavigateBack = { currentSubScreen = null }
                    )
                }

                is Screen.CreateAlarm -> {
                    CreateEditAlarmScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentSubScreen = null }
                    )
                }

                is Screen.EditAlarm -> {
                    CreateEditAlarmScreen(
                        viewModel = viewModel,
                        alarmIdToEdit = sub.alarmId,
                        onNavigateBack = { currentSubScreen = null }
                    )
                }

                is Screen.ConfigureHabitAlarm -> {
                    CreateEditAlarmScreen(
                        viewModel = viewModel,
                        habitIdToConfigure = sub.habitId,
                        onNavigateBack = { currentSubScreen = null }
                    )
                }

                is Screen.GoalCompletion -> {
                    GoalCompletionScreen(
                        goalId = sub.goalId,
                        viewModel = viewModel,
                        onReturnHome = {
                            currentSubScreen = null
                            currentTab = BottomNavDestination.HOME
                        }
                    )
                }

                is Screen.AboutDeveloper -> {
                    AboutDeveloperScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentSubScreen = null }
                    )
                }

                is Screen.SetPin -> {
                    PinLockScreen(
                        mode = PinMode.SETUP,
                        pinManager = viewModel.pinManager,
                        language = language,
                        onSuccess = {
                            viewModel.refreshPinStatus()
                            currentSubScreen = null
                        },
                        onCancel = { currentSubScreen = null }
                    )
                }

                is Screen.DisablePin -> {
                    PinLockScreen(
                        mode = PinMode.DISABLE,
                        pinManager = viewModel.pinManager,
                        language = language,
                        onSuccess = {
                            viewModel.refreshPinStatus()
                            currentSubScreen = null
                        },
                        onCancel = { currentSubScreen = null }
                    )
                }

                null -> {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(140))
                        },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            BottomNavDestination.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    onCreateGoalClick = { currentSubScreen = Screen.CreateGoal },
                                    onNavigateToGoals = { currentTab = BottomNavDestination.GOALS },
                                    onNavigateToAlarms = { currentTab = BottomNavDestination.ALARMS },
                                    onNavigateToCalendar = { currentTab = BottomNavDestination.CALENDAR },
                                    onGoalCompleted = { goalId ->
                                        currentSubScreen = Screen.GoalCompletion(goalId)
                                    }
                                )
                            }

                            BottomNavDestination.CALENDAR -> {
                                CalendarScreen(viewModel = viewModel)
                            }

                            BottomNavDestination.GOALS -> {
                                GoalsScreen(
                                    viewModel = viewModel,
                                    onCreateGoalClick = { currentSubScreen = Screen.CreateGoal },
                                    onEditGoalClick = { goalId ->
                                        currentSubScreen = Screen.EditGoal(goalId)
                                    },
                                    onConfigureHabitAlarm = { habitId ->
                                        currentSubScreen = Screen.ConfigureHabitAlarm(habitId)
                                    }
                                )
                            }

                            BottomNavDestination.ALARMS -> {
                                AlarmsScreen(
                                    viewModel = viewModel,
                                    onCreateStandaloneAlarm = { currentSubScreen = Screen.CreateAlarm },
                                    onEditAlarm = { alarmId ->
                                        currentSubScreen = Screen.EditAlarm(alarmId)
                                    },
                                    onConfigureHabitAlarm = { habitId ->
                                        currentSubScreen = Screen.ConfigureHabitAlarm(habitId)
                                    }
                                )
                            }

                            BottomNavDestination.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateToAboutDeveloper = {
                                        currentSubScreen = Screen.AboutDeveloper
                                    },
                                    onNavigateToAlarms = {
                                        currentTab = BottomNavDestination.ALARMS
                                    },
                                    onRequestSetPin = {
                                        currentSubScreen = Screen.SetPin
                                    },
                                    onRequestDisablePin = {
                                        currentSubScreen = Screen.DisablePin
                                    }
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun SkeuoBottomDock(
    currentTab: BottomNavDestination,
    language: com.example.data.localization.AppLanguage,
    onSelectTab: (BottomNavDestination) -> Unit
) {
    val isLight = LocalNivoColors.current.isLight
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isLight) {
                    Brush.verticalGradient(listOf(Color.White, PureBlack))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF17181D), PureBlack))
                }
            )
            .testTag("nivo_bottom_navigation")
    ) {
        if (isLight) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(OutlineGrey)
            )
        } else {
            // Machined top metallic lip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BevelHighlight.copy(alpha = 0.7f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BevelShadow)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavDestination.values().forEach { destination ->
                val isSelected = currentTab == destination
                val title = when (destination) {
                    BottomNavDestination.HOME -> NivoStrings.navHome(language)
                    BottomNavDestination.CALENDAR -> NivoStrings.navCalendar(language)
                    BottomNavDestination.GOALS -> NivoStrings.navGoals(language)
                    BottomNavDestination.ALARMS -> NivoStrings.navAlarms(language)
                    BottomNavDestination.SETTINGS -> NivoStrings.navSettings(language)
                }

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "nav_key_scale"
                )

                val keyShape = RoundedCornerShape(14.dp)
                val keyBg = when {
                    isLight && isSelected -> Brush.verticalGradient(listOf(Color(0xFFE2F8E9), Color(0xFFD6F3DF)))
                    isLight -> Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    isSelected -> Brush.verticalGradient(listOf(Color(0xFF31171B), Color(0xFF1A0E10)))
                    else -> Brush.verticalGradient(listOf(MetalTop.copy(alpha = 0.45f), MetalBottom))
                }
                val keyBorder = if (isLight) {
                    val c = if (isSelected) PrimaryRed.copy(alpha = 0.35f) else Color.Transparent
                    Brush.verticalGradient(listOf(c, c))
                } else {
                    Brush.verticalGradient(
                        listOf(
                            if (isSelected) AccentRed.copy(alpha = 0.6f) else BevelHighlight.copy(alpha = 0.4f),
                            BevelShadow
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .scale(scale)
                        .clip(keyShape)
                        .background(keyBg)
                        .border(
                            width = if (isSelected) 1.dp else 0.8.dp,
                            brush = keyBorder,
                            shape = keyShape
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(color = PrimaryRed.copy(alpha = 0.3f)),
                            onClick = { onSelectTab(destination) }
                        )
                        .testTag(destination.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // LED / pill status bar above icon
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 18.dp else 6.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        isSelected -> AccentRed
                                        isLight -> Color(0xFFD5DCE4)
                                        else -> Color(0xFF2D303A)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = title,
                            tint = when {
                                isSelected -> AccentRed
                                isLight -> SecondaryText
                                else -> SilverAccent.copy(alpha = 0.72f)
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryText else SecondaryText
                        )
                    }
                }
            }
        }
    }
}
