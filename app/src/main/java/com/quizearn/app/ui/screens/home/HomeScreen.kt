package com.quizearn.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quizearn.app.R
import com.quizearn.app.data.repository.AuthRepository
import com.quizearn.app.data.repository.AuthRepositoryImpl
import com.quizearn.app.data.repository.QuizRepositoryImpl
import com.quizearn.app.domain.model.User
import com.quizearn.app.ui.components.BackendModeBanner
import com.quizearn.app.ui.components.RewardBadge
import com.quizearn.app.ui.theme.ButtonShape
import com.quizearn.app.ui.theme.CardShape
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.RewardGold
import com.quizearn.app.ui.theme.VibrantViolet
import com.quizearn.app.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    onStartQuiz: (categoryId: String) -> Unit,
    onViewWallet: () -> Unit,
    onNavigateToTasks: () -> Unit = {},
    onViewNotifications: () -> Unit = {},
    authRepository: AuthRepository = AuthRepositoryImpl.getInstance(),
    quizRepository: QuizRepositoryImpl = QuizRepositoryImpl.getInstance(),
    notificationRepository: com.quizearn.app.data.repository.NotificationRepository = remember { com.quizearn.app.data.repository.NotificationRepositoryImpl.getInstance() },
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val currentUser by authRepository.currentUser.collectAsState(initial = null)
    val unreadNotificationsCount by notificationRepository.unreadCount.collectAsState(initial = 0)
    val featuredQuizzes = remember { quizRepository.getFeaturedQuizzes() }
    val categories = remember { quizRepository.getCategoryDisplays() }
    val tournament = remember { quizRepository.getActiveTournament() }
    val dailyChallenge = remember { quizRepository.getDailyChallenge() }

    val currentStreak = 3
    val totalPoints = currentUser?.pointsBalance ?: 250L

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = spacing.xxl)
        ) {
            BackendModeBanner(
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs)
            )

            // Top App Bar / Header
            HomeTopHeader(
                user = currentUser,
                pointsBalance = totalPoints,
                currentStreak = currentStreak,
                unreadNotifications = unreadNotificationsCount,
                onWalletClick = onViewWallet,
                onNotificationsClick = onViewNotifications,
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm)
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            // Link to the authoritative daily and weekly task flows
            DailyTasksCard(
                onViewTasks = onNavigateToTasks,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md)
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            // Featured Quizzes Carousel Section
            SectionHeader(
                title = "Featured Quizzes",
                badgeText = "SPECIAL EVENTS",
                modifier = Modifier.padding(horizontal = spacing.md)
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                items(featuredQuizzes, key = { it.id }) { featured ->
                    FeaturedQuizCard(
                        quiz = featured,
                        onPlayClick = { onStartQuiz(featured.categoryId) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.lg))

            // Categories Section
            SectionHeader(
                title = "Explore Categories",
                badgeText = "${categories.size} TOPICS",
                modifier = Modifier.padding(horizontal = spacing.md)
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            CategoryGrid(
                categories = categories,
                onCategoryClick = onStartQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md)
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            // Live Tournament & Quick Tasks Cards
            LiveTournamentCard(
                tournament = tournament,
                onJoinClick = { onStartQuiz("cat_general") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md)
            )

            Spacer(modifier = Modifier.height(spacing.md))

            DailyChallengeCard(
                challenge = dailyChallenge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md)
            )
        }
    }
}

