package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** GAME_RULES.md §2–§4. */
class GameEngineTurnFlowTest {
    @Test
    fun `start makes the first slot current`() {
        val game = play("APPLE", "BREAD", "CHAIR")

        assertEquals(GameStatus.PLAYING, game.state.status)
        assertEquals('A', game.currentLetter)
        assertEquals(SlotState.CURRENT, game.state.wheel.slots[0].state)
        assertEquals(listOf<EngineEffect>(EngineEffect.Advanced(toLetter = 'A', lap = 1)), game.effects)
        assertEquals(1_000L, game.state.startedAtMs)
    }

    @Test
    fun `start skips skipped slots`() {
        val base = wheelOf("BREAD", "CHAIR")
        val wheel = Wheel(listOf(Slot('A', null, SlotState.SKIPPED)) + base.slots)

        assertEquals('B', play(wheel = wheel).currentLetter)
    }

    @Test
    fun `start on a wheel without playable slots ends the game`() {
        val game = play(wheel = Wheel(listOf(Slot('Q', null, SlotState.SKIPPED))))

        assertEquals(GameStatus.FINISHED, game.state.status)
        val summary = checkNotNull(game.state.summary)
        assertEquals(GameOverReason.EMPTY_WHEEL, summary.reason)
        assertEquals(false, summary.completed)
        assertEquals(0, summary.timeBonus)
        assertEquals(0, summary.score)
    }

    @Test
    fun `events before start are ignored`() {
        val game = play("APPLE", start = false)
        val before = game.state

        game.send(GameEvent.KeyTapped(0), GameEvent.Pass, GameEvent.Tick(500L), GameEvent.Backspace)

        assertSame(before, game.state)
        assertTrue(game.effects.isEmpty())
    }

    @Test
    fun `second start is ignored`() {
        val game = play("APPLE", "BREAD").pass()

        game.send(GameEvent.Start(timestampMs = 9_999L))

        assertEquals('B', game.currentLetter)
        assertTrue(game.lastEffects.isEmpty())
    }

    @Test
    fun `current slot keys come from the keys sub-seed of its letter`() {
        val game = play("APPLE", seed = 77L)

        val expected = KeyboardGenerator.generate("APPLE", SeededRandom(77L).derive(KeyboardGenerator.seedPurpose('A')))
        assertEquals(expected, game.input.keys)
        assertEquals(5, game.input.cells.size)
    }

    @Test
    fun `tapping a key places its letter in the first empty cell and marks the key used`() {
        val game = play("APPLE")
        val index = game.input.keys.indexOfFirst { it.letter == 'A' }

        game.send(GameEvent.KeyTapped(index))

        assertEquals(PlacedLetter('A', keyIndex = index, revealed = false), game.input.cells[0])
        assertEquals(KeyState.USED, game.input.keys[index].state)
        assertEquals(listOf<EngineEffect>(EngineEffect.LetterPlaced(cellIndex = 0, letter = 'A')), game.lastEffects)
    }

    @Test
    fun `tapping a used or unknown key is ignored`() {
        val game = play("APPLE").type("A")
        val used = game.input.keys.indexOfFirst { it.state == KeyState.USED }
        val before = game.state

        game.send(GameEvent.KeyTapped(used), GameEvent.KeyTapped(-1), GameEvent.KeyTapped(99))

        assertSame(before, game.state)
    }

    @Test
    fun `backspace returns the last placed letter to its key`() {
        val game = play("APPLE").type("AP")
        val keyOfP = checkNotNull(game.input.cells[1]).keyIndex

        game.send(GameEvent.Backspace)

        assertNull(game.input.cells[1])
        assertEquals(KeyState.AVAILABLE, game.input.keys[keyOfP].state)
        assertEquals('A', game.input.cells[0]?.letter)
    }

    @Test
    fun `backspace without placed letters is ignored`() {
        val game = play("APPLE")
        val before = game.state

        game.send(GameEvent.Backspace)

        assertSame(before, game.state)
    }

    @Test
    fun `keys are never reordered during a slot`() {
        val game = play("APPLE")
        val order = game.input.keys.map { it.letter }

        game.type("APP").send(GameEvent.Backspace).type("P")

        assertEquals(order, game.input.keys.map { it.letter })
    }

