package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.components.rememberGameMotion
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe destination of the game screen. Arguments are enum *names*: navigation 2.10 has no enum nav type on iOS
 * (`parseEnum()` is unsupported outside Android), so strings keep the route identical on both platforms.
 */
@Serializable
data class GameRoute(val level: String, val mode: String, val category: String? = null, val pack: String? = null) {
    constructor(level: Level, mode: GameMode, category: CategoryId? = null, pack: ExamPackId? = null) :
        this(level = level.name, mode = mode.name, category = category?.value, pack = pack?.value)

    internal val gameLevel: Level get() = Level.valueOf(level)

    internal val gameMode: GameMode get() = GameMode.valueOf(mode)

    internal val categoryId: CategoryId? get() = category?.let(::CategoryId)

    internal val packId: ExamPackId? get() = pack?.let(::ExamPackId)

    companion object {
        /**
         * The review wheel over the due notebook cards of every level (`GAME_RULES.md` §10); [level] is only a
         * placeholder, the game takes the level of the wheel's first card.
         */
        fun review(): GameRoute = GameRoute(Level.A1, GameMode.REVIEW)

        /**
         * The shared mixed-level daily wheel (`GAME_RULES.md` §5); [level] is only a placeholder,
         * the game takes the level of the wheel's first card.
         */
        fun daily(): GameRoute = GameRoute(Level.A1, GameMode.DAILY)

        /**
         * An exam pack wheel (`GAME_RULES.md` §1, §5): the pack's cards of any level, played with Classic rules; [level]
         * is only a placeholder, the game takes the level of the wheel's first card.
         */
        fun examPack(pack: ExamPackId): GameRoute = GameRoute(Level.A1, GameMode.CLASSIC, pack = pack)
    }
}

/**
 * Registers the game screen. [onFinished] runs after the finished game is recorded, with the result to show;
 * [onAbandoned] when a restored game can no longer be played; [onBack] when the player leaves a game that could not
 * be loaded ("Geri dön"); [onOpenSettings] for "Ayarlar" of the pause dialog (the game stays paused underneath).
 */
fun NavGraphBuilder.gameScreen(
    onFinished: (ResultRoute) -> Unit,
    onAbandoned: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    composable<GameRoute> {
        GameRoute(onFinished = onFinished, onAbandoned = onAbandoned, onBack = onBack, onOpenSettings = onOpenSettings)
    }
}

@Composable
internal fun GameRoute(
    onFinished: (ResultRoute) -> Unit,
    onAbandoned: () -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: GameViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Read only inside lambdas (draw phase / derived state), so ticks never recompose the route.
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
                    is GameEffect.NavigateToResult -> currentOnFinished(effect.result)
                    GameEffect.Abandoned -> currentOnAbandoned()
                    GameEffect.NavigateBack -> currentOnBack()
                    GameEffect.NavigateToSettings -> currentOnOpenSettings()
                    else -> launch { motion.play(effect) }
                }
            }
        }
    }
    // System back on both platforms: sheet → closed, running game → paused (GameIntent.Back). While loading, before
    // the start or after a load error, back leaves the screen as usual.
    GameBackHandler(
        enabled = !state.isLoading && !state.isStarting && state.loadError == null,
        onBack = { viewModel.onIntent(GameIntent.Back) },
    )
    val readClock = remember(clock) { { clock.value } }
    GameScreen(state = state, clock = readClock, onIntent = viewModel::onIntent, motion = motion)
}

/**
 * System back (Android back button / predictive back, iOS swipe-back) while the game is shown, through the
 * navigation-event handler (`navigationevent-compose`, declared explicitly in the catalog).
 */
@Composable
internal fun GameBackHandler(enabled: Boolean, onBack: () -> Unit) {
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = enabled,
        onBackCompleted = onBack,
    )
}
