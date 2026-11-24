package dev.talhabayhan.lexi.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import dev.talhabayhan.lexi.core.designsystem.motion.FrameMonitor
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MorphOverlayHost
import dev.talhabayhan.lexi.core.designsystem.motion.MorphOverlayState
import dev.talhabayhan.lexi.core.designsystem.motion.TransitionKeys
import dev.talhabayhan.lexi.core.designsystem.motion.TransitionTelemetry
import dev.talhabayhan.lexi.core.designsystem.motion.rememberMotionTier
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.platform.UrlOpener
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.feature.exams.examPacksScreen
import dev.talhabayhan.lexi.feature.game.GamePreparer
import dev.talhabayhan.lexi.feature.game.GameRoute
import dev.talhabayhan.lexi.feature.game.duel.duelScreens
import dev.talhabayhan.lexi.feature.game.gameScreen
import dev.talhabayhan.lexi.feature.game.levelTestScreen
import dev.talhabayhan.lexi.feature.game.result.resultScreen
import dev.talhabayhan.lexi.feature.home.HomeRoute
import dev.talhabayhan.lexi.feature.home.homeScreen
import dev.talhabayhan.lexi.feature.levels.picker.categoryPickerScreen
import dev.talhabayhan.lexi.feature.notebook.notebookScreen
import dev.talhabayhan.lexi.feature.onboarding.OnboardingRoute
import dev.talhabayhan.lexi.feature.onboarding.onboardingScreen
import dev.talhabayhan.lexi.feature.onboarding.result.LevelTestResultRoute
import dev.talhabayhan.lexi.feature.onboarding.result.levelTestResultScreen
import dev.talhabayhan.lexi.feature.settings.about.aboutScreen
import dev.talhabayhan.lexi.feature.settings.reminder.reminderPrePromptScreen
import dev.talhabayhan.lexi.feature.settings.settingsScreen
import dev.talhabayhan.lexi.feature.stats.statsScreen
import dev.talhabayhan.lexi.perf.PerfTour
import dev.talhabayhan.lexi.perf.PerfTourConfig
import dev.talhabayhan.lexi.splash.SplashRoute
import dev.talhabayhan.lexi.splash.splashScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.compose.koinInject

/**
 * The app graph (video flow): `Splash → Home → CategoryPicker ("Çarkını seç", mode / time dialog) → Game → Result`,
 * `CategoryPicker → Game (exam pack)`, `Home → Notebook → Game (review) → Result` and `Home → Stats`,
 * `Home → Settings`, `Home → Exam packs`, and `Home → Reminder pre-prompt` (once, after the first real game) or `Settings → Reminder
 * pre-prompt` (turning the reminder on). First run: `Splash → Onboarding → Home`, or `Onboarding → LevelTest → LevelTestResult → Home`
 * (the result replaces the test; "Pes et" and back from the result return to the onboarding). The
 * result replaces everything above Home, so system back and "Ana menüye dön" both return to Home; TEKRAR OYNA starts
 * the next game in the same place (`Home → Game`). Every navigation to the game first prepares it
 * ([LexiNavigator.startGame], at most 250 ms), so the game slides in with its board already drawn. The first-use
 * drawing (game board, card flip, dialog) is warmed up by the splash on the first launch of each version
 * (`SplashRoute`); the perf tour starts once Home is first RESUMED.
 *
 * Transitions (`motion.md` #12, slide + fade-through): the new screen slides in horizontally by 24dp over 260 ms and
 * fades in over 200 ms starting at 60 ms; the old one slides 24dp the other way and fades out over its first 90 ms;
 * back plays it in reverse. The host is filled with `cream` so the mid-fade never shows the window background.
 * Reduced motion: a 150 ms crossfade, no translation.
 *
 * Letter morph (`motion.md` #16, video flow): the host sits in a [MorphOverlayHost]. OYNA of the mode / time dialog
 * starts the morph from the button (tiers Full and Lite; Minimal keeps the cross-fade), the game is prepared during
 * the expand and navigated to under the ink without a transition; the game screen holds its clock until the board
 * has landed. System back or the app going to the background cancels the morph (no stuck overlay, no navigation once
 * cancelled); back after the game was opened under the ink also leaves it again.
 */
