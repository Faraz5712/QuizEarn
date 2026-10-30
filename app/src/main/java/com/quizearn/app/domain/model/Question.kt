package com.quizearn.app.domain.model

data class Question(
    val id: String,
    val categoryId: String,
    val text: String,
    val options: List<String>,
    val timeLimitSeconds: Int = 15,
    val pointsReward: Int = 10
)
