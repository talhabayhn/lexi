package dev.talhabayhan.lexi.feature.settings.reminder

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

/** Type-safe navigation destination of the reminder pre-prompt (from Home, proactively, and from the settings). */
@Serializable
data object ReminderPrePromptRoute

/** Registers the pre-prompt; [onDone] runs after either answer (the graph pops back). */
fun NavGraphBuilder.reminderPrePromptScreen(onDone: () -> Unit) {
    composable<ReminderPrePromptRoute> { ReminderPrePromptRoute(onDone = onDone) }
}

@Composable
internal fun ReminderPrePromptRoute(onDone: () -> Unit, viewModel: ReminderPrePromptViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnDone by rememberUpdatedState(onDone)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    ReminderPrePromptEffect.Done -> currentOnDone()
                }
            }
        }
    }
    ReminderPrePromptScreen(state = state, onIntent = viewModel::onIntent)
}
