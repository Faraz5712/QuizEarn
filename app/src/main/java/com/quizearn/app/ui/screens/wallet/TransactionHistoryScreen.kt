package com.quizearn.app.ui.screens.wallet

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.TransactionReason
import com.quizearn.app.domain.model.TransactionType as DomainTxType
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.EmptyStateView
import com.quizearn.app.ui.components.QuizEarnCard
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.ErrorCrimson
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    walletRepository: WalletRepository = WalletRepositoryImpl.getInstance(),
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All", "Earned (+)", "Redeemed (-)")

    val transactionsResult by walletRepository.getTransactions().collectAsState(initial = Result.Loading)

    val allTransactions = remember(transactionsResult) {
        when (val res = transactionsResult) {
            is Result.Success -> {
                res.data.map { tx ->
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
                        timestampFormatted = "Today, 10:30 AM",
                        pointsDelta = 4300,
                        isCredit = true,
                        status = TransactionStatus.COMPLETED
                    ),
                    TransactionItem(
                        id = "tx_02",
                        type = TransactionType.DAILY_CHECKIN,
                        title = "7-Day Streak Bonus",
                        timestampFormatted = "Today, 08:00 AM",
                        pointsDelta = 25,
                        isCredit = true,
                        status = TransactionStatus.COMPLETED
                    ),
                    TransactionItem(
                        id = "tx_03",
                        type = TransactionType.QUIZ_REWARD,
                        title = "Science & Nature Quiz Mastered (5/5)",
                        timestampFormatted = "Yesterday, 09:15 PM",
                        pointsDelta = 50,
                        isCredit = true,
                        status = TransactionStatus.COMPLETED
                    ),
                    TransactionItem(
                        id = "tx_04",
                        type = TransactionType.TASK_COMPLETED,
                        title = "Weekly Goal: Complete 5 Quizzes",
                        timestampFormatted = "Oct 27, 2026",
                        pointsDelta = 75,
                        isCredit = true,
                        status = TransactionStatus.COMPLETED
                    ),
                    TransactionItem(
                        id = "tx_05",
                        type = TransactionType.REDEMPTION,
                        title = "Amazon $5 Digital Gift Card",
                        timestampFormatted = "Oct 28, 2026",
                        pointsDelta = -5000,
                        isCredit = false,
                        status = TransactionStatus.COMPLETED
                    )
                )
            }
        }
    }

    val filteredTransactions = remember(selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> allTransactions.filter { it.isCredit }
            2 -> allTransactions.filter { !it.isCredit }
            else -> allTransactions
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transaction History",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ledger Summary Header Card
            PaddingBox(padding = spacing.md) {
                LedgerSummaryHeader(
                    totalEarned = allTransactions.filter { it.isCredit }.sumOf { it.pointsDelta },
                    totalRedeemed = allTransactions.filter { !it.isCredit }.sumOf { -it.pointsDelta }
                )
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterTabs.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            if (filteredTransactions.isEmpty()) {
                EmptyStateView(
                    title = "No Transactions Found",
                    description = "There are no transactions recorded under this category yet."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.md,
                        end = spacing.md,
                        top = spacing.xs,
                        bottom = spacing.md
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    items(filteredTransactions, key = { it.id }) { item ->
                        DetailedTransactionRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaddingBox(
    padding: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.padding(padding)) {
        content()
    }
}

/**
 * Summary card showing lifetime points earned vs redeemed.
 */
@Composable
private fun LedgerSummaryHeader(
    totalEarned: Int,
    totalRedeemed: Int,
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
                .padding(spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOTAL POINTS EARNED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "+%,d Pts".format(totalEarned),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MintTeal
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = spacing.md)
            ) {
                Text(
                    text = "TOTAL REDEEMED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "-%,d Pts".format(totalRedeemed),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ErrorCrimson
                )
            }
        }
    }
}

/**
 * Detailed Transaction Row for history list with category icons and status badges.
 */
@Composable
private fun DetailedTransactionRow(
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isCredit) MintTeal.copy(alpha = 0.12f)
                            else ErrorCrimson.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isCredit) Icons.Default.ArrowDownward else Icons.Default.CardGiftcard,
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

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (item.isCredit) "+%,d Pts".format(item.pointsDelta) else "%,d Pts".format(item.pointsDelta),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isCredit) MintTeal else ErrorCrimson
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (item.status) {
                        TransactionStatus.COMPLETED -> MintTeal.copy(alpha = 0.15f)
                        TransactionStatus.PENDING -> RewardGold.copy(alpha = 0.15f)
                        TransactionStatus.FAILED -> ErrorCrimson.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = item.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (item.status) {
                            TransactionStatus.COMPLETED -> MintTeal
                            TransactionStatus.PENDING -> RewardGold
                            TransactionStatus.FAILED -> ErrorCrimson
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
