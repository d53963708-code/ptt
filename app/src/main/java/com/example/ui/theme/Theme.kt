package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = TacticalDarkSlate,
    primaryContainer = ElectricCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = SignalLime,
    onSecondary = TacticalDarkSlate,
    tertiary = WarningOrange,
    onTertiary = TacticalDarkSlate,
    background = TacticalDarkSlate,
    onBackground = TextPrimary,
    surface = TacticalCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = TacticalCardBorder,
    onSurfaceVariant = TextSecondary,
    outline = TacticalCardBorder,
    error = AlertRed
)

private val LightColorScheme = darkColorScheme(
    // Force modern dark tactical theme for Walkie-Talkie high readability
    primary = ElectricCyan,
    onPrimary = TacticalDarkSlate,
    primaryContainer = ElectricCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = SignalLime,
    onSecondary = TacticalDarkSlate,
    tertiary = WarningOrange,
    onTertiary = TacticalDarkSlate,
    background = TacticalDarkSlate,
    onBackground = TextPrimary,
    surface = TacticalCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = TacticalCardBorder,
    onSurfaceVariant = TextSecondary,
    outline = TacticalCardBorder,
    error = AlertRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
