package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.animation.core.Easing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/**
 * Splash geometry and keyframes from `14-splash` (`source/14-splash.dc.html`, `docs/design/motion.md` #9). The demo
 * runs 3.6 s; the keyframe fractions are the same in the 1.2 s in-app version (`LexiMotion.splashDurationMillis`).
 * Distances are the CSS pixels of the keyframes. The demo's fade-out at 90–100 % is not part of the app: the screen
 * navigates when the animation ends.
 */
public object SplashDefaults {
    /** Yellow circle top-left (`left/top: -120`) and blue square bottom-right (`right/bottom: -90`). */
    internal val SunSize: Dp = 320.dp
    internal val SunOffset: Dp = (-120).dp
    internal val BlueSize: Dp = 260.dp
    internal val BlueOffset: Dp = (-90).dp

    /** Gap between the logo and the tagline. */
    internal val ContentGap: Dp = 22.dp

    /** Progress at which the L, E, X and I land: a light haptic and a "tık" each (motion.md #9). */
    public val LandingFractions: List<Float> = listOf(0.26f, 0.32f, 0.40f, 0.46f)

    internal val LeftStart = (-320).dp
    internal val LeftOvershoot = 8.dp
    internal val TopStart = (-480).dp
    internal val TopBounce = 10.dp
    internal val BottomStart = 420.dp
    internal val BottomOvershoot = (-8).dp
    internal val TaglineRise = 12.dp
    internal const val SPIN_START = -270f
    internal const val SPIN_OVERSHOOT = 10f
    internal const val SCALE_OVERSHOOT = 1.08f
}

private class Stop(val at: Float, val value: Float)

private fun stop(at: Float, value: Float) = Stop(at, value)

/**
 * CSS keyframes at [progress] (0..1): between two stops the value is interpolated with [easing] (CSS applies the
 * `animation-timing-function` per keyframe interval); before the first / after the last stop it holds.
 */
private fun sample(progress: Float, easing: Easing, vararg stops: Stop): Float {
    val first = stops.first()
    if (progress <= first.at) return first.value
    for (i in 1 until stops.size) {
        val from = stops[i - 1]
        val to = stops[i]
        if (progress <= to.at) {
            val t = easing.transform((progress - from.at) / (to.at - from.at))
            return from.value + (to.value - from.value) * t
        }
    }
    return stops.last().value
}

/**
 * Splash (`14-splash`): cream screen, yellow circle and blue square scaling in from their corners, the four logo blocks
 * arriving one by one (L slides from the left, E drops and bounces, X spins in, I rises) and the tagline fading up.
 * [progress] is the animation position 0..1, read in the draw phase only; pass a constant `1f` for reduced motion
 * (the logo is shown statically, motion.md). [tagline] is "A'DAN Z'YE İNGİLİZCE".
 */
@Composable
public fun LexiSplash(progress: () -> Float, tagline: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val mechanical = LexiTheme.motion.mechanical
    val easeOut = LexiTheme.motion.press
    Box(modifier.fillMaxSize().background(colors.cream)) {
        DecorShape(
            kind = DecorShapeKind.Circle,
            color = colors.yellow,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(SplashDefaults.SunOffset, SplashDefaults.SunOffset)
                .size(SplashDefaults.SunSize)
                .graphicsLayer {
                    val scale = sample(progress(), mechanical, stop(0f, 0f), stop(0.18f, 1f))
                    scaleX = scale
                    scaleY = scale
                },
        )
        DecorShape(
            kind = DecorShapeKind.Square,
            color = colors.blue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(-SplashDefaults.BlueOffset, -SplashDefaults.BlueOffset)
                .size(SplashDefaults.BlueSize)
                .graphicsLayer {
                    val scale = sample(progress(), mechanical, stop(0f, 0f), stop(0.18f, 1f))
                    scaleX = scale
                    scaleY = scale
                },
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(SplashDefaults.ContentGap, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LexiLogo(size = LexiLogoSize.Splash) { index -> animateBlock(index, progress(), mechanical) }
            Text(
                text = tagline,
                style = LexiTheme.typography.label15Wide,
                modifier = Modifier.graphicsLayer {
                    val p = progress()
                    alpha = sample(p, easeOut, stop(0f, 0f), stop(0.48f, 0f), stop(0.58f, 1f))
                    translationY = sample(
                        p,
                        easeOut,
                        stop(0f, SplashDefaults.TaglineRise.toPx()),
                        stop(0.48f, SplashDefaults.TaglineRise.toPx()),
                        stop(0.58f, 0f),
                    )
                },
            )
        }
    }
}

private fun GraphicsLayerScope.animateBlock(index: Int, p: Float, easing: Easing) {
    when (index) {
        0 -> translationX = sample(
            p,
            easing,
            stop(0f, SplashDefaults.LeftStart.toPx()),
            stop(0.04f, SplashDefaults.LeftStart.toPx()),
            stop(0.20f, SplashDefaults.LeftOvershoot.toPx()),
            stop(0.26f, 0f),
        )

        1 -> translationY = sample(
            p,
            easing,
            stop(0f, SplashDefaults.TopStart.toPx()),
            stop(0.10f, SplashDefaults.TopStart.toPx()),
            stop(0.26f, SplashDefaults.TopBounce.toPx()),
            stop(0.32f, 0f),
        )

        2 -> {
            rotationZ = sample(
                p,
                easing,
                stop(0f, SplashDefaults.SPIN_START),
                stop(0.16f, SplashDefaults.SPIN_START),
                stop(0.34f, SplashDefaults.SPIN_OVERSHOOT),
                stop(0.40f, 0f),
            )
            val scale = sample(
                p,
                easing,
                stop(0f, 0f),
                stop(0.16f, 0f),
                stop(0.34f, SplashDefaults.SCALE_OVERSHOOT),
                stop(0.40f, 1f),
            )
            scaleX = scale
            scaleY = scale
        }

        else -> translationY = sample(
            p,
            easing,
            stop(0f, SplashDefaults.BottomStart.toPx()),
            stop(0.22f, SplashDefaults.BottomStart.toPx()),
            stop(0.40f, SplashDefaults.BottomOvershoot.toPx()),
            stop(0.46f, 0f),
        )
    }
}

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LexiSplashPreview() {
    LexiTheme(reducedMotion = false) {
        LexiSplash(progress = { 1f }, tagline = "A'DAN Z'YE İNGİLİZCE")
    }
}
