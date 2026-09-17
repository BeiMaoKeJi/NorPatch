package me.bmax.apatch.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * NorPatch default theme — a soft Material Design 3 palette inspired by the
 * FolkPatch "二次元" beautified look: gentle rose/pastel tones, clean warm
 * whites in light mode, deep plum in dark mode. UI layer only.
 */

// ---------------- Light ----------------
private val nor_light_primary = Color(0xFFD15B7E)
private val nor_light_onPrimary = Color(0xFFFFFFFF)
private val nor_light_primaryContainer = Color(0xFFFFD9E2)
private val nor_light_onPrimaryContainer = Color(0xFF3F0019)
private val nor_light_secondary = Color(0xFF8E5967)
private val nor_light_onSecondary = Color(0xFFFFFFFF)
private val nor_light_secondaryContainer = Color(0xFFFFD9E4)
private val nor_light_onSecondaryContainer = Color(0xFF390B1C)
private val nor_light_tertiary = Color(0xFF7C5A91)
private val nor_light_onTertiary = Color(0xFFFFFFFF)
private val nor_light_tertiaryContainer = Color(0xFFF4DAFF)
private val nor_light_onTertiaryContainer = Color(0xFF341843)
private val nor_light_error = Color(0xFFBA1A1A)
private val nor_light_onError = Color(0xFFFFFFFF)
private val nor_light_errorContainer = Color(0xFFFFDAD6)
private val nor_light_onErrorContainer = Color(0xFF410002)
private val nor_light_background = Color(0xFFFFF8F9)
private val nor_light_onBackground = Color(0xFF211A1D)
private val nor_light_surface = Color(0xFFFFF8F9)
private val nor_light_onSurface = Color(0xFF211A1D)
private val nor_light_surfaceVariant = Color(0xFFF2DDE2)
private val nor_light_onSurfaceVariant = Color(0xFF514348)
private val nor_light_outline = Color(0xFF847479)
private val nor_light_outlineVariant = Color(0xFFD5C1C7)
private val nor_light_inverseOnSurface = Color(0xFFFFEDF0)
private val nor_light_inverseSurface = Color(0xFF372E31)
private val nor_light_inversePrimary = Color(0xFFFFB1C8)
private val nor_light_shadow = Color(0xFF000000)
private val nor_light_surfaceTint = Color(0xFFD15B7E)
private val nor_light_scrim = Color(0xFF000000)

// ---------------- Dark ----------------
private val nor_dark_primary = Color(0xFFFFB1C8)
private val nor_dark_onPrimary = Color(0xFF5E1133)
private val nor_dark_primaryContainer = Color(0xFF7D2B4C)
private val nor_dark_onPrimaryContainer = Color(0xFFFFD9E2)
private val nor_dark_secondary = Color(0xFFE8BDC8)
private val nor_dark_onSecondary = Color(0xFF472530)
private val nor_dark_secondaryContainer = Color(0xFF603B46)
private val nor_dark_onSecondaryContainer = Color(0xFFFFD9E4)
private val nor_dark_tertiary = Color(0xFFD7BAEE)
private val nor_dark_onTertiary = Color(0xFF2A0D39)
private val nor_dark_tertiaryContainer = Color(0xFF432659)
private val nor_dark_onTertiaryContainer = Color(0xFFF4DAFF)
private val nor_dark_error = Color(0xFFFFB4AB)
private val nor_dark_onError = Color(0xFF690005)
private val nor_dark_errorContainer = Color(0xFF93000A)
private val nor_dark_onErrorContainer = Color(0xFFFFDAD6)
private val nor_dark_background = Color(0xFF191114)
private val nor_dark_onBackground = Color(0xFFF0DEE2)
private val nor_dark_surface = Color(0xFF191114)
private val nor_dark_onSurface = Color(0xFFF0DEE2)
private val nor_dark_surfaceVariant = Color(0xFF514348)
private val nor_dark_onSurfaceVariant = Color(0xFFD5C1C7)
private val nor_dark_outline = Color(0xFF9E8B90)
private val nor_dark_outlineVariant = Color(0xFF514348)
private val nor_dark_inverseOnSurface = Color(0xFF372E31)
private val nor_dark_inverseSurface = Color(0xFFF0DEE2)
private val nor_dark_inversePrimary = Color(0xFFD15B7E)
private val nor_dark_shadow = Color(0xFF000000)
private val nor_dark_surfaceTint = Color(0xFFFFB1C8)
private val nor_dark_scrim = Color(0xFF000000)

val LightNorPatchTheme = lightColorScheme(
    primary = nor_light_primary,
    onPrimary = nor_light_onPrimary,
    primaryContainer = nor_light_primaryContainer,
    onPrimaryContainer = nor_light_onPrimaryContainer,
    secondary = nor_light_secondary,
    onSecondary = nor_light_onSecondary,
    secondaryContainer = nor_light_secondaryContainer,
    onSecondaryContainer = nor_light_onSecondaryContainer,
    tertiary = nor_light_tertiary,
    onTertiary = nor_light_onTertiary,
    tertiaryContainer = nor_light_tertiaryContainer,
    onTertiaryContainer = nor_light_onTertiaryContainer,
    error = nor_light_error,
    onError = nor_light_onError,
    errorContainer = nor_light_errorContainer,
    onErrorContainer = nor_light_onErrorContainer,
    background = nor_light_background,
    onBackground = nor_light_onBackground,
    surface = nor_light_surface,
    onSurface = nor_light_onSurface,
    surfaceVariant = nor_light_surfaceVariant,
    onSurfaceVariant = nor_light_onSurfaceVariant,
    outline = nor_light_outline,
    inverseOnSurface = nor_light_inverseOnSurface,
    inverseSurface = nor_light_inverseSurface,
    inversePrimary = nor_light_inversePrimary,
    surfaceTint = nor_light_surfaceTint,
    outlineVariant = nor_light_outlineVariant,
    scrim = nor_light_scrim,
)

val DarkNorPatchTheme = darkColorScheme(
    primary = nor_dark_primary,
    onPrimary = nor_dark_onPrimary,
    primaryContainer = nor_dark_primaryContainer,
    onPrimaryContainer = nor_dark_onPrimaryContainer,
    secondary = nor_dark_secondary,
    onSecondary = nor_dark_onSecondary,
    secondaryContainer = nor_dark_secondaryContainer,
    onSecondaryContainer = nor_dark_onSecondaryContainer,
    tertiary = nor_dark_tertiary,
    onTertiary = nor_dark_onTertiary,
    tertiaryContainer = nor_dark_tertiaryContainer,
    onTertiaryContainer = nor_dark_onTertiaryContainer,
    error = nor_dark_error,
    onError = nor_dark_onError,
    errorContainer = nor_dark_errorContainer,
    onErrorContainer = nor_dark_onErrorContainer,
    background = nor_dark_background,
    onBackground = nor_dark_onBackground,
    surface = nor_dark_surface,
    onSurface = nor_dark_onSurface,
    surfaceVariant = nor_dark_surfaceVariant,
    onSurfaceVariant = nor_dark_onSurfaceVariant,
    outline = nor_dark_outline,
    inverseOnSurface = nor_dark_inverseOnSurface,
    inverseSurface = nor_dark_inverseSurface,
    inversePrimary = nor_dark_inversePrimary,
    surfaceTint = nor_dark_surfaceTint,
    outlineVariant = nor_dark_outlineVariant,
    scrim = nor_dark_scrim,
)
