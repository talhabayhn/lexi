package dev.talhabayhan.lexi.feature.stats

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

/** Type-safe navigation destination of the statistics. */
@Serializable
data object StatsRoute

/** Registers the statistics screen. */
fun NavGraphBuilder.statsScreen(onBack: () -> Unit) {
    composable<StatsRoute> {
        StatsRoute(onBack = onBack)
    }
}

@Composable
internal fun StatsRoute(onBack: () -> Unit, viewModel: StatsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    StatsEffect.NavigateBack -> currentOnBack()
                }
            }
        }
    }
    StatsScreen(state = state, onIntent = viewModel::onIntent)
}
