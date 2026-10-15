package dev.talhabayhan.lexi.feature.levels.di

import dev.talhabayhan.lexi.feature.levels.LevelSelectViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val levelsModule: Module = module {
    viewModelOf(::LevelSelectViewModel)
}
