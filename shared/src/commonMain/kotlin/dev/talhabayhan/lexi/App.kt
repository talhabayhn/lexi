package dev.talhabayhan.lexi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import dev.talhabayhan.lexi.splash.SplashState
import kotlinx.coroutines.flow.first
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration

/**
 * Root composable for Android (`MainActivity`) and iOS (`MainViewController`): starts Koin and hosts the nav graph
 * inside [LexiTheme] (light only, ADR 0010) and [LexiViewport] (the Expanded tier: large viewports draw the designs
 * scaled, `screen-map.md`). With "Doğru cevapta telaffuz" on, the speech engine and voice are warmed up right after the
 * splash so the first correct answer is spoken at once.
 */
@Composable
fun App(onLanguageChanged: ((AppLanguage) -> Unit)? = null) {
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
        CompositionLocalProvider(
            LocalHaptics provides koinInject(),
            LocalSoundPlayer provides koinInject(),
        ) {
            LexiTheme {
                LexiViewport {
                    LexiNavHost()
                }
            }
        }
    }
}
