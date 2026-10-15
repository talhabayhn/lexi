package dev.talhabayhan.lexi.core.domain.srs

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.play
import dev.talhabayhan.lexi.core.domain.model.CardId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** GAME_RULES.md §10. */
class SrsTest {
    private val today = 20_000L
    private val now = 1_728_000_000_000L
    private val id = CardId("a1-apple")

    private fun inBox(box: Int, due: Long = today, wrong: Int = 0) =
        CardProgress(cardId = id, box = box, dueEpochDay = due, seen = 1, wrong = wrong)

    private fun apply(progress: CardProgress?, outcome: CardOutcome) =
        Srs.apply(progress, id, outcome, todayEpochDay = today, nowMs = now)

    @Test
    fun `box intervals are 0 1 3 7 and 21 days`() {
        assertEquals(listOf(0, 1, 3, 7, 21), (0..4).map(Srs::intervalDays))
        assertFailsWith<IllegalArgumentException> { Srs.intervalDays(5) }
    }

    @Test
    fun `wrong answer puts a new card into box 0 due today`() {
        val progress = apply(null, CardOutcome.WRONG)

        assertEquals(0, progress.box)
        assertEquals(today, progress.dueEpochDay)
        assertEquals(1, progress.wrong)
        assertEquals(1, progress.seen)
        assertEquals(now, progress.lastSeenMs)
        assertTrue(progress.isDue(today))
    }

    @Test
    fun `passed card enters box 0 without counting as wrong`() {
        val progress = apply(null, CardOutcome.PASSED)

        assertEquals(0, progress.box)
        assertEquals(0, progress.wrong)
        assertEquals(0, progress.correct)
    }

    @Test
    fun `wrong answer returns a notebook card to box 0`() {
        val progress = apply(inBox(3, due = today + 5), CardOutcome.WRONG)

        assertEquals(0, progress.box)
        assertEquals(today, progress.dueEpochDay)
    }

    @Test
    fun `correct answer for a card not in the notebook only counts as seen`() {
        val progress = apply(null, CardOutcome.CORRECT)

        assertNull(progress.box)
        assertFalse(progress.inNotebook)
        assertEquals(1, progress.correct)
        assertFalse(progress.isDue(today))
    }

    @Test
    fun `correct answer moves a notebook card one box up with that box's interval`() {
        val expected = mapOf(0 to (1 to 1L), 1 to (2 to 3L), 2 to (3 to 7L), 3 to (4 to 21L))

        for ((box, next) in expected) {
            val progress = apply(inBox(box), CardOutcome.CORRECT)
            assertEquals(next.first, progress.box, "from box $box")
            assertEquals(today + next.second, progress.dueEpochDay, "from box $box")
        }
    }

    @Test
    fun `correct answer in box 4 masters the card and it leaves the due queue`() {
        val progress = apply(inBox(4), CardOutcome.CORRECT)

        assertTrue(progress.mastered)
        assertEquals(4, progress.box)
        assertNull(progress.dueEpochDay)
        assertFalse(progress.isDue(today + 1_000))
    }

    @Test
    fun `wrong answer for a mastered card brings it back to box 0`() {
        val mastered = inBox(4).copy(mastered = true, dueEpochDay = null)

        val progress = apply(mastered, CardOutcome.WRONG)

        assertFalse(progress.mastered)
        assertEquals(0, progress.box)
    }

    @Test
    fun `correct answer for a mastered card keeps it mastered`() {
        val mastered = inBox(4).copy(mastered = true, dueEpochDay = null)

        assertEquals(mastered.copy(seen = 2, correct = 1, lastSeenMs = now), apply(mastered, CardOutcome.CORRECT))
    }

    @Test
    fun `card is due on and after its due day`() {
        val progress = inBox(2, due = today + 3)

        assertFalse(progress.isDue(today + 2))
        assertTrue(progress.isDue(today + 3))
        assertTrue(progress.isDue(today + 10))
    }

    @Test
    fun `due cards are ordered by due day then id`() {
        val cards = listOf(
            CardProgress(CardId("b"), box = 1, dueEpochDay = today - 1),
            CardProgress(CardId("a"), box = 0, dueEpochDay = today),
            CardProgress(CardId("c"), box = 0, dueEpochDay = today - 1),
            CardProgress(CardId("d"), box = 2, dueEpochDay = today + 1),
            CardProgress(CardId("e"), correct = 1),
        )

        assertEquals(listOf("b", "c", "a"), Srs.dueCards(cards, today).map { it.value })
        assertEquals(3, Srs.dueCount(cards, today))
    }

