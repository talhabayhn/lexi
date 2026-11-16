package dev.talhabayhan.lexi.spike

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Video-flow feasibility spike (ADR 0027): does `SharedTransitionLayout` + `Modifier.sharedElement` animate one element
 * between two Navigation Compose destinations of this repo's Compose Multiplatform version? Run on the Android host
 * (Robolectric) and on the iOS simulator by thin per-platform test classes.
 *
 * The source sits bottom-left (like the OYNA button), the target top-right (like the wheel marker). Halfway through
 * the transition the element must be drawn strictly between the two rectangles; after it, exactly on the target.
 */
@Serializable
internal data object SpikeSource

@Serializable
internal data object SpikeTarget

private const val TRANSITION_MILLIS = 400
private const val KEY = "morph"

internal class SharedElementSpikeResult(val source: Rect, val middle: Rect, val end: Rect)

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.runSharedElementSpike(): SharedElementSpikeResult {
    var nav: NavHostController? = null
    val positions = mutableMapOf<Any, Rect>()
    mainClock.autoAdvance = false
    setContent {
        val controller = rememberNavController().also { nav = it }
        SpikeHost(controller) { route, rect -> positions[route] = rect }
    }
    mainClock.advanceTimeByFrame()
    waitForIdle()
    val source = assertNotNull(positions[SpikeSource], "source never laid out")

    runOnUiThread { assertNotNull(nav).navigate(SpikeTarget) }
    // A couple of frames to start the transition, then half of it.
    mainClock.advanceTimeByFrame()
    mainClock.advanceTimeByFrame()
    mainClock.advanceTimeBy((TRANSITION_MILLIS / 2).toLong())
    val middle = assertNotNull(positions[SpikeTarget], "target never laid out during the transition")

    mainClock.advanceTimeBy(TRANSITION_MILLIS.toLong() * 2)
    waitForIdle()
    val end = assertNotNull(positions[SpikeTarget])
    return SharedElementSpikeResult(source, middle, end)
}

internal fun SharedElementSpikeResult.assertAnimatedBetweenDestinations(expectedTarget: Rect) {
    assertTrue(source.left < expectedTarget.left && source.top > expectedTarget.top, "fixture: source $source")
    assertTrue(middle.left > source.left && middle.left < expectedTarget.left, "mid x not interpolated: $middle")
    assertTrue(middle.top < source.top && middle.top > expectedTarget.top, "mid y not interpolated: $middle")
    assertTrue(middle.width < source.width && middle.width > expectedTarget.width, "mid width: $middle")
    assertTrue(end.topLeft.minus(expectedTarget.topLeft).getDistance() < 1f, "end $end != $expectedTarget")
}

@Composable
private fun SpikeHost(controller: NavHostController, onPositioned: (Any, Rect) -> Unit) {
    val spec = tween<Float>(TRANSITION_MILLIS, easing = LinearEasing)
    SharedTransitionLayout(Modifier.fillMaxSize()) {
        NavHost(
            navController = controller,
            startDestination = SpikeSource,
            enterTransition = { androidx.compose.animation.fadeIn(spec) },
            exitTransition = { androidx.compose.animation.fadeOut(spec) },
        ) {
            composable<SpikeSource> {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .offset(x = 20.dp, y = 600.dp)
                            .sharedElement(
                                rememberSharedContentState(KEY),
                                animatedVisibilityScope = this@composable,
                                boundsTransform = { _, _ -> tween(TRANSITION_MILLIS, easing = LinearEasing) },
                            )
                            .onGloballyPositioned { onPositioned(SpikeSource, it.boundsInRoot()) }
                            .size(width = 200.dp, height = 60.dp)
                            .background(Color.Red),
                    )
                }
            }
            composable<SpikeTarget> {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .offset(x = SPIKE_TARGET_X.dp, y = SPIKE_TARGET_Y.dp)
                            .sharedElement(
                                rememberSharedContentState(KEY),
                                animatedVisibilityScope = this@composable,
                                boundsTransform = { _, _ -> tween(TRANSITION_MILLIS, easing = LinearEasing) },
                            )
                            .onGloballyPositioned { onPositioned(SpikeTarget, it.boundsInRoot()) }
                            .size(SPIKE_TARGET_SIZE.dp)
                            .background(Color.Black),
                    )
                }
            }
        }
    }
}

internal const val SPIKE_TARGET_X = 300
internal const val SPIKE_TARGET_Y = 40
internal const val SPIKE_TARGET_SIZE = 36
