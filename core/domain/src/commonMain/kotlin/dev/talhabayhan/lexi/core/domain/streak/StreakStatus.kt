package dev.talhabayhan.lexi.core.domain.streak

import dev.talhabayhan.lexi.core.domain.repository.StreakRepository
import dev.talhabayhan.lexi.core.domain.stats.Stats
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

public data class StreakStatus(
    /** Current consecutive days streak count. */
    val currentStreak: Int,
    /** Whether a wheel has already been completed today. */
    val playedToday: Boolean,
    /** All recorded streak days (epoch days with ≥ 1 completed wheel). */
    val days: List<Long>,
)

public class GetStreakStatus(private val streaks: StreakRepository, private val clock: AppClock) {
    public fun observe(today: Flow<Long>): Flow<StreakStatus> = combine(streaks.observeDays(), today) { days, day ->
        StreakStatus(
            currentStreak = Stats.currentStreak(days, day),
            playedToday = day in days,
            days = days,
        )
    }

    public fun observe(): Flow<StreakStatus> = streaks.observeDays().map { days ->
        val today = clock.epochDay(clock.nowMs())
        StreakStatus(
            currentStreak = Stats.currentStreak(days, today),
            playedToday = today in days,
            days = days,
        )
    }
}
