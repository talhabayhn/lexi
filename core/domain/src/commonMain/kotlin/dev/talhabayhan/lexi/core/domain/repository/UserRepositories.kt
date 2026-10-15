package dev.talhabayhan.lexi.core.domain.repository

import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.joker.JokerWallet
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import kotlinx.coroutines.flow.Flow

/** SRS progress per card (`GAME_RULES.md` §10). Progress of cards no longer in content is kept but hidden. */
public interface ProgressRepository {
    /** Progress of every card in the installed content, by card id (notebook, stats, due badges). */
    public fun observeAll(): Flow<List<CardProgress>>

    /** Stored progress of [ids] (missing = never answered), e.g. before `Srs.update`. */
    public suspend fun get(ids: Collection<CardId>): Map<CardId, CardProgress>

    /** Inserts or replaces the given entries (e.g. the changes returned by `Srs.update`) in one transaction. */
    public suspend fun save(progress: Collection<CardProgress>)

    /** `lastSeenMs` per card, for `WheelGenerator.recentlySeen`. */
    public suspend fun lastSeenMs(): Map<CardId, Long>
}

/** Finished games (`GAME_RULES.md` §11). */
public interface SessionRepository {
    /**
     * Stores a finished game and, when completed, marks its day for the streak. Modes in
     * `GameConfig.srsExcludedModes` (hot-seat duel) are not recorded; returns whether it was stored.
     */
    public suspend fun record(session: SessionResult): Boolean

    /** What `Stats` needs of every stored session, oldest first. */
    public fun observeRecords(): Flow<List<SessionRecord>>
}

/**
 * Days with at least one completed wheel (written by [SessionRepository.record]); `Stats.currentStreak` gives the
 * current streak, `JokerGifts` its bonus. More streak rules land in M6.
 */
public interface StreakRepository {
    /** Epoch days, ascending. */
    public fun observeDays(): Flow<List<Long>>
}

/** The Daily wheel's one scored attempt per day (`GAME_RULES.md` §5). */
public interface DailyRepository {
    public fun observe(epochDay: Long): Flow<DailyResult?>

    /** Stores the day's attempt; returns false and changes nothing when the day was already played. */
    public suspend fun record(result: DailyResult): Boolean
}

/** Persisted joker inventory (`GAME_RULES.md` §7). */
public interface JokerRepository {
    public fun observeInventory(): Flow<JokerInventory>

    /** Replaces the inventory, e.g. with `GameState.jokers` after `JokerApplied`. */
    public suspend fun save(inventory: JokerInventory)

    /** Adds [amount] jokers of [type] (rewarded ad, Pro refill); daily gifts go through [updateWallet]. */
    public suspend fun add(type: JokerType, amount: Int)

    /** Gives [startPack] on the very first call (no inventory stored yet); returns whether it was granted. */
    public suspend fun grantStartPack(startPack: JokerInventory): Boolean

    /**
     * Reads the inventory and the gift bookkeeping, applies [transform] (run exactly once) and stores the result in
     * one transaction; returns the stored wallet. The inventory is written only when it changed, so an update before
     * [grantStartPack] doesn't block the start pack. Gift days only move forward: a transform that clears or lowers
     * one throws [IllegalArgumentException] and nothing is stored.
     */
    public suspend fun updateWallet(transform: (JokerWallet) -> JokerWallet): JokerWallet
}
