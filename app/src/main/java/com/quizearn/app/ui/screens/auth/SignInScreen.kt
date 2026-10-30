package com.quizearn.app.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quizearn.app.R
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.AuthRepository
import com.quizearn.app.data.repository.AuthRepositoryImpl
import com.quizearn.app.ui.components.QuizEarnOutlinedButton
import com.quizearn.app.ui.components.QuizEarnPrimaryButton
import com.quizearn.app.ui.components.QuizEarnTextField
import com.quizearn.app.ui.components.BackendModeBanner
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.LocalSpacing
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    authRepository: AuthRepository = AuthRepositoryImpl.getInstance(),
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    var uiState by remember { mutableStateOf<AuthUiState>(AuthUiState.Idle) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onSignInSuccess()
        }
    }

    val handleSignIn: () -> Unit = {
        focusManager.clearFocus()
        val error = AuthValidator.validateLoginForm(email, password)
        if (error != null) {
            validationError = error
        } else {
            validationError = null
            uiState = AuthUiState.Loading
            coroutineScope.launch {
                when (val result = authRepository.signIn(email, password)) {
                    is Result.Success -> {
                        uiState = AuthUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        uiState = AuthUiState.Error(result.exception.message ?: "Authentication failed. Please try again.")
                    }
                    is Result.Loading -> {
                        uiState = AuthUiState.Loading
                    }
                }
            }
        }
    }

    val handleGuestSignIn: () -> Unit = {
        focusManager.clearFocus()
        validationError = null
        uiState = AuthUiState.Loading
        coroutineScope.launch {
            when (val result = authRepository.signInAsGuest()) {
                is Result.Success -> {
                    uiState = AuthUiState.Success(result.data)
                }
                is Result.Error -> {
                    uiState = AuthUiState.Error(result.exception.message ?: "Guest session failed.")
                }
                is Result.Loading -> {
                    uiState = AuthUiState.Loading
                }
            }
        }
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
                .padding(horizontal = spacing.lg, vertical = spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BackendModeBanner()
            Spacer(modifier = Modifier.height(spacing.lg))

            // Header Graphic & Wordmark
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ElectricIndigo.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_quizearn_logo),
                    contentDescription = "QuizEarn Logo",
                    tint = ElectricIndigo,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(spacing.md))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(spacing.xxs))

            Text(
                text = "Sign in to access your wallet, rank, and active quizzes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // Error Message Banner
            val activeError = validationError ?: (uiState as? AuthUiState.Error)?.message
            if (activeError != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing.md)
                ) {
                    Text(
                        text = activeError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            // Email Input
            QuizEarnTextField(
                value = email,
                onValueChange = {
                    email = it
                    validationError = null
                },
                label = "Email Address",
                placeholder = "player@quizearn.app",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(spacing.md))

            // Password Input
            QuizEarnTextField(
                value = password,
                onValueChange = {
                    password = it
                    validationError = null
                },
                label = "Password",
                placeholder = "Enter your password",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { handleSignIn() }
                )
            )

            // Remember Me and Forgot Password Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ElectricIndigo
                        )
                    )
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.lg))

            // Primary Sign In Button
            QuizEarnPrimaryButton(
                text = "Sign In",
                onClick = handleSignIn,
                isLoading = uiState is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.md))

            // Guest Login Button
            QuizEarnOutlinedButton(
                text = "Continue as Guest",
                onClick = handleGuestSignIn,
                enabled = uiState !is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            // Footer Switch to Sign Up
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToSignUp) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ElectricIndigo
                    )
                }
            }
        }
    }
}
