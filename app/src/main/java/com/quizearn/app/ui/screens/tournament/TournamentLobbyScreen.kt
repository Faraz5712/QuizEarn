package com.quizearn.app.ui.screens.tournament

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
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.R
import com.quizearn.app.data.repository.TournamentRepository
import com.quizearn.app.data.repository.TournamentRepositoryImpl
import com.quizearn.app.data.repository.WalletRepository
import com.quizearn.app.data.repository.WalletRepositoryImpl
import com.quizearn.app.domain.model.TournamentEvent
import com.quizearn.app.domain.model.TournamentStatus
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.RewardBadge
import com.quizearn.app.ui.theme.ButtonShape
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.ErrorCrimson
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold
import com.quizearn.app.ui.theme.VibrantViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentLobbyScreen(
    onBack: () -> Unit,
    onEnterTournament: (tournamentId: String, categoryId: String) -> Unit,
    tournamentRepository: TournamentRepository = remember { TournamentRepositoryImpl.getInstance() },
    walletRepository: WalletRepository = remember { WalletRepositoryImpl.getInstance() },
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filterOptions = listOf("All", "Live Now", "Upcoming")

    // Reactive streams from Authoritative Backend Repositories
    val tournaments by tournamentRepository.getTournaments().collectAsState(initial = emptyList())
    val balance by walletRepository.getBalance().collectAsState(initial = 5450L)

    var confirmingTournament by remember { mutableStateOf<TournamentEvent?>(null) }

    val filteredTournaments = remember(selectedFilterIndex, tournaments) {
        when (selectedFilterIndex) {
            1 -> tournaments.filter { it.status == TournamentStatus.LIVE }
            2 -> tournaments.filter { it.status == TournamentStatus.UPCOMING }
            else -> tournaments
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Live Tournaments",
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
                        points = balance,
                        modifier = Modifier.padding(end = spacing.sm)
                    )
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
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                filterOptions.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedFilterIndex == index,
                        onClick = { selectedFilterIndex = index },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            // Tournament Events List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTournaments, key = { it.id }) { event ->
                    TournamentCard(
                        event = event,
                        onRegisterClick = {
                            if (!event.isRegistered) {
                                confirmingTournament = event
                            }
                        },
                        onEnterClick = { onEnterTournament(event.id, event.categoryId) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(spacing.lg))
                }
            }
        }

        // Registration Confirmation Dialog with atomic fee deduction
        confirmingTournament?.let { event ->
            AlertDialog(
                onDismissRequest = { confirmingTournament = null },
                title = {
                    Text(
                        text = "Register for Tournament",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Category: ${event.categoryName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Entry Fee:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${event.entryFeePoints} Pts",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RewardGold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Your Balance:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "$balance Pts",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (balance >= event.entryFeePoints) MintTeal else ErrorCrimson
                            )
                        }
                        if (balance < event.entryFeePoints) {
                            Text(
                                text = "Insufficient balance to cover entry fee.",
                                style = MaterialTheme.typography.labelSmall,
                                color = ErrorCrimson
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = confirmingTournament
                            confirmingTournament = null
                            if (target != null) {
                                coroutineScope.launch {
                                    val result = tournamentRepository.registerForTournament(target.id)
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar(
                                            "Registered for '${target.title}'! ${target.entryFeePoints} Pts deducted."
                                        )
                                    } else {
                                        val err = result.exceptionOrNull()?.message ?: "Registration failed."
                                        snackbarHostState.showSnackbar(err)
                                    }
                                }
                            }
                        },
                        enabled = balance >= event.entryFeePoints,
                        shape = ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                    ) {
                        Text("Confirm & Pay")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmingTournament = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun TournamentCard(
    event: TournamentEvent,
    onRegisterClick: () -> Unit,
    onEnterClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val isLive = event.status == TournamentStatus.LIVE

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLive) VibrantViolet.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isLive) {
                        Brush.verticalGradient(
                            listOf(VibrantViolet.copy(alpha = 0.10f), Color.Transparent)
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent)
                        )
                    }
                )
                .padding(spacing.md)
        ) {
            // Status and Category Row
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
                            .background(if (isLive) ErrorCrimson else MintTeal)
                    )
                    Text(
                        text = if (isLive) "LIVE NOW" else "UPCOMING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isLive) ErrorCrimson else MintTeal
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = event.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(spacing.md))

            // Metrics row: Prize Pool & Entry Fee
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // Prize Pool
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_quizearn_spark),
                            contentDescription = null,
                            tint = RewardGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${event.prizePoolPoints} Pool",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RewardGold
                        )
                    }

                    // Entry fee
                    Text(
                        text = "Fee: ${event.entryFeePoints} Pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Remaining Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = event.remainingTimeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.md))

            // Footer with participants and Action CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${event.registeredParticipants} Registered",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                when {
                    isLive && event.isRegistered -> {
                        Button(
                            onClick = onEnterClick,
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VibrantViolet,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Enter Now",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    isLive && !event.isRegistered -> {
                        Button(
                            onClick = onRegisterClick,
                            shape = ButtonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricIndigo,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Register",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = onRegisterClick,
                            shape = ButtonShape
                        ) {
                            Text(
                                text = if (event.isRegistered) "Registered ✓" else "Register",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (event.isRegistered) MintTeal else ElectricIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
