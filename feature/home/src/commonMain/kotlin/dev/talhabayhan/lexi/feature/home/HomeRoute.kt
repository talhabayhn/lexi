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
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/** Type-safe navigation destination for the home screen. */
@Serializable
data object HomeRoute

/**
 * [onStartGame] starts a game of the level, mode and category chosen on Home (null = the level's mixed wheel),
 * [onOpenDaily] the daily wheel, [onOpenNotebook] the notebook, [onOpenStats] the statistics, [onOpenSettings] the
 * settings, [onOpenExamPacks] the exam packs page, [onOpenDuel] the hot-seat duel setup (the graph in `shared` decides
 * the routes).
 */
fun NavGraphBuilder.homeScreen(
    onStartGame: (Level, GameMode, CategoryId?) -> Unit,
    onOpenDaily: () -> Unit,
    onOpenNotebook: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenExamPacks: () -> Unit,
    onOpenDuel: () -> Unit,
    onOpenReminderPrePrompt: () -> Unit = {},
) {
    composable<HomeRoute> {
        HomeRoute(
            onStartGame = onStartGame,
            onOpenDaily = onOpenDaily,
            onOpenNotebook = onOpenNotebook,
            onOpenStats = onOpenStats,
            onOpenSettings = onOpenSettings,
            onOpenExamPacks = onOpenExamPacks,
            onOpenDuel = onOpenDuel,
            onOpenReminderPrePrompt = onOpenReminderPrePrompt,
        )
    }
}

@Composable
internal fun HomeRoute(
    onStartGame: (Level, GameMode, CategoryId?) -> Unit,
    onOpenDaily: () -> Unit,
    onOpenNotebook: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenExamPacks: () -> Unit,
    onOpenDuel: () -> Unit,
    onOpenReminderPrePrompt: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnStartGame by rememberUpdatedState(onStartGame)
    val currentOnOpenDaily by rememberUpdatedState(onOpenDaily)
    val currentOnOpenNotebook by rememberUpdatedState(onOpenNotebook)
    val currentOnOpenStats by rememberUpdatedState(onOpenStats)
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)
    val currentOnOpenExamPacks by rememberUpdatedState(onOpenExamPacks)
    val currentOnOpenDuel by rememberUpdatedState(onOpenDuel)
    val currentOnOpenReminderPrePrompt by rememberUpdatedState(onOpenReminderPrePrompt)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is HomeEffect.StartGame -> currentOnStartGame(effect.level, effect.mode, effect.category)
                    HomeEffect.NavigateToDaily -> currentOnOpenDaily()
                    HomeEffect.NavigateToNotebook -> currentOnOpenNotebook()
                    HomeEffect.NavigateToStats -> currentOnOpenStats()
                    HomeEffect.NavigateToSettings -> currentOnOpenSettings()
                    HomeEffect.NavigateToExamPacks -> currentOnOpenExamPacks()
                    HomeEffect.NavigateToDuel -> currentOnOpenDuel()
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
    // The gift counts as consumed once it is on screen; leaving Home drops it (a new day's gift shows again).
    val hasNotice = state.dailyGiftNotice != null
    LaunchedEffect(viewModel, lifecycleOwner, hasNotice) {
        if (hasNotice) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.onIntent(HomeIntent.NoticeShown)
                try {
                    awaitCancellation()
                } finally {
                    viewModel.onIntent(HomeIntent.Left)
                }
            }
        }
    }
    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        noticeVisible = lifecycleState.isAtLeast(Lifecycle.State.RESUMED),
    )
}