@Composable
fun LexiNavHost(modifier: Modifier = Modifier, perfTour: PerfTourConfig? = null) {
    val navController = rememberNavController()
    val gamePreparer = koinInject<GamePreparer>()
    val scope = rememberCoroutineScope()
    val navigator = remember(navController, gamePreparer, scope) { LexiNavigator(navController, gamePreparer, scope) }
    val motion = LexiTheme.motion
    val density = LocalDensity.current
    val distancePx = with(density) { motion.screenTransitionSlideDistance.roundToPx() }
    val transitions = remember(motion, distancePx) { ScreenTransitions(motion, distancePx) }
    val urlOpener = koinInject<UrlOpener>()
    val analytics = koinInject<AnalyticsTracker>()
    var homeReady by remember { mutableStateOf(false) }
    LaunchedEffect(navigator) {
        // The perf tour starts on Home once its transition is over.
        navigator.awaitResumedOn(HomeRoute::class)
        homeReady = true
    }
    val morph = remember { MorphOverlayState() }
    val policy = LocalMotionPolicy.current
    val tier = rememberMotionTier(policy)
    val currentTier by rememberUpdatedState(tier)
    // Field frame times (`transition_frames`): every measured span of the session's monitor, once per transition.
    val telemetry = koinInject<TransitionTelemetry>()
    DisposableEffect(policy, telemetry) {
        val monitor = policy.frameMonitor
        val listener = telemetry.listener { currentTier }
        monitor.onTransitionMeasured = listener
        onDispose { if (monitor.onTransitionMeasured === listener) monitor.onTransitionMeasured = null }
    }
    MeasureOnboardingToHome(navController, policy.frameMonitor)
    val launcher = remember(morph, navigator, navController) {
        LetterMorphLauncher(
            morph = OverlayLetterMorph(morph),
            navigation = object : GameStartNavigation {
                override fun canStartGame() = navigator.canStartGame()

                override fun isOnGame() = navController.currentDestination?.hasRoute(GameRoute::class) == true

                override fun popGame() {
                    navController.popBackStack()
                }
            },
        )
    }
    val startGame: (Level, GameMode, CategoryId?, Int?, Char) -> Unit = { level, mode, category, time, firstLetter ->
        // OYNA's own press already plays the light haptic (#1, #16).
        launcher.start(currentTier, firstLetter.toString()) { awaitCover, onDropped ->
            navigator.startGame(level, mode, category, time, awaitCover, onDropped)
        }
    }
    MorphOverlayHost(
        state = morph,
        modifier = modifier,
        frameMonitor = policy.frameMonitor,
        tier = { currentTier },
        onCancelled = launcher::onCancelled,
        onWaitTimedOut = {
            analytics.track(AnalyticsEvent.MotionFailSafe(AnalyticsEvent.MotionFailSafeKind.MORPH_TIMEOUT))
        },
    ) {
        LexiNavGraph(navController, navigator, transitions, urlOpener, morph, startGame)
        // Enabled only while a morph runs. The screens' own handlers that could outrank it (newer handlers win) step
        // aside while it runs: the picker's dialog handler is off, the game's is off before its start.
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = morph.isRunning,
            onBackCompleted = launcher::onBack,
        )
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { launcher.onStop() }
    if (perfTour != null && homeReady) PerfTour(navigator = navigator, config = perfTour)
}

/**
 * Registers the navigation from onboarding (or the level-test result) to Home with [monitor]
 * ([TransitionKeys.ONBOARDING_TO_HOME]) while both screens are visible, i.e. for the screen transition (#12); at most
 * [ONBOARDING_TO_HOME_LIMIT_MILLIS], so a transition that never settles is not watched for ever.
 */
@Composable
private fun MeasureOnboardingToHome(navController: NavHostController, monitor: FrameMonitor) {
    LaunchedEffect(navController, monitor) {
        navController.visibleEntries.first { entries ->
            entries.any { it.destination.hasRoute(HomeRoute::class) } && entries.any { it.isOnboarding() }
        }
        monitor.beginTransition(TransitionKeys.ONBOARDING_TO_HOME)
        try {
            withTimeoutOrNull(ONBOARDING_TO_HOME_LIMIT_MILLIS) {
                navController.visibleEntries.first { entries -> entries.none { it.isOnboarding() } }
            }
        } finally {
            monitor.endTransition()
        }
    }
}

