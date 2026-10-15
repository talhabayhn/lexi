package dev.talhabayhan.lexi.feature.home

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
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/** Type-safe navigation destination for the home screen. */
@Serializable
data object HomeRoute

/** [onStartGame]: temporary entry to an A1 classic game (the graph in `shared` decides the route). */
fun NavGraphBuilder.homeScreen(onStartGame: () -> Unit) {
    composable<HomeRoute> {
        HomeRoute(onStartGame = onStartGame)
    }
}

@Composable
internal fun HomeRoute(onStartGame: () -> Unit, viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnStartGame by rememberUpdatedState(onStartGame)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    HomeEffect.NavigateToGame -> currentOnStartGame()
                }
            }
        }
    }
    HomeScreen(state = state, onIntent = viewModel::onIntent)
}
