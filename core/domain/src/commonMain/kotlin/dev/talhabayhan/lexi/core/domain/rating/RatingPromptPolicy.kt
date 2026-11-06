package dev.talhabayhan.lexi.core.domain.rating

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord

/**
 * When the app may ask the OS for a rating (`docs/GAME_RULES.md` §14, ADR 0024). Pure: "today" is a parameter.
 *
 * - at least [MIN_REAL_GAMES] completed real games (level test and duel are not real games)
 * - at least [MIN_DAYS_SINCE_INSTALL] days since install; this also rules out the first session
 * - the latest real game was a success: the wheel was completed (a forfeit, time-up or Hardcore failure isn't)
 * - at least [PERIOD_DAYS] days since our last request (the OS limits it as well)
 */
public object RatingPromptPolicy {
    public const val MIN_REAL_GAMES: Int = 5
    public const val MIN_DAYS_SINCE_INSTALL: Long = 3L
    public const val PERIOD_DAYS: Long = 120L

    private val NOT_REAL = setOf(GameMode.LEVEL_TEST, GameMode.DUEL)

    /** A wheel that counts towards the rating rules: a real mode, played to the end. */
    public fun isCompletedRealGame(record: SessionRecord): Boolean =
        record.completed && record.mode != null && record.mode !in NOT_REAL

    private fun isRealGame(record: SessionRecord): Boolean = record.mode != null && record.mode !in NOT_REAL

    /**
     * [sessions] oldest first. [installDay] is the epoch day of the first launch (null = unknown, never asks);
     * [lastRequestDay] the epoch day of our last request, null if none.
     */
    public fun shouldRequest(
        sessions: List<SessionRecord>,
        installDay: Long?,
        lastRequestDay: Long?,
        todayEpochDay: Long,
    ): Boolean {
        if (installDay == null || todayEpochDay - installDay < MIN_DAYS_SINCE_INSTALL) return false
        if (lastRequestDay != null && todayEpochDay - lastRequestDay < PERIOD_DAYS) return false
        if (sessions.count(::isCompletedRealGame) < MIN_REAL_GAMES) return false
        val latest = sessions.lastOrNull(::isRealGame) ?: return false
        return latest.completed
    }
}
