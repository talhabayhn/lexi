package dev.talhabayhan.lexi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.core.domain.feedback.Speaker
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.AppLanguage
import dev.talhabayhan.lexi.di.appModules
import dev.talhabayhan.lexi.di.rememberPlatformModule
import dev.talhabayhan.lexi.navigation.LexiNavHost
import dev.talhabayhan.lexi.perf.PerfTourConfig
import dev.talhabayhan.lexi.splash.SplashState
import dev.talhabayhan.lexi.startup.StartupTrace
import dev.talhabayhan.lexi.startup.preloadAllStrings
import kotlinx.coroutines.flow.first
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

/**
 * Root composable for Android (`MainActivity`) and iOS (`MainViewController`): starts Koin and hosts the nav graph
 * inside [LexiTheme] (light only, ADR 0010) and [LexiViewport] (the Expanded tier: large viewports draw the designs
 * scaled, `screen-map.md`).
 * [perfTour] (launch flag only, `docs/PERF.md`) runs the scripted perf tour. With "Doğru cevapta telaffuz" on,
 * the speech engine and voice are warmed up right after the splash so the first correct answer is spoken at once, and
 * every module's strings are preloaded into the resource cache on a background dispatcher.
 */
@Composable
fun App(onLanguageChanged: ((AppLanguage) -> Unit)? = null, perfTour: PerfTourConfig? = null) {
    StartupTrace.mark("app composition")
    LaunchedEffect(Unit) {
        withFrameNanos { }
        StartupTrace.mark("first frame")
    }
    val platformModule = rememberPlatformModule()
    KoinApplication(configuration = koinConfiguration { modules(appModules + platformModule) }) {
        val settingsRepository = koinInject<SettingsRepository>()
        if (onLanguageChanged != null) {
            LaunchedEffect(settingsRepository) {
                settingsRepository.settings.collect {
                    onLanguageChanged(it.appLanguage)
                }
            }
        }
        val speaker = koinInject<Speaker>()
        LaunchedEffect(speaker) {
            // After the splash: the engine's start-up would otherwise compete with its animation for the main thread.
            SplashState.finished.first { it }
            if (settingsRepository.settings.first().speakOnCorrect) speaker.warmUp()
        }
        LaunchedEffect(Unit) {
            // Off the main thread and after the splash, so it never competes with the splash or a transition.
            SplashState.finished.first { it }
            preloadAllStrings()
        }
        CompositionLocalProvider(
            LocalHaptics provides koinInject(),
            LocalSoundPlayer provides koinInject(),
        ) {
            LexiTheme {
                LexiViewport {
                    LexiNavHost(perfTour = perfTour)
                }
            }
        }
    }
}
