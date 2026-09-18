package me.bmax.apatch.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * NorPatch semantic status colors (MD3-friendly, dark-mode aware).
 *
 * Semantics:
 *  - success: everything is healthy ("已安装 / 就绪")
 *  - warning: needs attention ("需更新")
 *  - info:    transient / ongoing ("安装中 / 卸载中 / 需重启")
 *  - muted:   inactive or unknown ("未安装 / 未知")
 *
 * Colors are Material tonal pairs tuned for both light and dark surfaces;
 * the composable accessors pick the readable variant automatically.
 */
object StatusColors {
    val Success = Color(0xFF2E7D32)
    val SuccessDark = Color(0xFF81C784)
    val Warning = Color(0xFFEF6C00)
    val WarningDark = Color(0xFFFFB74D)
    val Info = Color(0xFF1565C0)
    val InfoDark = Color(0xFF64B5F6)
    val Muted = Color(0xFF7E57C2)
    val MutedDark = Color(0xFFB39DDB)

    @Composable
    fun success(): Color = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) SuccessDark else Success

    @Composable
    fun warning(): Color = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) WarningDark else Warning

    @Composable
    fun info(): Color = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) InfoDark else Info

    @Composable
    fun muted(): Color = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) MutedDark else Muted
}