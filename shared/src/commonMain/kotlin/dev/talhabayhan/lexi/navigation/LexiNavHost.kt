package dev.talhabayhan.lexi.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.platform.UrlOpener
import dev.talhabayhan.lexi.feature.game.gameScreen
import dev.talhabayhan.lexi.feature.game.result.resultScreen
import dev.talhabayhan.lexi.feature.home.examPacksScreen
import dev.talhabayhan.lexi.feature.home.homeScreen
import dev.talhabayhan.lexi.feature.levels.categoriesScreen
import dev.talhabayhan.lexi.feature.levels.levelSelectScreen
import dev.talhabayhan.lexi.feature.notebook.notebookScreen
import dev.talhabayhan.lexi.feature.onboarding.onboardingScreen
import dev.talhabayhan.lexi.feature.settings.about.aboutScreen
import dev.talhabayhan.lexi.feature.settings.settingsScreen
import dev.talhabayhan.lexi.feature.stats.statsScreen
import dev.talhabayhan.lexi.perf.PerfTour
import dev.talhabayhan.lexi.perf.PerfTourConfig
import dev.talhabayhan.lexi.splash.SplashRoute
import dev.talhabayhan.lexi.splash.splashScreen
import org.koin.compose.koinInject

/**
 * The app graph: `Splash → Home → LevelSelect [→ Categories] → Game → Result`, `Home → Notebook → Game (review)
 * → Result` and `Home → Stats`, `Home → Settings`, `Home → Practice` (level select preselected to Practice) and `Home → Exam packs`. Onboarding (M6) will slot in after the splash. The
 * result replaces everything above Home, so system back and "Ana menüye dön" both return to Home; TEKRAR OYNA starts
 * the next game in the same place (`Home → Game`).
 *
 * Transitions (`motion.md` #12, slide + fade-through): the new screen slides in horizontally by 24dp over 260 ms and
 * fades in over 200 ms starting at 60 ms; the old one slides 24dp the other way and fades out over its first 90 ms;
 * back plays it in reverse. The host is filled with `cream` so the mid-fade never shows the window background.
 * Reduced motion: a 150 ms crossfade, no translation.
 */
@Composable
fun LexiNavHost(modifier: Modifier = Modifier, perfTour: PerfTourConfig? = null) {
    val navController = rememberNavController()
    val navigator = remember(navController) { LexiNavigator(navController) }
    val motion = LexiTheme.motion
    val density = LocalDensity.current
    val distancePx = with(density) { motion.screenTransitionSlideDistance.roundToPx() }
    val transitions = remember(motion, distancePx) { ScreenTransitions(motion, distancePx) }
    val urlOpener = koinInject<UrlOpener>()
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = modifier.background(LexiTheme.colors.cream),
        enterTransition = { transitions.enter(forward = true) },
        exitTransition = { transitions.exit(forward = true) },
        popEnterTransition = { transitions.enter(forward = false) },
        popExitTransition = { transitions.exit(forward = false) },
    ) {
        splashScreen(onFinished = navigator::finishSplash)
        onboardingScreen(onFinish = navigator::finishOnboarding)
        homeScreen(
            onPlay = { navigator.openLevelSelect() },
            onOpenDaily = { navigator.startDaily() },
            onOpenNotebook = { navigator.openNotebook() },
            onOpenStats = { navigator.openStats() },
            onOpenSettings = { navigator.openSettings() },
            onOpenPractice = { navigator.openLevelSelect(GameMode.PRACTICE) },
            onOpenExamPacks = { navigator.openExamPacks() },
        )
        examPacksScreen(onBack = { navigator.back() })
        settingsScreen(
            onBack = { navigator.back() },
            onOpenAbout = { navigator.openAbout() },
            onOpenUrl = { url -> urlOpener.openUrl(url) },
        )
        aboutScreen(
            onBack = { navigator.back() },
            onOpenUrl = { url -> urlOpener.openUrl(url) },
        )
        statsScreen(onBack = { navigator.back() })
        notebookScreen(
            onBack = { navigator.back() },
            onStartReview = { navigator.startReview() },
        )
        levelSelectScreen(
            onBack = { navigator.back() },
            onStart = { level, mode -> navigator.startGame(level, mode) },
            onOpenCategories = { level, mode -> navigator.openCategories(level, mode) },
        )
        categoriesScreen(
            onBack = { navigator.back() },
            onStart = { level, mode, category -> navigator.startGame(level, mode, category) },
        )
        gameScreen(
            onFinished = navigator::finishGame,
            onAbandoned = navigator::abandonGame,
            onBack = { navigator.back() },
            onOpenSettings = { navigator.openSettings() },
        )
        resultScreen(
            onPlayAgain = { game -> navigator.playAgain(game) },
            onHome = { navigator.home() },
        )
    }
    if (perfTour != null) PerfTour(navigator = navigator, config = perfTour)
}

/** #12: forward = the new screen comes from the right (end) and the old one leaves to the left; back mirrors it. */
private class ScreenTransitions(private val motion: LexiMotion, private val distancePx: Int) {
    private val slide = tween<IntOffset>(motion.screenTransitionDurationMillis, easing = motion.mechanical)
    private val fadeInSpec = tween<Float>(
        durationMillis = motion.screenTransitionFadeInMillis,
        delayMillis = motion.screenTransitionFadeInDelayMillis,
        easing = motion.mechanical,
    )
    private val fadeOutSpec = tween<Float>(motion.screenTransitionFadeOutMillis, easing = motion.mechanical)
    private val crossfade = tween<Float>(motion.screenTransitionReducedCrossfadeMillis, easing = motion.mechanical)

    fun enter(forward: Boolean): EnterTransition = if (motion.reducedMotion) {
        fadeIn(crossfade)
    } else {
        slideInHorizontally(slide) { if (forward) distancePx else -distancePx } + fadeIn(fadeInSpec)
    }

    fun exit(forward: Boolean): ExitTransition = if (motion.reducedMotion) {
        fadeOut(crossfade)
    } else {
        slideOutHorizontally(slide) { if (forward) -distancePx else distancePx } + fadeOut(fadeOutSpec)
    }
}
