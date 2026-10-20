package dev.talhabayhan.lexi.feature.onboarding.result

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
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe destination of the level test result ("Seviyen: B1", `GAME_RULES.md` §9). [level] is the [Level] name
 * (no enum nav type on iOS, see `GameRoute`); an unknown name falls back to A1.
 */
@Serializable
public data class LevelTestResultRoute(val level: String) {
    internal val placement: Level get() = Level.entries.firstOrNull { it.name == level } ?: Level.A1
}

/**
 * Registers the level test result. [onFinish] after the level was saved and the onboarding completed (go Home);
 * [onBack] returns to the onboarding's level choice (nothing saved).
 */
public fun NavGraphBuilder.levelTestResultScreen(onFinish: () -> Unit, onBack: () -> Unit) {
    composable<LevelTestResultRoute> {
        LevelTestResultRoute(onFinish = onFinish, onBack = onBack)
    }
}

@Composable
internal fun LevelTestResultRoute(
    onFinish: () -> Unit,
    onBack: () -> Unit,
    viewModel: LevelTestResultViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnFinish by rememberUpdatedState(onFinish)
    val currentOnBack by rememberUpdatedState(onBack)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    LevelTestResultEffect.NavigateToHome -> currentOnFinish()
                    LevelTestResultEffect.NavigateBack -> currentOnBack()
                }
            }
        }
    }
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = { viewModel.onIntent(LevelTestResultIntent.Back) },
    )
    LevelTestResultScreen(state = state, onIntent = viewModel::onIntent)
}
