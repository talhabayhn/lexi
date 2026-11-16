package dev.talhabayhan.lexi.feature.levels.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
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
}
