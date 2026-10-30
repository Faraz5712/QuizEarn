package com.quizearn.app.ui.screens.auth

import com.quizearn.app.domain.model.User

/**
 * UI state for authentication operations (Sign In, Sign Up, Guest session).
 */
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

/**
 * UI state for password reset operations.
 */
sealed interface PasswordResetUiState {
    data object Idle : PasswordResetUiState
    data object Loading : PasswordResetUiState
    data class Success(val email: String) : PasswordResetUiState
    data class Error(val message: String) : PasswordResetUiState
}

/**
 * Client-side validation utilities for authentication inputs.
 */
object AuthValidator {

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotBlank() && emailRegex.matches(trimmed)
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidUsername(username: String): Boolean {
        return username.trim().length in 3..25
    }

    fun validateLoginForm(email: String, password: String): String? {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) return "Please enter your email address."
        if (!isValidEmail(cleanEmail)) return "Please enter a valid email format."
        if (password.isBlank()) return "Please enter your password."
        if (!isValidPassword(password)) return "Password must be at least 6 characters."
        return null
    }

    fun validateSignUpForm(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ): String? {
        val cleanUser = username.trim()
        val cleanEmail = email.trim()

        if (cleanUser.isBlank()) return "Please enter your name or username."
        if (!isValidUsername(cleanUser)) return "Username must be between 3 and 25 characters."
        if (cleanEmail.isBlank()) return "Please enter your email address."
        if (!isValidEmail(cleanEmail)) return "Please enter a valid email format."
        if (password.isBlank()) return "Please enter a password."
        if (!isValidPassword(password)) return "Password must be at least 6 characters."
        if (password != confirmPassword) return "Passwords do not match."
        if (!termsAccepted) return "You must accept the Terms of Service & Privacy Policy."
        return null
    }

    fun validateForgotPasswordForm(email: String): String? {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) return "Please enter your email address."
        if (!isValidEmail(cleanEmail)) return "Please enter a valid email format."
        return null
    }
}
