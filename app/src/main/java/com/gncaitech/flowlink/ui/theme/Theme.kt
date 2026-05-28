package com.gncaitech.flowlink.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlowLinkColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = NavyFaint,
    onPrimaryContainer = Navy,
    secondary = MedTeal,
    onSecondary = Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = MedTeal,
    tertiary = ArtRed,
    onTertiary = Color.White,
    tertiaryContainer = RedLight,
    onTertiaryContainer = ArtRed,
    error = ArtRed,
    background = SnowGray,
    onBackground = G700,
    surface = Color.White,
    onSurface = G700,
    surfaceVariant = G100,
    outline = G200,
)

@Composable
fun FlowLinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlowLinkColorScheme,
        typography = AppTypography,
        content = content
    )
}
