package com.quizearn.app.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Backward-compatible wrapper delegating to [SignInScreen].
 */
@Composable
fun AuthScreen(
    onSignInSuccess: () -> Unit,
    onBack: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    SignInScreen(
        onSignInSuccess = onSignInSuccess,
        onNavigateToSignUp = onNavigateToSignUp,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        modifier = modifier
    )
}
