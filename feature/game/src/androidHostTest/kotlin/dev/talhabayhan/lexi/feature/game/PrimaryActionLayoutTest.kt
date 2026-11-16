package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.feature.game.duel.DuelResultSamples
import dev.talhabayhan.lexi.feature.game.duel.DuelResultScreen
import dev.talhabayhan.lexi.feature.game.duel.DuelSetupSample
import dev.talhabayhan.lexi.feature.game.duel.DuelSetupScreen
import dev.talhabayhan.lexi.feature.game.result.ResultSamples
import dev.talhabayhan.lexi.feature.game.result.ResultScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * The primary action of every game-module screen is fully on screen without scrolling on a 320 × 568 phone, at 100 %,
 * 130 % and 200 % font scale, in Turkish and English (the pattern of `OnboardingLayoutTest`: a primary button that is
 * the last item of a scrolling column is cut off at the viewport edge, so the actions are pinned below the scroll).
 * The checks live here; the subclasses below only set the configuration.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class PrimaryActionLayoutChecks(private val labels: Labels) {
    class Labels(val playAgain: String, val share: String, val start: String)

    @Test
    fun `game result keeps play again and share on screen`() {
        assertVisible(labels.playAgain, labels.share) { ResultScreen(state = ResultSamples.design, onIntent = {}) }
    }

    @Test
    fun `daily result keeps share on screen`() {
        assertVisible(labels.share) {
            ResultScreen(state = ResultSamples.design.copy(mode = GameMode.DAILY), onIntent = {})
        }
    }

    @Test
    fun `result with all four groups keeps its actions on screen`() {
        assertVisible(labels.playAgain, labels.share) {
            ResultScreen(state = ResultSamples.allFourGroups, onIntent = {})
        }
    }

    @Test
    fun `duel result keeps play again on screen`() {
        assertVisible(labels.playAgain) {
            DuelResultScreen(DuelResultSamples.tieBreakWrong, onPlayAgain = {}, onHome = {})
        }
    }

    @Test
    fun `duel setup keeps start on screen`() {
        assertVisible(labels.start) { DuelSetupScreen(DuelSetupSample, onIntent = {}) }
    }

    @Test
    fun `game screen keeps its action row on screen`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                LexiViewport {
                    GameScreen(state = GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
                }
            }
        }
        val screen = onRoot().getUnclippedBoundsInRoot()
        val actions = onNodeWithTag(GameTestTags.ACTIONS).getUnclippedBoundsInRoot()
        assertTrue(actions.top >= 0.dp, "actions start above the screen: ${actions.top}")
        assertTrue(actions.bottom <= screen.bottom, "actions cut off: ${actions.bottom} > ${screen.bottom}")
    }

    private fun assertVisible(vararg labels: String, screen: @Composable () -> Unit) = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { LexiViewport { screen() } } }
        val root = onRoot().getUnclippedBoundsInRoot()
        labels.forEach { label ->
            val bounds = onNodeWithText(label).getUnclippedBoundsInRoot()
            assertTrue(bounds.top >= 0.dp, "$label starts above the screen: ${bounds.top}")
            assertTrue(bounds.bottom <= root.bottom, "$label is cut off: bottom ${bounds.bottom} > ${root.bottom}")
        }
    }
}

private val TR = PrimaryActionLayoutChecks.Labels(playAgain = "TEKRAR OYNA", share = "Paylaş", start = "BAŞLA")
private val EN = PrimaryActionLayoutChecks.Labels(playAgain = "PLAY AGAIN", share = "Share", start = "START")

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutTr100Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutTr130Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutTr200Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutEn100Test : PrimaryActionLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutEn130Test : PrimaryActionLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutEn200Test : PrimaryActionLayoutChecks(EN)
