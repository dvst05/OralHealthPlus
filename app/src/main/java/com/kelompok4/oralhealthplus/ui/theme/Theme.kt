package com.kelompok4.oralhealthplus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = OralPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = OralSecondarySoft,
    onPrimaryContainer = OralTextPrimary,
    secondary = OralPrimaryLight,
    onSecondary = OralTextPrimary,
    background = OralBackgroundLight,
    onBackground = OralTextPrimary,
    surface = OralSurfaceWhite,
    onSurface = OralTextPrimary,
    surfaceVariant = OralBackgroundLight,
    onSurfaceVariant = OralTextMuted,
    outline = OralBorderHairline,
    error = OralErrorRed,
    onError = Color.White
)

@Composable
fun OralHealthPlusTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
