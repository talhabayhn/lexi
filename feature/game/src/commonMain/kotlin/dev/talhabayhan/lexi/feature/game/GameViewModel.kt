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
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.game.SavedGame
import dev.talhabayhan.lexi.core.domain.game.SavedGameCodec
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.repository.JokerRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.session.FinishGame
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.feature.game.GameSession.Companion.isIgnored
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
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
 * Runs one game (ADR 0019): takes the game prepared before the navigation ([GamePreparer]) or loads / restores it
 * through the [GameLoader], owns time (a 100 ms ticker over the injected [timeSource]
 * that runs only while playing, unpaused and in the foreground), feeds intents to the [GameSession] and maps the
 * engine's state and effects. Background ⇒ final tick + Pause; foreground keeps the game paused. The applied events
 * are kept in [SavedStateHandle] for process-death restore until the finished game is recorded once by [FinishGame].
 * Owns the screen flags the engine doesn't know: the joker sheet (the clock keeps running, `GAME_RULES.md` §5) and
 * the quit and restart confirmations of the pause dialog.
 */
internal class GameViewModel(
    /** Navigation arguments, `handle.toRoute<GameRoute>()` (see `gameModule`). */
    private val route: GameRoute,
    private val handle: SavedStateHandle,
    private val loader: GameLoader,
    private val jokers: JokerRepository,
    private val settings: SettingsRepository,
    private val finishGame: FinishGame,
    private val appClock: AppClock,
    private val speaker: Speaker,
    private val config: GameConfig,
    private val timeSource: TimeSource.WithComparableMarks,
    private val flags: FeatureFlags = FeatureFlags(),
    /** A new game prepared before the navigation ([GamePreparer]); taken once, unless a saved game is restored. */
    preparer: GamePreparer? = null,
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

    private var lastMark: ComparableTimeMark? = null
    private var jokerSheetOpen = false
    private var quitConfirmation = false
    private var restartConfirmation = false

    /** Joker inventory writes run one after another, each saving the latest inventory (ADR 0019). */
    private val jokerSaves = Mutex()

    /** The running [load], so a double tap on "Tekrar dene" loads once. */
    private var loading: Job? = null

    init {
        val prepared = if (handle.contains(KEY_GAME)) null else preparer?.take(route)
        loading = when {
            prepared == null || prepared.isCancelled -> viewModelScope.launch { load() }

            // Ready before the navigation: published now, so the first composition already shows the board.
            prepared.isCompleted -> {
                @OptIn(ExperimentalCoroutinesApi::class)
                show(prepared.getCompleted())
                null
            }

            else -> viewModelScope.launch { awaitPrepared(prepared) }
        }
        viewModelScope.launch {
            settings.settings.collect {
                speakOnCorrect = it.speakOnCorrect
                // Normally done at app start; also here when the setting was just turned on.
                if (speakOnCorrect) speaker.warmUp()
            }
        }
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

            GameIntent.RequestQuit -> if (isPausedGame()) updateFlags(quit = true)

            GameIntent.CancelQuit -> updateFlags(quit = false)

            GameIntent.Forfeit -> input(GameEvent.Forfeit)

            GameIntent.RequestRestart -> if (isPausedGame() && state.value.canRestart) updateFlags(restart = true)

            GameIntent.CancelRestart -> updateFlags(restart = false)

            GameIntent.Restart -> restart()

            GameIntent.OpenSettings -> if (isPausedGame()) emit(GameEffect.NavigateToSettings)

            GameIntent.Back -> back()

            GameIntent.Retry -> retry()

            GameIntent.Leave -> if (state.value.loadError != null) emit(GameEffect.NavigateBack)

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

            restartConfirmation -> updateFlags(restart = false)

            current.status == GameStatus.PLAYING && !current.isPaused -> input(GameEvent.Pause)

            // Paused (the dialog stays: only "Devam et" restarts the clock), not started or finished: nothing.
            else -> Unit
        }
    }

    private fun updateFlags(
        jokerSheet: Boolean = jokerSheetOpen,
        quit: Boolean = quitConfirmation,
        restart: Boolean = restartConfirmation,
    ) {
        jokerSheetOpen = jokerSheet
        quitConfirmation = quit
        restartConfirmation = restart
        session?.state?.let(::publish)
    }

    /** A running game that is paused (the pause dialog is shown). */
    private fun isPausedGame(): Boolean {
        val current = session?.state ?: return false
        return !finished && current.status == GameStatus.PLAYING && current.isPaused
    }

    /**
     * "Yeniden başlat" confirmed: the paused game is dropped without being recorded (its saved snapshot too; used
     * jokers stay used, ADR 0019) and [load] starts a new wheel for the same route with a new seed. The Classic time
     * limit the game started with is kept ([KEY_CLASSIC_LIMIT]); the new game starts at once (the screen is resumed).
     */
    private fun restart() {
        if (!restartConfirmation || !isPausedGame() || !state.value.canRestart || loading?.isActive == true) return
        stopSpeaking()
        session = null
        engine = null
        jokerSheetOpen = false
        quitConfirmation = false
        restartConfirmation = false
        syncTicker()
        handle.remove<String>(KEY_GAME)
        mutableState.value = GameUiState()
        mutableClock.value = GameClockUi.Untimed
        loading = viewModelScope.launch { load() }
    }

    /** Leaving a running game silences it; the last word of a finished game may still play over the result. */
    override fun onCleared() {
        if (!finished) speaker.stop()
    }

    /**
     * Loads the wheel: restores the saved game or starts a new one. A read failure (content or user database) shows
     * [GameLoadError.Unreadable] with a retry; a wheel without cards shows [GameLoadError.NoCards] and is never
     * recorded. A saved game whose cards left the content and a review with nothing due are abandoned.
     */
    private suspend fun load() {
        val opened = try {
            val limit = loader.classicLimit(handle.get<Int>(KEY_CLASSIC_LIMIT))
            handle[KEY_CLASSIC_LIMIT] = limit
            loader.open(route, limit, handle.get<String>(KEY_GAME))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // TODO(M7): report through the crash reporter (Crashlytics behind an interface).
            log.e(e) { "game not loaded" }
            Opened.Failed
        }
        show(opened)
    }

    /** A preparation that was still running when the screen opened (it navigated after the cap); loading until then. */
    private suspend fun awaitPrepared(prepared: Deferred<Opened>) {
        val opened = try {
            prepared.await()
        } catch (e: CancellationException) {
            // This screen was cleared: nobody needs the preparation any more.
            if (!currentCoroutineContext().isActive) prepared.cancel()
            currentCoroutineContext().ensureActive()
            // Only the preparation was cancelled: load as without one.
            null
        }
        if (opened == null) load() else show(opened)
    }

    private fun show(opened: Opened) {
        when (opened) {
            Opened.Abandoned -> {
                handle.remove<String>(KEY_GAME)
                effectChannel.trySend(GameEffect.Abandoned)
            }

            Opened.Failed -> showLoadError(GameLoadError.Unreadable)

            Opened.NoCards -> showLoadError(GameLoadError.NoCards)

            is Opened.Ready -> {
                engine = opened.engine
                session = opened.session
                handle[KEY_CLASSIC_LIMIT] = opened.engine.config.classicTimeLimitSeconds
                opened.snapshot?.let { handle[KEY_GAME] = it }
                val state = opened.session.state
                publish(state, opened.ui)
                when {
                    state.status == GameStatus.FINISHED -> onGameOver()

                    // ADR 0019: a restored running game always comes back paused.
                    opened.restored && state.status == GameStatus.PLAYING -> dispatch(GameEvent.Pause)

                    else -> startIfReady()
                }
            }
        }
    }

    private fun showLoadError(error: GameLoadError) {
        mutableState.value = GameUiState(isLoading = false, loadError = error)
    }

    /** "Tekrar dene": only after a read failure, and not while a load is running. */
    private fun retry() {
        if (state.value.loadError != GameLoadError.Unreadable || loading?.isActive == true) return
        mutableState.value = GameUiState()
        loading = viewModelScope.launch { load() }
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
        if (!state.isPaused || state.status != GameStatus.PLAYING) {
            quitConfirmation = false
            restartConfirmation = false
        }
        publish(state)
        transition.effects.forEach { onEngineEffect(it, state, before) }
        syncTicker()
    }

    private fun acceptsInput(state: GameState): Boolean =
        state.status == GameStatus.PLAYING && !state.isPaused && !state.isRevealing && state.currentInput != null

    private fun publish(
        state: GameState,
        mapped: GameUiState = GameUiMapper.map(state, checkNotNull(engine), mutableState.value),
    ) {
        mutableState.value = mapped.copy(
            isJokerSheetOpen = jokerSheetOpen,
            isQuitConfirmation = quitConfirmation,
            isRestartConfirmation = restartConfirmation,
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
                val answer = state.wheel.slots.firstOrNull { it.card?.id == effect.card }?.card?.answer
                // motion.md #3: the word is spoken the moment the answer is judged, in place of the correct sound. Speech
                // off, an engine still starting or without a voice: the correct sound plays as before.
                val spoken = speakOnCorrect && answer != null && speaker.isReady
                if (spoken) speaker.speak(checkNotNull(answer).lowercase())
                emit(GameEffect.Correct(effect.letter, effect.points, spoken))
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
                    state.currentSlot?.card?.answer?.let { speaker.speak(it.lowercase()) }
                }
            }

            is EngineEffect.GameOver -> {
                if (effect.summary.reason == GameOverReason.TIME_UP) emit(GameEffect.TimeUp)
                onGameOver()
            }

            // Duel only (M8).
            is EngineEffect.TurnChanged, is EngineEffect.MatchOver -> Unit
        }
    }

    private fun stopSpeaking() {
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
        restartConfirmation = false
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

        /** A failed [FinishGame] is retried once. */
        private const val FINISH_ATTEMPTS = 2

        private val log = Logger.withTag("GameViewModel")

        /** [SavedStateHandle] keys: the [SavedGame] JSON and the Classic time limit the game started with. */
        const val KEY_GAME: String = "game.saved"
        const val KEY_CLASSIC_LIMIT: String = "game.classicLimitSeconds"
    }
}
