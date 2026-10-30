package com.quizearn.app.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.EmptyStateView
import com.quizearn.app.ui.components.RewardBadge
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    // Sample historical sessions data
    val historyItems = remember {
        listOf(
            QuizHistoryItem(
                sessionId = "sess_hist_01",
                categoryTitle = "Science & Nature",
                timestampFormatted = "Today, 11:20 AM",
                score = 40,
                totalQuestions = 5,
                pointsEarned = 50L,
                accuracyRate = 80.0
            ),
            QuizHistoryItem(
                sessionId = "sess_hist_02",
                categoryTitle = "Technology & Code",
                timestampFormatted = "Yesterday, 4:15 PM",
                score = 50,
                totalQuestions = 5,
                pointsEarned = 60L,
                accuracyRate = 100.0
            ),
            QuizHistoryItem(
                sessionId = "sess_hist_03",
                categoryTitle = "History & Geo",
                timestampFormatted = "Oct 27, 2:30 PM",
                score = 30,
                totalQuestions = 5,
                pointsEarned = 35L,
                accuracyRate = 60.0
            ),
            QuizHistoryItem(
                sessionId = "sess_hist_04",
                categoryTitle = "General Knowledge",
                timestampFormatted = "Oct 26, 7:45 PM",
                score = 50,
                totalQuestions = 5,
                pointsEarned = 75L,
                accuracyRate = 100.0
            )
        )
    }

    val totalPlayed = historyItems.size
    val totalPointsWon = historyItems.sumOf { it.pointsEarned }
    val avgAccuracy = if (historyItems.isNotEmpty()) {
        historyItems.map { it.accuracyRate }.average().toInt()
    } else 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quiz History",
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.md)
        ) {
            if (historyItems.isEmpty()) {
                EmptyStateView(
                    title = "No Quiz History Yet",
                    message = "Complete your first quiz to view your historical scores, accuracy, and points rewards."
                )
            } else {
                // Summary Metrics Banner
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.sm),
                    shape = CardShape,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.md),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Quizzes Played",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalPlayed",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ElectricIndigo
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Avg Accuracy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$avgAccuracy%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MintTeal
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Points Won",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+$totalPointsWon",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = RewardGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.sm))

                // Sessions List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(historyItems, key = { it.sessionId }) { item ->
                        HistorySessionCard(item = item)
                    }
                    item {
                        Spacer(modifier = Modifier.height(spacing.lg))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    item: QuizHistoryItem
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElectricIndigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = item.categoryTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.timestampFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "Score: ${item.score} (${item.accuracyRate.toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Points Badge
            RewardBadge(points = item.pointsEarned)
        }
    }
}
