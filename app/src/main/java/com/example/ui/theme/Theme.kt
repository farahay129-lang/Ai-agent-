package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF002533),
    primaryContainer = Color(0xFF0A3459),
    onPrimaryContainer = Color(0xFFB3F4FF),
    secondary = HoloBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0F2B57),
    onSecondaryContainer = Color(0xFFC2DCFF),
    tertiary = NeonAmber,
    onTertiary = Color(0xFF2B1B00),
    background = CyberDarkBg,
    onBackground = TextPrimaryDark,
    surface = CyberDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = CyberDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF075985),
    secondary = Color(0xFF1D4ED8),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = Color(0xFFD97706),
    onTertiary = Color.White,
    background = CyberLightBg,
    onBackground = TextPrimaryLight,
    surface = CyberLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CyberLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = CyberLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
