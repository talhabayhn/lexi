package dev.talhabayhan.lexi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.talhabayhan.lexi.feature.home.HomeRoute
import dev.talhabayhan.lexi.feature.home.homeScreen

@Composable
fun LexiNavHost(modifier: Modifier = Modifier) {
    NavHost(
        navController = rememberNavController(),
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeScreen()
    }
}
