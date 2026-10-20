package dev.talhabayhan.lexi.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.game.EngineEffect
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.SavedGame
import dev.talhabayhan.lexi.core.domain.game.SavedGameCodec
import dev.talhabayhan.lexi.core.domain.leveltest.LevelTest
import dev.talhabayhan.lexi.core.domain.leveltest.LevelTestRun
import dev.talhabayhan.lexi.core.domain.leveltest.LevelTestRunner
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/** One-off events of the level test run. */
internal sealed interface LevelTestEffect {
    /** Transient motion / feedback of the game screen (`motion.md` #2–#8); never a navigation effect. */
    data class Motion(val effect: GameEffect) : LevelTestEffect

    /** The test is over (last answer or time up): the placement to show. Nothing was recorded. */
    data class Finished(val level: Level) : LevelTestEffect

    /** "Pes et" (confirmed) or "Geri dön" of the load error: back to the level choice; nothing is saved. */
    data object Quit : LevelTestEffect

    /** "Ayarlar" of the pause dialog: the settings over the paused test. */
    data object OpenSettings : LevelTestEffect
}

/**
 * Runs the onboarding level test (`GAME_RULES.md` §9) on the game screen. Loads the level cards
 * ([CardRepository.levelCards]) asynchronously, owns the [LevelTestRunner] run and time like [GameViewModel] (a
 * 100 ms ticker over [timeSource] only while a card is playing, unpaused and in the foreground; background ⇒ final
 * tick + Pause; a restored run comes back paused, ADR 0019) and maps it with [LevelTestUiMapper].
 *
 * Records nothing (owner decision 1): no `FinishGame`, `SessionRecord`, SRS, stats, streak or recently-seen history —
 * the only state written is the process-death snapshot in [handle] ([KEY_RUN]: the seed and the applied events as a
 * [SavedGame] without slots, the first event being the run's `Start`, consecutive ticks coalesced). Jokers and the
 * restart are not offered; "Pes et" leaves with [LevelTestEffect.Quit].
 */
