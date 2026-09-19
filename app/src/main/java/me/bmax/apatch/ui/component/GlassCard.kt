package me.bmax.apatch.ui.component
import me.bmax.apatch.ui.theme.BackgroundConfig

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import coil3.compose.AsyncImage
import java.io.File
import kotlin.math.roundToInt
import me.bmax.apatch.APApplication
import me.bmax.apatch.R

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

/** The four homepage blocks; each has its own enabled/background config. */
enum class HomeBlock(val key: String) {
    TITLE("title"),
    STATUS("status"),
    CUSTOM("custom"),
    DEVICE("device"),
}

/** Per-block background flavor. */
enum class BlockBgType(val key: String) {
    IMAGE("image"),
    SOLID("solid");

    companion object {
        fun fromKey(k: String?): BlockBgType = entries.firstOrNull { it.key == k } ?: SOLID
    }
}

/** Display types available for the homepage custom block. */
enum class CustomContentType(val key: String) {
    IMAGE("image"),
    UPTIME("uptime"),
    CUSTOM_TEXT("custom_text"),
    BASIC_INFO("basic_info"),
    DETAIL_PARAMS("detail_params"),
    DEVICE_NAME("device_name");

    companion object {
        fun fromKey(k: String?): CustomContentType = entries.firstOrNull { it.key == k } ?: DEVICE_NAME
    }
}

object HomePrefs {

    private const val KEY_BG_ENABLED = "home_bg_enabled"
    private const val KEY_BG_URI = "home_bg_uri"
    private const val KEY_BG_ALPHA = "home_bg_alpha"
    private const val KEY_BG_BLUR = "home_bg_blur"
    private const val KEY_HOME_TITLE = "home_title"
    private const val KEY_HOME_SUBTITLE = "home_subtitle"
    private const val KEY_COMMUNITY_ENABLED = "home_community_enabled"

    const val DEFAULT_TITLE = "NorPatch"
    const val DEFAULT_SUBTITLE = ""
    const val DEFAULT_ALPHA = 60
    const val DEFAULT_BLUR = 25

    const val DEFAULT_BLOCK_ALPHA = 100
    const val DEFAULT_BLOCK_BLUR = 22
    const val DEFAULT_BLOCK_COLOR = 0xFFFFFFFF
    const val DEFAULT_CUSTOM_TEXT = ""

    // Window-style block appearance defaults
    const val DEFAULT_BLOCK_CORNER = 22f
    const val DEFAULT_BLOCK_BORDER = false
    const val DEFAULT_BLOCK_BORDER_COLOR = 0xFFE8DDE8
    const val DEFAULT_BLOCK_SHADOW = true
    const val DEFAULT_BLOCK_SHADOW_STRENGTH = 12
    const val DEFAULT_BLOCK_WIDTH_RATIO = 1f
    const val DEFAULT_BLOCK_SPACING = 14

    /** Named layout profiles that store each block's visibility + order. */
    val LAYOUT_SLOTS = listOf("default", "layout1", "layout2")

    private const val KEY_LAYOUT_PROFILE = "layout_profile"
    private const val KEY_BLOCK_ORDER = "block_order"

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
    var homeSubtitle by mutableStateOf(DEFAULT_SUBTITLE)
        private set
    private var communityEnabledInternal by mutableStateOf(true)
    val communityEnabled: Boolean get() = communityEnabledInternal

    // ---- Per-block configuration (each homepage block) ----
    val blockEnabled = mutableStateMapOf<HomeBlock, Boolean>()
    val blockBgType = mutableStateMapOf<HomeBlock, BlockBgType>()
    val blockBgPath = mutableStateMapOf<HomeBlock, String>()
    val blockBgColor = mutableStateMapOf<HomeBlock, Long>()
    val blockAlpha = mutableStateMapOf<HomeBlock, Int>()
    val blockBlur = mutableStateMapOf<HomeBlock, Int>()

