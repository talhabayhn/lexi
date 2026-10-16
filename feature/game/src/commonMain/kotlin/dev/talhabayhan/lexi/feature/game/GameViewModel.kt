package dev.talhabayhan.lexi.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.domain.config.FeatureFlags
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.game.EngineEffect
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.game.SavedGame
import dev.talhabayhan.lexi.core.domain.game.SavedGameCodec
import dev.talhabayhan.lexi.core.domain.game.WheelGenerator
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.JokerRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.session.FinishGame
import dev.talhabayhan.lexi.core.domain.srs.Srs
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.feature.game.GameSession.Companion.isIgnored
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/** Seed of a new solo game: a random 64-bit value (`ARCHITECTURE.md` §4). Injected so tests are deterministic. */
internal fun interface SeedSource {
    fun nextSeed(): Long
}

/**
 * Runs one game (ADR 0019): loads or restores the wheel, owns time (a 100 ms ticker over the injected [timeSource]
 * that runs only while playing, unpaused and in the foreground), feeds intents to the [GameSession] and maps the
 * engine's state and effects. Background ⇒ final tick + Pause; foreground keeps the game paused. The applied events
 * are kept in [SavedStateHandle] for process-death restore until the finished game is recorded once by [FinishGame].
 * Owns the screen flags the engine doesn't know: the joker sheet (the clock keeps running, `GAME_RULES.md` §5) and
 * the quit confirmation of the pause dialog.
 */
