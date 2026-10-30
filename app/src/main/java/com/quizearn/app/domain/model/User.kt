package com.quizearn.app.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val pointsBalance: Long = 0L,
    val level: Int = 1,
    val totalQuizzesPlayed: Int = 0,
    val accuracyRate: Double = 0.0,
    val role: UserRole = UserRole.USER
)

enum class UserRole {
    USER,
    SUPPORT,
    ADMIN
}
