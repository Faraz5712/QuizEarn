package com.quizearn.app.ui.screens.wallet

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.Redemption
import com.quizearn.app.domain.model.RedemptionStatus as DomainRedemptionStatus
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.EmptyStateView
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
import com.quizearn.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardCatalogScreen(
    onBack: () -> Unit,
    walletRepository: WalletRepository = WalletRepositoryImpl.getInstance(),
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()

    // Live reactive balance from double-entry ledger
    val liveBalance by walletRepository.getBalance().collectAsState()
    val currentBalance = liveBalance.toInt()

    // Live reactive redemptions
    val redemptionsResult by walletRepository.getRedemptions().collectAsState(initial = Result.Loading)
    val myRedemptions = remember(redemptionsResult) {
        when (val res = redemptionsResult) {
            is Result.Success -> res.data
            else -> emptyList()
        }
    }

    var selectedMainTab by remember { mutableIntStateOf(0) }
    val mainTabs = listOf("Voucher Catalog", "My Redemptions (${myRedemptions.size})")

    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = listOf("All", "Gift Cards", "Mobile Top-Up")

    // Active dialog target & submission feedback
    var selectedRewardForRedeem by remember { mutableStateOf<RewardItem?>(null) }
    var confirmedRedemptionCode by remember { mutableStateOf<String?>(null) }
    var redemptionErrorMessage by remember { mutableStateOf<String?>(null) }

    val rewards = remember {
        listOf(
            RewardItem(
                id = "rew_amz_05",
                name = "Amazon $5 Gift Card",
                brand = RewardBrand.AMAZON,
                description = "Digital code redeemable on amazon.com",
                pointsRequired = 5000,
                monetaryValueUsd = 5.0
            ),
            RewardItem(
                id = "rew_gplay_05",
                name = "Google Play $5 Card",
                brand = RewardBrand.GOOGLE_PLAY,
                description = "Apps, games, movies on Google Play",
                pointsRequired = 5000,
                monetaryValueUsd = 5.0
            ),
            RewardItem(
                id = "rew_topup_05",
                name = "Mobile Airtime $5",
                brand = RewardBrand.MOBILE_RECHARGE,
                description = "Instant prepaid airtime top-up",
                pointsRequired = 5000,
                monetaryValueUsd = 5.0
            ),
            RewardItem(
                id = "rew_amz_10",
                name = "Amazon $10 Gift Card",
                brand = RewardBrand.AMAZON,
                description = "Digital code redeemable on amazon.com",
                pointsRequired = 10000,
                monetaryValueUsd = 10.0
            ),
            RewardItem(
                id = "rew_gplay_10",
                name = "Google Play $10 Card",
                brand = RewardBrand.GOOGLE_PLAY,
                description = "Apps, games, movies on Google Play",
                pointsRequired = 10000,
                monetaryValueUsd = 10.0
            ),
            RewardItem(
                id = "rew_apple_10",
                name = "Apple $10 Gift Card",
                brand = RewardBrand.APPLE,
                description = "App Store, iTunes, Apple services",
                pointsRequired = 10000,
                monetaryValueUsd = 10.0
            )
        )
    }

    val filteredRewards = remember(selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> rewards.filter { it.brand != RewardBrand.MOBILE_RECHARGE }
            2 -> rewards.filter { it.brand == RewardBrand.MOBILE_RECHARGE }
            else -> rewards
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rewards & Redemptions",
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
                actions = {
                    RewardBadge(
                        points = currentBalance,
                        modifier = Modifier.padding(end = spacing.sm)
                    )
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
            // Balance & Exchange Rate Bar
            CatalogBalanceHeader(balance = currentBalance)

            // Main View Switcher TabRow
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ElectricIndigo,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                        color = ElectricIndigo
                    )
                }
            ) {
                mainTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedMainTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            if (selectedMainTab == 0) {
                // Tab 0: Rewards Catalog Grid
                Column(modifier = Modifier.fillMaxSize()) {
                    // Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.md, vertical = spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEachIndexed { index, label ->
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

                    // 2-Column Rewards Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredRewards, key = { it.id }) { reward ->
                            RewardCatalogCard(
                                reward = reward,
                                currentBalance = currentBalance,
                                onRedeemClick = { selectedRewardForRedeem = reward }
                            )
                        }
                    }
                }
            } else {
                // Tab 1: My Redemptions History
                if (myRedemptions.isEmpty()) {
                    EmptyStateView(
                        title = "No Redemptions Yet",
                        message = "Redeem your earned points for gift cards and airtime vouchers above."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(myRedemptions, key = { it.id }) { redemption ->
                            RedemptionStatusCard(redemption = redemption)
                        }
                    }
                }
            }
        }
    }

    // Redemption Request Modal Dialog
    selectedRewardForRedeem?.let { reward ->
        RedemptionRequestDialog(
            reward = reward,
            currentBalance = currentBalance,
            onDismiss = { selectedRewardForRedeem = null },
            onConfirmRedemption = { targetAddress ->
                coroutineScope.launch {
                    val idempotencyKey = UUID.randomUUID().toString()
                    when (val result = walletRepository.requestRedemption(reward.id, targetAddress, idempotencyKey)) {
                        is Result.Success -> {
                            selectedRewardForRedeem = null
                            confirmedRedemptionCode = "QE-RED-${result.data.id.takeLast(6).uppercase()}"
                        }
                        is Result.Error -> {
                            selectedRewardForRedeem = null
                            redemptionErrorMessage = result.exception.message ?: "Failed to process redemption request."
                        }
                        is Result.Loading -> {
                            // Handled by UI
                        }
                    }
                }
            }
        )
    }

    // Confirmation Success Dialog
    confirmedRedemptionCode?.let { code ->
        AlertDialog(
            onDismissRequest = { confirmedRedemptionCode = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MintTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Redemption Submitted!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your request has been deducted from your balance and recorded on the ledger. Tracking Reference:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(
                        text = "You can track progress in the 'My Redemptions' tab. Upon verification, your digital voucher code will appear directly on your card.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                QuizEarnButton(
                    text = "View My Redemptions",
                    onClick = {
                        confirmedRedemptionCode = null
                        selectedMainTab = 1
                    },
                    buttonType = QuizEarnButtonType.Primary
                )
            }
        )
    }

    // Rejection / Error Dialog
    redemptionErrorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { redemptionErrorMessage = null },
            title = {
                Text(
                    text = "Redemption Issue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { redemptionErrorMessage = null }) {
                    Text("OK", fontWeight = FontWeight.Bold, color = ElectricIndigo)
                }
            }
        )
    }
}

