package dev.talhabayhan.lexi.di

import dev.talhabayhan.lexi.feature.home.di.homeModule
import org.koin.core.module.Module

/** Every Koin module of the app; `shared` is the only module that wires features and data together. */
val appModules: List<Module> = listOf(
    homeModule,
)
