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

/**
 * [onPlay] opens the level and mode select, [onOpenNotebook] the notebook, [onOpenStats] the statistics,
 * [onOpenSettings] the settings (the graph in `shared` decides the routes).
 */
fun NavGraphBuilder.homeScreen(
    onPlay: () -> Unit,
    onOpenNotebook: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    composable<HomeRoute> {
        HomeRoute(
            onPlay = onPlay,
            onOpenNotebook = onOpenNotebook,
            onOpenStats = onOpenStats,
            onOpenSettings = onOpenSettings,
        )
    }
}

@Composable
internal fun HomeRoute(
    onPlay: () -> Unit,
    onOpenNotebook: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnPlay by rememberUpdatedState(onPlay)
    val currentOnOpenNotebook by rememberUpdatedState(onOpenNotebook)
    val currentOnOpenStats by rememberUpdatedState(onOpenStats)
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    HomeEffect.NavigateToLevelSelect -> currentOnPlay()
                    HomeEffect.NavigateToNotebook -> currentOnOpenNotebook()
                    HomeEffect.NavigateToStats -> currentOnOpenStats()
                    HomeEffect.NavigateToSettings -> currentOnOpenSettings()
                }
            }
        }
    }
    // "Today" (daily card, streak, due reviews) is read again whenever the home comes back to the foreground.
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) { viewModel.onIntent(HomeIntent.Resumed) }
    }
    HomeScreen(state = state, onIntent = viewModel::onIntent)
}
