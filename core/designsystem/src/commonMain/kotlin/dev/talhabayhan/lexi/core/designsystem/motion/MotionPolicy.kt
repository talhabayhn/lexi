package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/**
 * Picks the [MotionTier] for the video flow: reduced motion ⇒ [MotionTier.Minimal]; Android low-RAM device, iOS Low
 * Power Mode or a slow streak seen by [frameMonitor] this session ⇒ [MotionTier.Lite]; otherwise [MotionTier.Full].
 * One instance per app session (the frame downgrade is sticky, the OS signals are re-read on every call).
 */
@Stable
public class MotionPolicy(public val frameMonitor: FrameMonitor = FrameMonitor()) {
    public fun tier(signals: MotionSignals): MotionTier = when {
        signals.reducedMotion -> MotionTier.Minimal
        signals.lowRamDevice || signals.lowPowerMode || frameMonitor.slowStreakDetected -> MotionTier.Lite
        else -> MotionTier.Full
    }
}

/**
 * The app session's [MotionPolicy]. The default is one process-wide instance, so a slow streak seen on one screen
 * keeps every later video-flow screen on [MotionTier.Lite]; tests and previews may provide their own.
 */
public val LocalMotionPolicy: ProvidableCompositionLocal<MotionPolicy> =
    staticCompositionLocalOf { SessionMotionPolicy }

private val SessionMotionPolicy: MotionPolicy = MotionPolicy()

/** The current tier: `LexiTheme.motion.reducedMotion`, the device signals and [policy]'s frame monitor, observed. */
@Composable
public fun rememberMotionTier(policy: MotionPolicy = LocalMotionPolicy.current): MotionTier {
    FrameMonitorEffect(policy.frameMonitor)
    val reduced = LexiTheme.motion.reducedMotion
    val device = deviceMotionSignals()
    val signals = remember(reduced, device) { device.copy(reducedMotion = reduced) }
    return policy.tier(signals)
}

/**
 * OS signals for the tier ([MotionSignals.reducedMotion] is filled in by [rememberMotionTier]). iOS Low Power Mode is
 * observed; Android's low-RAM flag is fixed per device and read once.
 */
@Composable
internal expect fun deviceMotionSignals(): MotionSignals
