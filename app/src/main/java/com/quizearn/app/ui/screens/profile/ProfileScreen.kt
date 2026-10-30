package com.quizearn.app.ui.screens.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.QuizEarnButton
import com.quizearn.app.ui.components.QuizEarnButtonType
import com.quizearn.app.ui.components.QuizEarnCard
import com.quizearn.app.ui.components.QuizEarnTextField
import com.quizearn.app.ui.components.RewardBadge
import com.quizearn.app.ui.theme.ButtonShape
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.ErrorCrimson
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold
import com.quizearn.app.ui.theme.VibrantViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onNavigateToQuizHistory: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    // Profile State (mutable in-memory for interactive edits)
    var profile by remember { mutableStateOf(UserProfileSummary()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Player Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
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
            // Profile Header Card
            item {
                ProfileHeaderCard(
                    profile = profile,
                    onEditClick = { showEditDialog = true }
                )
            }

            // 4-Grid Lifetime Statistics
            item {
                Text(
                    text = "LIFETIME PERFORMANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = spacing.xs)
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                LifetimeStatsGrid(profile = profile)
            }

            // Achievements Showcase Preview
            item {
                AchievementsShowcaseSection(
                    onViewAllClick = onNavigateToAchievements
                )
            }

            // Action & Navigation Menu
            item {
                Text(
                    text = "ACCOUNT & SUPPORT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = spacing.xs)
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                QuizEarnCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ProfileMenuItem(
                            icon = Icons.Default.History,
                            title = "Quiz History & Review",
                            subtitle = "View past results and answers",
                            onClick = onNavigateToQuizHistory
                        )
                        ProfileMenuItem(
                            icon = Icons.Default.EmojiEvents,
                            title = "Tasks & Badges",
                            subtitle = "Daily goals and achievement progression",
                            onClick = onNavigateToAchievements
                        )
                        ProfileMenuItem(
                            icon = Icons.Default.Settings,
                            title = "Settings & Preferences",
                            subtitle = "Sound, notifications, and theme",
                            onClick = onNavigateToSettings
                        )
                        ProfileMenuItem(
                            icon = Icons.Default.HelpOutline,
                            title = "Help & Support",
                            subtitle = "FAQs and contact assistance",
                            onClick = onNavigateToHelp,
                            showDivider = false
                        )
                    }
                }
            }

            // Sign Out Button
            item {
                Spacer(modifier = Modifier.height(spacing.xs))
                OutlinedButton(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ButtonShape
                ) {
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.labelLarge,
                        color = ErrorCrimson,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            currentUsername = profile.username,
            currentColorHex = profile.avatarColorHex,
            onDismiss = { showEditDialog = false },
            onSave = { newUsername, newColorHex ->
                val initials = newUsername.trim().take(2).uppercase()
                profile = profile.copy(
                    username = newUsername,
                    avatarColorHex = newColorHex,
                    avatarInitials = if (initials.isNotEmpty()) initials else "QP"
                )
                showEditDialog = false
            }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of QuizEarn? You will need to sign in again to access your balance and ranking.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                QuizEarnButton(
                    text = "Sign Out",
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                    buttonType = QuizEarnButtonType.Primary
                )
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Top profile header card with avatar, level badge, and points pill.
 */
@Composable
private fun ProfileHeaderCard(
    profile: UserProfileSummary,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    QuizEarnCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // Avatar with Edit overlay badge
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(profile.avatarColorHex))
                            .clickable(onClick = onEditClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.avatarInitials,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        // Edit icon badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(ElectricIndigo)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit avatar",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = profile.username,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Username",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Level badge pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VibrantViolet.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "Level ${profile.level} • ${profile.levelTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = VibrantViolet,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Joined ${profile.memberSinceFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Balance Badge
                RewardBadge(points = profile.totalPoints)
            }
        }
    }
}

/**
 * 4-Grid Lifetime performance metrics card.
 */
@Composable
private fun LifetimeStatsGrid(
    profile: UserProfileSummary,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    QuizEarnCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatGridItem(
                    label = "Quizzes Played",
                    value = "${profile.quizzesPlayed}",
                    icon = Icons.Default.History,
                    color = ElectricIndigo,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(spacing.md))
                StatGridItem(
                    label = "Win / Accuracy",
                    value = "${profile.accuracyRate}%",
                    icon = Icons.Default.MilitaryTech,
                    color = MintTeal,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatGridItem(
                    label = "Win Streak",
                    value = "${profile.winStreak} Days",
                    icon = Icons.Default.Whatshot,
                    color = RewardGold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(spacing.md))
                StatGridItem(
                    label = "Best Topic",
                    value = profile.bestCategory,
                    icon = Icons.Default.EmojiEvents,
                    color = VibrantViolet,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatGridItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Preview showcase of unlocked achievement badges.
 */
@Composable
private fun AchievementsShowcaseSection(
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT ACHIEVEMENTS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = spacing.xs)
            )
            Text(
                text = "View All →",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricIndigo,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onViewAllClick)
                    .padding(horizontal = spacing.xs, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(spacing.xs))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            BadgePreviewPill(
                emoji = "⚡",
                name = "Speed Demon",
                color = RewardGold,
                modifier = Modifier.weight(1f)
            )
            BadgePreviewPill(
                emoji = "🎯",
                name = "Sharpshooter",
                color = MintTeal,
                modifier = Modifier.weight(1f)
            )
            BadgePreviewPill(
                emoji = "🔥",
                name = "Streak Master",
                color = VibrantViolet,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BadgePreviewPill(
    emoji: String,
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/**
 * Standard menu item in Profile actions card.
 */
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }

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

        if (showDivider) {
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(horizontal = spacing.md),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * Interactive Edit Profile Dialog.
 */
@Composable
private fun EditProfileDialog(
    currentUsername: String,
    currentColorHex: Long,
    onDismiss: () -> Unit,
    onSave: (username: String, colorHex: Long) -> Unit
) {
    val spacing = LocalSpacing.current
    var username by remember { mutableStateOf(currentUsername) }
    var selectedColor by remember { mutableStateOf(currentColorHex) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                // Avatar preview & color selection
                Text(
                    text = "Select Avatar Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AVATAR_COLOR_PRESETS) { colorHex ->
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Username input field
                QuizEarnTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        if (it.trim().length >= 3) errorMessage = null
                    },
                    label = "Username",
                    leadingIcon = Icons.Default.Person,
                    isError = errorMessage != null,
                    errorMessage = errorMessage
                )
            }
        },
        confirmButton = {
            QuizEarnButton(
                text = "Save Changes",
                onClick = {
                    if (username.trim().length < 3) {
                        errorMessage = "Username must be at least 3 characters"
                    } else {
                        onSave(username.trim(), selectedColor)
                    }
                },
                buttonType = QuizEarnButtonType.Primary
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
