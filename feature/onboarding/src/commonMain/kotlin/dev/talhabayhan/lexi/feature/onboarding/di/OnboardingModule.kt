package dev.talhabayhan.lexi.feature.onboarding.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.talhabayhan.lexi.feature.onboarding.OnboardingViewModel
import dev.talhabayhan.lexi.feature.onboarding.result.LevelTestResultRoute
import dev.talhabayhan.lexi.feature.onboarding.result.LevelTestResultViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Onboarding and the level test result; the result's route comes from the back-stack entry's `SavedStateHandle`. */
public val onboardingModule: Module = module {
    viewModelOf(::OnboardingViewModel)
    viewModel {
        LevelTestResultViewModel(
            route = get<SavedStateHandle>().toRoute<LevelTestResultRoute>(),
            onboarding = get(),
        )
    }
}
