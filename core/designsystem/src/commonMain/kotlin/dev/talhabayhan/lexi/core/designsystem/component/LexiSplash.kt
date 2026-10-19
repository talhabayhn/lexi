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

/**
 * CSS keyframes (`at` fractions 0..1 and their values), built once: sampling them every frame allocates nothing.
 * Between two stops the value is interpolated with the easing (CSS applies the `animation-timing-function` per
 * keyframe interval); before the first / after the last stop it holds.
 */
private class Keyframes(vararg stops: Pair<Float, Float>) {
    private val at = FloatArray(stops.size) { stops[it].first }
    private val values = FloatArray(stops.size) { stops[it].second }

    /** The value at [progress]; [scale] multiplies every stop first (dp → px), as `stop(at, value.toPx())` did. */
    fun sample(progress: Float, easing: Easing, scale: Float = 1f): Float {
        if (progress <= at[0]) return values[0] * scale
        for (i in 1 until at.size) {
            if (progress <= at[i]) {
                val t = easing.transform((progress - at[i - 1]) / (at[i] - at[i - 1]))
                val from = values[i - 1] * scale
                return from + (values[i] * scale - from) * t
            }
        }
        return values[values.size - 1] * scale
    }
}

private object SplashKeyframes {
    val ShapeScale = Keyframes(0f to 0f, 0.18f to 1f)
    val TaglineAlpha = Keyframes(0f to 0f, 0.48f to 0f, 0.58f to 1f)

    /** × the tagline rise in px. */
    val TaglineRise = Keyframes(0f to 1f, 0.48f to 1f, 0.58f to 0f)

    // Block tracks in dp (× density) or degrees / scale factors.
    val L = Keyframes(
        0f to SplashDefaults.LeftStart.value,
        0.04f to SplashDefaults.LeftStart.value,
        0.20f to SplashDefaults.LeftOvershoot.value,
        0.26f to 0f,
    )
    val E = Keyframes(
        0f to SplashDefaults.TopStart.value,
        0.10f to SplashDefaults.TopStart.value,
        0.26f to SplashDefaults.TopBounce.value,
        0.32f to 0f,
    )
    val XSpin = Keyframes(
        0f to SplashDefaults.SPIN_START,
        0.16f to SplashDefaults.SPIN_START,
        0.34f to SplashDefaults.SPIN_OVERSHOOT,
        0.40f to 0f,
    )
    val XScale = Keyframes(0f to 0f, 0.16f to 0f, 0.34f to SplashDefaults.SCALE_OVERSHOOT, 0.40f to 1f)
    val I = Keyframes(
        0f to SplashDefaults.BottomStart.value,
        0.22f to SplashDefaults.BottomStart.value,
        0.40f to SplashDefaults.BottomOvershoot.value,
        0.46f to 0f,
    )
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
                    val scale = SplashKeyframes.ShapeScale.sample(progress(), mechanical)
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
                    val scale = SplashKeyframes.ShapeScale.sample(progress(), mechanical)
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
                    alpha = SplashKeyframes.TaglineAlpha.sample(p, easeOut)
                    translationY = SplashKeyframes.TaglineRise.sample(p, easeOut, SplashDefaults.TaglineRise.toPx())
                },
            )
        }
    }
}

private fun GraphicsLayerScope.animateBlock(index: Int, p: Float, easing: Easing) {
    when (index) {
        0 -> translationX = SplashKeyframes.L.sample(p, easing, density)

        1 -> translationY = SplashKeyframes.E.sample(p, easing, density)

        2 -> {
            rotationZ = SplashKeyframes.XSpin.sample(p, easing)
            val scale = SplashKeyframes.XScale.sample(p, easing)
            scaleX = scale
            scaleY = scale
        }

        else -> translationY = SplashKeyframes.I.sample(p, easing, density)
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
