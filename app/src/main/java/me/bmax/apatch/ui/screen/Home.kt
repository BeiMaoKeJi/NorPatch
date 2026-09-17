package me.bmax.apatch.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.system.Os
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.dropUnlessResumed
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AboutScreenDestination
import com.ramcosta.composedestinations.generated.destinations.APModuleScreenDestination
import com.ramcosta.composedestinations.generated.destinations.InstallModeSelectScreenDestination
import com.ramcosta.composedestinations.generated.destinations.KPModuleScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PatchesDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.APApplication
import me.bmax.apatch.Natives
import me.bmax.apatch.R
import me.bmax.apatch.apApp
import me.bmax.apatch.ui.component.DrawerIconButton
import me.bmax.apatch.ui.component.GlassCard
import me.bmax.apatch.ui.component.GlassShapes
import me.bmax.apatch.ui.component.HomePrefs
import me.bmax.apatch.ui.component.ProvideMenuShape
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
import me.bmax.apatch.util.ui.NavigationBarsSpacer
import org.json.JSONArray
import java.time.LocalTime

private val managerVersion = getManagerVersion()

/** NorPatch community QQ group; users copy the number and join in QQ manually. */
private const val QQ_GROUP_NUMBER = "1072360977"

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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(0.dp))

            // Status (kernel patch) glass card
            StatusCard(kpState, apState, navigator)

            // AndroidPatch (system patch) actions card
            if (kpState != APApplication.State.UNKNOWN_STATE &&
                apState != APApplication.State.ANDROIDPATCH_INSTALLED
            ) {
                AStatusCard(apState)
            }

            // Greeting + device model + custom big title
            GreetingAndTitle()

            // System / kernel module counts
            ModuleCountRow(navigator, kpState)

            // Executable su path
            SuPathCard(kpState)

            // Backup warning (original behavior kept)
            WarningCard()

            val prefs = APApplication.sharedPreferences
            val checkUpdate by produceState(initialValue = prefs.getBoolean("check_update", true)) {
                val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                    if (key == "check_update") {
                        value = p.getBoolean(key, true)
                    }
                }
                prefs.registerOnSharedPreferenceChangeListener(listener)
                awaitDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
            }
            if (checkUpdate) {
                UpdateCard()
            }

            InfoCard(kpState, apState)
            LearnMoreCard()
            NavigationBarsSpacer()
            Spacer(Modifier.height(24.dp))
        }
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/** Floating glass action bar at the top of the homepage. */
@Composable
private fun HomeTopActions(
    onInstallClick: () -> Unit, navigator: DestinationsNavigator, kpState: APApplication.State
) {
    val context = LocalContext.current
    val communityCopiedMsg = stringResource(R.string.home_community_copied)
    fun copyCommunityGroup() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("NorPatch QQ Group", QQ_GROUP_NUMBER))
        Toast.makeText(context, communityCopiedMsg, Toast.LENGTH_SHORT).show()
    }
    var showDropdownMoreOptions by remember { mutableStateOf(false) }
    var showDropdownReboot by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DrawerIconButton()

        Spacer(Modifier.weight(1f))

        GlassActionButton(
            onClick = onInstallClick,
            icon = Icons.Filled.InstallMobile,
            contentDescription = stringResource(R.string.mode_select_page_title),
        )

        if (kpState != APApplication.State.UNKNOWN_STATE) {
            val downloadTitle = stringResource(id = R.string.reboot_download)
            val downloadConfirmText = stringResource(id = R.string.reboot_download_confirm)
            val edlTitle = stringResource(id = R.string.reboot_edl)
            val edlConfirmText = stringResource(id = R.string.reboot_edl_confirm)
            var pendingRebootReason by remember { mutableStateOf<String?>(null) }
            val rebootConfirmDialog = rememberConfirmDialog(onConfirm = {
                pendingRebootReason?.let { reboot(it) }
            })

            Box {
                GlassActionButton(
                    onClick = { showDropdownReboot = true },
                    icon = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.reboot),
                )
                ProvideMenuShape(RoundedCornerShape(10.dp)) {
                    DropdownMenu(expanded = showDropdownReboot, onDismissRequest = {
                        showDropdownReboot = false
                    }) {
                        RebootDropdownItem(id = R.string.reboot)
                        RebootDropdownItem(id = R.string.reboot_soft, reason = "soft_reboot")
                        RebootDropdownItem(id = R.string.reboot_recovery, reason = "recovery")
                        RebootDropdownItem(id = R.string.reboot_bootloader, reason = "bootloader")
                        RebootDropdownItem(id = R.string.reboot_download, onClick = {
                            showDropdownReboot = false
                            pendingRebootReason = "download"
                            rebootConfirmDialog.showConfirm(
                                title = downloadTitle, content = downloadConfirmText
                            )
                        })
                        RebootDropdownItem(id = R.string.reboot_edl, onClick = {
                            showDropdownReboot = false
                            pendingRebootReason = "edl"
                            rebootConfirmDialog.showConfirm(
                                title = edlTitle, content = edlConfirmText
                            )
                        })
                    }
                }
            }
        }

        Box {
            GlassActionButton(
                onClick = { showDropdownMoreOptions = true },
                icon = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.settings),
            )
            ProvideMenuShape(RoundedCornerShape(10.dp)) {
                DropdownMenu(expanded = showDropdownMoreOptions, onDismissRequest = {
                    showDropdownMoreOptions = false
                }) {
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
    }
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
// Status card: 运行正常 / 运行异常 + kernel patch version + system patch state
// ---------------------------------------------------------------------------

private data class StatusVisual(
    val key: String,
    val text: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
private fun StatusCard(
    kpState: APApplication.State,
    apState: APApplication.State,
    navigator: DestinationsNavigator,
) {
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

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        fillAlpha = 0.80f,
        onClick = {
            if (!isJailbreak && kpState != APApplication.State.KERNELPATCH_INSTALLED) {
                navigator.navigate(InstallModeSelectScreenDestination)
            }
        },
    ) {
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
                        .size(46.dp)
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
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = v.text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.home_kernel_patch) + "  " +
                            kernelPatchVersionText(kpState, isJailbreak),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.home_system_patch) + "  " +
                            stringResource(systemPatchStateRes(apState)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!isJailbreak &&
                        kpState != APApplication.State.UNKNOWN_STATE &&
                        kpState != APApplication.State.KERNELPATCH_NEED_UPDATE &&
                        kpState != APApplication.State.KERNELPATCH_NEED_REBOOT
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${Version.installedKPVString()} (${managerVersion.second}) - " +
                                if (apState != APApplication.State.ANDROIDPATCH_NOT_INSTALLED) "Full" else "KernelPatch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = {
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
                        Icon(Icons.Outlined.Cached, contentDescription = "busy")
                    }

                    else -> {
                        Text(text = stringResource(id = R.string.home_ap_cando_uninstall))
                    }
                }
            }

            if (kpState == APApplication.State.UNKNOWN_STATE && isPermissive) {
                Spacer(Modifier.width(8.dp))
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
                    shape = GlassShapes.Small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                ) {
                    Text(stringResource(R.string.jailbreak))
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

@Composable
private fun AStatusCard(apState: APApplication.State) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        fillAlpha = 0.80f,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(GlassShapes.Pill)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                when (apState) {
                    APApplication.State.ANDROIDPATCH_NOT_INSTALLED -> {
                        Icon(Icons.Outlined.Block, stringResource(R.string.home_not_installed))
                    }

                    APApplication.State.ANDROIDPATCH_INSTALLING -> {
                        Icon(Icons.Outlined.InstallMobile, stringResource(R.string.home_installing))
                    }

                    APApplication.State.ANDROIDPATCH_INSTALLED -> {
                        Icon(Icons.Outlined.CheckCircle, stringResource(R.string.home_working))
                    }

                    APApplication.State.ANDROIDPATCH_NEED_UPDATE -> {
                        Icon(Icons.Outlined.SystemUpdate, stringResource(R.string.home_need_update))
                    }

                    else -> {
                        Icon(
                            Icons.AutoMirrored.Outlined.HelpOutline,
                            stringResource(R.string.home_install_unknown)
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_system_patch),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                when (apState) {
                    APApplication.State.ANDROIDPATCH_NOT_INSTALLED -> {
                        Text(
                            text = stringResource(R.string.home_not_installed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    APApplication.State.ANDROIDPATCH_INSTALLING -> {
                        Text(
                            text = stringResource(R.string.home_installing),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    APApplication.State.ANDROIDPATCH_NEED_UPDATE -> {
                        Text(
                            text = stringResource(
                                R.string.apatch_version_update,
                                Version.installedApdVString,
                                managerVersion.second
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    else -> {
                        Text(
                            text = stringResource(R.string.home_system_patch_unknown),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (apState != APApplication.State.UNKNOWN_STATE) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = {
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
                    },
                    shape = GlassShapes.Small,
                ) {
                    when (apState) {
                        APApplication.State.ANDROIDPATCH_NOT_INSTALLED -> {
                            Text(text = stringResource(id = R.string.home_ap_cando_install))
                        }

                        APApplication.State.ANDROIDPATCH_NEED_UPDATE -> {
                            Text(text = stringResource(id = R.string.home_ap_cando_update))
                        }

                        APApplication.State.ANDROIDPATCH_UNINSTALLING -> {
                            Icon(Icons.Outlined.Cached, contentDescription = "busy")
                        }

                        else -> {
                            Text(text = stringResource(id = R.string.home_ap_cando_uninstall))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Greeting + device model + custom title
// ---------------------------------------------------------------------------

@Composable
private fun GreetingAndTitle() {
    HomePrefs.ensureInit()
    val hour = LocalTime.now().hour
    val greeting = greetingForHour(
        hour,
        stringResource(R.string.home_greeting_morning),
        stringResource(R.string.home_greeting_afternoon),
        stringResource(R.string.home_greeting_evening),
    )
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = Build.MODEL,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        AnimatedContent(
            targetState = HomePrefs.homeTitle.ifBlank { HomePrefs.DEFAULT_TITLE },
            transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(220)) },
            label = "homeTitle",
        ) { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Module count cards
// ---------------------------------------------------------------------------

@Composable
private fun ModuleCountRow(navigator: DestinationsNavigator, kpState: APApplication.State) {
    // Refresh counts every time the screen resumes (e.g. after installing a
    // module on another page) and when the kernel state changes.
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CountCard(
            label = stringResource(R.string.home_system_modules),
            count = sysCount.toLong(),
            modifier = Modifier.weight(1f),
            onClick = { navigator.navigate(APModuleScreenDestination) },
        )
        CountCard(
            label = stringResource(R.string.home_kernel_modules),
            count = kpmCount,
            modifier = Modifier.weight(1f),
            onClick = { navigator.navigate(KPModuleScreenDestination) },
        )
    }
}

@Composable
private fun CountCard(label: String, count: Long, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(
        modifier = modifier,
        fillAlpha = 0.80f,
        contentPadding = PaddingValues(18.dp),
        onClick = onClick,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        AnimatedContent(
            targetState = count,
            transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) },
            label = "count",
        ) { c ->
            Text(
                text = if (c >= 0) c.toString() else "—",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// su path card
// ---------------------------------------------------------------------------

@Composable
private fun SuPathCard(kpState: APApplication.State) {
    val suPath = if (kpState != APApplication.State.UNKNOWN_STATE) {
        runCatching { Natives.suPath() }.getOrDefault("—")
    } else {
        "—"
    }
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        fillAlpha = 0.80f,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(GlassShapes.Pill)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = stringResource(R.string.home_su_exec_path),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = suPath,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
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

private fun getSystemVersion(): String {
    return "${Build.VERSION.RELEASE} ${if (Build.VERSION.PREVIEW_SDK_INT != 0) "Preview" else ""} (API ${Build.VERSION.SDK_INT})"
}

private fun getDeviceInfo(): String {
    var manufacturer =
        Build.MANUFACTURER[0].uppercaseChar().toString() + Build.MANUFACTURER.substring(1)
    if (!Build.BRAND.equals(Build.MANUFACTURER, ignoreCase = true)) {
        manufacturer += " " + Build.BRAND[0].uppercaseChar() + Build.BRAND.substring(1)
    }
    manufacturer += " " + Build.MODEL + " "
    return manufacturer
}

@Composable
private fun InfoCard(kpState: APApplication.State, apState: APApplication.State) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        fillAlpha = 0.80f,
    ) {
        val uname = Os.uname()

        @Composable
        fun InfoCardItem(label: String, content: String) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (kpState != APApplication.State.UNKNOWN_STATE) {
            InfoCardItem(
                stringResource(R.string.home_kpatch_version), Version.installedKPVString()
            )

            Spacer(Modifier.height(16.dp))
            InfoCardItem(stringResource(R.string.home_su_path), Natives.suPath())

            Spacer(Modifier.height(16.dp))
        }

        if (apState != APApplication.State.UNKNOWN_STATE &&
            apState != APApplication.State.ANDROIDPATCH_NOT_INSTALLED
        ) {
            InfoCardItem(
                stringResource(R.string.home_apatch_version), managerVersion.second.toString()
            )
            Spacer(Modifier.height(16.dp))
        }

        InfoCardItem(stringResource(R.string.home_device_info), getDeviceInfo())

        Spacer(Modifier.height(16.dp))
        InfoCardItem(stringResource(R.string.home_kernel), uname.release)

        Spacer(Modifier.height(16.dp))
        InfoCardItem(stringResource(R.string.home_system_version), getSystemVersion())

        Spacer(Modifier.height(16.dp))
        InfoCardItem(stringResource(R.string.home_fingerprint), Build.FINGERPRINT)

        Spacer(Modifier.height(16.dp))
        InfoCardItem(stringResource(R.string.home_selinux_status), getSELinuxStatus())
    }
}

@Composable
fun UpdateCard() {
    val latestVersionInfo = LatestVersionInfo()
    val newVersion by produceState(initialValue = latestVersionInfo) {
        value = withContext(Dispatchers.IO) {
            checkNewVersion()
        }
    }
    val currentVersionCode = managerVersion.second
    val newVersionCode = newVersion.versionCode
    val newVersionUrl = newVersion.downloadUrl
    val changelog = newVersion.changelog

    val uriHandler = LocalUriHandler.current
    val title = stringResource(id = R.string.apm_changelog)
    val updateText = stringResource(id = R.string.apm_update)

    AnimatedVisibility(
        visible = newVersionCode > currentVersionCode,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val updateDialog = rememberConfirmDialog(onConfirm = { uriHandler.openUri(newVersionUrl) })
        ComponentWarningCard(
            message = stringResource(id = R.string.home_new_apatch_found).format(newVersionCode),
            color = MaterialTheme.colorScheme.outlineVariant,
            onClick = {
                if (changelog.isEmpty()) {
                    uriHandler.openUri(newVersionUrl)
                } else {
                    updateDialog.showConfirm(
                        title = title, content = changelog, markdown = true, confirm = updateText
                    )
                }
            }
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.home_community_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
