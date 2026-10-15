package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.Slot
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import dev.talhabayhan.lexi.core.testing.testCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameUiMapperTest {
    private val config = GameConfig()
    private val engine = GameEngine(config)
    private val apple = testCard("APPLE")
    private val iceCream = testCard("ICE CREAM", letter = 'C')
    private val wheel = Wheel(
        listOf(
            Slot('A', apple, SlotState.TODO),
            Slot('B', null, SlotState.SKIPPED),
            Slot('C', iceCream, SlotState.TODO),
        ),
    )

    private fun started(): GameState =
        engine.reduce(engine.newGame(SEED, GameMode.CLASSIC, Level.A1, wheel), GameEvent.Start(0L)).state

    private fun GameState.after(vararg events: GameEvent): GameState =
        events.fold(this) { state, event -> engine.reduce(state, event).state }

    private fun GameState.typed(word: String): GameState = after(*keyTaps(this, word).toTypedArray())

    @Test
    fun `a game that has not started is loading and shows only playable letters`() {
        val ui = GameUiMapper.map(engine.newGame(SEED, GameMode.CLASSIC, Level.A1, wheel))

        assertTrue(ui.isLoading)
        assertEquals(listOf('A', 'C'), ui.wheel.map { it.letter })
        assertNull(ui.turn)
        assertFalse(ui.inputEnabled)
    }

    @Test
    fun `the current slot shows its clue and rule with empty boxes and unused keys`() {
        val ui = GameUiMapper.map(started())

        val turn = checkNotNull(ui.turn)
        assertFalse(ui.isLoading)
        assertTrue(ui.inputEnabled)
        assertEquals(WheelLetterState.Current, ui.wheel[0].state)
        assertEquals(WheelLetterState.Todo, ui.wheel[1].state)
        assertEquals('A', turn.letter)
        assertEquals(LetterRule.STARTS, turn.rule)
        assertEquals(apple.clue, turn.clue)
        assertEquals(PartOfSpeech.NOUN, turn.partOfSpeech)
        assertEquals(Level.A1, turn.level)
        assertEquals(5, turn.letterCount)
        assertEquals(
            listOf(AnswerSlotState.Active) + List(4) { AnswerSlotState.Empty },
            turn.words.single().map { it.state },
        )
        assertEquals(10, turn.keys.size)
        assertTrue(turn.keys.all { it.state == LetterKeyState.Normal })
    }

    @Test
    fun `typed letters fill the boxes in order and use their keys`() {
        val ui = GameUiMapper.map(started().typed("AP"))

        val cells = checkNotNull(ui.turn).words.single()
        assertEquals(listOf('A', 'P', null, null, null), cells.map { it.letter })
        assertEquals(
            listOf(AnswerSlotState.Filled, AnswerSlotState.Filled, AnswerSlotState.Active) +
                List(2) { AnswerSlotState.Empty },
            cells.map { it.state },
        )
        assertEquals(2, checkNotNull(ui.turn).keys.count { it.state == LetterKeyState.Used })
    }

    @Test
    fun `a wrong answer shows the correct one in red and blocks input`() {
        val ui = GameUiMapper.map(started().typed("APPEL"))

        val turn = checkNotNull(ui.turn)
        assertTrue(turn.isRevealing)
        assertFalse(ui.inputEnabled)
        assertEquals("APPLE", turn.words.single().map { it.letter }.joinToString(""))
        assertTrue(turn.words.single().all { it.state == AnswerSlotState.Wrong })
        assertEquals(WheelLetterState.Wrong, ui.wheel[0].state)
    }

    @Test
    fun `a pass turns the letter into a leaf and moves to the next one`() {
        val ui = GameUiMapper.map(started().after(GameEvent.Pass))

        assertEquals(WheelLetterState.Passed, ui.wheel[0].state)
        assertEquals(WheelLetterState.Current, ui.wheel[1].state)
        assertEquals('C', checkNotNull(ui.turn).letter)
        assertEquals(LetterRule.CONTAINS, checkNotNull(ui.turn).rule)
    }

    @Test
    fun `a multi-word answer is split into words without the space`() {
        val ui = GameUiMapper.map(started().after(GameEvent.Pass))

        assertEquals(listOf(3, 5), checkNotNull(ui.turn).words.map { it.size })
        assertEquals(8, checkNotNull(ui.turn).letterCount)
    }

    @Test
    fun `a correct answer turns the letter blue and adds the points`() {
        val ui = GameUiMapper.map(started().typed("APPLE"))

        assertEquals(WheelLetterState.Correct, ui.wheel[0].state)
        assertEquals(10, ui.score)
    }

    @Test
    fun `a paused game blocks input`() {
        val ui = GameUiMapper.map(started().after(GameEvent.Pause))

        assertTrue(ui.isPaused)
        assertFalse(ui.inputEnabled)
    }

    @Test
    fun `after game over the last turn stays visible`() {
        val playing = started()
        val before = GameUiMapper.map(playing)
        val finished = playing.after(GameEvent.Forfeit)
        assertEquals(GameStatus.FINISHED, finished.status)

        val ui = GameUiMapper.map(finished, previous = before)

        assertEquals(before.turn, ui.turn)
        assertFalse(ui.inputEnabled)
        assertEquals(WheelLetterState.Wrong, ui.wheel[0].state)
    }

    @Test
    fun `the clock shows the full time before start and the remaining part afterwards`() {
        val limit = checkNotNull(config.timeLimitMs(GameMode.CLASSIC))
        val notStarted = engine.newGame(SEED, GameMode.CLASSIC, Level.A1, wheel)

        assertEquals(
            GameClockUi(isTimed = true, remainingMs = limit, fraction = 1f, wholeSeconds = 180, isWarning = false),
            GameUiMapper.clock(notStarted, limit, config.timeWarningSeconds),
        )
        val clock = GameUiMapper.clock(started().after(GameEvent.Tick(72_500L)), limit, config.timeWarningSeconds)
        assertEquals(107_500L, clock.remainingMs)
        assertEquals(108, clock.wholeSeconds)
        assertEquals(107_500f / 180_000f, clock.fraction)
        assertFalse(clock.isWarning)
    }

    @Test
    fun `the last ten seconds are a warning`() {
        val limit = checkNotNull(config.timeLimitMs(GameMode.CLASSIC))
        val state = started()

        assertFalse(GameUiMapper.clock(state.after(GameEvent.Tick(169_999L)), limit, 10).isWarning)
        assertTrue(GameUiMapper.clock(state.after(GameEvent.Tick(170_000L)), limit, 10).isWarning)
    }

    @Test
    fun `untimed modes have no clock`() {
        assertEquals(GameClockUi.Untimed, GameUiMapper.clock(started(), timeLimitMs = null, warningSeconds = 10))
    }

    private companion object {
        const val SEED = 7L
    }
}
