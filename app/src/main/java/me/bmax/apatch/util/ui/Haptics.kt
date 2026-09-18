package me.bmax.apatch.util.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Lightweight Compose-native haptic helpers — no third-party libraries.
 *
 * Chosen feedback types (stable across Android 8–16):
 *  - [HapticFeedbackType.TextHandleMove]: a short, subtle tick, ideal for light
 *    taps on key actions (install entry, copy, navigation) without feeling buzzy.
 *  - [HapticFeedbackType.LongPress]: a slightly stronger confirmation pulse,
 *    used right after a destructive or long-running action is triggered
 *    (update / uninstall) so the user feels the state change even without
 *    looking at the screen.
 */
object Haptics {

    /** Short subtle tick for light taps on key actions. */
    fun tick(h: HapticFeedback) {
        h.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /** Stronger confirmation pulse for update / uninstall / reboot triggers. */
    fun confirm(h: HapticFeedback) {
        h.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

/** Convenience accessor for the current haptic feedback performer. */
@Composable
fun rememberHaptics(): HapticFeedback = LocalHapticFeedback.current
