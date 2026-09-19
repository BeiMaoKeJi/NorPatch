package me.bmax.apatch.ui.screen

import android.os.Build
import android.system.Os
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.ramcosta.composedestinations.generated.destinations.AboutScreenDestination
import com.ramcosta.composedestinations.generated.destinations.InstallModeSelectScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import me.bmax.apatch.ui.theme.BackgroundConfig
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.request.crossfade
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.copyableInfo
import me.bmax.apatch.Natives
import me.bmax.apatch.util.SystemInfoCollector
import me.bmax.apatch.util.getSELinuxStatus
import me.bmax.apatch.util.Version
import me.bmax.apatch.util.Version.getManagerVersion
import me.bmax.apatch.util.ui.HomeBottomSpacer

private val managerVersion = getManagerVersion()

/**
 * FP Dashboard UI home 闁?ported from FolkPatch HomeScreenV4 (the
 * "dashboard_ui" layout): breathing-gradient hero card with version columns,
 * system info list, device status rings and storage progress bars.
 * UI-layer only; kernel/su/module logic below untouched.
 */
@Composable
fun HomeDashboardV4(
    innerPadding: PaddingValues,
    navigator: DestinationsNavigator,
    kpState: APApplication.State,
    apState: APApplication.State,
) {
    val showUninstallDialog = remember { mutableStateOf(false) }
    val showInstallDialog = remember { mutableStateOf(false) }

    if (showUninstallDialog.value) {
        UninstallDialog(showDialog = showUninstallDialog, navigator)
    }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(0.dp))

        HeroStatusCardV4(
            kpState = kpState,
            apState = apState,
            navigator = navigator,
            showUninstallDialog = showUninstallDialog,
            showInstallDialog = showInstallDialog,
        )

        AnimatedVisibility(
            visible = kpState != APApplication.State.UNKNOWN_STATE &&
                apState != APApplication.State.UNKNOWN_STATE &&
                apState != APApplication.State.ANDROIDPATCH_INSTALLED,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            AndroidPatchCardV4(apState = apState)
        }

        SystemInfoCardV4(kpState = kpState, apState = apState)
        DeviceStatusCardV4()
        StorageInfoCardV4()
        LearnMoreCardV4(navigator)

        HomeBottomSpacer()
    }
}

