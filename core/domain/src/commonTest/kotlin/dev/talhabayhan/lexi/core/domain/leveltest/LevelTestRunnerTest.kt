package dev.talhabayhan.lexi.core.domain.leveltest

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.fullAlphabetPool
import dev.talhabayhan.lexi.core.domain.game.EngineEffect
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.game.KeyState
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** GAME_RULES.md §9 (level test run: one one-slot game per card on a shared clock). */
class LevelTestRunnerTest {
    private val config = GameConfig()
    private val runner = LevelTestRunner(GameEngine(config))
    private val pool: List<Card> = Level.entries.flatMap { fullAlphabetPool(level = it, suffix = "ON") }

    /** Drives a [LevelTestRunner] and records every effect. */
    private inner class Driver(seed: Long = 7L, private val cards: List<Card> = pool) {
        val effects = mutableListOf<EngineEffect>()
        var lastEffects: List<EngineEffect> = emptyList()
            private set
        var run: LevelTestRun = runner.start(seed, cards, timestampMs = 1_000L).also { effects += it.effects }.state
            private set

        val game: GameState get() = checkNotNull(run.game) { "no card is shown" }

        fun send(vararg events: GameEvent): Driver {
            val collected = mutableListOf<EngineEffect>()
            for (event in events) {
                val transition = runner.reduce(run, cards, event)
                run = transition.state
                collected += transition.effects
            }
            lastEffects = collected
            effects += collected
            return this
        }

        private fun type(letters: String): Driver {
            for (letter in letters) {
                val keys = checkNotNull(game.currentInput).keys
                val index = keys.indexOfFirst { it.letter == letter && it.state == KeyState.AVAILABLE }
                check(index >= 0) { "no available key '$letter'" }
                send(GameEvent.KeyTapped(index))
            }
            return this
        }

        /** Open (not given) letters of the current card's answer. */
        private fun open(): String {
            val input = checkNotNull(game.currentInput)
            return checkNotNull(game.currentSlot?.card).answer
                .filterIndexed { index, _ -> input.cells[index]?.isLocked != true }
        }

        fun right(): Driver = type(open())

        /** "ON" → "NO": always wrong for the `<letter>ON` pool. */
        fun wrong(): Driver = type(open().reversed())

        fun answers(vararg correct: Boolean): Driver {
            for (ok in correct) {
                if (ok) right() else wrong().send(GameEvent.Tick(config.revealMs))
            }
            return this
        }
    }

    @Test
    fun `run starts the first card on a one-slot level-test wheel with the full clock`() {
        val test = Driver(seed = 7L)

        val expected = LevelTest.nextCard(LevelTest.start(7L), pool)
        assertEquals(expected, test.game.currentSlot?.card)
        assertEquals(1, test.game.wheel.slots.size)
        assertEquals(SlotState.CURRENT, test.game.wheel.slots.single().state)
        assertEquals(GameMode.LEVEL_TEST, test.game.mode)
        assertEquals(7L, test.game.seed)
        assertEquals(120_000L, test.run.remainingMs)
        assertEquals(120_000L, test.game.remainingMs)
        assertEquals<List<EngineEffect>>(
            listOf(EngineEffect.Advanced(toLetter = checkNotNull(expected).letter, lap = 1)),
            test.effects,
        )
        assertNull(test.run.result)
        assertFalse(test.run.isFinished)
    }

    @Test
    fun `same seed and events give the same run`() {
        fun play(seed: Long) = Driver(seed).answers(true, false).send(GameEvent.Tick(2_345L), GameEvent.Pass)
            .answers(true, true).run

        assertEquals(play(11L), play(11L))
        assertNotEquals(play(11L).asked.map { it.card }, play(12L).asked.map { it.card })
    }

    @Test
    fun `correct answer goes straight to the next card`() {
        val test = Driver()
        val first = test.game.currentSlot?.card

        test.right()

        assertEquals(listOf(true), test.run.test.answers.map { it.correct })
        assertEquals(SlotState.CORRECT, test.run.asked.single().state)
        assertEquals(first, test.run.asked.single().card)
        assertNotEquals(first, test.game.currentSlot?.card)
        assertEquals(SlotState.CURRENT, test.game.wheel.slots.single().state)
        assertTrue(test.lastEffects.any { it is EngineEffect.Correct })
        assertTrue(test.lastEffects.last() is EngineEffect.Advanced)
        assertTrue(test.effects.none { it is EngineEffect.GameOver })
    }

    @Test
    fun `wrong answer waits for the answer reveal and then continues`() {
        val test = Driver().wrong()
        val first = test.game.currentSlot?.card

        assertTrue(test.game.isRevealing)
        assertTrue(test.run.test.answers.isEmpty())

        test.send(GameEvent.Tick(config.revealMs - 1))
        assertEquals(first, test.game.currentSlot?.card)

        test.send(GameEvent.Tick(1L))
        assertEquals(listOf(false), test.run.test.answers.map { it.correct })
        assertEquals(SlotState.WRONG, test.run.asked.single().state)
        assertEquals(Level.A1, test.run.test.currentLevel)
        assertEquals(Level.A1, test.game.currentSlot?.card?.level)
        assertEquals(120_000L, test.run.remainingMs, "the clock stands still during the reveal")
    }

