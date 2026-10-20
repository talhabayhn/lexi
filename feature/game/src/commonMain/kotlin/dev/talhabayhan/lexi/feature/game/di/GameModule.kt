package dev.talhabayhan.lexi.feature.game.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.talhabayhan.lexi.feature.game.GameLoader
import dev.talhabayhan.lexi.feature.game.GamePreparer
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.game.GameViewModel
import dev.talhabayhan.lexi.feature.game.SeedSource
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import dev.talhabayhan.lexi.feature.game.result.ResultViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.random.Random
import kotlin.time.TimeSource

/**
 * Game and result screens: the ViewModels plus the game clock (monotonic, ADR 0019), solo seed source, the game
 * loader and the app-wide [GamePreparer] (one preparation before a navigation to the game). Route args
 * come from the back-stack entry's `SavedStateHandle` (`toRoute`); the game's handle also keeps the process-death
 * snapshot.
 */
val gameModule: Module = module {
    single<TimeSource.WithComparableMarks> { TimeSource.Monotonic }
    single<SeedSource> { SeedSource { Random.nextLong() } }
    single {
        GameLoader(
            cards = get(),
            progress = get(),
            jokers = get(),
            settings = get(),
            appClock = get(),
            config = get(),
            seeds = get(),
            getDailyWheel = get(),
        )
    }
    single { GamePreparer(loader = get(), scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)) }
    viewModel {
        val handle = get<SavedStateHandle>()
        GameViewModel(
            route = handle.toRoute<GameRoute>(),
            handle = handle,
            loader = get(),
            jokers = get(),
            settings = get(),
            finishGame = get(),
            appClock = get(),
            speaker = get(),
            config = get(),
            timeSource = get(),
            flags = get(),
            preparer = get(),
        )
    }
    viewModel {
        ResultViewModel(
            route = get<SavedStateHandle>().toRoute<ResultRoute>(),
            cards = get(),
            speaker = get(),
            config = get(),
            progress = get(),
            clock = get(),
        )
    }
}
