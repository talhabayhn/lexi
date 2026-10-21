package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** GAME_RULES.md §7. */
class GameEngineJokerTest {
    private fun Play.joker(type: JokerType): Play = send(GameEvent.UseJoker(type))

    private fun Play.typed(): String = input.cells.joinToString("") { it?.letter?.toString() ?: "_" }

    @Test
    fun `reveal letter puts the correct letter into the next empty cell for 10 s`() {
        val game = play("APPLE", "BREAD").joker(JokerType.REVEAL_LETTER)

        assertEquals("AP___", game.typed())
        assertEquals(true, game.input.cells[1]?.revealed)
        assertEquals(170_000L, game.state.remainingMs)
        assertEquals(8, game.state.jokers.count(JokerType.REVEAL_LETTER))
        assertEquals(
            listOf<EngineEffect>(EngineEffect.JokerApplied(JokerType.REVEAL_LETTER), EngineEffect.LetterPlaced(1, 'P')),
            game.lastEffects,
        )
    }

    @Test
    fun `reveal letter never uses the given first letter`() {
        val game = play("APPLE", "BREAD").joker(JokerType.REVEAL_LETTER)

        assertEquals(true, game.input.cells[0]?.given)
        assertEquals(1, game.lastEffects.filterIsInstance<EngineEffect.LetterPlaced>().single().cellIndex)
    }

