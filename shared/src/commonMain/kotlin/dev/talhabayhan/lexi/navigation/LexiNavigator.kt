package dev.talhabayhan.lexi.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.GamePreparer
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.game.LevelTestRoute
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import dev.talhabayhan.lexi.feature.home.ExamPacksRoute
import dev.talhabayhan.lexi.feature.home.HomeRoute
import dev.talhabayhan.lexi.feature.levels.CategoriesRoute
import dev.talhabayhan.lexi.feature.levels.LevelSelectRoute
import dev.talhabayhan.lexi.feature.notebook.NotebookRoute
import dev.talhabayhan.lexi.feature.onboarding.OnboardingRoute
import dev.talhabayhan.lexi.feature.onboarding.result.LevelTestResultRoute
import dev.talhabayhan.lexi.feature.settings.SettingsRoute
import dev.talhabayhan.lexi.feature.settings.about.AboutRoute
import dev.talhabayhan.lexi.feature.stats.StatsRoute
import dev.talhabayhan.lexi.splash.SplashRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Every navigation action of the app, shared by the screens (via [LexiNavHost]) and the perf tour, so both run the
 * same code. The `open…`/`back`/`start…` actions are dropped unless the current destination is RESUMED and no game is
 * being prepared, so one tap gives one navigation (entries never stack, the graph never empties); they return whether
 * they navigated (game starts: whether the start was accepted, see [openGame]).
 */
internal class LexiNavigator(
    private val navController: NavHostController,
    private val gamePreparer: GamePreparer,
    private val scope: CoroutineScope,
) {
    /** The running [openGame]: its preparation, then the navigation. */
    private var startingGame: Job? = null

    /** True while the current destination is [route] and fully shown (RESUMED). */
    fun isResumedOn(route: KClass<*>): Boolean {
        val entry = navController.currentBackStackEntry ?: return false
        return entry.lifecycle.currentState == Lifecycle.State.RESUMED && entry.destination.hasRoute(route)
    }

    /** Suspends until [route] is the current destination and RESUMED (follows the back stack, no frame polling). */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun awaitResumedOn(route: KClass<*>) {
        navController.currentBackStackEntryFlow
            .flatMapLatest { entry ->
                if (entry.destination.hasRoute(route)) entry.lifecycle.currentStateFlow else flowOf(null)
            }
            .first { it == Lifecycle.State.RESUMED }
    }

    // Not RESUMED-guarded: fires once from the splash's LaunchedEffect, possibly while the app is in the background
    // (first-run DB copy); a dropped call would leave the app stuck on the splash.
    fun finishSplash(completed: Boolean) {
        val destination = if (completed) HomeRoute else OnboardingRoute
        navController.navigate(destination) {
            launchSingleTop = true
            popUpTo(SplashRoute) { inclusive = true }
        }
    }

    fun finishOnboarding() {
        navController.navigate(HomeRoute) {
            launchSingleTop = true
            popUpTo(OnboardingRoute) { inclusive = true }
        }
    }

    /** "Başla" of the level test intro: the test runs above the onboarding (`GAME_RULES.md` §9). */
    fun startLevelTest(): Boolean = ifResumed { navigate(LevelTestRoute) { launchSingleTop = true } }

    // Not RESUMED-guarded: fired once from the level test's effects (while STARTED), like [finishGame]. The result
    // replaces the test, so back from the result returns to the onboarding.
    fun finishLevelTest(level: Level) {
        navController.navigate(LevelTestResultRoute(level.name)) {
            launchSingleTop = true
            popUpTo(LevelTestRoute) { inclusive = true }
        }
    }

    /**
     * "Pes et", "Geri dön" of a test that can't start, and back from the result: back to the onboarding (its level
     * choice); nothing is saved. Not guarded, see [finishLevelTest]; a second call finds the onboarding on top.
     */
    fun quitLevelTest() {
        navController.popBackStack(OnboardingRoute, inclusive = false)
    }

    fun openLevelSelect(mode: GameMode? = null): Boolean = ifResumed {
        navigate(if (mode == null) LevelSelectRoute() else LevelSelectRoute(mode)) { launchSingleTop = true }
    }

    fun openCategories(level: Level, mode: GameMode): Boolean = ifResumed {
        navigate(CategoriesRoute(level, mode)) { launchSingleTop = true }
    }

    fun startGame(level: Level, mode: GameMode, category: CategoryId? = null): Boolean =
        openGame(GameRoute(level, mode, category)) { launchSingleTop = true }

    fun startDaily(): Boolean = openGame(GameRoute.daily()) { launchSingleTop = true }

    fun startReview(): Boolean = openGame(GameRoute.review()) { launchSingleTop = true }

    /** TEKRAR OYNA: the next game replaces everything above Home. */
    fun playAgain(game: GameRoute): Boolean = openGame(game) {
        launchSingleTop = true
        popUpTo(HomeRoute) { inclusive = false }
    }

    fun openNotebook(): Boolean = ifResumed { navigate(NotebookRoute) { launchSingleTop = true } }

    fun openStats(): Boolean = ifResumed { navigate(StatsRoute) { launchSingleTop = true } }

    /** Also the pause dialog's "Ayarlar": the game stays on the back stack, paused. */
    fun openSettings(): Boolean = ifResumed { navigate(SettingsRoute) { launchSingleTop = true } }

    fun openAbout(): Boolean = ifResumed { navigate(AboutRoute) { launchSingleTop = true } }

    fun openExamPacks(): Boolean = ifResumed { navigate(ExamPacksRoute) { launchSingleTop = true } }

    fun back(): Boolean = ifResumed { popBackStack() }

    /** Ana menüye dön. */
    fun home(): Boolean = ifResumed { backToHome() }

    // Not RESUMED-guarded: fired once from the game's effects (while STARTED) after the game is recorded.
    fun finishGame(result: ResultRoute) {
        navController.navigate(result) {
            launchSingleTop = true
            popUpTo(HomeRoute) { inclusive = false }
        }
    }

    /** A restored game that can no longer be played (never recorded); not guarded, see [finishGame]. */
    fun abandonGame() = navController.backToHome()

    /**
     * Every navigation to the game: the game is prepared first ([GamePreparer]) and the navigation follows once it is
     * ready or after [GamePreparer.NAVIGATION_CAP], so the game's first frame shows the board. Taps while it prepares
     * are dropped; it navigates only if the tapped entry is still the current one and RESUMED, otherwise the
     * preparation is discarded. True when the start was accepted.
     */
    private fun openGame(route: GameRoute, options: NavOptionsBuilder.() -> Unit): Boolean {
        val source = resumedEntry() ?: return false
        startingGame = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            var navigated = false
            try {
                gamePreparer.prepare(route)
                val current = navController.currentBackStackEntry
                if (current === source && source.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    navController.navigate(route, options)
                    navigated = true
                }
            } finally {
                // Also when the host left the composition while preparing.
                if (!navigated) gamePreparer.discard(route)
            }
        }
        return true
    }

    /** The current entry when it is RESUMED and no game is being prepared. */
    private fun resumedEntry(): NavBackStackEntry? {
        if (startingGame?.isActive == true) return null
        return navController.currentBackStackEntry?.takeIf { it.lifecycle.currentState == Lifecycle.State.RESUMED }
    }

    private inline fun ifResumed(action: NavHostController.() -> Unit): Boolean {
        if (resumedEntry() == null) return false
        navController.action()
        return true
    }

    private fun NavHostController.backToHome() {
        popBackStack(HomeRoute, inclusive = false)
    }
}
