package dev.talhabayhan.lexi.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.game.gameScreen
import dev.talhabayhan.lexi.feature.game.result.resultScreen
import dev.talhabayhan.lexi.feature.home.ExamPacksRoute
import dev.talhabayhan.lexi.feature.home.HomeRoute
import dev.talhabayhan.lexi.feature.home.examPacksScreen
import dev.talhabayhan.lexi.feature.home.homeScreen
import dev.talhabayhan.lexi.feature.levels.CategoriesRoute
import dev.talhabayhan.lexi.feature.levels.LevelSelectRoute
import dev.talhabayhan.lexi.feature.levels.categoriesScreen
import dev.talhabayhan.lexi.feature.levels.levelSelectScreen
import dev.talhabayhan.lexi.feature.notebook.NotebookRoute
import dev.talhabayhan.lexi.feature.notebook.notebookScreen
import dev.talhabayhan.lexi.feature.settings.SettingsRoute
import dev.talhabayhan.lexi.feature.settings.settingsScreen
import dev.talhabayhan.lexi.feature.stats.StatsRoute
import dev.talhabayhan.lexi.feature.stats.statsScreen
import dev.talhabayhan.lexi.splash.SplashRoute
import dev.talhabayhan.lexi.splash.splashScreen

/**
 * The app graph: `Splash → Home → LevelSelect [→ Categories] → Game → Result`, `Home → Notebook → Game (review)
 * → Result` and `Home → Stats`, `Home → Settings`, `Home → Practice` (level select preselected to Practice) and `Home → Exam packs`. Onboarding (M6) will slot in after the splash. The
 * result replaces everything above Home, so system back and "Ana menüye dön" both return to Home; TEKRAR OYNA starts
 * the next game in the same place (`Home → Game`).
 *
 * Transitions (`motion.md` #12): the new screen slides in horizontally by 24dp over the old one and the old one moves
 * 24dp the other way, 220 ms, no fade; back plays it in reverse. Reduced motion: screens switch in place.
 */
@Composable
fun LexiNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val motion = LexiTheme.motion
    val density = LocalDensity.current
    val distancePx = with(density) { motion.screenTransitionSlideDistance.roundToPx() }
    val transitions = remember(motion, distancePx) { ScreenTransitions(motion, distancePx) }
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier,
        enterTransition = { transitions.enter(forward = true) },
        exitTransition = { transitions.exit(forward = true) },
        popEnterTransition = { transitions.enter(forward = false) },
        popExitTransition = { transitions.exit(forward = false) },
    ) {
        splashScreen(
            // Not RESUMED-guarded: this fires once from the splash's LaunchedEffect, possibly while the app is in the
            // background (first-run DB copy); a dropped call would leave the app stuck on the splash.
            onFinished = {
                navController.navigate(HomeRoute) {
                    launchSingleTop = true
                    popUpTo(SplashRoute) { inclusive = true }
                }
            },
        )
        homeScreen(
            onPlay = { navController.ifResumed { navigate(LevelSelectRoute()) { launchSingleTop = true } } },
            onOpenNotebook = { navController.ifResumed { navigate(NotebookRoute) { launchSingleTop = true } } },
            onOpenStats = { navController.ifResumed { navigate(StatsRoute) { launchSingleTop = true } } },
            onOpenSettings = { navController.ifResumed { navigate(SettingsRoute) { launchSingleTop = true } } },
            onOpenPractice = {
                navController.ifResumed { navigate(LevelSelectRoute(GameMode.PRACTICE)) { launchSingleTop = true } }
            },
            onOpenExamPacks = { navController.ifResumed { navigate(ExamPacksRoute) { launchSingleTop = true } } },
        )
        examPacksScreen(onBack = { navController.ifResumed { popBackStack() } })
        settingsScreen(onBack = { navController.ifResumed { popBackStack() } })
        statsScreen(onBack = { navController.ifResumed { popBackStack() } })
        notebookScreen(
            onBack = { navController.ifResumed { popBackStack() } },
            onStartReview = {
                navController.ifResumed { navigate(GameRoute.review()) { launchSingleTop = true } }
            },
        )
        levelSelectScreen(
            onBack = { navController.ifResumed { popBackStack() } },
            onStart = { level, mode ->
                navController.ifResumed { navigate(GameRoute(level, mode)) { launchSingleTop = true } }
            },
            onOpenCategories = { level, mode ->
                navController.ifResumed { navigate(CategoriesRoute(level, mode)) { launchSingleTop = true } }
            },
        )
        categoriesScreen(
            onBack = { navController.ifResumed { popBackStack() } },
            onStart = { level, mode, category ->
                navController.ifResumed { navigate(GameRoute(level, mode, category)) { launchSingleTop = true } }
            },
        )
        gameScreen(
            // Not RESUMED-guarded: fired once from the game's effects (while STARTED) after the game is recorded.
            onFinished = { result ->
                navController.navigate(result) {
                    launchSingleTop = true
                    popUpTo(HomeRoute) { inclusive = false }
                }
            },
            onAbandoned = { navController.backToHome() },
            onBack = { navController.ifResumed { popBackStack() } },
        )
        resultScreen(
            onPlayAgain = { game ->
                navController.ifResumed {
                    navigate(game) {
                        launchSingleTop = true
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                }
            },
            onHome = { navController.ifResumed { backToHome() } },
        )
    }
}

/** #12: forward = the new screen comes from the right (end) and the old one leaves to the left; back mirrors it. */
private class ScreenTransitions(private val motion: LexiMotion, private val distancePx: Int) {
    private val spec = tween<IntOffset>(motion.screenTransitionDurationMillis, easing = motion.mechanical)

    fun enter(forward: Boolean): EnterTransition = if (motion.reducedMotion) {
        EnterTransition.None
    } else {
        slideInHorizontally(spec) { if (forward) distancePx else -distancePx }
    }

    fun exit(forward: Boolean): ExitTransition = if (motion.reducedMotion) {
        ExitTransition.None
    } else {
        slideOutHorizontally(spec) { if (forward) -distancePx else distancePx }
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
