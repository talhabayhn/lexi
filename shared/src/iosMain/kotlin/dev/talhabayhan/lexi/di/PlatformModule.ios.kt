@file:OptIn(ExperimentalNativeApi::class)

package dev.talhabayhan.lexi.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import co.touchlab.kermit.Logger
import dev.talhabayhan.lexi.IosPlatformServices
import dev.talhabayhan.lexi.core.data.db.DatabaseFiles
import dev.talhabayhan.lexi.core.data.db.IosDatabaseFiles
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.platform.ShareService
import dev.talhabayhan.lexi.core.domain.platform.UrlOpener
import dev.talhabayhan.lexi.core.domain.reminder.NotificationPermission
import dev.talhabayhan.lexi.core.domain.reminder.ReminderScheduler
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import dev.talhabayhan.lexi.core.platform.feedback.GatedHaptics
import dev.talhabayhan.lexi.core.platform.feedback.GatedSoundPlayer
import dev.talhabayhan.lexi.core.platform.feedback.IosHaptics
import dev.talhabayhan.lexi.core.platform.feedback.IosSoundPlayer
import dev.talhabayhan.lexi.core.platform.feedback.IosSpeaker
import dev.talhabayhan.lexi.core.platform.feedback.LiveSettings
import dev.talhabayhan.lexi.core.platform.reminder.IosNotificationPermission
import dev.talhabayhan.lexi.core.platform.reminder.IosReminderScheduler
import dev.talhabayhan.lexi.core.platform.share.IosShareService
import dev.talhabayhan.lexi.core.platform.telemetry.ConsentGatedAnalyticsTracker
import dev.talhabayhan.lexi.core.platform.uri.IosUrlOpener
import dev.talhabayhan.lexi.telemetry.IosAnalyticsTracker
import dev.talhabayhan.lexi.telemetry.IosCrashReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

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
            val timingLog = if (Platform.isDebugBinary) {
                Logger.withTag("Speech").let { log -> { message: String -> log.d(message) } }
            } else {
                null
            }
            IosSpeaker(accent = { settings.current.accent }, timingLog = timingLog)
        }
        single<UrlOpener> { IosUrlOpener() }
        single<ShareService> { IosShareService() }
        single<ReminderScheduler> { IosReminderScheduler() }
        single<NotificationPermission> { IosNotificationPermission() }
        // Firebase through the Swift bridge (ADR 0023).
        // Analytics only after the player's opt-in; Crashlytics always.
        single<AnalyticsTracker> {
            ConsentGatedAnalyticsTracker(
                sink = IosAnalyticsTracker(IosPlatformServices.services.telemetry),
                settings = get(),
                scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            )
        }
        single<CrashReporter> { IosCrashReporter(IosPlatformServices.services.telemetry) }
    }
}
