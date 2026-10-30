package com.quizearn.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Core Brand Palette
val ElectricIndigo = Color(0xFF5B5FEF)
val ElectricIndigoLight = Color(0xFF7B7FFF)
val ElectricIndigoDark = Color(0xFF4346B8)

val VibrantViolet = Color(0xFF8B5CF6)
val VibrantVioletLight = Color(0xFFA78BFA)
val VibrantVioletDark = Color(0xFF6D28D9)

val MintTeal = Color(0xFF2DD4BF)
val MintTealContainer = Color(0xFFE6FAF7)
val MintTealDarkContainer = Color(0xFF0F3D37)

val RewardGold = Color(0xFFF5B942)
val RewardGoldContainer = Color(0xFFFEF6E6)
val RewardGoldDarkContainer = Color(0xFF4A3814)

val ErrorCrimson = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)

// Neutral Light Tokens
val BackgroundLight = Color(0xFFF7F8FC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEEF0F8)
val OutlineLight = Color(0xFFE2E4F0)
val TextPrimaryLight = Color(0xFF1E202C)
val TextSecondaryLight = Color(0xFF64748B)
val TextDisabledLight = Color(0xFF94A3B8)

// Neutral Dark Tokens
val BackgroundDark = Color(0xFF10111A)
val SurfaceDark = Color(0xFF1A1C28)
val SurfaceVariantDark = Color(0xFF24283B)
val OutlineDark = Color(0xFF323854)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextDisabledDark = Color(0xFF64748B)

val LightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECEEFE),
    onPrimaryContainer = Color(0xFF1F2266),
    secondary = VibrantViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3EDFD),
    onSecondaryContainer = Color(0xFF371E6A),
    tertiary = MintTeal,
    onTertiary = Color.White,
    tertiaryContainer = MintTealContainer,
    onTertiaryContainer = Color(0xFF003831),
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = ErrorCrimson,
    onError = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary = ElectricIndigoLight,
    onPrimary = Color(0xFF1A1C28),
    primaryContainer = ElectricIndigoDark,
    onPrimaryContainer = Color(0xFFECEEFE),
    secondary = VibrantVioletLight,
    onSecondary = Color(0xFF1A1C28),
    secondaryContainer = VibrantVioletDark,
    onSecondaryContainer = Color(0xFFF3EDFD),
    tertiary = MintTeal,
    onTertiary = Color(0xFF10111A),
    tertiaryContainer = MintTealDarkContainer,
    onTertiaryContainer = Color(0xFFE6FAF7),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = ErrorCrimson,
    onError = Color.White
)
