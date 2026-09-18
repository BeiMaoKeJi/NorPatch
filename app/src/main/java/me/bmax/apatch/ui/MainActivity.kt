package me.bmax.apatch.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import kotlinx.coroutines.launch
import me.bmax.apatch.util.ui.glassDockEffect
import me.bmax.apatch.APApplication
import me.bmax.apatch.ui.component.HomeBackgroundLayer
import me.bmax.apatch.ui.navigation.BottomBar
import me.bmax.apatch.ui.screen.BottomBarDestination
import me.bmax.apatch.util.ui.navBarLiquefiable
import me.bmax.apatch.util.ui.rememberNavBarGlassLiquidState
import me.bmax.apatch.ui.theme.APatchTheme
import me.bmax.apatch.ui.viewmodel.SuperUserViewModel
import me.bmax.apatch.util.ui.LocalSnackbarHost

class MainActivity : AppCompatActivity() {

    private var isLoading = true

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen().setKeepOnScreenCondition { isLoading }

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

        setContent {
            APatchTheme {
                val navController = rememberNavController()
                val snackBarHostState = remember { SnackbarHostState() }
                val configuration = LocalConfiguration.current
                val bottomBarRoutes = remember {
                    BottomBarDestination.entries.map { it.direction.route }.toSet()
                }
                val state by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
                val kPatchReady = state != APApplication.State.UNKNOWN_STATE
                val aPatchReady = state == APApplication.State.ANDROIDPATCH_INSTALLED
                val visibleDestinations = remember(state) {
                    BottomBarDestination.entries.filter { destination ->
                        !(destination.kPatchRequired && !kPatchReady) && !(destination.aPatchRequired && !aPatchReady)
                    }.toSet()
                }
                val navigator = navController.rememberDestinationsNavigator()
                // Bottom Dock: Home | Kernel Modules | Superuser | System
                // Modules | Settings. Superuser is hidden entirely when root is
                // unavailable (no placeholder, no gray-out); the other four
                // entries are always present.
                val bottomNavDestinations = remember(state) {
                    buildList {
                        add(BottomBarDestination.Home)
                        add(BottomBarDestination.KModule)
                        if (kPatchReady && aPatchReady) {
                            add(BottomBarDestination.SuperUser)
                        }
                        add(BottomBarDestination.AModule)
                        add(BottomBarDestination.Settings)
                    }
                }

                val defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                    // Unified smooth transitions: fade + gentle scale + a small
                    // vertical drift for every page change 鈥?no slide jank.
                    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                        {
                            fadeIn(animationSpec = tween(340)) +
                                scaleIn(initialScale = 0.97f, animationSpec = tween(340)) +
                                slideInVertically(tween(340)) { it / 24 }
                        }

                    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                        {
                            fadeOut(animationSpec = tween(300))
                        }

                    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                        {
                            fadeIn(animationSpec = tween(340)) +
                                scaleIn(initialScale = 0.97f, animationSpec = tween(340))
                        }

                    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                        {
                            fadeOut(animationSpec = tween(300)) +
                                scaleOut(targetScale = 0.97f, animationSpec = tween(300)) +
                                slideOutVertically(tween(300)) { -it / 24 }
                        }
                }

                // FolkPatch-style frosted dock: real-time backdrop blur via liquid.
                val floatingLiquidState = rememberNavBarGlassLiquidState()
                // FP-style dock auto-hide: scroll down collapses the dock, scroll up brings it back.
                var dockVisible by remember { mutableStateOf(true) }
                val dockScrollConnection = remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                            dockVisible = available.y >= -1f
                            return Offset.Zero
                        }
                    }
                }

                                LaunchedEffect(Unit) {
                    if (SuperUserViewModel.apps.isEmpty()) {
                        SuperUserViewModel().fetchAppList()
                    }
                }

                CompositionLocalProvider(
                    LocalSnackbarHost provides snackBarHostState,
                ) {
                    HomeBackgroundLayer {
                        Scaffold(
                            containerColor = Color.Transparent,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            ) { innerPadding ->
                                if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                    Row(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))) {
                                        SideBar(
                                            navController = navController,
                                            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                                            visibleDestinations = visibleDestinations
                                        )
                                        DestinationsNavHost(
                                            modifier = Modifier
                                                .weight(1f)
                                                .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start)),
                                            navGraph = NavGraphs.root,
                                            navController = navController,
                                            engine = rememberNavHostEngine(navHostContentAlignment = Alignment.TopCenter),
                                            defaultTransitions = defaultTransitions
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                        DestinationsNavHost(
                                            modifier = Modifier.fillMaxSize()
                                                .nestedScroll(dockScrollConnection).navBarLiquefiable(floatingLiquidState)
                                                .consumeWindowInsets(innerPadding),
                                            navGraph = NavGraphs.root,
                                            navController = navController,
                                            engine = rememberNavHostEngine(navHostContentAlignment = Alignment.TopCenter),
                                            defaultTransitions = defaultTransitions
                                        )
                                        AnimatedVisibility(
                                            visible = dockVisible,
                                            modifier = Modifier.align(Alignment.BottomCenter),
                                            enter = slideInVertically(tween(260)) { it },
                                            exit = slideOutVertically(tween(260)) { it },
                                        ) {
                                            BottomBar(
                                                navController = navController,
                                                isFloating = true,
                                                liquidState = floatingLiquidState,
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            }

        SingletonImageLoader.setSafe(
            SingletonImageLoader.Factory { context ->
                ImageLoader.Builder(context)
                    .components {
                        add(AppIconKeyer())
                        add(AppIconFetcher.Factory(context))
                    }
                    .build()
            }
        )

        isLoading = false
    }
}

/** Left navigation rail shown on landscape / large screens (kept from APatch). */
@Composable
private fun SideBar(navController: NavHostController, modifier: Modifier = Modifier, visibleDestinations: Set<BottomBarDestination>) {
    val navigator = navController.rememberDestinationsNavigator()

    Crossfade(
        targetState = visibleDestinations,
        label = "SideBarStateCrossfade"
    ) { visibleDestinations ->
        NavigationRail(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                visibleDestinations.forEach { destination ->
                    val isCurrentDestOnBackStack by navController.isRouteOnBackStackAsState(destination.direction)
                    NavigationRailItem(
                        selected = isCurrentDestOnBackStack,
                        onClick = {
                            if (isCurrentDestOnBackStack) {
                                navigator.popBackStack(destination.direction, false)
                            }
                            navigator.navigate(destination.direction) {
                                popUpTo(NavGraphs.root) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (isCurrentDestOnBackStack) {
                                Icon(destination.iconSelected, stringResource(destination.label))
                            } else {
                                Icon(destination.iconNotSelected, stringResource(destination.label))
                            }
                        },
                        label = { Text(stringResource(destination.label), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        alwaysShowLabel = true,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
}
