package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/**
 * Picks the [MotionTier] for the video flow, first match wins ([tierReason]): [forcedTier]; reduced motion ⇒
 * [MotionTier.Minimal]; Android low-RAM device ⇒ [MotionTier.Lite]; a [FrameMonitor.downgrade] ⇒ [MotionTier.Lite];
 * otherwise [MotionTier.Full]. Full is the default: only measured jank ([FrameMonitor]) lowers it, reversibly. iOS Low
 * Power Mode is deliberately not a signal (1.1.1: it kept every iPhone in Low Power on the scale + fade, with no flip,
 * although the flip runs smoothly there; `docs/design/video-flow-notes.md` § Performance). The frame rules only move
 * between Full and Lite (they never override the signals above them). One instance per app session (the monitor keeps
 * its evidence and its per-process warm-up), the OS signals are re-read on every call.
 */
@Stable
public class MotionPolicy(public val frameMonitor: FrameMonitor = FrameMonitor()) {
    /**
     * Developer override (the Android debug build's tier switch; never set in release builds): when not null it wins
     * over every signal, reduced motion included.
     */
    public var forcedTier: MotionTier? by mutableStateOf(null)

    public fun tier(signals: MotionSignals): MotionTier = forcedTier ?: when (tierReason(signals)) {
        MotionTierReason.ReducedMotion -> MotionTier.Minimal
        MotionTierReason.LowRamDevice, MotionTierReason.SlowFrames -> MotionTier.Lite
        MotionTierReason.Forced, MotionTierReason.Default -> MotionTier.Full
    }

    /** Which rule decides [tier] for [signals] (the debug tier chip shows it). */
    public fun tierReason(signals: MotionSignals): MotionTierReason = when {
        forcedTier != null -> MotionTierReason.Forced
        signals.reducedMotion -> MotionTierReason.ReducedMotion
        signals.lowRamDevice -> MotionTierReason.LowRamDevice
        frameMonitor.isDowngraded -> MotionTierReason.SlowFrames
        else -> MotionTierReason.Default
    }
}

/** The rule behind [MotionPolicy.tier], in priority order. */
public enum class MotionTierReason {
    /** [MotionPolicy.forcedTier] (debug builds only). */
    Forced,
    ReducedMotion,
    LowRamDevice,

    /** [FrameMonitor.downgrade] (details there). */
    SlowFrames,

    /** Nothing asks for less: [MotionTier.Full]. */
    Default,
}

/**
 * The app session's [MotionPolicy]. The default is one process-wide instance, so a frame downgrade seen on one screen
 * applies to every later video-flow screen until the monitor recovers; tests and previews may provide their own.
 */
public val LocalMotionPolicy: ProvidableCompositionLocal<MotionPolicy> =
    staticCompositionLocalOf { SessionMotionPolicy }

private val SessionMotionPolicy: MotionPolicy = MotionPolicy()

/** The current tier: `LexiTheme.motion.reducedMotion`, the device signals and [policy]'s frame monitor, observed. */
@Composable
public fun rememberMotionTier(policy: MotionPolicy = LocalMotionPolicy.current): MotionTier {
    FrameMonitorEffect(policy.frameMonitor)
    return policy.tier(rememberMotionSignals())
}

/** The signals [rememberMotionTier] decides on: `LexiTheme.motion.reducedMotion` and the device signals, observed. */
@Composable
public fun rememberMotionSignals(): MotionSignals {
    val reduced = LexiTheme.motion.reducedMotion
    val device = deviceMotionSignals()
    return remember(reduced, device) { device.copy(reducedMotion = reduced) }
}

/**
 * OS signals for the tier ([MotionSignals.reducedMotion] is filled in by [rememberMotionTier]). Android's low-RAM flag
 * is fixed per device and read once; iOS has none (Low Power Mode is not one, see [MotionPolicy]).
 */
@Composable
internal expect fun deviceMotionSignals(): MotionSignals