    // ---- Window-style per-block appearance ----
    val blockCorner = mutableStateMapOf<HomeBlock, Float>()
    val blockBorderEnabled = mutableStateMapOf<HomeBlock, Boolean>()
    val blockBorderColor = mutableStateMapOf<HomeBlock, Long>()
    val blockShadowEnabled = mutableStateMapOf<HomeBlock, Boolean>()
    val blockShadowStrength = mutableStateMapOf<HomeBlock, Int>()
    val blockWidthRatio = mutableStateMapOf<HomeBlock, Float>()
    val blockSpacing = mutableStateMapOf<HomeBlock, Int>()

    // ---- Layout: block order + active layout profile ----
    var layoutProfile by mutableStateOf(LAYOUT_SLOTS[0])
        private set
    val blockOrder = androidx.compose.runtime.mutableStateListOf<HomeBlock>()

    // ---- Custom block content (one display type at a time) ----
    // Internal mutable backing fields (their JVM setters are private), exposed
    // through read-only properties plus explicit setter functions below to
    // avoid a platform declaration clash.
    private var customContentTypeInternal by mutableStateOf(CustomContentType.DEVICE_NAME)
    private var customContentImagePathInternal by mutableStateOf("")
    private var customContentTextInternal by mutableStateOf(DEFAULT_CUSTOM_TEXT)

    val customContentType: CustomContentType get() = customContentTypeInternal
    val customContentImagePath: String get() = customContentImagePathInternal
    val customContentText: String get() = customContentTextInternal

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
        homeSubtitle = prefs.getString(KEY_HOME_SUBTITLE, DEFAULT_SUBTITLE) ?: DEFAULT_SUBTITLE
        communityEnabledInternal = prefs.getBoolean(KEY_COMMUNITY_ENABLED, true)
        HomeBlock.entries.forEach { b ->
            blockEnabled[b] = prefs.getBoolean("blk_${b.key}_enabled", true)
            blockBgType[b] = BlockBgType.fromKey(prefs.getString("blk_${b.key}_bgtype", BlockBgType.SOLID.key))
            blockBgPath[b] = prefs.getString("blk_${b.key}_bgpath", "") ?: ""
            blockBgColor[b] = prefs.getLong("blk_${b.key}_color", DEFAULT_BLOCK_COLOR)
            blockAlpha[b] = prefs.getInt("blk_${b.key}_alpha", DEFAULT_BLOCK_ALPHA)
            blockBlur[b] = prefs.getInt("blk_${b.key}_blur", DEFAULT_BLOCK_BLUR)
            blockCorner[b] = prefs.getFloat("blk_${b.key}_corner", DEFAULT_BLOCK_CORNER)
            blockBorderEnabled[b] = prefs.getBoolean("blk_${b.key}_border", DEFAULT_BLOCK_BORDER)
            blockBorderColor[b] = prefs.getLong("blk_${b.key}_bordercolor", DEFAULT_BLOCK_BORDER_COLOR)
            blockShadowEnabled[b] = prefs.getBoolean("blk_${b.key}_shadow", DEFAULT_BLOCK_SHADOW)
            blockShadowStrength[b] = prefs.getInt("blk_${b.key}_shadowstrength", DEFAULT_BLOCK_SHADOW_STRENGTH)
            blockWidthRatio[b] = prefs.getFloat("blk_${b.key}_widthratio", DEFAULT_BLOCK_WIDTH_RATIO)
            blockSpacing[b] = prefs.getInt("blk_${b.key}_spacing", DEFAULT_BLOCK_SPACING)
        }
        layoutProfile = prefs.getString(KEY_LAYOUT_PROFILE, LAYOUT_SLOTS[0]) ?: LAYOUT_SLOTS[0]
        blockOrder.clear()
        val orderStr = prefs.getString(KEY_BLOCK_ORDER, "") ?: ""
        if (orderStr.isNotBlank()) {
            orderStr.split(",").mapNotNull { key -> HomeBlock.entries.firstOrNull { it.key == key } }
                .forEach { blockOrder.add(it) }
        }
        HomeBlock.entries.forEach { if (it !in blockOrder) blockOrder.add(it) }
        customContentTypeInternal = CustomContentType.fromKey(
            prefs.getString("home_custom_content_type", CustomContentType.DEVICE_NAME.key)
        )
        customContentImagePathInternal = prefs.getString("home_custom_content_image", "") ?: ""
        customContentTextInternal = prefs.getString("home_custom_content_text", DEFAULT_CUSTOM_TEXT) ?: DEFAULT_CUSTOM_TEXT
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

