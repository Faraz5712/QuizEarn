package com.quizearn.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quizearn.app.R
import com.quizearn.app.data.repository.AuthRepository
import com.quizearn.app.data.repository.AuthRepositoryImpl
import com.quizearn.app.data.repository.PreferencesRepository
import com.quizearn.app.data.repository.PreferencesRepositoryImpl
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import com.quizearn.app.ui.theme.MintTeal
import com.quizearn.app.ui.theme.RewardGold
import com.quizearn.app.ui.theme.VibrantViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Animated branded Splash Screen that determines the initial application destination.
 */
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToHome: () -> Unit,
    preferencesRepository: PreferencesRepository? = null,
    authRepository: AuthRepository? = null,
    isAuthenticated: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val spacing = LocalSpacing.current

    val prefs = remember(context, preferencesRepository) {
        preferencesRepository ?: PreferencesRepositoryImpl(context.applicationContext)
    }

    val auth = remember(authRepository) {
        authRepository ?: AuthRepositoryImpl.getInstance()
    }

    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Logo entrance animation
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 850,
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 750,
                    easing = LinearEasing
                )
            )
        }

        // Secondary text fade in
        launch {
            delay(350)
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 650,
                    easing = LinearEasing
                )
            )
        }

        // Ambient pulse effect on the spark logo
        launch {
            pulseScale.animateTo(
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1800
                        1.0f at 0 using FastOutSlowInEasing
                        1.06f at 900 using FastOutSlowInEasing
                        1.0f at 1800 using FastOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }

        // Wait for branded splash display duration (1.8 seconds)
        delay(1800)

        // Evaluate user onboarding state
        val hasCompletedOnboarding = try {
            prefs.hasCompletedOnboarding.first()
        } catch (_: Exception) {
            false
        }

        // Evaluate persisted auth session
        val hasActiveSession = try {
            val user = auth.restoreActiveSession().first()
            user != null || isAuthenticated
        } catch (_: Exception) {
            isAuthenticated
        }

        when {
            !hasCompletedOnboarding -> onNavigateToOnboarding()
            !hasActiveSession -> onNavigateToAuth()
            else -> onNavigateToHome()
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.background
        )
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Branded Logo Container
                Box(
                    modifier = Modifier
                        .scale(logoScale.value * pulseScale.value)
                        .alpha(logoAlpha.value)
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ElectricIndigo.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(ElectricIndigo, VibrantViolet, MintTeal, RewardGold, ElectricIndigo)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_splash_logo),
                        contentDescription = "QuizEarn Logo",
                        modifier = Modifier.size(110.dp)
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xl))

                // Brand Name
                Text(
                    text = "QuizEarn",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                )

                Spacer(modifier = Modifier.height(spacing.xs))

                // Tagline
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    modifier = Modifier.alpha(contentAlpha.value)
                ) {
                    Text(
                        text = "Play",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ElectricIndigo
                    )
                    Text(
                        text = "Learn",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MintTeal
                    )
                    Text(
                        text = "Earn Rewards",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = RewardGold
                    )
                }

                Spacer(modifier = Modifier.height(spacing.xxl))

                // Loading Indicator with status text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(contentAlpha.value)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = ElectricIndigo,
                        strokeWidth = 2.5.dp
                    )

                    Spacer(modifier = Modifier.height(spacing.sm))

                    Text(
                        text = "Loading experience...",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom Brand Footnote
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = spacing.md),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Fair Play • Secure Rewards • v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.alpha(contentAlpha.value)
                )
            }
        }
    }
}