    @Test
    fun `hard cards have at least 2 wrongs`() {
        val cards = listOf(
            CardProgress(CardId("a"), box = 0, dueEpochDay = today, wrong = 2),
            CardProgress(CardId("b"), box = 0, dueEpochDay = today, wrong = 1),
            CardProgress(CardId("c"), box = 3, dueEpochDay = today, wrong = 5),
        )

        assertEquals(listOf("a", "c"), Srs.hardCards(cards).map { it.value })
    }

    @Test
    fun `results of a finished game come from the final slot states`() {
        val game = play("APPLE", "BREAD", "CHAIR", "DOG").answer().answerWrongAndWait().send(GameEvent.Pass)
            .send(GameEvent.Tick(180_000L))

        val results = Srs.results(game.state)

        val expected = listOf(
            CardResult(CardId("a1-apple"), CardOutcome.CORRECT),
            CardResult(CardId("a1-bread"), CardOutcome.WRONG),
            CardResult(CardId("a1-chair"), CardOutcome.PASSED),
        )
        assertEquals(expected, results)
    }

    @Test
    fun `forfeit sends every remaining card to the notebook as wrong`() {
        val game = play("APPLE", "BREAD", "CHAIR").answer().send(GameEvent.Pass, GameEvent.Forfeit)

        val updated = Srs.update(emptyMap(), game.state, todayEpochDay = 100L, nowMs = 5L, config = GameConfig())

        assertEquals(
            listOf(CardOutcome.CORRECT, CardOutcome.WRONG, CardOutcome.WRONG),
            Srs.results(game.state).map { it.outcome },
        )
        for (id in listOf(CardId("a1-bread"), CardId("a1-chair"))) {
            assertEquals(0, updated.getValue(id).box, "$id")
            assertEquals(1, updated.getValue(id).wrong, "$id")
        }
    }

    @Test
    fun `passed card answered on a later lap counts only as answered`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Pass).answer().answer()

        assertEquals(listOf(CardOutcome.CORRECT, CardOutcome.CORRECT), Srs.results(game.state).map { it.outcome })
    }

    @Test
    fun `update applies the results of a finished game to the progress`() {
        val game = play("APPLE", "BREAD").answer().answerWrongAndWait()
        val progress = mapOf(CardId("a1-apple") to CardProgress(CardId("a1-apple"), box = 1, dueEpochDay = today))

        val updated = Srs.update(progress, game.state, todayEpochDay = today, nowMs = now, config = GameConfig())

        assertEquals(2, updated.getValue(CardId("a1-apple")).box)
        assertEquals(0, updated.getValue(CardId("a1-bread")).box)
    }

    @Test
    fun `review wheel correct moves up and wrong goes back to box 0`() {
        val game = play("APPLE", "BREAD", mode = GameMode.REVIEW).answer().answerWrongAndWait()
        val progress = listOf("a1-apple", "a1-bread").associate { CardId(it) to CardProgress(CardId(it), 2, today) }

        val updated = Srs.update(progress, game.state, todayEpochDay = today, nowMs = now, config = GameConfig())

        assertEquals(3, updated.getValue(CardId("a1-apple")).box)
        assertEquals(0, updated.getValue(CardId("a1-bread")).box)
    }

    @Test
    fun `duel results do not change the notebook`() {
        val game = play("APPLE", "BREAD", mode = GameMode.DUEL).answerWrongAndWait().answerWrongAndWait()

        assertEquals(emptyMap(), Srs.update(emptyMap(), game.state, today, now, GameConfig()))
    }

    @Test
    fun `practice still feeds the notebook`() {
        val game = play("APPLE", "BREAD", mode = GameMode.PRACTICE).answerWrongAndWait().answer()

        assertEquals(0, Srs.update(emptyMap(), game.state, today, now, GameConfig()).getValue(CardId("a1-apple")).box)
    }

    @Test
    fun `update requires a finished game`() {
        assertFailsWith<IllegalArgumentException> {
            Srs.update(emptyMap(), play("APPLE").state, today, now, GameConfig())
        }
    }
}
