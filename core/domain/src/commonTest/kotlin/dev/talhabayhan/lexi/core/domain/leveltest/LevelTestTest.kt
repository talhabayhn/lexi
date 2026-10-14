package dev.talhabayhan.lexi.core.domain.leveltest

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.fullAlphabetPool
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** GAME_RULES.md §9. */
class LevelTestTest {
    private val pool: List<Card> = Level.entries.flatMap { fullAlphabetPool(level = it, suffix = "ON") }

    /** Records answers; each answer uses a fresh card of the current level. */
    private fun LevelTestState.answer(vararg correct: Boolean): LevelTestState = correct.fold(this) { state, ok ->
        LevelTest.record(state, checkNotNull(LevelTest.nextCard(state, pool)), ok)
    }

    @Test
    fun `level test starts at A2`() {
        assertEquals(Level.A2, LevelTest.start(seed = 1L).currentLevel)
    }

    @Test
    fun `two correct in a row at a level go up`() {
        val state = LevelTest.start(1L).answer(true, true)

        assertEquals(Level.B1, state.currentLevel)
        assertEquals(0, state.correctInARow)
    }

    @Test
    fun `one correct is not enough to go up`() {
        assertEquals(Level.A2, LevelTest.start(1L).answer(true).currentLevel)
    }

    @Test
    fun `one wrong or pass goes down and resets the run`() {
        val state = LevelTest.start(1L).answer(true, false)

        assertEquals(Level.A1, state.currentLevel)
        assertEquals(0, state.correctInARow)
        assertEquals(Level.A2, LevelTest.start(1L).answer(true, true, true, false).currentLevel)
    }

    @Test
    fun `level stays within A1 and NAT`() {
        assertEquals(Level.A1, LevelTest.start(1L).answer(false, false, false).currentLevel)
        assertEquals(Level.NAT, LevelTest.start(1L).answer(*BooleanArray(12) { true }).currentLevel)
    }

    @Test
    fun `test ends after 12 answers when the current level has 3 asked cards`() {
        // A2 → A1 after the wrong one, then A1 stays at A1 with alternating answers.
        val state = LevelTest.start(1L).answer(false, true, false, true, false, true, false, true, false, true, false)
        assertFalse(state.isFinished)

        assertTrue(state.answer(false).isFinished)
    }

    /** A2 → A1 after the first wrong, nine more wrongs at A1, then two corrects back up to A2 (1 asked there). */
    private fun twelveEndingOnAFreshLevel(): LevelTestState =
        LevelTest.start(1L).answer(*BooleanArray(10) { false }).answer(true, true)

    @Test
    fun `test goes on past 12 while the current level has fewer than 3 asked cards`() {
        val twelve = twelveEndingOnAFreshLevel()
        assertEquals(Level.A2, twelve.currentLevel)
        assertFalse(twelve.isFinished)

        val thirteen = twelve.answer(false)

        assertEquals(Level.A1, thirteen.currentLevel)
        assertTrue(thirteen.isFinished)
    }

    @Test
    fun `test always ends after 16 answers`() {
        val fifteen = twelveEndingOnAFreshLevel().answer(true, true, true)
        assertEquals(Level.B1, fifteen.currentLevel)
        assertFalse(fifteen.isFinished)

        assertTrue(fifteen.answer(true).isFinished)
        assertEquals(LevelTest.MAX_SLOTS, fifteen.answer(true).answers.size)
    }

    @Test
    fun `record rejects answers after the end`() {
        val state = LevelTest.start(1L).answer(*BooleanArray(12) { true })
        assertTrue(state.isFinished)

        assertFailsWith<IllegalArgumentException> { LevelTest.record(state, pool.first(), true) }
    }

    @Test
    fun `result is the highest level with at least 60 percent of at least 3 asked`() {
        val state = stateOf(
            Level.A2 to listOf(true, true, true),
            Level.B1 to listOf(true, false, true, true, false),
            Level.B2 to listOf(true, false, false),
            Level.C1 to listOf(true, true),
        )

        assertEquals(Level.B1, LevelTest.result(state))
    }

    @Test
    fun `result without any qualifying level is the level below the start`() {
        val state = stateOf(Level.A2 to listOf(false, true), Level.A1 to listOf(false, false, true, false))

        assertEquals(Level.A1, LevelTest.result(state))
    }

    @Test
    fun `next card is of the current level and neither a repeated card nor a used letter`() {
        var state = LevelTest.start(3L)
        repeat(6) {
            val card = checkNotNull(LevelTest.nextCard(state, pool))
            assertEquals(state.currentLevel, card.level)
            assertTrue(state.answers.none { it.cardId == card.id || it.letter == card.letter })
            state = LevelTest.record(state, card, correct = it % 3 != 2)
        }
    }

    @Test
    fun `next card reuses letters when no unused letter is left at the level`() {
        val small = listOf(card("APPLE", level = Level.A2), card("ARM", level = Level.A2))
        val first = checkNotNull(LevelTest.nextCard(LevelTest.start(1L), small))
        val state = LevelTest.record(LevelTest.start(1L), first, correct = true)

        val second = checkNotNull(LevelTest.nextCard(state, small))

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `next card is null when the level has no unasked card`() {
        assertNull(LevelTest.nextCard(LevelTest.start(1L), listOf(card("APPLE", level = Level.B2))))
    }

    @Test
    fun `next card is deterministic per seed and differs between seeds`() {
        val picks = (1L..20L).map { LevelTest.nextCard(LevelTest.start(it), pool)?.id }

        assertEquals(picks, (1L..20L).map { LevelTest.nextCard(LevelTest.start(it), pool)?.id })
        assertTrue(picks.toSet().size > 5)
    }

    private fun stateOf(vararg answers: Pair<Level, List<Boolean>>): LevelTestState {
        var index = 0
        val recorded = answers.flatMap { (level, results) ->
            results.map { LevelTestAnswer(CardId("c${index++}"), level, 'A' + index % 26, it) }
        }
        return LevelTestState(seed = 1L, answers = recorded)
    }
}
