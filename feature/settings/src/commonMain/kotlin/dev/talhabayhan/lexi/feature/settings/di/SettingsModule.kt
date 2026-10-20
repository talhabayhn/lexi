package dev.talhabayhan.lexi.feature.settings.di

import dev.talhabayhan.lexi.feature.settings.SettingsViewModel
import dev.talhabayhan.lexi.feature.settings.about.AboutViewModel
import dev.talhabayhan.lexi.feature.settings.reminder.ReminderPrePromptViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule: Module = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AboutViewModel)
    viewModelOf(::ReminderPrePromptViewModel)
}
