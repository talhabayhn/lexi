package dev.talhabayhan.lexi.feature.levels.picker

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
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe destination of "Çarkını seç" (`video-flow-notes.md` §2). [level] is an enum *name* (navigation 2.10 has
 * no enum nav type on iOS, like `GameRoute`); Home opens it on the remembered level. An unknown name reads as A1.
 */
@Serializable
data class CategoryPickerRoute(val level: String = Level.A1.name) {
    constructor(level: Level) : this(level = level.name)

    internal val startLevel: Level get() = Level.entries.firstOrNull { it.name == level } ?: Level.A1
}

/**
 * Registers "Çarkını seç". [onStart] opens a game of the level, mode, category (null = Karışık) and time (seconds;
 * null = untimed), [onStartPack] an exam pack's wheel; the graph in `shared` owns the game route.
 */
fun NavGraphBuilder.categoryPickerScreen(
    onBack: () -> Unit,
    onStart: (Level, GameMode, CategoryId?, Int?) -> Unit,
    onStartPack: (ExamPackId) -> Unit,
) {
    composable<CategoryPickerRoute> {
        CategoryPickerRoute(onBack = onBack, onStart = onStart, onStartPack = onStartPack)
    }
}

@Composable
internal fun CategoryPickerRoute(
    onBack: () -> Unit,
    onStart: (Level, GameMode, CategoryId?, Int?) -> Unit,
    onStartPack: (ExamPackId) -> Unit,
    viewModel: CategoryPickerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStartPack by rememberUpdatedState(onStartPack)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    CategoryPickerEffect.NavigateBack -> currentOnBack()

                    is CategoryPickerEffect.StartGame ->
                        currentOnStart(effect.level, effect.mode, effect.category, effect.timeSeconds)

                    is CategoryPickerEffect.StartPack -> currentOnStartPack(effect.pack)
                }
            }
        }
    }
    val flip = rememberPickerDialogFlip(state.dialog)
    // System back (Android back / predictive back, iOS swipe-back) closes the open dialog first (reversing a running
    // open) and is held while the dialog animates out.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.dialog != null || flip.dialogShown,
        onBackCompleted = { viewModel.onIntent(CategoryPickerIntent.CloseDialog) },
    )
    CategoryPickerScreen(
        state = state,
        onIntent = viewModel::onIntent,
        entrance = rememberPlayGridEntrance(),
        flip = flip,
    )
}