    @Test
    fun `filling all cells with the answer submits it as correct and advances`() {
        val game = play("APPLE", "BREAD").answer()

        assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state)
        assertEquals('B', game.currentLetter)
        assertTrue(game.effects.any { it is EngineEffect.Correct && it.letter == 'A' })
        assertTrue(game.lastEffects.last() == EngineEffect.Advanced(toLetter = 'B', lap = 1))
    }

    @Test
    fun `fixed spaces of multi-word answers need no key`() {
        val game = play("GIVE UP")

        assertEquals(6, game.input.cells.size)
        game.type("GIVEUP")

        assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state)
    }

    @Test
    fun `filling all cells with a wrong word submits it as wrong`() {
        val game = play("APPLE", "BREAD").answerWrong()

        assertEquals(SlotState.WRONG, game.state.wheel.slots[0].state)
        assertEquals(EngineEffect.Wrong(letter = 'A', card = CardId("a1-apple")), game.lastEffects.last())
    }

    @Test
    fun `wrong answer is revealed for 1200 ms before advancing`() {
        val game = play("APPLE", "BREAD").answerWrong()

        assertEquals(1_200L, game.state.revealRemainingMs)
        game.send(GameEvent.Tick(1_199L))
        assertEquals('A', game.currentLetter)
        game.send(GameEvent.Tick(1L))

        assertEquals('B', game.currentLetter)
        assertEquals(0L, game.state.revealRemainingMs)
        assertEquals(listOf<EngineEffect>(EngineEffect.Advanced(toLetter = 'B', lap = 1)), game.lastEffects)
    }

    @Test
    fun `input is ignored while the answer is revealed`() {
        val game = play("APPLE", "BREAD").answerWrong()
        val before = game.state

        game.send(GameEvent.KeyTapped(0), GameEvent.Pass, GameEvent.Backspace, GameEvent.UseJoker(JokerType.EXAMPLE))

        assertSame(before, game.state)
    }

    @Test
    fun `pass marks the slot passed and advances`() {
        val game = play("APPLE", "BREAD").pass()

        assertEquals(SlotState.PASSED, game.state.wheel.slots[0].state)
        assertEquals('B', game.currentLetter)
        assertEquals(
            listOf<EngineEffect>(EngineEffect.Passed(letter = 'A'), EngineEffect.Advanced(toLetter = 'B', lap = 1)),
            game.lastEffects,
        )
    }

    @Test
    fun `advance skips answered slots and wraps to passed slots on the next lap`() {
        val game = play("APPLE", "BREAD", "CHAIR", "DOG")

        game.pass().answer().pass().answer()

        assertEquals('A', game.currentLetter)
        assertEquals(2, game.state.lap)
        assertEquals(EngineEffect.Advanced(toLetter = 'A', lap = 2), game.lastEffects.last())
        game.answer()
        assertEquals('C', game.currentLetter)
        assertEquals(SlotState.CURRENT, game.state.wheel.slots[2].state)
    }

    @Test
    fun `the only passed slot comes back on the next lap`() {
        val game = play("APPLE").pass()

        assertEquals('A', game.currentLetter)
        assertEquals(2, game.state.lap)
        assertEquals(SlotState.CURRENT, game.state.wheel.slots[0].state)
    }

    @Test
    fun `typed letters are kept when a passed slot comes back`() {
        val game = play("APPLE", "BREAD").type("AP").pass().answer()

        assertEquals('A', game.currentLetter)
        assertEquals("AP", game.input.cells.mapNotNull { it?.letter }.joinToString(""))
    }

    @Test
    fun `game ends when no todo or passed slots are left`() {
        val game = play("APPLE", "BREAD").answer().answerWrongAndWait()

        assertEquals(GameStatus.FINISHED, game.state.status)
        assertNull(game.state.currentIndex)
        val summary = checkNotNull(game.state.summary)
        assertEquals(GameOverReason.ALL_ANSWERED, summary.reason)
        assertEquals(EngineEffect.GameOver(summary), game.lastEffects.last())
    }

    @Test
    fun `wrong answer on the last open slot ends the game after the reveal`() {
        val game = play("APPLE", "BREAD").answer().answerWrong()
        assertEquals(GameStatus.PLAYING, game.state.status)
        assertTrue(game.state.isRevealing)

        game.send(GameEvent.Tick(1_200L))

        val summary = checkNotNull(game.state.summary)
        assertEquals(GameOverReason.ALL_ANSWERED, summary.reason)
        assertEquals(true, summary.completed)
        assertEquals(1, summary.wrong)
        assertTrue(game.lastEffects.none { it is EngineEffect.Advanced })
    }

    @Test
    fun `forfeit ends the game`() {
        val game = play("APPLE", "BREAD").answer().send(GameEvent.Forfeit)

        assertEquals(GameStatus.FINISHED, game.state.status)
        assertEquals(GameOverReason.FORFEIT, game.state.summary?.reason)
        assertEquals(1, game.state.summary?.unanswered)
    }

    @Test
    fun `events after game over are ignored`() {
        val game = play("APPLE").send(GameEvent.Forfeit)
        val before = game.state

        game.send(GameEvent.Start(0L), GameEvent.Tick(100L), GameEvent.Pass, GameEvent.Forfeit, GameEvent.Resume)

        assertSame(before, game.state)
        assertTrue(game.lastEffects.isEmpty())
    }

    @Test
    fun `same seed and events give the same state`() {
        fun run() = play("APPLE", "BREAD", "CHAIR", seed = 5L)
            .type("AP").pass().answerWrong().send(GameEvent.Tick(2_000L)).answer().state

        assertEquals(run(), run())
    }

    private fun Play.pass(): Play = send(GameEvent.Pass)
}
