package com.calculator.md3watch.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WatchColorScheme: ColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = MintOnPrimary,
    primaryContainer = MintPrimaryContainer,
    onPrimaryContainer = MintOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    background = AlmondBackground,
    onBackground = TextPrimary,
    surface = AlmondSurface,
    onSurface = TextPrimary,
    surfaceVariant = AlmondSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = WarmOutline,
)

@Composable
fun WatchCalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WatchColorScheme,
        typography = WatchTypography,
        content = content,
    )
}