    @Test
    fun `pass ends the card at once`() {
        val test = Driver()
        val first = checkNotNull(test.game.currentSlot?.card)

        test.send(GameEvent.Pass)

        assertEquals(listOf(false), test.run.test.answers.map { it.correct })
        assertEquals(SlotState.PASSED, test.run.asked.single().state)
        assertEquals(first, test.run.asked.single().card)
        assertNotEquals(first, test.game.currentSlot?.card)
        assertTrue(EngineEffect.Passed(first.letter) in test.lastEffects)
        assertTrue(test.lastEffects.none { it is EngineEffect.Advanced && it.lap > 1 }, "no second lap")
    }

    @Test
    fun `remaining time of the shared clock carries into the next card`() {
        val test = Driver().send(GameEvent.Tick(5_000L)).right().send(GameEvent.Tick(2_500L), GameEvent.Pass)

        assertEquals(112_500L, test.run.remainingMs)
        assertEquals(112_500L, test.game.remainingMs)
    }

    @Test
    fun `ticks are additive across the end of a wrong card`() {
        val split = Driver().wrong().send(GameEvent.Tick(config.revealMs), GameEvent.Tick(4_000L)).run
        val merged = Driver().wrong().send(GameEvent.Tick(config.revealMs + 4_000L)).run

        assertEquals(split, merged)
        assertEquals(116_000L, merged.remainingMs)
    }

    @Test
    fun `time up mid-card ends the test with the result of the answers so far`() {
        val test = Driver().answers(true, true, true).send(GameEvent.Tick(120_000L))

        assertTrue(test.run.isFinished)
        assertNull(test.run.game)
        assertEquals(0L, test.run.remainingMs)
        assertEquals(3, test.run.test.answers.size)
        assertEquals(3, test.run.asked.size)
        assertEquals(LevelTest.result(test.run.test), test.run.result)
        assertTrue(test.effects.none { it is EngineEffect.GameOver })
    }

    @Test
    fun `test stops after 10 answers once the current level can be judged`() {
        val test = Driver().answers(false, true, false, true, false, true, false, true, false)
        assertFalse(test.run.isFinished)

        test.answers(false)

        assertTrue(test.run.isFinished)
        assertNull(test.run.game)
        assertEquals(10, test.run.asked.size)
        assertEquals(Level.A1, test.run.result)
        assertEquals(test.run.asked.map { it.card?.id }, test.run.test.answers.map { it.cardId })
    }

    @Test
    fun `test always stops after 14 answers`() {
        val test = Driver().answers(*BooleanArray(8) { false }).answers(true, true, true, true, true)
        assertFalse(test.run.isFinished)

        test.answers(true)

        assertTrue(test.run.isFinished)
        assertEquals(LevelTest.MAX_SLOTS, test.run.test.answers.size)
        assertEquals(LevelTest.result(test.run.test), test.run.result)
    }

    @Test
    fun `finished run ignores further events`() {
        val test = Driver().send(GameEvent.Tick(120_000L))
        val finished = test.run

        test.send(GameEvent.Tick(1_000L), GameEvent.Pass, GameEvent.Resume)

        assertSame(finished, test.run)
        assertTrue(test.lastEffects.isEmpty())
    }

    @Test
    fun `test ends early when the pool has no unasked card of the current level`() {
        val small = listOf(card("APPLE", level = Level.A2), card("BREAD", level = Level.A1))
        val test = Driver(cards = small).answers(true)

        assertTrue(test.run.isFinished)
        assertEquals(Level.A1, test.run.result)
    }

    @Test
    fun `empty pool makes the test unavailable`() {
        for (cards in listOf(emptyList(), listOf(card("BREAD", level = Level.B1)))) {
            val transition = runner.start(seed = 1L, pool = cards, timestampMs = 0L)

            assertTrue(transition.state.isUnavailable)
            assertNull(transition.state.game)
            assertNull(transition.state.result)
            assertFalse(transition.state.isFinished)
            assertTrue(transition.effects.isEmpty())
            assertSame(transition.state, runner.reduce(transition.state, cards, GameEvent.Pass).state)
        }
    }

    @Test
    fun `jokers are refused`() {
        val test = Driver()
        assertEquals(JokerInventory(), test.game.jokers)
        val before = test.run

        for (type in JokerType.entries) test.send(GameEvent.UseJoker(type))

        assertSame(before, test.run)
        assertTrue(test.lastEffects.isEmpty())
    }

    @Test
    fun `start and forfeit events are ignored by the run`() {
        val test = Driver()
        val before = test.run

        test.send(GameEvent.Start(5L, remainingMs = 1L), GameEvent.Forfeit)

        assertSame(before, test.run)
    }

    @Test
    fun `pause stops the shared clock`() {
        val test = Driver().send(GameEvent.Pause, GameEvent.Tick(10_000L))
        assertEquals(120_000L, test.run.remainingMs)

        test.send(GameEvent.Resume, GameEvent.Tick(1_000L))
        assertEquals(119_000L, test.run.remainingMs)
    }

    @Test
    fun `run survives a json round trip`() {
        val run = Driver().answers(true, false).send(GameEvent.Tick(800L)).run
        val json = Json.encodeToString(LevelTestRun.serializer(), run)

        assertEquals(run, Json.decodeFromString(LevelTestRun.serializer(), json))
        assertNotNull(run.game)
    }
}
