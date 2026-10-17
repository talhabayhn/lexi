package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.domain.time.AppClock

/**
 * Stores the one scored daily attempt for [level] on the current local day (`GAME_RULES.md` §5).
 * Returns true if recorded, false if already played for this day and level.
 */
public class RecordDailyResult(private val daily: DailyRepository, private val clock: AppClock) {
    public suspend operator fun invoke(
        level: Level,
        score: Int,
        timeMs: Long,
        epochDay: Long = clock.epochDay(clock.nowMs()),
        shareCode: String? = null,
    ): Boolean {
        val result = DailyResult(
            epochDay = epochDay,
            level = level,
            score = score,
            timeMs = timeMs,
            shareCode = shareCode,
        )
        return daily.record(result)
    }
}
