package dev.talhabayhan.lexi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.game.gameScreen
import dev.talhabayhan.lexi.feature.home.HomeRoute
import dev.talhabayhan.lexi.feature.home.homeScreen

@Composable
fun LexiNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        // Temporary: home starts A1 classic directly until level select (03-level-select) exists.
        homeScreen(onStartGame = { navController.navigate(GameRoute(Level.A1, GameMode.CLASSIC)) })
        // Until the result screen (06-result) exists, a finished game returns to home.
        gameScreen(
            onFinished = { navController.backToHome() },
            onAbandoned = { navController.backToHome() },
        )
    }
}

private fun NavHostController.backToHome() {
    popBackStack(HomeRoute, inclusive = false)
}
