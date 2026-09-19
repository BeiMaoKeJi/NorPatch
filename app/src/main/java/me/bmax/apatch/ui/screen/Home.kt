package me.bmax.apatch.ui.screen

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.system.Os
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.dropUnlessResumed
import coil3.compose.AsyncImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AboutScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeAppearanceScreenDestination
import com.ramcosta.composedestinations.generated.destinations.APModuleScreenDestination
import com.ramcosta.composedestinations.generated.destinations.InstallModeSelectScreenDestination
import com.ramcosta.composedestinations.generated.destinations.KPModuleScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PatchesDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.APApplication
import me.bmax.apatch.Natives
import me.bmax.apatch.R
import me.bmax.apatch.apApp
import me.bmax.apatch.ui.component.StatusColors
import me.bmax.apatch.ui.component.BlockCard
import me.bmax.apatch.ui.component.BlockPalette
import me.bmax.apatch.ui.component.BlockSettingsSheet
import me.bmax.apatch.ui.component.CustomContentType
import me.bmax.apatch.ui.component.GlassCard
import me.bmax.apatch.ui.component.GlassShapes
import me.bmax.apatch.ui.component.HomeBlock
import me.bmax.apatch.ui.component.HomePrefs
import me.bmax.apatch.ui.component.ProvideMenuShape
import me.bmax.apatch.ui.component.blockTitleRes
import me.bmax.apatch.ui.component.importImageToFiles
import me.bmax.apatch.ui.component.rememberConfirmDialog
import me.bmax.apatch.ui.component.WarningCard as ComponentWarningCard
import me.bmax.apatch.ui.viewmodel.PatchesViewModel
import me.bmax.apatch.util.LatestVersionInfo
import me.bmax.apatch.util.Version
import me.bmax.apatch.util.Version.getManagerVersion
import me.bmax.apatch.util.checkNewVersion
import me.bmax.apatch.util.getSELinuxStatus
import me.bmax.apatch.util.installJailbreak
import me.bmax.apatch.util.isJailbreakMode
import me.bmax.apatch.util.isSELinuxPermissive
import me.bmax.apatch.util.listModules
import me.bmax.apatch.util.migrateStockBootBackup
import me.bmax.apatch.util.reboot
import me.bmax.apatch.util.softReboot
import me.bmax.apatch.util.ui.APDialogBlurBehindUtils
import me.bmax.apatch.util.ui.Haptics
import me.bmax.apatch.util.ui.NavigationBarsSpacer
import org.json.JSONArray
import java.time.LocalTime
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalHapticFeedback
import me.bmax.apatch.ui.theme.refreshTheme
import com.ramcosta.composedestinations.generated.destinations.SuperUserScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingScreenDestination
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.fillMaxSize

private val managerVersion = getManagerVersion()

/** NorPatch community QQ group; users copy the number and join in QQ manually. */
private const val QQ_GROUP_NUMBER = "1121505516"

@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val kpState by APApplication.kpStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val apState by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)

    // Pick up a stock boot backup left behind by a manually flashed PATCH_ONLY
    // install; see migrateStockBootBackup.
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { migrateStockBootBackup() }
    }

    // Home layout switching: "gridu" / "dashboard_ui" / "norpatch" (persisted).
    val layoutRefresh by refreshTheme.observeAsState(false)
    var homeLayout by remember {
        mutableStateOf(APApplication.sharedPreferences.getString("home_layout_style", "norpatch"))
    }
    if (layoutRefresh) {
        homeLayout = APApplication.sharedPreferences.getString("home_layout_style", "norpatch")
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HomeTopActions(
                onInstallClick = dropUnlessResumed {
                    navigator.navigate(InstallModeSelectScreenDestination)
                },
                navigator = navigator,
                kpState = kpState,
            )
        },
    ) { innerPadding ->
        when (homeLayout) {
            "gridu" -> HomeGridu(innerPadding, navigator, kpState, apState)
        "dashboard_ui" -> HomeDashboardV4(innerPadding, navigator, kpState, apState)
            else -> HomeLayoutNorPatch(innerPadding, navigator, kpState, apState)
        }
    }
}
/** Single home card wrapper: AnimatedVisibility (fade/expand) + uniform BlockCard shell. */
/** Single home card wrapper: FolkPatch-style staggered entrance (fade + rise)
 *  plus the visibility switch from Home Appearance. The expand/shrink exit
 *  keeps layout shifts smooth when a card is toggled off. */
