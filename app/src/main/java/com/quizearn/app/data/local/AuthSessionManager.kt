package com.quizearn.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quizearn.app.domain.model.User
import com.quizearn.app.domain.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "quizearn_auth_session")

/**
 * Domain representation of an active, authenticated player session.
 */
data class UserSession(
    val token: String,
    val userId: String,
    val email: String,
    val username: String,
    val pointsBalance: Long = 0L,
    val level: Int = 1,
    val totalQuizzesPlayed: Int = 0,
    val accuracyRate: Double = 0.0,
    val role: String = "USER",
    val isGuest: Boolean = false,
    val avatarColorHex: Long = 0xFF5B5FEF,
    val expiresAtTimestamp: Long = 0L
) {
    fun toUser(): User = User(
        id = userId,
        username = username,
        email = email,
        pointsBalance = pointsBalance,
        level = level,
        totalQuizzesPlayed = totalQuizzesPlayed,
        accuracyRate = accuracyRate,
        role = try { UserRole.valueOf(role) } catch (_: Exception) { UserRole.USER }
    )
}

/**
 * Contract for persisting and retrieving user authentication session state.
 */
interface AuthSessionStorage {
    val currentSession: Flow<UserSession?>
    fun hasActiveSession(): Flow<Boolean>
    suspend fun saveSession(session: UserSession)
    suspend fun updateProfile(username: String, avatarColorHex: Long)
    suspend fun recordQuizCompletion(pointsEarned: Long, accuracyRate: Double)
    suspend fun clearSession()
}

/**
 * AndroidX DataStore implementation of [AuthSessionStorage].
 */
