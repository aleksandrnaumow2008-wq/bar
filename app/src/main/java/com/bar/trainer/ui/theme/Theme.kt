package com.bar.trainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ObsidianBg = Color(0xFF090B10)
val DeepSlateSurface = Color(0xFF121622)
val FrostedGlassFill = Color(0x18FFFFFF)
val FrostedGlassBorder = Color(0x33FFFFFF)

val NeonCyan = Color(0xFF00F5D4)
val ElectricMagenta = Color(0xFFFF2A85)
val AmberGold = Color(0xFFFFB703)
val EmeraldSuccess = Color(0xFF00E676)
val CrimsonError = Color(0xFFFF1744)
val TextPrimary = Color(0xFFF4F6FA)
val TextSecondary = Color(0xFF94A3B8)

private val BarDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = ElectricMagenta,
    tertiary = AmberGold,
    background = ObsidianBg,
    surface = DeepSlateSurface,
    onPrimary = ObsidianBg,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = CrimsonError
)

val BarTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.8.sp
    )
)

@Composable
fun BarTrainerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BarDarkColorScheme,
        typography = BarTypography,
        content = content
    )
}