@Composable
private fun HomeCard(
    block: HomeBlock,
    enabled: Boolean,
    index: Int,
    content: @Composable ColumnScope.() -> Unit,
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (enabled) {
            entered = true
        } else {
            entered = false
        }
    }
    AnimatedVisibility(
        visible = entered && enabled,
        enter = fadeIn(animationSpec = tween(380, delayMillis = index * 90)) +
            slideInVertically(
                animationSpec = tween(380, delayMillis = index * 90),
                initialOffsetY = { it / 4 },
            ),
        exit = fadeOut(animationSpec = tween(240)) +
            shrinkVertically(animationSpec = tween(240)),
    ) {
        BlockCard(
            block = block,
            windowMode = false,
            content = content,
        )
    }
}
/** NP default home layout: three fixed glass cards (status / device / about). */
@Composable
private fun HomeLayoutNorPatch(
    innerPadding: PaddingValues,
    navigator: DestinationsNavigator,
    kpState: APApplication.State,
    apState: APApplication.State,
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxWidth(),
    ) {
        // Single outer scroll container; cards never scroll internally.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
                Spacer(Modifier.height(12.dp))
                val cardSpacing = (HomePrefs.blockSpacing[HomeBlock.STATUS]
                    ?: HomePrefs.DEFAULT_BLOCK_SPACING).dp

                // 1. Status overview card (fixed order, no drag, no gear)
                HomeCard(HomeBlock.STATUS, HomePrefs.blockEnabled[HomeBlock.STATUS] ?: true, index = 0) {
                    StatusOverviewContent(kpState, apState)
                }

                Spacer(Modifier.height(cardSpacing))

                // 2. Device info card
                HomeCard(HomeBlock.DEVICE, HomePrefs.blockEnabled[HomeBlock.DEVICE] ?: true, index = 1) {
                    DeviceInfoContent(kpState, apState)
                }

                Spacer(Modifier.height(cardSpacing))

                // 3. About NorPatch card
                HomeCard(HomeBlock.TITLE, HomePrefs.blockEnabled[HomeBlock.TITLE] ?: true, index = 2) {
                    AboutNorPatchContent(navigator)
                }

                Spacer(Modifier.height(cardSpacing))

                Spacer(Modifier.height(24.dp)) // thin tail; content slides behind the frosted dock like FP
                Spacer(Modifier.height(24.dp))
        }
    }
}


// ---------------------------------------------------------------------------
// Card 1 璺?Status overview: big status text (left) + two rounded status tags
// ---------------------------------------------------------------------------

@Composable
private fun StatusOverviewContent(
    kpState: APApplication.State,
    apState: APApplication.State,
) {
    val bigText = when (kpState) {
        APApplication.State.KERNELPATCH_INSTALLED -> stringResource(R.string.home_status_kp_big_installed)
        else -> stringResource(R.string.home_status_kp_big)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bigText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_status_kp_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusTag(
                label = stringResource(R.string.home_patch_kernel),
                value = kpStateText(kpState),
                level = kpLevel(kpState),
            )
            StatusTag(
                label = stringResource(R.string.home_patch_system),
                value = apStateText(apState),
                level = apLevel(apState),
            )
        }
    }
}

private enum class StatusLevel { OK, WARN, INFO, MUTED }

private fun kpLevel(state: APApplication.State): StatusLevel = when (state) {
    APApplication.State.KERNELPATCH_INSTALLED -> StatusLevel.OK
    APApplication.State.KERNELPATCH_NEED_UPDATE -> StatusLevel.WARN
    APApplication.State.KERNELPATCH_NEED_REBOOT -> StatusLevel.INFO
    APApplication.State.KERNELPATCH_UNINSTALLING -> StatusLevel.INFO
    else -> StatusLevel.MUTED
}

private fun apLevel(state: APApplication.State): StatusLevel = when (state) {
    APApplication.State.ANDROIDPATCH_INSTALLED -> StatusLevel.OK
    APApplication.State.ANDROIDPATCH_NEED_UPDATE -> StatusLevel.WARN
    APApplication.State.ANDROIDPATCH_INSTALLING,
    APApplication.State.ANDROIDPATCH_UNINSTALLING -> StatusLevel.INFO
    else -> StatusLevel.MUTED
}

