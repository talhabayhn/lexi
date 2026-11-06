package dev.talhabayhan.lexi.feature.game.duel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.game.EngineEffect
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.MatchEndReason
import dev.talhabayhan.lexi.core.domain.game.MatchEngine
import dev.talhabayhan.lexi.core.domain.game.MatchEvent
import dev.talhabayhan.lexi.core.domain.game.MatchPhase
import dev.talhabayhan.lexi.core.domain.game.MatchState
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.feature.game.GameClockUi
import dev.talhabayhan.lexi.feature.game.GameEffect
import dev.talhabayhan.lexi.feature.game.GameIntent
import dev.talhabayhan.lexi.feature.game.GameLoadError
import dev.talhabayhan.lexi.feature.game.GameUiMapper
import dev.talhabayhan.lexi.feature.game.GameViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Runs a hot-seat duel (`GAME_RULES.md` §8) over the pure [MatchEngine], like [GameViewModel] runs a solo game (ADR
 * 0019): a 100 ms ticker over [timeSource] charges the active player's clock only while their board is shown, playing,
 * unpaused and in the foreground; background ⇒ final tick + Pause (the pause dialog shows on return, like solo). The
 * handover card stops everything until HAZIRIM ([MatchEvent.BeginTurn]). No jokers (`GameConfig.jokerFreeModes`) and
 * nothing is recorded (`srsExcludedModes`): no session, notebook, streak or card history.
 *
 * Process death: the match is not restored. [KEY_ACTIVE] marks a running match in the [SavedStateHandle]; a ViewModel
 * created with it set is a recreated process, so the match is dropped ([DuelEffect.Abandoned] → Home).
 */
