package com.bth.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bth.launcher.ui.theme.LocalLauncherColors

// PERF: static scrim brush allocated once instead of on every recomposition
private val scrimBrush = Brush.verticalGradient(
    listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.65f))
)

/**
 * Glassmorphism card: translucent fill, subtle gradient sheen, and a hairline
 * border that mimics frosted glass over the wallpaper.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val colors = LocalLauncherColors.current
    val shape = RoundedCornerShape(cornerRadius)

    // PERF: brushes are remembered per color so they are not re-allocated
    // on every recomposition of every card
    val fillBrush = remember(colors.glassSurface) {
        Brush.verticalGradient(
            listOf(
                colors.glassSurface.copy(
                    alpha = (colors.glassSurface.alpha + 0.06f).coerceAtMost(1f)
                ),
                colors.glassSurface
            )
        )
    }
    val borderBrush = remember(colors.glassBorder) {
        Brush.verticalGradient(
            listOf(colors.glassBorder, colors.glassBorder.copy(alpha = 0.05f))
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(fillBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
    ) {
        content()
    }
}

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassCard(modifier = modifier, cornerRadius = 100.dp, content = content)
}

/** Soft scrim placed behind sheets/overlays for depth. */
@Composable
fun OverlayScrim(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(scrimBrush))
}
