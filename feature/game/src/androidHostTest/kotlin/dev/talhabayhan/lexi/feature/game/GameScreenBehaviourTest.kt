package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.feature.game.components.GameColumn
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GameScreenBehaviourTest {

    /** Regression (review of 30c4bbf): `inputEnabled` was mapped but the controls stayed tappable during a reveal. */
    @Test
    fun `keys, backspace, pass and joker are disabled during the reveal`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.wrongReveal, clock = { GameSamples.designClock }, onIntent = { intents += it })
            }
        }

        onNodeWithText("S").assertIsNotEnabled().performClick()
        onNodeWithContentDescription("Harf sil").assertIsNotEnabled().performClick()
        onNodeWithContentDescription("Joker kullan").assertIsNotEnabled().performClick()
        onNodeWithText("PAS").assertIsNotEnabled().performClick()

        assertEquals(emptyList(), intents.filter { it !is GameIntent.Foreground && it !is GameIntent.Background })
    }

    @Test
    fun `controls are enabled while typing`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
            }
        }

        onNodeWithText("S").assertIsEnabled()
        onNodeWithContentDescription("Harf sil").assertIsEnabled()
        onNodeWithContentDescription("Joker kullan").assertIsEnabled()
        onNodeWithText("PAS").assertIsEnabled()
    }

    @Test
    fun `a joker row uses its joker and a joker without inventory is disabled`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.jokerSheet, clock = {
                    GameSamples.jokerSheetClock
                }, onIntent = { intents += it })
            }
        }

        onNodeWithText("Harf aç").performClick()
        onNodeWithText("Telaffuzu dinle").assertIsNotEnabled()
        onNodeWithContentDescription("Kapat").performClick()
        onNodeWithText("Süre işlemeye devam ediyor · 1:31").assertExists()

        assertEquals(
            listOf(GameIntent.UseJoker(JokerType.REVEAL_LETTER), GameIntent.CloseJokers),
            intents.filter { it is GameIntent.UseJoker || it is GameIntent.CloseJokers },
        )
    }

    @Test
    fun `the pause dialog resumes or asks to confirm giving up`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.paused, clock = { GameSamples.designClock }, onIntent = { intents += it })
            }
        }

        onNodeWithText("Devam et").performClick()
        onNodeWithText("Pes et").performClick()

        assertEquals(
            listOf(GameIntent.Resume, GameIntent.RequestQuit),
            intents.filter { it is GameIntent.Resume || it is GameIntent.RequestQuit },
        )
    }

    /** Regression (review of 30c4bbf): the column reported more height than its constraints allow. */
    @Test
    fun `the game column never reports more height than its bounds`() = runComposeUiTest {
        setContent {
            Box(Modifier.size(390.dp, 300.dp)) {
                GameColumn(
                    gap = 16.dp,
                    answerTopMargin = 4.dp,
                    keysHeight = 166.dp,
                    keysActionsGap = 12.dp,
                    maxWheelSize = 320.dp,
                    hud = { Spacer(Modifier.height(48.dp)) },
                    wheel = { Spacer(Modifier.size(it)) },
                    clue = { Spacer(Modifier.height(120.dp)) },
                    answer = { Spacer(Modifier.height(48.dp)) },
                    keys = { Spacer(Modifier.height(108.dp)) },
                    actions = { Spacer(Modifier.height(56.dp)) },
                    modifier = Modifier.testTag(COLUMN),
                )
            }
        }

        onNodeWithTag(COLUMN).assertHeightIsEqualTo(300.dp)
    }

    private companion object {
        const val COLUMN = "column"
    }
}
