package me.bmax.apatch.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.generated.destinations.InstallModeSelectScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.bmax.apatch.ui.theme.BackgroundConfig
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.crossfade
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.util.Version
import me.bmax.apatch.util.Version.getManagerVersion
import me.bmax.apatch.util.ui.HomeBottomSpacer

private val managerVersion = getManagerVersion()

/**
 * FP Gridu UI home 闂?ported from FolkPatch HomeScreenV2 (the "GridUI"
 * layout): big status card on the left, two small info cards (kernel patch /
 * system patch) stacked on the right, then the AP card, device info list and
 * the learn-more card. UI-layer only; kernel/su/module logic below untouched.
 */
@Composable
fun HomeGridu(
    innerPadding: PaddingValues,
    navigator: DestinationsNavigator,
    kpState: APApplication.State,
    apState: APApplication.State,
) {
    val showUninstallDialog = remember { mutableStateOf(false) }

    if (showUninstallDialog.value) {
        UninstallDialog(showDialog = showUninstallDialog, navigator)
    }

    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(0.dp))

        // Top section: big status card (left) + two small cards (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusCardBigGridu(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                kpState = kpState,
                apState = apState,
                onClick = {
                    navigator.navigate(InstallModeSelectScreenDestination)
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SmallInfoCardGridu(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.kernel_patch),
                    value = if (kpState != APApplication.State.UNKNOWN_STATE) {
                        "${Version.installedKPVString()} (${managerVersion.second})"
                    } else {
                        "N/A"
                    },
                    icon = Icons.Outlined.Extension,
                    onClick = {
                        if (kpState == APApplication.State.KERNELPATCH_NEED_UPDATE) {
                            navigator.navigate(InstallModeSelectScreenDestination)
                        }
                    },
                )

                SmallInfoCardGridu(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.android_patch),
                    value = when (apState) {
                        APApplication.State.ANDROIDPATCH_INSTALLED -> "Active"
                        APApplication.State.ANDROIDPATCH_NEED_UPDATE -> "Update"
                        APApplication.State.ANDROIDPATCH_INSTALLING -> "..."
                        else -> "Inactive"
                    },
                    icon = Icons.Outlined.Android,
                    onClick = {
                        if (apState == APApplication.State.ANDROIDPATCH_INSTALLED) {
                            showUninstallDialog.value = true
                        } else if (apState != APApplication.State.ANDROIDPATCH_NOT_INSTALLED &&
                            kpState == APApplication.State.KERNELPATCH_INSTALLED
                        ) {
                            APApplication.installApatch()
                        }
                    },
                )
            }
        }

        // Android patch card (only when not installed)
        if (kpState != APApplication.State.UNKNOWN_STATE &&
            apState != APApplication.State.UNKNOWN_STATE &&
            apState != APApplication.State.ANDROIDPATCH_INSTALLED
        ) {
            AStatusCardFP(apState)
        }

        // Device info list (label above value)
        ListInfoCardFP(kpState, apState)

        // Learn more
        LearnMoreCardFP(navigator)

        HomeBottomSpacer()
    }
    }
}

/** Big status card: state text bottom-start, large check/warning icon top-end. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusCardBigGridu(
    modifier: Modifier = Modifier,
    kpState: APApplication.State,
    apState: APApplication.State,
    onClick: () -> Unit,
) {
    val isWorking = kpState == APApplication.State.KERNELPATCH_INSTALLED
    val isUpdate = kpState == APApplication.State.KERNELPATCH_NEED_UPDATE ||
        kpState == APApplication.State.KERNELPATCH_NEED_REBOOT

    // FolkPatch-style working card wallpaper
    val hasGridBg = BackgroundConfig.isGridWorkingCardBackgroundEnabled &&
        !BackgroundConfig.gridWorkingCardBackgroundUri.isNullOrEmpty()

    val containerColor = when {
        hasGridBg -> Color.Transparent
        isWorking -> MaterialTheme.colorScheme.primary
        isUpdate -> MaterialTheme.colorScheme.surfaceContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = when {
        hasGridBg -> Color.White
        isWorking -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            if (hasGridBg) {
                AsyncImage(
                    model = BackgroundConfig.gridWorkingCardBackgroundUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(BackgroundConfig.gridWorkingCardBackgroundOpacity),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = BackgroundConfig.gridWorkingCardBackgroundDim)),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Column {
                    if (!BackgroundConfig.isGridWorkingCardTextHidden) {
                        Text(
                            text = when {
                                kpState == APApplication.State.KERNELPATCH_INSTALLED ->
                                    stringResource(R.string.home_working)
                                kpState == APApplication.State.KERNELPATCH_NEED_UPDATE ->
                                    stringResource(R.string.home_kp_need_update)
                                kpState == APApplication.State.KERNELPATCH_NEED_REBOOT ->
                                    stringResource(R.string.home_ap_cando_reboot)
                                kpState == APApplication.State.UNKNOWN_STATE ->
                                    stringResource(R.string.home_install_unknown)
                                else ->
                                    stringResource(R.string.home_not_installed)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        )
                    }
                    if (isWorking && !BackgroundConfig.isGridWorkingCardModeHidden) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (apState == APApplication.State.ANDROIDPATCH_INSTALLED) "<Full>" else "<Half>",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.8f),
                        )
                    }
                }
            }

            if (!BackgroundConfig.isGridWorkingCardCheckHidden) {
                Icon(
                    imageVector = when {
                        isWorking -> Icons.Filled.CheckCircle
                        else -> Icons.Filled.Warning
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(48.dp),
                    tint = contentColor,
                )
            }
        }
    }
}

/** Small info card used in the top-right stack. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallInfoCardGridu(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
