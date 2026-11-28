package me.bmax.apatch.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.BlockConfigScreenDestination
import com.ramcosta.composedestinations.generated.destinations.GlobalBackgroundScreenDestination
import com.ramcosta.composedestinations.generated.destinations.CardAppearanceScreenDestination
import com.ramcosta.composedestinations.generated.destinations.AnimationScreenDestination
import com.ramcosta.composedestinations.generated.destinations.GriduCardScreenDestination
import com.ramcosta.composedestinations.generated.destinations.DashboardCardScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.CardAnimations
import me.bmax.apatch.ui.component.HomeBlock
import me.bmax.apatch.ui.component.HomePrefs
import me.bmax.apatch.ui.component.SectionCard
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.theme.BackgroundConfig
import me.bmax.apatch.ui.theme.BackgroundManager
import me.bmax.apatch.ui.theme.refreshTheme
import me.bmax.apatch.util.ui.NavigationBarsSpacer

/** Shared greeting logic used by the homepage and the appearance preview. */
internal fun greetingForHour(hour: Int, morning: String, afternoon: String, evening: String): String =
    when (hour) {
        in 5..11 -> morning
        in 12..17 -> afternoon
        else -> evening
    }

@StringRes
internal fun HomeBlock.labelRes(): Int = when (this) {
    HomeBlock.TITLE -> R.string.home_block_title
    HomeBlock.STATUS -> R.string.home_block_status
    HomeBlock.CUSTOM -> R.string.home_block_custom
    HomeBlock.DEVICE -> R.string.home_block_device
}

internal fun homeBlockIcon(block: HomeBlock): ImageVector = when (block) {
    HomeBlock.TITLE -> Icons.Filled.Title
    HomeBlock.STATUS -> Icons.Filled.CheckCircle
    HomeBlock.CUSTOM -> Icons.Filled.Image
    HomeBlock.DEVICE -> Icons.Filled.PhoneAndroid
}

// ---------------------------------------------------------------------------
// Home Appearance — split into a menu of categorized sub-pages.
// ---------------------------------------------------------------------------

/** Sub-page scaffold: top bar with back button + scrollable content column. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSubPage(
    navigator: DestinationsNavigator,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(android.R.string.cancel),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            content()
            NavigationBarsSpacer()
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Menu entry row on the Home Appearance index page. */
@Composable
private fun AppearanceMenuRow(
    icon: ImageVector,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(
                alpha = BackgroundConfig.cardGlassAlpha,
            ),
        ),
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        },
        supportingContent = {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(BackgroundConfig.cardCornerRadius.dp))
            .clickable(onClick = onClick),
    )
}

