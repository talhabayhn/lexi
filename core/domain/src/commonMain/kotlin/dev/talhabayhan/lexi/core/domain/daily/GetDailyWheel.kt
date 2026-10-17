package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.WheelGenerator
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.ContentVersionRepository
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.time.AppClock

/**
 * Builds or checks the status of a daily wheel for a given [level] (`GAME_RULES.md` §5).
 * Deterministic: ignores card history so every player gets the identical wheel for the same date + level + contentVersion.
 */
public class GetDailyWheel(
    private val cards: CardRepository,
    private val contentVersion: ContentVersionRepository,
    private val daily: DailyRepository,
    private val clock: AppClock,
) {
    public suspend operator fun invoke(level: Level, epochDay: Long = clock.epochDay(clock.nowMs())): DailyPlayState {
        val played = daily.get(epochDay, level)
        if (played != null) {
            return DailyPlayState.AlreadyPlayed(epochDay = epochDay, level = level, result = played)
        }
        val version = contentVersion.contentVersion()
        val seed = DailySeed.seed(epochDay, level, version)
        val pool = cards.wheelPool(level, CategoryId.MIXED)
        val random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE)
        val wheel = WheelGenerator.generate(
            pool = pool,
            level = level,
            category = CategoryId.MIXED,
            mode = GameMode.DAILY,
            random = random,
            recentlySeen = emptySet(),
        )
        if (wheel.playableSlots.isEmpty()) return DailyPlayState.NoCards
        return DailyPlayState.Ready(epochDay = epochDay, level = level, seed = seed, wheel = wheel)
    }
}
