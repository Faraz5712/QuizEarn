package com.quizearn.app.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizearn.app.R
import com.quizearn.app.data.repository.PreferencesRepository
import com.quizearn.app.data.repository.PreferencesRepositoryImpl
import com.quizearn.app.ui.components.QuizEarnButton
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold
import com.quizearn.app.ui.theme.VibrantViolet
import kotlinx.coroutines.launch

/**
 * Slide model representing each value proposition page of the onboarding carousel.
 */
data class OnboardingSlide(
    val categoryBadge: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val accentColor: Color,
    val backgroundBrush: Brush
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onCompleteOnboarding: () -> Unit,
    preferencesRepository: PreferencesRepository? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current
    val coroutineScope = rememberCoroutineScope()

    val prefs = remember(context, preferencesRepository) {
        preferencesRepository ?: PreferencesRepositoryImpl(context.applicationContext)
    }

    val slides = remember {
        listOf(
            OnboardingSlide(
                categoryBadge = "KNOWLEDGE & DISCOVERY",
                title = "Play & Learn",
                description = "Explore thousands of curated, engaging quiz questions across science, history, tech, and culture. Expand your intellect daily.",
                iconResId = R.drawable.ic_quizearn_logo,
                accentColor = ElectricIndigo,
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(ElectricIndigo.copy(alpha = 0.15f), Color.Transparent)
                )
            ),
            OnboardingSlide(
                categoryBadge = "GLOBAL COMPETITION",
                title = "Climb the Ranks",
                description = "Compete with real quiz enthusiasts on verified leaderboards, join daily live tournaments, and protect your winning streak.",
                iconResId = R.drawable.ic_splash_logo,
                accentColor = VibrantViolet,
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(VibrantViolet.copy(alpha = 0.15f), Color.Transparent)
                )
            ),
            OnboardingSlide(
                categoryBadge = "VERIFIED REWARDS",
                title = "Earn Real Rewards",
                description = "Accumulate verified points backed by our immutable ledger and redeem them for digital gift cards, cashouts, and exclusive perks.",
                iconResId = R.drawable.ic_quizearn_spark,
                accentColor = RewardGold,
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(RewardGold.copy(alpha = 0.18f), Color.Transparent)
                )
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { slides.size })
    val isLastPage = pagerState.currentPage == slides.size - 1

    val finishOnboarding: () -> Unit = {
        coroutineScope.launch {
            prefs.setOnboardingCompleted(true)
            onCompleteOnboarding()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.md, vertical = spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_quizearn_spark),
                        contentDescription = null,
                        tint = ElectricIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "QuizEarn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Skip button (visible on non-last pages)
                if (!isLastPage) {
                    TextButton(onClick = finishOnboarding) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg, vertical = spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = spacing.lg)
                ) {
                    repeat(slides.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "indicatorWidth"
                        )
                        val color by animateColorAsState(
                            targetValue = if (isSelected) {
                                slides[pagerState.currentPage].accentColor
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            },
                            animationSpec = tween(durationMillis = 300),
                            label = "indicatorColor"
                        )

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Action Buttons
                if (isLastPage) {
                    QuizEarnButton(
                        text = "Get Started",
                        onClick = finishOnboarding,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = finishOnboarding) {
                            Text(
                                text = "Skip All",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        QuizEarnButton(
                            text = "Next",
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { pageIndex ->
            val slide = slides[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Visual Hero Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(slide.backgroundBrush)
                            .padding(spacing.lg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Badge pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = slide.accentColor.copy(alpha = 0.12f),
                                modifier = Modifier.padding(bottom = spacing.lg)
                            ) {
                                Text(
                                    text = slide.categoryBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = slide.accentColor,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            // Central Graphic Emblem
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 2.dp,
                                        color = slide.accentColor.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = slide.iconResId),
                                    contentDescription = slide.title,
                                    tint = slide.accentColor,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.xl))

                // Slide Title
                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(spacing.md))

                // Slide Description
                Text(
                    text = slide.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )
            }
        }
    }
}
