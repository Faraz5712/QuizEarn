package com.quizearn.app.ui.screens.notifications

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.data.repository.NotificationRepository
import com.quizearn.app.data.repository.NotificationRepositoryImpl
import com.quizearn.app.domain.model.NotificationItem
import com.quizearn.app.domain.model.NotificationType
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.EmptyStateView
import com.quizearn.app.ui.components.QuizEarnCard
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold
import com.quizearn.app.ui.theme.VibrantViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationInboxScreen(
    onBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {},
    notificationRepository: NotificationRepository = remember { NotificationRepositoryImpl.getInstance() },
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()

    val notifications by notificationRepository.notifications.collectAsState(initial = emptyList())
    val unreadCount by notificationRepository.unreadCount.collectAsState(initial = 0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    notificationRepository.markAllAsRead()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark All as Read",
                                tint = ElectricIndigo
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(spacing.md),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = "No Notifications",
                    message = "You're all caught up! Game alerts, tournament reminders, and reward updates will appear here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationCard(
                        item = item,
                        onClick = {
                            coroutineScope.launch {
                                notificationRepository.markAsRead(item.id)
                            }
                            item.actionRoute?.let { onNavigateToRoute(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    val (icon: ImageVector, iconBg: Color, iconTint: Color) = when (item.type) {
        NotificationType.WALLET_CREDIT -> Triple(Icons.Default.CardGiftcard, RewardGold.copy(alpha = 0.15f), RewardGold)
        NotificationType.TOURNAMENT_ALERT -> Triple(Icons.Default.EmojiEvents, VibrantViolet.copy(alpha = 0.15f), VibrantViolet)
        NotificationType.STREAK_REMINDER -> Triple(Icons.Default.Whatshot, Color(0xFFF97316).copy(alpha = 0.15f), Color(0xFFF97316))
        NotificationType.SUPPORT_UPDATE -> Triple(Icons.Default.SupportAgent, MintTeal.copy(alpha = 0.15f), MintTeal)
        NotificationType.SYSTEM_ANNOUNCEMENT -> Triple(Icons.Default.Info, ElectricIndigo.copy(alpha = 0.15f), ElectricIndigo)
    }

    QuizEarnCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardShape,
        borderColor = if (!item.isRead) ElectricIndigo.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        elevation = if (!item.isRead) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            // Category Icon with Circle background
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Notification Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!item.isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricIndigo)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.timestampIso,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
