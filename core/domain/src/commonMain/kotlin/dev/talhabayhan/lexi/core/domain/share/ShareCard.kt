package dev.talhabayhan.lexi.core.domain.share

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.time.CivilDate

/**
 * The share card of a finished game (`12-share-card`, `GAME_RULES.md` §5 "Share card"), built by [ShareCardBuilder].
 * [level] is null for Daily (its wheel mixes levels). [remainingSeconds] is null in untimed modes (no time segment).
 * [total] is the number of playable slots, so a review wheel scores against fewer than 26.
 */
public data class ShareCard(
    val mode: GameMode,
    val level: Level?,
    val date: CivilDate,
    val correct: Int,
    val total: Int,
    val remainingSeconds: Int?,
    val timeUp: Boolean,
    val streakDays: Int,
    val cells: List<ShareCell>,
    val caption: ShareCaption,
) {
    /** The "N sn kala" / "Süre bitti" segment is shown (timed modes only). */
    val showsTime: Boolean get() = remainingSeconds != null

    /** The "N gün seri" segment is shown (dropped at 0 days). */
    val showsStreak: Boolean get() = streakDays > 0
}

/** One playable wheel position on the card, in wheel order. */
public data class ShareCell(val letter: Char, val outcome: SlotOutcome)

/** How a slot ended. [TIME_UP] = never answered (the clock ran out, or the game ended before it was reached). */
public enum class SlotOutcome {
    CORRECT,
    WRONG,
    PASSED,
    TIME_UP,
    ;

    public companion object {
        /** The outcome of a finished slot; null for SKIPPED (no card, not on the card). */
        public fun of(state: SlotState): SlotOutcome? = when (state) {
            SlotState.CORRECT -> CORRECT
            SlotState.WRONG -> WRONG
            SlotState.PASSED -> PASSED
            SlotState.TODO, SlotState.CURRENT -> TIME_UP
            SlotState.SKIPPED -> null
        }
    }
}

/** The headline of the share text; see [ShareCardBuilder.build] for which one a game gets. */
public sealed interface ShareCaption {
    /** Daily wheel: "[correct]/total in today's wheel". */
    public data class Daily(val correct: Int) : ShareCaption

    /** Completed timed wheel: finished with [seconds] left. */
    public data class Time(val seconds: Int) : ShareCaption

    /** A streak of [days] (≥ 2) days. */
    public data class Streak(val days: Int) : ShareCaption

    /** Fallback when no other caption applies: plain score. */
    public data class Score(val correct: Int, val total: Int) : ShareCaption
}
