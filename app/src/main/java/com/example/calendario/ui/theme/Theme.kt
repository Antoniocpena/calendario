package com.example.calendario.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CampusColorScheme = lightColorScheme(
    primary = CampusBlue,
    onPrimary = CampusSurface,

    primaryContainer = CampusLightBlue,
    onPrimaryContainer = CampusBlueDark,

    background = CampusBackground,
    onBackground = CampusText,

    surface = CampusSurface,
    onSurface = CampusText,

    surfaceVariant = CampusLightGray,
    onSurfaceVariant = CampusMuted,

    outline = CampusOutline,
    error = CampusRed
)

@Composable
fun CalendarioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CampusColorScheme,
        typography = CampusTypography,
        content = content
    )
}
