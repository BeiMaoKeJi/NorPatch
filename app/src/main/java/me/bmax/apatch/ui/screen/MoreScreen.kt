package me.bmax.apatch.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.SuperUserScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import me.bmax.apatch.APApplication
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.GlassShapes
import me.bmax.apatch.ui.component.HomePrefs
import me.bmax.apatch.ui.component.SectionCard
import me.bmax.apatch.util.ui.NavigationBarsSpacer

/** NorPatch community QQ group; users copy the number and join in QQ manually. */
private const val QQ_GROUP_NUMBER = "1121505516"

/** "More" tab: only the Join-community section (shown/hidden by the Home
 *  Appearance switch). Pure UI layer. */
@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navigator: DestinationsNavigator) {
    HomePrefs.ensureInit()
    val context = LocalContext.current
    val copiedMsg = stringResource(R.string.home_community_copied)
    val state by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = state != APApplication.State.UNKNOWN_STATE
    val aPatchReady =
        (state == APApplication.State.ANDROIDPATCH_INSTALLING || state == APApplication.State.ANDROIDPATCH_INSTALLED || state == APApplication.State.ANDROIDPATCH_NEED_UPDATE)

    fun copyCommunityGroup() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("NorPatch QQ Group", QQ_GROUP_NUMBER))
        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more), fontWeight = FontWeight.SemiBold) },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))

            // Superuser: root permission manager; hidden entirely without root.
            if (kPatchReady && aPatchReady) {
                SectionCard(stringResource(R.string.settings_group_features)) {
                    MoreEntry(
                        icon = { Icon(Icons.Filled.Security, null, tint = MaterialTheme.colorScheme.primary) },
                        title = stringResource(R.string.su_title),
                        onClick = { navigator.navigate(SuperUserScreenDestination) },
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            // Community: controlled by the "Join community entry" switch on the
            // Home Appearance page. When off, the whole section disappears.
            AnimatedVisibility(
                visible = HomePrefs.communityEnabled,
                enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.97f, animationSpec = tween(300)),
                exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.98f, animationSpec = tween(220)),
            ) {
                Column {
                    SectionCard(stringResource(R.string.home_community_title)) {
                        MoreEntry(
                            icon = { Icon(Icons.Filled.Forum, null, tint = MaterialTheme.colorScheme.tertiary) },
                            title = stringResource(R.string.home_community_group),
                            subtitle = stringResource(R.string.home_community_hint),
                            onClick = { copyCommunityGroup() },
                        )
                    }
                }
            }

            NavigationBarsSpacer()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MoreEntry(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(GlassShapes.Small)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) { icon() }
        },
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = {
            Icon(
                Icons.Filled.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}
