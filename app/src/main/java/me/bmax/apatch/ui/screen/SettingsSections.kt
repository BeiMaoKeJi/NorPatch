package me.bmax.apatch.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.HomeAppearanceScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.APApplication
import me.bmax.apatch.BuildConfig
import me.bmax.apatch.Natives
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.SectionCard
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.ui.component.rememberLoadingDialog
import me.bmax.apatch.ui.theme.refreshTheme
import me.bmax.apatch.util.getBugreportFile
import me.bmax.apatch.util.getKernelVersionCode
import me.bmax.apatch.util.isGkiKernel
import me.bmax.apatch.util.isGlobalNamespaceEnabled
import me.bmax.apatch.util.outputStream
import me.bmax.apatch.util.rootShellForResult
import me.bmax.apatch.util.setGlobalNamespaceEnabled
import me.bmax.apatch.util.ui.LocalSnackbarHost
import me.bmax.apatch.util.ui.NavigationBarsSpacer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------------------------------------------------------------------------
// System features: one sub-page collecting every system-function toggle
// (global namespace mode, sucompat, SELinux hide, WebView debugging).
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SystemFeaturesScreen(navigator: DestinationsNavigator) {
    val state by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = state != APApplication.State.UNKNOWN_STATE
    val aPatchReady =
        (state == APApplication.State.ANDROIDPATCH_INSTALLING || state == APApplication.State.ANDROIDPATCH_INSTALLED || state == APApplication.State.ANDROIDPATCH_NEED_UPDATE)
    var isGlobalNamespaceEnabled by rememberSaveable {
        mutableStateOf(false)
    }
    var namespaceLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(kPatchReady && aPatchReady) {
        if (kPatchReady && aPatchReady) {
            isGlobalNamespaceEnabled = withContext(Dispatchers.IO) { isGlobalNamespaceEnabled() }
            namespaceLoaded = true
        }
    }

    val prefs = APApplication.sharedPreferences
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_group_features)) },
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
        ) {
            Spacer(Modifier.height(4.dp))
            SectionCard(stringResource(R.string.settings_group_features)) {
                var first = true
                @Composable
                fun divider() {
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                }

                // Global mount
                if (kPatchReady && aPatchReady) {
                    first = false
                    SwitchItem(
                        icon = Icons.Filled.Engineering,
                        title = stringResource(id = R.string.settings_global_namespace_mode),
                        summary = stringResource(id = R.string.settings_global_namespace_mode_summary),
                        checked = isGlobalNamespaceEnabled,
                        enabled = namespaceLoaded,
                        onCheckedChange = {
                            setGlobalNamespaceEnabled(
                                if (isGlobalNamespaceEnabled) {
                                    "0"
                                } else {
                                    "1"
                                }
                            )
                            isGlobalNamespaceEnabled = it
                        })
                }

                // Legacy sucompat (path_probe) support
                if (kPatchReady && aPatchReady) {
                    var sucompatEnabled by rememberSaveable {
                        mutableStateOf(
                            prefs.getBoolean("sucompat_enabled", false)
                        )
                    }
                    if (!first) divider()
                    first = false
                    SwitchItem(
                        icon = Icons.AutoMirrored.Filled.FeaturedPlayList,
                        title = stringResource(id = R.string.settings_sucompat),
                        summary = stringResource(id = R.string.settings_sucompat_summary),
                        checked = sucompatEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch(Dispatchers.IO) {
                                val result = if (enabled) {
                                    // Enable: create marker file and register hooks via supercall
                                    rootShellForResult("touch ${APApplication.SUCOMPAT_FILE}")
                                    Natives.controlFeature("sucompat_extra", true)
                                } else {
                                    // Disable: remove marker file and unregister hooks via supercall
                                    rootShellForResult("rm -f ${APApplication.SUCOMPAT_FILE}")
                                    Natives.controlFeature("sucompat_extra", false)
                                }
                                Log.d("SucompatToggle", "sucompat_extra ${if (enabled) "enable" else "disable"} result: $result")
                                if (result == 0L) {
                                    prefs.edit { putBoolean("sucompat_enabled", enabled) }
                                    sucompatEnabled = enabled
                                }
                            }
                        })
                }

                // Hide SELinux modification (test)
                if (kPatchReady && aPatchReady) {
                    val kernelVersion = remember { getKernelVersionCode() }
                    val kernelSupported = (kernelVersion ?: 0) >= 419
                    val isGki = remember { isGkiKernel() }
                    var selinuxHideEnabled by rememberSaveable {
                        mutableStateOf(prefs.getBoolean("selinux_hide_enabled", false))
                    }
                    val showSelinuxHideWarning = remember { mutableStateOf(false) }

                    fun applySelinuxHide(enabled: Boolean) {
                        scope.launch(Dispatchers.IO) {
                            val command = if (enabled) {
                                "touch ${APApplication.SELINUX_HIDE_FILE}"
                            } else {
                                "rm -f ${APApplication.SELINUX_HIDE_FILE}"
                            }
                            val result = rootShellForResult(command)
                            Log.d("SelinuxHideToggle", "$command result: ${result.code}")
                            if (result.isSuccess) {
                                prefs.edit { putBoolean("selinux_hide_enabled", enabled) }
                                selinuxHideEnabled = enabled
                            }
                        }
                    }

                    if (!first) divider()
                    first = false
                    SwitchItem(
                        icon = Icons.Filled.Security,
                        title = stringResource(id = R.string.settings_selinux_hide),
                        summary = stringResource(id = R.string.settings_selinux_hide_summary),
                        checked = selinuxHideEnabled,
                        enabled = kernelSupported,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // Only tested on 5.10+, and non-GKI carries a bigger risk, so warn first.
                                val below510 = (kernelVersion ?: 0) < 510
                                if (below510 || !isGki) {
                                    showSelinuxHideWarning.value = true
                                } else {
                                    applySelinuxHide(true)
                                }
                            } else {
                                applySelinuxHide(false)
                            }
                        }
                    )

                    if (showSelinuxHideWarning.value) {
                        SelinuxHideWarningDialog(
                            showDialog = showSelinuxHideWarning,
                            kernelVersion = kernelVersion,
                            isGki = isGki,
                            onConfirm = { applySelinuxHide(true) },
                        )
                    }
                }

                // WebView Debug
                if (aPatchReady) {
                    var enableWebDebugging by rememberSaveable {
                        mutableStateOf(
                            prefs.getBoolean("enable_web_debugging", false)
                        )
                    }
                    if (!first) divider()
                    SwitchItem(
                        icon = Icons.Filled.DeveloperMode,
                        title = stringResource(id = R.string.enable_web_debugging),
                        summary = stringResource(id = R.string.enable_web_debugging_summary),
                        checked = enableWebDebugging
                    ) {
                        APApplication.sharedPreferences.edit {
                            putBoolean("enable_web_debugging", it)
                        }
                        enableWebDebugging = it
                    }
                }
            }
            NavigationBarsSpacer()
        }
    }
}

