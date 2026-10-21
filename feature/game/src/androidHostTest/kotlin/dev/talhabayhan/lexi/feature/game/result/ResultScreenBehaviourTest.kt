package dev.talhabayhan.lexi.feature.game.result

import android.graphics.BitmapFactory
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.feature.game.result.share.ShareCardSamples
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
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

    @Test
    fun `tapping share asks to share`() = runComposeUiTest {
        val intents = mutableListOf<ResultIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { ResultScreen(ResultSamples.design, onIntent = { intents += it }) }
        }

        onNodeWithText("Paylaş").performClick()

        assertEquals(listOf<ResultIntent>(ResultIntent.Share), intents)
    }

    @Test
    fun `a running share renders the card off screen to a 1080 x 1350 png with the share text`() = runComposeUiTest {
        val intents = mutableListOf<ResultIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                ResultScreen(
                    ResultSamples.design.copy(shareCard = ShareCardSamples.design, isSharing = true),
                    onIntent = { intents += it },
                )
            }
        }

        // Robolectric draws the window only when asked (a device draws every frame): draw it while waiting.
        waitUntil(timeoutMillis = 5_000) {
            onRoot().captureToImage()
            intents.isNotEmpty()
        }
        val shared = assertIs<ResultIntent.SharePng>(intents.single())
        val bitmap = assertNotNull(BitmapFactory.decodeByteArray(shared.png, 0, shared.png.size))
        assertEquals(1080 to 1350, bitmap.width to bitmap.height)
        assertEquals("A'dan Z'ye 34 sn kala bitirdim. Yenebilir misin?\nLexi'de oynuyorum.", shared.text)
        // Never visible and not in the accessibility tree.
        onNodeWithText("21/26").assertDoesNotExist()
    }

    @Test
    fun `a failed share shows the error toast`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                ResultScreen(ResultSamples.design, onIntent = {}, shareErrorVisible = true)
            }
        }

        onNodeWithText("Paylaşım başlatılamadı.").assertIsDisplayed()
    }

    fun `the review title and the header are headings`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { ResultScreen(ResultSamples.design, onIntent = {}) } }

        val headings = onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).fetchSemanticsNodes()
        assertTrue(headings.size >= 2, "result headings: $headings")
    }

    fun `the home link and the show-more row keep a touch target of at least 44dp`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { ResultScreen(ResultSamples.longGroup, onIntent = {}) } }

        val min = with(density) { 44.dp.toPx() }
        listOf("Ana menüye dön", "+3 kelime daha · Daha fazla göster →").forEach { text ->
            val node = onNodeWithText(text).performScrollTo().fetchSemanticsNode()
            assertTrue(node.touchBoundsInRoot.height >= min, "$text touch bounds ${node.touchBoundsInRoot}")
        }
    }
}
