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

    /** #1 button press: 80 ms out, 80 ms back. */
    public val pressDurationMillis: Int = 80

    /** #2 letter key flies to the slot. */
    public val keyFlyDurationMillis: Int = 220

    /** #3 correct answer pop. */
    public val correctDurationMillis: Int = 320

    /** #4 wrong answer shake. */
    public val wrongDurationMillis: Int = 360

    /** #5 pass rotation. */
    public val passDurationMillis: Int = 260

    /** #6 wheel advance (step, no easing between slots). */
    public val wheelAdvanceDurationMillis: Int = 140

    /** #7 combo burst. */
    public val comboDurationMillis: Int = 420

    /** #10 bottom sheet slide-up and scrim. */
    public val bottomSheetDurationMillis: Int = 220

    /** #11 toast slide-down. */
    public val toastDurationMillis: Int = 180

    /** #11 toast auto-dismiss delay. */
    public val toastVisibleMillis: Int = 2500

    /** #12 screen transitions. */
    public val screenTransitionDurationMillis: Int = 220

    /** #9 splash, total in app. */
    public val splashDurationMillis: Int = 1200

    /** #11 toast slides down from the top by this distance. */
    public val toastSlideDistance: Dp = 16.dp

    /** #12 horizontal slide of screen transitions (no fade). */
    public val screenTransitionSlideDistance: Dp = 24.dp
}
