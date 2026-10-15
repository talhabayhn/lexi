package dev.talhabayhan.lexi.di

import androidx.compose.runtime.Composable
import dev.talhabayhan.lexi.core.data.db.BundledContentDb
import dev.talhabayhan.lexi.core.data.di.dataModule
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.session.FinishGame
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.core.platform.time.SystemAppClock
import dev.talhabayhan.lexi.data.ResourcesBundledContentDb
import dev.talhabayhan.lexi.feature.game.di.gameModule
import dev.talhabayhan.lexi.feature.home.di.homeModule
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * App-level bindings: game tunables (Remote Config overrides later), the bundled content.db, the wall clock, the
 * speaker and domain use cases. `FinishGame` is a factory: one instance per game (its run-once guard is per instance).
 */
internal val appCoreModule: Module = module {
    single { GameConfig() }
    single<BundledContentDb> { ResourcesBundledContentDb() }
    single<AppClock> { SystemAppClock() }
    factory { FinishGame(sessions = get(), progress = get(), clock = get(), config = get()) }
    // Silent until the M4 platform task binds TextToSpeech / AVSpeechSynthesizer (core:platform).
    single<Speaker> { SilentSpeaker }
}

private object SilentSpeaker : Speaker {
    override fun speak(word: String) = Unit

    override fun stop() = Unit
}

/**
 * Every platform-independent Koin module of the app; `shared` is the only module that wires features and data
 * together. Platform objects (`DatabaseFiles`) come from [rememberPlatformModule].
 */
val appModules: List<Module> = listOf(
    appCoreModule,
    dataModule,
    homeModule,
    gameModule,
)

/** Koin definitions that need platform objects (Android `Context`), created at the composition root. */
@Composable
internal expect fun rememberPlatformModule(): Module
