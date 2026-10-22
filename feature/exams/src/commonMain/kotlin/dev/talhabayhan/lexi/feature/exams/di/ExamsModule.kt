package dev.talhabayhan.lexi.feature.exams.di

import dev.talhabayhan.lexi.feature.exams.ExamPacksViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val examsModule: Module = module {
    viewModelOf(::ExamPacksViewModel)
}