    fun setSubtitle(subtitle: String) {
        if (homeSubtitle == subtitle) return
        homeSubtitle = subtitle
        prefs.edit { putString(KEY_HOME_SUBTITLE, subtitle) }
    }

    /** Show/hide the "Join community" section on the More page. */
    fun setCommunityEnabled(enabled: Boolean) {
        if (communityEnabledInternal == enabled) return
        communityEnabledInternal = enabled
        prefs.edit { putBoolean(KEY_COMMUNITY_ENABLED, enabled) }
    }

    // ---- Per-block setters ----

    fun setBlockEnabled(block: HomeBlock, enabled: Boolean) {
        if (blockEnabled[block] == enabled) return
        blockEnabled[block] = enabled
        prefs.edit { putBoolean("blk_${block.key}_enabled", enabled) }
    }

    fun setBlockBgType(block: HomeBlock, type: BlockBgType) {
        if (blockBgType[block] == type) return
        blockBgType[block] = type
        prefs.edit { putString("blk_${block.key}_bgtype", type.key) }
    }

    /** Path of the block background image; pass "" to clear (also deletes the file). */
    fun setBlockBgPath(block: HomeBlock, path: String) {
        val old = blockBgPath[block]
        if (!old.isNullOrBlank() && old != path) {
            runCatching { File(old).delete() }
        }
        blockBgPath[block] = path
        prefs.edit { putString("blk_${block.key}_bgpath", path) }
    }

    fun setBlockColor(block: HomeBlock, color: Long) {
        if (blockBgColor[block] == color) return
        blockBgColor[block] = color
        prefs.edit { putLong("blk_${block.key}_color", color) }
    }

    fun setBlockAlpha(block: HomeBlock, alpha: Int) {
        val v = alpha.coerceIn(0, 100)
        if (blockAlpha[block] == v) return
        blockAlpha[block] = v
        prefs.edit { putInt("blk_${block.key}_alpha", v) }
    }

    fun setBlockBlur(block: HomeBlock, blur: Int) {
        val v = blur.coerceIn(0, 100)
        if (blockBlur[block] == v) return
        blockBlur[block] = v
        prefs.edit { putInt("blk_${block.key}_blur", v) }
    }

    // ---- Window-style appearance setters ----

    fun setBlockCorner(block: HomeBlock, corner: Float) {
        val v = corner.coerceIn(8f, 48f)
        if (blockCorner[block] == v) return
        blockCorner[block] = v
        prefs.edit { putFloat("blk_${block.key}_corner", v) }
    }

    fun setBlockBorderEnabled(block: HomeBlock, enabled: Boolean) {
        if (blockBorderEnabled[block] == enabled) return
        blockBorderEnabled[block] = enabled
        prefs.edit { putBoolean("blk_${block.key}_border", enabled) }
    }

    fun setBlockBorderColor(block: HomeBlock, color: Long) {
        if (blockBorderColor[block] == color) return
        blockBorderColor[block] = color
        prefs.edit { putLong("blk_${block.key}_bordercolor", color) }
    }

    fun setBlockShadowEnabled(block: HomeBlock, enabled: Boolean) {
        if (blockShadowEnabled[block] == enabled) return
        blockShadowEnabled[block] = enabled
        prefs.edit { putBoolean("blk_${block.key}_shadow", enabled) }
    }

    fun setBlockShadowStrength(block: HomeBlock, strength: Int) {
        val v = strength.coerceIn(0, 40)
        if (blockShadowStrength[block] == v) return
        blockShadowStrength[block] = v
        prefs.edit { putInt("blk_${block.key}_shadowstrength", v) }
    }

    fun setBlockWidthRatio(block: HomeBlock, ratio: Float) {
        val v = ratio.coerceIn(0.6f, 1f)
        if (blockWidthRatio[block] == v) return
        blockWidthRatio[block] = v
        prefs.edit { putFloat("blk_${block.key}_widthratio", v) }
    }