@Composable
private fun StatusTag(label: String, value: String, level: StatusLevel) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = surfaceColor.luminance() < 0.5f

    val targetBg = when (level) {
        StatusLevel.OK -> StatusColors.success().copy(alpha = 0.16f)
        StatusLevel.WARN -> StatusColors.warning().copy(alpha = 0.16f)
        StatusLevel.INFO -> StatusColors.info().copy(alpha = 0.16f)
        StatusLevel.MUTED -> StatusColors.muted().copy(alpha = 0.14f)
    }
    val targetFg = when (level) {
        StatusLevel.OK -> StatusColors.success()
        StatusLevel.WARN -> StatusColors.warning()
        StatusLevel.INFO -> StatusColors.info()
        StatusLevel.MUTED -> StatusColors.muted()
    }

    // Smooth color morph when the state changes (e.g. "闂団偓閺囧瓨鏌? -> "瀹告彃鐣ㄧ憗?).
    val animatedBg by animateColorAsState(targetBg, tween(450), label = "tagBg")
    val animatedFg by animateColorAsState(targetFg, tween(450), label = "tagFg")

    // Gentle morph pulse on level transitions only (no startup flicker).
    val scale = remember { Animatable(1f) }
    var prevLevel by remember { mutableStateOf(level) }
    LaunchedEffect(level) {
        if (level != prevLevel) {
            scale.snapTo(0.9f)
            scale.animateTo(1f, tween(460, easing = FastOutSlowInEasing))
            prevLevel = level
        }
    }

    Row(
        modifier = Modifier
            .height(30.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(RoundedCornerShape(15.dp))
            .background(animatedBg)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = animatedFg,
        )
    }
}

@Composable
private fun kpStateText(state: APApplication.State): String = when (state) {
    APApplication.State.KERNELPATCH_INSTALLED -> stringResource(R.string.home_state_installed)
    APApplication.State.KERNELPATCH_NEED_UPDATE -> stringResource(R.string.home_state_need_update)
    APApplication.State.KERNELPATCH_NEED_REBOOT -> stringResource(R.string.home_state_need_reboot)
    APApplication.State.KERNELPATCH_UNINSTALLING -> stringResource(R.string.home_state_uninstalling)
    else -> stringResource(R.string.home_state_not_installed)
}

@Composable
private fun apStateText(state: APApplication.State): String = when (state) {
    APApplication.State.ANDROIDPATCH_INSTALLED -> stringResource(R.string.home_state_ready)
    APApplication.State.ANDROIDPATCH_NOT_INSTALLED -> stringResource(R.string.home_state_not_installed)
    APApplication.State.ANDROIDPATCH_NEED_UPDATE -> stringResource(R.string.home_state_need_update)
    APApplication.State.ANDROIDPATCH_INSTALLING -> stringResource(R.string.home_state_installing)
    APApplication.State.ANDROIDPATCH_UNINSTALLING -> stringResource(R.string.home_state_uninstalling)
    else -> stringResource(R.string.home_state_unknown)
}

// ---------------------------------------------------------------------------
// Card 2 璺?Device info: device name on top, fixed-height two-column rows
// ---------------------------------------------------------------------------

@Composable
private fun DeviceInfoContent(
    kpState: APApplication.State,
    apState: APApplication.State,
) {
    Text(
        text = getDeviceInfo(),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(10.dp))
    val uname = Os.uname()
    DeviceInfoRow(stringResource(R.string.home_system_version), getSystemVersion())
    DeviceInfoRow(stringResource(R.string.home_kernel), uname.release)
    if (kpState != APApplication.State.UNKNOWN_STATE) {
        DeviceInfoRow(stringResource(R.string.home_kpatch_version), Version.installedKPVString())
        DeviceInfoRow(
            stringResource(R.string.home_su_path),
            runCatching { Natives.suPath() }.getOrDefault("N/A")
        )
    }
    if (apState != APApplication.State.UNKNOWN_STATE &&
        apState != APApplication.State.ANDROIDPATCH_NOT_INSTALLED
    ) {
        DeviceInfoRow(stringResource(R.string.home_apatch_version), managerVersion.second.toString())
    }
    DeviceInfoRow(stringResource(R.string.home_fingerprint), Build.FINGERPRINT)
    DeviceInfoRow(stringResource(R.string.home_selinux_status), getSELinuxStatus())
}

/** Fixed-height two-column row: label (left) + smaller value (right, right-aligned). */
@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.9f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.1f),
        )
    }
}

// ---------------------------------------------------------------------------
// Card 3 璺?About NorPatch
// ---------------------------------------------------------------------------

