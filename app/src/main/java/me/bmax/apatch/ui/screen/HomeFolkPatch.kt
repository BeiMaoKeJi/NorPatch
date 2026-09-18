package me.bmax.apatch.ui.screen

import android.os.Build
import android.system.Os
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.outlined.InstallMobile
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.generated.destinations.AboutScreenDestination
import com.ramcosta.composedestinations.generated.destinations.InstallModeSelectScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.bmax.apatch.APApplication
import me.bmax.apatch.Natives
import me.bmax.apatch.R
import me.bmax.apatch.util.Version
import me.bmax.apatch.util.Version.getManagerVersion
import me.bmax.apatch.util.getSELinuxStatus
import me.bmax.apatch.util.reboot

/**
 * FolkPatch home, ported nearly verbatim from the FolkPatch source (Home.kt:
 * HomeScreenV1 + KStatusCard + AStatusCard + ListInfoCard + LearnMoreCard).
 * Only UI-layer; the kernel-patch / su / module logic below is untouched.
 * BackgroundConfig / jailbreak / music extensions are intentionally dropped.
 */
/** System-patch status card (FolkPatch AStatusCard). */
@Composable
fun AStatusCardFP(apState: APApplication.State) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row {
                Text(
                    text = stringResource(R.string.android_patch),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (apState) {
                    APApplication.State.ANDROIDPATCH_NOT_INSTALLED ->
                        Icon(Icons.Outlined.Block, stringResource(R.string.home_not_installed))

                    APApplication.State.ANDROIDPATCH_INSTALLING ->
                        Icon(Icons.Outlined.InstallMobile, stringResource(R.string.home_installing))

                    APApplication.State.ANDROIDPATCH_INSTALLED ->
                        Icon(Icons.Filled.CheckCircle, stringResource(R.string.home_working))

                    APApplication.State.ANDROIDPATCH_NEED_UPDATE ->
                        Icon(Icons.Outlined.SystemUpdate, stringResource(R.string.home_kp_need_update))

                    else ->
                        Icon(
                            Icons.AutoMirrored.Outlined.HelpOutline,
                            stringResource(R.string.home_install_unknown),
                        )
                }
                Column(
                    Modifier
                        .weight(2f)
                        .padding(start = 16.dp),
                ) {
                    when (apState) {
                        APApplication.State.ANDROIDPATCH_NOT_INSTALLED ->
                            Text(stringResource(R.string.home_not_installed), style = MaterialTheme.typography.titleMedium)

                        APApplication.State.ANDROIDPATCH_INSTALLING ->
                            Text(stringResource(R.string.home_installing), style = MaterialTheme.typography.titleMedium)

                        APApplication.State.ANDROIDPATCH_INSTALLED ->
                            Text(stringResource(R.string.home_working), style = MaterialTheme.typography.titleMedium)

                        APApplication.State.ANDROIDPATCH_NEED_UPDATE ->
                            Text(stringResource(R.string.home_kp_need_update), style = MaterialTheme.typography.titleMedium)

                        else ->
                            Text(stringResource(R.string.home_install_unknown), style = MaterialTheme.typography.titleMedium)
                    }
                }
                if (apState != APApplication.State.UNKNOWN_STATE) {
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Button(onClick = {
                            when (apState) {
                                APApplication.State.ANDROIDPATCH_NOT_INSTALLED ->
                                    APApplication.installApatch()

                                APApplication.State.ANDROIDPATCH_UNINSTALLING -> {
                                    // Do nothing
                                }

                                APApplication.State.ANDROIDPATCH_NEED_UPDATE ->
                                    APApplication.installApatch()

                                else ->
                                    APApplication.uninstallApatch()
                            }
                        }, content = {
                            when (apState) {
                                APApplication.State.ANDROIDPATCH_NOT_INSTALLED ->
                                    Text(text = stringResource(id = R.string.home_ap_cando_install))

                                APApplication.State.ANDROIDPATCH_UNINSTALLING ->
                                    Icon(Icons.Outlined.Cached, contentDescription = "busy")

                                APApplication.State.ANDROIDPATCH_NEED_UPDATE ->
                                    Text(text = stringResource(id = R.string.home_kp_cando_update))

                                else ->
                                    Text(text = stringResource(id = R.string.home_ap_cando_uninstall))
                            }
                        })
                    }
                }
            }
        }
    }
}

/** Device / system info list (FolkPatch ListInfoCard). */
@Composable
fun ListInfoCardFP(kpState: APApplication.State, apState: APApplication.State) {
    val suPath = remember { Natives.suPath() }
    val uname = Os.uname()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
        ) {
            @Composable
            fun InfoCardItem(label: String, content: String) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    Text(text = content, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (kpState != APApplication.State.UNKNOWN_STATE) {
                InfoCardItem(stringResource(R.string.home_kpatch_version), Version.installedKPVString())
                Spacer(Modifier.height(16.dp))
            }

            if (kpState != APApplication.State.UNKNOWN_STATE) {
                InfoCardItem(stringResource(R.string.home_su_path), suPath)
                Spacer(Modifier.height(16.dp))
            }

            if (apState != APApplication.State.UNKNOWN_STATE &&
                apState != APApplication.State.ANDROIDPATCH_NOT_INSTALLED
            ) {
                InfoCardItem(stringResource(R.string.home_apatch_version), getManagerVersion().second.toString())
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
}

/** "Learn more" card (FolkPatch LearnMoreCard), navigates to the About page. */
@Composable
fun LearnMoreCardFP(navigator: DestinationsNavigator) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navigator.navigate(AboutScreenDestination)
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_learn_apatch),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_click_to_learn_apatch),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}