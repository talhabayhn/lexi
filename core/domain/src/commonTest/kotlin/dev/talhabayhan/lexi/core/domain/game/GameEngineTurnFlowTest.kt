package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
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
        assertEquals(expected.map { it.letter }, game.input.keys.map { it.letter })
        assertEquals(expected.map { it.isDistractor }, game.input.keys.map { it.isDistractor })
        assertEquals(5, game.input.cells.size)
    }

    @Test
    fun `tapping a key places its letter in the first empty cell and marks the key used`() {
        val game = play("APPLE")
        val index = game.input.keys.indexOfFirst { it.letter == 'P' }

        game.send(GameEvent.KeyTapped(index))

        assertEquals(PlacedLetter('P', keyIndex = index, revealed = false), game.input.cells[1])
        assertEquals(KeyState.USED, game.input.keys[index].state)
        assertEquals(listOf<EngineEffect>(EngineEffect.LetterPlaced(cellIndex = 1, letter = 'P')), game.lastEffects)
    }

    @Test
    fun `tapping a used or unknown key is ignored`() {
        val game = play("APPLE").type("P")
        val used = game.input.keys.indexOfFirst { it.state == KeyState.USED }
        val before = game.state

        game.send(GameEvent.KeyTapped(used), GameEvent.KeyTapped(-1), GameEvent.KeyTapped(99))

        assertSame(before, game.state)
    }

    @Test
    fun `backspace returns the last placed letter to its key`() {
        val game = play("APPLE").type("PP")
        val keyOfP = checkNotNull(game.input.cells[2]).keyIndex

        game.send(GameEvent.Backspace)

        assertNull(game.input.cells[2])
        assertEquals(KeyState.AVAILABLE, game.input.keys[keyOfP].state)
        assertEquals('P', game.input.cells[1]?.letter)
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

        game.type("PPL").send(GameEvent.Backspace).type("L")

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
        game.type("IVEUP")

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
    fun `typed letters are cleared by a pass so the slot comes back empty`() {
        val game = play("APPLE", "BREAD").type("PP").pass().answer()

        assertEquals('A', game.currentLetter)
        assertTrue(game.input.cells.drop(1).all { it == null })
        assertEquals(true, game.input.cells[0]?.given)
        assertTrue(game.input.placementOrder.isEmpty())
        assertEquals(1, game.input.keys.count { it.state == KeyState.USED })
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
    fun `forfeit ends the game and every remaining letter counts as wrong`() {
        val game = play("APPLE", "BREAD", "CHAIR", "DOG").answer().send(GameEvent.Pass).answer()
        assertEquals('D', game.currentLetter)

        game.send(GameEvent.Forfeit)

        assertEquals(GameStatus.FINISHED, game.state.status)
        val summary = checkNotNull(game.state.summary)
        assertEquals(GameOverReason.FORFEIT, summary.reason)
        assertEquals(2, summary.correct)
        assertEquals(2, summary.wrong)
        assertEquals(0, summary.passed)
        assertEquals(0, summary.unanswered)
        assertEquals(listOf(CardId("a1-bread"), CardId("a1-dog")), summary.missedCards)
        assertEquals(
            listOf(SlotState.CORRECT, SlotState.WRONG, SlotState.CORRECT, SlotState.WRONG),
            game.state.wheel.slots.map { it.state },
        )
        assertEquals(listOf<EngineEffect>(EngineEffect.GameOver(summary)), game.lastEffects)
    }

    @Test
    fun `forfeit leaves skipped slots skipped`() {
        val wheel = Wheel(wheelOf("APPLE").slots + Slot('Q', null, SlotState.SKIPPED))
        val summary = checkNotNull(play(wheel = wheel).send(GameEvent.Forfeit).state.summary)

        assertEquals(1, summary.wrong)
        assertEquals(1, summary.skipped)
    }

    @Test
    fun `forfeit before start ends the game with every letter wrong and the full time left`() {
        val game = play("APPLE", "BREAD", start = false).send(GameEvent.Forfeit)

        val summary = checkNotNull(game.state.summary)
        assertEquals(GameOverReason.FORFEIT, summary.reason)
        assertEquals(2, summary.wrong)
        assertEquals(180_000L, summary.remainingMs)
        assertNull(game.state.startedAtMs)
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
            .type("PP").pass().answerWrong().send(GameEvent.Tick(2_000L)).answer().state

        assertEquals(run(), run())
    }

    @Test
    fun `starts slot opens with its letter locked in the first cell and its key used`() {
        val game = play("APPLE")

        val given = checkNotNull(game.input.cells[0])
        assertEquals(PlacedLetter('A', keyIndex = given.keyIndex, revealed = false, given = true), given)
        assertEquals(KeyState.USED, game.input.keys[given.keyIndex].state)
        assertEquals(given.keyIndex, game.input.keys.indexOfFirst { it.letter == 'A' })
        assertTrue(game.input.cells.drop(1).all { it == null })
        assertTrue(game.input.placementOrder.isEmpty())
    }

    @Test
    fun `backspace never removes the given first letter`() {
        val game = play("APPLE").type("P").send(GameEvent.Backspace)
        val before = game.state

        game.send(GameEvent.Backspace)

        assertSame(before, game.state)
        assertEquals(true, game.input.cells[0]?.given)
    }

    @Test
    fun `guess with the given first letter is judged on the full word`() {
        val right = play("APPLE", "BREAD").type("PPLE")
        assertEquals(SlotState.CORRECT, right.state.wheel.slots[0].state)

        val wrong = play("APPLE", "BREAD").type("PPEL")
        assertEquals(SlotState.WRONG, wrong.state.wheel.slots[0].state)
    }

    @Test
    fun `contains slot opens with its letter locked at the first occurrence and its key used`() {
        val game = play(wheel = containsWheel("BANANA", 'N'))

        val given = checkNotNull(game.input.cells[2])
        assertEquals(PlacedLetter('N', keyIndex = given.keyIndex, revealed = false, given = true), given)
        assertEquals(given.keyIndex, game.input.keys.indexOfFirst { it.letter == 'N' })
        assertEquals(KeyState.USED, game.input.keys[given.keyIndex].state)
        assertEquals(1, game.input.keys.count { it.state == KeyState.USED })
        assertTrue(game.input.cells.filterIndexed { index, _ -> index != 2 }.all { it == null })
        assertTrue(game.input.placementOrder.isEmpty())
    }

    @Test
    fun `contains slot typing fills around the given letter and is judged on the full word`() {
        val right = play(wheel = containsWheel("CAT", 'A')).type("CT")
        assertEquals(SlotState.CORRECT, right.state.wheel.slots[0].state)

        val wrong = play(wheel = containsWheel("CAT", 'A')).type("TC")
        assertEquals(SlotState.WRONG, wrong.state.wheel.slots[0].state)
    }

    @Test
    fun `contains slot letter in a multi-word answer is given at its letter cell`() {
        val game = play(wheel = containsWheel("ICE CREAM", 'R'))

        assertEquals('R', game.input.cells[4]?.letter)
        assertEquals(true, game.input.cells[4]?.given)
    }

    @Test
    fun `backspace and pass keep the given contains letter`() {
        val game = play(wheel = containsWheel("CAT", 'A'), start = false)
        game.send(GameEvent.Start(1_000L)).type("C").send(GameEvent.Backspace, GameEvent.Backspace)

        assertEquals(true, game.input.cells[1]?.given)
        assertTrue(game.input.cells[0] == null)

        val passed = play(wheel = Wheel(containsWheel("CAT", 'A').slots + Slot('B', card("BREAD"), SlotState.TODO)))
            .type("C").send(GameEvent.Pass, GameEvent.Pass)
        assertEquals('A', passed.currentLetter)
        assertEquals(true, passed.input.cells[1]?.given)
        assertTrue(passed.input.cells[0] == null && passed.input.cells[2] == null)
    }

    @Test
    fun `games without the contains letter rule open contains slots with empty cells`() {
        val engine = GameEngine()
        val initial = engine.newGame(
            1L,
            GameMode.CLASSIC,
            Level.A1,
            containsWheel("CAT", 'A'),
            prefillsContainsLetter = false,
        )
        val game = Play(initial, engine).send(GameEvent.Start(0L))

        assertTrue(game.input.cells.all { it == null })
        assertTrue(game.input.keys.none { it.state == KeyState.USED })
        game.type("CAT")
        assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state)
    }

    @Test
    fun `property - contains slots are solvable with the given letter at its first occurrence`() {
        val answers = listOf("CAT", "BANANA", "ICE CREAM", "TAXI", "BOX", "PIZZA", "QUEEN", "MAJOR", "ALLOW", "KIWI")
        for (answer in answers) {
            val letters = answer.replace(" ", "")
            for (letter in letters.drop(1).toSet() - letters.first()) {
                for (seed in 1L..20L) {
                    val game = play(wheel = containsWheel(answer, letter), seed = seed)
                    val first = letters.indexOf(letter)
                    assertEquals(
                        PlacedLetter(letter, game.input.cells[first]!!.keyIndex, revealed = false, given = true),
                        game.input.cells[first],
                        "$answer/$letter/$seed",
                    )
                    assertEquals(1, game.input.cells.count { it != null }, "$answer/$letter/$seed")
                    val open = letters.filterIndexed { index, _ -> index != first }
                    val available = game.input.keys.filter { it.state == KeyState.AVAILABLE }.map { it.letter }
                    assertEquals(
                        open.groupingBy { it }.eachCount(),
                        available.filter { it in open }.groupingBy { it }.eachCount(),
                        "$answer/$letter/$seed",
                    )
                    game.answer()
                    assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state, "$answer/$letter/$seed")
                }
            }
        }
    }

    private fun containsWheel(answer: String, letter: Char): Wheel =
        Wheel(listOf(Slot(letter, card(answer, letter = letter, rule = LetterRule.CONTAINS), SlotState.TODO)))

    @Test
    fun `games without the given letter rule open with empty cells`() {
        val engine = GameEngine()
        val initial = engine.newGame(1L, GameMode.CLASSIC, Level.A1, wheelOf("APPLE"), prefillsFirstLetter = false)
        val game = Play(initial, engine).send(GameEvent.Start(0L))

        assertTrue(game.input.cells.all { it == null })
        game.type("APPLE")
        assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state)
    }

    private fun Play.pass(): Play = send(GameEvent.Pass)
}