internal class DuelViewModel(
    private val route: DuelRoute,
    private val handle: SavedStateHandle,
    private val loader: DuelLoader,
    private val settings: SettingsRepository,
    private val appClock: AppClock,
    private val speaker: Speaker,
    private val timeSource: TimeSource.WithComparableMarks,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    private val mutableState = MutableStateFlow(DuelUiState())
    val state: StateFlow<DuelUiState> = mutableState.asStateFlow()

    private val mutableClock = MutableStateFlow(GameClockUi.Untimed)

    /** The active player's clock (the HUD's active card and the screen-reader time warnings). */
    val clock: StateFlow<GameClockUi> = mutableClock.asStateFlow()

    private val effectChannel = Channel<DuelEffect>(Channel.BUFFERED)
    val effects: Flow<DuelEffect> = effectChannel.receiveAsFlow()

    private var engine: MatchEngine? = null
    private var games: GameEngine? = null
    private var match: MatchState? = null
    private var foreground = false
    private var speakOnCorrect = true
    private var finished = false
    private var quitConfirmation = false
    private var ticker: Job? = null
    private var lastMark: ComparableTimeMark? = null
    private var loading: Job? = null

    /** Who played the turn before the current handover, and whether their clock ended it. */
    private var previousPlayer: Int? = null
    private var previousTimeUp = false

    init {
        if (handle.get<Boolean>(KEY_ACTIVE) == true) {
            // Recreated after a process death: the duel is not restored (GAME_RULES.md §8).
            finished = true
            effectChannel.trySend(DuelEffect.Abandoned)
        } else {
            loading = viewModelScope.launch { load() }
        }
        viewModelScope.launch {
            settings.settings.collect {
                speakOnCorrect = it.speakOnCorrect
                if (speakOnCorrect) speaker.warmUp()
            }
        }
    }

    fun onIntent(intent: DuelIntent) {
        when (intent) {
            DuelIntent.Ready -> beginTurn()
            is DuelIntent.Board -> onBoardIntent(intent.intent)
        }
    }

    private fun onBoardIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.KeyTapped -> input(GameEvent.KeyTapped(intent.index))

            GameIntent.Backspace -> input(GameEvent.Backspace)

            GameIntent.Pass -> input(GameEvent.Pass)

            GameIntent.Pause -> input(GameEvent.Pause)

            GameIntent.Resume -> input(GameEvent.Resume)

            GameIntent.RequestQuit -> if (isPausedBoard() || isHandover()) setQuitConfirmation(true)

            GameIntent.CancelQuit -> setQuitConfirmation(false)

            GameIntent.Forfeit -> if (quitConfirmation) input(GameEvent.Forfeit)

            GameIntent.OpenSettings -> if (isPausedBoard()) effectChannel.trySend(DuelEffect.NavigateToSettings)

            GameIntent.Back -> back()

            GameIntent.Retry -> retry()

            GameIntent.Leave -> if (state.value.loadError != null) effectChannel.trySend(DuelEffect.NavigateBack)

            GameIntent.Background -> {
                foreground = false
                input(GameEvent.Pause)
                speaker.stop()
            }

            GameIntent.Foreground -> {
                foreground = true
                syncTicker()
            }

            // No jokers and no restart in a duel (GAME_RULES.md §7, the pause dialog hides YENİDEN BAŞLAT).
            GameIntent.OpenJokers, GameIntent.CloseJokers, is GameIntent.UseJoker,
            GameIntent.RequestRestart, GameIntent.CancelRestart, GameIntent.Restart,
            -> Unit
        }
    }

    /**
     * System back never leaves a running match silently: a confirmation is closed, a running board pauses (the pause
     * dialog offers PES ET), the handover card asks "Pes etmek istiyor musun?"; on the pause dialog it does nothing.
     */
    private fun back() {
        val current = match ?: return
        val board = current.players[current.activePlayer]
        when {
            finished -> Unit
            quitConfirmation -> setQuitConfirmation(false)
            current.phase == MatchPhase.HANDOVER -> setQuitConfirmation(true)
            board.status == GameStatus.PLAYING && !board.isPaused -> input(GameEvent.Pause)
            else -> Unit
        }
    }

    override fun onCleared() {
        if (!finished) speaker.stop()
    }

    // region loading

    private suspend fun load() {
        val opened = try {
            loader.open(route.duelLevel, route.clockSeconds)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.e(e) { "duel not loaded" }
            crashReporter.recordException(e, "duel not loaded")
            mutableState.value = DuelUiState(isLoading = false, loadError = GameLoadError.Unreadable)
            return
        }
        if (opened == null) {
            mutableState.value = DuelUiState(isLoading = false, loadError = GameLoadError.NoCards)
            return
        }
        val (matchEngine, initial) = opened
        engine = matchEngine
        games = GameEngine(matchEngine.config)
        match = initial
        handle[KEY_ACTIVE] = true
        analytics.track(AnalyticsEvent.DuelStart(matchEngine.config.duelClockSeconds))
        publish()
    }

    private fun retry() {
        if (state.value.loadError != GameLoadError.Unreadable || loading?.isActive == true) return
        mutableState.value = DuelUiState()
        loading = viewModelScope.launch { load() }
    }

    // endregion

    private fun isHandover(): Boolean = !finished && match?.phase == MatchPhase.HANDOVER

    private fun isPausedBoard(): Boolean {
        val current = match ?: return false
        val board = current.players[current.activePlayer]
        return !finished && current.phase == MatchPhase.PLAYING && board.status == GameStatus.PLAYING && board.isPaused
    }

    private fun setQuitConfirmation(value: Boolean) {
        if (quitConfirmation == value) return
        quitConfirmation = value
        publish()
    }

    /** HAZIRIM: only in the foreground (the clock starts at once) and never twice. */
    private fun beginTurn() {
        if (!foreground || !isHandover() || quitConfirmation) return
        apply(MatchEvent.BeginTurn(appClock.nowMs()))
    }

    /** Player and lifecycle events go to the active player's game; the time up to now is charged first. */
    private fun input(event: GameEvent) {
        flushTick()
        apply(MatchEvent.Play(event))
    }

    private fun apply(event: MatchEvent) {
        val matchEngine = engine ?: return
        val before = match ?: return
        if (finished) return
        val transition = matchEngine.reduce(before, event)
        // MatchEngine returns the very same state for an ignored event.
        if (transition.state === before && transition.effects.isEmpty()) return
        val after = transition.state
        match = after
        // The confirmation belongs to the pause dialog or the handover card; it ends with them.
        if (!isPausedBoard() && !isHandover()) quitConfirmation = false
        val playedBefore = before.players[before.activePlayer]
        val playedAfter = after.players[before.activePlayer]
        var matchOver: EngineEffect.MatchOver? = null
        transition.effects.forEach { effect ->
            when (effect) {
                is EngineEffect.TurnChanged -> {
                    previousPlayer = before.activePlayer
                    previousTimeUp = playedAfter.summary?.reason == GameOverReason.TIME_UP
                    quitConfirmation = false
                }

                is EngineEffect.MatchOver -> matchOver = effect

                else -> onEngineEffect(effect, playedAfter, playedBefore)
            }
        }
        publish()
        syncTicker()
        matchOver?.let { onMatchOver(after) }
    }

    /** Board motion like a solo game; [state] / [before] are the player's game after / before the event. */
    private fun onEngineEffect(effect: EngineEffect, state: GameState, before: GameState) {
        val motion: GameEffect? = when (effect) {
            is EngineEffect.LetterPlaced -> {
                val slot = before.currentIndex ?: state.currentIndex
                val keyIndex = slot?.let(state.inputs::get)?.cells?.getOrNull(effect.cellIndex)?.keyIndex
                keyIndex?.let { GameEffect.LetterPlaced(effect.cellIndex, it, effect.letter) }
            }

            is EngineEffect.Correct -> {
                val answer = state.wheel.slots.firstOrNull { it.card?.id == effect.card }?.card?.answer
                val spoken = speakOnCorrect && answer != null && speaker.isReady
                if (spoken) speaker.speak(checkNotNull(answer).lowercase())
                GameEffect.Correct(effect.letter, effect.points, spoken)
            }

            is EngineEffect.Wrong -> GameEffect.Wrong(effect.letter)

            is EngineEffect.Passed -> GameEffect.Passed(effect.letter)

            is EngineEffect.Advanced -> GameEffect.Advanced(effect.toLetter)

            is EngineEffect.ComboReached -> GameEffect.ComboReached(effect.streak)

            is EngineEffect.TimeWarning -> GameEffect.TimeWarning(effect.secondsLeft)

            is EngineEffect.GameOver -> if (effect.summary.reason == GameOverReason.TIME_UP) GameEffect.TimeUp else null

            is EngineEffect.JokerApplied, is EngineEffect.TurnChanged, is EngineEffect.MatchOver -> null
        }
        motion?.let { effectChannel.trySend(DuelEffect.Motion(it)) }
    }

    private fun publish() {
        val current = match ?: return
        val gameEngine = checkNotNull(games)
        val activeGame = current.players[current.activePlayer]
        val previousBoard = mutableState.value.board.takeIf { mutableState.value.activePlayer == current.activePlayer }
        val board = GameUiMapper.map(activeGame, gameEngine, previousBoard).copy(
            isQuitConfirmation = quitConfirmation && current.phase == MatchPhase.PLAYING,
            canRestart = false,
        )
        mutableState.value = DuelUiState(
            isLoading = false,
            phase = current.phase,
            board = board,
            players = current.players.mapIndexed { index, game ->
                DuelPlayerUi(
                    name = route.names[index],
                    correct = game.wheel.slots.count { it.state == SlotState.CORRECT },
                    remainingMs = game.remainingMs ?: timeLimitMs(),
                )
            }.toImmutableList(),
            activePlayer = current.activePlayer,
            handover = if (current.phase == MatchPhase.HANDOVER) handoverOf(current) else null,
        )
        mutableClock.value = GameUiMapper.clock(
            state = activeGame,
            timeLimitMs = gameEngine.config.timeLimitMs(activeGame.mode),
            warningSeconds = gameEngine.config.timeWarningSeconds,
        )
    }

    private fun handoverOf(current: MatchState): DuelHandoverUi {
        val next = current.players[current.activePlayer]
        val previous = previousPlayer?.let { index ->
            val game = current.players[index]
            DuelPreviousUi(
                player = index,
                correct = game.wheel.slots.count { it.state == SlotState.CORRECT },
                wrong = game.wheel.slots.count { it.state == SlotState.WRONG },
            )
        }
        return DuelHandoverUi(
            player = current.activePlayer,
            remainingMs = next.remainingMs ?: timeLimitMs(),
            lettersLeft = next.wheel.slots.count {
                it.state == SlotState.TODO || it.state == SlotState.CURRENT || it.state == SlotState.PASSED
            },
            previous = previous,
            previousTimeUp = previous != null && previousTimeUp,
            isQuitConfirmation = quitConfirmation,
        )
    }

    /** The clock per player; a player's clock before their first turn. */
    private fun timeLimitMs(): Long = checkNotNull(engine).config.duelClockSeconds * MS_PER_SECOND

    /** Sends `duel_finish` (no names) and leaves for the result; nothing is recorded. */
    private fun onMatchOver(final: MatchState) {
        if (finished) return
        finished = true
        quitConfirmation = false
        syncTicker()
        handle.remove<Boolean>(KEY_ACTIVE)
        val result = checkNotNull(final.result)
        val outcome = DuelOutcomes.of(final, route, timeLimitMs())
        analytics.track(
            AnalyticsEvent.DuelFinish(
                when {
                    result.reason == MatchEndReason.FORFEIT -> AnalyticsEvent.DuelResultType.FORFEIT
                    result.winner == null -> AnalyticsEvent.DuelResultType.DRAW
                    else -> AnalyticsEvent.DuelResultType.WIN
                },
            ),
        )
        effectChannel.trySend(DuelEffect.NavigateToResult(DuelResultRoute.of(outcome)))
    }

    // region ticker (ADR 0019 §1)

    private fun syncTicker() {
        val current = match
        val active = current?.players?.get(current.activePlayer)
        val run = !finished && foreground && current?.phase == MatchPhase.PLAYING &&
            active?.status == GameStatus.PLAYING && !active.isPaused
        if (run && ticker == null) {
            lastMark = timeSource.markNow()
            ticker = viewModelScope.launch {
                while (true) {
                    delay(GameViewModel.TICK_INTERVAL)
                    flushTick()
                }
            }
        } else if (!run && ticker != null) {
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
        apply(MatchEvent.Play(GameEvent.Tick(elapsedMs)))
    }

    // endregion

    companion object {
        /** [SavedStateHandle] flag of a loaded, unfinished match (see the class doc: process death drops it). */
        const val KEY_ACTIVE: String = "duel.active"

        private const val MS_PER_SECOND = 1_000L

        private val log = Logger.withTag("DuelViewModel")
    }
}
