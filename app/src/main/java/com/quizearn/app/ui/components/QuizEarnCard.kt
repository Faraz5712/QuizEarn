package com.quizearn.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val CardShape = com.quizearn.app.ui.theme.CardShape
val OptionShape = com.quizearn.app.ui.theme.OptionShape
val ButtonShape = com.quizearn.app.ui.theme.ButtonShape

@Composable
fun QuizEarnCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    border: BorderStroke? = null,
    borderColor: Color? = null,
    elevation: Dp = 2.dp,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val effectiveBorder = border ?: borderColor?.let { BorderStroke(1.dp, it) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = colors,
        border = effectiveBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}
