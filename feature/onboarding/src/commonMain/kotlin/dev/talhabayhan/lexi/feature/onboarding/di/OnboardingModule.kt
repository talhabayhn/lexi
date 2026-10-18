package dev.talhabayhan.lexi.feature.onboarding.di

import dev.talhabayhan.lexi.feature.onboarding.OnboardingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

public val onboardingModule: Module = module {
    viewModelOf(::OnboardingViewModel)
}