// ---------------------------------------------------------------------------
// General settings sub-page: night mode, theme color, home appearance entry,
// su path reset, language (Chinese / English) and logs.
// ---------------------------------------------------------------------------

@Destination<RootGraph>
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GeneralScreen(navigator: DestinationsNavigator) {
    val state by APApplication.apStateLiveData.observeAsState(APApplication.State.UNKNOWN_STATE)
    val kPatchReady = state != APApplication.State.UNKNOWN_STATE
    val prefs = APApplication.sharedPreferences
    val snackBarHost = LocalSnackbarHost.current

    val showLanguageDialog = rememberSaveable { mutableStateOf(false) }
    LanguageDialog(showLanguageDialog)

    val showResetSuPathDialog = remember { mutableStateOf(false) }
    if (showResetSuPathDialog.value) {
        ResetSUPathDialog(showResetSuPathDialog)
    }

    val showThemeChooseDialog = remember { mutableStateOf(false) }
    if (showThemeChooseDialog.value) {
        ThemeChooseDialog(showThemeChooseDialog)
    }

    var showLogBottomSheet by remember { mutableStateOf(false) }
    val saveLog = stringResource(R.string.save_log)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val logSavedMessage = stringResource(R.string.log_saved)
    val loadingDialog = rememberLoadingDialog()
    val exportBugreportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/gzip")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                loadingDialog.show()
                uri.outputStream().use { output ->
                    getBugreportFile(context).inputStream().use {
                        it.copyTo(output)
                    }
                }
                loadingDialog.hide()
                snackBarHost.showSnackbar(message = logSavedMessage)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_group_general)) },
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
        snackbarHost = { SnackbarHost(snackBarHost) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(4.dp))
            SectionCard(stringResource(R.string.settings_group_general)) {
                var first = true
                @Composable
                fun divider() {
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )
                }

                // Night Mode Follow System
                var nightFollowSystem by rememberSaveable {
                    mutableStateOf(
                        prefs.getBoolean("night_mode_follow_sys", true)
                    )
                }
                divider()
                SwitchItem(
                    icon = Icons.Filled.InvertColors,
                    title = stringResource(id = R.string.settings_night_mode_follow_sys),
                    summary = stringResource(id = R.string.settings_night_mode_follow_sys_summary),
                    checked = nightFollowSystem
                ) {
                    prefs.edit { putBoolean("night_mode_follow_sys", it) }
                    nightFollowSystem = it
                    refreshTheme.value = true
                }

                // Custom Night Theme Switch
                if (!nightFollowSystem) {
                    var nightThemeEnabled by rememberSaveable {
                        mutableStateOf(
                            prefs.getBoolean("night_mode_enabled", false)
                        )
                    }
                    divider()
                    SwitchItem(
                        icon = Icons.Filled.DarkMode,
                        title = stringResource(id = R.string.settings_night_theme_enabled),
                        checked = nightThemeEnabled
                    ) {
                        prefs.edit { putBoolean("night_mode_enabled", it) }
                        nightThemeEnabled = it
                        refreshTheme.value = true
                    }
                }

                // System dynamic color theme
                val isDynamicColorSupport = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                if (isDynamicColorSupport) {
                    var useSystemDynamicColor by rememberSaveable {
                        mutableStateOf(
                            prefs.getBoolean("use_system_color_theme", true)
                        )
                    }
                    divider()
                    SwitchItem(
                        icon = Icons.Filled.ColorLens,
                        title = stringResource(id = R.string.settings_use_system_color_theme),
                        summary = stringResource(id = R.string.settings_use_system_color_theme_summary),
                        checked = useSystemDynamicColor
                    ) {
                        prefs.edit { putBoolean("use_system_color_theme", it) }
                        useSystemDynamicColor = it
                        refreshTheme.value = true
                    }

                    if (!useSystemDynamicColor) {
                        divider()
                        ListItem(headlineContent = {
                            Text(text = stringResource(id = R.string.settings_custom_color_theme))
                        }, modifier = Modifier.clickable {
                            showThemeChooseDialog.value = true
                        }, supportingContent = {
                            val colorMode = prefs.getString("custom_color", "norpatch")
                            Text(
                                text = stringResource(colorNameToString(colorMode.toString())),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }, leadingContent = { Icon(Icons.Filled.FormatColorFill, null) })
                    }
                } else {
                    divider()
                    ListItem(headlineContent = {
                        Text(text = stringResource(id = R.string.settings_custom_color_theme))
                    }, modifier = Modifier.clickable {
                        showThemeChooseDialog.value = true
                    }, supportingContent = {
                        val colorMode = prefs.getString("custom_color", "norpatch")
                        Text(
                            text = stringResource(colorNameToString(colorMode.toString())),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }, leadingContent = { Icon(Icons.Filled.FormatColorFill, null) })
                }

                // NorPatch: Home Appearance entry
                divider()
                ListItem(
                    leadingContent = { Icon(Icons.Filled.Wallpaper, null) },
                    headlineContent = { Text(stringResource(id = R.string.home_appearance)) },
                    supportingContent = { Text(stringResource(id = R.string.home_appearance_summary)) },
                    modifier = Modifier.clickable {
                        navigator.navigate(HomeAppearanceScreenDestination)
                    })

                // su path
                if (kPatchReady) {
                    divider()
                    ListItem(
                        leadingContent = {
                            Icon(
                                Icons.Filled.Commit, stringResource(id = R.string.setting_reset_su_path)
                            )
                        },
                        supportingContent = {},
                        headlineContent = { Text(stringResource(id = R.string.setting_reset_su_path)) },
                        modifier = Modifier.clickable {
                            showResetSuPathDialog.value = true
                        })
                }

                // language
                divider()
                ListItem(headlineContent = {
                    Text(text = stringResource(id = R.string.settings_app_language))
                }, modifier = Modifier.clickable {
                    showLanguageDialog.value = true
                }, supportingContent = {
                    Text(text = AppCompatDelegate.getApplicationLocales()[0]?.displayLanguage?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    } ?: stringResource(id = R.string.system_default),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline)
                }, leadingContent = { Icon(Icons.Filled.Translate, null) })

                // log
                divider()
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Filled.BugReport, stringResource(id = R.string.send_log)
                        )
                    },
                    headlineContent = { Text(stringResource(id = R.string.send_log)) },
                    modifier = Modifier.clickable {
                        showLogBottomSheet = true
                    })
            }

            if (showLogBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showLogBottomSheet = false },
                    contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
                    content = {
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.CenterHorizontally)

                        ) {
                            Box {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .clickable {
                                            scope.launch {
                                                val formatter =
                                                    DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm")
                                                val current = LocalDateTime.now().format(formatter)
                                                exportBugreportLauncher.launch("APatch_bugreport_${current}.tar.gz")
                                                showLogBottomSheet = false
                                            }
                                        }
                                ) {
                                    Icon(
                                        Icons.Filled.Save,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.save_log),
                                        modifier = Modifier.padding(top = 16.dp),
                                        textAlign = TextAlign.Center

                                    )
                                }

                            }
                            Box {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .clickable {
                                            scope.launch {
                                                val bugreport = loadingDialog.withLoading {
                                                    withContext(Dispatchers.IO) {
                                                        getBugreportFile(context)
                                                    }
                                                }

                                                val uri: Uri = FileProvider.getUriForFile(
                                                    context,
                                                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                                                    bugreport
                                                )

                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    setDataAndType(uri, "application/gzip")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }

                                                context.startActivity(
                                                    Intent.createChooser(
                                                        shareIntent,
                                                        saveLog
                                                    )
                                                )
                                                showLogBottomSheet = false
                                            }
                                        }) {
                                    Icon(
                                        Icons.Filled.Share,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.send_log),
                                        modifier = Modifier.padding(top = 16.dp),
                                        textAlign = TextAlign.Center

                                    )
                                }

                            }
                        }
                        NavigationBarsSpacer()
                    })
            }

            NavigationBarsSpacer()
        }
    }
}
