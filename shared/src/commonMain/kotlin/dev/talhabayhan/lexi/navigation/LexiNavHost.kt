package dev.talhabayhan.lexi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.game.gameScreen
import dev.talhabayhan.lexi.feature.home.HomeRoute
import dev.talhabayhan.lexi.feature.home.homeScreen
import dev.talhabayhan.lexi.feature.levels.LevelSelectRoute
import dev.talhabayhan.lexi.feature.levels.levelSelectScreen
import dev.talhabayhan.lexi.splash.SplashRoute
import dev.talhabayhan.lexi.splash.splashScreen

/**
 * The app graph: `Splash → Home → LevelSelect → Game`. Onboarding (M6) will slot in after the splash; a finished game
 * returns to Home until the result screen (`06-result`) exists.
 */
@Composable
fun LexiNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier,
    ) {
        splashScreen(
            onFinished = {
                navController.ifResumed {
                    navigate(HomeRoute) {
                        launchSingleTop = true
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            },
        )
        homeScreen(onPlay = { navController.ifResumed { navigate(LevelSelectRoute) { launchSingleTop = true } } })
        levelSelectScreen(
            onBack = { navController.ifResumed { popBackStack() } },
            onStart = { level, mode ->
                navController.ifResumed { navigate(GameRoute(level, mode)) { launchSingleTop = true } }
            },
        )
        gameScreen(
            onFinished = { navController.backToHome() },
            onAbandoned = { navController.backToHome() },
        )
    }
}

/**
 * Runs [action] only while the current destination is fully shown (RESUMED). A second tap on OYNA, BAŞLA or Back
 * during the transition finds the entry no longer resumed and is dropped, so entries never stack or the graph empty.
 */
private inline fun NavHostController.ifResumed(action: NavHostController.() -> Unit) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) action()
}

private fun NavHostController.backToHome() {
    popBackStack(HomeRoute, inclusive = false)
}
