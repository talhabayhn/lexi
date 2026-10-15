package dev.talhabayhan.lexi

import androidx.compose.runtime.Composable
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.di.appModules
import dev.talhabayhan.lexi.di.rememberPlatformModule
import dev.talhabayhan.lexi.navigation.LexiNavHost
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

/**
 * Root composable for Android (`MainActivity`) and iOS (`MainViewController`): starts Koin and hosts the nav graph
 * inside [LexiTheme] (light only, ADR 0010).
 */
@Composable
fun App() {
    val platformModule = rememberPlatformModule()
    KoinApplication(configuration = koinConfiguration { modules(appModules + platformModule) }) {
        LexiTheme {
            LexiNavHost()
        }
    }
}
