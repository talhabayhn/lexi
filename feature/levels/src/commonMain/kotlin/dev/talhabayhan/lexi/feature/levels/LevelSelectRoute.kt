package dev.talhabayhan.lexi.feature.levels

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
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe navigation destination for the level and mode select. [mode] is the mode that starts selected (an enum
 * *name*, like `CategoriesRoute`): Home's OYNA opens it on Classic, the practice tile on Practice.
 */
@Serializable
data class LevelSelectRoute(val mode: String = GameMode.CLASSIC.name) {
    constructor(mode: GameMode) : this(mode = mode.name)

    internal val initialMode: GameMode get() = GameMode.entries.firstOrNull { it.name == mode } ?: GameMode.CLASSIC
}

/** [onStart] opens a game of the chosen level and mode (the graph in `shared` owns the game route). */
fun NavGraphBuilder.levelSelectScreen(
    onBack: () -> Unit,
    onStart: (Level, GameMode) -> Unit,
    onOpenCategories: (Level, GameMode) -> Unit,
) {
    composable<LevelSelectRoute> {
        LevelSelectRoute(onBack = onBack, onStart = onStart, onOpenCategories = onOpenCategories)
    }
}

@Composable
internal fun LevelSelectRoute(
    onBack: () -> Unit,
    onStart: (Level, GameMode) -> Unit,
    onOpenCategories: (Level, GameMode) -> Unit,
    viewModel: LevelSelectViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnOpenCategories by rememberUpdatedState(onOpenCategories)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    LevelSelectEffect.NavigateBack -> currentOnBack()
                    is LevelSelectEffect.StartGame -> currentOnStart(effect.level, effect.mode)
                    is LevelSelectEffect.OpenCategories -> currentOnOpenCategories(effect.level, effect.mode)
                }
            }
        }
    }
    LevelSelectScreen(state = state, onIntent = viewModel::onIntent)
}
