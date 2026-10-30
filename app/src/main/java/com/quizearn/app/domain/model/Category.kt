package com.quizearn.app.domain.model

data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val iconUrl: String? = null,
    val colorHex: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val totalQuestions: Int = 0
)
