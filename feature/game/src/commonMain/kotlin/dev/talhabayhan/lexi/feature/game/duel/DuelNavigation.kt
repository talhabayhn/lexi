package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.MatchPhase
import dev.talhabayhan.lexi.feature.game.GameBackHandler
import dev.talhabayhan.lexi.feature.game.GameClockUi
import dev.talhabayhan.lexi.feature.game.GameIntent
import dev.talhabayhan.lexi.feature.game.GameScreen
import dev.talhabayhan.lexi.feature.game.GameUiState
import dev.talhabayhan.lexi.feature.game.components.DuelHudUi
import dev.talhabayhan.lexi.feature.game.components.rememberGameMotion
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * The hot-seat duel's destinations (`GAME_RULES.md` §8, `screen-map.md` 17–20): setup → duel (handover card and
 * board) → result → a player's answer review. [onStartDuel] opens a duel from the setup; [onFinished] shows the result (pops everything above
 * Home); [onAbandoned] returns Home when a match was dropped (process death); [onBack] leaves the setup or a duel that
 * could not be loaded; [onOpenSettings] is the pause dialog's "Ayarlar"; [onPlayAgain] starts the same setup again
 * from the result; [onHome] is "Ana menüye dön"; [onOpenReview] opens a player's answers from the result (its back is
 * [onBack], to the result).
 */
fun NavGraphBuilder.duelScreens(
    onStartDuel: (DuelRoute) -> Unit,
    onFinished: (DuelResultRoute) -> Unit,
    onAbandoned: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onPlayAgain: (DuelRoute) -> Unit,
    onHome: () -> Unit,
    onOpenReview: (DuelReviewRoute) -> Unit,
) {
    composable<DuelSetupRoute> { DuelSetupRoute(onStartDuel = onStartDuel, onBack = onBack) }
    composable<DuelRoute> {
        DuelRoute(onFinished = onFinished, onAbandoned = onAbandoned, onBack = onBack, onOpenSettings = onOpenSettings)
    }
    composable<DuelResultRoute> { entry ->
        val outcome = remember(entry) { entry.toRoute<DuelResultRoute>().outcome }
        DuelResultScreen(
            outcome = outcome,
            onPlayAgain = {
                onPlayAgain(DuelRoute(outcome.level, outcome.clockSeconds, outcome.players.map { it.name }))
            },
            onHome = onHome,
            onOpenReview = { player -> onOpenReview(DuelReviewRoute.of(outcome, player)) },
        )
    }
    composable<DuelReviewRoute> { DuelReviewRoute(onBack = onBack) }
}

@Composable
internal fun DuelReviewRoute(onBack: () -> Unit, viewModel: DuelReviewViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DuelReviewScreen(
        state = state,
        onIntent = { intent -> if (intent == DuelReviewIntent.Back) onBack() else viewModel.onIntent(intent) },
    )
}

@Composable
internal fun DuelSetupRoute(
    onStartDuel: (DuelRoute) -> Unit,
    onBack: () -> Unit,
    viewModel: DuelSetupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnStartDuel by rememberUpdatedState(onStartDuel)
    val currentOnBack by rememberUpdatedState(onBack)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DuelSetupEffect.StartDuel -> currentOnStartDuel(effect.route)
                DuelSetupEffect.NavigateBack -> currentOnBack()
            }
        }
    }
    // System back closes the name dialog first; otherwise the navigation's own back leaves the setup.
    GameBackHandler(enabled = state.editingPlayer != null, onBack = { viewModel.onIntent(DuelSetupIntent.Back) })
    DuelSetupScreen(state = state, onIntent = viewModel::onIntent)
}

/**
 * The running match: the handover card (`18-duel-handover`) while it is shown, else the game board with the duel HUD
 * (`19-duel-game`), cross-faded (`motion.md` #12 reduced crossfade; also with full motion, so the next player's board
 * never slides into view before HAZIRIM). Lifecycle ON_PAUSE / ON_RESUME go to the ViewModel on both (ADR 0019). System
 * back is always taken once the match is loaded: it never leaves a match silently (`DuelViewModel`).
 */
@Composable
internal fun DuelRoute(
    onFinished: (DuelResultRoute) -> Unit,
    onAbandoned: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DuelViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clock = viewModel.clock.collectAsStateWithLifecycle()
    val motion = rememberGameMotion(LexiTheme.motion)
    val currentOnFinished by rememberUpdatedState(onFinished)
    val currentOnAbandoned by rememberUpdatedState(onAbandoned)
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner, motion) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is DuelEffect.Motion -> launch { motion.play(effect.effect) }
                    is DuelEffect.NavigateToResult -> currentOnFinished(effect.result)
                    DuelEffect.Abandoned -> currentOnAbandoned()
                    DuelEffect.NavigateBack -> currentOnBack()
                    DuelEffect.NavigateToSettings -> currentOnOpenSettings()
                }
            }
        }
    }
    val onBoardIntent = remember(viewModel) { { intent: GameIntent -> viewModel.onIntent(DuelIntent.Board(intent)) } }
    // Also while the handover card is shown. On the board GameScreen sends the same events: a second Pause is ignored
    // by the engine and Foreground is idempotent, so the duplicates are safe.
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { onBoardIntent(GameIntent.Background) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { onBoardIntent(GameIntent.Foreground) }
    GameBackHandler(
        enabled = !state.isLoading && state.loadError == null,
        onBack = { onBoardIntent(GameIntent.Back) },
    )
    val readClock = remember(clock) { { clock.value } }
    val handover = state.handover
    val content = if (state.phase == MatchPhase.HANDOVER && handover != null) {
        DuelContent.Handover(handover, state.players.map { it.name })
    } else {
        val board = state.loadError?.let { GameUiState(isLoading = false, loadError = it) } ?: state.board
        DuelContent.Board(board, if (state.players.isEmpty()) null else DuelHudUi(state.players, state.activePlayer))
    }
    // Each side keeps the state it was shown with while it fades out, so the next player's board is never seen
    // before HAZIRIM.
    val fade = tween<Float>(LexiTheme.motion.screenTransitionReducedCrossfadeMillis)
    AnimatedContent(
        targetState = content,
        transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) },
        contentKey = { it is DuelContent.Handover },
        label = "duel-handover",
    ) { shown ->
        when (shown) {
            is DuelContent.Handover -> DuelHandoverCard(
                handover = shown.handover,
                names = shown.names,
                onIntent = viewModel::onIntent,
            )

            is DuelContent.Board -> GameScreen(
                state = shown.board,
                clock = rememberSlotClock(readClock, exiting = transition.targetState == EnterExitState.PostExit),
                onIntent = onBoardIntent,
                motion = motion,
                duelHud = shown.hud,
            )
        }
    }
}

/**
 * The board's clock while it is shown; once it fades out (turn change) the last value it read, so the old player's
 * card never shows the next player's time. A plain holder, not state: it is written while the clock is read.
 */
@Composable
private fun rememberSlotClock(clock: () -> GameClockUi, exiting: Boolean): () -> GameClockUi {
    val last = remember { arrayOf(GameClockUi.Untimed) }
    return remember(clock, exiting) {
        if (exiting) {
            val frozen = last[0]
            { frozen }
        } else {
            { clock().also { last[0] = it } }
        }
    }
}

/** What the duel destination shows: the handover card or the board. */
private sealed interface DuelContent {
    data class Handover(val handover: DuelHandoverUi, val names: List<String>) : DuelContent

    data class Board(val board: GameUiState, val hud: DuelHudUi?) : DuelContent
}
