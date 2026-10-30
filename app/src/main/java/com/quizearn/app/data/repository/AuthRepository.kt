package com.quizearn.app.data.repository

import com.quizearn.app.core.result.Result
import com.quizearn.app.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface governing user authentication, session persistence, and profile lifecycle.
 */
interface AuthRepository {
    /**
     * Reactive stream of the currently active authenticated user (or null if signed out).
     */
    val currentUser: Flow<User?>

    /**
     * Restores the persisted active session on application startup.
     */
    fun restoreActiveSession(): Flow<User?>

    /**
     * Authenticate an existing user with email and password.
     */
    suspend fun signIn(email: String, password: String): Result<User>

    /**
     * Register a new user with email, password, and chosen display username.
     */
    suspend fun signUp(email: String, password: String, username: String): Result<User>

    /**
     * Create or restore an anonymous guest session for instant exploration.
     */
    suspend fun signInAsGuest(): Result<User>

    /**
     * Convert an active guest session to a permanent registered account while preserving points balance.
     */
    suspend fun convertGuestToAccount(email: String, password: String, username: String): Result<User>

    /**
     * Update player profile display name and avatar color preference.
     */
    suspend fun updateProfile(username: String, avatarColorHex: Long): Result<User>

    /**
     * Dispatch a password reset email link.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Invalidate active session and clear persisted credentials.
     */
    suspend fun signOut(): Result<Unit>
}
