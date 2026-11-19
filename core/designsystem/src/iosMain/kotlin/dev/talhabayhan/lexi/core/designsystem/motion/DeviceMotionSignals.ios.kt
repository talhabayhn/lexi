package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Composable

/**
 * iOS has no device signal for the tier: Low Power Mode is deliberately not one (`MotionPolicy`), so an iPhone starts in
 * Full and only measured jank lowers it.
 */
@Composable
internal actual fun deviceMotionSignals(): MotionSignals = IosSignals

private val IosSignals = MotionSignals(reducedMotion = false, lowRamDevice = false)
