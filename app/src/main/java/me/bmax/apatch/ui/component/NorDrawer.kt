package me.bmax.apatch.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import me.bmax.apatch.R
import me.bmax.apatch.ui.screen.BottomBarDestination

/**
 * NorPatch left navigation drawer (UI layer only).
 *
 * The drawer keeps the exact navigation semantics of the original bottom bar:
 * same destinations, same popUpTo/saveState/restoreState behavior, same
 * kPatchRequired/aPatchRequired visibility filtering.
 */

class DrawerController(
    val open: () -> Unit,
    val close: () -> Unit,
)

val LocalDrawerController = staticCompositionLocalOf { DrawerController({}, {}) }

/** Floating glass hamburger button used by every main screen top bar. */
@Composable
fun DrawerIconButton(modifier: Modifier = Modifier) {
    val controller = LocalDrawerController.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressScaleModifier(interactionSource)
            .size(42.dp)
            .clip(GlassShapes.Pill)
            .background(
                (if (isDark) Color(0xFF241A20) else Color.White).copy(alpha = 0.72f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = controller.open,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Menu,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun NorDrawerContent(
    navController: NavHostController,
    visibleDestinations: Set<BottomBarDestination>,
    onClose: () -> Unit,
) {
    val navigator = navController.rememberDestinationsNavigator()

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "nP",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.about_powered_by, "KernelPatch"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
        }

        visibleDestinations.forEach { destination ->
            val selected by navController.isRouteOnBackStackAsState(destination.direction)
            val interactionSource = remember { MutableInteractionSource() }
            NavigationDrawerItem(
                selected = selected,
                onClick = {
                    if (selected) {
                        navigator.popBackStack(destination.direction, false)
                    } else {
                        navigator.navigate(destination.direction) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    onClose()
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.iconSelected else destination.iconNotSelected,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(destination.label)) },
                shape = RoundedCornerShape(20.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    unselectedContainerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .pressScaleModifier(interactionSource),
            )
        }

        Spacer(Modifier.weight(1f))
    }
}
