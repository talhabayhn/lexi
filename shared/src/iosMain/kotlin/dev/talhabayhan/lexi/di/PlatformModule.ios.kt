package dev.talhabayhan.lexi.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import dev.talhabayhan.lexi.core.data.db.IosDatabaseFiles
import org.koin.core.module.Module
import org.koin.dsl.module

@Composable
internal actual fun rememberPlatformModule(): Module = remember {
    module { single<DatabaseFiles> { IosDatabaseFiles() } }
}
