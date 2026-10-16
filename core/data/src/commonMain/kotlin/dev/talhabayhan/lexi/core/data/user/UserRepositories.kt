package dev.talhabayhan.lexi.core.data.user

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.talhabayhan.lexi.core.data.content.MAX_SQL_VARIABLES
import dev.talhabayhan.lexi.core.data.db.Databases
import dev.talhabayhan.lexi.core.data.db.SuspendLazy
import dev.talhabayhan.lexi.core.data.user.db.Joker
import dev.talhabayhan.lexi.core.data.user.db.Progress
import dev.talhabayhan.lexi.core.data.user.db.UserDatabase
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.joker.JokerGrantReason
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Shared plumbing: user.db access on the IO dispatcher. */
internal abstract class UserStore(protected val databases: Databases, protected val io: CoroutineDispatcher) {
    protected suspend fun <T> write(block: UserDatabase.() -> T): T = withContext(io) { databases.user.get().block() }

    /** Queries and the row mapping run on [io]: screens collect these on the main thread during transitions. */
    protected fun <T> observe(query: UserDatabase.() -> Flow<T>): Flow<T> = flow {
        emitAll(databases.user.get().query())
    }.flowOn(io)
}

/** Progress of card ids that are not in the installed content.db is kept but filtered out of every list. */
internal class ProgressRepositoryImpl(databases: Databases, io: CoroutineDispatcher) :
    UserStore(databases, io),
    ProgressRepository {
    private val contentIds = SuspendLazy(io) {
        databases.content.get().contentReadsQueries.allCardIds().executeAsList().toSet()
    }

    override fun observeAll(): Flow<List<CardProgress>> = flow {
        val known = contentIds.get()
        emitAll(
            databases.user.get().progressQueries.selectAll().asFlow().mapToList(io).map { rows ->
                rows.filter { it.card_id in known }.mapNotNull { it.toDomain() }
            },
        )
    }.flowOn(io)

    override suspend fun get(ids: Collection<CardId>): Map<CardId, CardProgress> = write {
        ids.map { it.value }.distinct().chunked(MAX_SQL_VARIABLES).flatMap { chunk ->
            progressQueries.selectByIds(chunk).executeAsList()
        }.mapNotNull { it.toDomain() }.associateBy { it.cardId }
    }

    override suspend fun save(progress: Collection<CardProgress>) = write {
        progressQueries.transaction {
            progress.forEach {
                progressQueries.upsert(
                    card_id = it.cardId.value,
                    box = it.box?.toLong(),
                    due_epoch_day = it.dueEpochDay,
                    seen = it.seen.toLong(),
                    correct = it.correct.toLong(),
                    wrong = it.wrong.toLong(),
                    last_seen_ms = it.lastSeenMs,
                    mastered = if (it.mastered) 1L else 0L,
                )
            }
        }
    }

    override suspend fun lastSeenMs(): Map<CardId, Long> {
        val known = contentIds.get()
        return write {
            progressQueries.lastSeen().executeAsList()
                .filter { it.card_id in known }
                .mapNotNull { row -> row.last_seen_ms?.let { CardId(row.card_id) to it } }
                .toMap()
        }
    }

    /** Null for a row that breaks a domain invariant (never written by this app). */
    private fun Progress.toDomain(): CardProgress? = runCatching {
        CardProgress(
            cardId = CardId(card_id),
            box = box?.toInt(),
            dueEpochDay = due_epoch_day,
            seen = seen.toInt(),
            correct = correct.toInt(),
            wrong = wrong.toInt(),
            lastSeenMs = last_seen_ms,
            mastered = mastered != 0L,
        )
    }.getOrNull()
}

/** Modes in [GameConfig.srsExcludedModes] (hot-seat duel) are never recorded (`GAME_RULES.md` §11). */
internal class SessionRepositoryImpl(databases: Databases, io: CoroutineDispatcher, private val config: GameConfig) :
    UserStore(databases, io),
    SessionRepository {
    override suspend fun record(session: SessionResult): Boolean {
        if (session.mode in config.srsExcludedModes) return false
        write {
            transaction {
                sessionQueries.insert(
                    mode = session.mode.name,
                    level = session.level.name,
                    category = session.category?.value,
                    seed = session.seed,
                    started_ms = session.startedAtMs,
                    ended_ms = session.endedAtMs,
                    epoch_day = session.epochDay,
                    score = session.score.toLong(),
                    correct = session.correct.toLong(),
                    wrong = session.wrong.toLong(),
                    passed = session.passed.toLong(),
                    completed = if (session.completed) 1L else 0L,
                )
                if (session.completed) streakQueries.markDay(session.epochDay)
            }
        }
        return true
    }

    override fun observeRecords(): Flow<List<SessionRecord>> = observe {
        sessionQueries.records { epochDay, completed, correct ->
            SessionRecord(epochDay = epochDay, completed = completed != 0L, correct = correct.toInt())
        }.asFlow().mapToList(io)
    }
}

