package dev.talhabayhan.lexi.feature.settings

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

/** Type-safe navigation destination of the settings. */
@Serializable
data object SettingsRoute

/** Registers the settings screen. */
fun NavGraphBuilder.settingsScreen(onBack: () -> Unit) {
    composable<SettingsRoute> {
        SettingsRoute(onBack = onBack)
    }
}

@Composable
internal fun SettingsRoute(onBack: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    SettingsEffect.NavigateBack -> currentOnBack()
                }
            }
        }
    }
    SettingsScreen(state = state, onIntent = viewModel::onIntent)
}
