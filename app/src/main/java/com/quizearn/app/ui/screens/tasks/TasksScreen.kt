package com.quizearn.app.ui.screens.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizearn.app.data.repository.TaskRepository
import com.quizearn.app.data.repository.TaskRepositoryImpl
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.AchievementBadge
import com.quizearn.app.domain.model.TaskItem
import com.quizearn.app.domain.model.TaskType
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.RewardBadge
import com.quizearn.app.ui.theme.ButtonShape
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    onNavigateToTournaments: () -> Unit = {},
    taskRepository: TaskRepository = remember { TaskRepositoryImpl.getInstance() },
    walletRepository: WalletRepository = remember { WalletRepositoryImpl.getInstance() },
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Daily", "Weekly", "Badges")

    // Reactive streams from Authoritative Backend Repositories
    val dailyTasks by taskRepository.getDailyTasks().collectAsState(initial = emptyList())
    val weeklyTasks by taskRepository.getWeeklyTasks().collectAsState(initial = emptyList())
    val achievements by taskRepository.getAchievements().collectAsState(initial = emptyList())
    val balance by walletRepository.getBalance().collectAsState(initial = 5450L)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with balance and tournament entry point
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Challenges & Badges",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Complete objectives for instant wallet rewards",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                RewardBadge(points = balance)
            }

            // Quick Tournaments Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.xs),
                shape = CardShape,
                color = ElectricIndigo.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = RewardGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Live Tournaments",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Compete for big coin prize pools",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToTournaments,
                        shape = ButtonShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Lobby",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = ElectricIndigo,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = ElectricIndigo
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    val badgeCount = when (index) {
                        0 -> dailyTasks.count { it.isCompleted && !it.isClaimed }
                        1 -> weeklyTasks.count { it.isCompleted && !it.isClaimed }
                        else -> 0
                    }

                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                                if (badgeCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = RewardGold,
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = badgeCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Daily Challenges Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        items(dailyTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onClaim = {
                                    coroutineScope.launch {
                                        val result = taskRepository.claimTaskReward(task.id)
                                        if (result.isSuccess) {
                                            val reward = result.getOrThrow()
                                            snackbarHostState.showSnackbar("Claimed +$reward Pts to your Wallet!")
                                        } else {
                                            val err = result.exceptionOrNull()?.message ?: "Failed to claim task reward"
                                            snackbarHostState.showSnackbar(err)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                1 -> {
                    // Weekly Goals Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        items(weeklyTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onClaim = {
                                    coroutineScope.launch {
                                        val result = taskRepository.claimTaskReward(task.id)
                                        if (result.isSuccess) {
                                            val reward = result.getOrThrow()
                                            snackbarHostState.showSnackbar("Claimed +$reward Pts to your Wallet!")
                                        } else {
                                            val err = result.exceptionOrNull()?.message ?: "Failed to claim task reward"
                                            snackbarHostState.showSnackbar(err)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                2 -> {
                    // Achievements Grid Tab
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        val chunked = achievements.chunked(2)
                        items(chunked) { rowBadges ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                            ) {
                                rowBadges.forEach { badge ->
                                    AchievementBadgeCard(
                                        badge = badge,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowBadges.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    onClaim: () -> Unit
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                RewardBadge(points = task.rewardPoints)
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            // Progress bar and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = spacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${task.currentProgress}/${task.targetGoal}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) MintTeal else ElectricIndigo
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { task.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (task.isCompleted) MintTeal else ElectricIndigo,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Action / Claim Button
                when {
                    task.isClaimed -> {
                        Surface(
                            shape = CircleShape,
                            color = MintTeal.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MintTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Claimed",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MintTeal
                                )
                            }
                        }
                    }
                    task.isCompleted -> {
                        Button(
                            onClick = onClaim,
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RewardGold,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 6.dp
                            )
                        ) {
                            Text(
                                text = "Claim",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "In Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementBadgeCard(
    badge: AchievementBadge,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    val icon: ImageVector = when (badge.categoryKey) {
        "SCIENCE" -> Icons.Default.Psychology
        "STREAK" -> Icons.Default.Whatshot
        "SPEED" -> Icons.Default.Speed
        "TOURNAMENT" -> Icons.Default.EmojiEvents
        "KNOWLEDGE" -> Icons.Default.MilitaryTech
        else -> Icons.Default.Star
    }

    Surface(
        modifier = modifier
            .height(180.dp)
            .alpha(if (badge.isUnlocked) 1f else 0.7f),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (badge.isUnlocked) RewardGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        tonalElevation = if (badge.isUnlocked) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Emblem Circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) RewardGold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        1.5.dp,
                        if (badge.isUnlocked) RewardGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (badge.isUnlocked) icon else Icons.Default.Lock,
                    contentDescription = badge.name,
                    tint = if (badge.isUnlocked) RewardGold else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            if (badge.isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MintTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge.unlockedDateFormatted ?: "Unlocked",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MintTeal,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${badge.progress}/${badge.targetGoal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { badge.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = ElectricIndigo,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