internal class StreakRepositoryImpl(databases: Databases, io: CoroutineDispatcher) :
    UserStore(databases, io),
    StreakRepository {
    override fun observeDays(): Flow<List<Long>> = observe { streakQueries.days().asFlow().mapToList(io) }
}

internal class DailyRepositoryImpl(databases: Databases, io: CoroutineDispatcher) :
    UserStore(databases, io),
    DailyRepository {
    override fun observe(epochDay: Long): Flow<DailyResult?> = observe {
        dailyResultQueries.byDay(epochDay).asFlow().mapToOneOrNull(io).map { row ->
            row?.let { r ->
                Level.entries.firstOrNull { it.name == r.level }
                    ?.let { DailyResult(r.epoch_day, it, r.score.toInt(), r.time_ms, r.share_code) }
            }
        }
    }

    override suspend fun record(result: DailyResult): Boolean = write {
        dailyResultQueries.transactionWithResult {
            if (dailyResultQueries.byDay(result.epochDay).executeAsOneOrNull() != null) {
                return@transactionWithResult false
            }
            dailyResultQueries.insertIfAbsent(
                epoch_day = result.epochDay,
                level = result.level.name,
                score = result.score.toLong(),
                time_ms = result.timeMs,
                share_code = result.shareCode,
            )
            true
        }
    }
}

/**
 * One row per [JokerType] name; rows of unknown types (from a newer app version) are ignored. Gift bookkeeping lives
 * in `joker_grant` (one row per [JokerGrantReason] name) so a gift and its day are written in one transaction.
 */
internal class JokerRepositoryImpl(databases: Databases, io: CoroutineDispatcher) :
    UserStore(databases, io),
    JokerRepository {
    override fun observeInventory(): Flow<JokerInventory> = observe {
        jokerQueries.selectAll().asFlow().mapToList(io).map { rows -> rows.toInventory() }
    }

    private fun List<Joker>.toInventory() = JokerInventory(
        mapNotNull { row -> JokerType.entries.firstOrNull { it.name == row.type }?.let { it to row.count.toInt() } }
            .toMap(),
    )

    override suspend fun save(inventory: JokerInventory) = write {
        jokerQueries.transaction {
            JokerType.entries.forEach { jokerQueries.setCount(it.name, inventory.count(it).toLong()) }
        }
    }

    override suspend fun add(type: JokerType, amount: Int) {
        require(amount >= 0) { "amount must not be negative, was $amount" }
        write {
            jokerQueries.transaction {
                jokerQueries.ensureRow(type.name)
                jokerQueries.addCount(amount = amount.toLong(), type = type.name)
            }
        }
    }

    override suspend fun grantStartPack(startPack: JokerInventory): Boolean = write {
        jokerQueries.transactionWithResult {
            if (jokerQueries.rowCount().executeAsOne() > 0) return@transactionWithResult false
            JokerType.entries.forEach { jokerQueries.setCount(it.name, startPack.count(it).toLong()) }
            true
        }
    }

    override suspend fun updateWallet(transform: (JokerWallet) -> JokerWallet): JokerWallet = write {
        jokerQueries.transactionWithResult {
            val days = jokerQueries.grantDays().executeAsList().associate { it.kind to it.epoch_day }
            val before = JokerWallet(
                inventory = jokerQueries.selectAll().executeAsList().toInventory(),
                lastDailyGiftDay = days[JokerGrantReason.DAILY_GIFT.name],
                lastStreakBonusDay = days[JokerGrantReason.STREAK_BONUS.name],
            )
            val after = transform(before)
            after.requireDaysNotBefore(before)
            if (after.inventory != before.inventory) {
                JokerType.entries.forEach { jokerQueries.setCount(it.name, after.inventory.count(it).toLong()) }
            }
            after.lastDailyGiftDay?.let { jokerQueries.setGrantDay(JokerGrantReason.DAILY_GIFT.name, it) }
            after.lastStreakBonusDay?.let { jokerQueries.setGrantDay(JokerGrantReason.STREAK_BONUS.name, it) }
            after
        }
    }
}