// ---------------------------------------------------------------------------
// Index page: categorized menu
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppearanceScreen(navigator: DestinationsNavigator) {
    HomePrefs.ensureInit()
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_appearance)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(android.R.string.cancel),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            AppearanceMenuRow(
                icon = Icons.Filled.CheckCircle,
                title = stringResource(R.string.home_blocks_config),
                summary = stringResource(R.string.home_appearance_menu_blocks_summary),
                onClick = { navigator.navigate(BlockConfigScreenDestination) },
            )
            AppearanceMenuRow(
                icon = Icons.Filled.Wallpaper,
                title = stringResource(R.string.home_appearance_section_background),
                summary = stringResource(R.string.home_appearance_menu_background_summary),
                onClick = { navigator.navigate(GlobalBackgroundScreenDestination) },
            )
            AppearanceMenuRow(
                icon = Icons.Filled.Style,
                title = stringResource(R.string.home_appearance_section_card),
                summary = stringResource(R.string.home_appearance_menu_card_summary),
                onClick = { navigator.navigate(CardAppearanceScreenDestination) },
            )
            AppearanceMenuRow(
                icon = Icons.Filled.BlurOn,
                title = stringResource(R.string.home_animation_preset),
                summary = stringResource(R.string.home_appearance_menu_animation_summary),
                onClick = { navigator.navigate(AnimationScreenDestination) },
            )
            AppearanceMenuRow(
                icon = Icons.Filled.GridView,
                title = stringResource(R.string.home_appearance_grid_card),
                summary = stringResource(R.string.home_appearance_menu_grid_card_summary),
                onClick = { navigator.navigate(GriduCardScreenDestination) },
            )
            AppearanceMenuRow(
                icon = Icons.Filled.Dashboard,
                title = stringResource(R.string.home_appearance_dashboard_card),
                summary = stringResource(R.string.home_appearance_menu_dashboard_card_summary),
                onClick = { navigator.navigate(DashboardCardScreenDestination) },
            )
            NavigationBarsSpacer()
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-page: block visibility
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockConfigScreen(navigator: DestinationsNavigator) {
    HomePrefs.ensureInit()
    AppearanceSubPage(navigator, stringResource(R.string.home_blocks_config)) {
        SectionCard(title = stringResource(R.string.home_blocks_config)) {
            SwitchItem(
                icon = Icons.Filled.CheckCircle,
                title = stringResource(R.string.home_about_norpatch_title),
                summary = stringResource(R.string.home_about_card_show),
                checked = HomePrefs.blockEnabled[HomeBlock.TITLE] ?: true,
                onCheckedChange = { HomePrefs.setBlockEnabled(HomeBlock.TITLE, it) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            SwitchItem(
                icon = Icons.Filled.Forum,
                title = stringResource(R.string.home_community_entry),
                summary = stringResource(R.string.home_community_entry_summary),
                checked = HomePrefs.communityEnabled,
                onCheckedChange = { HomePrefs.setCommunityEnabled(it) },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-page: global background
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalBackgroundScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val pickBackgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = BackgroundManager.saveAndApplyCustomBackground(context, uri)
                toast(context.getString(if (ok) R.string.home_bg_saved else R.string.home_bg_error))
            }
        }
    }

    AppearanceSubPage(navigator, stringResource(R.string.home_appearance_section_background)) {
        SectionCard(title = stringResource(R.string.home_appearance_section_background)) {
            SwitchItem(
                icon = Icons.Filled.Wallpaper,
                title = stringResource(R.string.home_bg_enabled),
                summary = stringResource(R.string.home_bg_enabled_summary),
                checked = BackgroundConfig.isCustomBackgroundEnabled,
                onCheckedChange = {
                    BackgroundConfig.setCustomBackgroundEnabledState(it)
                    BackgroundConfig.save(context)
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.primary) },
                headlineContent = { Text(stringResource(R.string.home_bg_pick)) },
                supportingContent = { Text(stringResource(R.string.home_bg_pick_summary)) },
                trailingContent = {
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickBackgroundLauncher.launch("image/*") },
            )
            if (BackgroundConfig.customBackgroundUri != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(Icons.Filled.Delete, null) },
                    headlineContent = { Text(stringResource(R.string.home_bg_remove)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { BackgroundManager.clearCustomBackground(context) },
                )
            }
            FpSliderItem(
                title = stringResource(R.string.home_bg_alpha),
                value = BackgroundConfig.customBackgroundOpacity,
                onValueChange = {
                    BackgroundConfig.setCustomBackgroundOpacityValue(it)
                    BackgroundConfig.save(context)
                },
            )
            FpSliderItem(
                title = stringResource(R.string.home_bg_blur),
                value = BackgroundConfig.customBackgroundBlur,
                onValueChange = {
                    BackgroundConfig.setCustomBackgroundBlurValue(it)
                    BackgroundConfig.save(context)
                },
            )
            FpSliderItem(
                title = stringResource(R.string.home_appearance_dim),
                value = BackgroundConfig.customBackgroundDim,
                onValueChange = {
                    BackgroundConfig.setCustomBackgroundDimValue(it)
                    BackgroundConfig.save(context)
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-page: card appearance
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardAppearanceScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    AppearanceSubPage(navigator, stringResource(R.string.home_appearance_section_card)) {
        SectionCard(title = stringResource(R.string.home_appearance_section_card)) {
            FpSliderItem(
                title = stringResource(R.string.home_card_glass_alpha),
                value = BackgroundConfig.cardGlassAlpha,
                valueRange = 0.15f..0.95f,
                onValueChange = {
                    BackgroundConfig.setCardGlassAlphaValue(it)
                    BackgroundConfig.save(context)
                },
            )
            FpSliderItem(
                title = stringResource(R.string.home_card_opacity),
                value = BackgroundConfig.cardOpacity,
                onValueChange = {
                    BackgroundConfig.setCardOpacityValue(it)
                    BackgroundConfig.save(context)
                },
            )
            FpSliderItem(
                title = stringResource(R.string.home_card_corner),
                value = BackgroundConfig.cardCornerRadius / 40f,
                onValueChange = {
                    BackgroundConfig.setCardCornerRadiusValue(it * 40f)
                    BackgroundConfig.save(context)
                },
            )
            SwitchItem(
                icon = Icons.Filled.BorderStyle,
                title = stringResource(R.string.home_card_border),
                summary = stringResource(R.string.home_card_border_summary),
                checked = BackgroundConfig.cardBorderEnabled,
                onCheckedChange = {
                    BackgroundConfig.setCardBorderEnabledState(it)
                    BackgroundConfig.save(context)
                },
            )
            SwitchItem(
                icon = Icons.Filled.BlurOn,
                title = stringResource(R.string.home_card_shadow),
                summary = stringResource(R.string.home_card_shadow_summary),
                checked = BackgroundConfig.cardShadowEnabled,
                onCheckedChange = {
                    BackgroundConfig.setCardShadowEnabledState(it)
                    BackgroundConfig.save(context)
                },
            )
            FpSliderItem(
                title = stringResource(R.string.home_card_padding),
                value = (BackgroundConfig.cardPaddingScale - 0.5f),
                onValueChange = {
                    BackgroundConfig.setCardPaddingScaleValue(0.5f + it)
                    BackgroundConfig.save(context)
                },
            )
        }

        // ---- One-tap reset (scoped to this category) ----
        OutlinedButton(
            onClick = {
                BackgroundConfig.reset()
                BackgroundConfig.save(context)
                HomePrefs.resetAllBlockStyles()
                refreshTheme.value = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Icon(Icons.Filled.RestartAlt, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.home_appearance_reset_all))
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-page: animation preset
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnimationScreen(navigator: DestinationsNavigator) {
    AppearanceSubPage(navigator, stringResource(R.string.home_animation_preset)) {
        SectionCard(title = stringResource(R.string.home_animation_preset)) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AnimationPresetChip(stringResource(R.string.home_animation_none), CardAnimations.PRESET_NONE)
                AnimationPresetChip(stringResource(R.string.home_animation_fade), CardAnimations.PRESET_FADE)
                AnimationPresetChip(stringResource(R.string.home_animation_scale), CardAnimations.PRESET_SCALE)
                AnimationPresetChip(stringResource(R.string.home_animation_slide), CardAnimations.PRESET_SLIDE_UP)
                AnimationPresetChip(stringResource(R.string.home_animation_spring), CardAnimations.PRESET_SPRING)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-page: Gridu working card wallpaper
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GriduCardScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val pickGridLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = BackgroundManager.saveAndApplyGridWorkingCardBackground(context, uri)
                toast(context.getString(if (ok) R.string.home_bg_saved else R.string.home_bg_error))
            }
        }
    }

    AppearanceSubPage(navigator, stringResource(R.string.home_appearance_grid_card)) {
        SectionCard(title = stringResource(R.string.home_appearance_grid_card)) {
            SwitchItem(
                icon = Icons.Filled.Wallpaper,
                title = stringResource(R.string.home_appearance_grid_card),
                summary = stringResource(R.string.home_appearance_card_bg_summary),
                checked = BackgroundConfig.isGridWorkingCardBackgroundEnabled,
                onCheckedChange = {
                    BackgroundConfig.setGridWorkingCardBackgroundEnabledState(it)
                    BackgroundConfig.save(context)
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.primary) },
                headlineContent = { Text(stringResource(R.string.home_bg_pick)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickGridLauncher.launch("image/*") },
            )
            if (BackgroundConfig.gridWorkingCardBackgroundUri != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(Icons.Filled.Delete, null) },
                    headlineContent = { Text(stringResource(R.string.home_bg_remove)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { BackgroundManager.clearGridWorkingCardBackground(context) },
                )
            }
            FpSliderItem(
                title = stringResource(R.string.home_appearance_opacity),
                value = BackgroundConfig.gridWorkingCardBackgroundOpacity,
                onValueChange = {
                    BackgroundConfig.setGridWorkingCardBackgroundOpacityValue(it)
                    BackgroundConfig.save(context)
                },
            )
            FpSliderItem(
                title = stringResource(R.string.home_appearance_dim),
                value = BackgroundConfig.gridWorkingCardBackgroundDim,
                onValueChange = {
                    BackgroundConfig.setGridWorkingCardBackgroundDimValue(it)
                    BackgroundConfig.save(context)
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            SwitchItem(
                icon = Icons.Filled.InvertColors,
                title = stringResource(R.string.home_appearance_grid_hide_check),
                checked = BackgroundConfig.isGridWorkingCardCheckHidden,
                onCheckedChange = {
                    BackgroundConfig.setGridWorkingCardCheckHiddenState(it)
                    BackgroundConfig.save(context)
                },
            )
            SwitchItem(
                icon = Icons.Filled.Title,
                title = stringResource(R.string.home_appearance_grid_hide_text),
                checked = BackgroundConfig.isGridWorkingCardTextHidden,
                onCheckedChange = {
                    BackgroundConfig.setGridWorkingCardTextHiddenState(it)
                    BackgroundConfig.save(context)
                },
            )
            SwitchItem(
                icon = Icons.Filled.Title,
                title = stringResource(R.string.home_appearance_grid_hide_mode),
                checked = BackgroundConfig.isGridWorkingCardModeHidden,
                onCheckedChange = {
                    BackgroundConfig.setGridWorkingCardModeHiddenState(it)
                    BackgroundConfig.save(context)
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-page: Dashboard hero card wallpaper
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCardScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    val pickDashboardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val ok = BackgroundManager.saveAndApplyDashboardCardBackground(context, uri)
                toast(context.getString(if (ok) R.string.home_bg_saved else R.string.home_bg_error))
            }
        }
    }

    AppearanceSubPage(navigator, stringResource(R.string.home_appearance_dashboard_card)) {
        SectionCard(title = stringResource(R.string.home_appearance_dashboard_card)) {
            SwitchItem(
                icon = Icons.Filled.Wallpaper,
                title = stringResource(R.string.home_appearance_dashboard_card),
                summary = stringResource(R.string.home_appearance_card_bg_summary),
                checked = BackgroundConfig.isDashboardCardBackgroundEnabled,
                onCheckedChange = {
                    BackgroundConfig.setDashboardCardBackgroundEnabledState(it)
                    BackgroundConfig.save(context)
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.primary) },
                headlineContent = { Text(stringResource(R.string.home_bg_pick)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickDashboardLauncher.launch("image/*") },
            )
            if (BackgroundConfig.dashboardCardBgUri != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = { Icon(Icons.Filled.Delete, null) },
                    headlineContent = { Text(stringResource(R.string.home_bg_remove)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { BackgroundManager.clearDashboardCardBackground(context) },
                )
            }
            FpSliderItem(
                title = stringResource(R.string.home_appearance_opacity),
                value = BackgroundConfig.dashboardCardBgOpacity,
                onValueChange = {
                    BackgroundConfig.setDashboardCardBgOpacityValue(it)
                    BackgroundConfig.save(context)
                },
            )
            FpSliderItem(
                title = stringResource(R.string.home_appearance_dim),
                value = BackgroundConfig.dashboardCardBgDim,
                onValueChange = {
                    BackgroundConfig.setDashboardCardBgDimValue(it)
                    BackgroundConfig.save(context)
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Shared row components
// ---------------------------------------------------------------------------

/** Slider row for FolkPatch-style 0f..1f float values. */
@Composable
private fun FpSliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

/** Single selectable chip for the global animation preset. */
@Composable
private fun AnimationPresetChip(label: String, preset: Int) {
    val context = LocalContext.current
    FilterChip(
        selected = BackgroundConfig.animationPreset == preset,
        onClick = {
            BackgroundConfig.setAnimationPresetValue(preset)
            BackgroundConfig.save(context)
        },
        label = { Text(label) },
    )
}