internal class GameViewModel(
    /** Navigation arguments, `handle.toRoute<GameRoute>()` (see `gameModule`). */
    private val route: GameRoute,
    private val handle: SavedStateHandle,
    private val cards: CardRepository,
    private val progress: ProgressRepository,
    private val jokers: JokerRepository,
    private val settings: SettingsRepository,
    private val finishGame: FinishGame,
    private val appClock: AppClock,
    private val speaker: Speaker,
    private val config: GameConfig,
    private val timeSource: TimeSource.WithComparableMarks,
    private val seeds: SeedSource,
    private val flags: FeatureFlags = FeatureFlags(),
) : ViewModel() {
    private val mutableState = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = mutableState.asStateFlow()

    private val mutableClock = MutableStateFlow(GameClockUi.Untimed)
    val clock: StateFlow<GameClockUi> = mutableClock.asStateFlow()

    private val effectChannel = Channel<GameEffect>(Channel.BUFFERED)
    val effects: Flow<GameEffect> = effectChannel.receiveAsFlow()

    private var engine: GameEngine? = null
    private var session: GameSession? = null
    private var foreground = false
    private var speakOnCorrect = true
    private var finished = false
    private var ticker: Job? = null

    /** The word of the last correct answer, spoken after the correct sound (motion.md #3). */
    private var speech: Job? = null
    private var lastMark: ComparableTimeMark? = null
    private var jokerSheetOpen = false
    private var quitConfirmation = false

    /** Joker inventory writes run one after another, each saving the latest inventory (ADR 0019). */
    private val jokerSaves = Mutex()

    init {
        viewModelScope.launch { load() }
        viewModelScope.launch { settings.settings.collect { speakOnCorrect = it.speakOnCorrect } }
    }

    fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.KeyTapped -> input(GameEvent.KeyTapped(intent.index))

            GameIntent.Backspace -> input(GameEvent.Backspace)

            GameIntent.Pass -> input(GameEvent.Pass)

            // The sheet does not pause the game (GAME_RULES.md §5).
            GameIntent.OpenJokers -> if (state.value.inputEnabled) updateFlags(jokerSheet = true)

            GameIntent.CloseJokers -> updateFlags(jokerSheet = false)

            is GameIntent.UseJoker -> input(GameEvent.UseJoker(intent.type))

            GameIntent.Pause -> input(GameEvent.Pause)

            GameIntent.Resume -> input(GameEvent.Resume)

            GameIntent.RequestQuit -> if (session?.state?.isPaused == true) updateFlags(quit = true)

            GameIntent.CancelQuit -> updateFlags(quit = false)

            GameIntent.Forfeit -> input(GameEvent.Forfeit)

            GameIntent.Back -> back()

            GameIntent.Background -> {
                foreground = false
                input(GameEvent.Pause)
                stopSpeaking()
            }

            GameIntent.Foreground -> {
                foreground = true
                startIfReady()
            }
        }
    }

    /** System back, see [GameIntent.Back]. */
    private fun back() {
        val current = session?.state ?: return
        when {
            jokerSheetOpen -> updateFlags(jokerSheet = false)

            quitConfirmation -> updateFlags(quit = false)

            current.status == GameStatus.PLAYING && !current.isPaused -> input(GameEvent.Pause)

            // Paused (the dialog stays: only "Devam et" restarts the clock), not started or finished: nothing.
            else -> Unit
        }
    }

    private fun updateFlags(jokerSheet: Boolean = jokerSheetOpen, quit: Boolean = quitConfirmation) {
        jokerSheetOpen = jokerSheet
        quitConfirmation = quit
        session?.state?.let(::publish)
    }

    /** Leaving a running game silences it; the last word of a finished game may still play over the result. */
    override fun onCleared() {
        if (finished) speech?.cancel() else stopSpeaking()
    }

    private suspend fun load() {
        val current = settings.settings.first()
        val limit = handle.get<Int>(KEY_CLASSIC_LIMIT)
            ?: current.classicTimeLimitSeconds.takeIf { it in config.classicTimeLimitOptionsSeconds }
            ?: config.classicTimeLimitSeconds
        handle[KEY_CLASSIC_LIMIT] = limit
        val engine = GameEngine(config.copy(classicTimeLimitSeconds = limit)).also { engine = it }
        val saved = handle.get<String>(KEY_GAME)?.let(SavedGameCodec::decodeOrNull)
        val restored = saved?.let { GameSession.restore(engine, it, cards.cards(it.cardIds)) }
        if (saved != null && restored == null) {
            handle.remove<String>(KEY_GAME)
            effectChannel.send(GameEffect.Abandoned)
            return
        }
        session = restored ?: newSession(engine)
        if (session == null) {
            // A review with nothing due (the notebook only offers it when something is).
            effectChannel.send(GameEffect.Abandoned)
            return
        }
        val state = checkNotNull(session).state
        publish(state)
        when {
            state.status == GameStatus.FINISHED -> onGameOver()

            // ADR 0019: a restored running game always comes back paused.
            restored != null && state.status == GameStatus.PLAYING -> dispatch(GameEvent.Pause)

            else -> startIfReady()
        }
    }

    /** Null when a review wheel has no due card. */
    private suspend fun newSession(engine: GameEngine): GameSession? {
        val mode = route.gameMode
        val category = route.categoryId ?: CategoryId.MIXED
        val seed = seeds.nextSeed()
        val random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE)
        var level = route.gameLevel
        val wheel = if (mode == GameMode.REVIEW) {
            // Review (GAME_RULES.md §1, §10): the due notebook cards of any level; the route's level is a placeholder.
            val today = appClock.epochDay(appClock.nowMs())
            val due = Srs.dueCards(progress.observeAll().first(), today)
            val reviewWheel = WheelGenerator.review(cards.cards(due), random)
            level = reviewWheel.slots.firstNotNullOfOrNull { it.card?.level } ?: return null
            reviewWheel
        } else {
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
            )
        }
        jokers.grantStartPack(config.jokerStartPack)
        val inventory = jokers.observeInventory().first()
        val initial = engine.newGame(seed = seed, mode = mode, level = level, wheel = wheel, jokers = inventory)
        return GameSession.start(engine, initial).also { save(it) }
    }

    /** `Start` once the wheel is loaded and the screen is resumed for the first time. */
    private fun startIfReady() {
        val current = session ?: return
        if (foreground && current.state.status == GameStatus.NOT_STARTED) {
            dispatch(GameEvent.Start(timestampMs = appClock.nowMs()))
        }
    }

    /** Player and lifecycle events: the time up to now is charged first, so input lands at the right moment. */
    private fun input(event: GameEvent) {
        flushTick()
        dispatch(event)
    }

    private fun dispatch(event: GameEvent) {
        val current = session ?: return
        val before = current.state
        val transition = current.dispatch(event)
        // Ignored = same state and no effects, exactly what GameSession leaves out of the log.
        if (transition.isIgnored(before)) return
        // Ticks are saved with the next other event; going to the background always dispatches Pause.
        if (event !is GameEvent.Tick || transition.state.status == GameStatus.FINISHED) save(current)
        val state = transition.state
        // The sheet closes when its joker was applied and whenever input stops (pause, reveal, game over).
        if (transition.effects.any { it is EngineEffect.JokerApplied } || !acceptsInput(state)) jokerSheetOpen = false
        if (!state.isPaused || state.status != GameStatus.PLAYING) quitConfirmation = false
        publish(state)
        transition.effects.forEach { onEngineEffect(it, state, before) }
        syncTicker()
    }

    private fun acceptsInput(state: GameState): Boolean =
        state.status == GameStatus.PLAYING && !state.isPaused && !state.isRevealing && state.currentInput != null

    private fun publish(state: GameState) {
        mutableState.value = GameUiMapper.map(state, checkNotNull(engine), mutableState.value).copy(
            isJokerSheetOpen = jokerSheetOpen,
            isQuitConfirmation = quitConfirmation,
            showJokerAd = flags.rewardedAdJokers,
        )
        mutableClock.value = GameUiMapper.clock(
            state = state,
            timeLimitMs = engine?.config?.timeLimitMs(state.mode),
            warningSeconds = config.timeWarningSeconds,
        )
    }

    /**
     * Keeps the event log for restore. A finished game stays saved until [FinishGame] has recorded it, so a process
     * death in between records it after restore (ADR 0019 addendum); [onGameOver] removes it.
     */
    private fun save(current: GameSession) {
        handle[KEY_GAME] = SavedGameCodec.encode(current.saved)
    }

    /** [before] is the state before the transition (the slot a letter was placed in may be done afterwards). */
    private fun onEngineEffect(effect: EngineEffect, state: GameState, before: GameState) {
        when (effect) {
            is EngineEffect.LetterPlaced -> {
                val slot = before.currentIndex ?: state.currentIndex
                val keyIndex = slot?.let(state.inputs::get)?.cells?.getOrNull(effect.cellIndex)?.keyIndex
                if (keyIndex != null) emit(GameEffect.LetterPlaced(effect.cellIndex, keyIndex, effect.letter))
            }

            is EngineEffect.Correct -> {
                emit(GameEffect.Correct(effect.letter, effect.points))
                val answer = state.wheel.slots.firstOrNull { it.card?.id == effect.card }?.card?.answer
                if (speakOnCorrect && answer != null) speakAfterCorrectSound(answer.lowercase(), state)
            }

            is EngineEffect.Wrong -> emit(GameEffect.Wrong(effect.letter))

            is EngineEffect.Passed -> emit(GameEffect.Passed(effect.letter))

            is EngineEffect.Advanced -> emit(GameEffect.Advanced(effect.toLetter))

            is EngineEffect.ComboReached -> emit(GameEffect.ComboReached(effect.streak))

            is EngineEffect.TimeWarning -> emit(GameEffect.TimeWarning(effect.secondsLeft))

            is EngineEffect.JokerApplied -> {
                saveJokers()
                // "Telaffuzu dinle": the answer is spoken (whatever the speak-on-correct setting).
                if (effect.type == JokerType.LISTEN) {
                    state.currentSlot?.card?.answer?.let {
                        speech?.cancel()
                        speaker.speak(it.lowercase())
                    }
                }
            }

            is EngineEffect.GameOver -> onGameOver()

            // Duel only (M8).
            is EngineEffect.TurnChanged, is EngineEffect.MatchOver -> Unit
        }
    }

    /**
     * motion.md #3: "two rising notes, then the word", so the word waits for the correct sound to end. The last answer
     * of a game is spoken at once: the screen leaves for the result (and this scope ends) before the sound is over.
     */
    private fun speakAfterCorrectSound(word: String, state: GameState) {
        speech?.cancel()
        if (state.status == GameStatus.FINISHED) {
            speaker.speak(word)
            return
        }
        speech = viewModelScope.launch {
            delay(CORRECT_SOUND_DURATION)
            speaker.speak(word)
        }
    }

    private fun stopSpeaking() {
        speech?.cancel()
        speech = null
        speaker.stop()
    }

    /** Transient motion is best effort: while nobody collects (background) and the buffer is full, it is dropped. */
    private fun emit(effect: GameEffect) {
        effectChannel.trySend(effect)
    }

    /**
     * Saved at once and not cancellable: killing the app or leaving the screen never refunds a joker (ADR 0019). The
     * writes are serialized and each stores the inventory current when it runs, so the last write is the newest.
     */
    private fun saveJokers() {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            withContext(NonCancellable) {
                jokerSaves.withLock {
                    val inventory = session?.state?.jokers ?: return@withLock
                    try {
                        jokers.save(inventory)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // TODO(M7): report through the crash reporter (Crashlytics behind an interface).
                        log.e(e) { "joker inventory not saved" }
                    }
                }
            }
        }
    }

    /**
     * Records the game exactly once (one retry after a storage failure), then leaves for the result. The saved
     * snapshot is removed only after [FinishGame] succeeded.
     */
    private fun onGameOver() {
        if (finished) return
        finished = true
        jokerSheetOpen = false
        quitConfirmation = false
        syncTicker()
        val final = checkNotNull(session).state
        publish(final)
        val category = sessionCategory()
        viewModelScope.launch {
            val recorded = withContext(NonCancellable) { record(final, category) }
            if (recorded) handle.remove<String>(KEY_GAME)
            effectChannel.send(GameEffect.NavigateToResult(ResultRoute.of(final, route.categoryId, recorded)))
        }
    }

    /** [FinishGame] with one retry (it repeats only the steps that failed); false when both attempts failed. */
    private suspend fun record(final: GameState, category: CategoryId?): Boolean {
        repeat(FINISH_ATTEMPTS) { attempt ->
            try {
                finishGame(final, category = category)
                return true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // TODO(M7): report through the crash reporter (Crashlytics behind an interface).
                log.e(e) { "finishing the game failed (attempt ${attempt + 1} of $FINISH_ATTEMPTS)" }
            }
        }
        return false
    }

    /** `SessionResult.category`: null for wheels without a category (review, daily, level test). */
    private fun sessionCategory(): CategoryId? = when (route.gameMode) {
        GameMode.REVIEW, GameMode.DAILY, GameMode.LEVEL_TEST -> null
        else -> route.categoryId ?: CategoryId.MIXED
    }

    // region ticker (ADR 0019 §1)

    private fun syncTicker() {
        val state = session?.state
        val run = !finished && foreground && state != null && state.status == GameStatus.PLAYING && !state.isPaused
        if (run && ticker == null) {
            lastMark = timeSource.markNow()
            ticker = viewModelScope.launch {
                while (true) {
                    delay(TICK_INTERVAL)
                    flushTick()
                }
            }
        } else if (!run && ticker != null) {
            ticker?.cancel()
            ticker = null
            lastMark = null
        }
    }

    /** Dispatches the whole milliseconds since the last tick; the mark advances by exactly that, so nothing drifts. */
    private fun flushTick() {
        val mark = lastMark ?: return
        val elapsedMs = mark.elapsedNow().inWholeMilliseconds
        if (elapsedMs <= 0) return
        lastMark = mark + elapsedMs.milliseconds
        dispatch(GameEvent.Tick(elapsedMs))
    }

    // endregion

    companion object {
        /** Technical tick rate, not a game rule: keeps the 1200 ms reveal and the timer bar precise. */
        val TICK_INTERVAL = 100.milliseconds

        /** Length of the correct sound (`files/sounds/correct.wav`, two notes of 110 + 170 ms); TTS starts after it. */
        val CORRECT_SOUND_DURATION = 280.milliseconds

        /** A failed [FinishGame] is retried once. */
        private const val FINISH_ATTEMPTS = 2

        private val log = Logger.withTag("GameViewModel")

        /** [SavedStateHandle] keys: the [SavedGame] JSON and the Classic time limit the game started with. */
        const val KEY_GAME: String = "game.saved"
        const val KEY_CLASSIC_LIMIT: String = "game.classicLimitSeconds"
    }
}
