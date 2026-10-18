package dev.talhabayhan.lexi.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import dev.talhabayhan.lexi.core.data.db.IosDatabaseFiles
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.platform.UrlOpener
import dev.talhabayhan.lexi.core.platform.feedback.GatedHaptics
import dev.talhabayhan.lexi.core.platform.feedback.GatedSoundPlayer
import dev.talhabayhan.lexi.core.platform.feedback.IosHaptics
import dev.talhabayhan.lexi.core.platform.feedback.IosSoundPlayer
import dev.talhabayhan.lexi.core.platform.feedback.IosSpeaker
import dev.talhabayhan.lexi.core.platform.feedback.LiveSettings
import dev.talhabayhan.lexi.core.platform.uri.IosUrlOpener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

@Composable
internal actual fun rememberPlatformModule(): Module = remember {
    module {
        single<DatabaseFiles> { IosDatabaseFiles() }
        single<Haptics> {
            val settings = get<LiveSettings>()
            GatedHaptics(IosHaptics()) { settings.current.haptics }
        }
        single<SoundPlayer> {
            val settings = get<LiveSettings>()
            val raw = IosSoundPlayer(CoroutineScope(SupervisorJob() + Dispatchers.Main), ::readSoundAsset)
            GatedSoundPlayer(raw) { settings.current.soundEffects }
        }
        single<Speaker> {
            val settings = get<LiveSettings>()
            IosSpeaker { settings.current.accent }
        }
        single<UrlOpener> { IosUrlOpener() }
    }
}
