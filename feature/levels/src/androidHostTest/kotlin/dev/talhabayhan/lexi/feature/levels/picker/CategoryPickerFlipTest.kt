package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMotion
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionPolicy
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Motion #13–#15 on the test clock: the dialog takes input only once it rests, a close mid-open turns it back into
 * the card, a tap on the same card mid-close turns it back up, and reduced motion is a 120 ms cross-fade.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class CategoryPickerFlipTest {

    /** The view model's dialog rules, enough for the screen: open from rest, close, play. */
    private class Harness {
        var state by mutableStateOf(PickerSampleState)
        val intents = mutableListOf<CategoryPickerIntent>()

        fun onIntent(intent: CategoryPickerIntent) {
            intents += intent
            state = when (intent) {
                is CategoryPickerIntent.OpenCategory -> if (state.dialog == null) {
                    state.copy(dialog = state.categories.first { it.id == intent.id }, selectedCategory = intent.id)
                } else {
                    state
                }

                CategoryPickerIntent.CloseDialog, CategoryPickerIntent.Play -> state.copy(dialog = null)

                else -> state
            }
        }

        // Written from the test thread: announced so the next frame recomposes.
        fun open() {
            onIntent(CategoryPickerIntent.OpenCategory(CategoryId.MIXED))
            Snapshot.sendApplyNotifications()
        }

        fun close() {
            onIntent(CategoryPickerIntent.CloseDialog)
            Snapshot.sendApplyNotifications()
        }
    }

    private fun flip(reducedMotion: Boolean, block: ComposeUiTest.(Harness, FlowMotion) -> Unit) {
        val harness = Harness()
        lateinit var flow: FlowMotion
        runComposeUiTest {
            setContent {
                // A fresh policy: no slow streak of another test can drop this one to Lite.
                CompositionLocalProvider(LocalMotionPolicy provides MotionPolicy()) {
                    LexiTheme(reducedMotion = reducedMotion) {
                        flow = LexiTheme.motion.flow
                        CategoryPickerScreen(harness.state, harness::onIntent)
                    }
                }
            }
            waitForIdle()
            mainClock.autoAdvance = false
            block(harness, flow)
        }
    }

    private fun ComposeUiTest.advance(millis: Long) = mainClock.advanceTimeBy(millis)

    private fun ComposeUiTest.playTapped(harness: Harness): Boolean {
        val before = harness.intents.size
        onNodeWithText("OYNA").performClick()
        return CategoryPickerIntent.Play in harness.intents.drop(before)
    }

    @Test
    fun `the dialog takes input only once it has settled`() = flip(reducedMotion = false) { harness, flow ->
        onNodeWithContentDescription("Karışık, 412 / 850 kelime öğrenildi").performClick()
        assertEquals(CategoryPickerIntent.OpenCategory(CategoryId.MIXED), harness.intents.last())

        advance(MID_OPEN)
        onNodeWithText("Pratik").performClick()
        assertFalse(harness.intents.any { it is CategoryPickerIntent.SelectMode }, "no choice while it opens")
        assertFalse(playTapped(harness), "no OYNA while it opens")

        advance(flow.dialogOpenSettledMillis - MID_OPEN + SLACK)
        onNodeWithText("Pratik").performClick()
        assertEquals(CategoryPickerIntent.SelectMode(GameMode.PRACTICE), harness.intents.last())
        assertTrue(playTapped(harness), "OYNA once the dialog rests")
    }

    @Test
    fun `a close mid-open turns the dialog back and leaves no scrim`() = flip(reducedMotion = false) { harness, _ ->
        harness.open()
        advance(MID_OPEN)
        onNodeWithTag(PlayDialogTags.SCRIM).assertExists()
        // The scrim closes it even while it opens.
        onNodeWithTag(PlayDialogTags.SCRIM).performTouchInput { click(Offset(10f, 10f)) }
        assertEquals(CategoryPickerIntent.CloseDialog, harness.intents.last())

        // Played backwards from where it was: gone after about the time it had run.
        advance(MID_OPEN + SLACK)
        onNodeWithTag(PlayDialogTags.SCRIM).assertDoesNotExist()
        onNodeWithText("OYNA").assertDoesNotExist()
        // The grid takes taps again.
        onNodeWithContentDescription("Karışık, 412 / 850 kelime öğrenildi").performClick()
        assertEquals(CategoryPickerIntent.OpenCategory(CategoryId.MIXED), harness.intents.last())
    }

    @Test
    fun `a tap on the same card mid-close reopens it and other taps are ignored`() =
        flip(reducedMotion = false) { harness, flow ->
            val card = onNodeWithContentDescription("Karışık, 412 / 850 kelime öğrenildi").getBoundsInRoot()
            val cardCentre = with(density) {
                Offset(((card.left + card.right) / 2).toPx(), ((card.top + card.bottom) / 2).toPx())
            }
            harness.open()
            advance(flow.dialogOpenSettledMillis + SLACK)
            harness.close()
            advance(MID_CLOSE)

            val before = harness.intents.size
            onRoot().performTouchInput { click(Offset(width - 10f, height - 10f)) }
            assertEquals(before, harness.intents.size, "a tap elsewhere is ignored while it closes")

            onRoot().performTouchInput { click(cardCentre) }
            assertEquals(CategoryPickerIntent.OpenCategory(CategoryId.MIXED), harness.intents.last())
            advance(MID_CLOSE / 2)
            assertFalse(playTapped(harness), "still turning back up")

            advance(MID_CLOSE + SLACK)
            assertTrue(playTapped(harness), "back at rest, open")
        }

    @Test
    fun `reduced motion cross-fades in 120 ms`() = flip(reducedMotion = true) { harness, flow ->
        harness.open()
        advance(FRAME * 2)
        assertFalse(playTapped(harness), "mid cross-fade")
        advance(flow.reducedCrossfadeMillis.toLong() + SLACK)
        assertTrue(playTapped(harness))

        advance(flow.reducedCrossfadeMillis.toLong() + SLACK)
        onNodeWithTag(PlayDialogTags.SCRIM).assertDoesNotExist()
    }

    private companion object {
        const val FRAME = 16L
        const val SLACK = 4 * FRAME
        const val MID_OPEN = 400L
        const val MID_CLOSE = 300L
    }
}