    fun setBlockSpacing(block: HomeBlock, spacing: Int) {
        val v = spacing.coerceIn(0, 48)
        if (blockSpacing[block] == v) return
        blockSpacing[block] = v
        prefs.edit { putInt("blk_${block.key}_spacing", v) }
    }

    fun blockCornerDp(block: HomeBlock): Dp = (blockCorner[block] ?: DEFAULT_BLOCK_CORNER).dp

    // ---- Layout order & profiles ----

    private fun saveBlockOrder() {
        prefs.edit { putString(KEY_BLOCK_ORDER, blockOrder.joinToString(",") { it.key }) }
    }

    /** Moves [block] to [toIndex] within the visible order and persists it. */
    fun moveBlock(block: HomeBlock, toIndex: Int) {
        val from = blockOrder.indexOf(block)
        if (from < 0 || toIndex < 0 || toIndex >= blockOrder.size || from == toIndex) return
        blockOrder.removeAt(from)
        blockOrder.add(toIndex, block)
        saveBlockOrder()
    }

    fun resetBlockOrder() {
        blockOrder.clear()
        blockOrder.addAll(HomeBlock.entries)
        saveBlockOrder()
    }

    /** Restores the whole layout: default order + every card enabled. */
    fun resetLayoutOnly() {
        HomeBlock.entries.forEach { setBlockEnabled(it, true) }
        resetBlockOrder()
    }

    /** Resets every card's appearance + layout to defaults in one shot. */
    fun resetAllBlockStyles() {
        HomeBlock.entries.forEach { resetBlock(it) }
        resetLayoutOnly()
    }

    private fun saveLayoutSnapshot(name: String) {
        val e = HomeBlock.entries.joinToString(",") {
            "${it.key}=${if (blockEnabled[it] == false) 0 else 1}"
        }
        val o = blockOrder.joinToString(",") { it.key }
        prefs.edit {
            putString("layout_${name}_enabled", e)
            putString("layout_${name}_order", o)
        }
    }

    private fun loadLayoutSnapshot(name: String) {
        val e = prefs.getString("layout_${name}_enabled", "") ?: ""
        val o = prefs.getString("layout_${name}_order", "") ?: ""
        val enabledMap = e.split(",").mapNotNull { part ->
            val kv = part.split("=")
            if (kv.size == 2) kv[0] to (kv[1] == "1") else null
        }.toMap()
        HomeBlock.entries.forEach { b -> blockEnabled[b] = enabledMap[b.key] ?: true }
        val newOrder = o.split(",").mapNotNull { key -> HomeBlock.entries.firstOrNull { it.key == key } }
        blockOrder.clear()
        blockOrder.addAll(newOrder.ifEmpty { HomeBlock.entries.toList() })
        HomeBlock.entries.forEach { if (it !in blockOrder) blockOrder.add(it) }
    }

    /** Switches to another layout profile, saving the current one first. */
    fun switchLayoutProfile(name: String) {
        if (name !in LAYOUT_SLOTS || layoutProfile == name) return
        saveLayoutSnapshot(layoutProfile)
        layoutProfile = name
        loadLayoutSnapshot(name)
        prefs.edit { putString(KEY_LAYOUT_PROFILE, name) }
    }

    fun blockFile(block: HomeBlock): File? {
        val path = blockBgPath[block] ?: return null
        return if (path.isBlank()) null else runCatching { File(path) }.getOrNull()?.takeIf { it.exists() }
    }

    fun blockAlphaFloat(block: HomeBlock): Float = (blockAlpha[block] ?: DEFAULT_BLOCK_ALPHA) / 100f

    /** Maps the 0..100 slider to a 0..36dp blur radius for a block. */
    fun blockBlurRadiusDp(block: HomeBlock): Dp = ((blockBlur[block] ?: DEFAULT_BLOCK_BLUR) / 100f * 36f).dp

    // ---- Custom block content setters ----

