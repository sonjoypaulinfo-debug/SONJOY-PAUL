package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ForestTealPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestTealDark,
    onPrimaryContainer = Color.White,
    secondary = SeafoamGreenSecondary,
    onSecondary = Color.White,
    tertiary = SunsetCoralTertiary,
    onTertiary = Color.White,
    background = SlateDarkBackground,
    onBackground = Color(0xFFF8FAFC), // very light slate
    surface = SlateDarkSurface,
    onSurface = Color(0xFFF1F5F9), // light slate
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8), // mid slate
    outline = Color(0xFF334155) // slate-700 border
)

private val LightColorScheme = lightColorScheme(
    primary = ForestTealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0FDFA), // Teal-50 (super clean fallback)
    onPrimaryContainer = ForestTealDark,
    secondary = SeafoamGreenSecondary,
    onSecondary = Color.White,
    tertiary = SunsetCoralTertiary,
    onTertiary = Color.White,
    background = NeutralLightBackground,
    onBackground = Color(0xFF0F172A), // Slate 900 (ultra readable)
    surface = NeutralLightSurface,
    onSurface = Color(0xFF1E293B), // Slate 800 (clean body)
    surfaceVariant = NeutralLightSurfaceVariant, // White-gray 50
    onSurfaceVariant = Color(0xFF64748B), // Slate 500
    outline = Color(0xFFE2E8F0) // Slate 200 (perfect modern thin border)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // uses existing Type.kt
        content = content
    )
}
