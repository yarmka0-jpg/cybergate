package com.bth.launcher.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.bth.launcher.data.LauncherTheme

/**
 * Custom theme engine. Each theme defines a cohesive palette plus a
 * two-stop background gradient rendered behind everything. For GLASS the
 * gradient is a translucent scrim so the wallpaper shows through while
 * keeping text readable (fixes the unreadable/flashing background).
 */
data class LauncherColors(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val surface: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val text: Color,
    val textSecondary: Color,
    val accent: Color,
    val onAccent: Color,
    val isLight: Boolean
)

val accentPalette = mapOf(
    "cyan" to Color(0xFF22D3EE),
    "emerald" to Color(0xFF34D399),
    "amber" to Color(0xFFFBBF24),
    "rose" to Color(0xFFFB7185),
    "blue" to Color(0xFF60A5FA)
)

fun buildColors(theme: LauncherTheme, accentKey: String): LauncherColors {
    val accent = accentPalette[accentKey] ?: accentPalette.getValue("cyan")
    return when (theme) {
        LauncherTheme.GLASS -> LauncherColors(
            backgroundTop = Color.Black.copy(alpha = 0.25f),
            backgroundBottom = Color.Black.copy(alpha = 0.55f),
            surface = Color(0xE6101418),
            glassSurface = Color.White.copy(alpha = 0.12f),
            glassBorder = Color.White.copy(alpha = 0.22f),
            text = Color.White,
            textSecondary = Color.White.copy(alpha = 0.7f),
            accent = accent,
            onAccent = Color(0xFF0A0E12),
            isLight = false
        )
        LauncherTheme.MIDNIGHT -> LauncherColors(
            backgroundTop = Color(0xFF050B16),
            backgroundBottom = Color(0xFF0C1D33),
            surface = Color(0xFF0A1422),
            glassSurface = Color(0xFF13243A).copy(alpha = 0.85f),
            glassBorder = Color(0xFF3B5A82).copy(alpha = 0.35f),
            text = Color(0xFFE8F0FA),
            textSecondary = Color(0xFF8DA4BF),
            accent = accent,
            onAccent = Color(0xFF050B16),
            isLight = false
        )
        LauncherTheme.DARK -> LauncherColors(
            backgroundTop = Color(0xFF121417),
            backgroundBottom = Color(0xFF1A1E24),
            surface = Color(0xFF1C2026),
            glassSurface = Color(0xFF22262D),
            glassBorder = Color.White.copy(alpha = 0.08f),
            text = Color(0xFFF4F4F5),
            textSecondary = Color(0xFFA1A1AA),
            accent = accent,
            onAccent = Color(0xFF121417),
            isLight = false
        )
        LauncherTheme.AMOLED -> LauncherColors(
            backgroundTop = Color.Black,
            backgroundBottom = Color.Black,
            surface = Color(0xFF0A0A0A),
            glassSurface = Color(0xFF111111),
            glassBorder = Color.White.copy(alpha = 0.1f),
            text = Color.White,
            textSecondary = Color(0xFF8A8A8A),
            accent = accent,
            onAccent = Color.Black,
            isLight = false
        )
        LauncherTheme.LIGHT -> LauncherColors(
            backgroundTop = Color(0xFFF7F7F8),
            backgroundBottom = Color(0xFFECEEF2),
            surface = Color.White,
            glassSurface = Color.White.copy(alpha = 0.85f),
            glassBorder = Color.Black.copy(alpha = 0.08f),
            text = Color(0xFF18181B),
            textSecondary = Color(0xFF52525B),
            accent = accent,
            onAccent = Color.White,
            isLight = true
        )
    }
}

/**
 * PERF: compositionLocalOf (NOT static) — with staticCompositionLocalOf every
 * animated color frame forced a full-tree recomposition (~60fps for 400ms on
 * each theme switch). Now only composables that actually read the colors
 * recompose.
 */
val LocalLauncherColors = compositionLocalOf {
    buildColors(LauncherTheme.GLASS, "cyan")
}

@Composable
fun LauncherThemeProvider(
    theme: LauncherTheme,
    accentKey: String,
    content: @Composable () -> Unit
) {
    val target = buildColors(theme, accentKey)

    // Smoothly animate theme switches without recreating anything
    val bgTop by animateColorAsState(target.backgroundTop, tween(400), label = "bgTop")
    val bgBottom by animateColorAsState(target.backgroundBottom, tween(400), label = "bgBottom")
    val surface by animateColorAsState(target.surface, tween(400), label = "surface")
    val glass by animateColorAsState(target.glassSurface, tween(400), label = "glass")
    val accent by animateColorAsState(target.accent, tween(400), label = "accent")
    val text by animateColorAsState(target.text, tween(400), label = "text")

    val animated = target.copy(
        backgroundTop = bgTop,
        backgroundBottom = bgBottom,
        surface = surface,
        glassSurface = glass,
        accent = accent,
        text = text
    )

    CompositionLocalProvider(LocalLauncherColors provides animated) {
        content()
    }
}