    fun setCustomContentType(type: CustomContentType) {
        if (customContentTypeInternal == type) return
        customContentTypeInternal = type
        prefs.edit { putString("home_custom_content_type", type.key) }
    }

    /** Path of the content image shown by the custom block's IMAGE type. */
    fun setCustomContentImagePath(path: String) {
        if (customContentImagePathInternal.isNotBlank() && customContentImagePathInternal != path) {
            runCatching { File(customContentImagePathInternal).delete() }
        }
        customContentImagePathInternal = path
        prefs.edit { putString("home_custom_content_image", path) }
    }

    fun setCustomContentText(text: String) {
        if (customContentTextInternal == text) return
        customContentTextInternal = text
        prefs.edit { putString("home_custom_content_text", text) }
    }

    fun customContentFile(): File? {
        val path = customContentImagePath
        return if (path.isBlank()) null else runCatching { File(path) }.getOrNull()?.takeIf { it.exists() }
    }

    fun backgroundFile(): File? {
        val path = bgImagePath
        return if (path.isBlank()) null
        else runCatching { File(path) }.getOrNull()?.takeIf { it.exists() }
    }

    /** Restores one block (and its custom content, if any) to defaults. */
    fun resetBlock(block: HomeBlock) {
        setBlockEnabled(block, true)
        setBlockBgType(block, BlockBgType.SOLID)
        setBlockBgPath(block, "")
        setBlockColor(block, DEFAULT_BLOCK_COLOR)
        setBlockAlpha(block, DEFAULT_BLOCK_ALPHA)
        setBlockBlur(block, DEFAULT_BLOCK_BLUR)
        setBlockCorner(block, DEFAULT_BLOCK_CORNER)
        setBlockBorderEnabled(block, DEFAULT_BLOCK_BORDER)
        setBlockBorderColor(block, DEFAULT_BLOCK_BORDER_COLOR)
        setBlockShadowEnabled(block, DEFAULT_BLOCK_SHADOW)
        setBlockShadowStrength(block, DEFAULT_BLOCK_SHADOW_STRENGTH)
        setBlockWidthRatio(block, DEFAULT_BLOCK_WIDTH_RATIO)
        setBlockSpacing(block, DEFAULT_BLOCK_SPACING)
        if (block == HomeBlock.CUSTOM) {
            setCustomContentType(CustomContentType.DEVICE_NAME)
            setCustomContentImagePath("")
            setCustomContentText(DEFAULT_CUSTOM_TEXT)
        }
    }

    fun reset() {
        setBackgroundEnabled(true)
        setBackgroundImagePath("")
        setAlpha(DEFAULT_ALPHA)
        setBlur(DEFAULT_BLUR)
        setTitle(DEFAULT_TITLE)
        setSubtitle(DEFAULT_SUBTITLE)
        setCommunityEnabled(true)
        HomeBlock.entries.forEach { b -> resetBlock(b) }
        resetBlockOrder()
        layoutProfile = LAYOUT_SLOTS[0]
        prefs.edit { putString(KEY_LAYOUT_PROFILE, LAYOUT_SLOTS[0]) }
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
 * A translucent "婵犳鍣徊鍓х矙閹捐崵宓侀柟鍓х帛閸? card: semi-transparent white (light) / plum (dark)
 * fill with a soft vertical sheen, a thin highlight border and large rounded
 * corners 闂?the FolkPatch M3E look. Pressing animates a gentle scale.
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
    val base = MaterialTheme.colorScheme.surface
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

    Box(
        modifier = cardModifier.graphicsLayer {
            alpha = BackgroundConfig.cardOpacity
        },
    ) {
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Column(modifier = Modifier.padding(contentPadding)) { content() }
        }
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
    val context = LocalContext.current
    remember { BackgroundConfig.load(context) }
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val animatedAlpha by animateFloatAsState(
        targetValue = BackgroundConfig.customBackgroundOpacity,
        animationSpec = tween(480),
        label = "bgAlpha",
    )
    val animatedDim by animateFloatAsState(
        targetValue = BackgroundConfig.customBackgroundDim,
        animationSpec = tween(480),
        label = "bgDim",
    )
    val targetBlur = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (BackgroundConfig.customBackgroundBlur * 60f).dp
    } else {
        0.dp
    }
    val animatedBlur by animateDpAsState(
        targetValue = targetBlur,
        animationSpec = tween(480),
        label = "bgBlur",
    )

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        // Layer 0: flat light page color 闂?never black, fills the whole screen.
        Box(
            Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF17131B) else Color(0xFFEEF2F7))
        )
        val uri = BackgroundConfig.customBackgroundUri
        if (BackgroundConfig.isCustomBackgroundEnabled && uri != null) {
            // Layer 1: background image 闂?full-screen and overscanned 1.2x
            // (zoomed by ContentScale.Crop) so the blurred halo stays outside
            // the visible area: no dark edges / black ring around the screen.
            Box(
                Modifier
                    .fillMaxSize()
                    .scale(1.2f)
            ) {
                AsyncImage(
                    model = uri,
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
                        ),
                )
            }
            // Layer 2: white translucent veil. The global transparency slider
            // only affects this layer 闂?the image underneath stays fully opaque,
            // and cards / text never participate in the veil (no black tint).
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = (1f - animatedAlpha) * 0.65f))
            )
            // Layer 3: optional dim layer for readability.
            if (animatedDim > 0.01f) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = animatedDim))
                )
            }
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

