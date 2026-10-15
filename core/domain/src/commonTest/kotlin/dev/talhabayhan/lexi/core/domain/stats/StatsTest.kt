package dev.talhabayhan.lexi.core.domain.stats

import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** GAME_RULES.md §11. */
class StatsTest {
    private fun progress(id: String, correct: Int = 0, wrong: Int = 0) =
        CardProgress(CardId(id), seen = correct + wrong, correct = correct, wrong = wrong)

    private fun session(day: Long, completed: Boolean = true, correct: Int = 0) =
        SessionRecord(epochDay = day, completed = completed, correct = correct)

    @Test
    fun `learned words are cards ever answered correctly`() {
        val cards = listOf(progress("a", correct = 1), progress("b", wrong = 3), progress("c", correct = 2, wrong = 1))

        assertEquals(2, Stats.learnedWords(cards))
    }

    @Test
    fun `accuracy is correct over answered rounded half up`() {
        val accuracy = Stats.accuracy(
            listOf(progress("a", correct = 2, wrong = 1), progress("b", correct = 0, wrong = 0)),
        )

        assertEquals(Accuracy(correct = 2, answered = 3), accuracy)
        assertEquals(67, accuracy.percent)
    }

    @Test
    fun `accuracy without answers has no percent`() {
        assertNull(Stats.accuracy(emptyList()).percent)
    }

    @Test
    fun `longest streak counts consecutive days with a completed wheel`() {
        val sessions = listOf(
            session(10), session(11), session(11), session(12, completed = false), session(13),
            session(20), session(21), session(22), session(23), session(25),
        )

        assertEquals(4, Stats.longestStreak(sessions))
        assertEquals(0, Stats.longestStreak(listOf(session(5, completed = false))))
    }

    @Test
    fun `longest streak without sessions is 0`() {
        assertEquals(0, Stats.longestStreak(emptyList()))
    }

    @Test
    fun `current streak counts the run ending today or yesterday`() {
        val today = 100L
        assertEquals(0, Stats.currentStreak(emptyList(), today))
        assertEquals(1, Stats.currentStreak(listOf(today), today))
        // Today is still open: yesterday's run is alive.
        assertEquals(2, Stats.currentStreak(listOf(today - 2, today - 1), today))
        // A day was skipped: the run ended.
        assertEquals(0, Stats.currentStreak(listOf(today - 3, today - 2), today))
        // Only the latest run counts, whatever the order.
        assertEquals(2, Stats.currentStreak(listOf(today, today - 1, today - 5, today - 4, today - 3), today))
    }

    @Test
    fun `words per day without sessions is 7 zero days`() {
        val days = Stats.wordsPerDay(emptyList(), todayEpochDay = 50L)

        assertEquals((44L..50L).map { DayCount(it, 0) }, days)
    }

    @Test
    fun `hardest letters without progress is empty`() {
        assertEquals(emptyList(), Stats.hardestLetters(emptyList(), emptyMap()))
    }

    @Test
    fun `completed wheels counts completed sessions only`() {
        assertEquals(2, Stats.completedWheels(listOf(session(1), session(2, completed = false), session(3))))
    }

    @Test
    fun `words per day sums correct answers of the last 7 days oldest first`() {
        val sessions = listOf(
            session(93, correct = 50),
            session(94, correct = 5),
            session(97, correct = 3),
            session(97, completed = false, correct = 4),
            session(100, correct = 9),
        )

        val days = Stats.wordsPerDay(sessions, todayEpochDay = 100L)

        assertEquals((94L..100L).toList(), days.map { it.epochDay })
        assertEquals(listOf(5, 0, 0, 7, 0, 0, 9), days.map { it.words })
    }

    @Test
    fun `per-level progress is learned over total`() {
        val cards = listOf(progress("a1-x", correct = 1), progress("a1-y", wrong = 1), progress("b1-z", correct = 3))
        val levels = mapOf(CardId("a1-x") to Level.A1, CardId("a1-y") to Level.A1, CardId("b1-z") to Level.B1)
        val totals = mapOf(Level.A1 to 600, Level.B1 to 800, Level.C1 to 1_000)

        val result = Stats.levelProgress(cards, levels, totals)

        val expected = mapOf(
            Level.A1 to LevelProgress(learned = 1, total = 600),
            Level.B1 to LevelProgress(learned = 1, total = 800),
            Level.C1 to LevelProgress(learned = 0, total = 1_000),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `per-category progress counts learned cards of the level's own categories`() {
        val cards = listOf(
            progress("a1-x", correct = 1),
            progress("a1-y", wrong = 2),
            progress("a1-z", correct = 4),
            progress("b1-other", correct = 1),
        )
        val categoryOf = mapOf(
            CardId("a1-x") to CategoryId("food"),
            CardId("a1-y") to CategoryId("food"),
            CardId("a1-z") to CategoryId("home"),
            CardId("a1-w") to CategoryId("travel"),
        )

        val result = Stats.categoryProgress(cards, categoryOf)

        val expected = mapOf(
            CategoryId("food") to LevelProgress(learned = 1, total = 2),
            CategoryId("home") to LevelProgress(learned = 1, total = 1),
            CategoryId("travel") to LevelProgress(learned = 0, total = 1),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `hardest letters have the lowest accuracy with at least 5 attempts`() {
        val cards = listOf(
            progress("q1", correct = 1, wrong = 4),
            progress("k1", correct = 2, wrong = 1),
            progress("k2", correct = 1, wrong = 1),
            progress("z1", correct = 0, wrong = 4),
            progress("e1", correct = 9, wrong = 1),
            progress("x1", correct = 2, wrong = 3),
        )
        val letters = mapOf(
            CardId("q1") to 'Q',
            CardId("k1") to 'K',
            CardId("k2") to 'K',
            CardId("z1") to 'Z',
            CardId("e1") to 'E',
            CardId("x1") to 'X',
        )

        val hardest = Stats.hardestLetters(cards, letters)

        assertEquals(listOf('Q', 'X', 'K', 'E'), hardest.map { it.letter })
        assertEquals(LetterAccuracy('Q', correct = 1, attempts = 5), hardest.first())
        assertEquals(20, hardest.first().percent)
    }
}
