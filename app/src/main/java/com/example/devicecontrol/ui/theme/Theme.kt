package com.example.devicecontrol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF65558F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF210F40),
    secondary = Color(0xFF625B71),
    background = Color(0xFFF7F5FA),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFF0ECF4),
    onSurface = Color(0xFF1D1A22),
    onSurfaceVariant = Color(0xFF6F6877),
    outline = Color(0xFFDED8E5),
)

@Composable
fun DeviceControlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
