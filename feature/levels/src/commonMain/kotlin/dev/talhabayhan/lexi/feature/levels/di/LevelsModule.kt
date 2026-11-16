package dev.talhabayhan.lexi.feature.levels.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.talhabayhan.lexi.feature.levels.CategoriesRoute
import dev.talhabayhan.lexi.feature.levels.CategoriesViewModel
import dev.talhabayhan.lexi.feature.levels.LevelSelectRoute
import dev.talhabayhan.lexi.feature.levels.LevelSelectViewModel
import dev.talhabayhan.lexi.feature.levels.picker.CategoryPickerRoute
import dev.talhabayhan.lexi.feature.levels.picker.CategoryPickerViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val levelsModule: Module = module {
    viewModel {
        CategoryPickerViewModel(
            route = get<SavedStateHandle>().toRoute<CategoryPickerRoute>(),
            cards = get(),
            categories = get(),
            examPacks = get(),
            progress = get(),
            settings = get(),
            config = get(),
            crashReporter = get(),
        )
    }
    viewModel {
        LevelSelectViewModel(
            route = get<SavedStateHandle>().toRoute<LevelSelectRoute>(),
            cards = get(),
            progress = get(),
            settings = get(),
            crashReporter = get(),
        )
    }
    viewModel {
        CategoriesViewModel(
            route = get<SavedStateHandle>().toRoute<CategoriesRoute>(),
            cards = get(),
            categories = get(),
            progress = get(),
            crashReporter = get(),
        )
    }
}