@Composable
private fun HomeTopHeader(
    user: User?,
    pointsBalance: Long,
    currentStreak: Int,
    unreadNotifications: Int = 0,
    onWalletClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val displayName = user?.username?.ifBlank { "Quiz Player" } ?: "Quiz Player"

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Profile Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ElectricIndigo.copy(alpha = 0.15f))
                    .border(1.5.dp, ElectricIndigo.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = ElectricIndigo,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column {
                Text(
                    text = "Hi, $displayName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ElectricIndigo.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Lv. ${user?.level ?: 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "• Fair Play Verified",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Streak, Points & Notifications Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            // Notification Bell with Badge (Accessible touch target >= 48dp)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNotificationsClick),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = if (unreadNotifications > 0) {
                            "View Notifications Inbox, $unreadNotifications unread"
                        } else {
                            "View Notifications Inbox"
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadNotifications > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(ElectricIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadNotifications > 9) "9+" else unreadNotifications.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Streak Pill
            Surface(
                shape = CircleShape,
                color = WarningAmber.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Current streak: $currentStreak days",
                        tint = WarningAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }
            }

            // Clickable Reward Points Badge
            RewardBadge(
                points = pointsBalance,
                modifier = Modifier.clickable(onClick = onWalletClick)
            )
        }
    }
}

@Composable
private fun DailyTasksCard(
    onViewTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier,
        shape = CardShape,
        color = RewardGold.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            RewardGold.copy(alpha = 0.5f)
        )
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
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(RewardGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = "Daily tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Complete goals and claim rewards in the Tasks screen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onViewTasks,
                shape = ButtonShape,
                modifier = Modifier.defaultMinSize(minWidth = 72.dp, minHeight = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RewardGold,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "View",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = ElectricIndigo.copy(alpha = 0.1f)
        ) {
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ElectricIndigo,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun FeaturedQuizCard(
    quiz: FeaturedQuiz,
    onPlayClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val accentColor = remember(quiz.accentColorHex) {
        Color(android.graphics.Color.parseColor(quiz.accentColorHex))
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onPlayClick),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
                .padding(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = quiz.tag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_quizearn_spark),
                        contentDescription = null,
                        tint = RewardGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "+${quiz.rewardPoints} Pts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = RewardGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            Text(
                text = quiz.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(spacing.xxs))

            Text(
                text = quiz.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${quiz.questionCount} Questions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${quiz.durationMinutes} Mins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ElectricIndigo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<QuizCategoryDisplay>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        val chunked = categories.chunked(2)
        chunked.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                rowCategories.forEach { category ->
                    CategoryItemCard(
                        category = category,
                        onClick = { onCategoryClick(category.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCategories.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryItemCard(
    category: QuizCategoryDisplay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val accentColor = remember(category.accentColorHex) {
        Color(android.graphics.Color.parseColor(category.accentColorHex))
    }

    val icon: ImageVector = when (category.iconKey) {
        CategoryIconKey.SCIENCE -> Icons.Default.Science
        CategoryIconKey.TECH -> Icons.Default.Code
        CategoryIconKey.HISTORY -> Icons.Default.Public
        CategoryIconKey.SPORTS -> Icons.Default.SportsSoccer
        CategoryIconKey.ARTS -> Icons.Default.Palette
        CategoryIconKey.GENERAL -> Icons.Default.HelpOutline
    }

    Surface(
        modifier = modifier
            .height(115.dp)
            .clickable(onClick = onClick),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.sm),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = category.difficulty,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${category.totalQuestions} Questions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LiveTournamentCard(
    tournament: LiveTournamentPreview,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantViolet.copy(alpha = 0.4f)),
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(VibrantViolet.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
                .padding(spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Text(
                        text = "LIVE TOURNAMENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = tournament.remainingTimeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = tournament.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Prize Pool: ${tournament.prizePool} Points",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = RewardGold
                    )
                    Text(
                        text = "${tournament.activeContenders} Players Competing",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onJoinClick,
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantViolet,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Join Now",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyChallengeCard(
    challenge: DailyChallengeProgress,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val progressFraction = (challenge.currentProgress.toFloat() / challenge.targetGoal.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = modifier,
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        tonalElevation = 1.dp
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElectricIndigo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = challenge.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "${challenge.currentProgress}/${challenge.targetGoal}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricIndigo
                )
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ElectricIndigo,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
