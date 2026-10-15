package dev.talhabayhan.lexi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                navController.navigate(HomeRoute) { popUpTo(SplashRoute) { inclusive = true } }
            },
        )
        homeScreen(onPlay = { navController.navigate(LevelSelectRoute) })
        levelSelectScreen(
            onBack = { navController.popBackStack() },
            onStart = { level, mode -> navController.navigate(GameRoute(level, mode)) },
        )
        gameScreen(
            onFinished = { navController.backToHome() },
            onAbandoned = { navController.backToHome() },
        )
    }
}

private fun NavHostController.backToHome() {
    popBackStack(HomeRoute, inclusive = false)
}
