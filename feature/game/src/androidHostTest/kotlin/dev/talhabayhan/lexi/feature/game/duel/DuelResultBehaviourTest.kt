package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.DuelDefaults
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** `20-duel-result` behaviour at 390 × 844dp, Turkish like the design. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class DuelResultBehaviourTest {
    private val screenWidth = 390.dp

    /** Left edge of the visible part of the yellow circle (it bleeds out at the top end). */
    private val circleStart = screenWidth - (DuelDefaults.ResultCircleSize - DuelDefaults.ResultCircleEndBleed)

    private fun ComposeUiTest.show(outcome: DuelOutcome, onOpenReview: (Int) -> Unit = {}) = setContent {
        LexiTheme(reducedMotion = true) {
            DuelResultScreen(outcome, onPlayAgain = {}, onHome = {}, onOpenReview = onOpenReview)
        }
    }

    private fun ComposeUiTest.titleLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        onNodeWithTag(DuelResultTestTags.TITLE).fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single()
    }

    /** The headline ends before the circle and is shown whole (no ellipsis, at most two lines). */
    private fun ComposeUiTest.assertTitleClearOfCircle() {
        val bounds = onNodeWithTag(DuelResultTestTags.TITLE).getBoundsInRoot()
        assertTrue(bounds.right <= circleStart, "title ends at ${bounds.right}, circle starts at $circleStart")
        val layout = titleLayout()
        assertFalse(layout.hasVisualOverflow, "title is cut off")
        assertTrue(layout.lineCount <= 2, "title has ${layout.lineCount} lines")
        assertFalse((0 until layout.lineCount).any { layout.isLineEllipsized(it) }, "title is ellipsized")
    }

    // Regression (1.0.12): "<name> kazandı" ran under the circle at the top end.
    @Test
    fun `a 16-character winner name stays clear of the circle`() = runComposeUiTest {
        show(DuelResultSamples.longName)

        assertEquals(16, DuelResultSamples.longName.players[0].name.length)
        assertTitleClearOfCircle()
    }

    @Test
    fun `a 16-character single-word winner name shrinks instead of running under the circle`() = runComposeUiTest {
        val name = "Abdülkerimoğulla"
        val outcome = DuelResultSamples.longName.copy(
            players = DuelResultSamples.longName.players.mapIndexed { i, p -> if (i == 0) p.copy(name = name) else p },
        )
        show(outcome)

        assertEquals(16, name.length)
        assertTitleClearOfCircle()
    }

    @Test
    fun `the draw headline stays clear of the circle`() = runComposeUiTest {
        show(DuelResultSamples.draw)

        assertTitleClearOfCircle()
    }

    @Test
    fun `each player card is a button that opens that player's answers`() = runComposeUiTest {
        val opened = mutableListOf<Int>()
        show(DuelResultSamples.forfeit, onOpenReview = { opened += it })

        val isButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
        onNodeWithContentDescription("Ayşe: cevapları gör").assert(isButton).performScrollTo().performClick()
        onNodeWithContentDescription("Mehmetcan Y.: cevapları gör").assert(isButton).performScrollTo().performClick()

        assertEquals(listOf(0, 1), opened)
    }

    @Test
    fun `a card states the winner and the counts`() = runComposeUiTest {
        show(DuelResultSamples.forfeit)

        onNodeWithContentDescription("Mehmetcan Y.: cevapları gör").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                "KAZANAN, 9 doğru, 2 yanlış, 1 pas, kalan süre 0:40",
            ),
        )
    }
}
