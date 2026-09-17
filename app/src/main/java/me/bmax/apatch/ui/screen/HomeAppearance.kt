package me.bmax.apatch.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bmax.apatch.R
import me.bmax.apatch.ui.component.DrawerIconButton
import me.bmax.apatch.ui.component.GlassCard
import me.bmax.apatch.ui.component.GlassShapes
import me.bmax.apatch.ui.component.HomeBackgroundImage
import me.bmax.apatch.ui.component.HomePrefs
import me.bmax.apatch.ui.component.SwitchItem
import me.bmax.apatch.util.ui.NavigationBarsSpacer
import java.io.File
import java.time.LocalTime
import kotlin.math.roundToInt

/** Shared greeting logic used by the homepage and the appearance preview. */
internal fun greetingForHour(hour: Int, morning: String, afternoon: String, evening: String): String =
    when (hour) {
        in 5..11 -> morning
        in 12..17 -> afternoon
        else -> evening
    }

@Destination<RootGraph>
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppearanceScreen() {
    HomePrefs.ensureInit()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showResetDialog = remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    val target = File(context.filesDir, "home_bg_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    withContext(Dispatchers.Main) {
                        HomePrefs.setBackgroundImagePath(target.absolutePath)
                    }
                }.onFailure { e ->
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to import image: ${e.message}",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_appearance)) },
                navigationIcon = { DrawerIconButton() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ---- Live preview -------------------------------------------------
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.home_appearance_preview),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(GlassShapes.Card),
                ) {
                    HomeBackgroundImage(Modifier.fillMaxSize())
                    GlassCard(
                        modifier = Modifier.fillMaxSize(),
                        fillAlpha = 0.78f,
                        contentPadding = PaddingValues(18.dp),
                    ) {
                        val hour = LocalTime.now().hour
                        Text(
                            text = greetingForHour(
                                hour,
                                stringResource(R.string.home_greeting_morning),
                                stringResource(R.string.home_greeting_afternoon),
                                stringResource(R.string.home_greeting_evening),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = HomePrefs.homeTitle.ifBlank { HomePrefs.DEFAULT_TITLE },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PreviewChip(stringResource(R.string.home_system_modules))
                            PreviewChip(stringResource(R.string.home_kernel_modules))
                        }
                    }
                }
            }

            // ---- Background group --------------------------------------------
            Column(Modifier.padding(horizontal = 16.dp)) {
                GlassCard(fillAlpha = 0.80f) {
                    SwitchItem(
                        icon = Icons.Filled.Wallpaper,
                        title = stringResource(R.string.home_bg_enabled),
                        summary = stringResource(R.string.home_bg_enabled_summary),
                        checked = HomePrefs.bgEnabled,
                        onCheckedChange = { HomePrefs.setBackgroundEnabled(it) },
                    )

                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Image, null) },
                        headlineContent = { Text(stringResource(R.string.home_bg_pick)) },
                        supportingContent = { Text(stringResource(R.string.home_bg_pick_summary)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickImageLauncher.launch("image/*") },
                    )

                    if (HomePrefs.backgroundFile() != null) {
                        ListItem(
                            leadingContent = { Icon(Icons.Filled.Delete, null) },
                            headlineContent = { Text(stringResource(R.string.home_bg_remove)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { HomePrefs.setBackgroundImagePath("") },
                        )
                    }

                    AppearanceSliderItem(
                        title = stringResource(R.string.home_bg_alpha),
                        value = HomePrefs.bgAlpha,
                        onValueChange = { HomePrefs.setAlpha(it) },
                    )
                    AppearanceSliderItem(
                        title = stringResource(R.string.home_bg_blur),
                        value = HomePrefs.bgBlur,
                        onValueChange = { HomePrefs.setBlur(it) },
                    )
                }
            }

            // ---- Homepage title ----------------------------------------------
            Column(Modifier.padding(horizontal = 16.dp)) {
                GlassCard(fillAlpha = 0.80f) {
                    Text(
                        text = stringResource(R.string.home_title_label),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = HomePrefs.homeTitle,
                        onValueChange = { HomePrefs.setTitle(it) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.home_title_summary),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ---- Reset --------------------------------------------------------
            Column(Modifier.padding(horizontal = 16.dp)) {
                Button(
                    onClick = { showResetDialog.value = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    shape = GlassShapes.Small,
                ) {
                    Icon(Icons.Filled.RestartAlt, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.home_appearance_reset))
                }
            }

            NavigationBarsSpacer()
        }
    }

    if (showResetDialog.value) {
        ResetAppearanceDialog(showDialog = showResetDialog, onConfirm = { HomePrefs.reset() })
    }
}

@Composable
private fun PreviewChip(label: String) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = if (isDark) 0.14f else 0.55f))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppearanceSliderItem(title: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$value%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 0f..100f,
        )
    }
}

@Composable
private fun ResetAppearanceDialog(
    showDialog: MutableState<Boolean>,
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = { showDialog.value = false }) {
        Surface(
            modifier = Modifier.width(310.dp),
            shape = RoundedCornerShape(30.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.home_appearance_reset),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.home_appearance_reset_confirm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        showDialog.value = false
                        onConfirm()
                    }) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}
