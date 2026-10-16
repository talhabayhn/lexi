package dev.talhabayhan.lexi.feature.notebook

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
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/** Type-safe navigation destination of the notebook. */
@Serializable
data object NotebookRoute

/**
 * Registers the notebook. [onStartReview] opens the review wheel over the due words (the graph in `shared` owns the
 * game route).
 */
fun NavGraphBuilder.notebookScreen(onBack: () -> Unit, onStartReview: () -> Unit) {
    composable<NotebookRoute> {
        NotebookRoute(onBack = onBack, onStartReview = onStartReview)
    }
}

@Composable
internal fun NotebookRoute(
    onBack: () -> Unit,
    onStartReview: () -> Unit,
    viewModel: NotebookViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnStartReview by rememberUpdatedState(onStartReview)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    NotebookEffect.NavigateBack -> currentOnBack()
                    NotebookEffect.StartReview -> currentOnStartReview()
                }
            }
        }
    }
    // System back closes the word sheet first; without a sheet it leaves the screen as usual.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.detail != null,
        onBackCompleted = { viewModel.onIntent(NotebookIntent.CloseWord) },
    )
    NotebookScreen(state = state, onIntent = viewModel::onIntent)
}
