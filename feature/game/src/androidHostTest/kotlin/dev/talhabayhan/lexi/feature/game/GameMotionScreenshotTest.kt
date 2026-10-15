package dev.talhabayhan.lexi.feature.game

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Stills of the game screen in the middle of `motion.md` animations (`15-motion`), stepped with the paused test clock
 * from the effect's start. The resting screenshots are in [GameScreenScreenshotTest].
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GameMotionScreenshotTest {

    /** #2 at the end of the travel: the copy of the "C" key at 1.15× over the fourth box, which lands next. */
    @Test
    fun letterFly() = runComposeUiTest {
        val harness = setGameScreen(MotionSamples.letterPlaced)
        play(harness, GameEffect.LetterPlaced(cellIndex = 3, keyIndex = 0, letter = 'C'))
        mainClock.advanceTimeBy(FLY_PEAK_MS)
        onRoot().captureRoboImage("$DIR/GameMotionLetterFly.png")
    }

    /** #3 at the peak: K is a blue circle at 1.45× with the ring at scale 1. */
    @Test
    fun correctPop() = runComposeUiTest {
        val harness = setGameScreen(MotionSamples.answered(WheelLetterState.Correct))
        play(harness, GameEffect.Correct('K', points = 10))
        mainClock.advanceTimeBy(CORRECT_PEAK_MS)
        onRoot().captureRoboImage("$DIR/GameMotionCorrectPop.png")
    }

    /** #3 later: K is back to about 1×, the ring has spread past it and fades. */
    @Test
    fun correctRing() = runComposeUiTest {
        val harness = setGameScreen(MotionSamples.answered(WheelLetterState.Correct))
        play(harness, GameEffect.Correct('K', points = 10))
        mainClock.advanceTimeBy(CORRECT_RING_MS)
        onRoot().captureRoboImage("$DIR/GameMotionCorrectRing.png")
    }

    /** #5 half way: K half turned and half a leaf, the arrow on its way towards L. */
    @Test
    fun passTurn() = runComposeUiTest {
        val harness = setGameScreen(MotionSamples.answered(WheelLetterState.Passed))
        play(harness, GameEffect.Passed('K'))
        mainClock.advanceTimeBy(PASS_HALF_MS)
        onRoot().captureRoboImage("$DIR/GameMotionPassTurn.png")
    }

    /** #7 mid-burst: the four shapes at their middle keyframe, the badge just after its peak. */
    @Test
    fun comboBurst() = runComposeUiTest {
        val harness = setGameScreen(GameSamples.design)
        play(harness, GameEffect.ComboReached(3))
        mainClock.advanceTimeBy(COMBO_MID_MS)
        onRoot().captureRoboImage("$DIR/GameMotionComboBurst.png")
    }

    /** #7 at rest (as in `15-motion.png`): "×3 SERİ" at −4°, the burst is over. */
    @Test
    fun comboBadge() = runComposeUiTest {
        val harness = setGameScreen(GameSamples.design)
        play(harness, GameEffect.ComboReached(3))
        mainClock.advanceTimeBy(COMBO_REST_MS)
        onRoot().captureRoboImage("$DIR/GameMotionComboBadge.png")
    }

    /** #8: red at once, the counter at the peak of its pulse (1.12×) after 0:10 → 0:09. */
    @Test
    fun timeWarningPulse() = runComposeUiTest {
        val harness = setGameScreen(GameSamples.design, clock = MotionSamples.warningAt10)
        setClock(harness, GameSamples.warningClock)
        mainClock.advanceTimeBy(FRAME_MS + PULSE_PEAK_MS)
        onRoot().captureRoboImage("$DIR/GameMotionTimeWarningPulse.png")
    }

    private companion object {
        const val DIR = "src/androidHostTest/screenshots"
        const val FLY_PEAK_MS = 157L
        const val CORRECT_PEAK_MS = 107L
        const val CORRECT_RING_MS = 200L
        const val PASS_HALF_MS = 130L
        const val COMBO_MID_MS = 210L
        const val COMBO_REST_MS = 600L
        const val PULSE_PEAK_MS = 300L
    }
}
