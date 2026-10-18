package dev.talhabayhan.lexi.feature.settings.about

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

/** Type-safe navigation destination of the About and licenses screen. */
@Serializable
data object AboutRoute

/** Registers the About and licenses screen. */
fun NavGraphBuilder.aboutScreen(onBack: () -> Unit, onOpenUrl: (String) -> Unit) {
    composable<AboutRoute> {
        AboutRoute(onBack = onBack, onOpenUrl = onOpenUrl)
    }
}

@Composable
internal fun AboutRoute(onBack: () -> Unit, onOpenUrl: (String) -> Unit, viewModel: AboutViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnOpenUrl by rememberUpdatedState(onOpenUrl)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    AboutEffect.NavigateBack -> currentOnBack()
                    is AboutEffect.OpenUrl -> currentOnOpenUrl(effect.url)
                }
            }
        }
    }
    AboutScreen(state = state, onIntent = viewModel::onIntent)
}
