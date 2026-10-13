package com.archiekuo.jukebox.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFF2B65A)

class JbColors(
    val bg: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val text: Color,
    val subText: Color,
    val accent: Color,
    val onAccent: Color,
    val line: Color,
    val dark: Boolean,
)

val DarkJb = JbColors(
    bg = Color(0xFF0B1226),
    surface = Color(0xFF141C33),
    surfaceHigh = Color(0xFF1D2745),
    text = Color.White,
    subText = Color(0xFF9AA4BF),
    accent = Gold,
    onAccent = Color(0xFF2A1B00),
    line = Color(0x22FFFFFF),
    dark = true,
)

val LightJb = JbColors(
    bg = Color(0xFFFAF6EE),
    surface = Color.White,
    surfaceHigh = Color(0xFFF1E9DA),
    text = Color(0xFF1B2340),
    subText = Color(0xFF6B7390),
    accent = Color(0xFFC9822B),
    onAccent = Color.White,
    line = Color(0x1A000000),
    dark = false,
)

val LocalJb = staticCompositionLocalOf { DarkJb }

/** Follows the system light / dark setting. */
@Composable
fun JukeboxTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val c = if (dark) DarkJb else LightJb
    val scheme = if (dark) {
        darkColorScheme(
            primary = c.accent,
            background = c.bg,
            surface = c.surface,
            onSurface = c.text,
            onBackground = c.text,
        )
    } else {
        lightColorScheme(
            primary = c.accent,
            background = c.bg,
            surface = c.surface,
            onSurface = c.text,
            onBackground = c.text,
        )
    }
    CompositionLocalProvider(LocalJb provides c) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

val ScriptFont = androidx.compose.ui.text.font.FontFamily(
    androidx.compose.ui.text.font.Font(com.archiekuo.jukebox.R.font.script_bold, androidx.compose.ui.text.font.FontWeight.Bold),
)
val SerifFont = androidx.compose.ui.text.font.FontFamily(
    androidx.compose.ui.text.font.Font(com.archiekuo.jukebox.R.font.serif_bold, androidx.compose.ui.text.font.FontWeight.Bold),
)
