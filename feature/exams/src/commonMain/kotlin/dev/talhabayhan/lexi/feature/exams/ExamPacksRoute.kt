package dev.talhabayhan.lexi.feature.exams

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
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/** Type-safe navigation destination of the exam packs page (`10-exam-packs`). */
@Serializable
data object ExamPacksRoute

/** Registers the exam packs page; [onBack] pops it, [onStartPack] opens a wheel of the pack (the graph owns the route). */
fun NavGraphBuilder.examPacksScreen(onBack: () -> Unit, onStartPack: (ExamPackId) -> Unit) {
    composable<ExamPacksRoute> {
        ExamPacksRoute(onBack = onBack, onStartPack = onStartPack)
    }
}

@Composable
internal fun ExamPacksRoute(
    onBack: () -> Unit,
    onStartPack: (ExamPackId) -> Unit,
    viewModel: ExamPacksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnStartPack by rememberUpdatedState(onStartPack)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ExamPacksEffect.NavigateBack -> currentOnBack()
                    is ExamPacksEffect.StartPack -> currentOnStartPack(effect.pack)
                }
            }
        }
    }
    ExamPacksScreen(state = state, onIntent = viewModel::onIntent)
}
