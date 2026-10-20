package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class ResultScreenBehaviourTest {

    @Test
    fun `tekrar oyna is shown after a normal game`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { ResultScreen(ResultSamples.design, onIntent = {}) } }

        onNodeWithText("TEKRAR OYNA").assertIsDisplayed()
        onNodeWithText("Paylaş").assertIsDisplayed()
    }

    @Test
    fun `tekrar oyna is hidden after a daily and share is shown`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                ResultScreen(ResultSamples.design.copy(mode = GameMode.DAILY), onIntent = {})
            }
        }

        onNodeWithText("TEKRAR OYNA").assertDoesNotExist()
        onNodeWithText("Paylaş").assertIsDisplayed()
    }

    @Test
    fun `the button row shows both TEKRAR OYNA and Paylaş without overlap`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { ResultScreen(ResultSamples.design, onIntent = {}) } }

        val playAgain = onNodeWithText("TEKRAR OYNA").assertIsDisplayed()
        val share = onNodeWithText("Paylaş").assertIsDisplayed()

        val playAgainBounds = playAgain.getBoundsInRoot()
        val shareBounds = share.getBoundsInRoot()

        assertTrue(
            playAgainBounds.right <= shareBounds.left,
            "TEKRAR OYNA (${playAgainBounds.right}) must not overlap Paylaş (${shareBounds.left})",
        )
    }

    @Test
    fun `a long group shows its first five rows and show more asks to open it`() = runComposeUiTest {
        val intents = mutableListOf<ResultIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { ResultScreen(ResultSamples.longGroup, onIntent = { intents += it }) }
        }

        onNodeWithText("fish").assertExists()
        onNodeWithText("green").assertDoesNotExist()

        onNodeWithText("+3 kelime daha · Daha fazla göster →").performScrollTo().performClick()

        assertEquals(listOf<ResultIntent>(ResultIntent.ToggleGroup(ReviewOutcome.Correct)), intents)
    }

    @Test
    fun `an open group shows every row and show less and the actions below`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                ResultScreen(
                    ResultSamples.longGroup.copy(
                        expandedGroups = kotlinx.collections.immutable.persistentSetOf(ReviewOutcome.Correct),
                    ),
                    onIntent = {},
                )
            }
        }

        onNodeWithText("key").assertExists()
        onNodeWithText("Daha az göster ↑").assertExists()
        onNodeWithText("TEKRAR OYNA").performScrollTo().assertIsDisplayed()
    }
}
