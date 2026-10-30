package com.quizearn.app.ui.screens.profile

/**
 * Domain & UI representation of a player's profile summary.
 */
data class UserProfileSummary(
    val id: String = "user_001",
    val username: String = "QuizMaster99",
    val email: String = "player@quizearn.app",
    val avatarColorHex: Long = 0xFF5B5FEF,
    val avatarInitials: String = "QM",
    val level: Int = 4,
    val levelTitle: String = "Scholar Prodigy",
    val totalPoints: Int = 2450,
    val quizzesPlayed: Int = 48,
    val accuracyRate: Int = 85,
    val winStreak: Int = 7,
    val bestCategory: String = "Science & Tech",
    val memberSinceFormatted: String = "October 2026"
)

/**
 * State for the interactive Edit Profile dialog/sheet.
 */
data class EditProfileFormState(
    val username: String = "",
    val selectedAvatarColorHex: Long = 0xFF5B5FEF,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Available avatar color presets for player personalization.
 */
val AVATAR_COLOR_PRESETS = listOf(
    0xFF5B5FEF, // Electric Indigo
    0xFF8B5CF6, // Vibrant Violet
    0xFF2DD4BF, // Mint Teal
    0xFFF5B942, // Reward Gold
    0xFFEC4899, // Pink
    0xFF3B82F6, // Blue
    0xFF10B981, // Emerald Green
    0xFFF97316  // Orange
)
