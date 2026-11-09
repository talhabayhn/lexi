package dev.talhabayhan.lexi.feature.game.duel

import dev.talhabayhan.lexi.core.domain.model.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DuelResultUiTest {
    private fun player(name: String, correct: Int, wrong: Int = 0, passed: Int = 0, remainingMs: Long = 0) =
        DuelPlayerOutcome(name, correct, wrong, passed, remainingMs, emptyList())

    private fun outcome(
        a: DuelPlayerOutcome,
        b: DuelPlayerOutcome,
        winner: Int?,
        end: DuelEnd = DuelEnd.ALL_ANSWERED,
    ) = DuelOutcome(Level.A1, 300, end, winner, listOf(a, b))

    @Test
    fun `a win by correct answers has no explanation`() {
        val ui = DuelResultUi.from(outcome(player("A", 20), player("B", 18), winner = 0))
        assertEquals(DuelExplanation.None, ui.explanation)
        assertEquals(listOf(true, false), ui.players.map { it.isWinner })
        assertNull(ui.forfeitedPlayer)
    }

    @Test
    fun `equal correct answers are decided by fewer wrong ones`() {
        val ui = DuelResultUi.from(outcome(player("A", 18, wrong = 4), player("B", 18, wrong = 6), winner = 0))
        assertEquals(DuelExplanation.TiebreakWrong, ui.explanation)
    }

    @Test
    fun `equal correct and wrong answers are decided by time left`() {
        val ui = DuelResultUi.from(
            outcome(player("A", 18, 4, remainingMs = 12_000), player("B", 18, 4, remainingMs = 0), winner = 0),
        )
        assertEquals(DuelExplanation.TiebreakTime, ui.explanation)
    }

    @Test
    fun `a draw has no explanation and no winner card`() {
        val ui = DuelResultUi.from(outcome(player("A", 18, 4), player("B", 18, 4), winner = null))
        assertEquals(DuelExplanation.None, ui.explanation)
        assertEquals(listOf(false, false), ui.players.map { it.isWinner })
    }

    @Test
    fun `a forfeit names the other player as the one who gave up`() {
        val ui = DuelResultUi.from(
            outcome(player("A", 3), player("B", 9), winner = 1, end = DuelEnd.FORFEIT),
        )
        assertEquals(DuelExplanation.Forfeit, ui.explanation)
        assertEquals(0, ui.forfeitedPlayer)
        assertEquals(DuelEnd.FORFEIT, ui.end)
    }

    @Test
    fun `time left is rounded up to whole seconds`() {
        val ui = DuelResultUi.from(outcome(player("A", 1, remainingMs = 11_001), player("B", 0), winner = 0))
        assertEquals(12, ui.players[0].remainingSeconds)
        assertEquals(0, ui.players[1].remainingSeconds)
    }
}