internal class LevelTestViewModel(
    private val handle: SavedStateHandle,
    private val cards: CardRepository,
    private val settings: SettingsRepository,
    private val appClock: AppClock,
    private val speaker: Speaker,
    private val config: GameConfig,
    private val timeSource: TimeSource.WithComparableMarks,
    private val seeds: SeedSource,
    /** The restore replay and first mapping run here, never on the main thread. */
    private val cpu: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val engine = GameEngine(config)
    private val runner = LevelTestRunner(engine)

    private val mutableState = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = mutableState.asStateFlow()

    private val mutableClock = MutableStateFlow(GameClockUi.Untimed)
    val clock: StateFlow<GameClockUi> = mutableClock.asStateFlow()

    private val effectChannel = Channel<LevelTestEffect>(Channel.BUFFERED)
    val effects: Flow<LevelTestEffect> = effectChannel.receiveAsFlow()

    private var pool: List<Card> = emptyList()
    private var run: LevelTestRun? = null

    /** Seed + applied events; [SavedGame.events] is empty until the run has started. */
    private var snapshot: SavedGame? = null
    private val started: Boolean get() = snapshot?.events?.isNotEmpty() == true

    /** Finished or quit: no more input, ticks or effects but the last one. */
    private var done = false
    private var foreground = false
    private var speakOnCorrect = true
    private var quitConfirmation = false
    private var ticker: Job? = null
    private var lastMark: ComparableTimeMark? = null
    private var loading: Job? = null

    init {
        loading = viewModelScope.launch { load() }
        viewModelScope.launch {
            settings.settings.collect {
                speakOnCorrect = it.speakOnCorrect
                if (speakOnCorrect) speaker.warmUp()
            }
        }
    }

    fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.KeyTapped -> input(GameEvent.KeyTapped(intent.index))

            GameIntent.Backspace -> input(GameEvent.Backspace)

            GameIntent.Pass -> input(GameEvent.Pass)

            GameIntent.Pause -> input(GameEvent.Pause)

            GameIntent.Resume -> input(GameEvent.Resume)

            GameIntent.RequestQuit -> if (isPausedRun()) updateQuit(true)

            GameIntent.CancelQuit -> updateQuit(false)

            GameIntent.Forfeit -> if (quitConfirmation && isPausedRun()) quit()

            GameIntent.OpenSettings -> if (isPausedRun()) emit(LevelTestEffect.OpenSettings)

            GameIntent.Back -> back()

            GameIntent.Retry -> retry()

            GameIntent.Leave -> if (state.value.loadError != null) emit(LevelTestEffect.Quit)

            GameIntent.Background -> {
                foreground = false
                input(GameEvent.Pause)
                speaker.stop()
            }

            GameIntent.Foreground -> {
                foreground = true
                startIfReady()
            }

            // No jokers and no restart in the level test (GAME_RULES.md §9): the screen doesn't offer them.
            GameIntent.OpenJokers, GameIntent.CloseJokers, is GameIntent.UseJoker,
            GameIntent.RequestRestart, GameIntent.CancelRestart, GameIntent.Restart,
            -> Unit
        }
    }

    override fun onCleared() {
        if (!done) speaker.stop()
    }

    /** System back: leaves the quit confirmation, pauses a running card; on the pause dialog it does nothing. */
    private fun back() {
        val game = run?.game ?: return
        when {
            quitConfirmation -> updateQuit(false)
            started && !done && game.status == GameStatus.PLAYING && !game.isPaused -> input(GameEvent.Pause)
            else -> Unit
        }
    }

    private fun isPausedRun(): Boolean {
        val game = run?.game ?: return false
        return started && !done && game.status == GameStatus.PLAYING && game.isPaused
    }

    private fun updateQuit(confirmation: Boolean) {
        quitConfirmation = confirmation
        run?.let(::publish)
    }

    /** "Pes et" confirmed: the run is dropped with its snapshot; nothing is saved. */
    private fun quit() {
        done = true
        quitConfirmation = false
        syncTicker()
        speaker.stop()
        handle.remove<String>(KEY_RUN)
        viewModelScope.launch { effectChannel.send(LevelTestEffect.Quit) }
    }

    // region loading

    private class Loaded(val pool: List<Card>, val snapshot: SavedGame, val run: LevelTestRun)

    private suspend fun load() {
        val loaded = try {
            val levelCards = cards.levelCards()
            val saved = handle.get<String>(KEY_RUN)?.let(SavedGameCodec::decodeOrNull)
            withContext(cpu) { open(levelCards, saved) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // TODO(M7): report through the crash reporter (Crashlytics behind an interface).
            log.e(e) { "level test not loaded" }
            null
        }
        if (loaded == null) showLoadError(GameLoadError.Unreadable) else show(loaded)
    }

    /**
     * Replays a saved run, or prepares a new one: before `Start` [run] is a preview started at time 0 (the cards
     * depend only on the seed, the start time is only recorded), so starting on resume changes nothing on screen. A
     * snapshot that no longer replays (the content changed) starts a new test.
     */
    private fun open(levelCards: List<Card>, saved: SavedGame?): Loaded {
        val start = saved?.events?.firstOrNull() as? GameEvent.Start
        if (saved != null && start != null) {
            val replayed = try {
                saved.events.drop(1).fold(runner.start(saved.seed, levelCards, start.timestampMs).state) { run, event ->
                    runner.reduce(run, levelCards, event).state
                }
            } catch (e: IllegalArgumentException) {
                log.w(e) { "saved level test no longer replays" }
                null
            } catch (e: IllegalStateException) {
                log.w(e) { "saved level test no longer replays" }
                null
            }
            if (replayed != null) return Loaded(levelCards, saved, replayed)
        }
        val seed = saved?.takeIf { start == null }?.seed ?: seeds.nextSeed()
        return Loaded(levelCards, newSnapshot(seed), runner.start(seed, levelCards, timestampMs = 0L).state)
    }

    private fun newSnapshot(seed: Long) = SavedGame(
        seed = seed,
        mode = GameMode.LEVEL_TEST,
        level = LevelTest.START_LEVEL,
        slots = emptyList(),
        jokers = JokerInventory(),
    )

    private fun show(loaded: Loaded) {
        pool = loaded.pool
        snapshot = loaded.snapshot
        run = loaded.run
        val current = loaded.run
        when {
            current.isUnavailable -> {
                handle.remove<String>(KEY_RUN)
                showLoadError(GameLoadError.LevelTestUnavailable)
            }

            !started -> {
                save()
                publish(current)
                startIfReady()
            }

            current.isFinished -> {
                publish(current)
                finish(timeUp = false)
            }

            else -> {
                publish(current)
                // ADR 0019: a restored running test always comes back paused.
                dispatch(GameEvent.Pause)
            }
        }
    }

    private fun showLoadError(error: GameLoadError) {
        mutableState.value = GameUiState(isLoading = false, loadError = error, isLevelTest = true)
    }

    private fun retry() {
        if (state.value.loadError != GameLoadError.Unreadable || loading?.isActive == true) return
        mutableState.value = GameUiState()
        loading = viewModelScope.launch { load() }
    }

    /** The real `Start` once the run is loaded and the screen is resumed for the first time. */
    private fun startIfReady() {
        val current = snapshot ?: return
        if (!foreground || started || done || run?.isUnavailable != false) return
        val timestampMs = appClock.nowMs()
        val transition = runner.start(current.seed, pool, timestampMs)
        snapshot = current.append(GameEvent.Start(timestampMs))
        save()
        apply(transition.state, transition.effects, event = null, before = checkNotNull(run))
    }

    // endregion

    /** Player and lifecycle events: the time up to now is charged first. */
    private fun input(event: GameEvent) {
        flushTick()
        dispatch(event)
    }

    private fun dispatch(event: GameEvent) {
        val current = run ?: return
        val saved = snapshot ?: return
        if (!started || done) return
        val transition = runner.reduce(current, pool, event)
        // Ignored = same run and no effects; not logged.
        if (transition.state === current && transition.effects.isEmpty()) return
        snapshot = saved.append(event)
        // Ticks are saved with the next other event; going to the background always dispatches Pause.
        if (event !is GameEvent.Tick || transition.state.isFinished) save()
        apply(transition.state, transition.effects, event, before = current)
    }

    private fun apply(next: LevelTestRun, effects: List<EngineEffect>, event: GameEvent?, before: LevelTestRun) {
        run = next
        if (next.game?.isPaused != true) quitConfirmation = false
        publish(next)
        effects.forEach { onEngineEffect(it, event, before, next) }
        if (next.isFinished) finish(timeUp = next.remainingMs <= 0L)
        syncTicker()
    }

    private fun publish(current: LevelTestRun) {
        val mapped = LevelTestUiMapper.map(current, engine, mutableState.value)
        mutableState.value = mapped.copy(
            isStarting = !started,
            inputEnabled = mapped.inputEnabled && started && !done,
            isPaused = mapped.isPaused && !done,
            isQuitConfirmation = quitConfirmation,
        )
        mutableClock.value = LevelTestUiMapper.clock(current, engine, config.timeWarningSeconds)
    }

    private fun save() {
        snapshot?.let { handle[KEY_RUN] = SavedGameCodec.encode(it) }
    }

    /**
     * Engine effects as motion. A letter can only come from a key here (no jokers), so its key is the tapped one; the
     * card of a correct answer may already be in [LevelTestRun.asked] when the next card started.
     */
    private fun onEngineEffect(effect: EngineEffect, event: GameEvent?, before: LevelTestRun, after: LevelTestRun) {
        when (effect) {
            is EngineEffect.LetterPlaced -> if (event is GameEvent.KeyTapped) {
                emitMotion(GameEffect.LetterPlaced(effect.cellIndex, event.keyIndex, effect.letter))
            }

            is EngineEffect.Correct -> {
                val answer = (after.asked.map { it.card } + before.game?.wheel?.slots?.map { it.card }.orEmpty())
                    .firstOrNull { it?.id == effect.card }?.answer
                val spoken = speakOnCorrect && answer != null && speaker.isReady
                if (spoken) speaker.speak(checkNotNull(answer).lowercase())
                emitMotion(GameEffect.Correct(effect.letter, effect.points, spoken))
            }

            is EngineEffect.Wrong -> emitMotion(GameEffect.Wrong(effect.letter))

            is EngineEffect.Passed -> emitMotion(GameEffect.Passed(effect.letter))

            is EngineEffect.Advanced -> emitMotion(GameEffect.Advanced(effect.toLetter))

            is EngineEffect.ComboReached -> emitMotion(GameEffect.ComboReached(effect.streak))

            is EngineEffect.TimeWarning -> emitMotion(GameEffect.TimeWarning(effect.secondsLeft))

            // Not passed on by the runner (GameOver) or not part of a one-player test.
            is EngineEffect.GameOver, is EngineEffect.JokerApplied, is EngineEffect.TurnChanged,
            is EngineEffect.MatchOver,
            -> Unit
        }
    }

    /** The test is over: time up (motion) and the placement, sent once and never dropped. */
    private fun finish(timeUp: Boolean) {
        if (done) return
        done = true
        quitConfirmation = false
        syncTicker()
        val current = checkNotNull(run)
        publish(current)
        if (timeUp) emitMotion(GameEffect.TimeUp)
        val level = checkNotNull(current.result)
        viewModelScope.launch { effectChannel.send(LevelTestEffect.Finished(level)) }
    }

    /** Transient motion is best effort, like the game's. */
    private fun emitMotion(effect: GameEffect) {
        effectChannel.trySend(LevelTestEffect.Motion(effect))
    }

    private fun emit(effect: LevelTestEffect) {
        effectChannel.trySend(effect)
    }

    // region ticker (ADR 0019 §1)

    private fun syncTicker() {
        val game = run?.game
        val running = !done && started && foreground && game != null && game.status == GameStatus.PLAYING &&
            !game.isPaused
        if (running && ticker == null) {
            lastMark = timeSource.markNow()
            ticker = viewModelScope.launch {
                while (true) {
                    delay(GameViewModel.TICK_INTERVAL)
                    flushTick()
                }
            }
        } else if (!running && ticker != null) {
            ticker?.cancel()
            ticker = null
            lastMark = null
        }
    }

    private fun flushTick() {
        val mark = lastMark ?: return
        val elapsedMs = mark.elapsedNow().inWholeMilliseconds
        if (elapsedMs <= 0) return
        lastMark = mark + elapsedMs.milliseconds
        dispatch(GameEvent.Tick(elapsedMs))
    }

    // endregion

    companion object {
        private val log = Logger.withTag("LevelTestViewModel")

        /** [SavedStateHandle] key of the run snapshot ([SavedGameCodec] JSON of the seed and applied events). */
        const val KEY_RUN: String = "leveltest.saved"
    }
}
