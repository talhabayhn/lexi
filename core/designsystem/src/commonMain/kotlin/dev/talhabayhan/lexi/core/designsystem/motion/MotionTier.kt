package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Immutable

/**
 * How much of the video-flow choreography a device plays (`docs/design/video-flow-notes.md` § Performance).
 * Durations never change between tiers; only the cost of what is drawn does.
 */
public enum class MotionTier {
    /** Everything in the catalogue: lift, Y flips, letter morph, per-item staggers, in-game bursts. */
    Full,

    /**
     * Same timing and structure, cheaper drawing: no tilt or shadow growth, flips become scale 0.92 → 1 + fade, the
     * morph keeps the ink rectangle but drops the big letter's pop, staggered groups enter together.
     */
    Lite,

    /** Reduced motion: every flow transition is one 120 ms cross-fade, nothing moves. */
    Minimal,
}

/** Inputs of [MotionPolicy] that come from the OS. */
@Immutable
public data class MotionSignals(
    /** Android "remove animations" / iOS Reduce Motion (or `LexiTheme(reducedMotion = …)`). */
    val reducedMotion: Boolean,
    /** Android `ActivityManager.isLowRamDevice`; always false on iOS. */
    val lowRamDevice: Boolean,
)
