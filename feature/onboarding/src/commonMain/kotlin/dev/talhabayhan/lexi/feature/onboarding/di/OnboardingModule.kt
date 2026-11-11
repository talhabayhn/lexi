package dev.talhabayhan.lexi.feature.onboarding.di

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import dev.talhabayhan.lexi.feature.onboarding.OnboardingViewModel
import dev.talhabayhan.lexi.feature.onboarding.result.LevelTestResultRoute
import dev.talhabayhan.lexi.feature.onboarding.result.LevelTestResultViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Onboarding and the level test result. Both keep their page in the back-stack entry's `SavedStateHandle`; the result's
 * route comes from it too.
 */
public val onboardingModule: Module = module {
    viewModel {
        OnboardingViewModel(
            onboarding = get(),
            cards = get(),
            settings = get(),
            analytics = get(),
            crashReporter = get(),
            savedState = get(),
        )
    }
    viewModel {
        val handle = get<SavedStateHandle>()
        LevelTestResultViewModel(
            route = handle.toRoute<LevelTestResultRoute>(),
            onboarding = get(),
            analytics = get(),
            crashReporter = get(),
            savedState = handle,
        )
    }
}
