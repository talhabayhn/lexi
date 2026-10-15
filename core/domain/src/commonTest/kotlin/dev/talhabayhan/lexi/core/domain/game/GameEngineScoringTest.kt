package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlin.test.Test
import kotlin.test.assertEquals

/** GAME_RULES.md §6. */
class GameEngineScoringTest {
    private val eightWords = arrayOf("APPLE", "BREAD", "CHAIR", "DOG", "EGG", "FISH", "GIRL", "HOUSE")

    private fun Play.correctPoints(): List<Int> = effectsOf<EngineEffect.Correct>().map { it.points }

    @Test
    fun `correct answer scores 10 points at A1`() {
        val game = play("APPLE", "BREAD").answer()

        assertEquals(listOf(10), game.correctPoints())
        assertEquals(10, game.state.score)
    }

    @Test
    fun `correct answer scores base times the card level multiplier`() {
        val expected = mapOf(Level.A2 to 11, Level.B1 to 12, Level.B2 to 13, Level.C1 to 15, Level.NAT to 17)

        val actual = expected.keys.associateWith { level ->
            play("APPLE", level = level).answer().correctPoints().single()
        }

        assertEquals(expected, actual)
    }

    @Test
    fun `streak bonus adds 2 per streak step capped at 10`() {
        val game = play(*eightWords)

        repeat(8) { game.answer() }

        assertEquals(listOf(10, 12, 14, 16, 18, 20, 20, 20), game.correctPoints())
        assertEquals(8, game.state.longestStreak)
    }

    @Test
    fun `combo celebrations at streak 3 5 and 10`() {
        val words = arrayOf(*eightWords, "IRON", "JUICE", "KING")
        val game = play(*words)

        repeat(11) { game.answer() }

        assertEquals(listOf(3, 5, 10), game.effectsOf<EngineEffect.ComboReached>().map { it.streak })
    }

    @Test
    fun `wrong answer scores nothing and breaks the streak`() {
        val game = play("APPLE", "BREAD", "CHAIR", "DOG").answer().answer().answerWrongAndWait().answer()

        assertEquals(listOf(10, 12, 10), game.correctPoints())
        assertEquals(1, game.effectsOf<EngineEffect.Wrong>().size)
        assertEquals(32, game.state.score)
    }

    @Test
    fun `pass scores nothing and breaks the streak`() {
        val game = play("APPLE", "BREAD", "CHAIR").answer().send(GameEvent.Pass).answer()

        assertEquals(listOf(10, 10), game.correctPoints())
    }

    @Test
    fun `passed slot answered on a later lap gets full points but the pass broke the streak`() {
        val game = play("APPLE", "BREAD", "CHAIR").send(GameEvent.Pass).answer().answer().answer()

        assertEquals(listOf(10, 12, 14), game.correctPoints())
        assertEquals(1, game.effectsOf<EngineEffect.Correct>().count { it.letter == 'A' })
    }

    @Test
    fun `completed wheel adds 1 point per remaining second`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Tick(30_500L)).answer().answer()

        val summary = checkNotNull(game.state.summary)
        assertEquals(149, summary.timeBonus)
        assertEquals(22 + 149, summary.score)
        assertEquals(true, summary.completed)
    }

    @Test
    fun `no time bonus when the wheel is not completed`() {
        val game = play("APPLE", "BREAD").answer().send(GameEvent.Forfeit)

        assertEquals(0, game.state.summary?.timeBonus)
        assertEquals(10, game.state.summary?.score)
    }

    @Test
    fun `completed wheel with wrong answers still gets the time bonus`() {
        val game = play("APPLE", "BREAD").answer().answerWrongAndWait()

        assertEquals(180, game.state.summary?.timeBonus)
    }

    @Test
    fun `hardcore multiplies the final total by 1_5`() {
        val game = play("APPLE", "BREAD", mode = GameMode.HARDCORE).send(GameEvent.Tick(100_000L)).answer().answer()

        // (10 + 12 + 80) * 1.5 = 153
        assertEquals(153, game.state.summary?.score)
    }

    @Test
    fun `hardcore multiplier rounds half up`() {
        val game = play("APPLE", "BREAD", mode = GameMode.HARDCORE).send(GameEvent.Tick(178_500L)).answer().answer()

        // (10 + 12 + 1) * 1.5 = 34.5
        assertEquals(35, game.state.summary?.score)
    }

    @Test
    fun `practice and review score nothing`() {
        for (mode in listOf(GameMode.PRACTICE, GameMode.REVIEW, GameMode.LEVEL_TEST)) {
            val game = play("APPLE", "BREAD", mode = mode).answer().answer()

            assertEquals(listOf(0, 0), game.correctPoints(), "$mode")
            assertEquals(0, game.state.summary?.score, "$mode")
            assertEquals(0, game.state.summary?.timeBonus, "$mode")
        }
    }

    @Test
    fun `passed slot that is current again at time up still counts as passed and missed`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Pass).answer()
        assertEquals('A', game.currentLetter)

        game.send(GameEvent.Tick(180_000L))

        val summary = checkNotNull(game.state.summary)
        assertEquals(1, summary.passed)
        assertEquals(0, summary.unanswered)
        assertEquals(listOf(CardId("a1-apple")), summary.missedCards)
        assertEquals(SlotState.PASSED, game.state.wheel.slots[0].state)
    }

    @Test
    fun `passed slot that is current again at forfeit counts as wrong`() {
        val game = play("APPLE", "BREAD").send(GameEvent.Pass).answer().send(GameEvent.Forfeit)

        assertEquals(0, game.state.summary?.passed)
        assertEquals(1, game.state.summary?.wrong)
        assertEquals(listOf(CardId("a1-apple")), game.state.summary?.missedCards)
    }

    @Test
    fun `passed slot answered on the last lap counts as correct or wrong`() {
        val correct = play("APPLE", "BREAD").send(GameEvent.Pass).answer().answer()
        assertEquals(
            listOf(CardId("a1-bread"), CardId("a1-apple")).sortedBy {
                it.value
            },
            correct.state.summary?.correctCards,
        )
        assertEquals(0, correct.state.summary?.passed)

        val wrong = play("APPLE", "BREAD").send(GameEvent.Pass).answer().answerWrongAndWait()
        assertEquals(1, wrong.state.summary?.wrong)
        assertEquals(0, wrong.state.summary?.passed)
    }

    @Test
    fun `summary counts slots and lists the cards to review`() {
        val base = wheelOf("APPLE", "BREAD", "CHAIR", "DOG")
        val wheel = Wheel(base.slots + Slot('Q', null, SlotState.SKIPPED))
        val game = play(wheel = wheel).answer().answerWrongAndWait().send(GameEvent.Pass, GameEvent.Tick(180_000L))

        val summary = checkNotNull(game.state.summary)
        assertEquals(1, summary.correct)
        assertEquals(1, summary.wrong)
        assertEquals(1, summary.passed)
        assertEquals(1, summary.skipped)
        assertEquals(1, summary.unanswered)
        assertEquals(listOf(CardId("a1-apple")), summary.correctCards)
        assertEquals(listOf(CardId("a1-bread"), CardId("a1-chair")), summary.missedCards)
        assertEquals(0L, summary.remainingMs)
    }
}