/** Hero status card 闁?breathing gradient when working, install prompt otherwise. */
@Composable
private fun HeroStatusCardV4(
    kpState: APApplication.State,
    apState: APApplication.State,
    navigator: DestinationsNavigator,
    showUninstallDialog: MutableState<Boolean>,
    showInstallDialog: MutableState<Boolean>,
) {
    val isWorking = kpState == APApplication.State.KERNELPATCH_INSTALLED
    val isUpdate = kpState == APApplication.State.KERNELPATCH_NEED_UPDATE ||
        kpState == APApplication.State.KERNELPATCH_NEED_REBOOT
    val isUnknown = kpState == APApplication.State.UNKNOWN_STATE

    // FolkPatch-style dashboard hero wallpaper
    val hasWallpaper = BackgroundConfig.isDashboardCardBackgroundEnabled &&
        !BackgroundConfig.dashboardCardBgUri.isNullOrEmpty()

    // Breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathAlpha",
    )

    // Smooth color transition
    val containerColor by animateColorAsState(
        targetValue = when {
            isWorking -> MaterialTheme.colorScheme.primary
            isUpdate -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.errorContainer
        },
        animationSpec = tween(500),
        label = "containerColor",
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            hasWallpaper && (isWorking) -> Color.White
            isWorking -> MaterialTheme.colorScheme.onPrimary
            isUpdate -> MaterialTheme.colorScheme.onSecondary
            else -> MaterialTheme.colorScheme.onErrorContainer
        },
        animationSpec = tween(500),
        label = "contentColor",
    )

    // Gradient background
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            containerColor.copy(alpha = if (isWorking) breathAlpha else 1f),
            containerColor.copy(alpha = 0.8f),
        ),
    )

    val isFull = apState == APApplication.State.ANDROIDPATCH_INSTALLED
    val modeText = if (isFull) "Full" else "Half"

    if (isWorking) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!hasWallpaper) Modifier.background(gradientBrush) else Modifier),
            ) {
                if (hasWallpaper) {
                AsyncImage(
                    model = BackgroundConfig.dashboardCardBgUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(BackgroundConfig.dashboardCardBgOpacity),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = BackgroundConfig.dashboardCardBgDim)),
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = contentColor,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.home_working),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(Modifier.height(4.dp))
                                ModeLabelChipV4(label = modeText, contentColor = contentColor)
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { showUninstallDialog.value = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = contentColor,
                            ),
                            border = BorderStroke(1.dp, contentColor.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.home_ap_cando_uninstall),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(
                        color = contentColor.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        VersionInfoColumnV4(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.home_kpatch_version),
                            value = Version.installedKPVString(),
                            contentColor = contentColor,
                        )
                        VersionInfoColumnV4(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.home_apatch_version),
                            value = managerVersion.second.toString(),
                            contentColor = contentColor,
                        )
                        VersionInfoColumnV4(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.home_selinux_status),
                            value = getSELinuxStatus(),
                            contentColor = contentColor,
                        )
                    }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navigator.navigate(InstallModeSelectScreenDestination)
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    isUpdate -> Icon(
                        imageVector = Icons.Outlined.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    isUnknown -> Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                    else -> Icon(
                        imageVector = Icons.Outlined.Block,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(Modifier.width(20.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = when {
                            isUpdate -> stringResource(R.string.home_kp_need_update)
                            isUnknown -> stringResource(R.string.home_install_unknown)
                            else -> stringResource(R.string.home_not_installed)
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_click_to_install),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

/** Android patch status card. */
@Composable
private fun AndroidPatchCardV4(apState: APApplication.State) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (apState) {
                APApplication.State.ANDROIDPATCH_INSTALLED -> Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                APApplication.State.ANDROIDPATCH_INSTALLING -> CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                APApplication.State.ANDROIDPATCH_NEED_UPDATE -> Icon(
                    imageVector = Icons.Outlined.SystemUpdate,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                else -> Icon(
                    imageVector = Icons.Outlined.Android,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.android_patch),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            FilledTonalButton(
                onClick = {
                    when (apState) {
                        APApplication.State.ANDROIDPATCH_NOT_INSTALLED,
                        APApplication.State.ANDROIDPATCH_NEED_UPDATE -> APApplication.installApatch()
                        APApplication.State.ANDROIDPATCH_INSTALLED -> APApplication.uninstallApatch()
                        else -> {}
                    }
                },
                enabled = apState != APApplication.State.ANDROIDPATCH_INSTALLING &&
                    apState != APApplication.State.ANDROIDPATCH_UNINSTALLING &&
                    apState != APApplication.State.UNKNOWN_STATE,
            ) {
                when (apState) {
                    APApplication.State.ANDROIDPATCH_NOT_INSTALLED ->
                        Text(stringResource(R.string.home_ap_cando_install))
                    APApplication.State.ANDROIDPATCH_NEED_UPDATE ->
                        Text(stringResource(R.string.home_kp_cando_update))
                    APApplication.State.ANDROIDPATCH_INSTALLING,
                    APApplication.State.ANDROIDPATCH_UNINSTALLING ->
                        Icon(Icons.Outlined.Cached, contentDescription = "busy")
                    else ->
                        Text(stringResource(R.string.home_ap_cando_uninstall))
                }
            }
        }
    }
}

/** System info card 闁?icon rows, label above value, long-press to copy. */
@Composable
private fun SystemInfoCardV4(
    kpState: APApplication.State,
    apState: APApplication.State,
) {
    val uname = Os.uname()
    val suPath = remember { Natives.suPath() }

    TonalCardV4 {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_kpatch_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            InfoItemV4(Icons.Outlined.PhoneAndroid, stringResource(R.string.home_device_info), getDeviceInfo())
            if (kpState != APApplication.State.UNKNOWN_STATE) {
                InfoItemV4(Icons.Outlined.Extension, stringResource(R.string.home_kpatch_version), Version.installedKPVString())
                InfoItemV4(Icons.Outlined.Code, stringResource(R.string.home_su_path), suPath)
            }
            if (apState == APApplication.State.ANDROIDPATCH_INSTALLED) {
                InfoItemV4(Icons.Outlined.Android, stringResource(R.string.home_apatch_version), managerVersion.second.toString())
            }
            InfoItemV4(Icons.Outlined.DeveloperBoard, stringResource(R.string.home_kernel), uname.release)
            InfoItemV4(Icons.Outlined.Info, stringResource(R.string.home_system_version), getSystemVersion())
            InfoItemV4(Icons.Outlined.Fingerprint, stringResource(R.string.home_fingerprint), Build.FINGERPRINT)
            InfoItemV4(Icons.Outlined.Shield, stringResource(R.string.home_selinux_status), getSELinuxStatus())
        }
    }
}

/** Icon info row, label above value, long-press to copy. */
@Composable
private fun InfoItemV4(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .copyableInfo(label, value)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Device status card 闁?battery temp / CPU load / battery level rings. */
@Composable
private fun DeviceStatusCardV4() {
    val context = LocalContext.current
    var deviceStatus by remember { mutableStateOf(SystemInfoCollector.DeviceStatus()) }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            deviceStatus = SystemInfoCollector.collectDeviceStatus(context)
            delay(10000)
        }
    }

    TonalCardV4 {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_device_status_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatusCircleV4(
                    value = "${deviceStatus.batteryTemp}閹虹煰",
                    label = stringResource(R.string.home_device_status_battery_temp),
                    progress = (deviceStatus.batteryTemp / 50f).coerceIn(0f, 1f),
                    color = MaterialTheme.colorScheme.primary,
                )
                StatusCircleV4(
                    value = "${deviceStatus.cpuUsage}%",
                    label = stringResource(R.string.home_device_status_cpu_load),
                    progress = (deviceStatus.cpuUsage / 100f).coerceIn(0f, 1f),
                    color = MaterialTheme.colorScheme.secondary,
                )
                StatusCircleV4(
                    value = "${deviceStatus.batteryLevel}%",
                    label = stringResource(R.string.home_device_status_battery_level),
                    progress = (deviceStatus.batteryLevel / 100f).coerceIn(0f, 1f),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

/** Double-ring status circle. */
@Composable
private fun StatusCircleV4(
    value: String,
    label: String,
    progress: Float,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(80.dp),
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = color.copy(alpha = 0.2f),
                strokeWidth = 8.dp,
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 8.dp,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Storage info card 闁?internal storage and RAM progress bars. */
@Composable
private fun StorageInfoCardV4() {
    var storageStatus by remember { mutableStateOf(SystemInfoCollector.StorageStatus()) }

    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            storageStatus = SystemInfoCollector.collectStorageStatus()
            delay(5000)
        }
    }

    TonalCardV4 {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.SdStorage,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_storage_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            StorageProgressBarV4(
                label = stringResource(R.string.home_storage_internal),
                used = storageStatus.storageUsed,
                total = storageStatus.storageTotal,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(12.dp))

            StorageProgressBarV4(
                label = stringResource(R.string.home_storage_ram),
                used = storageStatus.ramUsed,
                total = storageStatus.ramTotal,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/** Labeled progress bar with used / total sizes. */
@Composable
private fun StorageProgressBarV4(
    label: String,
    used: Long,
    total: Long,
    color: Color,
) {
    val context = LocalContext.current
    val progress = if (total > 0) used.toFloat() / total.toFloat() else 0f
    val usedStr = android.text.format.Formatter.formatFileSize(context, used)
    val totalStr = android.text.format.Formatter.formatFileSize(context, total)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "$usedStr / $totalStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}

/** Learn more card 闁?opens the About screen. */
@Composable
private fun LearnMoreCardV4(navigator: DestinationsNavigator) {
    TonalCardV4(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navigator.navigate(AboutScreenDestination)
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.home_learn_apatch),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.home_click_to_learn_apatch),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Tonal card 闁?20dp rounded, light elevation container. */
@Composable
private fun TonalCardV4(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(content = content)
    }
}

/** Mode label chip (Full / Half). */
@Composable
private fun ModeLabelChipV4(label: String, contentColor: Color) {
    Surface(
        color = contentColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
    }
}

/** Version info column used in the hero card. */
@Composable
private fun VersionInfoColumnV4(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    contentColor: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
