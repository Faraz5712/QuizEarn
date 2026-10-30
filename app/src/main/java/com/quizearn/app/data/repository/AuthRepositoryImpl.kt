package com.quizearn.app.data.repository

import com.quizearn.app.core.result.Result
import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.local.UserSession
import com.quizearn.app.data.remote.FirebaseBackend
import com.quizearn.app.domain.model.User
import com.quizearn.app.domain.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Concrete implementation of [AuthRepository].
 * Backed by [AuthSessionManager] for persistent session restoration across restarts,
 * offline demo execution, and seamless integration with Firebase Auth.
 */
class AuthRepositoryImpl(
    private val sessionManager: AuthSessionManager = AuthSessionManager.getInstance()
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override fun restoreActiveSession(): Flow<User?> {
        return sessionManager.currentSession.map { session ->
            val user = session?.toUser()
            _currentUser.value = user
            user
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        delay(500) // Simulate network round-trip

        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.Error(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val user = if (cleanEmail == "demo@quizearn.app") {
            User(
                id = "usr_demo_001",
                username = "QuizChampion",
                email = cleanEmail,
                pointsBalance = 1500L,
                level = 3,
                totalQuizzesPlayed = 42,
                accuracyRate = 88.5,
                role = UserRole.USER
            )
        } else {
            val generatedUsername = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            User(
                id = "usr_${UUID.randomUUID().toString().take(8)}",
                username = generatedUsername,
                email = cleanEmail,
                pointsBalance = 250L,
                level = 1,
                totalQuizzesPlayed = 0,
                accuracyRate = 0.0,
                role = UserRole.USER
            )
        }

        // Persist session token and details securely
        val session = UserSession(
            token = "jwt_${UUID.randomUUID()}",
            userId = user.id,
            email = user.email,
            username = user.username,
            pointsBalance = user.pointsBalance,
            level = user.level,
            totalQuizzesPlayed = user.totalQuizzesPlayed,
            accuracyRate = user.accuracyRate,
            role = user.role.name,
            isGuest = false,
            avatarColorHex = 0xFF5B5FEF,
            expiresAtTimestamp = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days
        )
        sessionManager.saveSession(session)
        _currentUser.value = user

        return Result.Success(user)
    }

    override suspend fun signUp(email: String, password: String, username: String): Result<User> {
        delay(600) // Simulate network round-trip

        val cleanEmail = email.trim().lowercase()
        val cleanUsername = username.trim()

        if (cleanUsername.length < 3) {
            return Result.Error(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.Error(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val newUser = User(
            id = "usr_${UUID.randomUUID().toString().take(8)}",
            username = cleanUsername,
            email = cleanEmail,
            pointsBalance = 100L, // Welcome bonus
            level = 1,
            totalQuizzesPlayed = 0,
            accuracyRate = 0.0,
            role = UserRole.USER
        )

        val session = UserSession(
            token = "jwt_${UUID.randomUUID()}",
            userId = newUser.id,
            email = newUser.email,
            username = newUser.username,
            pointsBalance = newUser.pointsBalance,
            level = newUser.level,
            totalQuizzesPlayed = newUser.totalQuizzesPlayed,
            accuracyRate = newUser.accuracyRate,
            role = newUser.role.name,
            isGuest = false,
            avatarColorHex = 0xFF5B5FEF,
            expiresAtTimestamp = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        sessionManager.saveSession(session)
        _currentUser.value = newUser

        return Result.Success(newUser)
    }

    override suspend fun signInAsGuest(): Result<User> {
        delay(350) // Fast local guest session creation

        val guestSuffix = UUID.randomUUID().toString().take(6).uppercase()
        val guestUser = User(
            id = "guest_${guestSuffix.lowercase()}",
            username = "Guest_$guestSuffix",
            email = "guest_$guestSuffix@quizearn.local",
            pointsBalance = 50L,
            level = 1,
            totalQuizzesPlayed = 0,
            accuracyRate = 0.0,
            role = UserRole.USER
        )

        val session = UserSession(
            token = "guest_tok_${UUID.randomUUID()}",
            userId = guestUser.id,
            email = guestUser.email,
            username = guestUser.username,
            pointsBalance = guestUser.pointsBalance,
            level = guestUser.level,
            totalQuizzesPlayed = guestUser.totalQuizzesPlayed,
            accuracyRate = guestUser.accuracyRate,
            role = guestUser.role.name,
            isGuest = true,
            avatarColorHex = 0xFF8B5CF6,
            expiresAtTimestamp = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000) // 7 days for guests
        )
        sessionManager.saveSession(session)
        _currentUser.value = guestUser

        return Result.Success(guestUser)
    }

    override suspend fun convertGuestToAccount(
        email: String,
        password: String,
        username: String
    ): Result<User> {
        delay(600)
        val currentGuest = _currentUser.value
        val retainedBalance = currentGuest?.pointsBalance ?: 50L
        val retainedQuizzes = currentGuest?.totalQuizzesPlayed ?: 0
        val retainedAccuracy = currentGuest?.accuracyRate ?: 0.0

        val cleanEmail = email.trim().lowercase()
        val cleanUsername = username.trim()

        if (cleanUsername.length < 3) {
            return Result.Error(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.Error(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.Error(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val convertedUser = User(
            id = currentGuest?.id ?: "usr_${UUID.randomUUID().toString().take(8)}",
            username = cleanUsername,
            email = cleanEmail,
            pointsBalance = retainedBalance + 50L, // Extra bonus for conversion
            level = currentGuest?.level ?: 1,
            totalQuizzesPlayed = retainedQuizzes,
            accuracyRate = retainedAccuracy,
            role = UserRole.USER
        )

        val session = UserSession(
            token = "jwt_${UUID.randomUUID()}",
            userId = convertedUser.id,
            email = convertedUser.email,
            username = convertedUser.username,
            pointsBalance = convertedUser.pointsBalance,
            level = convertedUser.level,
            totalQuizzesPlayed = convertedUser.totalQuizzesPlayed,
            accuracyRate = convertedUser.accuracyRate,
            role = convertedUser.role.name,
            isGuest = false,
            avatarColorHex = 0xFF5B5FEF,
            expiresAtTimestamp = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        sessionManager.saveSession(session)
        _currentUser.value = convertedUser

        return Result.Success(convertedUser)
    }

    override suspend fun updateProfile(username: String, avatarColorHex: Long): Result<User> {
        val cleanUsername = username.trim()
        if (cleanUsername.length < 3) {
            return Result.Error(IllegalArgumentException("Username must be at least 3 characters."))
        }

        sessionManager.updateProfile(cleanUsername, avatarColorHex)
        val updated = _currentUser.value?.copy(username = cleanUsername)
        if (updated != null) {
            _currentUser.value = updated
            return Result.Success(updated)
        }
        return Result.Error(IllegalStateException("No active user session to update."))
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        delay(500)
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.Error(IllegalArgumentException("Please enter a valid email address."))
        }
        return Result.Success(Unit)
    }

    override suspend fun signOut(): Result<Unit> {
        delay(200)
        sessionManager.clearSession()
        _currentUser.value = null
        return Result.Success(Unit)
    }

    companion object {
        @Volatile
        private var instance: AuthRepositoryImpl? = null

        fun getInstance(sessionManager: AuthSessionManager? = null): AuthRepository {
            if (FirebaseBackend.isConfigured) {
                return FirebaseAuthRepository(sessionManager = sessionManager ?: AuthSessionManager.getInstance())
            }
            return instance ?: synchronized(this) {
                instance ?: AuthRepositoryImpl(
                    sessionManager ?: AuthSessionManager.getInstance()
                ).also { instance = it }
            }
        }
    }
}
