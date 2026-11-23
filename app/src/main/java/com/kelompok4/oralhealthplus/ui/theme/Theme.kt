package com.kelompok4.oralhealthplus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Desain Figma hanya punya mode terang, jadi dark mode dan dynamic color dimatikan
// supaya warna aplikasi selalu sama dengan Figma di semua HP.
private val OralHealthColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = SurfaceBlue,
    onPrimaryContainer = BluePrimary,
    secondary = BlueLight,
    onSecondary = White,
    secondaryContainer = ChipBlue,
    onSecondaryContainer = BlueDark,
    tertiary = GreenSuccess,
    onTertiary = White,
    tertiaryContainer = AmberSoft,
    onTertiaryContainer = AmberText,
    background = White,
    onBackground = BluePrimary,
    surface = White,
    onSurface = BluePrimary,
    outline = SurfaceBlue,
    error = RedError,
    onError = White,
)

@Composable
fun OralHealthPlusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OralHealthColorScheme,
        typography = Typography,
        content = content,
    )
}
