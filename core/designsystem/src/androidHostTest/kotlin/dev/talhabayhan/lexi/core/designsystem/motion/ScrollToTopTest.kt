package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Motion #19: the grid's "Başa dön" scroll takes the same time from any distance, ends exactly on the top, a drag wins. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class ScrollToTopTest {
    private val easing = LexiMotion(reducedMotion = false).easeOut

    private class Harness(val state: LazyGridState, val scope: CoroutineScope)

    private fun withGrid(items: Int, block: androidx.compose.ui.test.ComposeUiTest.(Harness) -> Unit) =
        runComposeUiTest {
            lateinit var harness: Harness
            setContent {
                val state = rememberLazyGridState()
                val scope = rememberCoroutineScope()
                harness = Harness(state, scope)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = state,
                    modifier = Modifier.fillMaxWidth().height(600.dp).testTag("grid"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items((0 until items).toList(), key = {
                        it
                    }) { Box(Modifier.fillMaxWidth().height(120.dp).padding(2.dp)) }
                }
            }
            waitForIdle()
            block(harness)
        }

    private fun androidx.compose.ui.test.ComposeUiTest.launchScroll(h: Harness, millis: Int = 400): Job {
        val job = h.scope.launch { h.state.animateScrollToTop(millis, easing, maxStretchPx = 1500f) }
        return job
    }

    private fun androidx.compose.ui.test.ComposeUiTest.jumpTo(h: Harness, index: Int) {
        runOnUiThread { kotlinx.coroutines.runBlocking { h.state.scrollToItem(index) } }
        waitForIdle()
    }

    @Test
    fun `a short distance and a very long one both take the capped time and end on the top`() {
        for (index in listOf(12, 600)) {
            withGrid(items = 800) { h ->
                jumpTo(h, index)
                assertTrue(h.state.firstVisibleItemIndex > 0)
                mainClock.autoAdvance = false
                val job = launchScroll(h)
                mainClock.advanceTimeByFrame()
                mainClock.advanceTimeBy(150)
                assertFalse(job.isCompleted, "still animating after ~150 ms (index $index)")
                assertTrue(h.state.firstVisibleItemIndex > 0)
                mainClock.advanceTimeBy(400)
                assertTrue(job.isCompleted, "done within the 400 ms cap + a frame (index $index)")
                assertEquals(0, h.state.firstVisibleItemIndex)
                assertEquals(0, h.state.firstVisibleItemScrollOffset)
            }
        }
    }

    @Test
    fun `the reduced time is respected`() = withGrid(items = 200) { h ->
        jumpTo(h, 100)
        mainClock.autoAdvance = false
        val job = launchScroll(h, millis = 120)
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeBy(200)
        assertTrue(job.isCompleted)
        assertEquals(0, h.state.firstVisibleItemIndex)
    }

    @Test
    fun `already on the top does nothing and a drag takes over mid-scroll`() = withGrid(items = 200) { h ->
        val atTop = launchScroll(h)
        waitForIdle()
        assertTrue(atTop.isCompleted)
        assertEquals(0, h.state.firstVisibleItemIndex)

        jumpTo(h, 100)
        mainClock.autoAdvance = false
        val job = launchScroll(h)
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeBy(100)
        val before = h.state.firstVisibleItemIndex
        assertTrue(before > 0)
        // The user's own finger takes the list: their scroll outranks the animation, which is cancelled mid-way.
        onNodeWithTag("grid").performTouchInput {
            down(center)
            moveBy(androidx.compose.ui.geometry.Offset(0f, 80f))
        }
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        mainClock.autoAdvance = true
        onNodeWithTag("grid").performTouchInput { up() }
        waitForIdle()
        assertTrue(job.isCancelled, "the user's drag cancels the animated scroll")
    }

    @Test
    fun `the distance estimate counts the rows above the first visible one`() = withGrid(items = 100) { h ->
        jumpTo(h, 20) // row 10, 2 columns
        val pitch = 128 // row height 120dp + 8dp spacing, the test runs at mdpi (1 dp = 1 px)
        val estimate = h.state.distanceToTopPx()
        assertEquals((10 * pitch).toFloat(), estimate, 1f)
    }
}
