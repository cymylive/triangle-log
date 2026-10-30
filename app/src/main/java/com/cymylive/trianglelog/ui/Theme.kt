package com.cymylive.trianglelog.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF121212)
val Surface1 = Color(0xFF1E1E1E)
val Surface2 = Color(0xFF2A2A2A)
val Accent = Color(0xFF7BD88F)
val AccentDim = Color(0xFF3D6B4A)
val TextMain = Color(0xFFEDEDED)
val TextSub = Color(0xFF9A9A9A)
val Danger = Color(0xFFE57373)

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF0D1F12),
    background = Bg,
    onBackground = TextMain,
    surface = Surface1,
    onSurface = TextMain,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSub,
    error = Danger
)

@Composable
fun TriangleLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
