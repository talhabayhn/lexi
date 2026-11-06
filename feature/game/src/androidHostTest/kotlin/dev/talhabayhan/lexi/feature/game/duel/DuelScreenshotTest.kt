package dev.talhabayhan.lexi.feature.game.duel

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.GameSamples
import dev.talhabayhan.lexi.feature.game.GameScreen
import dev.talhabayhan.lexi.feature.game.components.DuelHudUi
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** `17-duel-setup`, `18-duel-handover`, `19-duel-game` and `20-duel-result` at 390 × 844dp, xhdpi (2×), Turkish like the designs. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class DuelScreenshotTest {
    private val hud = DuelHudUi(
        persistentListOf(
            DuelPlayerUi(name = "", correct = 10, remainingMs = 107_000L),
            DuelPlayerUi(name = "", correct = 7, remainingMs = 125_000L),
        ),
        activePlayer = 0,
    )

    @Test
    fun game() {
        captureRoboImage("src/androidHostTest/screenshots/DuelGame.png") {
            LexiTheme(reducedMotion = false) {
                GameScreen(
                    state = GameSamples.design.copy(canRestart = false),
                    clock = { GameSamples.designClock },
                    onIntent = {},
                    duelHud = hud,
                )
            }
        }
    }

    @Test
    fun gamePaused() {
        captureRoboImage("src/androidHostTest/screenshots/DuelGamePaused.png") {
            LexiTheme(reducedMotion = true) {
                GameScreen(
                    state = GameSamples.design.copy(isPaused = true, canRestart = false),
                    clock = { GameSamples.designClock },
                    onIntent = {},
                    duelHud = hud.copy(activePlayer = 1),
                )
            }
        }
    }

    @Test
    fun setup() {
        captureRoboImage("src/androidHostTest/screenshots/DuelSetup.png") {
            LexiTheme(reducedMotion = false) { DuelSetupScreen(DuelSetupSample, onIntent = {}) }
        }
    }

    @Test
    fun setupNameDialog() {
        captureRoboImage("src/androidHostTest/screenshots/DuelSetupNameDialog.png") {
            LexiTheme(reducedMotion = true) {
                DuelSetupScreen(DuelSetupSample.copy(editingPlayer = 0, nameDraft = "Ayşe"), onIntent = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun setupEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/DuelSetupEnglish.png") {
            LexiTheme(reducedMotion = false) { DuelSetupScreen(DuelSetupSample, onIntent = {}) }
        }
    }

    @Test
    fun handover() {
        captureRoboImage("src/androidHostTest/screenshots/DuelHandover.png") {
            LexiTheme(reducedMotion = false) {
                DuelHandoverCard(DuelHandoverSample, names = listOf("", ""), onIntent = {})
            }
        }
    }

    @Test
    fun handoverLongNameQuitConfirmation() {
        captureRoboImage("src/androidHostTest/screenshots/DuelHandoverQuit.png") {
            LexiTheme(reducedMotion = true) {
                DuelHandoverCard(
                    DuelHandoverSample.copy(isQuitConfirmation = true),
                    names = listOf("Ayşe", "Mehmetcan Y."),
                    onIntent = {},
                )
            }
        }
    }

    @Test
    fun resultTieBreakWrong() {
        captureRoboImage("src/androidHostTest/screenshots/DuelResult.png") {
            LexiTheme(reducedMotion = false) {
                DuelResultScreen(DuelResultSamples.tieBreakWrong, onPlayAgain = {}, onHome = {})
            }
        }
    }

    @Test
    fun resultForfeit() {
        captureRoboImage("src/androidHostTest/screenshots/DuelResultForfeit.png") {
            LexiTheme(reducedMotion = false) {
                DuelResultScreen(DuelResultSamples.forfeit, onPlayAgain = {}, onHome = {})
            }
        }
    }

    @Test
    fun resultDraw() {
        captureRoboImage("src/androidHostTest/screenshots/DuelResultDraw.png") {
            LexiTheme(reducedMotion = false) {
                DuelResultScreen(DuelResultSamples.draw, onPlayAgain = {}, onHome = {})
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun resultEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/DuelResultEnglish.png") {
            LexiTheme(reducedMotion = false) {
                DuelResultScreen(DuelResultSamples.tieBreakWrong, onPlayAgain = {}, onHome = {})
            }
        }
    }

    @Test
    @Config(fontScale = 2.0f)
    fun resultFontScale200() {
        captureRoboImage("src/androidHostTest/screenshots/DuelResultFontScale200.png") {
            LexiTheme(reducedMotion = false) {
                DuelResultScreen(DuelResultSamples.tieBreakWrong, onPlayAgain = {}, onHome = {})
            }
        }
    }
}