    @Test
    fun `reveal letter on a two-letter starts answer fills the last cell and submits`() {
        val game = play("GO", "HAT").joker(JokerType.REVEAL_LETTER)

        assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state)
    }

    @Test
    fun `reveal letter replaces typed letters from the first mistake`() {
        val game = play("APPLE", "BREAD").type("LE").joker(JokerType.REVEAL_LETTER)

        assertEquals("AP___", game.typed())
        val available = game.input.keys.filter { it.state == KeyState.AVAILABLE }.map { it.letter }
        assertTrue('L' in available && 'E' in available)
    }

    @Test
    fun `revealed letter cannot be removed with backspace`() {
        val game = play("APPLE", "BREAD").joker(JokerType.REVEAL_LETTER).type("P").send(GameEvent.Backspace)
        assertEquals("AP___", game.typed())
        val before = game.state

        game.send(GameEvent.Backspace)

        assertSame(before, game.state)
    }

    @Test
    fun `reveal letter in the last empty cell submits a correct answer`() {
        val game = play("APPLE", "BREAD").type("PPL").joker(JokerType.REVEAL_LETTER)

        assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state)
        assertEquals('B', game.currentLetter)
    }

    @Test
    fun `slot answered with a joker gets no streak bonus but keeps the streak`() {
        val game = play("APPLE", "BREAD", "CHAIR").answer().joker(JokerType.EXAMPLE).answer().answer()

        assertEquals(listOf(10, 10, 14), game.effectsOf<EngineEffect.Correct>().map { it.points })
        assertEquals(10 + 10 - 5 + 14, game.state.score)
    }

    @Test
    fun `remove distractors disables every distractor key for 15 s and returns placed ones`() {
        val game = play("APPLE", "BREAD").type("P")
        val distractor = game.input.keys.indexOfFirst { it.isDistractor }
        game.send(GameEvent.KeyTapped(distractor))

        game.joker(JokerType.REMOVE_DISTRACTORS)

        assertEquals("AP___", game.typed())
        assertTrue(game.input.keys.filter { it.isDistractor }.all { it.state == KeyState.DISABLED })
        assertTrue(game.input.keys.filterNot { it.isDistractor }.none { it.state == KeyState.DISABLED })
        assertEquals(165_000L, game.state.remainingMs)
        game.send(GameEvent.KeyTapped(distractor))
        assertEquals("AP___", game.typed())
    }

    @Test
    fun `remove distractors is refused when the slot has no distractors`() {
        val game = play("BASKETBALL")
        val before = game.state

        game.joker(JokerType.REMOVE_DISTRACTORS)

        assertSame(before, game.state)
    }

    @Test
    fun `example costs 5 points and listen costs 10 points`() {
        val game = play("APPLE", "BREAD", "CHAIR", "DOG").answer().answer().joker(JokerType.EXAMPLE)
        assertEquals(17, game.state.score)

        game.answer().joker(JokerType.LISTEN)

        assertEquals(17 + 10 - 10, game.state.score)
        assertEquals(setOf(JokerType.LISTEN), game.input.jokersUsed)
        assertEquals(180_000L, game.state.remainingMs)
    }

    @Test
    fun `joker point cost never makes the score negative`() {
        val game = play("APPLE", "BREAD").joker(JokerType.LISTEN)

        assertEquals(0, game.state.score)
    }

    @Test
    fun `only one joker of any type can be used per slot`() {
        for (second in JokerType.entries) {
            val game = play("APPLE", "BREAD").joker(JokerType.EXAMPLE)
            val before = game.state

            game.joker(second)

            assertSame(before, game.state, "$second")
            assertEquals(8, game.state.jokers.count(JokerType.EXAMPLE))
        }
    }

    @Test
    fun `a slot that got a joker gets no other joker on a later lap`() {
        val game = play("APPLE", "BREAD").joker(JokerType.REVEAL_LETTER).send(GameEvent.Pass).answer()
        assertEquals('A', game.currentLetter)
        val before = game.state

        for (type in JokerType.entries) game.joker(type)

        assertSame(before, game.state)
    }

    @Test
    fun `the same joker can be used again on another slot`() {
        val game = play("APPLE", "BREAD").joker(JokerType.EXAMPLE).send(GameEvent.Pass).joker(JokerType.EXAMPLE)

        assertEquals(7, game.state.jokers.count(JokerType.EXAMPLE))
    }

    @Test
    fun `revealed letter survives a pass while typed letters are cleared`() {
        val game = play("APPLE", "BREAD").joker(JokerType.REVEAL_LETTER).type("PL").send(GameEvent.Pass).answer()

        assertEquals('A', game.currentLetter)
        assertEquals("AP___", game.typed())
        assertEquals(true, game.input.cells[0]?.given)
        assertEquals(true, game.input.cells[1]?.revealed)
        assertEquals(2, game.input.keys.count { it.state == KeyState.USED })
        game.type("PLE")
        assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state)
    }

    @Test
    fun `reveal letter on a contains slot skips the given letter`() {
        val cat = card("CAT", letter = 'A', rule = LetterRule.CONTAINS)
        val game = play(wheel = Wheel(listOf(Slot('A', cat, SlotState.TODO)))).joker(JokerType.REVEAL_LETTER)

        assertEquals("CA_", game.typed())
        assertEquals(true, game.input.cells[0]?.revealed)
        assertEquals(true, game.input.cells[1]?.given)
        game.joker(JokerType.REVEAL_LETTER)
        assertEquals("CA_", game.typed())
        game.type("T")
        assertEquals(SlotState.CORRECT, game.state.wheel.slots[0].state)
    }

    @Test
    fun `disabled distractor keys stay disabled after a pass`() {
        val game = play("APPLE", "BREAD").joker(JokerType.REMOVE_DISTRACTORS).type("P").send(GameEvent.Pass).answer()

        assertEquals("A____", game.typed())
        assertTrue(game.input.keys.filter { it.isDistractor }.all { it.state == KeyState.DISABLED })
    }

    @Test
    fun `jokers used on a slot stay used when it comes back on the next lap`() {
        val game = play("APPLE", "BREAD").joker(JokerType.EXAMPLE).send(GameEvent.Pass).answer()
        assertEquals('A', game.currentLetter)
        val before = game.state

        game.joker(JokerType.EXAMPLE)

        assertSame(before, game.state)
        assertEquals(setOf(JokerType.EXAMPLE), game.input.jokersUsed)
    }

    @Test
    fun `jokers need inventory and consume one`() {
        val empty = play("APPLE", jokers = JokerInventory())
        val before = empty.state
        empty.joker(JokerType.EXAMPLE)
        assertSame(before, empty.state)

        val one = play("APPLE", jokers = JokerInventory(mapOf(JokerType.EXAMPLE to 1))).joker(JokerType.EXAMPLE)
        assertEquals(0, one.state.jokers.count(JokerType.EXAMPLE))
    }

    @Test
    fun `jokers are not allowed in daily duel and level test`() {
        for (mode in listOf(GameMode.DAILY, GameMode.DUEL, GameMode.LEVEL_TEST)) {
            val game = play("APPLE", mode = mode)
            val before = game.state

            game.joker(JokerType.EXAMPLE)

            assertSame(before, game.state, "$mode")
        }
    }

    @Test
    fun `jokers are allowed in practice and review without a clock`() {
        for (mode in listOf(GameMode.PRACTICE, GameMode.REVIEW)) {
            val game = play("APPLE", "BREAD", mode = mode).joker(JokerType.REVEAL_LETTER)

            assertEquals("AP___", game.typed(), "$mode")
            assertNull(game.state.remainingMs)
            assertEquals(0, game.state.score)
        }
    }

    @Test
    fun `listen is disabled for C1 and NAT cards`() {
        for (level in listOf(Level.C1, Level.NAT)) {
            val game = play("APPLE", level = level)
            val before = game.state
            game.joker(JokerType.LISTEN)
            assertSame(before, game.state, "$level")
        }
        val b2 = play("APPLE", level = Level.B2).joker(JokerType.LISTEN)
        assertEquals(setOf(JokerType.LISTEN), b2.input.jokersUsed)
    }

    @Test
    fun `time joker is refused when it would use up the remaining time`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Tick(170_000L))
        val before = game.state

        game.joker(JokerType.REVEAL_LETTER).joker(JokerType.REMOVE_DISTRACTORS)
        assertSame(before, game.state)

        game.joker(JokerType.EXAMPLE)
        assertEquals(setOf(JokerType.EXAMPLE), game.input.jokersUsed)
    }

    @Test
    fun `time joker cost counts toward the time warnings`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Tick(160_000L)).joker(JokerType.REVEAL_LETTER)

        assertEquals(10_000L, game.state.remainingMs)
        assertTrue(EngineEffect.TimeWarning(10) in game.lastEffects)
    }

    @Test
    fun `time joker that skips several warning seconds emits only the lowest one`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Tick(165_000L)).joker(JokerType.REVEAL_LETTER)

        assertEquals(listOf(5), game.lastEffects.filterIsInstance<EngineEffect.TimeWarning>().map { it.secondsLeft })
    }

    @Test
    fun `canUseJoker matches what reduce applies in every situation`() {
        val engine = GameEngine()
        val situations = listOf(
            play("APPLE", "BREAD", start = false),
            play("APPLE", "BREAD"),
            play("APPLE", "BREAD", jokers = JokerInventory()),
            play("APPLE", "BREAD").send(GameEvent.Pause),
            play("APPLE", "BREAD").answerWrong(),
            play("APPLE", "BREAD").joker(JokerType.EXAMPLE),
            play("APPLE", "BREAD").send(GameEvent.Tick(170_000L)),
            play("APPLE", "BREAD", mode = GameMode.DAILY),
            play("APPLE", "BREAD", level = Level.C1),
            play("APPLE", "BREAD", mode = GameMode.PRACTICE),
            play("APPLE", "BREAD").joker(JokerType.REMOVE_DISTRACTORS).send(GameEvent.Pass, GameEvent.Pass),
            play("APPLE").answer(),
        )
        for ((index, game) in situations.withIndex()) {
            for (type in JokerType.entries) {
                val applied = engine.reduce(game.state, GameEvent.UseJoker(type)).state !== game.state
                assertEquals(applied, engine.canUseJoker(game.state, type), "situation $index, $type")
            }
        }
    }
}
