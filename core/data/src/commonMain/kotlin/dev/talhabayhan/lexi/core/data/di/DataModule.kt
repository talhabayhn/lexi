package dev.talhabayhan.lexi.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.talhabayhan.lexi.core.data.content.CardRepositoryImpl
import dev.talhabayhan.lexi.core.data.content.CategoryRepositoryImpl
import dev.talhabayhan.lexi.core.data.content.ContentVersionRepositoryImpl
import dev.talhabayhan.lexi.core.data.content.ExamPackRepositoryImpl
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import dev.talhabayhan.lexi.core.data.db.Databases
import dev.talhabayhan.lexi.core.data.settings.SettingsRepositoryImpl
import dev.talhabayhan.lexi.core.data.settings.createSettingsDataStore
import dev.talhabayhan.lexi.core.data.user.DailyRepositoryImpl
import dev.talhabayhan.lexi.core.data.user.JokerRepositoryImpl
import dev.talhabayhan.lexi.core.data.user.ProgressRepositoryImpl
import dev.talhabayhan.lexi.core.data.user.SessionRepositoryImpl
import dev.talhabayhan.lexi.core.data.user.StreakRepositoryImpl
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.CategoryRepository
import dev.talhabayhan.lexi.core.domain.repository.ContentVersionRepository
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.repository.ExamPackRepository
import dev.talhabayhan.lexi.core.domain.repository.JokerRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.repository.StreakRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Qualifier of the dispatcher that runs all database and file work. */
public val IoDispatcher: Qualifier = named("io")

/**
 * Repository implementations behind the domain interfaces (`ARCHITECTURE.md` §6). The app provides
 * `DatabaseFiles` (platform), `BundledContentDb` (resources) and `GameConfig`.
 */
public val dataModule: Module = module {
    single<CoroutineDispatcher>(IoDispatcher) { Dispatchers.IO }
    single { Databases(bundled = get(), files = get(), io = get(IoDispatcher)) }

    single<CardRepository> { CardRepositoryImpl(get(), get(IoDispatcher)) }
    single<CategoryRepository> { CategoryRepositoryImpl(get(), get(IoDispatcher)) }
    single<ExamPackRepository> { ExamPackRepositoryImpl(get(), get(IoDispatcher)) }
    single<ContentVersionRepository> { ContentVersionRepositoryImpl(get(), get(IoDispatcher)) }

    single<ProgressRepository> { ProgressRepositoryImpl(get(), get(IoDispatcher)) }
    single<SessionRepository> { SessionRepositoryImpl(get(), get(IoDispatcher), get()) }
    single<StreakRepository> { StreakRepositoryImpl(get(), get(IoDispatcher)) }
    single<DailyRepository> { DailyRepositoryImpl(get(), get(IoDispatcher)) }
    single<JokerRepository> { JokerRepositoryImpl(get(), get(IoDispatcher)) }

    single<DataStore<Preferences>> {
        val io = get<CoroutineDispatcher>(IoDispatcher)
        createSettingsDataStore(get<DatabaseFiles>().settingsFilePath(), CoroutineScope(SupervisorJob() + io))
    }
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get()) }
}
