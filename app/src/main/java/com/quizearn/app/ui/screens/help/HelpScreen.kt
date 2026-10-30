package com.quizearn.app.ui.screens.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.data.repository.SupportRepository
import com.quizearn.app.data.repository.SupportRepositoryImpl
import com.quizearn.app.domain.model.SupportTicket
import com.quizearn.app.domain.model.TicketCategory
import com.quizearn.app.domain.model.TicketStatus
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.QuizEarnButton
import com.quizearn.app.ui.components.QuizEarnButtonType
import com.quizearn.app.ui.components.QuizEarnCard
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    supportRepository: SupportRepository = remember { SupportRepositoryImpl.getInstance() },
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("FAQ", "My Tickets", "New Ticket")

    // Reactive tickets from repository
    val tickets by supportRepository.getTickets().collectAsState(initial = emptyList())

    // FAQ items list
    val faqs = remember {
        listOf(
            FaqItem(
                id = "faq_01",
                question = "How do I earn points?",
                answer = "You can earn points by completing daily category quizzes (+10 Pts per correct answer), checking in daily (+25 Pts streak bonus), finishing tasks, and placing in live weekend tournaments.",
                category = "Points"
            ),
            FaqItem(
                id = "faq_02",
                question = "How do reward redemptions work?",
                answer = "Once your points reach the minimum threshold (e.g. 5,000 Points = $5), you can redeem for Amazon, Google Play, or digital vouchers. Redemptions undergo automated verification and digital codes are delivered to your wallet.",
                category = "Rewards"
            ),
            FaqItem(
                id = "faq_03",
                question = "Are tournaments free to enter?",
                answer = "Yes! QuizEarn regularly hosts free-to-enter tournaments. For high-stakes championships, a nominal points entry fee may apply, with 100% of collected fees contributing to the winners' prize pool.",
                category = "Tournaments"
            ),
            FaqItem(
                id = "faq_04",
                question = "What is the Fair Play policy?",
                answer = "QuizEarn strictly enforces server-authoritative scoring and timing. Any use of bots, screen scrapers, or multiple accounts is automatically flagged by our anti-cheat system and leads to account termination.",
                category = "Fair Play"
            ),
            FaqItem(
                id = "faq_05",
                question = "What happens if I disconnect during a quiz?",
                answer = "If you disconnect or close the app before finishing, your answers up to that point will be submitted upon session timer expiration. Unanswered questions receive 0 points.",
                category = "Gameplay"
            ),
            FaqItem(
                id = "faq_06",
                question = "How do lifelines work?",
                answer = "Eligible quizzes offer two lifelines: '50:50' eliminates two incorrect answers, and 'Skip' bypasses a difficult question without penalty. Each lifeline may be used once per quiz session.",
                category = "Gameplay"
            )
        )
    }

    val expandedFaqIds = remember { mutableStateListOf<String>() }

    // Ticket Form state
    var selectedCategory by remember { mutableStateOf(TicketCategory.GAMEPLAY) }
    var subjectText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var lastSubmittedTicketId by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Help & Support",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ElectricIndigo,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = ElectricIndigo
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            val badge = if (index == 1 && tickets.isNotEmpty()) " (${tickets.size})" else ""
                            Text(
                                text = "$title$badge",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                // Tab 0: FAQ Section
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        items(faqs, key = { it.id }) { faq ->
                            val isExpanded = expandedFaqIds.contains(faq.id)

                            QuizEarnCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = CardShape,
                                borderColor = if (isExpanded) ElectricIndigo.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isExpanded) expandedFaqIds.remove(faq.id) else expandedFaqIds.add(faq.id)
                                        }
                                        .padding(spacing.md)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = faq.question,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                                            tint = ElectricIndigo
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        Column {
                                            Spacer(modifier = Modifier.height(spacing.sm))
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                            )
                                            Spacer(modifier = Modifier.height(spacing.sm))
                                            Text(
                                                text = faq.answer,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 1: My Support Tickets
                1 -> {
                    if (tickets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(spacing.xl),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(spacing.sm)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QuestionAnswer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Text(
                                    text = "No Support Tickets Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Have questions or found a bug? Submit a ticket from the 'New Ticket' tab.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(spacing.md),
                            verticalArrangement = Arrangement.spacedBy(spacing.md)
                        ) {
                            items(tickets, key = { it.id }) { ticket ->
                                TicketCard(ticket = ticket)
                            }
                        }
                    }
                }

                // Tab 2: Submit New Support Ticket
                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(spacing.md),
                        verticalArrangement = Arrangement.spacedBy(spacing.md)
                    ) {
                        item {
                            if (lastSubmittedTicketId != null) {
                                Surface(
                                    shape = CardShape,
                                    color = MintTeal.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MintTeal.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(spacing.md),
                                        verticalArrangement = Arrangement.spacedBy(spacing.xs)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MintTeal
                                            )
                                            Text(
                                                text = "Ticket Submitted: $lastSubmittedTicketId",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MintTeal
                                            )
                                        }
                                        Text(
                                            text = "Our customer support team will investigate and respond within 24 hours. You can track updates in the 'My Tickets' tab.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.height(spacing.xs))
                                        QuizEarnButton(
                                            text = "View in My Tickets",
                                            onClick = {
                                                lastSubmittedTicketId = null
                                                selectedTabIndex = 1
                                            },
                                            buttonType = QuizEarnButtonType.Secondary
                                        )
                                    }
                                }
                            } else {
                                QuizEarnCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = CardShape
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(spacing.md),
                                        verticalArrangement = Arrangement.spacedBy(spacing.md)
                                    ) {
                                        Text(
                                            text = "Submit a Support Ticket",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Describe your issue or feedback. We sanitize all input to protect your privacy.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        // Category Selection
                                        Text(
                                            text = "Category",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                                        ) {
                                            TicketCategory.values().take(3).forEach { cat ->
                                                FilterChip(
                                                    selected = selectedCategory == cat,
                                                    onClick = { selectedCategory = cat },
                                                    label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = ElectricIndigo,
                                                        selectedLabelColor = Color.White
                                                    )
                                                )
                                            }
                                        }

                                        // Subject Field
                                        OutlinedTextField(
                                            value = subjectText,
                                            onValueChange = { subjectText = it },
                                            label = { Text("Subject") },
                                            placeholder = { Text("Brief summary of inquiry...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true
                                        )

                                        // Message Field
                                        OutlinedTextField(
                                            value = messageText,
                                            onValueChange = { messageText = it },
                                            label = { Text("Detailed Description") },
                                            placeholder = { Text("Explain what happened or what you need assistance with...") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(130.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            maxLines = 5
                                        )

                                        // Submit Action
                                        QuizEarnButton(
                                            text = if (isSubmitting) "Submitting..." else "Submit Ticket",
                                            onClick = {
                                                if (subjectText.trim().length < 3) {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Subject must be at least 3 characters.")
                                                    }
                                                    return@QuizEarnButton
                                                }
                                                if (messageText.trim().length < 10) {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Description must be at least 10 characters.")
                                                    }
                                                    return@QuizEarnButton
                                                }

                                                coroutineScope.launch {
                                                    isSubmitting = true
                                                    val result = supportRepository.submitTicket(
                                                        subject = subjectText,
                                                        category = selectedCategory,
                                                        message = messageText
                                                    )
                                                    isSubmitting = false
                                                    if (result.isSuccess) {
                                                        val tkt = result.getOrThrow()
                                                        lastSubmittedTicketId = tkt.id
                                                        subjectText = ""
                                                        messageText = ""
                                                        snackbarHostState.showSnackbar("Ticket ${tkt.id} created successfully!")
                                                    } else {
                                                        val err = result.exceptionOrNull()?.message ?: "Submission failed."
                                                        snackbarHostState.showSnackbar(err)
                                                    }
                                                }
                                            },
                                            buttonType = QuizEarnButtonType.Primary,
                                            enabled = !isSubmitting,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
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
private fun TicketCard(
    ticket: SupportTicket,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    val (statusBg: Color, statusText: Color) = when (ticket.status) {
        TicketStatus.OPEN -> Pair(WarningAmber.copy(alpha = 0.15f), WarningAmber)
        TicketStatus.IN_PROGRESS -> Pair(ElectricIndigo.copy(alpha = 0.15f), ElectricIndigo)
        TicketStatus.RESOLVED -> Pair(MintTeal.copy(alpha = 0.15f), MintTeal)
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
            // Header Row: ID, Category, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = ticket.id,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElectricIndigo
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = ticket.category.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = ticket.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = ticket.subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = ticket.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Submitted ${ticket.createdAtIso}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // Official Admin Response Bubble if present
            ticket.adminResponse?.let { reply ->
                Spacer(modifier = Modifier.height(spacing.xs))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ElectricIndigo.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(spacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = ElectricIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Support Agent Response",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ElectricIndigo
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = reply,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
