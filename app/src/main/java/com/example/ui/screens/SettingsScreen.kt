package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.localization.NivoStrings
import com.example.ui.components.NivoLogo
import com.example.ui.components.SkeuoCarvedDivider
import com.example.ui.components.SkeuoPanelCard
import com.example.ui.components.SkeuoRecessedWell
import com.example.ui.components.SkeuoSectionHeader
import com.example.ui.components.SkeuoSwitch
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardElevated
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkRed
import com.example.ui.theme.OutlineGrey
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.PrimaryText
import com.example.ui.theme.RecessedWell
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SilverAccent
import com.example.ui.viewmodel.NivoViewModel

@Composable
fun SettingsScreen(
    viewModel: NivoViewModel,
    onNavigateToAboutDeveloper: () -> Unit,
    onNavigateToAlarms: () -> Unit = {},
    onRequestSetPin: () -> Unit,
    onRequestDisablePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val language by viewModel.language.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val alarms by viewModel.alarms.collectAsState()

    val enabledAlarmCount = remember(alarms) { alarms.count { it.isEnabled } }

    var showNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userName) }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showAboutNivoDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = NivoStrings.settingsTitle(language),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
        Text(
            text = NivoStrings.settingsSubtitle(language),
            fontSize = 13.sp,
            color = SecondaryText,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 1. Profile
        SkeuoSectionHeader(NivoStrings.sectionPersonalization(language))
        Spacer(modifier = Modifier.height(4.dp))
        SettingsControlPanel {
            SettingsItemRow(
                icon = Icons.Filled.Person,
                title = NivoStrings.yourName(language),
                subtitle = userName,
                onClick = {
                    tempName = userName
                    showNameDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Language
        SkeuoSectionHeader(NivoStrings.sectionLanguage(language))
        Spacer(modifier = Modifier.height(4.dp))
        SettingsControlPanel {
            SettingsItemRow(
                icon = Icons.Filled.Language,
                title = NivoStrings.sectionLanguage(language),
                subtitle = "${language.displayName} (${language.nativeName})",
                onClick = { showLanguageDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2b. Display & Theme
        SkeuoSectionHeader(NivoStrings.sectionDisplayTheme(language))
        Spacer(modifier = Modifier.height(4.dp))
        SettingsControlPanel {
            SettingsItemRow(
                icon = Icons.Filled.Palette,
                title = NivoStrings.themeSelectionTitle(language),
                subtitle = if (selectedTheme == com.example.ui.theme.NivoThemeStyle.LIGHT) NivoStrings.themeLightName(language) else NivoStrings.themeDarkRedName(language),
                onClick = { showThemeDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Notifications, Alarm Settings & PIN Security (NO theme selector!)
        SkeuoSectionHeader(NivoStrings.sectionSecurity(language))
        Spacer(modifier = Modifier.height(4.dp))
        SettingsControlPanel {
            SettingsToggleRow(
                icon = Icons.Filled.Notifications,
                title = NivoStrings.reminderTitle(language),
                subtitle = NivoStrings.reminderSubtitle(language),
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                testTag = "settings_notifications_switch"
            )
            SkeuoCarvedDivider()
            SettingsItemRow(
                icon = Icons.Filled.Alarm,
                title = NivoStrings.alarmSettingsTitle(language),
                subtitle = NivoStrings.alarmSettingsSubtitle(language, enabledAlarmCount),
                onClick = onNavigateToAlarms
            )
            SkeuoCarvedDivider()
            SettingsToggleRow(
                icon = Icons.Filled.Lock,
                title = NivoStrings.appLockTitle(language),
                subtitle = if (isPinEnabled) {
                    if (language == AppLanguage.BANGLA) "৪-সংখ্যার পিন দিয়ে সুরক্ষিত" else "Protected with 4-digit PIN"
                } else {
                    NivoStrings.appLockSubtitle(language)
                },
                checked = isPinEnabled,
                onCheckedChange = { checked ->
                    if (checked) {
                        onRequestSetPin()
                    } else {
                        onRequestDisablePin()
                    }
                },
                testTag = "settings_pin_switch"
            )

            if (isPinEnabled) {
                SkeuoCarvedDivider()
                SettingsItemRow(
                    icon = Icons.Filled.Key,
                    title = NivoStrings.changePin(language),
                    subtitle = if (language == AppLanguage.BANGLA) "নতুন ৪-সংখ্যার পিন সেট করুন" else "Update your 4-digit lock code",
                    onClick = onRequestSetPin
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Privacy, Data Backup, Data Restore, Reset
        SkeuoSectionHeader(NivoStrings.sectionData(language))
        Spacer(modifier = Modifier.height(4.dp))
        SettingsControlPanel {
            SettingsItemRow(
                icon = Icons.Filled.Security,
                title = NivoStrings.privacyTitle(language),
                subtitle = NivoStrings.privacySubtitle(language),
                onClick = { }
            )
            SkeuoCarvedDivider()
            SettingsItemRow(
                icon = Icons.Filled.Download,
                title = NivoStrings.exportDataTitle(language),
                subtitle = NivoStrings.exportDataDesc(language),
                onClick = { showExportDialog = true },
                testTag = "settings_data_backup_row"
            )
            SkeuoCarvedDivider()
            SettingsItemRow(
                icon = Icons.Filled.Restore,
                title = NivoStrings.restoreDataTitle(language),
                subtitle = NivoStrings.restoreDataDesc(language),
                onClick = {
                    restoreJsonInput = ""
                    showRestoreDialog = true
                },
                testTag = "settings_data_restore_row"
            )
            SkeuoCarvedDivider()
            SettingsItemRow(
                icon = Icons.Filled.DeleteForever,
                title = NivoStrings.dataManagementTitle(language),
                subtitle = NivoStrings.dataManagementSubtitle(language),
                onClick = { showResetDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. About NIVO & About Developer
        SkeuoSectionHeader(NivoStrings.sectionAbout(language))
        Spacer(modifier = Modifier.height(4.dp))
        SettingsControlPanel {
            SettingsItemRow(
                icon = Icons.Filled.Info,
                title = NivoStrings.aboutNivo(language),
                subtitle = "Version 2.0 • Brand & Vision",
                onClick = { showAboutNivoDialog = true }
            )
            SkeuoCarvedDivider()
            SettingsItemRow(
                icon = Icons.Filled.Person,
                title = NivoStrings.aboutDeveloper(language),
                subtitle = NivoStrings.devName(language),
                onClick = onNavigateToAboutDeveloper
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        NivoLogo(
            iconSize = 54.dp,
            showTagline = true,
            language = language,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = CardBackground,
            title = {
                Text(
                    NivoStrings.selectLanguageTitle(language),
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    AppLanguage.values().forEach { langOption ->
                        val isSelected = language == langOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CardElevated else CardBackground)
                                .clickable {
                                    viewModel.setLanguage(langOption)
                                    showLanguageDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) AccentRed else SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = langOption.nativeName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AccentRed else PrimaryText
                                )
                                Text(
                                    text = langOption.displayName,
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
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(NivoStrings.doneButton(language), color = AccentRed)
                }
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = CardBackground,
            title = {
                Text(
                    NivoStrings.themeSelectionTitle(language),
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    com.example.ui.theme.NivoThemeStyle.values().forEach { themeOption ->
                        val isSelected = selectedTheme == themeOption
                        val optionTitle = if (themeOption == com.example.ui.theme.NivoThemeStyle.LIGHT) NivoStrings.themeLightName(language) else NivoStrings.themeDarkRedName(language)
                        val optionDesc = if (themeOption == com.example.ui.theme.NivoThemeStyle.LIGHT) NivoStrings.themeLightDesc(language) else NivoStrings.themeDarkRedDesc(language)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CardElevated else CardBackground)
                                .clickable {
                                    viewModel.setTheme(themeOption)
                                    showThemeDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) AccentRed else SecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = optionTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AccentRed else PrimaryText
                                )
                                Text(
                                    text = optionDesc,
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
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(NivoStrings.doneButton(language), color = AccentRed)
                }
            }
        )
    }

    // Edit Name Dialog
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = CardBackground,
            title = { Text(NivoStrings.updateName(language), color = PrimaryText, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        focusedContainerColor = RecessedWell,
                        unfocusedContainerColor = RecessedWell,
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = OutlineGrey,
                        cursorColor = AccentRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateUserName(tempName)
                        showNameDialog = false
                    }
                ) {
                    Text(NivoStrings.saveGoalButton(language), color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text(NivoStrings.cancelButton(language), color = SecondaryText)
                }
            }
        )
    }

    // Export Data Backup Dialog
    if (showExportDialog) {
        val backupJson = remember { viewModel.exportBackupJson() }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = CardBackground,
            title = { Text(NivoStrings.exportDataTitle(language), color = PrimaryText, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        NivoStrings.exportDataDesc(language),
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SkeuoRecessedWell(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        cornerRadius = 10.dp,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = backupJson,
                                fontSize = 11.sp,
                                color = SilverAccent
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("NIVO_BACKUP", backupJson)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, NivoStrings.copiedSuccess(language), Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Text(NivoStrings.copyToClipboard(language), color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(NivoStrings.cancelButton(language), color = SecondaryText)
                }
            }
        )
    }

    // Data Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            containerColor = CardBackground,
            title = { Text(NivoStrings.restoreDataTitle(language), color = PrimaryText, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        NivoStrings.restoreDataDesc(language),
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = {
                            Text(NivoStrings.restoreInputPlaceholder(language), color = SecondaryText, fontSize = 12.sp)
                        },
                        minLines = 4,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            focusedContainerColor = RecessedWell,
                            unfocusedContainerColor = RecessedWell,
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = OutlineGrey,
                            cursorColor = AccentRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("restore_backup_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val success = viewModel.importBackupJson(restoreJsonInput.trim())
                        Toast.makeText(
                            context,
                            if (success) NivoStrings.restoreSuccess(language) else NivoStrings.restoreFailed(language),
                            Toast.LENGTH_SHORT
                        ).show()
                        if (success) {
                            showRestoreDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_restore_button")
                ) {
                    Text(NivoStrings.restoreButton(language), color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(NivoStrings.cancelButton(language), color = SecondaryText)
                }
            }
        )
    }

    // Reset Data Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = CardBackground,
            title = { Text(NivoStrings.resetConfirmTitle(language), color = PrimaryText, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    NivoStrings.resetConfirmMessage(language),
                    color = SecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetData()
                        showResetDialog = false
                    }
                ) {
                    Text(NivoStrings.deleteButton(language), color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(NivoStrings.cancelButton(language), color = SecondaryText)
                }
            }
        )
    }

    // About NIVO Dialog
    if (showAboutNivoDialog) {
        AlertDialog(
            onDismissRequest = { showAboutNivoDialog = false },
            containerColor = CardBackground,
            title = {
                Text(NivoStrings.aboutNivo(language), color = PrimaryText, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NivoLogo(iconSize = 52.dp, showTagline = true, language = language)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.BANGLA) {
                            "নিভো হলো একটি ১০০% অফলাইন লক্ষ্য ও অভ্যাস ট্র্যাকার, যা প্রতিটি মানুষের মধ্যে অটল আত্মশৃঙ্খলা গড়ে তোলার লক্ষ্যে নির্মিত। কোনো সাবস্ক্রিপশন নেই, বিজ্ঞাপন নেই, ট্র্যাকিং নেই—শুধু আপনি এবং আপনার লক্ষ্য, প্রতিদিন এক ধাপ করে।"
                        } else {
                            "NIVO is an offline-first goal and habit tracker designed to help you build unbreakable discipline. No subscriptions, no ads, no trackers — just you and your goals, one day at a time."
                        },
                        color = SecondaryText,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutNivoDialog = false }) {
                    Text(NivoStrings.doneButton(language), color = AccentRed, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun SettingsControlPanel(content: @Composable () -> Unit) {
    SkeuoPanelCard(
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String = ""
) {
    val taggedModifier = if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier
    Row(
        modifier = taggedModifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(PrimaryRed.copy(alpha = 0.28f), DarkRed.copy(alpha = 0.22f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SecondaryText
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = SilverAccent.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.verticalGradient(listOf(PrimaryRed.copy(alpha = 0.28f), DarkRed.copy(alpha = 0.22f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SecondaryText
            )
        }
        SkeuoSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            testTag = testTag
        )
    }
}
