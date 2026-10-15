package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Motion tokens (`docs/design/motion.md`): short, mechanical movements, 80–420 ms, no soft fades for state changes.
 *
 * When [reducedMotion] is true (Android "remove animations", iOS "Reduce Motion") only color/shape changes are
 * allowed: no translation, scale or rotation.
 */
@Immutable
public class LexiMotion internal constructor(
    /** System reduced-motion setting, or the override passed to `LexiTheme`. */
    public val reducedMotion: Boolean,
) {
    /** (0.2, 0.8, 0.2, 1): splash, general enter. */
    public val mechanical: Easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

    /** (0.3, 0.7, 0.2, 1): key → slot. */
    public val fly: Easing = CubicBezierEasing(0.3f, 0.7f, 0.2f, 1f)

    /** (0.3, 1.6, 0.5, 1), overshoots: correct pop, combo badge. */
    public val pop: Easing = CubicBezierEasing(0.3f, 1.6f, 0.5f, 1f)

    /** (0.4, 0, 0.2, 1): pass rotation. */
    public val turn: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    /** Timer bar. */
    public val linear: Easing = LinearEasing

    /** CSS `ease-out` (0, 0, 0.58, 1): button press, `.lx-press` in `source/15-motion.dc.html`. */
    public val press: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    /** CSS `ease-out` (0, 0, 0.58, 1): #3 ring (`aRing`) and #7 burst shapes (`aB1`–`aB4`). */
    public val easeOut: Easing = press

    /** #1 button press: 80 ms out, 80 ms back. */
    public val pressDurationMillis: Int = 80

    /** #2 letter key flies to the slot. */
    public val keyFlyDurationMillis: Int = 220

    /** #2 scale of the flying letter when it reaches the slot. */
    public val keyFlyPeakScale: Float = 1.15f

    /**
     * #2 part of [keyFlyDurationMillis] spent travelling (scale 1 → [keyFlyPeakScale]); the rest settles the scale back
     * to 1 (`aFly`: travel 20 % → 45 %, settle 45 % → 55 % of the demo loop).
     */
    public val keyFlyTravelFraction: Float = 5f / 7f

    /** #3 correct answer: pop and ring together (the ring ends last). */
    public val correctDurationMillis: Int = 320

    /** #3 peak chip scale (1 → 1.45 → 1). */
    public val correctPopScale: Float = 1.45f

    /**
     * #3 keyframes as parts of [correctDurationMillis] (`aPop` / `aRing`: start 30 %, peak 40 %, pop back 50 %, ring
     * gone 60 % of the demo loop): pop and ring peak at 1/3, the pop is back to 1 at 2/3, the ring fades out at the end.
     */
    public val correctPeakFraction: Float = 1f / 3f
    public val correctPopEndFraction: Float = 2f / 3f

    /** #3 ring scale at start, peak and end (0.6 → 1 → 1.8); its opacity goes 0 → 1 → 0. */
    public val correctRingStartScale: Float = 0.6f
    public val correctRingEndScale: Float = 1.8f

    /** #4 wrong answer shake. */
    public val wrongDurationMillis: Int = 360

    /** #4 wrong answer: horizontal offsets of the answer slots, evenly spaced over [wrongDurationMillis]. */
    public val wrongShakeOffsets: List<Dp> = listOf((-12).dp, 12.dp, (-9).dp, 9.dp, (-4).dp, 0.dp)

    /** #5 pass rotation, morph and arrow. */
    public val passDurationMillis: Int = 260

    /** #5 the chip shape turns clockwise by this much while it morphs circle → leaf. */
    public val passRotationDegrees: Float = 90f

    /** #5 the arrow slides this far (to the next letter, clockwise along the wheel). */
    public val passArrowDistance: Dp = 70.dp

    /** #5 part of [passDurationMillis] at which the arrow is fully visible (`aArrow`: 0 → 1 → 0 opacity). */
    public val passArrowPeakFraction: Float = 2f / 7f

    /** #6 wheel advance (step, no easing between slots). */
    public val wheelAdvanceDurationMillis: Int = 140

    /** #7 combo: burst of the four shapes and pop of the badge. */
    public val comboDurationMillis: Int = 420

    /** #7 the badge stays at rest this long after its pop (`aCombo` 40 % → 85 % of the 1.6 s demo). */
    public val comboBadgeHoldMillis: Int = 720

    /** #7 the badge shrinks to 0 in this time (`aCombo` 85 % → 100 %). */
    public val comboBadgeExitMillis: Int = 240

    /** #7 badge pop: scale 0 → 1.3 → 1 with rotation −20° → 6° → −4°, peak at [comboBadgePeakFraction]. */
    public val comboBadgePeakScale: Float = 1.3f
    public val comboBadgeStartRotation: Float = -20f
    public val comboBadgePeakRotation: Float = 6f
    public val comboBadgeRestRotation: Float = -4f
    public val comboBadgePeakFraction: Float = 0.6f

    /** #7 burst: the shapes reach their middle keyframe here (`aB1`–`aB4`: 20 % → 60 % → 100 %) and fade after it. */
    public val comboBurstMidFraction: Float = 0.5f

    /** #10 bottom sheet slide-up and scrim. */
    public val bottomSheetDurationMillis: Int = 220

    /** #11 toast slide-down. */
    public val toastDurationMillis: Int = 180

    /** #11 toast auto-dismiss delay. */
    public val toastVisibleMillis: Int = 2500

    /** #12 screen transitions. */
    public val screenTransitionDurationMillis: Int = 220

    /** #8 time warning: one counter pulse 1 → 1.12 → 1, played each second (with the tick) while the warning lasts. */
    public val timeWarningPulseMillis: Int = 600

    /** #8 time warning: peak counter scale. */
    public val timeWarningPulseScale: Float = 1.12f

    /** #9 splash, total in app. */
    public val splashDurationMillis: Int = 1200

    /** #9 splash with reduced motion: the logo is shown statically for this long before the app moves on. */
    public val splashReducedHoldMillis: Int = 600

    /** #11 toast slides down from the top by this distance. */
    public val toastSlideDistance: Dp = 16.dp

    /** #12 horizontal slide of screen transitions (no fade). */
    public val screenTransitionSlideDistance: Dp = 24.dp
}
