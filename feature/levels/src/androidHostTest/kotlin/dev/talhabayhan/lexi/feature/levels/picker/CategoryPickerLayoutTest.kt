package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.text.LocalTextLayoutProbe
import dev.talhabayhan.lexi.core.designsystem.text.WordSplitRecorder
import dev.talhabayhan.lexi.core.designsystem.text.textWordProblems
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.game.GameMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * "Çarkını seç" and its dialog at 320 × 568, 360 × 640, 390 × 844 and 412 × 915, font scale 1.0 / 1.3 / 2.0, Turkish
 * and English: back, the level chips and the first card are on screen without scrolling; the dialog's OYNA is pinned
 * fully on screen (its choices scroll above it) in every mode; every button is at least 48dp high; no label (chips,
 * card names, progress, "YAKINDA", dialog segments, OYNA) breaks a word across lines or is cut off.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class CategoryPickerLayoutChecks(
    private val back: String,
    private val chip: String,
    private val mixed: String,
    private val play: String,
) {
    @Test
    @Config(qualifiers = "+w320dp-h568dp")
    fun w320h568() = check()

    @Test
    @Config(qualifiers = "+w360dp-h640dp")
    fun w360h640() = check()

    @Test
    @Config(qualifiers = "+w390dp-h844dp")
    fun w390h844() = check()

    @Test
    @Config(qualifiers = "+w412dp-h915dp")
    fun w412h915() = check()

    private fun check() {
        runComposeUiTest {
            val recorder = show(PickerSampleState)
            val height = onRoot().fetchSemanticsNode().size.height.toFloat()
            listOf(back, chip, mixed).forEach { label ->
                assertOnScreen(
                    onNode(hasContentDescription(label, substring = true)).fetchSemanticsNode(),
                    label,
                    height,
                )
            }
            assertButtonsTallEnough()
            assertNoWordSplits(recorder)
            // The exam section at the end of the grid: the open pack and the locked "YAKINDA" cards.
            onNode(hasScrollToNodeAction()).performScrollToNode(hasContentDescription(LOCKED_LAST, substring = true))
            assertNoWordSplits(recorder)
        }
        PickerModes.forEach { mode -> checkDialog(mode) }
    }

    /** OYNA is on screen without scrolling; the last choice (7:00) scrolls into view and OYNA stays put. */
    private fun checkDialog(mode: GameMode) = runComposeUiTest {
        val recorder = show(PickerSampleState.withDialog(mode))
        val height = onRoot().fetchSemanticsNode().size.height.toFloat()
        assertOnScreen(onNode(hasText(play)).fetchSemanticsNode(), "$play ($mode)", height)
        onNode(hasText(LAST_TIME)).performScrollTo()
        assertOnScreen(onNode(hasText(LAST_TIME)).fetchSemanticsNode(), "$LAST_TIME ($mode)", height)
        assertOnScreen(onNode(hasText(play)).fetchSemanticsNode(), "$play after scrolling ($mode)", height)
        assertButtonsTallEnough()
        assertNoWordSplits(recorder)
    }

    private fun ComposeUiTest.show(state: CategoryPickerUiState): WordSplitRecorder {
        val recorder = WordSplitRecorder()
        setContent {
            CompositionLocalProvider(LocalTextLayoutProbe provides recorder) {
                LexiTheme(reducedMotion = true) { LexiViewport { CategoryPickerScreen(state, {}) } }
            }
        }
        return recorder
    }

    /** Every label, also those hidden behind a component's content description, keeps its words whole. */
    private fun ComposeUiTest.assertNoWordSplits(recorder: WordSplitRecorder) {
        waitForIdle()
        val problems = recorder.problems + onRoot(useUnmergedTree = true).fetchSemanticsNode().textWordProblems()
        assertTrue(problems.isEmpty(), "words broken or cut: ${problems.distinct()}")
    }

    private fun assertOnScreen(node: SemanticsNode, label: String, bottomLimit: Float) {
        val top = node.boundsInRoot.top
        val bottom = node.boundsInRoot.bottom
        assertTrue(top >= 0f, "$label starts above the screen: $top")
        assertTrue(bottom <= bottomLimit, "$label is cut off: bottom $bottom > $bottomLimit")
    }

    private fun ComposeUiTest.assertButtonsTallEnough() {
        val minPx = MIN_TOUCH_DP * onRoot().fetchSemanticsNode().layoutInfo.density.density
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)).fetchSemanticsNodes().forEach { node ->
            val label = node.config.getOrNull(SemanticsProperties.ContentDescription)
                ?: node.config.getOrNull(SemanticsProperties.Text)
            assertTrue(node.size.height >= minPx - 1f, "$label is only ${node.size.height}px high")
        }
    }

    private companion object {
        const val MIN_TOUCH_DP = 48f
        const val LAST_TIME = "7:00"

        /** The last locked exam card (`LockedExamIds`); its description is the same in both languages. */
        const val LOCKED_LAST = "IELTS"
    }
}

private val TR = arrayOf("Geri", "B2 seviyesi", "Karışık", "OYNA")
private val EN = arrayOf("Back", "Level B2", "Mixed", "PLAY")

@Config(sdk = [36], qualifiers = "tr-xhdpi")
class CategoryPickerLayoutTr100Test : CategoryPickerLayoutChecks(TR[0], TR[1], TR[2], TR[3])

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 1.3f)
class CategoryPickerLayoutTr130Test : CategoryPickerLayoutChecks(TR[0], TR[1], TR[2], TR[3])

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 2.0f)
class CategoryPickerLayoutTr200Test : CategoryPickerLayoutChecks(TR[0], TR[1], TR[2], TR[3])

@Config(sdk = [36], qualifiers = "en-xhdpi")
class CategoryPickerLayoutEn100Test : CategoryPickerLayoutChecks(EN[0], EN[1], EN[2], EN[3])

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 1.3f)
class CategoryPickerLayoutEn130Test : CategoryPickerLayoutChecks(EN[0], EN[1], EN[2], EN[3])

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 2.0f)
class CategoryPickerLayoutEn200Test : CategoryPickerLayoutChecks(EN[0], EN[1], EN[2], EN[3])
