package dev.talhabayhan.lexi.core.domain.share

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.game.Slot
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.time.CivilDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShareCardBuilderTest {
    private val oct14of2026 = 20_740L

    private fun slot(letter: Char, state: SlotState): Slot =
        Slot(letter, if (state == SlotState.SKIPPED) null else card("${letter}XY"), state)

    /** 26 slots: the first [correct] CORRECT, then WRONG, PASSED, then the rest in [rest]. */
    private fun wheel(correct: Int, wrong: Int = 0, passed: Int = 0, rest: SlotState = SlotState.CORRECT): List<Slot> =
        ('A'..'Z').mapIndexed { i, letter ->
            val state = when {
                i < correct -> SlotState.CORRECT
                i < correct + wrong -> SlotState.WRONG
                i < correct + wrong + passed -> SlotState.PASSED
                else -> rest
            }
            slot(letter, state)
        }

    private fun build(
        mode: GameMode = GameMode.CLASSIC,
        level: Level = Level.A1,
        reason: GameOverReason = GameOverReason.ALL_ANSWERED,
        remainingMs: Long? = 34_000,
        slots: List<Slot> = wheel(correct = 26),
        streakDays: Int = 0,
    ): ShareCard = ShareCardBuilder.build(
        mode = mode,
        level = level,
        reason = reason,
        remainingMs = remainingMs,
        cells = ShareCardBuilder.cellsOf(slots),
        epochDay = oct14of2026,
        streakDays = streakDays,
    )

    @Test
    fun `daily card has no level and the daily caption and the date of the epoch day`() {
        val card = build(
            mode = GameMode.DAILY,
            level = Level.B1,
            slots = wheel(correct = 21, wrong = 3, passed = 2),
            streakDays = 7,
        )

        assertEquals(GameMode.DAILY, card.mode)
        assertNull(card.level)
        assertEquals(CivilDate(2026, 10, 14), card.date)
        assertEquals(21, card.correct)
        assertEquals(26, card.total)
        assertEquals(34, card.remainingSeconds)
        assertFalse(card.timeUp)
        assertEquals(7, card.streakDays)
        assertEquals(ShareCaption.Daily(21), card.caption)
    }

    @Test
    fun `classic time up marks unanswered slots TIME_UP and keeps zero seconds`() {
        val slots = wheel(correct = 18, wrong = 2, passed = 1, rest = SlotState.TODO)
            .mapIndexed { i, s -> if (i == 21) slot(s.letter, SlotState.CURRENT) else s }

        val card = build(reason = GameOverReason.TIME_UP, remainingMs = 0, slots = slots, streakDays = 3)

        assertEquals(Level.A1, card.level)
        assertTrue(card.timeUp)
        assertEquals(0, card.remainingSeconds)
        assertTrue(card.showsTime)
        assertEquals(18, card.correct)
        assertEquals(26, card.total)
        assertEquals(List(18) { SlotOutcome.CORRECT }, card.cells.take(18).map { it.outcome })
        val middle = card.cells.subList(18, 21).map { it.outcome }
        assertEquals(listOf(SlotOutcome.WRONG, SlotOutcome.WRONG, SlotOutcome.PASSED), middle)
        assertEquals(List(5) { SlotOutcome.TIME_UP }, card.cells.drop(21).map { it.outcome })
        assertEquals(('A'..'Z').toList(), card.cells.map { it.letter })
        assertEquals(ShareCaption.Streak(3), card.caption)
    }

    @Test
    fun `untimed practice drops the time segment and never gets the time caption`() {
        val card = build(mode = GameMode.PRACTICE, level = Level.B2, remainingMs = null, streakDays = 2)

        assertNull(card.remainingSeconds)
        assertFalse(card.showsTime)
        assertFalse(card.timeUp)
        assertEquals(Level.B2, card.level)
        assertEquals(ShareCaption.Streak(2), card.caption)
    }

    @Test
    fun `review wheel with fewer than 26 slots scores against its slot count`() {
        val slots = listOf(
            slot('B', SlotState.CORRECT),
            slot('F', SlotState.WRONG),
            slot('K', SlotState.CORRECT),
            slot('R', SlotState.PASSED),
            slot('T', SlotState.CORRECT),
        )

        val card = build(mode = GameMode.REVIEW, level = Level.C1, remainingMs = null, slots = slots)

        assertEquals(3, card.correct)
        assertEquals(5, card.total)
        assertEquals(listOf('B', 'F', 'K', 'R', 'T'), card.cells.map { it.letter })
        val expected = listOf(
            SlotOutcome.CORRECT,
            SlotOutcome.WRONG,
            SlotOutcome.CORRECT,
            SlotOutcome.PASSED,
            SlotOutcome.CORRECT,
        )
        assertEquals(expected, card.cells.map { it.outcome })
    }

    @Test
    fun `skipped slots are not cells and do not count in the total`() {
        val slots = wheel(correct = 24).mapIndexed { i, s ->
            if (i == 16 || i == 23) slot(s.letter, SlotState.SKIPPED) else s
        }

        val card = build(slots = slots)

        assertEquals(24, card.total)
        assertEquals(24, card.correct)
        assertFalse('Q' in card.cells.map { it.letter })
        assertFalse('X' in card.cells.map { it.letter })
    }

    @Test
    fun `streak segment is dropped at zero days and shown from one day`() {
        val zero = build(streakDays = 0, reason = GameOverReason.TIME_UP, remainingMs = 0)
        val one = build(streakDays = 1, reason = GameOverReason.TIME_UP, remainingMs = 0)
        val seven = build(streakDays = 7, reason = GameOverReason.TIME_UP, remainingMs = 0)

        assertFalse(zero.showsStreak)
        assertTrue(one.showsStreak)
        assertTrue(seven.showsStreak)
        assertEquals(7, seven.streakDays)
    }

    @Test
    fun `a negative streak is treated as no streak`() {
        val card = build(streakDays = -1)

        assertEquals(0, card.streakDays)
        assertFalse(card.showsStreak)
    }

    @Test
    fun `streak caption needs at least two days and otherwise the score caption is the fallback`() {
        val slots = wheel(correct = 20, wrong = 6)
        fun caption(days: Int) =
            build(reason = GameOverReason.FORFEIT, remainingMs = 50_000, slots = slots, streakDays = days).caption

        assertEquals(ShareCaption.Score(20, 26), caption(0))
        assertEquals(ShareCaption.Score(20, 26), caption(1))
        assertEquals(ShareCaption.Streak(2), caption(2))
        assertEquals(ShareCaption.Streak(7), caption(7))
    }

    @Test
    fun `completed timed wheel gets the time caption even with a streak`() {
        val classic = build(remainingMs = 41_500, streakDays = 9)
        val hardcore = build(mode = GameMode.HARDCORE, remainingMs = 10_000, streakDays = 9)

        assertEquals(ShareCaption.Time(42), classic.caption)
        assertEquals(ShareCaption.Time(10), hardcore.caption)
    }

    @Test
    fun `completed wheel with wrong answers still counts as completed for the time caption`() {
        val card = build(remainingMs = 5_000, slots = wheel(correct = 20, wrong = 4, rest = SlotState.WRONG))

        assertEquals(20, card.correct)
        assertEquals(ShareCaption.Time(5), card.caption)
    }

    @Test
    fun `daily caption wins over the time and streak captions`() {
        val card = build(mode = GameMode.DAILY, remainingMs = 60_000, streakDays = 12)

        assertEquals(ShareCaption.Daily(26), card.caption)
    }

    @Test
    fun `hardcore wrong ends the game with unanswered slots marked TIME_UP but no time up flag`() {
        val slots = wheel(correct = 5, wrong = 1, rest = SlotState.TODO)

        val card = build(
            mode = GameMode.HARDCORE,
            reason = GameOverReason.WRONG_IN_HARDCORE,
            remainingMs = 90_000,
            slots = slots,
        )

        assertFalse(card.timeUp)
        assertEquals(90, card.remainingSeconds)
        assertEquals(20, card.cells.count { it.outcome == SlotOutcome.TIME_UP })
        assertEquals(ShareCaption.Score(5, 26), card.caption)
    }

    @Test
    fun `remaining seconds round up like the result screen`() {
        fun seconds(ms: Long) = build(remainingMs = ms, reason = GameOverReason.FORFEIT).remainingSeconds

        assertEquals(0, seconds(0))
        assertEquals(1, seconds(1))
        assertEquals(1, seconds(999))
        assertEquals(1, seconds(1_000))
        assertEquals(2, seconds(1_001))
        assertEquals(34, seconds(33_001))
        assertEquals(34, seconds(34_000))
        assertEquals(0, seconds(-250))
    }

    @Test
    fun `slot outcome maps every slot state`() {
        assertEquals(SlotOutcome.CORRECT, SlotOutcome.of(SlotState.CORRECT))
        assertEquals(SlotOutcome.WRONG, SlotOutcome.of(SlotState.WRONG))
        assertEquals(SlotOutcome.PASSED, SlotOutcome.of(SlotState.PASSED))
        assertEquals(SlotOutcome.TIME_UP, SlotOutcome.of(SlotState.TODO))
        assertEquals(SlotOutcome.TIME_UP, SlotOutcome.of(SlotState.CURRENT))
        assertNull(SlotOutcome.of(SlotState.SKIPPED))
    }
}