// ---------------------------------------------------------------------------
// Per-block frosted card with its own background
// ---------------------------------------------------------------------------

/**
 * Draws a single block's background: either the user-picked image or a solid
 * color, with smooth animated alpha and blur. Blur is gated to Android 12+
 * (GPU RenderEffect), matching the global background behavior.
 */
@Composable
fun BlockBackground(block: HomeBlock, modifier: Modifier = Modifier) {
    HomePrefs.ensureInit()
    val alpha by animateFloatAsState(
        targetValue = HomePrefs.blockAlphaFloat(block),
        animationSpec = tween(420),
        label = "blockAlpha",
    )
    val targetBlur = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        HomePrefs.blockBlurRadiusDp(block)
    } else {
        0.dp
    }
    val blur by animateDpAsState(
        targetValue = targetBlur,
        animationSpec = tween(420),
        label = "blockBlur",
    )
    when (HomePrefs.blockBgType[block] ?: BlockBgType.SOLID) {
        BlockBgType.IMAGE -> {
            val file = HomePrefs.blockFile(block)
            if (file != null) {
                // Overscan + crop: keeps the blurred image edge (dark halo)
                // outside the visible block area, so rounded corners stay clean.
                Box(
                    modifier.clipToBounds()
                ) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .scale(1.2f)
                    ) {
                        AsyncImage(
                            model = file,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        Modifier.blur(blur)
                                    } else {
                                        Modifier
                                    }
                                )
                                .alpha(alpha),
                        )
                    }
                }
            } else {
                Box(
                    modifier.background(Color(0xFFFFFFFF))
                )
            }
        }

        BlockBgType.SOLID -> {
            // Frosted-glass feel over custom backgrounds: semi-opaque white lets the
            // (already blurred) custom background show through instead of a flat slab.
            // Unified frosted glass across every screen: driven by the single global
            // background-opacity slider so tweaking it morphs home cards and all
            // other pages (settings/superuser/module lists) together.
            val frostedAlpha = if (BackgroundConfig.isCustomBackgroundEnabled) {
                BackgroundConfig.customBackgroundOpacity.coerceIn(0.85f, 1.0f)
            } else {
                alpha
            }
            Box(
                modifier.background(
                    MaterialTheme.colorScheme.surfaceContainer
                        .copy(alpha = frostedAlpha)
                )
            )
        }
    }
}

/**
 * Frosted block card: the block's own background plus a light surface sheen
 * that keeps text readable over bright images, all inside the shared large
 * rounded shape. In window mode it gains a unified title bar (name, drag
 * handle, settings + close buttons) and honors the block's corner radius,
 * border, shadow, width and spacing settings. Pure UI layer.
 */
