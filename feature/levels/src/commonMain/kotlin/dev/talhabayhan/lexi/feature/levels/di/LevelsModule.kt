package dev.talhabayhan.lexi.feature.levels.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.talhabayhan.lexi.feature.levels.CategoriesRoute
import dev.talhabayhan.lexi.feature.levels.CategoriesViewModel
import dev.talhabayhan.lexi.feature.levels.LevelSelectViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val levelsModule: Module = module {
    viewModelOf(::LevelSelectViewModel)
    viewModel {
        CategoriesViewModel(
            route = get<SavedStateHandle>().toRoute<CategoriesRoute>(),
            cards = get(),
            categories = get(),
            progress = get(),
        )
    }
}
