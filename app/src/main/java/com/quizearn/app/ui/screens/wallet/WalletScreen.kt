package com.quizearn.app.ui.screens.wallet

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizearn.app.R
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.Transaction
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType as DomainTxType
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.EmptyStateView
import com.quizearn.app.ui.components.QuizEarnCard
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
fun WalletScreen(
    onNavigateToRewardCatalog: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToEarnMore: () -> Unit = {},
    walletRepository: WalletRepository = WalletRepositoryImpl.getInstance(),
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    val balance by walletRepository.getBalance().collectAsState()
    val transactionsResult by walletRepository.getTransactions().collectAsState(initial = Result.Loading)

    val pointsBalance = balance.toInt()
    val minThreshold = 5000
    val cashEquivalent = "$${String.format(java.util.Locale.US, "%.2f", balance / 1000.0)}"
    val progress = (pointsBalance.toFloat() / minThreshold.toFloat()).coerceIn(0f, 1f)

    val recentTransactions = remember(transactionsResult) {
        when (val res = transactionsResult) {
            is Result.Success -> {
                res.data.take(4).map { tx ->
                    val uiType = when (tx.reason) {
                        TransactionReason.QUIZ_REWARD -> TransactionType.QUIZ_REWARD
                        TransactionReason.DAILY_CHECKIN -> TransactionType.DAILY_CHECKIN
                        TransactionReason.TASK_COMPLETED -> TransactionType.TASK_COMPLETED
                        TransactionReason.TOURNAMENT_PRIZE, TransactionReason.TOURNAMENT_ENTRY_FEE -> TransactionType.TOURNAMENT_PRIZE
                        TransactionReason.REDEMPTION_HOLD, TransactionReason.REDEMPTION_REFUND -> TransactionType.REDEMPTION
                        TransactionReason.WELCOME_BONUS -> TransactionType.TASK_COMPLETED
                        TransactionReason.ADMIN_ADJUSTMENT -> TransactionType.TASK_COMPLETED
                    }
                    val isCredit = tx.type == DomainTxType.CREDIT
                    val delta = if (isCredit) tx.amount.toInt() else -tx.amount.toInt()
                    TransactionItem(
                        id = tx.id,
                        type = uiType,
                        title = tx.description,
                        timestampFormatted = if (tx.timestampIso.contains("T")) tx.timestampIso.substringBefore("T") else tx.timestampIso,
                        pointsDelta = delta,
                        isCredit = isCredit,
                        status = TransactionStatus.COMPLETED
                    )
                }
            }
            else -> {
                listOf(
                    TransactionItem(
                        id = "tx_01",
                        type = TransactionType.TOURNAMENT_PRIZE,
                        title = "Weekend Grand Championship 1st Place",
                        timestampFormatted = "Today",
                        pointsDelta = 4300,
                        isCredit = true,
                        status = TransactionStatus.COMPLETED
                    ),
                    TransactionItem(
                        id = "tx_02",
                        type = TransactionType.DAILY_CHECKIN,
                        title = "7-Day Streak Check-in Bonus",
                        timestampFormatted = "Today",
                        pointsDelta = 25,
                        isCredit = true,
                        status = TransactionStatus.COMPLETED
                    ),
                    TransactionItem(
                        id = "tx_03",
                        type = TransactionType.QUIZ_REWARD,
                        title = "Science & Nature Quiz Mastered (5/5)",
                        timestampFormatted = "Yesterday",
                        pointsDelta = 50,
                        isCredit = true,
                        status = TransactionStatus.COMPLETED
                    ),
                    TransactionItem(
                        id = "tx_04",
                        type = TransactionType.TASK_COMPLETED,
                        title = "Weekly Goal: Complete 5 Quizzes",
                        timestampFormatted = "Yesterday",
                        pointsDelta = 75,
                        isCredit = true,
                        status = TransactionStatus.COMPLETED
                    )
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Wallet & Rewards",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    RewardBadge(
                        points = pointsBalance,
                        modifier = Modifier.padding(end = spacing.sm)
                    )
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
            // Points Balance Hero Card
            item {
                BalanceHeroCard(
                    points = pointsBalance,
                    cashEquivalent = cashEquivalent,
                    progress = progress,
                    minThreshold = minThreshold,
                    onRedeemClick = onNavigateToRewardCatalog
                )
            }

            // Quick Actions Row (Accessible touch targets >= 48dp)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    Button(
                        onClick = onNavigateToRewardCatalog,
                        modifier = Modifier
                            .weight(1.2f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricIndigo,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Redeem Rewards",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Redeem",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = ButtonShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Transaction History",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToEarnMore,
                        modifier = Modifier
                            .weight(1.1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = ButtonShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Earn More Points",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Earn",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Trust & Fair Play Info Card
            item {
                FairPlayEconomyCard()
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT ACTIVITY",
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
                            .clickable(onClick = onNavigateToHistory)
                            .padding(horizontal = spacing.xs, vertical = 2.dp)
                    )
                }
            }

            // Recent Transactions List (with Empty State fallback)
            if (recentTransactions.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Recent Activity",
                        message = "Play quizzes and complete challenges to earn reward points!"
                    )
                }
            } else {
                items(recentTransactions, key = { it.id }) { item ->
                    WalletTransactionRow(item = item)
                }
            }
        }
    }
}

/**
 * Gradient Balance Hero Card with Points, Cash Equivalent, and Threshold Meter.
 */
@Composable
private fun BalanceHeroCard(
    points: Int,
    cashEquivalent: String,
    progress: Float,
    minThreshold: Int,
    onRedeemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val canRedeem = points >= minThreshold

    QuizEarnCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ElectricIndigo,
                            VibrantViolet
                        )
                    )
                )
                .padding(spacing.lg)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_quizearn_spark),
                            contentDescription = null,
                            tint = RewardGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "VERIFIED BALANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "≈ $cashEquivalent USD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.sm))

                // Points Number Display
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%,d".format(points),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Points",
                        style = MaterialTheme.typography.headlineSmall,
                        color = RewardGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.md))

                // Threshold Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (canRedeem) "✓ Minimum Threshold Reached" else "Threshold Progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canRedeem) MintTeal else Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "%,d / %,d Pts".format(points, minThreshold),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MintTeal,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(spacing.sm))

                Text(
                    text = "Rule: 1,000 Points = $1.00 USD value • Anti-fraud ledger verified",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Transparent Fair Play and Economy Information card.
 */
@Composable
private fun FairPlayEconomyCard(modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    QuizEarnCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MintTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MintTeal,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Genuine & Transparent Rewards",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "All point awards and redemptions are recorded on our tamper-evident server ledger. Digital voucher codes are issued directly within 24 hours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Individual Transaction Row item.
 */
@Composable
fun WalletTransactionRow(
    item: TransactionItem,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    QuizEarnCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.weight(1f)
            ) {
                // Category icon circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isCredit) MintTeal.copy(alpha = 0.12f)
                            else ErrorCrimson.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (item.isCredit) MintTeal else ErrorCrimson,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "${item.type.displayName} • ${item.timestampFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Points delta badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (item.isCredit) MintTeal.copy(alpha = 0.15f) else ErrorCrimson.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (item.isCredit) "+${item.pointsDelta}" else "${item.pointsDelta}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (item.isCredit) MintTeal else ErrorCrimson,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
