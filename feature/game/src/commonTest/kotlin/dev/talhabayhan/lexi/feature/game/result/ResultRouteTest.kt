package dev.talhabayhan.lexi.feature.game.result

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.GameSummary
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.Slot
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.share.ShareCell
import dev.talhabayhan.lexi.core.domain.share.SlotOutcome
import dev.talhabayhan.lexi.core.testing.testCard
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultRouteTest {
    private val finished = GameState(
        seed = 1L,
        mode = GameMode.CLASSIC,
        level = Level.A1,
        wheel = Wheel(
            listOf(
                Slot('A', testCard("APPLE"), SlotState.CORRECT),
                Slot('B', null, SlotState.SKIPPED),
                Slot('C', testCard("CHAIR"), SlotState.PASSED),
                Slot('D', testCard("DOG"), SlotState.WRONG),
                Slot('E', testCard("EGG"), SlotState.CURRENT),
                Slot('F', testCard("FISH"), SlotState.TODO),
            ),
        ),
        jokers = JokerInventory(),
        status = GameStatus.FINISHED,
        remainingMs = 0L,
        summary = GameSummary(
            reason = GameOverReason.TIME_UP,
            score = 10,
            timeBonus = 0,
            completed = false,
            correct = 1,
            wrong = 1,
            passed = 1,
            skipped = 1,
            unanswered = 2,
            longestStreak = 1,
            remainingMs = 0L,
            correctCards = listOf(CardId("a1-apple")),
            missedCards = listOf(CardId("a1-chair"), CardId("a1-dog")),
        ),
    )

    @Test
    fun `the route keeps every playable letter and its outcome in wheel order`() {
        val route = ResultRoute.of(finished, category = null, recorded = true)

        assertEquals("ACDEF", route.wheelLetters)
        assertEquals(listOf("CORRECT", "PASSED", "WRONG", "TIME_UP", "TIME_UP"), route.wheelOutcomes)
        assertEquals(
            listOf(
                ShareCell('A', SlotOutcome.CORRECT),
                ShareCell('C', SlotOutcome.PASSED),
                ShareCell('D', SlotOutcome.WRONG),
                ShareCell('E', SlotOutcome.TIME_UP),
                ShareCell('F', SlotOutcome.TIME_UP),
            ),
            route.shareCells,
        )
    }

    @Test
    fun `a route without wheel data has no share cells`() {
        val route = ResultRoute(
            level = "A1",
            mode = "CLASSIC",
            reason = "TIME_UP",
            score = 0,
            correct = 0,
            wrong = 0,
            passed = 0,
            longestStreak = 0,
        )

        assertEquals(emptyList(), route.shareCells)
    }

    @Test
    fun `an unknown outcome is left out instead of crashing`() {
        val route = ResultRoute(
            level = "A1",
            mode = "CLASSIC",
            reason = "TIME_UP",
            score = 0,
            correct = 0,
            wrong = 0,
            passed = 0,
            longestStreak = 0,
            wheelLetters = "AB",
            wheelOutcomes = listOf("SKIPPED", "CORRECT"),
        )

        assertEquals(listOf(ShareCell('B', SlotOutcome.CORRECT)), route.shareCells)
    }
}
