package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** #16 host behaviour on the frame clock: the shrink waits for ready + target, then the morph finishes once. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class MorphOverlayHostTest {
    private val flow = LexiMotion(reducedMotion = false).flow

    @Test
    fun `the morph holds until the game is ready and finishes after the shrink`() = runComposeUiTest {
        val state = MorphOverlayState()
        val monitor = FrameMonitor()
        var finished = 0
        mainClock.autoAdvance = false
        setContent {
            LexiTheme(reducedMotion = false) {
                MorphOverlayHost(state, frameMonitor = monitor, onFinished = { finished++ }) {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.offset(20.dp, 700.dp).size(350.dp, 80.dp).morphAnchor("play"))
                        Box(Modifier.offset(177.dp, 120.dp).size(36.dp).morphAnchor("marker"))
                    }
                }
            }
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        assertTrue(runOnUiThread { state.start(sourceKey = "play", targetKey = "marker", letter = "A") })
        assertFalse(runOnUiThread { state.start("play", "marker", "B") }, "a second start is ignored")
        // Writes from the test thread reach the recomposer only through an apply (no auto-advance here).
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(flow.inkShrink.endMillis + 500L)
        assertTrue(state.isRunning, "must hold the full screen until ready")
        assertTrue(monitor.isWatching)

        runOnUiThread { state.markReady() }
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(flow.inkShrink.durationMillis + 100L)
        waitForIdle()

        assertFalse(state.isRunning)
        assertEquals(1, finished)
        assertFalse(monitor.isWatching)
    }

    @Test
    fun `a start without a laid out source does nothing`() = runComposeUiTest {
        val state = MorphOverlayState()
        setContent { LexiTheme(reducedMotion = false) { MorphOverlayHost(state) {} } }

        assertFalse(runOnUiThread { state.start("missing", "marker", "A") })
        assertFalse(state.isRunning)
    }
}
