package dev.talhabayhan.lexi.core.domain.streak

import dev.talhabayhan.lexi.core.domain.repository.StreakRepository
import dev.talhabayhan.lexi.core.domain.stats.Stats
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Streak semantics from `GAME_RULES.md` (§6, §7, §11). */
public object StreakSemantics {
    /** Milestones for in-game combo celebrations: 3, 5, 10 (`GAME_RULES.md` §6). */
    public val COMBO_CELEBRATIONS: Set<Int> = setOf(3, 5, 10)

    /** Text for in-game combo celebration badge: `"×N SERİ"`. */
    public fun comboBadgeText(streak: Int): String = "×$streak SERİ"

    /** Display count for the home screen streak badge. */
    public fun homeBadgeStreak(days: Collection<Long>, today: Long): Int = Stats.currentStreak(days, today)
}

public data class StreakStatus(
    /** Current consecutive days streak count. */
    val currentStreak: Int,
    /** Whether a wheel has already been completed today. */
    val playedToday: Boolean,
    /** All recorded streak days (epoch days with ≥ 1 completed wheel). */
    val days: List<Long>,
)

public class GetStreakStatus(private val streaks: StreakRepository, private val clock: AppClock) {
    public fun observe(): Flow<StreakStatus> = streaks.observeDays().map { days ->
        val today = clock.epochDay(clock.nowMs())
        StreakStatus(
            currentStreak = Stats.currentStreak(days, today),
            playedToday = today in days,
            days = days,
        )
    }

    public suspend fun current(): StreakStatus {
        val days = streaks.observeDays().first()
        val today = clock.epochDay(clock.nowMs())
        return StreakStatus(
            currentStreak = Stats.currentStreak(days, today),
            playedToday = today in days,
            days = days,
        )
    }
}
