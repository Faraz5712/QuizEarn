package com.quizearn.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.repository.AuthRepository
import com.quizearn.app.data.repository.AuthRepositoryImpl
import com.quizearn.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel governing authentication state, credential validation,
 * guest logins, guest-to-account upgrades, and password reset flows.
 */
class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl.getInstance()
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _passwordResetState = MutableStateFlow<PasswordResetUiState>(PasswordResetUiState.Idle)
    val passwordResetState: StateFlow<PasswordResetUiState> = _passwordResetState.asStateFlow()

    fun signIn(email: String, password: String) {
        val validationError = AuthValidator.validateLoginForm(email, password)
        if (validationError != null) {
            _uiState.value = AuthUiState.Error(validationError)
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val res = authRepository.signIn(email, password)) {
                is Result.Success -> _uiState.value = AuthUiState.Success(res.data)
                is Result.Error -> _uiState.value = AuthUiState.Error(res.exception.message ?: "Sign in failed.")
                is Result.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun signUp(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ) {
        val validationError = AuthValidator.validateSignUpForm(
            username = username,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            termsAccepted = termsAccepted
        )
        if (validationError != null) {
            _uiState.value = AuthUiState.Error(validationError)
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val res = authRepository.signUp(email, password, username)) {
                is Result.Success -> _uiState.value = AuthUiState.Success(res.data)
                is Result.Error -> _uiState.value = AuthUiState.Error(res.exception.message ?: "Registration failed.")
                is Result.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun signInAsGuest() {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val res = authRepository.signInAsGuest()) {
                is Result.Success -> _uiState.value = AuthUiState.Success(res.data)
                is Result.Error -> _uiState.value = AuthUiState.Error(res.exception.message ?: "Guest session failed.")
                is Result.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun upgradeGuestAccount(email: String, password: String, username: String) {
        val validationError = AuthValidator.validateLoginForm(email, password)
        if (validationError != null) {
            _uiState.value = AuthUiState.Error(validationError)
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val res = authRepository.convertGuestToAccount(email, password, username)) {
                is Result.Success -> _uiState.value = AuthUiState.Success(res.data)
                is Result.Error -> _uiState.value = AuthUiState.Error(res.exception.message ?: "Account upgrade failed.")
                is Result.Loading -> _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val validationError = AuthValidator.validateForgotPasswordForm(email)
        if (validationError != null) {
            _passwordResetState.value = PasswordResetUiState.Error(validationError)
            return
        }

        _passwordResetState.value = PasswordResetUiState.Loading
        viewModelScope.launch {
            when (val res = authRepository.sendPasswordResetEmail(email)) {
                is Result.Success -> _passwordResetState.value = PasswordResetUiState.Success(email)
                is Result.Error -> _passwordResetState.value = PasswordResetUiState.Error(res.exception.message ?: "Password reset failed.")
                is Result.Loading -> _passwordResetState.value = PasswordResetUiState.Loading
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
        _passwordResetState.value = PasswordResetUiState.Idle
    }
}
