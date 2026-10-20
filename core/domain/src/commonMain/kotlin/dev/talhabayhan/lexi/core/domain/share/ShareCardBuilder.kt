package dev.talhabayhan.lexi.core.domain.share

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.game.Slot
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.time.civilDateOfEpochDay

/** Builds the [ShareCard] of a finished game (`GAME_RULES.md` §5 "Share card"). Pure: no clock, no randomness. */
public object ShareCardBuilder {
    /**
     * [level], [mode], [reason] and [remainingMs] come from the game summary (`ResultRoute`); [remainingMs] is null in
     * untimed modes. [cells] are the playable slots in wheel order ([cellsOf]); [epochDay] is the day the game ended
     * and [streakDays] the current streak including it.
     *
     * Caption: Daily mode → [ShareCaption.Daily]; a completed ([GameOverReason.ALL_ANSWERED]) timed wheel →
     * [ShareCaption.Time]; otherwise [ShareCaption.Streak] from 2 days; otherwise [ShareCaption.Score].
     */
    public fun build(
        mode: GameMode,
        level: Level,
        reason: GameOverReason,
        remainingMs: Long?,
        cells: List<ShareCell>,
        epochDay: Long,
        streakDays: Int,
    ): ShareCard {
        val correct = cells.count { it.outcome == SlotOutcome.CORRECT }
        val total = cells.size
        val remainingSeconds = remainingMs?.let(::wholeSecondsRoundedUp)
        val streak = streakDays.coerceAtLeast(0)
        val caption = when {
            mode == GameMode.DAILY -> ShareCaption.Daily(correct)
            reason == GameOverReason.ALL_ANSWERED && remainingSeconds != null -> ShareCaption.Time(remainingSeconds)
            streak >= MIN_STREAK_CAPTION_DAYS -> ShareCaption.Streak(streak)
            else -> ShareCaption.Score(correct, total)
        }
        return ShareCard(
            mode = mode,
            level = if (mode == GameMode.DAILY) null else level,
            date = civilDateOfEpochDay(epochDay),
            correct = correct,
            total = total,
            remainingSeconds = remainingSeconds,
            timeUp = reason == GameOverReason.TIME_UP,
            streakDays = streak,
            cells = cells,
            caption = caption,
        )
    }

    /** The cells of a finished wheel: every playable slot in wheel order (SKIPPED slots are left out). */
    public fun cellsOf(slots: List<Slot>): List<ShareCell> =
        slots.mapNotNull { slot -> SlotOutcome.of(slot.state)?.let { ShareCell(slot.letter, it) } }

    /** Whole seconds rounded up, like the game clock and the result screen (0 ms → 0, 1 ms → 1). */
    private fun wholeSecondsRoundedUp(ms: Long): Int =
        ((ms.coerceAtLeast(0) + MS_PER_SECOND - 1) / MS_PER_SECOND).toInt()

    private const val MS_PER_SECOND = 1_000L
    private const val MIN_STREAK_CAPTION_DAYS = 2
}