class DataStoreAuthSessionStorage(
    private val context: Context
) : AuthSessionStorage {

    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("user_email")
        val USERNAME = stringPreferencesKey("user_username")
        val POINTS_BALANCE = longPreferencesKey("points_balance")
        val LEVEL = intPreferencesKey("user_level")
        val TOTAL_QUIZZES = intPreferencesKey("total_quizzes_played")
        val ACCURACY_RATE = doublePreferencesKey("accuracy_rate")
        val ROLE = stringPreferencesKey("user_role")
        val IS_GUEST = booleanPreferencesKey("is_guest")
        val AVATAR_COLOR = longPreferencesKey("avatar_color_hex")
        val EXPIRES_AT = longPreferencesKey("expires_at_timestamp")
    }

    override val currentSession: Flow<UserSession?> = context.authDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val token = prefs[Keys.TOKEN]
            val userId = prefs[Keys.USER_ID]
            if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                null
            } else {
                UserSession(
                    token = token,
                    userId = userId,
                    email = prefs[Keys.EMAIL] ?: "",
                    username = prefs[Keys.USERNAME] ?: "Player",
                    pointsBalance = prefs[Keys.POINTS_BALANCE] ?: 0L,
                    level = prefs[Keys.LEVEL] ?: 1,
                    totalQuizzesPlayed = prefs[Keys.TOTAL_QUIZZES] ?: 0,
                    accuracyRate = prefs[Keys.ACCURACY_RATE] ?: 0.0,
                    role = prefs[Keys.ROLE] ?: "USER",
                    isGuest = prefs[Keys.IS_GUEST] ?: false,
                    avatarColorHex = prefs[Keys.AVATAR_COLOR] ?: 0xFF5B5FEF,
                    expiresAtTimestamp = prefs[Keys.EXPIRES_AT] ?: 0L
                )
            }
        }

    override fun hasActiveSession(): Flow<Boolean> = currentSession.map { it != null }

    override suspend fun saveSession(session: UserSession) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = session.token
            prefs[Keys.USER_ID] = session.userId
            prefs[Keys.EMAIL] = session.email
            prefs[Keys.USERNAME] = session.username
            prefs[Keys.POINTS_BALANCE] = session.pointsBalance
            prefs[Keys.LEVEL] = session.level
            prefs[Keys.TOTAL_QUIZZES] = session.totalQuizzesPlayed
            prefs[Keys.ACCURACY_RATE] = session.accuracyRate
            prefs[Keys.ROLE] = session.role
            prefs[Keys.IS_GUEST] = session.isGuest
            prefs[Keys.AVATAR_COLOR] = session.avatarColorHex
            prefs[Keys.EXPIRES_AT] = session.expiresAtTimestamp
        }
    }

    override suspend fun updateProfile(username: String, avatarColorHex: Long) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.USERNAME] = username
            prefs[Keys.AVATAR_COLOR] = avatarColorHex
        }
    }

    override suspend fun recordQuizCompletion(pointsEarned: Long, accuracyRate: Double) {
        context.authDataStore.edit { prefs ->
            val curPoints = prefs[Keys.POINTS_BALANCE] ?: 0L
            val curQuizzes = prefs[Keys.TOTAL_QUIZZES] ?: 0
            val curAccuracy = prefs[Keys.ACCURACY_RATE] ?: 0.0
            val newQuizzes = curQuizzes + 1
            val updatedAccuracy = if (newQuizzes > 1) {
                ((curAccuracy * curQuizzes) + accuracyRate) / newQuizzes
            } else {
                accuracyRate
            }
            prefs[Keys.POINTS_BALANCE] = curPoints + pointsEarned
            prefs[Keys.TOTAL_QUIZZES] = newQuizzes
            prefs[Keys.ACCURACY_RATE] = updatedAccuracy
        }
    }

    override suspend fun clearSession() {
        context.authDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

/**
 * In-memory fallback of [AuthSessionStorage] for testing and preview modes.
 */
class InMemoryAuthSessionStorage : AuthSessionStorage {
    private val _session = MutableStateFlow<UserSession?>(null)

    override val currentSession: Flow<UserSession?> = _session.asStateFlow()

    override fun hasActiveSession(): Flow<Boolean> = _session.map { it != null }

    override suspend fun saveSession(session: UserSession) {
        _session.value = session
    }

    override suspend fun updateProfile(username: String, avatarColorHex: Long) {
        _session.value = _session.value?.copy(
            username = username,
            avatarColorHex = avatarColorHex
        )
    }

    override suspend fun recordQuizCompletion(pointsEarned: Long, accuracyRate: Double) {
        val cur = _session.value ?: return
        val newQuizzes = cur.totalQuizzesPlayed + 1
        val updatedAccuracy = if (newQuizzes > 1) {
            ((cur.accuracyRate * cur.totalQuizzesPlayed) + accuracyRate) / newQuizzes
        } else {
            accuracyRate
        }
        _session.value = cur.copy(
            pointsBalance = cur.pointsBalance + pointsEarned,
            totalQuizzesPlayed = newQuizzes,
            accuracyRate = updatedAccuracy
        )
    }

    override suspend fun clearSession() {
        _session.value = null
    }
}

/**
 * Singleton coordinator for managing the application's active session.
 */
class AuthSessionManager(
    private val storage: AuthSessionStorage
) {
    val currentSession: Flow<UserSession?> = storage.currentSession
    fun hasActiveSession(): Flow<Boolean> = storage.hasActiveSession()

    suspend fun saveSession(session: UserSession) = storage.saveSession(session)
    suspend fun updateProfile(username: String, avatarColorHex: Long) = storage.updateProfile(username, avatarColorHex)
    suspend fun recordQuizCompletion(pointsEarned: Long, accuracyRate: Double) = storage.recordQuizCompletion(pointsEarned, accuracyRate)
    suspend fun clearSession() = storage.clearSession()

    companion object {
        @Volatile
        private var instance: AuthSessionManager? = null

        fun getInstance(context: Context? = null): AuthSessionManager {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val storage = if (context != null) {
                        DataStoreAuthSessionStorage(context.applicationContext)
                    } else {
                        InMemoryAuthSessionStorage()
                    }
                    AuthSessionManager(storage).also { instance = it }
                }
            }
        }
    }
}
