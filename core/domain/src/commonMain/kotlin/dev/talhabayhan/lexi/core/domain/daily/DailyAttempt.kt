package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.domain.time.AppClock

/**
 * The day's one Daily attempt counts from `Start` (`GAME_RULES.md` §5, owner decision 2026-11-11): [started] stores
 * it with no points, [progress] replaces it with the progress so far while the game runs (the game going to the
 * background), and `FinishGame` stores the final result. A game killed in between stays played for its day with the
 * progress last stored, as if its time had run out then. [epochDay] is always the day the wheel was built for
 * ([DailyPlayState.Ready.epochDay]), not the day of the start or the end.
 */
public class DailyAttempt(private val daily: DailyRepository, private val clock: AppClock) {
    /** At `Start`: the day's attempt with 0 points; false (nothing changes) when the day already has one. */
    public suspend fun started(epochDay: Long): Boolean =
        daily.record(DailyResult(epochDay = epochDay, score = 0, timeMs = 0L))

    /**
     * The running Daily [game]'s progress so far: its score (Daily has no multiplier and a time-up gives no time
     * bonus, §6) and the wall-clock time since the start. Nothing for another mode or a game that isn't running.
     */
    public suspend fun progress(game: GameState, epochDay: Long) {
        if (game.mode != GameMode.DAILY || game.status != GameStatus.PLAYING) return
        val startedAtMs = game.startedAtMs ?: return
        daily.save(DailyResult(epochDay = epochDay, score = game.score, timeMs = clock.nowMs() - startedAtMs))
    }
}
