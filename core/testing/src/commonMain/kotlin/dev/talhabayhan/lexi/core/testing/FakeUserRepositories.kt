package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.repository.JokerRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [ProgressRepository]; [saveCalls] counts writes. */
public class FakeProgressRepository(initial: Collection<CardProgress> = emptyList()) : ProgressRepository {
    private val state = MutableStateFlow(initial.associateBy { it.cardId })

    /** Current progress by card id. */
    public val progress: Map<CardId, CardProgress> get() = state.value

    public var saveCalls: Int = 0
        private set

    override fun observeAll(): Flow<List<CardProgress>> = state.map { it.values.sortedBy { p -> p.cardId.value } }

    override suspend fun get(ids: Collection<CardId>): Map<CardId, CardProgress> = state.value.filterKeys { it in ids }

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

/** In-memory [JokerRepository]; `null` inventory = nothing stored yet (start pack not granted). */
public class FakeJokerRepository(initial: JokerInventory? = null) : JokerRepository {
    private val state = MutableStateFlow(initial)

    public val inventory: StateFlow<JokerInventory?> = state.asStateFlow()

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
}
