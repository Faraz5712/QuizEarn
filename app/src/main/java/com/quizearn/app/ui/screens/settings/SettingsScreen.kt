package com.quizearn.app.ui.screens.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.data.repository.PreferencesRepository
import com.quizearn.app.data.repository.PreferencesRepositoryImpl
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.QuizEarnButton
import com.quizearn.app.ui.components.QuizEarnButtonType
import com.quizearn.app.ui.components.QuizEarnCard
import com.quizearn.app.ui.theme.ButtonShape
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.ErrorCrimson
import com.quizearn.app.ui.theme.LocalSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onSignOut: () -> Unit,
    onAccountDeleted: () -> Unit,
    preferencesRepository: PreferencesRepository = remember { PreferencesRepositoryImpl.getInstance() },
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()

    // Persistent preference toggles backed by AndroidX DataStore
    val soundEffectsEnabled by preferencesRepository.soundEffectsEnabled.collectAsState(initial = true)
    val hapticsEnabled by preferencesRepository.hapticsEnabled.collectAsState(initial = true)
    val dailyReminderEnabled by preferencesRepository.dailyReminderEnabled.collectAsState(initial = true)
    val tournamentAlertsEnabled by preferencesRepository.tournamentAlertsEnabled.collectAsState(initial = true)
    val streakAlertsEnabled by preferencesRepository.streakAlertsEnabled.collectAsState(initial = true)
    val darkModeEnabled by preferencesRepository.darkModeEnabled.collectAsState(initial = false)

    // Dialog states
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            // Section 1: Sound & Haptics
            item {
                SettingsSectionHeader(title = "SOUND & HAPTICS")
                QuizEarnCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsToggleRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Sound Effects",
                            subtitle = "Quiz countdown, correct/incorrect chimes",
                            checked = soundEffectsEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch { preferencesRepository.setSoundEffectsEnabled(checked) }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = spacing.md),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Vibration,
                            title = "Haptic Feedback",
                            subtitle = "Vibration on button presses and timer warning",
                            checked = hapticsEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch { preferencesRepository.setHapticsEnabled(checked) }
                            }
                        )
                    }
                }
            }

            // Section 2: Notifications
            item {
                SettingsSectionHeader(title = "NOTIFICATIONS")
                QuizEarnCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsToggleRow(
                            icon = Icons.Default.Notifications,
                            title = "Daily Quiz Reminder",
                            subtitle = "Keep your active win streak alive",
                            checked = dailyReminderEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch { preferencesRepository.setDailyReminderEnabled(checked) }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = spacing.md),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Notifications,
                            title = "Tournament Alerts",
                            subtitle = "Notifies when registered events start",
                            checked = tournamentAlertsEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch { preferencesRepository.setTournamentAlertsEnabled(checked) }
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = spacing.md),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        SettingsToggleRow(
                            icon = Icons.Default.Notifications,
                            title = "Reward & Claim Alerts",
                            subtitle = "Alerts when task rewards are ready to claim",
                            checked = streakAlertsEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch { preferencesRepository.setStreakAlertsEnabled(checked) }
                            }
                        )
                    }
                }
            }

            // Section 3: Appearance
            item {
                SettingsSectionHeader(title = "APPEARANCE")
                QuizEarnCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape
                ) {
                    SettingsToggleRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Enable darker palette for reduced eye strain",
                        checked = darkModeEnabled,
                        onCheckedChange = { checked ->
                            coroutineScope.launch { preferencesRepository.setDarkModeEnabled(checked) }
                        }
                    )
                }
            }

            // Section 4: Legal & Support
            item {
                SettingsSectionHeader(title = "ABOUT & LEGAL")
                QuizEarnCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SettingsNavigationRow(
                            icon = Icons.Default.HelpOutline,
                            title = "Help & Support",
                            subtitle = "Frequently asked questions and contact",
                            onClick = onNavigateToHelp
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = spacing.md),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy Policy",
                            subtitle = "Zero PII selling, local encrypted storage",
                            onClick = { showPrivacyDialog = true }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = spacing.md),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.Gavel,
                            title = "Terms of Service",
                            subtitle = "Fair Play rules, account integrity policies",
                            onClick = { showTermsDialog = true }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = spacing.md),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        SettingsNavigationRow(
                            icon = Icons.Default.Info,
                            title = "App Version",
                            subtitle = "QuizEarn v1.0.0 (Build 2026.10)",
                            onClick = {}
                        )
                    }
                }
            }

            // Section 5: Account Deletion (Danger Zone)
            item {
                SettingsSectionHeader(title = "DANGER ZONE", color = ErrorCrimson)
                QuizEarnCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    borderColor = ErrorCrimson.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.md)
                    ) {
                        Text(
                            text = "Account Deletion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ErrorCrimson
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Permanently delete your player profile, points balance, badges, and tournament entries. This action cannot be reversed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(spacing.md))
                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = true },
                            shape = ButtonShape,
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorCrimson)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = ErrorCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Delete Account & Data",
                                style = MaterialTheme.typography.labelLarge,
                                color = ErrorCrimson,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "QuizEarn Privacy Commitment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1. Zero PII on Leaderboards: Only chosen player usernames are displayed publicly. Email and personal identities are never exposed.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. Minimal Data Collection: We only store quiz gameplay timestamps and points ledger records necessary for fair game verification.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Encryption & Integrity: All points transactions and tokens are protected via server-authoritative ledger signing.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Fair Play & Contest Rules",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1. One Account Per Player: Creating multiple accounts to farm referral points or manipulate tournament leaderboards is strictly forbidden.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. No Automated Scripts: Use of OCR bots, automated solvers, or modified APKs will result in permanent account termination and forfeiture of points.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Reward Redemptions: All reward redemptions undergo anti-fraud verification before digital code issuance.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    text = "Permanently Delete Account?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ErrorCrimson
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This action is permanent and cannot be undone:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• All accumulated points balance will be permanently forfeited.\n• All tournament registrations will be canceled.\n• All achievement badges and quiz history will be erased.\n• Your username will be released.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Note: In this preview version, confirming will reset your local profile and sign you out.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                QuizEarnButton(
                    text = "Permanently Delete",
                    onClick = {
                        showDeleteAccountDialog = false
                        onAccountDeleted()
                    },
                    buttonType = QuizEarnButtonType.Primary
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val spacing = LocalSpacing.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = spacing.xs, vertical = spacing.xxs)
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricIndigo,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricIndigo
            )
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricIndigo,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