private fun NavBackStackEntry.isOnboarding(): Boolean =
    destination.hasRoute(OnboardingRoute::class) || destination.hasRoute(LevelTestResultRoute::class)

/** Longest the onboarding → Home measurement lasts (the transition itself takes 260 ms, `motion.md` #12). */
private const val ONBOARDING_TO_HOME_LIMIT_MILLIS = 2_000L

@Composable
private fun LexiNavGraph(
    navController: NavHostController,
    navigator: LexiNavigator,
    transitions: ScreenTransitions,
    urlOpener: UrlOpener,
    morph: MorphOverlayState,
    startGame: (Level, GameMode, CategoryId?, Int?, Char) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = Modifier.fillMaxSize().background(LexiTheme.colors.cream),
        // Under the morph's ink the switch is instant (it is never seen, and must not cost frames).
        enterTransition = { if (morph.isRunning) EnterTransition.None else transitions.enter(forward = true) },
        exitTransition = { if (morph.isRunning) ExitTransition.None else transitions.exit(forward = true) },
        popEnterTransition = { if (morph.isRunning) EnterTransition.None else transitions.enter(forward = false) },
        popExitTransition = { if (morph.isRunning) ExitTransition.None else transitions.exit(forward = false) },
    ) {
        splashScreen(onFinished = navigator::finishSplash)
        onboardingScreen(
            onFinish = navigator::finishOnboarding,
            onStartLevelTest = { navigator.startLevelTest() },
            onOpenUrl = { url -> urlOpener.openUrl(url) },
        )
        levelTestScreen(
            onFinished = navigator::finishLevelTest,
            onQuit = navigator::quitLevelTest,
            onOpenSettings = { navigator.openSettings() },
        )
        levelTestResultScreen(
            onFinish = navigator::finishOnboarding,
            onBack = navigator::quitLevelTest,
        )
        homeScreen(
            onPlay = { level -> navigator.openCategoryPicker(level) },
            onOpenDaily = { navigator.startDaily() },
            onOpenNotebook = { navigator.openNotebook() },
            onOpenStats = { navigator.openStats() },
            onOpenSettings = { navigator.openSettings() },
            onOpenExamPacks = { navigator.openExamPacks() },
            onOpenDuel = { navigator.openDuelSetup() },
            onOpenReminderPrePrompt = { navigator.openReminderPrePrompt() },
        )
        examPacksScreen(onBack = { navigator.back() }, onStartPack = { pack -> navigator.startExamPack(pack) })
        settingsScreen(
            onBack = { navigator.back() },
            onOpenAbout = { navigator.openAbout() },
            onOpenUrl = { url -> urlOpener.openUrl(url) },
            onOpenReminderPrePrompt = { navigator.openReminderPrePrompt() },
        )
        reminderPrePromptScreen(onDone = { navigator.closeReminderPrePrompt() })
        aboutScreen(
            onBack = { navigator.back() },
            onOpenUrl = { url -> urlOpener.openUrl(url) },
        )
        statsScreen(onBack = { navigator.back() })
        notebookScreen(
            onBack = { navigator.back() },
            onStartReview = { navigator.startReview() },
        )
        categoryPickerScreen(
            onBack = { navigator.back() },
            onStart = startGame,
            onStartPack = { pack -> navigator.startExamPack(pack) },
        )
        gameScreen(
            onFinished = navigator::finishGame,
            onAbandoned = navigator::abandonGame,
            onBack = { navigator.back() },
            onOpenSettings = { navigator.openSettings() },
        )
        duelScreens(
            onStartDuel = { duel -> navigator.startDuel(duel) },
            onFinished = navigator::finishDuel,
            onAbandoned = navigator::abandonGame,
            onBack = { navigator.back() },
            onOpenSettings = { navigator.openSettings() },
            onPlayAgain = { duel -> navigator.playDuelAgain(duel) },
            onHome = { navigator.home() },
            onOpenReview = { review -> navigator.openDuelReview(review) },
        )
        resultScreen(
            onPlayAgain = { game -> navigator.playAgain(game) },
            onHome = { navigator.home() },
        )
    }
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
