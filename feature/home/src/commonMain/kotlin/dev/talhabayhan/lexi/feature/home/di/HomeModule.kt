package dev.talhabayhan.lexi.feature.home.di

import dev.talhabayhan.lexi.feature.home.HomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule: Module = module {
    viewModelOf(::HomeViewModel)
}
