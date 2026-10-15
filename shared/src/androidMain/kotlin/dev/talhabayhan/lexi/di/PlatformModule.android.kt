package dev.talhabayhan.lexi.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.talhabayhan.lexi.core.data.db.AndroidDatabaseFiles
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import org.koin.core.module.Module
import org.koin.dsl.module

@Composable
internal actual fun rememberPlatformModule(): Module {
    val context = LocalContext.current.applicationContext
    return remember(context) { androidPlatformModule(context) }
}

internal fun androidPlatformModule(context: Context): Module = module {
    single<DatabaseFiles> { AndroidDatabaseFiles(context) }
}
