package dev.talhabayhan.lexi.core.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** GAME_RULES.md §3 (end conditions), §5 (modes, timer) and §12 (TimeWarning). */
class GameEngineTimerTest {
    @Test
    fun `classic starts with 180 s and ticks count down`() {
        val game = play("APPLE")
        assertEquals(180_000L, game.state.remainingMs)

        game.send(GameEvent.Tick(1_500L), GameEvent.Tick(500L))

        assertEquals(178_000L, game.state.remainingMs)
    }

    @Test
    fun `classic uses the time limit from settings`() {
        val game = play("APPLE", config = GameConfig(classicTimeLimitSeconds = 120))

        assertEquals(120_000L, game.state.remainingMs)
    }

    @Test
    fun `time up ends the game`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Tick(180_000L))

        assertEquals(GameStatus.FINISHED, game.state.status)
        assertEquals(0L, game.state.remainingMs)
        assertEquals(GameOverReason.TIME_UP, game.state.summary?.reason)
        assertTrue(game.lastEffects.last() is EngineEffect.GameOver)
    }

    @Test
    fun `practice has no clock and never times out`() {
        val game = play("APPLE", mode = GameMode.PRACTICE).send(GameEvent.Tick(10_000_000L))

        assertNull(game.state.remainingMs)
        assertEquals(GameStatus.PLAYING, game.state.status)
    }

    @Test
    fun `review has no clock`() {
        assertNull(play("APPLE", mode = GameMode.REVIEW).state.remainingMs)
    }

    @Test
    fun `hardcore and daily last 180 s and level test 120 s`() {
        assertEquals(180_000L, play("APPLE", mode = GameMode.HARDCORE).state.remainingMs)
        assertEquals(180_000L, play("APPLE", mode = GameMode.DAILY).state.remainingMs)
        assertEquals(120_000L, play("APPLE", mode = GameMode.LEVEL_TEST).state.remainingMs)
    }

    @Test
    fun `pause stops the clock and resume restarts it`() {
        val game = play("APPLE").send(GameEvent.Pause, GameEvent.Tick(5_000L))
        assertEquals(180_000L, game.state.remainingMs)
        assertTrue(game.state.isPaused)

        game.send(GameEvent.Resume, GameEvent.Tick(5_000L))

        assertEquals(175_000L, game.state.remainingMs)
    }

    @Test
    fun `input is ignored while paused`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Pause)
        val before = game.state

        game.send(GameEvent.KeyTapped(0), GameEvent.Pass, GameEvent.UseJoker(JokerType.EXAMPLE), GameEvent.Pause)

        assertSame(before, game.state)
    }

    @Test
    fun `resume without pause is ignored`() {
        val game = play("APPLE")
        val before = game.state

        game.send(GameEvent.Resume)

        assertSame(before, game.state)
    }

    @Test
    fun `forfeit works while paused`() {
        val game = play("APPLE").send(GameEvent.Pause, GameEvent.Forfeit)

        assertEquals(GameOverReason.FORFEIT, game.state.summary?.reason)
        assertEquals(false, game.state.isPaused)
    }

    @Test
    fun `time warning is emitted each second from 10 seconds left`() {
        val game = play("APPLE").send(GameEvent.Tick(169_500L))
        assertTrue(game.effectsOf<EngineEffect.TimeWarning>().isEmpty())

        repeat(11) { game.send(GameEvent.Tick(1_000L)) }

        assertEquals((10 downTo 1).toList(), game.effectsOf<EngineEffect.TimeWarning>().map { it.secondsLeft })
        assertTrue(game.lastEffects.single() is EngineEffect.GameOver)
    }

    @Test
    fun `a tick that skips several seconds emits only the lowest one`() {
        val game = play("APPLE").send(GameEvent.Tick(169_500L), GameEvent.Tick(5_000L))

        assertEquals(listOf<EngineEffect>(EngineEffect.TimeWarning(secondsLeft = 6)), game.lastEffects)
    }

    @Test
    fun `no time warning when the tick reaches 0`() {
        val game = play("APPLE").send(GameEvent.Tick(178_500L)).send(GameEvent.Tick(5_000L))

        assertEquals(listOf(2), game.effectsOf<EngineEffect.TimeWarning>().map { it.secondsLeft })
        assertTrue(game.lastEffects.single() is EngineEffect.GameOver)
    }

    @Test
    fun `clock does not run while a wrong answer is revealed`() {
        val game = play("APPLE", "BREAD").answerWrong().send(GameEvent.Tick(2_000L))

        assertEquals(179_200L, game.state.remainingMs)
        assertEquals('B', game.currentLetter)
    }

    @Test
    fun `clock runs during the reveal when configured`() {
        val config = GameConfig(clockRunsDuringReveal = true)
        val game = play("APPLE", "BREAD", config = config).answerWrong().send(GameEvent.Tick(2_000L))

        assertEquals(178_000L, game.state.remainingMs)
        assertEquals('B', game.currentLetter)
    }

    @Test
    fun `time running out during the reveal ends the game without advancing when the clock runs`() {
        val config = GameConfig(clockRunsDuringReveal = true)
        val game = play("APPLE", "BREAD", config = config).send(GameEvent.Tick(179_000L)).answerWrong()

        game.send(GameEvent.Tick(1_200L))

        val summary = checkNotNull(game.state.summary)
        assertEquals(GameOverReason.TIME_UP, summary.reason)
        assertEquals(1, summary.wrong)
        assertEquals(1, summary.unanswered)
        assertTrue(game.lastEffects.none { it is EngineEffect.Advanced })
        assertEquals(SlotState.TODO, game.state.wheel.slots[1].state)
    }

    @Test
    fun `pause also stops the reveal`() {
        val game = play("APPLE", "BREAD").answerWrong().send(GameEvent.Pause, GameEvent.Tick(5_000L))

        assertEquals(1_200L, game.state.revealRemainingMs)
    }

    @Test
    fun `wrong answer in hardcore ends the game after the reveal`() {
        val game = play("APPLE", "BREAD", mode = GameMode.HARDCORE).answerWrong()
        assertEquals(GameStatus.PLAYING, game.state.status)

        game.send(GameEvent.Tick(1_200L))

        assertEquals(GameStatus.FINISHED, game.state.status)
        assertEquals(GameOverReason.WRONG_IN_HARDCORE, game.state.summary?.reason)
    }

    @Test
    fun `wrong answer in classic does not end the game`() {
        val game = play("APPLE", "BREAD").answerWrongAndWait()

        assertEquals(GameStatus.PLAYING, game.state.status)
    }

    @Test
    fun `zero or negative ticks are ignored`() {
        val game = play("APPLE")
        val before = game.state

        game.send(GameEvent.Tick(0L), GameEvent.Tick(-5L))

        assertSame(before, game.state)
    }
}
