package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.components.rememberGameMotion
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe destination of the game screen. Arguments are enum *names*: navigation 2.10 has no enum nav type on iOS
 * (`parseEnum()` is unsupported outside Android), so strings keep the route identical on both platforms.
 */
@Serializable
data class GameRoute(val level: String, val mode: String, val category: String? = null) {
    constructor(level: Level, mode: GameMode, category: CategoryId? = null) :
        this(level = level.name, mode = mode.name, category = category?.value)

    internal val gameLevel: Level get() = Level.valueOf(level)

    internal val gameMode: GameMode get() = GameMode.valueOf(mode)

    internal val categoryId: CategoryId? get() = category?.let(::CategoryId)
}

/**
 * Registers the game screen. [onFinished] runs after the finished game is recorded (the result screen comes later in
 * M4), [onAbandoned] when a restored game can no longer be played.
 */
fun NavGraphBuilder.gameScreen(onFinished: () -> Unit, onAbandoned: () -> Unit) {
    composable<GameRoute> {
        GameRoute(onFinished = onFinished, onAbandoned = onAbandoned)
    }
}

@Composable
internal fun GameRoute(onFinished: () -> Unit, onAbandoned: () -> Unit, viewModel: GameViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Read only inside lambdas (draw phase / derived state), so ticks never recompose the route.
    val clock = viewModel.clock.collectAsStateWithLifecycle()
    val motion = rememberGameMotion(LexiTheme.motion)
    val currentOnFinished by rememberUpdatedState(onFinished)
    val currentOnAbandoned by rememberUpdatedState(onAbandoned)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner, motion) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    GameEffect.NavigateToResult -> currentOnFinished()
                    GameEffect.Abandoned -> currentOnAbandoned()
                    else -> launch { motion.play(effect) }
                }
            }
        }
    }
    GameScreen(state = state, clock = { clock.value }, onIntent = viewModel::onIntent, motion = motion)
}
