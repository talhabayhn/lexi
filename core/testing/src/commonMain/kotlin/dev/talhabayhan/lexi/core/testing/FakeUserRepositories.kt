package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.joker.JokerWallet
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.repository.JokerRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.repository.StreakRepository
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * In-memory [ProgressRepository]; [saveCalls] counts writes. While [failing] is true, reads throw
 * [IllegalStateException] ([observeAll] when collected).
 */
public class FakeProgressRepository(initial: Collection<CardProgress> = emptyList()) : ProgressRepository {
    private val state = MutableStateFlow(initial.associateBy { it.cardId })

    /** Failure injection: reads throw while true. */
    public var failing: Boolean = false

    /** Current progress by card id. */
    public val progress: Map<CardId, CardProgress> get() = state.value

    public var saveCalls: Int = 0
        private set

    override fun observeAll(): Flow<List<CardProgress>> = flow {
        check(!failing) { "injected progress read failure" }
        emitAll(state.map { it.values.sortedBy { p -> p.cardId.value } })
    }

    override suspend fun get(ids: Collection<CardId>): Map<CardId, CardProgress> {
        check(!failing) { "injected progress read failure" }
        return state.value.filterKeys { it in ids }
    }

    override suspend fun save(progress: Collection<CardProgress>) {
        saveCalls++
        state.value += progress.associateBy { it.cardId }
    }

    override suspend fun lastSeenMs(): Map<CardId, Long> =
        state.value.mapNotNull { (id, p) -> p.lastSeenMs?.let { id to it } }.toMap()
}

/** In-memory [SessionRepository] that skips `GameConfig.srsExcludedModes` like the real one. */
public class FakeSessionRepository(private val config: GameConfig = GameConfig()) : SessionRepository {
    private val stored = MutableStateFlow<List<SessionResult>>(emptyList())

    /** Stored sessions, oldest first. */
    public val sessions: List<SessionResult> get() = stored.value

    /** Every call of [record], stored or not. */
    public var recordCalls: Int = 0
        private set

    /** The next this many [record] calls throw [IllegalStateException] without storing (failure injection). */
    public var failingRecords: Int = 0

    override suspend fun record(session: SessionResult): Boolean {
        recordCalls++
        if (failingRecords > 0) {
            failingRecords--
            throw IllegalStateException("injected record failure")
        }
        if (session.mode in config.srsExcludedModes) return false
        stored.value += session
        return true
    }

    override fun observeRecords(): Flow<List<SessionRecord>> = stored.map { list -> list.map { it.toRecord() } }
}

/**
 * In-memory [JokerRepository]; `null` inventory = nothing stored yet (start pack not granted). [wallet] holds the gift
 * bookkeeping (its inventory mirrors [inventory]).
 */
public class FakeJokerRepository(initial: JokerInventory? = null) : JokerRepository {
    private val state = MutableStateFlow(initial)
    private var giftDays = JokerWallet()

    public val inventory: StateFlow<JokerInventory?> = state.asStateFlow()

    public val wallet: JokerWallet get() = giftDays.copy(inventory = state.value ?: JokerInventory())

    override fun observeInventory(): Flow<JokerInventory> = state.map { it ?: JokerInventory() }

    override suspend fun save(inventory: JokerInventory) {
        state.value = inventory
    }

    override suspend fun add(type: JokerType, amount: Int) {
        state.value = (state.value ?: JokerInventory()).plus(type, amount)
    }

    override suspend fun grantStartPack(startPack: JokerInventory): Boolean {
        if (state.value != null) return false
        state.value = startPack
        return true
    }

    override suspend fun updateWallet(transform: (JokerWallet) -> JokerWallet): JokerWallet {
        val before = wallet
        val after = transform(before)
        after.requireDaysNotBefore(before)
        if (after.inventory != before.inventory) state.value = after.inventory
        giftDays = after
        return wallet
    }
}

/** In-memory [StreakRepository]; [days] are epoch days and may be replaced at any time. */
public class FakeStreakRepository(days: Collection<Long> = emptyList()) : StreakRepository {
    private val state = MutableStateFlow(days.distinct().sorted())

    public var days: List<Long>
        get() = state.value
        set(value) {
            state.value = value.distinct().sorted()
        }

    override fun observeDays(): Flow<List<Long>> = state
}

/** In-memory [DailyRepository]. */
public class FakeDailyRepository(initial: Collection<DailyResult> = emptyList()) : DailyRepository {
    private val state = MutableStateFlow(initial.toList())

    override fun observe(epochDay: Long): Flow<DailyResult?> =
        state.map { list -> list.firstOrNull { it.epochDay == epochDay } }

    override suspend fun get(epochDay: Long): DailyResult? = state.value.firstOrNull { it.epochDay == epochDay }

    override suspend fun record(result: DailyResult): Boolean {
        if (state.value.any { it.epochDay == result.epochDay }) return false
        state.value += result
        return true
    }
}
