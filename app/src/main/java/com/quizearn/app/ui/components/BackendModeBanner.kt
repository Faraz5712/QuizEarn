package com.quizearn.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quizearn.app.data.remote.BackendConfig
import com.quizearn.app.data.remote.BackendEnvironment
import com.quizearn.app.ui.theme.ElectricIndigo
import com.quizearn.app.ui.theme.WarningAmber

@Composable
fun BackendModeBanner(modifier: Modifier = Modifier) {
    val presentation = when (BackendConfig.activeEnvironment) {
        BackendEnvironment.MOCK_LOCAL -> BackendModePresentation(
            title = "Local demo mode",
            detail = "Mock data; points are not real or redeemable.",
            color = WarningAmber,
            icon = Icons.Filled.Code
        )
        BackendEnvironment.FIREBASE_EMULATOR -> BackendModePresentation(
            title = "Firebase emulator",
            detail = "Test environment; data is not production data.",
            color = ElectricIndigo,
            icon = Icons.Filled.Science
        )
        BackendEnvironment.FIREBASE_PRODUCTION -> BackendModePresentation(
            title = "Firebase backend configured",
            detail = "Internet access is required for cloud features.",
            color = ElectricIndigo,
            icon = Icons.Filled.Cloud
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = presentation.color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(presentation.icon, contentDescription = null, tint = presentation.color)
            Text(
                text = "${presentation.title} - ${presentation.detail}",
                modifier = Modifier.padding(start = 10.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class BackendModePresentation(
    val title: String,
    val detail: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
