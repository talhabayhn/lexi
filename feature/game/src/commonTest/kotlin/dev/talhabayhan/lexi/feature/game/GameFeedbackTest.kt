package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.components.GameFeedback
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The `motion.md` catalogue: haptic and sound per game effect. */
class GameFeedbackTest {

    @Test
    fun `correct answer plays success and the two rising notes`() {
        assertEquals(
            GameFeedback(HapticKind.Success, SoundEffect.Correct),
            GameFeedback.of(GameEffect.Correct('A', 10)),
        )
    }

    @Test
    fun `correct answer that is spoken keeps the haptic but drops the notes so they do not clash`() {
        assertEquals(
            GameFeedback(HapticKind.Success, sound = null),
            GameFeedback.of(GameEffect.Correct('A', 10, spoken = true)),
        )
    }

    @Test
    fun `time up plays the two-tone buzzer`() {
        assertEquals(GameFeedback(haptic = null, sound = SoundEffect.TimeUp), GameFeedback.of(GameEffect.TimeUp))
    }

    @Test
    fun `wrong answer plays error and bzzt`() {
        assertEquals(GameFeedback(HapticKind.Error, SoundEffect.Wrong), GameFeedback.of(GameEffect.Wrong('A')))
    }

    @Test
    fun `pass plays soft and vinn`() {
        assertEquals(GameFeedback(HapticKind.Soft, SoundEffect.Pass), GameFeedback.of(GameEffect.Passed('A')))
    }

    @Test
    fun `combo plays heavy and the fanfare`() {
        assertEquals(GameFeedback(HapticKind.Heavy, SoundEffect.Combo), GameFeedback.of(GameEffect.ComboReached(3)))
    }

    @Test
    fun `time warning plays a tick and tik each second`() {
        assertEquals(GameFeedback(HapticKind.Tick, SoundEffect.Tick), GameFeedback.of(GameEffect.TimeWarning(9)))
    }

    /** #2's light haptic is the key's own #1 press feedback; a second one would double it. */
    @Test
    fun `a placed letter adds nothing to the key press`() {
        assertNull(GameFeedback.of(GameEffect.LetterPlaced(cellIndex = 0, keyIndex = 3, letter = 'A')))
    }

    @Test
    fun `wheel advance and navigation are silent`() {
        assertNull(GameFeedback.of(GameEffect.Advanced('B')))
        assertNull(GameFeedback.of(GameEffect.Abandoned))
        val result = ResultRoute(
            level = Level.A1.name,
            mode = GameMode.CLASSIC.name,
            category = null,
            reason = GameOverReason.TIME_UP.name,
            score = 0,
            correct = 0,
            wrong = 0,
            passed = 0,
            longestStreak = 0,
            remainingMs = 0L,
            missedCards = emptyList(),
            passedCards = emptyList(),
            recorded = true,
        )
        assertNull(GameFeedback.of(GameEffect.NavigateToResult(result)))
    }
}
