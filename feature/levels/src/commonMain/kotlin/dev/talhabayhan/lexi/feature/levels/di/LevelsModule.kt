package dev.talhabayhan.lexi.feature.levels.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.talhabayhan.lexi.feature.levels.CategoriesRoute
import dev.talhabayhan.lexi.feature.levels.CategoriesViewModel
import dev.talhabayhan.lexi.feature.levels.LevelSelectRoute
import dev.talhabayhan.lexi.feature.levels.LevelSelectViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val levelsModule: Module = module {
    viewModel {
        LevelSelectViewModel(
            route = get<SavedStateHandle>().toRoute<LevelSelectRoute>(),
            cards = get(),
            progress = get(),
            settings = get(),
        )
    }
    viewModel {
        CategoriesViewModel(
            route = get<SavedStateHandle>().toRoute<CategoriesRoute>(),
            cards = get(),
            categories = get(),
            progress = get(),
        )
    }
}