@Composable
fun BlockCard(
    block: HomeBlock,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    windowMode: Boolean = false,
    onSettingsClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    HomePrefs.ensureInit()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val corner = HomePrefs.blockCornerDp(block)
    val shape = RoundedCornerShape(corner)
    val borderEnabled = HomePrefs.blockBorderEnabled[block] ?: HomePrefs.DEFAULT_BLOCK_BORDER
    val borderColor = Color(HomePrefs.blockBorderColor[block] ?: HomePrefs.DEFAULT_BLOCK_BORDER_COLOR)
    val shadowEnabled = HomePrefs.blockShadowEnabled[block] ?: HomePrefs.DEFAULT_BLOCK_SHADOW
    val shadowStrength = (HomePrefs.blockShadowStrength[block] ?: HomePrefs.DEFAULT_BLOCK_SHADOW_STRENGTH).dp

    val cardModifier = modifier
        .then(if (shadowEnabled) Modifier.shadow(shadowStrength, shape, clip = false) else Modifier)
        .then(if (borderEnabled) Modifier.border(1.dp, borderColor, shape) else Modifier)
        .clip(shape)

    Box(
        modifier = cardModifier.graphicsLayer {
            alpha = BackgroundConfig.cardOpacity
        },
    ) {
        BlockBackground(block, Modifier.matchParentSize())
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                            Color.Transparent,
                        )
                    )
                )
        )
        Column(Modifier.padding(contentPadding)) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
                if (windowMode) {
                    BlockWindowTitleBar(
                        block = block,
                        onSettingsClick = onSettingsClick,
                    )
                    Spacer(Modifier.height(10.dp))
                }
                content()
            }
        }
    }
}

/** Display name resource of a homepage block. */
fun blockTitleRes(block: HomeBlock): Int = when (block) {
    HomeBlock.TITLE -> R.string.home_block_title
    HomeBlock.STATUS -> R.string.home_block_status
    HomeBlock.CUSTOM -> R.string.home_block_custom
    HomeBlock.DEVICE -> R.string.home_block_device
}

/** Unified window title bar: name (left) + settings gear (right), fixed height. */
@Composable
private fun BlockWindowTitleBar(
    block: HomeBlock,
    onSettingsClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(blockTitleRes(block)),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (onSettingsClick != null) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.home_window_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

/** Soft pastel palette shared by block backgrounds and content colors.
 *  Light tints only 闂?no black / dark-gray content layer colors. */
val BlockPalette = listOf(
    0xFFFDF1F5, 0xFFFFF3E3, 0xFFE8F6EC, 0xFFE8F1FF,
    0xFFF3E8FF, 0xFFFFEBF1, 0xFFEAF7F7,
)

/** Copies a picked image into the app files dir and returns its absolute path. */
fun importImageToFiles(context: android.content.Context, uri: android.net.Uri, prefix: String): String? {
    return runCatching {
        val dest = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { out -> input.copyTo(out) }
        }
        dest.absolutePath
    }.getOrNull()
}

/** Label row helper used inside the block settings sheet. */
@Composable
private fun SettingSliderRow(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    Column(Modifier.padding(top = 4.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
        )
    }
}

