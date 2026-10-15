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
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe destination of the categories screen. Arguments are enum *names* (navigation 2.10 has no enum nav type on
 * iOS, like `GameRoute`); [mode] is the mode picked on the level select, kept for the game started from here.
 */
@Serializable
data class CategoriesRoute(val level: String, val mode: String = GameMode.CLASSIC.name) {
    constructor(level: Level, mode: GameMode) : this(level = level.name, mode = mode.name)

    internal val startLevel: Level get() = Level.entries.firstOrNull { it.name == level } ?: Level.A1

    internal val startMode: GameMode get() = GameMode.entries.firstOrNull { it.name == mode } ?: GameMode.CLASSIC
}

/**
 * Registers the categories screen. [onStart] opens a game of the level, mode and category (null = mixed wheel); the
 * graph in `shared` owns the game route.
 */
fun NavGraphBuilder.categoriesScreen(onBack: () -> Unit, onStart: (Level, GameMode, CategoryId?) -> Unit) {
    composable<CategoriesRoute> {
        CategoriesRoute(onBack = onBack, onStart = onStart)
    }
}

@Composable
internal fun CategoriesRoute(
    onBack: () -> Unit,
    onStart: (Level, GameMode, CategoryId?) -> Unit,
    viewModel: CategoriesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnStart by rememberUpdatedState(onStart)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    CategoriesEffect.NavigateBack -> currentOnBack()
                    is CategoriesEffect.StartGame -> currentOnStart(effect.level, effect.mode, effect.category)
                }
            }
        }
    }
    CategoriesScreen(state = state, onIntent = viewModel::onIntent)
}
