package com.example.devicecontrol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF222222),
    onPrimary = Color.White,
    secondary = Color(0xFF4E6E5D),
    background = Color(0xFFFAFAF8),
    surface = Color(0xFFFAFAF8),
    onSurface = Color(0xFF202020),
    onSurfaceVariant = Color(0xFF6F6F68),
    outline = Color(0xFFE0E0DA),
)

@Composable
fun DeviceControlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
