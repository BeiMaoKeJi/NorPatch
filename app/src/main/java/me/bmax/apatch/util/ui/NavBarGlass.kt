package me.bmax.apatch.util.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

/**
 * FolkPatch-style frosted dock effect, drawn purely with drawBehind:
 * translucent base (lets the global wallpaper show through) + top highlight
 * gradient + specular line + inner glow + hairline border. No third-party
 * liquid/blur dependency — works on every API level.
 */
@Composable
fun Modifier.glassDockEffect(): Modifier {
    val isDarkTheme = !MaterialTheme.colorScheme.background.luminance().let { it > 0.5f }
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    return this.then(
        Modifier.drawBehind {
            val w = size.width
            val h = size.height
            val radius = 24.dp.toPx()
            val cornerRadius = CornerRadius(radius, radius)
            val halfDp = 0.5.dp.toPx()
            val oneDp = 1.dp.toPx()
            val twoDp = 2.dp.toPx()
            val oneAndHalfDp = 1.5.dp.toPx()
            val fourDp = 4.dp.toPx()
            val eightDp = 8.dp.toPx()

            // Base: translucent surface — the global background shows through.
            drawRoundRect(
                color = baseColor.copy(alpha = if (isDarkTheme) 0.55f else 0.62f),
                cornerRadius = cornerRadius,
            )

            // Hairline border.
            drawRoundRect(
                color = if (isDarkTheme) {
                    Color.White.copy(alpha = 0.08f)
                } else {
                    Color.White.copy(alpha = 0.25f)
                },
                topLeft = Offset(halfDp, halfDp),
                size = Size(w - oneDp, h - oneDp),
                cornerRadius = CornerRadius(
                    (radius - halfDp).coerceAtLeast(0f),
                    (radius - halfDp).coerceAtLeast(0f),
                ),
                style = Stroke(width = oneDp),
            )

            // Glass highlight gradient (top brighter → bottom softer).
            val highAlpha = if (isDarkTheme) 0.15f else 0.26f
            val lowAlpha = if (isDarkTheme) 0.05f else 0.11f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = highAlpha),
                        Color.White.copy(alpha = lowAlpha),
                    ),
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = cornerRadius,
            )

            // Specular reflection line.
            val specularAlpha = if (isDarkTheme) 0.22f else 0.50f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = specularAlpha),
                        Color.Transparent,
                    ),
                    startX = w * 0.15f,
                    endX = w * 0.85f,
                ),
                topLeft = Offset(w * 0.15f, twoDp),
                size = Size(w * 0.7f, oneAndHalfDp),
                cornerRadius = CornerRadius(oneDp),
            )

            // Inner glow near the bottom edge.
            val glowAlpha = if (isDarkTheme) 0.06f else 0.12f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = glowAlpha)),
                ),
                topLeft = Offset(fourDp, h - eightDp),
                size = Size(w - eightDp, fourDp),
                cornerRadius = CornerRadius(twoDp),
            )
        },
    )
}
