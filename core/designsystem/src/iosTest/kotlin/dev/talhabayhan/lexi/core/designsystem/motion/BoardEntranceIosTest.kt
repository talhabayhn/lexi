package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression (iPhone 13 / 17, 1.1.2): after OYNA the game stayed hidden behind / after the letter morph. The board's
 * pieces fade in under the morph's shrink ([BoardEntrance]); in 1.1.2 that fade was a `graphicsLayer` with outsets,
 * frozen at alpha 0 by Skia. Here a red "key" piece enters under a real [MorphOverlayHost] run on the iOS simulator:
 * once the morph is over it must be drawn. Also the fail-safe: a game that never gets ready ends the morph.
 */
@OptIn(ExperimentalTestApi::class)
class BoardEntranceIosTest {

    @Composable
    private fun FakeGame() {
        val entrance = rememberBoardEntrance()
        Box(Modifier.offset(200.dp, 600.dp).size(20.dp).morphAnchor(FlowMorphKeys.WHEEL_MARKER))
        Box(
            Modifier
                .offset(100.dp, 400.dp)
                .then(entrance?.piece(LexiTheme.motion.flow.boardKey, offset = 12.dp) ?: Modifier)
                .size(40.dp)
                .background(Color.Red),
        )
    }

    private fun run(markReady: Boolean): Triple<Color, Boolean, Boolean> {
        var pixel = Color.Unspecified
        var timedOut = false
        var finished = false
        runComposeUiTest {
            val state = MorphOverlayState()
            var game by mutableStateOf(false)
            setContent {
                LexiTheme(reducedMotion = false) {
                    MorphOverlayHost(
                        state = state,
                        onFinished = { finished = true },
                        onWaitTimedOut = { timedOut = true },
                    ) {
                        Box(Modifier.size(390.dp, 844.dp).background(Color.White)) {
                            if (game) {
                                // Like a NavHost destination: the screen is drawn in a layer of its own.
                                Box(Modifier.graphicsLayer {}) { FakeGame() }
                            } else {
                                Box(
                                    Modifier.offset(
                                        40.dp,
                                        700.dp,
                                    ).size(300.dp, 56.dp).morphAnchor(FlowMorphKeys.PLAY_BUTTON),
                                )
                            }
                        }
                    }
                }
            }
            waitForIdle()
            runOnIdle { assertTrue(state.start(FlowMorphKeys.PLAY_BUTTON, FlowMorphKeys.WHEEL_MARKER, "A")) }
            mainClock.advanceTimeBy(COVERED_MILLIS)
            // The game is opened under the ink, as LexiNavigator does once it covers the screen.
            game = true
            mainClock.advanceTimeBy(FRAME_MILLIS * 2)
            if (markReady) state.markReady()
            mainClock.advanceTimeBy(UNTIL_SETTLED_MILLIS)
            waitForIdle()
            val px = with(density) { 120.dp.roundToPx() }
            val py = with(density) { 420.dp.roundToPx() }
            pixel = onRoot().captureToImage().toPixelMap()[px, py]
            assertEquals(false, state.isRunning)
        }
        return Triple(pixel, timedOut, finished)
    }

    @Test
    fun theBoardIsDrawnOnceTheMorphHasLanded() {
        val (pixel, timedOut, finished) = run(markReady = true)
        assertTrue(finished, "the morph finished")
        assertTrue(!timedOut)
        assertTrue(pixel.red > 0.9f && pixel.green < 0.1f, "the entered piece is not drawn: $pixel")
    }

    @Test
    fun aGameThatNeverGetsReadyEndsTheMorphAndIsDrawn() {
        val (pixel, timedOut, finished) = run(markReady = false)
        assertTrue(timedOut, "the wait limit fired")
        assertTrue(!finished)
        assertTrue(pixel.red > 0.9f && pixel.green < 0.1f, "the board must be at rest after the fail-safe: $pixel")
    }

    private companion object {
        const val FRAME_MILLIS = 16L
        const val COVERED_MILLIS = 400L
        const val UNTIL_SETTLED_MILLIS = 6_000L
    }
}
