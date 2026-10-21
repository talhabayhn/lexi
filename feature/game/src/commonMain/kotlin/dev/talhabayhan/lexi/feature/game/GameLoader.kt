package dev.talhabayhan.lexi.feature.game

import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.domain.daily.DailyPlayState
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.SavedGameCodec
import dev.talhabayhan.lexi.core.domain.game.WheelGenerator
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.JokerRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.srs.Srs
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** Result of opening a game ([GameLoader.open]). */
internal sealed interface Opened {
    /**
     * A playable game: [ui] is its first screen state ([GameUiMapper.map]); [snapshot] is the [SavedGameCodec] JSON
     * of a new game for the `SavedStateHandle` (null for a restored game, whose snapshot is already there).
     */
    data class Ready(
        val engine: GameEngine,
        val session: GameSession,
        val restored: Boolean,
        val ui: GameUiState,
        val snapshot: String?,
    ) : Opened

    data object Abandoned : Opened

    data object NoCards : Opened

    data object Failed : Opened
}

/**
 * Opens a game for a [GameRoute], for the [GameViewModel] and the [GamePreparer]. The repositories do their database
 * work on their IO dispatcher; the CPU work (wheel generation, `GameEngine.newGame`, the restore replay, the first UI
 * mapping and the snapshot encoding) runs on [cpu], never on the main thread. One seed per new game, drawn exactly as
 * before, so the engine stays deterministic.
 */
internal class GameLoader(
    private val cards: CardRepository,
    private val progress: ProgressRepository,
    private val jokers: JokerRepository,
    private val settings: SettingsRepository,
    private val appClock: AppClock,
    private val config: GameConfig,
    private val seeds: SeedSource,
    private val crashReporter: CrashReporter,
    private val getDailyWheel: GetDailyWheel? = null,
    private val cpu: CoroutineDispatcher = Dispatchers.Default,
) {
    /** The Classic time limit of a game: the one it started with ([stored]), else the setting if it is an option. */
    suspend fun classicLimit(stored: Int?): Int = stored
        ?: settings.settings.first().classicTimeLimitSeconds.takeIf { it in config.classicTimeLimitOptionsSeconds }
        ?: config.classicTimeLimitSeconds

    /**
     * Restores [saved] (the snapshot JSON) or starts a new game for [route]. A read failure throws (a retry keeps the
     * saved game); [Opened.Abandoned] when a card of the saved game left the content or a review has nothing due,
     * [Opened.NoCards] for a wheel without cards (never recorded).
     */
    suspend fun open(route: GameRoute, classicLimit: Int, saved: String?): Opened = withContext(cpu) {
        val engine = GameEngine(config.copy(classicTimeLimitSeconds = classicLimit))
        val savedGame = saved?.let(SavedGameCodec::decodeOrNull)
        if (savedGame == null) {
            newSession(route, engine)
        } else {
            GameSession.restore(engine, savedGame, cards.cards(savedGame.cardIds))
                ?.let { ready(engine, it, restored = true) }
                ?: Opened.Abandoned
        }
    }

    /** A new game for [route] with the Classic limit from the settings; a failure is [Opened.Failed], never thrown. */
    suspend fun openNew(route: GameRoute): Opened = try {
        open(route, classicLimit(stored = null), saved = null)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        log.e(e) { "game not prepared" }
        crashReporter.recordException(e, "game not prepared")
        Opened.Failed
    }

    /** A new game; [Opened.Abandoned] when a review wheel has no due card, [Opened.NoCards] for an empty wheel. */
    private suspend fun newSession(route: GameRoute, engine: GameEngine): Opened {
        val mode = route.gameMode
        val category = route.categoryId ?: CategoryId.MIXED
        var seed = seeds.nextSeed()
        val random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE)
        var level = route.gameLevel
        val wheel = when {
            mode == GameMode.REVIEW -> {
                // Review (GAME_RULES.md §1, §10): the due notebook cards of any level; the route's level is a placeholder.
                val today = appClock.epochDay(appClock.nowMs())
                val due = Srs.dueCards(progress.observeAll().first(), today)
                val reviewWheel = WheelGenerator.review(cards.cards(due), random, config)
                level = reviewWheel.slots.firstNotNullOfOrNull { it.card?.level } ?: return Opened.Abandoned
                reviewWheel
            }

            mode == GameMode.DAILY -> {
                val dailyWheelLoader = getDailyWheel ?: return Opened.Failed
                when (val dailyState = dailyWheelLoader(level)) {
                    is DailyPlayState.Ready -> {
                        seed = dailyState.seed
                        dailyState.wheel
                    }

                    is DailyPlayState.AlreadyPlayed -> return Opened.Abandoned

                    DailyPlayState.NoCards -> return Opened.NoCards
                }
            }

            else -> {
                val recentlySeen = if (mode.usesCardHistory) {
                    WheelGenerator.recentlySeen(progress.lastSeenMs(), appClock.nowMs(), config)
                } else {
                    emptySet()
                }
                WheelGenerator.generate(
                    pool = cards.wheelPool(level, category),
                    level = level,
                    category = category,
                    mode = mode,
                    random = random,
                    recentlySeen = recentlySeen,
                    config = config,
                )
            }
        }
        // The engine would end such a wheel at Start (EMPTY_WHEEL); it is not a game, so nothing is recorded.
        if (wheel.playableSlots.isEmpty()) return Opened.NoCards
        // Grants once per install (no-op afterwards), so a preparation that is never shown changes nothing else.
        jokers.grantStartPack(config.jokerStartPack)
        val inventory = jokers.observeInventory().first()
        val initial = engine.newGame(seed = seed, mode = mode, level = level, wheel = wheel, jokers = inventory)
        return ready(engine, GameSession.start(engine, initial), restored = false)
    }

    private fun ready(engine: GameEngine, session: GameSession, restored: Boolean) = Opened.Ready(
        engine = engine,
        session = session,
        restored = restored,
        ui = GameUiMapper.map(session.state, engine),
        snapshot = if (restored) null else SavedGameCodec.encode(session.saved),
    )

    private companion object {
        val log = Logger.withTag("GameLoader")
    }
}
