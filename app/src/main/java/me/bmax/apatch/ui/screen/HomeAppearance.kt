package me.bmax.apatch.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.HomeBlock
import me.bmax.apatch.ui.component.HomePrefs
import me.bmax.apatch.ui.component.SectionCard
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.theme.BackgroundConfig
import me.bmax.apatch.ui.theme.BackgroundManager
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
// Home Appearance — FolkPatch-style full customization
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppearanceScreen(navigator: DestinationsNavigator) {
    HomePrefs.ensureInit()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    // Custom background picker
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

    // Gridu working card wallpaper picker
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

    // Dashboard hero card wallpaper picker
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ---- Card visibility (hard requirement: these two switches) ----
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

            // ---- Global custom background -----------------------------------
            SectionCard(title = stringResource(R.string.home_bg_enabled)) {
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
                    title = stringResource(R.string.home_card_opacity),
                    value = BackgroundConfig.cardOpacity,
                    onValueChange = { BackgroundConfig.setCardOpacityValue(it) },
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

            // ---- Gridu working card wallpaper --------------------------------
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
                    leadingContent = { Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(stringResource(R.string.home_bg_pick)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickGridLauncher.launch("image/*") },
                )
                if (BackgroundConfig.gridWorkingCardBackgroundUri != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ListItem(
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

            // ---- Dashboard hero card wallpaper -------------------------------
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
                    leadingContent = { Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.primary) },
                    headlineContent = { Text(stringResource(R.string.home_bg_pick)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickDashboardLauncher.launch("image/*") },
                )
                if (BackgroundConfig.dashboardCardBgUri != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ListItem(
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

            // ---- Reset all appearance ---------------------------------------
            Column(Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = {
                        BackgroundConfig.reset()
                        BackgroundConfig.save(context)
                        toast(context.getString(R.string.home_appearance_reset))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(Icons.Filled.RestartAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.home_appearance_reset))
                }
            }

            NavigationBarsSpacer()
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Slider row for FolkPatch-style 0f..1f float values. */
@Composable
private fun FpSliderItem(title: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
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
            value = value.coerceIn(0f, 1f),
            onValueChange = onValueChange,
            valueRange = 0f..1f,
        )
    }
}