/** Color swatch row used for block background / border colors. */
@Composable
private fun ColorSwatchRow(
    selected: Long,
    onPick: (Long) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BlockPalette.forEach { color ->
            val c = Color(color)
            val isSelected = selected == color
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(c)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        } else {
                            Modifier.border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
                        }
                    )
                    .clickable { onPick(color) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = if (c.luminance() < 0.5f) Color.White else Color(0xFF5A4050),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Bottom-sheet panel to tune one block card in real time: visibility, alpha,
 * background (image / solid + palette), corner radius, border, shadow, width
 * ratio, vertical spacing and a "restore defaults" action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockSettingsSheet(
    block: HomeBlock,
    onDismiss: () -> Unit,
    imagePickerLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, android.net.Uri?>,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(blockTitleRes(block)),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, stringResource(R.string.close))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Alpha
            SettingSliderRow(
                label = stringResource(R.string.home_block_alpha),
                value = HomePrefs.blockAlpha[block] ?: HomePrefs.DEFAULT_BLOCK_ALPHA,
                range = 0..100,
                onChange = { HomePrefs.setBlockAlpha(block, it) },
            )

            // Background type
            Text(
                text = stringResource(R.string.home_block_bg_type),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = (HomePrefs.blockBgType[block] ?: BlockBgType.SOLID) == BlockBgType.SOLID,
                    onClick = { HomePrefs.setBlockBgType(block, BlockBgType.SOLID) },
                    label = { Text(stringResource(R.string.home_block_bg_solid)) },
                )
                FilterChip(
                    selected = (HomePrefs.blockBgType[block] ?: BlockBgType.SOLID) == BlockBgType.IMAGE,
                    onClick = { HomePrefs.setBlockBgType(block, BlockBgType.IMAGE) },
                    label = { Text(stringResource(R.string.home_block_bg_image)) },
                )
            }

            if ((HomePrefs.blockBgType[block] ?: BlockBgType.SOLID) == BlockBgType.IMAGE) {
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Image, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.home_block_bg_pick))
                }
            } else {
                Spacer(Modifier.height(8.dp))
                ColorSwatchRow(
                    selected = HomePrefs.blockBgColor[block] ?: HomePrefs.DEFAULT_BLOCK_COLOR,
                    onPick = { HomePrefs.setBlockColor(block, it) },
                )
            }

            // Corner radius
            SettingSliderRow(
                label = stringResource(R.string.home_block_corner),
                value = (HomePrefs.blockCorner[block] ?: HomePrefs.DEFAULT_BLOCK_CORNER).roundToInt(),
                range = 8..48,
                onChange = { HomePrefs.setBlockCorner(block, it.toFloat()) },
            )

            // Border
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_block_border),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = HomePrefs.blockBorderEnabled[block] ?: HomePrefs.DEFAULT_BLOCK_BORDER,
                    onCheckedChange = { HomePrefs.setBlockBorderEnabled(block, it) },
                )
            }
            if (HomePrefs.blockBorderEnabled[block] == true) {
                Spacer(Modifier.height(4.dp))
                ColorSwatchRow(
                    selected = HomePrefs.blockBorderColor[block] ?: HomePrefs.DEFAULT_BLOCK_BORDER_COLOR,
                    onPick = { HomePrefs.setBlockBorderColor(block, it) },
                )
            }

            // Shadow
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_block_shadow),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = HomePrefs.blockShadowEnabled[block] ?: HomePrefs.DEFAULT_BLOCK_SHADOW,
                    onCheckedChange = { HomePrefs.setBlockShadowEnabled(block, it) },
                )
            }
            if (HomePrefs.blockShadowEnabled[block] == true) {
                SettingSliderRow(
                    label = stringResource(R.string.home_block_shadow_strength),
                    value = HomePrefs.blockShadowStrength[block] ?: HomePrefs.DEFAULT_BLOCK_SHADOW_STRENGTH,
                    range = 0..40,
                    onChange = { HomePrefs.setBlockShadowStrength(block, it) },
                )
            }

            // Width ratio
            SettingSliderRow(
                label = stringResource(R.string.home_block_width),
                value = ((HomePrefs.blockWidthRatio[block] ?: HomePrefs.DEFAULT_BLOCK_WIDTH_RATIO) * 100).roundToInt(),
                range = 60..100,
                onChange = { HomePrefs.setBlockWidthRatio(block, it / 100f) },
            )

            // Vertical spacing
            SettingSliderRow(
                label = stringResource(R.string.home_block_spacing),
                value = HomePrefs.blockSpacing[block] ?: HomePrefs.DEFAULT_BLOCK_SPACING,
                range = 0..48,
                onChange = { HomePrefs.setBlockSpacing(block, it) },
            )

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { HomePrefs.resetBlock(block) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_block_reset))
            }
        }
    }
}

/**
 * Rounded-card settings group with an optional group title, used across the
 * Settings and Home Appearance screens for a unified grouped look.
 */
@Composable
fun SectionCard(
    title: String,
    summary: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (summary != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
        Spacer(Modifier.height(8.dp))
        GlassCard(fillAlpha = 0.80f) {
            Column { content() }
        }
    }
}
