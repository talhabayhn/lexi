package dev.talhabayhan.lexi

import androidx.compose.runtime.Composable
import dev.talhabayhan.lexi.di.appModules
import dev.talhabayhan.lexi.navigation.LexiNavHost
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

/** Root composable for Android (`MainActivity`) and iOS (`MainViewController`): starts Koin and the nav graph. */
@Composable
fun App() {
    KoinApplication(configuration = koinConfiguration { modules(appModules) }) {
        LexiNavHost()
    }
}
