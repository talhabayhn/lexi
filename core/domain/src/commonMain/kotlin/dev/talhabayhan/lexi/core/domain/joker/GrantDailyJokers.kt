package dev.talhabayhan.lexi.core.domain.joker

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.repository.JokerRepository
import dev.talhabayhan.lexi.core.domain.repository.StreakRepository
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.flow.first

/**
 * Gives the free jokers of today (`GAME_RULES.md` §7): ensures the start pack, then applies [JokerGifts.grant] for the
 * local day from [AppClock] and the stored streak days in one repository transaction. Idempotent per day and
 * milestone, so it is safe to call on every app start; returns what was given (empty when nothing).
 */
public class GrantDailyJokers(
    private val jokers: JokerRepository,
    private val streaks: StreakRepository,
    private val clock: AppClock,
    private val config: GameConfig,
) {
    public suspend operator fun invoke(): List<JokerGrant> {
        // First: a gift written before the start pack would make the inventory look non-empty and skip the pack.
        jokers.grantStartPack(config.jokerStartPack)
        val today = clock.epochDay(clock.nowMs())
        val streakDays = streaks.observeDays().first()
        var grants = emptyList<JokerGrant>()
        jokers.updateWallet { wallet ->
            val outcome = JokerGifts.grant(wallet, today, streakDays, config.jokerGifts)
            grants = outcome.grants
            outcome.wallet
        }
        return grants
    }
}
