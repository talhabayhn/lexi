package dev.talhabayhan.lexi.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.core.data.db.AndroidDatabaseFiles
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.platform.UrlOpener
import dev.talhabayhan.lexi.core.platform.feedback.AndroidHaptics
import dev.talhabayhan.lexi.core.platform.feedback.AndroidSoundPlayer
import dev.talhabayhan.lexi.core.platform.feedback.AndroidSpeaker
import dev.talhabayhan.lexi.core.platform.feedback.GatedHaptics
import dev.talhabayhan.lexi.core.platform.feedback.GatedSoundPlayer
import dev.talhabayhan.lexi.core.platform.feedback.LiveSettings
import dev.talhabayhan.lexi.core.platform.uri.AndroidUrlOpener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

@Composable
internal actual fun rememberPlatformModule(): Module {
    val context = LocalContext.current.applicationContext
    return remember(context) { androidPlatformModule(context) }
}

internal fun androidPlatformModule(context: Context): Module = module {
    single<DatabaseFiles> { AndroidDatabaseFiles(context) }
    single<Haptics> {
        val settings = get<LiveSettings>()
        GatedHaptics(AndroidHaptics(context)) { settings.current.haptics }
    }
    single<SoundPlayer> {
        val settings = get<LiveSettings>()
        val raw = AndroidSoundPlayer(context, CoroutineScope(SupervisorJob() + Dispatchers.Default), ::readSoundAsset)
        GatedSoundPlayer(raw) { settings.current.soundEffects }
    } onClose { (it as? AutoCloseable)?.close() }
    single<Speaker> {
        val settings = get<LiveSettings>()
        val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val timingLog = if (debuggable) {
            Logger.withTag("Speech").let { log ->
                { message: String -> log.d(message) }
            }
        } else {
            null
        }
        AndroidSpeaker(context, accent = { settings.current.accent }, timingLog = timingLog)
    } onClose { (it as? AutoCloseable)?.close() }
    single<UrlOpener> { AndroidUrlOpener(context) }
}
