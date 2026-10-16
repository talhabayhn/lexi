package dev.talhabayhan.lexi.feature.notebook.di

import dev.talhabayhan.lexi.feature.notebook.NotebookViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notebookModule: Module = module {
    viewModelOf(::NotebookViewModel)
}
