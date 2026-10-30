package com.quizearn.app.ui.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizearn.app.R
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.QuizRepository
import com.quizearn.app.data.repository.QuizRepositoryImpl
import com.quizearn.app.data.repository.TournamentRepositoryImpl
import com.quizearn.app.data.remote.FirebaseBackend
import com.quizearn.app.ui.components.CardShape
import com.quizearn.app.ui.components.OptionShape
import com.quizearn.app.ui.components.QuizEarnOutlinedButton
import com.quizearn.app.ui.components.QuizEarnPrimaryButton
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.ErrorCrimson
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold

@Composable
fun QuizResultScreen(
    sessionId: String,
    tournamentId: String? = null,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit,
    quizRepository: QuizRepository = QuizRepositoryImpl.getInstance(),
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    var isReviewExpanded by remember { mutableStateOf(false) }

    var evaluatedResult by remember { mutableStateOf<EvaluatedQuizResult?>(null) }
    var tournamentSubmissionMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionId, tournamentId) {
        when (val res = quizRepository.getQuizResult(sessionId)) {
            is Result.Success -> {
                evaluatedResult = res.data
                if (!tournamentId.isNullOrBlank() && FirebaseBackend.isConfigured) {
                    val submission = TournamentRepositoryImpl.getInstance()
                        .submitTournamentResult(tournamentId, sessionId)
                    tournamentSubmissionMessage = submission.fold(
                        onSuccess = { "Verified tournament result submitted." },
                        onFailure = { "Tournament result was not accepted: ${it.message ?: "Unknown error"}" }
                    )
                }
            }
            else -> {
                // Fallback will use local defaults
            }
        }
    }

    // Evaluated result state (produced server-authoritatively)
    val resultSummary = evaluatedResult?.summary ?: remember(sessionId) {
        QuizResultSummary(
            sessionId = sessionId,
            categoryTitle = "Science & Nature",
            score = 40,
            totalQuestions = 5,
            correctCount = 4,
            pointsEarned = 50L,
            accuracyRate = 80.0,
            timeElapsedSeconds = 48
        )
    }

    val questionReviews = evaluatedResult?.reviews ?: remember(sessionId) {
        listOf(
            QuestionReviewItem(
                questionNumber = 1,
                questionText = "What is the closest planet to the Sun in our solar system?",
                options = listOf("Venus", "Mercury", "Mars", "Earth"),
                playerSelectedOptionIndex = 1,
                correctOptionIndex = 1,
                isCorrect = true,
                explanationText = "Mercury is the smallest planet and closest to the Sun, orbiting at roughly 58 million kilometers."
            ),
            QuestionReviewItem(
                questionNumber = 2,
                questionText = "Which gas do plants primarily absorb during photosynthesis?",
                options = listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"),
                playerSelectedOptionIndex = 1,
                correctOptionIndex = 1,
                isCorrect = true,
                explanationText = "Plants absorb carbon dioxide (CO2) and water, converting them into glucose and oxygen via sunlight."
            ),
            QuestionReviewItem(
                questionNumber = 3,
                questionText = "What is the chemical symbol for Gold?",
                options = listOf("Au", "Ag", "Fe", "Pb"),
                playerSelectedOptionIndex = 1,
                correctOptionIndex = 0,
                isCorrect = false,
                explanationText = "The chemical symbol for Gold is 'Au', derived from the Latin word 'Aurum'. 'Ag' represents Silver."
            ),
            QuestionReviewItem(
                questionNumber = 4,
                questionText = "How many bones are in the adult human body?",
                options = listOf("186", "206", "226", "246"),
                playerSelectedOptionIndex = 1,
                correctOptionIndex = 1,
                isCorrect = true,
                explanationText = "An adult human skeleton consists of 206 bones, down from around 270 bones at birth due to bone fusion."
            ),
            QuestionReviewItem(
                questionNumber = 5,
                questionText = "Which element has the atomic number 1?",
                options = listOf("Helium", "Hydrogen", "Lithium", "Carbon"),
                playerSelectedOptionIndex = 1,
                correctOptionIndex = 1,
                isCorrect = true,
                explanationText = "Hydrogen is the lightest chemical element on the periodic table with an atomic number of 1."
            )
        )
    }

    val tierColor = remember(resultSummary.performanceTier) {
        Color(android.graphics.Color.parseColor(resultSummary.performanceTier.colorHex))
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.lg, vertical = spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            tournamentSubmissionMessage?.let { message ->
                Text(
                    text = message,
                    color = if (message.startsWith("Verified")) MintTeal else ErrorCrimson,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.sm))
            }
            // Header Celebratory Emblem
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(tierColor.copy(alpha = 0.15f))
                    .border(2.dp, tierColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = tierColor,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(spacing.md))

            // Performance Tier Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tierColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = resultSummary.performanceTier.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = tierColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            Text(
                text = "Quiz Completed!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = resultSummary.performanceTier.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.lg))

            // 4-Grid Performance Metrics Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Final Score",
                            value = "${resultSummary.correctCount}/${resultSummary.totalQuestions}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "Accuracy",
                            value = "${resultSummary.accuracyRate.toInt()}%",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "Points Won",
                            value = "+${resultSummary.pointsEarned} Pts",
                            valueColor = RewardGold,
                            isPoints = true,
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            label = "Time Taken",
                            value = "${resultSummary.timeElapsedSeconds}s",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.lg))

            // Expandable Review Answers Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isReviewExpanded = !isReviewExpanded }
                            .padding(spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Review Answers (${questionReviews.size} Questions)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = if (isReviewExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isReviewExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = isReviewExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.md, vertical = spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(spacing.md)
                        ) {
                            questionReviews.forEach { review ->
                                QuestionReviewCard(review = review)
                            }
                            Spacer(modifier = Modifier.height(spacing.sm))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Action Buttons
            QuizEarnPrimaryButton(
                text = "Play Again",
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            QuizEarnOutlinedButton(
                text = "Back to Home",
                onClick = onBackHome,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isPoints: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isPoints) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_quizearn_spark),
                    contentDescription = null,
                    tint = RewardGold,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun QuestionReviewCard(
    review: QuestionReviewItem
) {
    val spacing = LocalSpacing.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OptionShape,
        color = MaterialTheme.colorScheme.background,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (review.isCorrect) MintTeal.copy(alpha = 0.5f) else ErrorCrimson.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md)
        ) {
            // Header with correctness badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q${review.questionNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ElectricIndigo
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (review.isCorrect) MintTeal.copy(alpha = 0.15f) else ErrorCrimson.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (review.isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (review.isCorrect) MintTeal else ErrorCrimson,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (review.isCorrect) "Correct" else "Incorrect",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (review.isCorrect) MintTeal else ErrorCrimson
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xs))

            // Question text
            Text(
                text = review.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            // Options feedback
            review.options.forEachIndexed { index, option ->
                val isSelected = review.playerSelectedOptionIndex == index
                val isCorrectAnswer = review.correctOptionIndex == index

                val optionBg = when {
                    isCorrectAnswer -> MintTeal.copy(alpha = 0.15f)
                    isSelected -> ErrorCrimson.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }

                val optionBorder = when {
                    isCorrectAnswer -> MintTeal
                    isSelected -> ErrorCrimson
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = optionBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, optionBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCorrectAnswer || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isCorrectAnswer -> MintTeal
                                isSelected -> ErrorCrimson
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        if (isCorrectAnswer) {
                            Text(
                                text = "Correct Answer",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MintTeal
                            )
                        } else if (isSelected) {
                            Text(
                                text = "Your Choice",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorCrimson
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            // Explanation box
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = RewardGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = review.explanationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