@Composable
private fun AboutNorPatchContent(navigator: DestinationsNavigator) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { navigator.navigate(AboutScreenDestination) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "nP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_block_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_about_card_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UninstallDialog(showDialog: MutableState<Boolean>, navigator: DestinationsNavigator) {
    BasicAlertDialog(
        onDismissRequest = { showDialog.value = false }, properties = DialogProperties(
            decorFitsSystemWindows = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(modifier = Modifier.padding(PaddingValues(all = 24.dp))) {
                Box(
                    Modifier
                        .padding(PaddingValues(bottom = 16.dp))
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = stringResource(id = R.string.home_dialog_uninstall_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Text(
                    text = stringResource(id = R.string.home_dialog_uninstall_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(PaddingValues(bottom = 24.dp))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }

                    TextButton(onClick = {
                        showDialog.value = false
                        APApplication.uninstallApatch()
                    }) {
                        Text(text = stringResource(id = R.string.home_dialog_uninstall_ap_only))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        showDialog.value = false
                        APApplication.uninstallApatch()
                        navigator.navigate(PatchesDestination(PatchesViewModel.PatchMode.UNPATCH))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = stringResource(id = R.string.home_dialog_uninstall_all))
                }
            }
            val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
            APDialogBlurBehindUtils.setupWindowBlurListener(dialogWindowProvider.window)
        }
    }
}

@Composable
fun RebootDropdownItem(@StringRes id: Int, reason: String = "", onClick: (() -> Unit)? = null) {
    DropdownMenuItem(text = {
        Text(stringResource(id))
    }, onClick = onClick ?: { reboot(reason) })
}

/** Fixed system-level top bar: menu button (left), hard-coded NorPatch title
 *  (center), download + more (right). The title is intentionally not editable. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopActions(
    onInstallClick: () -> Unit, navigator: DestinationsNavigator, kpState: APApplication.State
) {
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val communityCopiedMsg = stringResource(R.string.home_community_copied)
    fun copyCommunityGroup() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("NorPatch QQ Group", QQ_GROUP_NUMBER))
        Toast.makeText(context, communityCopiedMsg, Toast.LENGTH_SHORT).show()
    }
    var showDropdownMoreOptions by remember { mutableStateOf(false) }

    val downloadTitle = stringResource(id = R.string.reboot_download)
    val downloadConfirmText = stringResource(id = R.string.reboot_download_confirm)
    val edlTitle = stringResource(id = R.string.reboot_edl)
    val edlConfirmText = stringResource(id = R.string.reboot_edl_confirm)
    var pendingRebootReason by remember { mutableStateOf<String?>(null) }
    val rebootConfirmDialog = rememberConfirmDialog(onConfirm = {
        pendingRebootReason?.let { reboot(it) }
    })

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "NorPatch",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            Text(
                text = stringResource(R.string.home),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp),
            )
        },
        actions = {
            IconButton(
                onClick = {
                    Haptics.tick(haptics)
                    onInstallClick()
                },
            ) {
                Icon(
                    imageVector = Icons.Filled.InstallMobile,
                    contentDescription = stringResource(R.string.mode_select_page_title),
                )
            }
            Box {
                IconButton(onClick = { showDropdownMoreOptions = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
                ProvideMenuShape(RoundedCornerShape(14.dp)) {
                    DropdownMenu(expanded = showDropdownMoreOptions, onDismissRequest = {
                        showDropdownMoreOptions = false
                    }) {
                        if (kpState != APApplication.State.UNKNOWN_STATE) {
                            HorizontalDivider()
                            RebootDropdownItem(id = R.string.reboot, onClick = {
                                showDropdownMoreOptions = false
                                reboot()
                            })
                            RebootDropdownItem(id = R.string.reboot_soft, reason = "soft_reboot")
                            RebootDropdownItem(id = R.string.reboot_recovery, reason = "recovery")
                            RebootDropdownItem(id = R.string.reboot_bootloader, reason = "bootloader")
                            RebootDropdownItem(id = R.string.reboot_download, onClick = {
                                showDropdownMoreOptions = false
                                pendingRebootReason = "download"
                                rebootConfirmDialog.showConfirm(
                                    title = downloadTitle, content = downloadConfirmText
                                )
                            })
                            RebootDropdownItem(id = R.string.reboot_edl, onClick = {
                                showDropdownMoreOptions = false
                                pendingRebootReason = "edl"
                                rebootConfirmDialog.showConfirm(
                                    title = edlTitle, content = edlConfirmText
                                )
                            })
                        }
                        HorizontalDivider()
                        DropdownMenuItem(text = {
                            Text(stringResource(R.string.home_more_menu_community))
                        }, onClick = {
                            showDropdownMoreOptions = false
                            copyCommunityGroup()
                        })
                        DropdownMenuItem(text = {
                            Text(stringResource(R.string.home_more_menu_about))
                        }, onClick = {
                            navigator.navigate(AboutScreenDestination)
                            showDropdownMoreOptions = false
                        })
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

/** Round glass icon button used by the floating action bar. */
@Composable
private fun GlassActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(42.dp)
            .clip(GlassShapes.Pill)
            .background(
                (if (isDark) Color(0xFF241A20) else Color.White).copy(alpha = 0.72f)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------------------------------------------------------------------
// Block 2: status block - running state + kernel/system patch states.
// Keeps the full original kernel-patch action logic (install/update/reboot/
// uninstall/jailbreak) and merges the system-patch (APatch) actions here.
// ---------------------------------------------------------------------------

private data class StatusVisual(
    val key: String,
    val text: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
private fun StatusBlockContent(
    kpState: APApplication.State,
    apState: APApplication.State,
    navigator: DestinationsNavigator,
) {
    val haptics = LocalHapticFeedback.current
    val showUninstallDialog = remember { mutableStateOf(false) }
    if (showUninstallDialog.value) {
        UninstallDialog(showDialog = showUninstallDialog, navigator)
    }

    // Jailbreak button appears when the kernel is not installed and SELinux is permissive.
    val isPermissive by produceState(initialValue = false) {
        value = withContext(Dispatchers.IO) { isSELinuxPermissive() }
    }
    // Jailbreak mode is active when the KernelPatch module has been loaded on a
    // stock kernel (a marker is written by apd late-load).
    val isJailbreak by produceState(initialValue = false) {
        value = withContext(Dispatchers.IO) { isJailbreakMode() }
    }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val jailbreakFailedMsg = stringResource(R.string.settings_jailbreak_failed)
    val jailbreakTriggeredMsg = stringResource(R.string.jailbreak_triggered)

    val normal = !isJailbreak && kpState == APApplication.State.KERNELPATCH_INSTALLED
    val needUpdate =
        kpState == APApplication.State.KERNELPATCH_NEED_UPDATE ||
            kpState == APApplication.State.KERNELPATCH_NEED_REBOOT

    val visual = when {
        isJailbreak -> StatusVisual(
            "jailbreak",
            stringResource(R.string.settings_jailbreak_mode),
            Icons.Filled.LockOpen,
            MaterialTheme.colorScheme.tertiary,
        )

        normal -> StatusVisual(
            "normal",
            stringResource(R.string.home_status_running),
            Icons.Filled.CheckCircle,
            Color(0xFF34A853),
        )

        needUpdate -> StatusVisual(
            "update",
            stringResource(R.string.home_status_abnormal),
            Icons.Outlined.SystemUpdate,
            MaterialTheme.colorScheme.tertiary,
        )

        else -> StatusVisual(
            "abnormal",
            stringResource(R.string.home_status_abnormal),
            Icons.AutoMirrored.Outlined.HelpOutline,
            MaterialTheme.colorScheme.error,
        )
    }
    val animatedColor by animateColorAsState(visual.color, tween(400), label = "statusColor")

    AnimatedContent(
        targetState = visual,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "status",
    ) { v ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(GlassShapes.Pill)
                    .background(v.color.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = v.icon,
                            contentDescription = null,
                            tint = animatedColor,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = v.text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.home_kernel_patch) + "  " +
                                kernelPatchVersionText(kpState, isJailbreak),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = stringResource(R.string.home_system_patch) + "  " +
                                stringResource(systemPatchStateRes(apState)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            if (!isJailbreak &&
                kpState != APApplication.State.UNKNOWN_STATE &&
                kpState != APApplication.State.KERNELPATCH_NEED_UPDATE &&
                kpState != APApplication.State.KERNELPATCH_NEED_REBOOT
            ) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${Version.installedKPVString()} (${managerVersion.second}) - " +
                        if (apState != APApplication.State.ANDROIDPATCH_NOT_INSTALLED) "Full" else "KernelPatch",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    Haptics.tick(haptics)
                    when {
                        isJailbreak -> {
                            softReboot()
                        }

                        kpState == APApplication.State.UNKNOWN_STATE -> {
                            navigator.navigate(InstallModeSelectScreenDestination)
                        }

                        kpState == APApplication.State.KERNELPATCH_NEED_UPDATE -> {
                            // todo: remove legacy compact for kp < 0.9.0
                            if (Version.installedKPVUInt() < 0x900u) {
                                navigator.navigate(PatchesDestination(PatchesViewModel.PatchMode.PATCH_ONLY))
                            } else {
                                navigator.navigate(InstallModeSelectScreenDestination)
                            }
                        }

                        kpState == APApplication.State.KERNELPATCH_NEED_REBOOT -> {
                            reboot()
                        }

                        kpState == APApplication.State.KERNELPATCH_UNINSTALLING -> {
                            // Do nothing
                        }

                        else -> {
                            if (apState == APApplication.State.ANDROIDPATCH_INSTALLED ||
                                apState == APApplication.State.ANDROIDPATCH_NEED_UPDATE
                            ) {
                                showUninstallDialog.value = true
                            } else {
                                navigator.navigate(PatchesDestination(PatchesViewModel.PatchMode.UNPATCH))
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = GlassShapes.Small,
            ) {
                when {
                    isJailbreak -> {
                        Text(text = stringResource(id = R.string.reboot_soft))
                    }

                    kpState == APApplication.State.UNKNOWN_STATE -> {
                        Text(text = stringResource(id = R.string.home_ap_cando_install))
                    }

                    kpState == APApplication.State.KERNELPATCH_NEED_UPDATE -> {
                        Text(text = stringResource(id = R.string.home_ap_cando_update))
                    }

                    kpState == APApplication.State.KERNELPATCH_NEED_REBOOT -> {
                        Text(text = stringResource(id = R.string.home_ap_cando_reboot))
                    }

                    kpState == APApplication.State.KERNELPATCH_UNINSTALLING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    }

                    else -> {
                        Text(text = stringResource(id = R.string.home_ap_cando_uninstall))
                    }
                }
            }

            if (kpState == APApplication.State.UNKNOWN_STATE && isPermissive) {
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val success = installJailbreak()
                            if (success) {
                                Toast.makeText(context, jailbreakTriggeredMsg, Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(context, jailbreakFailedMsg, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = GlassShapes.Small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                ) {
                    Text(stringResource(R.string.jailbreak))
                }
            }

            // Merged system-patch (APatch) actions, kept from the original AStatusCard.
            if (apState != APApplication.State.UNKNOWN_STATE &&
                apState != APApplication.State.ANDROIDPATCH_INSTALLED
            ) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(GlassShapes.Pill)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (apState) {
                            APApplication.State.ANDROIDPATCH_NOT_INSTALLED -> {
                                Icon(Icons.Outlined.Block, null, modifier = Modifier.size(16.dp))
                            }

                            APApplication.State.ANDROIDPATCH_INSTALLING -> {
                                Icon(Icons.Outlined.Cached, null, modifier = Modifier.size(16.dp))
                            }

                            APApplication.State.ANDROIDPATCH_NEED_UPDATE -> {
                                Icon(Icons.Outlined.SystemUpdate, null, modifier = Modifier.size(16.dp))
                            }

                            else -> {
                                Icon(
                                    Icons.AutoMirrored.Outlined.HelpOutline,
                                    null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when (apState) {
                            APApplication.State.ANDROIDPATCH_NOT_INSTALLED -> {
                                stringResource(R.string.home_not_installed)
                            }

                            APApplication.State.ANDROIDPATCH_INSTALLING -> {
                                stringResource(R.string.home_installing)
                            }

                            APApplication.State.ANDROIDPATCH_NEED_UPDATE -> {
                                stringResource(
                                    R.string.apatch_version_update,
                                    Version.installedApdVString,
                                    managerVersion.second
                                )
                            }

                            else -> {
                                stringResource(R.string.home_system_patch_unknown)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            Haptics.tick(haptics)
                            when (apState) {
                                APApplication.State.ANDROIDPATCH_NOT_INSTALLED,
                                APApplication.State.ANDROIDPATCH_NEED_UPDATE -> {
                                    APApplication.installApatch()
                                }

                                APApplication.State.ANDROIDPATCH_UNINSTALLING -> {
                                    // Do nothing
                                }

                                else -> {
                                    APApplication.uninstallApatch()
                                }
                            }
                        }
                    ) {
                        when (apState) {
                            APApplication.State.ANDROIDPATCH_NOT_INSTALLED -> {
                                Text(text = stringResource(id = R.string.home_ap_cando_install))
                            }

                            APApplication.State.ANDROIDPATCH_NEED_UPDATE -> {
                                Text(text = stringResource(id = R.string.home_ap_cando_update))
                            }

                            APApplication.State.ANDROIDPATCH_UNINSTALLING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                            }

                            else -> {
                                Text(text = stringResource(id = R.string.home_ap_cando_uninstall))
                            }
                        }
                    }
                }
            }
}

@Composable
private fun kernelPatchVersionText(kpState: APApplication.State, isJailbreak: Boolean): String {
    val notInstalled = stringResource(R.string.home_not_installed)
    return when {
        isJailbreak -> stringResource(R.string.settings_jailbreak_mode)
        kpState == APApplication.State.KERNELPATCH_INSTALLED ||
            kpState == APApplication.State.KERNELPATCH_NEED_UPDATE ||
            kpState == APApplication.State.KERNELPATCH_NEED_REBOOT ->
            runCatching { Version.installedKPVString() }.getOrDefault(notInstalled)

        else -> notInstalled
    }
}

@StringRes
private fun systemPatchStateRes(apState: APApplication.State): Int = when (apState) {
    APApplication.State.ANDROIDPATCH_INSTALLED -> R.string.home_system_patch_ready
    APApplication.State.ANDROIDPATCH_INSTALLING -> R.string.home_system_patch_installing
    APApplication.State.ANDROIDPATCH_NEED_UPDATE -> R.string.home_system_patch_update
    APApplication.State.ANDROIDPATCH_NOT_INSTALLED -> R.string.home_not_installed
    else -> R.string.home_system_patch_unknown
}

// ---------------------------------------------------------------------------
// Greeting block: first item of the scrollable area, below the fixed top bar.
// Shows the time-based greeting plus the user-editable description text.
// (The hard-coded "NorPatch" title lives in the fixed top bar above.)
// ---------------------------------------------------------------------------

@Composable
private fun GreetingBlockContent() {
    HomePrefs.ensureInit()
    val hour = java.time.LocalTime.now().hour
    val greeting = greetingForHour(
        hour,
        stringResource(R.string.home_greeting_morning),
        stringResource(R.string.home_greeting_afternoon),
        stringResource(R.string.home_greeting_night),
    )
    Text(
        text = greeting,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )
    AnimatedContent(
        targetState = HomePrefs.homeSubtitle,
        transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(220)) },
        label = "homeSubtitle",
    ) { subtitle ->
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Block 3: custom block - one of the display types chosen in Home Appearance
// ---------------------------------------------------------------------------

@Composable
private fun uptimeText(): String {
    val totalMin = SystemClock.elapsedRealtime() / 60000
    val days = totalMin / 1440
    val hours = (totalMin % 1440) / 60
    val mins = totalMin % 60
    val dayUnit = stringResource(R.string.home_uptime_days)
    val hourUnit = stringResource(R.string.home_uptime_hours)
    val minUnit = stringResource(R.string.home_uptime_mins)
    return when {
        days > 0 -> "$days$dayUnit $hours$hourUnit"
        hours > 0 -> "$hours$hourUnit $mins$minUnit"
        else -> "$mins$minUnit"
    }
}

@Composable
private fun totalRamGb(): String {
    val context = LocalContext.current
    return runCatching {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        String.format("%.1f GB", mi.totalMem / 1073741824.0)
    }.getOrDefault("N/A")
}

private fun storageGb(): String {
    return runCatching {
        String.format("%.1f GB", Environment.getDataDirectory().totalSpace / 1073741824.0)
    }.getOrDefault("N/A")
}

@Composable
private fun CustomBlockContent(kpState: APApplication.State) {
    HomePrefs.ensureInit()
    AnimatedContent(
        targetState = HomePrefs.customContentType,
        transitionSpec = {
            (fadeIn(tween(320)) + scaleIn(initialScale = 0.95f, animationSpec = tween(320))) togetherWith
                (fadeOut(tween(220)) + scaleOut(targetScale = 0.97f, animationSpec = tween(220)))
        },
        label = "customContent",
    ) { type ->
        when (type) {
            CustomContentType.IMAGE -> CustomImageContent()
            CustomContentType.UPTIME -> CustomUptimeContent()
            CustomContentType.CUSTOM_TEXT -> CustomTextContent()
            CustomContentType.BASIC_INFO -> CustomBasicInfoContent()
            CustomContentType.DETAIL_PARAMS -> CustomDetailParamsContent()
            CustomContentType.DEVICE_NAME -> CustomDeviceNameContent()
        }
    }
}

@Composable
private fun CustomImageContent() {
    val file = HomePrefs.customContentFile()
    if (file != null) {
        AsyncImage(
            model = file,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(GlassShapes.Small),
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_custom_image_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CustomUptimeContent() {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tick++
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = uptimeText(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.home_custom_uptime_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CustomTextContent() {
    val text = HomePrefs.customContentText.ifBlank {
        stringResource(R.string.home_custom_text_empty)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_custom_text_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun CustomBasicInfoContent() {
    val rows = listOf(
        stringResource(R.string.home_device_info) to getDeviceInfo(),
        stringResource(R.string.home_system_version) to getSystemVersion(),
        stringResource(R.string.home_kernel) to Os.uname().release,
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { (label, value) ->
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CustomDetailParamsContent() {
    val configuration = LocalConfiguration.current
    val resolution = "${configuration.screenWidthDp} 鑴?${configuration.screenHeightDp}"
    val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "N/A"
    val rows = listOf(
        stringResource(R.string.home_device_resolution) to resolution,
        stringResource(R.string.home_device_cpu) to abi,
        stringResource(R.string.home_device_ram) to totalRamGb(),
        stringResource(R.string.home_device_storage) to storageGb(),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun CustomDeviceNameContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.PhoneAndroid,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(34.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = getDeviceInfo(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.home_device_info),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------------------------------------------------------------------
// Backup warning (original behavior preserved)
// ---------------------------------------------------------------------------

@Composable
fun WarningCard() {
    var show by rememberSaveable { mutableStateOf(apApp.getBackupWarningState()) }
    if (show) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            fillAlpha = 0.88f,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Warning, contentDescription = "warning", tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(12.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.patch_warnning),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.width(12.dp))
                Icon(
                    Icons.Outlined.Clear,
                    contentDescription = "",
                    modifier = Modifier.clickable {
                        show = false
                        apApp.updateBackupWarningState(false)
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun getSystemVersion(): String {
    return "${Build.VERSION.RELEASE} ${if (Build.VERSION.PREVIEW_SDK_INT != 0) "Preview" else ""} (API ${Build.VERSION.SDK_INT})"
}

internal fun getDeviceInfo(): String {
    var manufacturer =
        Build.MANUFACTURER[0].uppercaseChar().toString() + Build.MANUFACTURER.substring(1)
    if (!Build.BRAND.equals(Build.MANUFACTURER, ignoreCase = true)) {
        manufacturer += " " + Build.BRAND[0].uppercaseChar() + Build.BRAND.substring(1)
    }
    manufacturer += " " + Build.MODEL + " "
    return manufacturer
}

// ---------------------------------------------------------------------------
// Block 4: device info block - big card with device / kernel / su / module info
// ---------------------------------------------------------------------------

@Composable
private fun DeviceBlockContent(
    kpState: APApplication.State,
    apState: APApplication.State,
    navigator: DestinationsNavigator,
) {
    // Module counts (original ModuleCountRow behavior, kept inside this block)
    var refreshTick by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) {
        refreshTick++
        onPauseOrDispose { }
    }
    val sysCount by produceState(initialValue = -1, refreshTick, kpState) {
        value = withContext(Dispatchers.IO) {
            runCatching { JSONArray(listModules()).length() }.getOrDefault(-1)
        }
    }
    val kpmCount by produceState(initialValue = -1L, refreshTick, kpState) {
        value = withContext(Dispatchers.IO) {
            if (kpState == APApplication.State.KERNELPATCH_INSTALLED) {
                runCatching { Natives.kernelPatchModuleNum() }.getOrDefault(-1L)
            } else {
                -1L
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(GlassShapes.Pill)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = getDeviceInfo(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.home_device_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ModuleCountPill(
            label = stringResource(R.string.home_system_modules),
            count = sysCount.toLong(),
            modifier = Modifier.weight(1f),
            onClick = { navigator.navigate(APModuleScreenDestination) },
        )
        ModuleCountPill(
            label = stringResource(R.string.home_kernel_modules),
            count = kpmCount,
            modifier = Modifier.weight(1f),
            onClick = { navigator.navigate(KPModuleScreenDestination) },
        )
    }

    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    Spacer(Modifier.height(12.dp))

    val uname = Os.uname()
    DeviceInfoRow(stringResource(R.string.home_system_version), getSystemVersion())
    DeviceInfoRow(stringResource(R.string.home_kernel), uname.release)
    if (kpState != APApplication.State.UNKNOWN_STATE) {
        DeviceInfoRow(
            stringResource(R.string.home_kpatch_version), Version.installedKPVString()
        )
        DeviceInfoRow(
            stringResource(R.string.home_su_path),
            runCatching { Natives.suPath() }.getOrDefault("N/A")
        )
    }
    if (apState != APApplication.State.UNKNOWN_STATE &&
        apState != APApplication.State.ANDROIDPATCH_NOT_INSTALLED
    ) {
        DeviceInfoRow(
            stringResource(R.string.home_apatch_version), managerVersion.second.toString()
        )
    }
    DeviceInfoRow(stringResource(R.string.home_fingerprint), Build.FINGERPRINT)
    DeviceInfoRow(stringResource(R.string.home_selinux_status), getSELinuxStatus())
}

@Composable
private fun ModuleCountPill(
    label: String,
    count: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(GlassShapes.Small)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = count,
            transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) },
            label = "count",
        ) { c ->
            Text(
                text = if (c >= 0) c.toString() else "N/A",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DeviceInfoRowLegacy(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun LearnMoreCard() {
    val context = LocalContext.current
    val communityCopiedMsg = stringResource(R.string.home_community_copied)

    fun copyCommunityGroup() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("NorPatch QQ Group", QQ_GROUP_NUMBER))
        Toast.makeText(context, communityCopiedMsg, Toast.LENGTH_SHORT).show()
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        fillAlpha = 0.80f,
    ) {
        // Local intro text (no external link)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = stringResource(R.string.home_about_norpatch_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_about_norpatch_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(12.dp))

        // Community entry: copy the QQ group number, join manually in QQ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { copyCommunityGroup() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Forum,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_community_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.home_community_group),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.home_community_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = stringResource(R.string.home_community_copied),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
