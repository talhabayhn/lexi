package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/** Type-safe navigation destination for the home screen. */
@Serializable
data object HomeRoute

/**
 * [onPlay] opens the level and mode select, [onOpenDaily] the daily wheel, [onOpenNotebook] the notebook, [onOpenStats] the statistics,
 * [onOpenSettings] the settings, [onOpenPractice] the level select on Practice, [onOpenExamPacks] the exam packs page (the graph in `shared` decides the routes).
 */
fun NavGraphBuilder.homeScreen(
    onPlay: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenNotebook: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenExamPacks: () -> Unit,
    onOpenReminderPrePrompt: () -> Unit = {},
) {
    composable<HomeRoute> {
        HomeRoute(
            onPlay = onPlay,
            onOpenDaily = onOpenDaily,
            onOpenNotebook = onOpenNotebook,
            onOpenStats = onOpenStats,
            onOpenSettings = onOpenSettings,
            onOpenPractice = onOpenPractice,
            onOpenExamPacks = onOpenExamPacks,
            onOpenReminderPrePrompt = onOpenReminderPrePrompt,
        )
    }
}

@Composable
internal fun HomeRoute(
    onPlay: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenNotebook: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPractice: () -> Unit,
    onOpenExamPacks: () -> Unit,
    onOpenReminderPrePrompt: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnPlay by rememberUpdatedState(onPlay)
    val currentOnOpenDaily by rememberUpdatedState(onOpenDaily)
    val currentOnOpenNotebook by rememberUpdatedState(onOpenNotebook)
    val currentOnOpenStats by rememberUpdatedState(onOpenStats)
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)
    val currentOnOpenPractice by rememberUpdatedState(onOpenPractice)
    val currentOnOpenExamPacks by rememberUpdatedState(onOpenExamPacks)
    val currentOnOpenReminderPrePrompt by rememberUpdatedState(onOpenReminderPrePrompt)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    HomeEffect.NavigateToLevelSelect -> currentOnPlay()
                    HomeEffect.NavigateToDaily -> currentOnOpenDaily()
                    HomeEffect.NavigateToNotebook -> currentOnOpenNotebook()
                    HomeEffect.NavigateToStats -> currentOnOpenStats()
                    HomeEffect.NavigateToSettings -> currentOnOpenSettings()
                    HomeEffect.NavigateToPractice -> currentOnOpenPractice()
                    HomeEffect.NavigateToExamPacks -> currentOnOpenExamPacks()
                    HomeEffect.NavigateToReminderPrePrompt -> currentOnOpenReminderPrePrompt()
                }
            }
        }
    }
    // Periodic refresh (daily card state, streak badge, countdown) on resume and once per minute while STARTED.
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.onIntent(HomeIntent.Entered)
            viewModel.onIntent(HomeIntent.Refresh)
            while (true) {
                kotlinx.coroutines.delay(60_000L)
                viewModel.onIntent(HomeIntent.Refresh)
            }
        }
    }
    // A gift granted while Home is still entering (onboarding → Home) waits in the state and shows once resumed.
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()
    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        noticeVisible = lifecycleState.isAtLeast(Lifecycle.State.RESUMED),
    )
}