/**
 * Card displaying user redemption status with badge and digital voucher code when approved.
 */
@Composable
private fun RedemptionStatusCard(
    redemption: Redemption,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    val (statusColor, statusIcon, statusLabel) = when (redemption.status) {
        DomainRedemptionStatus.APPROVED -> Triple(MintTeal, Icons.Default.CheckCircle, "Approved")
        DomainRedemptionStatus.PENDING -> Triple(WarningAmber, Icons.Default.HourglassEmpty, "Under Verification")
        DomainRedemptionStatus.PROCESSING -> Triple(ElectricIndigo, Icons.Default.HourglassEmpty, "Processing")
        DomainRedemptionStatus.REJECTED -> Triple(ErrorCrimson, Icons.Default.Cancel, "Rejected (Refunded)")
    }

    QuizEarnCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = redemption.rewardTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Recipient: ${redemption.payoutDetails}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "-%,d Pts".format(redemption.pointsDeducted),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = ErrorCrimson
                )
            }

            // Approved: reveal digital voucher code
            if (redemption.status == DomainRedemptionStatus.APPROVED && redemption.digitalCode != null) {
                Spacer(modifier = Modifier.height(spacing.xs))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MintTeal.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MintTeal.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "DIGITAL VOUCHER CODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = redemption.digitalCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Rejected: explain reason and points refund
            if (redemption.status == DomainRedemptionStatus.REJECTED && redemption.rejectionReason != null) {
                Spacer(modifier = Modifier.height(spacing.xs))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ErrorCrimson.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Reason: ${redemption.rejectionReason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorCrimson,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "%,d points have been automatically refunded to your wallet.".format(redemption.pointsDeducted),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top Balance Header bar.
 */
@Composable
private fun CatalogBalanceHeader(balance: Int) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AVAILABLE TO REDEEM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "%,d Points".format(balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ElectricIndigo
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MintTeal.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "5,000 Pts = $5.00 USD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MintTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Reward item card in the 2-column catalog grid.
 */
@Composable
private fun RewardCatalogCard(
    reward: RewardItem,
    currentBalance: Int,
    onRedeemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val canAfford = currentBalance >= reward.pointsRequired

    val (brandColor, brandIcon) = when (reward.brand) {
        RewardBrand.AMAZON -> Pair(Color(0xFFFF9900), Icons.Default.ShoppingBag)
        RewardBrand.GOOGLE_PLAY -> Pair(Color(0xFF00C853), Icons.Default.Shop)
        RewardBrand.APPLE -> Pair(Color(0xFF6B7280), Icons.Default.CardGiftcard)
        RewardBrand.MOBILE_RECHARGE -> Pair(ElectricIndigo, Icons.Default.PhoneAndroid)
    }

    QuizEarnCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Icon Circle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = brandIcon,
                    contentDescription = null,
                    tint = brandColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            // Value Tag
            Text(
                text = "$%.2f USD".format(reward.monetaryValueUsd),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            // Reward Name
            Text(
                text = reward.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(spacing.xs))

            // Points Cost Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (canAfford) RewardGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "%,d Pts".format(reward.pointsRequired),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (canAfford) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            // Redeem Action Button
            Button(
                onClick = onRedeemClick,
                enabled = canAfford,
                shape = ButtonShape,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricIndigo,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (canAfford) "Redeem" else "Need ${reward.pointsRequired - currentBalance} Pts",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (canAfford) 13.sp else 10.sp
                )
            }
        }
    }
}

/**
 * Interactive Redemption Confirmation & Target Address Dialog.
 */
@Composable
private fun RedemptionRequestDialog(
    reward: RewardItem,
    currentBalance: Int,
    onDismiss: () -> Unit,
    onConfirmRedemption: (targetAddress: String) -> Unit
) {
    val spacing = LocalSpacing.current
    val isMobileTopUp = reward.brand == RewardBrand.MOBILE_RECHARGE

    var targetInput by remember { mutableStateOf("") }
    var agreedToFairPlay by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Redeem ${reward.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                // Deduction breakdown
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ErrorCrimson.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Points Deduction:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "-%,d Pts".format(reward.pointsRequired),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ErrorCrimson
                        )
                    }
                }

                // Recipient Input Field
                QuizEarnTextField(
                    value = targetInput,
                    onValueChange = {
                        targetInput = it
                        if (it.isNotBlank()) inputError = null
                    },
                    label = if (isMobileTopUp) "Mobile Phone Number" else "Recipient Email Address",
                    leadingIcon = if (isMobileTopUp) Icons.Default.Phone else Icons.Default.Email,
                    isError = inputError != null,
                    errorMessage = inputError
                )

                // Fair Play Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { agreedToFairPlay = !agreedToFairPlay },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreedToFairPlay,
                        onCheckedChange = { agreedToFairPlay = it },
                        colors = CheckboxDefaults.colors(checkedColor = ElectricIndigo)
                    )
                    Text(
                        text = "I certify that all points were earned fairly without scripts or multi-accounting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            QuizEarnButton(
                text = "Confirm & Deduct Points",
                onClick = {
                    if (targetInput.isBlank()) {
                        inputError = if (isMobileTopUp) "Phone number is required" else "Email address is required"
                    } else if (!agreedToFairPlay) {
                        inputError = "Please certify the Fair Play terms to continue"
                    } else {
                        onConfirmRedemption(targetInput.trim())
                    }
                },
                buttonType = QuizEarnButtonType.Primary
            )
        },
        dismissButton = {
            QuizEarnButton(
                text = "Cancel",
                onClick = onDismiss,
                buttonType = QuizEarnButtonType.Outlined
            )
        }
    )
}
