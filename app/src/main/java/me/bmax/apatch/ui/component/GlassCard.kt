package me.bmax.apatch.ui.component

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import coil3.compose.AsyncImage
import java.io.File
import me.bmax.apatch.APApplication

/**
 * NorPatch frosted-glass UI primitives.
 *
 * Pure presentation layer: all appearance settings are stored as ordinary
 * SharedPreferences keys and exposed through reactive Compose state, so the
 * homepage, the global background and the "Home Appearance" screen stay in
 * sync. No root / module / kernel logic lives here.
 */

// ---------------------------------------------------------------------------
// Appearance persistence (reactive)
// ---------------------------------------------------------------------------

object HomePrefs {

    private const val KEY_BG_ENABLED = "home_bg_enabled"
    private const val KEY_BG_URI = "home_bg_uri"
    private const val KEY_BG_ALPHA = "home_bg_alpha"
    private const val KEY_BG_BLUR = "home_bg_blur"
    private const val KEY_HOME_TITLE = "home_title"

    const val DEFAULT_TITLE = "NorPatch"
    const val DEFAULT_ALPHA = 60
    const val DEFAULT_BLUR = 25

    private val prefs get() = APApplication.sharedPreferences

    var bgEnabled by mutableStateOf(true)
        private set
    var bgImagePath by mutableStateOf("")
        private set
    var bgAlpha by mutableIntStateOf(DEFAULT_ALPHA)
        private set
    var bgBlur by mutableIntStateOf(DEFAULT_BLUR)
        private set
    var homeTitle by mutableStateOf(DEFAULT_TITLE)
        private set

    private var initialized = false

    /** Idempotent; safe to call from any composable before reading values. */
    fun ensureInit() {
        if (initialized) return
        initialized = true
        bgEnabled = prefs.getBoolean(KEY_BG_ENABLED, true)
        bgImagePath = prefs.getString(KEY_BG_URI, "") ?: ""
        bgAlpha = prefs.getInt(KEY_BG_ALPHA, DEFAULT_ALPHA)
        bgBlur = prefs.getInt(KEY_BG_BLUR, DEFAULT_BLUR)
        homeTitle = prefs.getString(KEY_HOME_TITLE, DEFAULT_TITLE) ?: DEFAULT_TITLE
    }

    fun setBackgroundEnabled(enabled: Boolean) {
        if (bgEnabled == enabled) return
        bgEnabled = enabled
        prefs.edit { putBoolean(KEY_BG_ENABLED, enabled) }
    }

    /** Path of the imported image; pass "" to clear (also deletes the file). */
    fun setBackgroundImagePath(path: String) {
        if (bgImagePath.isNotBlank() && bgImagePath != path) {
            runCatching { File(bgImagePath).delete() }
        }
        bgImagePath = path
        prefs.edit { putString(KEY_BG_URI, path) }
    }

    fun setAlpha(alpha: Int) {
        val v = alpha.coerceIn(0, 100)
        if (bgAlpha == v) return
        bgAlpha = v
        prefs.edit { putInt(KEY_BG_ALPHA, v) }
    }

    fun setBlur(blur: Int) {
        val v = blur.coerceIn(0, 100)
        if (bgBlur == v) return
        bgBlur = v
        prefs.edit { putInt(KEY_BG_BLUR, v) }
    }

    fun setTitle(title: String) {
        if (homeTitle == title) return
        homeTitle = title
        prefs.edit { putString(KEY_HOME_TITLE, title) }
    }

    fun backgroundFile(): File? {
        val path = bgImagePath
        return if (path.isBlank()) null
        else runCatching { File(path) }.getOrNull()?.takeIf { it.exists() }
    }

    fun reset() {
        setBackgroundEnabled(true)
        setBackgroundImagePath("")
        setAlpha(DEFAULT_ALPHA)
        setBlur(DEFAULT_BLUR)
        setTitle(DEFAULT_TITLE)
    }

    fun alphaFloat(): Float = bgAlpha / 100f

    /** Maps the 0..100 slider to a 0..36dp blur radius. */
    fun blurRadiusDp(): Dp = (bgBlur / 100f * 36f).dp
}

// ---------------------------------------------------------------------------
// Shapes
// ---------------------------------------------------------------------------

object GlassShapes {
    val Card = RoundedCornerShape(28.dp)
    val Small = RoundedCornerShape(22.dp)
    val Pill = RoundedCornerShape(50)
}

// ---------------------------------------------------------------------------
// Frosted-glass card
// ---------------------------------------------------------------------------

/**
 * A translucent "毛玻璃" card: semi-transparent white (light) / plum (dark)
 * fill with a soft vertical sheen, a thin highlight border and large rounded
 * corners — the FolkPatch M3E look. Pressing animates a gentle scale.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassShapes.Card,
    fillAlpha: Float = 0.78f,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val base = if (isDark) Color(0xFF241A20) else Color.White
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "glassPressScale",
    )

    val cardModifier = modifier
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    base.copy(alpha = (fillAlpha + 0.08f).coerceIn(0f, 1f)),
                    base.copy(alpha = fillAlpha),
                )
            )
        )
        .border(
            width = 1.dp,
            color = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.62f),
            shape = shape,
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
            } else {
                Modifier
            }
        )

    Box(modifier = cardModifier) {
        Column(modifier = Modifier.padding(contentPadding)) { content() }
    }
}

/** Shared press-scale modifier for non-card clickable items (drawer etc.). */
@Composable
fun pressScaleModifier(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.94f,
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pressScale",
    )
    return Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

// ---------------------------------------------------------------------------
// Global background layer
// ---------------------------------------------------------------------------

/**
 * Draws the global background: a soft tint plus (when enabled) the user-picked
 * image, with smooth animated alpha and blur. Blur is gated to Android 12+
 * where it runs on the GPU via RenderEffect, keeping older devices smooth.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeBackgroundImage(modifier: Modifier = Modifier) {
    HomePrefs.ensureInit()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val animatedAlpha by animateFloatAsState(
        targetValue = HomePrefs.alphaFloat(),
        animationSpec = tween(480),
        label = "bgAlpha",
    )
    val targetBlur = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        HomePrefs.blurRadiusDp()
    } else {
        0.dp
    }
    val animatedBlur by animateDpAsState(
        targetValue = targetBlur,
        animationSpec = tween(480),
        label = "bgBlur",
    )

    Box(modifier = modifier) {
        Box(
            Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF191114) else Color(0xFFFDF1F5))
        )
        val file = HomePrefs.backgroundFile()
        if (HomePrefs.bgEnabled && file != null) {
            AsyncImage(
                model = file,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(animatedBlur)
                        } else {
                            Modifier
                        }
                    )
                    .alpha(animatedAlpha),
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                            )
                        )
                    )
            )
        }
    }
}

/** Full-screen background layer to wrap app content. */
@Composable
fun HomeBackgroundLayer(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        HomeBackgroundImage(modifier = Modifier.fillMaxSize())
        content()
    }
}
