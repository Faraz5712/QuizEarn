package com.quizearn.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.R
import com.quizearn.app.ui.theme.BadgeShape
import com.quizearn.app.ui.theme.RewardGold
import com.quizearn.app.ui.theme.RewardGoldContainer

@Composable
fun RewardBadge(
    points: Long,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .background(
                color = RewardGoldContainer,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = RewardGold.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_quizearn_spark),
            contentDescription = "$points Points Badge",
            tint = RewardGold,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$points pts",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6B4E00)
        )
    }
}

@Composable
fun RewardBadge(
    points: Int,
    modifier: Modifier = Modifier
) {
    RewardBadge(points = points.toLong(), modifier = modifier)
}
