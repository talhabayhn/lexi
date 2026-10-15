package dev.talhabayhan.lexi.feature.game

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** `motion.md` #2–#8 on the game screen: feedback in sync with the start of the motion, reduced motion. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GameMotionTest {

    @Test
    fun `each effect plays its haptic and sound through the provided players as its motion starts`() =
        runComposeUiTest {
            val feedback = RecordingFeedback()
            val harness = setGameScreen(MotionSamples.answered(WheelLetterState.Correct), feedback = feedback)

            play(harness, GameEffect.Correct('K', points = 10))
            assertEquals(listOf(HapticKind.Success), feedback.haptics)
            assertEquals(listOf(SoundEffect.Correct), feedback.sounds)
            assertNotNull(harness.motion.chipMotion('K'), "the pop runs")

            play(harness, GameEffect.ComboReached(3))
            play(harness, GameEffect.Passed('L'))
            play(harness, GameEffect.Wrong('L'))
            play(harness, GameEffect.TimeWarning(9))
            play(harness, GameEffect.Advanced('M'))

            assertEquals(
                listOf(HapticKind.Success, HapticKind.Heavy, HapticKind.Soft, HapticKind.Error, HapticKind.Tick),
                feedback.haptics,
            )
            assertEquals(
                listOf(SoundEffect.Correct, SoundEffect.Combo, SoundEffect.Pass, SoundEffect.Wrong, SoundEffect.Tick),
                feedback.sounds,
            )
        }

    @Test
    fun `reduced motion keeps haptics and sounds but plays no transforms`() = runComposeUiTest {
        val feedback = RecordingFeedback()
        val harness = setGameScreen(MotionSamples.letterPlaced, reducedMotion = true, feedback = feedback)

        play(harness, GameEffect.LetterPlaced(cellIndex = 3, keyIndex = 0, letter = 'C'))
        play(harness, GameEffect.Correct('K', points = 10))
        play(harness, GameEffect.Passed('J'))

        assertEquals(listOf(HapticKind.Success, HapticKind.Soft), feedback.haptics)
        assertEquals(listOf(SoundEffect.Correct, SoundEffect.Pass), feedback.sounds)
        assertTrue(harness.motion.flights.isEmpty(), "no letter flies")
        assertNull(harness.motion.chipMotion('K'), "no pop")
        assertNull(harness.motion.chipMotion('J'), "no turn")
    }

    @Test
    fun `a letter flies for 220 ms and lands on its cell`() = runComposeUiTest {
        val harness = setGameScreen(MotionSamples.letterPlaced)

        play(harness, GameEffect.LetterPlaced(cellIndex = 3, keyIndex = 0, letter = 'C'))
        mainClock.advanceTimeBy(100)
        assertEquals(setOf(3), harness.motion.flyingCells())

        mainClock.advanceTimeBy(200)
        assertTrue(harness.motion.flights.isEmpty())
    }

    @Test
    fun `a finished answer ends the letters in the air`() = runComposeUiTest {
        val harness = setGameScreen(MotionSamples.letterPlaced)

        play(harness, GameEffect.LetterPlaced(cellIndex = 3, keyIndex = 0, letter = 'C'))
        play(harness, GameEffect.Wrong('K'))

        assertTrue(harness.motion.flights.isEmpty())
    }

    @Test
    fun `the pop and the pass turn end on the resting chip`() = runComposeUiTest {
        val harness = setGameScreen(MotionSamples.answered(WheelLetterState.Passed))

        play(harness, GameEffect.Passed('K'))
        val pass = assertNotNull(harness.motion.chipMotion('K'))
        mainClock.advanceTimeBy(100)
        assertTrue(pass.passProgress in 0f..1f && pass.passProgress < 1f)

        mainClock.advanceTimeBy(300)
        assertNull(harness.motion.chipMotion('K'))
    }

    @Test
    fun `the combo badge pops, holds and leaves`() = runComposeUiTest {
        val harness = setGameScreen(GameSamples.design)

        play(harness, GameEffect.ComboReached(5))
        mainClock.advanceTimeBy(600)
        onNodeWithText("×5 SERİ").assertExists()

        mainClock.advanceTimeBy(1_000)
        onNodeWithText("×5 SERİ").assertDoesNotExist()
    }

    @Test
    fun `under reduced motion the combo badge still shows`() = runComposeUiTest {
        val harness = setGameScreen(GameSamples.design, reducedMotion = true)

        play(harness, GameEffect.ComboReached(3))
        mainClock.advanceTimeBy(600)
        onNodeWithText("×3 SERİ").assertExists()

        mainClock.advanceTimeBy(1_000)
        onNodeWithText("×3 SERİ").assertDoesNotExist()
    }
}
