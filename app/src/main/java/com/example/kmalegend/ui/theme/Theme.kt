package com.example.kmalegend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColors = lightColors(
    primary = KmaRed,
    primaryVariant = KmaRedDark,
    secondary = KmaGold,
    background = Surface,
    surface = White,
    error = Error,
    onPrimary = White,
    onSecondary = Black,
    onBackground = OnSurfaceHigh,
    onSurface = OnSurfaceHigh,
    onError = White
)

private val DarkColors = darkColors(
    primary = KmaRedLight,
    primaryVariant = KmaRed,
    secondary = KmaGold,
    background = androidx.compose.ui.graphics.Color(0xFF121212),
    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
    error = KmaRedLight,
    onPrimary = White,
    onSecondary = Black,
    onBackground = White,
    onSurface = White
)

@Composable
fun KMALegendTheme(
    darkTheme: Boolean = false, // Cố định light mode, không theo thiết bị
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
