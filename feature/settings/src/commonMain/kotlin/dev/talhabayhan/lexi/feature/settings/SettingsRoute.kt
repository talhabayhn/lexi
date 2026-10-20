package dev.talhabayhan.lexi.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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
fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
    onOpenReminderPrePrompt: () -> Unit = {},
) {
    composable<SettingsRoute> {
        SettingsRoute(
            onBack = onBack,
            onOpenAbout = onOpenAbout,
            onOpenUrl = onOpenUrl,
            onOpenReminderPrePrompt = onOpenReminderPrePrompt,
        )
    }
}

@Composable
internal fun SettingsRoute(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenReminderPrePrompt: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnOpenAbout by rememberUpdatedState(onOpenAbout)
    val currentOnOpenUrl by rememberUpdatedState(onOpenUrl)
    val currentOnOpenReminderPrePrompt by rememberUpdatedState(onOpenReminderPrePrompt)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    SettingsEffect.NavigateBack -> currentOnBack()
                    SettingsEffect.NavigateToAbout -> currentOnOpenAbout()
                    is SettingsEffect.OpenUrl -> currentOnOpenUrl(effect.url)
                    SettingsEffect.NavigateToReminderPrePrompt -> currentOnOpenReminderPrePrompt()
                }
            }
        }
    }
    // Back from the system settings (or the OS dialog): the notification permission may have changed.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onIntent(SettingsIntent.RefreshNotificationPermission) }
    SettingsScreen(state = state, onIntent = viewModel::onIntent)
}
