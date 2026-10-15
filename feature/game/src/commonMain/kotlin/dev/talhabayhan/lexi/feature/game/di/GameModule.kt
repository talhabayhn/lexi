package dev.talhabayhan.lexi.feature.game.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.game.GameViewModel
import dev.talhabayhan.lexi.feature.game.SeedSource
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import dev.talhabayhan.lexi.feature.game.result.ResultViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.random.Random
import kotlin.time.TimeSource

/**
 * Game and result screens: the ViewModels plus the game clock (monotonic, ADR 0019) and solo seed source. Route args
 * come from the back-stack entry's `SavedStateHandle` (`toRoute`); the game's handle also keeps the process-death
 * snapshot.
 */
val gameModule: Module = module {
    single<TimeSource.WithComparableMarks> { TimeSource.Monotonic }
    single<SeedSource> { SeedSource { Random.nextLong() } }
    viewModel {
        val handle = get<SavedStateHandle>()
        GameViewModel(
            route = handle.toRoute<GameRoute>(),
            handle = handle,
            cards = get(),
            progress = get(),
            jokers = get(),
            settings = get(),
            finishGame = get(),
            appClock = get(),
            speaker = get(),
            config = get(),
            timeSource = get(),
            seeds = get(),
        )
    }
    viewModel {
        ResultViewModel(
            route = get<SavedStateHandle>().toRoute<ResultRoute>(),
            cards = get(),
            speaker = get(),
            config = get(),
        )
    }
}
