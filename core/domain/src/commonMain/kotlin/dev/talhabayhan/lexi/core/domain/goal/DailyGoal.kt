package dev.talhabayhan.lexi.core.domain.goal

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/**
 * Daily goal progress for the player's current local day.
 * [done] is the count of completed wheels today (any scored or practice mode; level tests and abandoned games excluded).
 * [goal] is the target wheels per day from settings.
 * [reached] is true when [done] >= [goal].
 */
public data class DailyGoalProgress(val done: Int, val goal: Int, val reached: Boolean) {
    /** The count to show next to [goal]: wheels beyond the goal still count in [done] but never show as "4/3". */
    val shown: Int get() = minOf(done, goal)
}

/**
 * Pure calculation for the daily goal (GAME_RULES.md stats/streak rules, owner decision).
 * A "çark" is any completed game played on the current local day. Daily wheel counts as one.
 * Level-test games, abandoned/quit games, and restart-dropped games do NOT count.
 */
public object DailyGoal {
    public fun isCounted(record: SessionRecord, today: Long): Boolean =
        record.epochDay == today && record.mode != null && record.mode != GameMode.LEVEL_TEST

    public fun progress(today: Long, sessions: Collection<SessionRecord>, goal: Int): DailyGoalProgress {
        val count = sessions.count { isCounted(it, today) }
        return DailyGoalProgress(
            done = count,
            goal = goal,
            reached = count >= goal,
        )
    }
}

/**
 * Returns today's completed-wheel count and the player's daily goal.
 */
public class GetDailyGoalProgress(
    private val sessions: SessionRepository,
    private val settings: SettingsRepository,
    private val clock: AppClock,
) {
    /**
     * Observes daily goal progress combining session records, settings goal, and the given [today] epoch-day flow.
     */
    public fun observe(today: Flow<Long>): Flow<DailyGoalProgress> =
        combine(sessions.observeRecords(), settings.settings, today) { records, s, day ->
            DailyGoal.progress(today = day, sessions = records, goal = s.dailyGoalWheels)
        }

    /**
     * Observes daily goal progress using the clock's current epoch day.
     */
    public fun observe(): Flow<DailyGoalProgress> =
        combine(sessions.observeRecords(), settings.settings) { records, s ->
            val day = clock.epochDay(clock.nowMs())
            DailyGoal.progress(today = day, sessions = records, goal = s.dailyGoalWheels)
        }

    /**
     * Returns a snapshot of the daily goal progress for [today] (defaults to now).
     */
    public suspend operator fun invoke(today: Long = clock.epochDay(clock.nowMs())): DailyGoalProgress {
        val records = sessions.observeRecords().first()
        val s = settings.settings.first()
        return DailyGoal.progress(today = today, sessions = records, goal = s.dailyGoalWheels)
    }
}
