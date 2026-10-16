package dev.talhabayhan.lexi.feature.stats.di

import dev.talhabayhan.lexi.feature.stats.StatsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val statsModule: Module = module {
    viewModelOf(::StatsViewModel)
}
