package com.quizearn.app.ui.screens.leaderboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizearn.app.R
import com.quizearn.app.data.repository.LeaderboardRepository
import com.quizearn.app.data.repository.LeaderboardRepositoryImpl
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.LeaderboardEntry
import com.quizearn.app.domain.model.LeaderboardTimeframe
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.QuizEarnCard
import com.quizearn.app.ui.components.RewardBadge
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold
import com.quizearn.app.ui.theme.VibrantViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    leaderboardRepository: LeaderboardRepository = remember { LeaderboardRepositoryImpl.getInstance() },
    walletRepository: WalletRepository = remember { WalletRepositoryImpl.getInstance() },
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTimeframe by remember { mutableStateOf(LeaderboardTimeframe.WEEKLY) }

    // Reactive streams from Authoritative Backend Repositories
    val leaderboardResult by leaderboardRepository.getLeaderboard(selectedTimeframe, forceRefresh = false)
        .collectAsState(initial = null)
    val balance by walletRepository.getBalance().collectAsState(initial = 5450L)

    val uiState = leaderboardResult?.getOrNull()

    // Fallback podium & entries during initialization
    val currentPodium = uiState?.topThree ?: remember {
        listOf(
            LeaderboardEntry(1, "QuizChampion", 4250, "4,250 Pts", "QC", 0xFFF5B942, quizzesWon = 32, winStreak = 12),
            LeaderboardEntry(2, "TriviaKing", 3890, "3,890 Pts", "TK", 0xFF94A3B8, quizzesWon = 28, winStreak = 8),
            LeaderboardEntry(3, "BrainMaster", 3400, "3,400 Pts", "BM", 0xFFCD7F32, quizzesWon = 25, winStreak = 7)
        )
    }

    val currentOthers = uiState?.remainingEntries ?: remember {
        listOf(
            LeaderboardEntry(4, "StarStudent", 2950, "2,950 Pts", "SS", 0xFF5B5FEF, quizzesWon = 21, winStreak = 5),
            LeaderboardEntry(5, "QuizLover", 2600, "2,600 Pts", "QL", 0xFF8B5CF6, quizzesWon = 19, winStreak = 4),
            LeaderboardEntry(6, "HyperMind", 2320, "2,320 Pts", "HM", 0xFF2DD4BF, quizzesWon = 17, winStreak = 6),
            LeaderboardEntry(7, "AlphaSeeker", 2100, "2,100 Pts", "AS", 0xFFEC4899, quizzesWon = 15, winStreak = 3),
            LeaderboardEntry(8, "OmegaScholar", 1950, "1,950 Pts", "OS", 0xFF3B82F6, quizzesWon = 14, winStreak = 2),
            LeaderboardEntry(9, "PrismIntellect", 1820, "1,820 Pts", "PI", 0xFF10B981, quizzesWon = 13, winStreak = 4),
            LeaderboardEntry(10, "ApexThinker", 1690, "1,690 Pts", "AT", 0xFF6366F1, quizzesWon = 12, winStreak = 2)
        )
    }

    val currentUser = uiState?.currentUserEntry ?: remember {
        LeaderboardEntry(14, "QuizMaster (You)", 1450, "1,450 Pts", "QM", 0xFF5B5FEF, isCurrentUser = true, quizzesWon = 10, winStreak = 4)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Leaderboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fair Play Rankings • Zero PII",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val res = leaderboardRepository.refreshLeaderboard(selectedTimeframe)
                                if (res.getOrNull()?.isRateLimited == true) {
                                    snackbarHostState.showSnackbar("Leaderboard refreshed recently. Serving cached standings.")
                                } else {
                                    snackbarHostState.showSnackbar("Leaderboard updated.")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Standings"
                        )
                    }
                    RewardBadge(
                        points = balance,
                        modifier = Modifier.padding(end = spacing.sm)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Sticky Current User Ranking Bar
            CurrentUserRankBar(entry = currentUser)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Timeframe Tab Selector
            TabRow(
                selectedTabIndex = selectedTimeframe.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ElectricIndigo,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTimeframe.ordinal]),
                        color = ElectricIndigo,
                        height = 3.dp
                    )
                }
            ) {
                LeaderboardTimeframe.values().forEach { timeframe ->
                    Tab(
                        selected = selectedTimeframe == timeframe,
                        onClick = { selectedTimeframe = timeframe },
                        text = {
                            Text(
                                text = timeframe.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTimeframe == timeframe) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            // Rate-limiting or Cache Notification Indicator
            if (uiState?.isRateLimited == true) {
                Surface(
                    color = RewardGold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ Serving cached standings (60s TTL cache active)",
                        style = MaterialTheme.typography.labelSmall,
                        color = RewardGold,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = spacing.md, vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            AnimatedContent(
                targetState = Triple(currentPodium, currentOthers, currentUser),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "LeaderboardTransition"
            ) { (podium, others, _) ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.md,
                        end = spacing.md,
                        top = spacing.md,
                        bottom = 80.dp // padding for sticky bottom bar
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    // Top 3 Podium
                    item {
                        if (podium.size >= 3) {
                            PodiumSection(
                                first = podium[0],
                                second = podium[1],
                                third = podium[2]
                            )
                            Spacer(modifier = Modifier.height(spacing.md))
                        }
                    }

                    // Section header for ranks 4+
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.xs, vertical = spacing.xs),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RANK & PLAYER",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "POINTS EARNED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Ranks 4+ List Items
                    items(others, key = { it.rank }) { entry ->
                        LeaderboardRowItem(entry = entry)
                    }
                }
            }
        }
    }
}

/**
 * Top 3 Podium component featuring 1st place in center (tallest, gold crown),
 * 2nd place on left (silver), and 3rd place on right (bronze).
 */
@Composable
private fun PodiumSection(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    QuizEarnCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOP PERFORMERS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.xs),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                PodiumColumn(
                    entry = second,
                    crownColor = Color(0xFFC0C0C0), // Silver
                    pedestalHeight = 90.dp,
                    pedestalLabel = "2nd",
                    pedestalGradient = listOf(Color(0xFF94A3B8), Color(0xFF64748B)),
                    modifier = Modifier.weight(1f)
                )

                // 1st Place (Gold - Center, Tallest)
                PodiumColumn(
                    entry = first,
                    crownColor = RewardGold,
                    pedestalHeight = 125.dp,
                    pedestalLabel = "1st",
                    pedestalGradient = listOf(Color(0xFFF5B942), Color(0xFFD97706)),
                    isFirst = true,
                    modifier = Modifier.weight(1.15f)
                )

                // 3rd Place (Bronze)
                PodiumColumn(
                    entry = third,
                    crownColor = Color(0xFFCD7F32), // Bronze
                    pedestalHeight = 70.dp,
                    pedestalLabel = "3rd",
                    pedestalGradient = listOf(Color(0xFFD97706), Color(0xFF92400E)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardEntry,
    crownColor: Color,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    pedestalLabel: String,
    pedestalGradient: List<Color>,
    isFirst: Boolean = false,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crown Icon for 1st place
        if (isFirst) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Champion",
                tint = RewardGold,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Circular Avatar
        Box(
            modifier = Modifier
                .size(if (isFirst) 54.dp else 44.dp)
                .clip(CircleShape)
                .background(Color(entry.avatarColorHex))
                .border(2.dp, crownColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.avatarInitials,
                style = if (isFirst) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(spacing.xs))

        // Username
        Text(
            text = entry.username,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Points
        Text(
            text = entry.pointsFormatted,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = crownColor,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.xs))

        // Pedestal block
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(pedestalGradient)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pedestalLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Standard list item for players ranked 4th and lower.
 */
@Composable
private fun LeaderboardRowItem(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = if (entry.isCurrentUser) {
            ElectricIndigo.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (entry.isCurrentUser) {
            androidx.compose.foundation.BorderStroke(1.5.dp, ElectricIndigo)
        } else {
            androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        },
        tonalElevation = if (entry.isCurrentUser) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank, Avatar, Username
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier.weight(1f)
            ) {
                // Rank number
                Box(
                    modifier = Modifier.width(28.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "#${entry.rank}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isCurrentUser) ElectricIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(entry.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.avatarInitials,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Name & Stats
                Column {
                    Text(
                        text = entry.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (entry.isCurrentUser) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (entry.isCurrentUser) ElectricIndigo else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${entry.quizzesWon} wins",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.winStreak > 0) {
                            Text(
                                text = "• 🔥 ${entry.winStreak} streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = RewardGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Points Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = entry.pointsFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Sticky Bottom Bar showing the authenticated user's current standing.
 */
@Composable
private fun CurrentUserRankBar(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ElectricIndigo.copy(alpha = 0.4f)
        )
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
                // Rank Chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricIndigo
                ) {
                    Text(
                        text = "#${entry.rank}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(entry.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.avatarInitials,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        text = entry.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your Current Standing",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // User Points
            RewardBadge(points = entry.points.toLong())
        }
    }
}
