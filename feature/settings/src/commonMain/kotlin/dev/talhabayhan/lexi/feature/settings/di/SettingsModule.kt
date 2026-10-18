package dev.talhabayhan.lexi.feature.settings.di

import dev.talhabayhan.lexi.feature.settings.SettingsViewModel
import dev.talhabayhan.lexi.feature.settings.about.AboutViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule: Module = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AboutViewModel)
}
