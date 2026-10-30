package com.quizearn.app.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.quizearn.app.core.result.Result
import com.quizearn.app.data.local.AuthSessionManager
import com.quizearn.app.data.local.UserSession
import com.quizearn.app.data.remote.FirebaseBackend
import com.quizearn.app.domain.model.User
import com.quizearn.app.domain.model.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseBackend.auth,
    private val sessionManager: AuthSessionManager = AuthSessionManager.getInstance()
) : AuthRepository {
    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            launch {
                try {
                    trySend(firebaseUser?.let { loadProfile(it) })
                } catch (exception: Exception) {
                    close(exception)
                }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun restoreActiveSession(): Flow<User?> = currentUser

    override suspend fun signIn(email: String, password: String): Result<User> = runCatching {
        val credential = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = credential.user ?: error("Firebase did not return an authenticated user.")
        loadProfile(firebaseUser, usernameHint = email.substringBefore("@"))
    }.toAppResult()

    override suspend fun signUp(email: String, password: String, username: String): Result<User> = runCatching {
        val credential = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = credential.user ?: error("Firebase did not return the registered user.")
        loadProfile(firebaseUser, usernameHint = username.trim())
    }.toAppResult()

    override suspend fun signInAsGuest(): Result<User> = runCatching {
        val firebaseUser = auth.signInAnonymously().await().user
            ?: error("Firebase did not return an anonymous user.")
        loadProfile(firebaseUser, usernameHint = "Guest")
    }.toAppResult()

    override suspend fun convertGuestToAccount(
        email: String,
        password: String,
        username: String
    ): Result<User> = runCatching {
        val guest = auth.currentUser?.takeIf { it.isAnonymous }
            ?: error("An active anonymous Firebase account is required.")
        val credential = EmailAuthProvider.getCredential(email.trim(), password)
        val linked = guest.linkWithCredential(credential).await().user
            ?: error("Firebase did not return the linked account.")
        loadProfile(linked, usernameHint = username.trim())
    }.toAppResult()

    override suspend fun updateProfile(username: String, avatarColorHex: Long): Result<User> = runCatching {
        val cleanUsername = username.trim()
        require(cleanUsername.length >= 3) { "Username must be at least 3 characters." }
        FirebaseBackend.call(
            "updateProfile",
            mapOf("username" to cleanUsername, "avatarColorHex" to avatarColorHex)
        )
        val firebaseUser = auth.currentUser ?: error("No active Firebase user.")
        loadProfile(firebaseUser, usernameHint = cleanUsername)
    }.toAppResult()

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.toAppResult()

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
        sessionManager.clearSession()
    }.toAppResult()

    private suspend fun loadProfile(firebaseUser: FirebaseUser, usernameHint: String? = null): User {
        val profile = FirebaseBackend.call(
            "ensureProfile",
            mapOf("username" to (usernameHint ?: firebaseUser.displayName.orEmpty()))
        )
        val user = User(
            id = firebaseUser.uid,
            username = profile["username"] as? String
                ?: error("Firebase profile did not include a username."),
            email = firebaseUser.email.orEmpty(),
            pointsBalance = (profile["pointsBalance"] as? Number)?.toLong()
                ?: error("Firebase profile did not include a wallet balance."),
            level = (profile["level"] as? Number)?.toInt()
                ?: error("Firebase profile did not include a level."),
            totalQuizzesPlayed = (profile["totalQuizzesPlayed"] as? Number)?.toInt()
                ?: error("Firebase profile did not include quiz statistics."),
            accuracyRate = (profile["accuracyRate"] as? Number)?.toDouble()
                ?: error("Firebase profile did not include quiz accuracy."),
            role = UserRole.USER
        )
        sessionManager.saveSession(
            UserSession(
                token = "firebase-session",
                userId = user.id,
                email = user.email,
                username = user.username,
                pointsBalance = user.pointsBalance,
                level = user.level,
                totalQuizzesPlayed = user.totalQuizzesPlayed,
                accuracyRate = user.accuracyRate,
                role = user.role.name,
                isGuest = firebaseUser.isAnonymous
            )
        )
        return user
    }

    private fun <T> kotlin.Result<T>.toAppResult(): Result<T> = this.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(it) }
    )
}
