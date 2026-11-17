package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * #16 host behaviour on the frame clock: the shrink waits for ready + target, the frame monitor watches only the
 * moving parts, the content is never recomposed by the morph, touches are swallowed, and leaving mid-morph resets it.
 * Writes from the test thread reach the recomposer only through `Snapshot.sendApplyNotifications()` (no auto-advance).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class MorphOverlayHostTest {
    private val flow = LexiMotion(reducedMotion = false).flow

    private fun ComposeUiTest.startMorph(state: MorphOverlayState) {
        assertTrue(runOnUiThread { state.start(sourceKey = "play", targetKey = "marker", letter = "A") })
        Snapshot.sendApplyNotifications()
    }

    @Test
    fun `the morph holds until the game is ready and finishes after the shrink`() = runComposeUiTest {
        val state = MorphOverlayState()
        val monitor = FrameMonitor()
        var finished = 0
        var contentCompositions = 0
        mainClock.autoAdvance = false
        setContent {
            LexiTheme(reducedMotion = false) {
                MorphOverlayHost(state, frameMonitor = monitor, onFinished = { finished++ }) {
                    SideEffect { contentCompositions++ }
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.offset(20.dp, 700.dp).size(350.dp, 80.dp).morphAnchor("play"))
                        Box(Modifier.offset(177.dp, 120.dp).size(36.dp).morphAnchor("marker"))
                    }
                }
            }
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        val compositionsBefore = contentCompositions

        startMorph(state)
        assertFalse(runOnUiThread { state.start("play", "marker", "B") }, "a second start is ignored")
        mainClock.advanceTimeBy(flow.inkExpand.delayMillis + 100L)
        assertTrue(monitor.isWatching, "the expand is measured")

        mainClock.advanceTimeBy(flow.inkShrink.endMillis + 500L)
        assertTrue(state.isRunning, "must hold the full screen until ready")
        assertFalse(monitor.isWatching, "the hold is not measured")

        runOnUiThread { state.markReady() }
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(100L)
        assertTrue(monitor.isWatching, "the shrink is measured")
        mainClock.advanceTimeBy(flow.inkShrink.durationMillis.toLong())
        assertTrue(state.isRunning, "the ink has landed but the board is still coming in")
        assertEquals(0, finished)
        mainClock.advanceTimeBy(flow.boardSettledMillis.toLong())
        waitForIdle()

        assertFalse(state.isRunning)
        assertEquals(1, finished, "the clock may start once the board has landed")
        assertFalse(monitor.isWatching)
        assertEquals(compositionsBefore, contentCompositions, "the morph must not recompose the content")
    }

    @Test
    fun `touches under the ink are swallowed while the morph runs`() = runComposeUiTest {
        val state = MorphOverlayState()
        var clicks = 0
        setContent {
            LexiTheme(reducedMotion = false) {
                MorphOverlayHost(state) {
                    Box(Modifier.fillMaxSize()) {
                        Box(
                            Modifier.offset(20.dp, 700.dp).size(350.dp, 80.dp).morphAnchor("play")
                                .testTag("play").clickable { clicks++ },
                        )
                    }
                }
            }
        }
        onNodeWithTag("play").performClick()
        assertEquals(1, clicks)

        mainClock.autoAdvance = false
        startMorph(state)
        mainClock.advanceTimeBy(300L)
        // Hidden from screen readers under the ink (cleared semantics); the touch itself is swallowed.
        onNodeWithTag("play").assertDoesNotExist()
        onRoot().performTouchInput { click(Offset(195f, 740f)) }

        assertEquals(1, clicks)
    }

    @Test
    fun `leaving the composition mid-morph resets the state without finishing`() = runComposeUiTest {
        val state = MorphOverlayState()
        var shown by mutableStateOf(true)
        var finished = 0
        mainClock.autoAdvance = false
        setContent {
            LexiTheme(reducedMotion = false) {
                if (shown) {
                    MorphOverlayHost(state, onFinished = { finished++ }) {
                        Box(Modifier.offset(20.dp, 700.dp).size(350.dp, 80.dp).morphAnchor("play"))
                    }
                }
            }
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        startMorph(state)
        mainClock.advanceTimeBy(300L)

        shown = false
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(100L)
        waitForIdle()

        assertFalse(state.isRunning)
        assertEquals(0, finished)
    }

    @Test
    fun `changing an anchor key republishes its rectangle without a new layout`() = runComposeUiTest {
        val state = MorphOverlayState()
        var key by mutableStateOf("old")
        setContent {
            LexiTheme(reducedMotion = false) {
                MorphOverlayHost(state) {
                    Box(Modifier.offset(10.dp, 20.dp).size(30.dp).morphAnchor(key))
                }
            }
        }
        waitForIdle()
        assertEquals(Rect(10f, 20f, 40f, 50f), state.anchors["old"])

        key = "new"
        waitForIdle()

        assertNull(state.anchors["old"])
        assertEquals(Rect(10f, 20f, 40f, 50f), state.anchors["new"])
    }

    @Test
    fun `a start without a laid out source does nothing`() = runComposeUiTest {
        val state = MorphOverlayState()
        setContent { LexiTheme(reducedMotion = false) { MorphOverlayHost(state) {} } }

        assertFalse(runOnUiThread { state.start("missing", "marker", "A") })
        assertFalse(state.isRunning)
    }

    private fun ComposeUiTest.hostWithAnchors(
        state: MorphOverlayState,
        onFinished: () -> Unit = {},
        onCancelled: () -> Unit = {},
    ) {
        mainClock.autoAdvance = false
        setContent {
            LexiTheme(reducedMotion = false) {
                MorphOverlayHost(state, onFinished = onFinished, onCancelled = onCancelled) {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.offset(20.dp, 700.dp).size(350.dp, 80.dp).morphAnchor("play"))
                        Box(Modifier.offset(177.dp, 120.dp).size(36.dp).morphAnchor("marker"))
                    }
                }
            }
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()
    }

    @Test
    fun `a cancel mid-hold fades the overlay out and reports it once without finishing`() = runComposeUiTest {
        val state = MorphOverlayState()
        var finished = 0
        var cancelled = 0
        hostWithAnchors(state, onFinished = { finished++ }, onCancelled = { cancelled++ })
        startMorph(state)
        mainClock.advanceTimeBy(flow.inkShrink.delayMillis.toLong())
        assertTrue(state.covered)

        runOnUiThread {
            state.cancel()
            state.markReady()
        }
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(flow.reducedCrossfadeMillis + 100L)
        waitForIdle()

        assertFalse(state.isRunning)
        assertEquals(1, cancelled)
        assertEquals(0, finished)
        assertEquals(Long.MAX_VALUE, runOnUiThread { state.boardClockMillis() }, "the board is at rest")
    }

    @Test
    fun `awaitCovered is true once the ink covers the screen and false after a cancel`() = runComposeUiTest {
        val state = MorphOverlayState()
        hostWithAnchors(state)
        var covered: Boolean? = null
        startMorph(state)
        val scope = runOnUiThread { CoroutineScope(Dispatchers.Main.immediate) }
        runOnUiThread { scope.launch { covered = state.awaitCovered() } }
        mainClock.advanceTimeBy(flow.inkExpand.delayMillis.toLong())
        waitForIdle()
        assertEquals(null, covered, "not yet covered while the ink grows")
        mainClock.advanceTimeBy(flow.inkExpand.durationMillis + 50L)
        waitForIdle()
        assertEquals(true, covered)

        runOnUiThread { state.cancel() }
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(flow.reducedCrossfadeMillis + 50L)
        waitForIdle()
        var second: Boolean? = null
        startMorph(state)
        runOnUiThread {
            state.cancel()
            scope.launch { second = state.awaitCovered() }
        }
        waitForIdle()
        assertEquals(false, second, "a cancelled morph never navigates")
    }

    @Test
    fun `the board clock waits under the ink and runs from the shrink start`() = runComposeUiTest {
        val state = MorphOverlayState()
        hostWithAnchors(state)
        startMorph(state)
        runOnUiThread { state.markReady() }
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(flow.inkShrink.delayMillis - 100L)
        assertEquals(0L, runOnUiThread { state.boardClockMillis() })

        mainClock.advanceTimeBy(300L)
        val clock = runOnUiThread { state.boardClockMillis() }
        assertTrue(clock in 1L..300L, "board clock $clock")
        assertTrue(state.boardStagger)
    }

    @Test
    fun `minimal never starts a morph`() = runComposeUiTest {
        val state = MorphOverlayState()
        hostWithAnchors(state)
        assertFalse(runOnUiThread { state.start("play", "marker", "A", MotionTier.Minimal) })
        assertFalse(state.isRunning)
    }
}
