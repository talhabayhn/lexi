package dev.talhabayhan.lexi.core.domain.session

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord

/**
 * A finished game as stored in the `session` table (`CONTENT.md` §8). [epochDay] is the day the game ended in the
 * player's time zone; [category] is null for wheels without one (review, daily, level test).
 */
public data class SessionResult(
    val mode: GameMode,
    val level: Level,
    val category: CategoryId?,
    val seed: Long,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val epochDay: Long,
    val score: Int,
    val correct: Int,
    val wrong: Int,
    val passed: Int,
    val completed: Boolean,
) {
    public fun toRecord(): SessionRecord = SessionRecord(epochDay = epochDay, completed = completed, correct = correct)
}

/** The one scored Daily wheel attempt of a day (`GAME_RULES.md` §5). [shareCode] is the share-card payload (M6). */
public data class DailyResult(
    val epochDay: Long,
    val level: Level? = null,
    val score: Int,
    val timeMs: Long,
    val shareCode: String? = null,
)
