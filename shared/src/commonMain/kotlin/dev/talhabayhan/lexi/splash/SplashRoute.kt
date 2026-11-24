package dev.talhabayhan.lexi.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.DpSize
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.talhabayhan.lexi.core.designsystem.component.HiddenPreDraw
import dev.talhabayhan.lexi.core.designsystem.component.LexiSplash
import dev.talhabayhan.lexi.core.designsystem.component.LexiSplashLogo
import dev.talhabayhan.lexi.core.designsystem.component.SplashDefaults
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.modifier.fadeWithShadowRoom
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMorphAnchors
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMorphOverlay
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.TransitionKeys
import dev.talhabayhan.lexi.core.designsystem.motion.TransitionTelemetry
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.AppInfo
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.repository.ContentVersionRepository
import dev.talhabayhan.lexi.core.domain.repository.OnboardingRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.feature.game.GameWarmUp
import dev.talhabayhan.lexi.feature.home.HomeWarmUp
import dev.talhabayhan.lexi.feature.onboarding.OnboardingWarmUp
import dev.talhabayhan.lexi.navigation.FlowWarmUp
import dev.talhabayhan.lexi.shared.generated.resources.Res
import dev.talhabayhan.lexi.shared.generated.resources.splash_tagline
import dev.talhabayhan.lexi.startup.StartupTrace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Whether the splash has finished in this process: work that is not needed for it waits for this (App). */
internal object SplashState {
    val finished: MutableStateFlow<Boolean> = MutableStateFlow(false)
}

/** Type-safe navigation destination of the splash, the start destination of the app. */
@Serializable
data object SplashRoute

/** [onFinished] runs when the logo animation is over and the content database is ready: the graph moves on to Onboarding or Home. */
fun NavGraphBuilder.splashScreen(onFinished: (onboardingCompleted: Boolean) -> Unit) {
    composable<SplashRoute> {
        SplashRoute(onFinished = onFinished)
    }
}

/**
 * Plays the splash (`docs/design/motion.md` #9: bg shapes, L, E, X, I, tagline, 1.2 s) while the bundled content
 * database is installed or version-checked in the background (the first read opens it). The app moves on when all are
 * done, so a fast start is never held up beyond the animation and a slow first-run copy is waited for. With reduced
 * motion the logo is shown statically for a short hold. Each landing gives a light haptic and a "tık".
 *
 * Launch logo (`docs/PERF.md` § Launch logo): the first frame shows the four blocks at rest ([LexiSplash] `logoOnly`),
 * exactly as the native iOS launch screen's `LaunchLogo` image does, so the hand-off is invisible; the animation starts
 * from there (no hold on later launches: the decision takes at most [SPLASH_DECISION_FRAMES] frames).
 *
 * First launch of each app version ([Settings.warmedUpVersionCode] differs from [AppInfo.BUILD_NUMBER],
 * [FirstLaunchWarmUp]): the logo is held while the app's first-use drawing ([AppWarmUp]: the splash's poses,
 * onboarding's first page, Home, the game board, the category card mid-flip, at rest and entering, the dialog mid-open,
 * the letter morph) is drawn at its real size for [SPLASH_WARM_UP_FRAMES] frames above the splash, under an opaque
 * cream cover inset from the screen's edges ([HiddenPreDraw]), with the held logo drawn again above the cover; the
 * frame looks unchanged. After an install or update Metal builds each GPU pipeline the first time it is drawn and
 * caches it; doing that here keeps it out of every animation. The animation starts when the warm-up has completed or
 * after [FIRST_LAUNCH_HOLD_CAP_MILLIS], whichever comes first; only a completed warm-up stores the version (a capped one
 * is tried again on the next launch). A version that cannot be read in time counts as warmed (no hold). Not with
 * reduced motion. The animation itself (keyframes, duration, easing, haptics) is unchanged; it is measured for
 * `transition_frames` ([TransitionKeys.SPLASH]).
 */
@Composable
internal fun SplashRoute(
    onFinished: (Boolean) -> Unit,
    content: ContentVersionRepository = koinInject(),
    onboarding: OnboardingRepository = koinInject(),
    analytics: AnalyticsTracker = koinInject(),
    settings: SettingsRepository = koinInject(),
    clock: AppClock = koinInject(),
    telemetry: TransitionTelemetry = koinInject(),
    warmUpFrames: Int = SPLASH_WARM_UP_FRAMES,
) {
    val motion = LexiTheme.motion
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    val monitor = LocalMotionPolicy.current.frameMonitor
    val currentOnFinished by rememberUpdatedState(onFinished)
    val progress = remember { Animatable(if (motion.reducedMotion) 1f else 0f) }
    var logoOnly by remember { mutableStateOf(!motion.reducedMotion) }
    var warmingUp by remember { mutableStateOf(false) }
    LaunchedEffect(content, onboarding, analytics, motion) {
        val completed = coroutineScope {
            val ready = async { StartupTrace.measureSuspend("content warm-up") { content.warmUp() } }
            val isCompleted = async {
                try {
                    onboarding.onboardingCompleted.first()
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    false
                }
            }
            if (motion.reducedMotion) {
                // Reduced motion turned on during a hold (the effect restarts): end on the full static logo.
                logoOnly = false
                progress.snapTo(1f)
                launch {
                    val stored = settings.warmedUpVersionOrNull()
                    telemetry.firstLaunchOfVersion =
                        FirstLaunchWarmUp.isFirstLaunchOfVersion(stored, AppInfo.BUILD_NUMBER)
                }
                delay(motion.splashReducedHoldMillis.toLong())
            } else {
                val warmed = settings.warmedUpVersionOrNull()
                telemetry.firstLaunchOfVersion = FirstLaunchWarmUp.isFirstLaunchOfVersion(warmed, AppInfo.BUILD_NUMBER)
                if (FirstLaunchWarmUp.shouldWarmUp(warmed, AppInfo.BUILD_NUMBER, reducedMotion = false)) {
                    // First launch of this version: the logo is held while the warm-up is drawn hidden.
                    val outcome = try {
                        holdForWarmUp(FIRST_LAUNCH_HOLD_CAP_MILLIS) {
                            warmingUp = true
                            repeat(warmUpFrames) { withFrameNanos { } }
                        }
                    } finally {
                        warmingUp = false
                    }
                    StartupTrace.mark("warm-up: $outcome")
                    // A short DataStore write; the splash's finish waits for it (far shorter than the animation).
                    if (FirstLaunchWarmUp.shouldRecord(outcome)) launch { settings.recordWarmUp() }
                } else if (warmed == null) {
                    StartupTrace.mark("warm-up skipped: settings not read in time")
                }
                val landings = launch {
                    var landed = 0
                    snapshotFlow { progress.value }.collect { value ->
                        while (landed < SplashDefaults.LandingFractions.size &&
                            value >= SplashDefaults.LandingFractions[landed]
                        ) {
                            haptics.perform(HapticKind.Light)
                            sounds.play(SoundEffect.Tap)
                            landed++
                        }
                    }
                }
                monitor.beginTransition(TransitionKeys.SPLASH)
                try {
                    // From the held logo to the animation's first pose (blocks off screen, shapes at 0).
                    logoOnly = false
                    progress.animateTo(1f, tween(motion.splashDurationMillis, easing = motion.linear))
                } finally {
                    monitor.endTransition()
                }
                landings.cancel()
            }
            ready.await()
            isCompleted.await()
        }
        StartupTrace.mark("splash finished")
        SplashState.finished.value = true
        // The splash runs once per cold start (docs/ANALYTICS.md).
        analytics.track(AnalyticsEvent.AppOpen)
        // The install date for the rating request rules (RequestInAppReview); a failed write only delays it.
        try {
            settings.recordFirstLaunch(clock.nowMs())
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            // Ignored: the rating request then simply waits for the next launch.
        }
        currentOnFinished(completed)
    }
    Box(Modifier.fillMaxSize()) {
        SplashScreen(progress = { progress.value }, logoOnly = logoOnly)
        // Above the splash (a pre-draw behind its full-screen cream is dropped), hidden under a cream cover, with the
        // held logo drawn again above that cover.
        if (warmingUp) {
            SplashWarmUpLayer()
            // A visual copy only: the splash below already announces the logo.
            LexiSplashLogo(
                tagline = stringResource(Res.string.splash_tagline),
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}

/** The covered warm-up drawn above the splash's held logo (see [SplashRoute]); fills the splash's own size. */
@Composable
internal fun SplashWarmUpLayer() {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val screen = DpSize(maxWidth, maxHeight)
        HiddenPreDraw(cover = LexiTheme.colors.cream, modifier = Modifier.fillMaxSize()) {
            AppWarmUp(stringResource(Res.string.splash_tagline), screen)
        }
    }
}

/**
 * Everything the first launch of a version warms up, drawn inside [HiddenPreDraw] at the real [screen] size (laid out
 * at the screen's size: in a smaller box the letter wheel got a size <= 0 and Skia's paragraph builder aborted the app
 * on iOS, `SplashWarmUpIosTest`): the splash at poses that between them use every kind of layer its animation uses
 * (shapes and X scaling in, X turned, tagline half faded in, landed logo), onboarding's first page
 * ([OnboardingWarmUp]), Home ([HomeWarmUp]), the game board ([GameWarmUp]) and the video flow ([FlowWarmUp]: card art
 * turned, the real category card mid-flip, at rest and entering, the dialog mid-open, the letter morph's ink and big
 * letter). Real size, so text is rasterised at the sizes the screens use. No semantics but its test tag, no input,
 * its intents go nowhere.
 */
@Composable
private fun AppWarmUp(tagline: String, screen: DpSize) {
    // Isolated from the real screens: no morph anchor (the game board's wheel marker) and no morph state reaches the
    // app's letter morph, so nothing the warm-up composes can stand in for, or wait on, a real screen.
    CompositionLocalProvider(LocalMorphAnchors provides null, LocalMorphOverlay provides null) {
        AppWarmUpContent(tagline, screen)
    }
}

@Composable
private fun AppWarmUpContent(tagline: String, screen: DpSize) {
    Box(Modifier.fillMaxSize().clearAndSetSemantics { testTag = SPLASH_WARM_UP_TAG }) {
        Box(Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).requiredSize(screen)) {
            for (pose in SplashWarmUpPoses) LexiSplash(progress = { pose }, tagline = tagline)
            // Mid-transition poses (`docs/PERF-AUDIT.md` § Shader variety): onboarding and Home inside a half-faded,
            // sub-pixel-shifted layer like the screen transition (#12), the board through the entrance's own fade with
            // a sub-pixel move and scale (#16 / #17), so the offscreen composite and the anti-aliased clips of moving
            // surfaces are built too, not only their resting draws.
            OnboardingWarmUp(Modifier.fillMaxSize().graphicsLayer(MidTransitionLayer))
            HomeWarmUp(Modifier.fillMaxSize().graphicsLayer(MidTransitionLayer))
            GameWarmUp(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(MidEntranceLayer)
                    .fadeWithShadowRoom(LexiTheme.shadows.fadeOutsets) { WARM_UP_HALF_ALPHA },
            )
            FlowWarmUp(Modifier.fillMaxSize())
        }
    }
}

/**
 * The stored warmed-up version code, waiting at most [SPLASH_DECISION_FRAMES] frames for the store's first read; null
 * when it is not there by then (no wait on a slow start: the warm-up is skipped and tried on the next launch).
 */
private suspend fun SettingsRepository.warmedUpVersionOrNull(): Int? {
    repeat(SPLASH_DECISION_FRAMES) {
        loaded.value?.let { return it.warmedUpVersionCode }
        withFrameNanos { }
    }
    return loaded.value?.warmedUpVersionCode
}

/** Stores that this version has warmed up; a failed write only means one more warm-up on the next launch. */
private suspend fun SettingsRepository.recordWarmUp() {
    try {
        update { it.copy(warmedUpVersionCode = AppInfo.BUILD_NUMBER) }
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        // Ignored: warmed up again next time.
    }
}

/** How the first-launch hold ended ([holdForWarmUp]). */
internal enum class WarmUpOutcome {
    /** Every warm-up frame was drawn: the version is stored. */
    Completed,

    /** [FIRST_LAUNCH_HOLD_CAP_MILLIS] passed first: the animation starts anyway, nothing is stored. */
    CappedOut,
}

/** The persisted-key rules of the first launch of a version (`warmed_up_version_code`, `docs/PERF.md`). */
internal object FirstLaunchWarmUp {
    /** A stored version known in time ([stored] not null) that is not the [running] one. */
    fun isFirstLaunchOfVersion(stored: Int?, running: Int): Boolean = stored != null && stored != running

    /** Hold the logo and warm up: a first launch of the version, never with reduced motion. */
    fun shouldWarmUp(stored: Int?, running: Int, reducedMotion: Boolean): Boolean =
        !reducedMotion && isFirstLaunchOfVersion(stored, running)

    /** Store the version only once the warm-up has completed, so a capped one runs again next time. */
    fun shouldRecord(outcome: WarmUpOutcome): Boolean = outcome == WarmUpOutcome.Completed
}

/**
 * Runs [warmUp] for at most [capMillis]: [WarmUpOutcome.Completed] when it returned in time, otherwise it is cancelled
 * and the result is [WarmUpOutcome.CappedOut]. The splash holds its logo while this runs.
 */
internal suspend fun holdForWarmUp(capMillis: Long, warmUp: suspend () -> Unit): WarmUpOutcome =
    if (withTimeoutOrNull(capMillis) { warmUp() } != null) WarmUpOutcome.Completed else WarmUpOutcome.CappedOut

/** The longest the first launch of a version holds the logo for its warm-up before the animation starts. */
internal const val FIRST_LAUNCH_HOLD_CAP_MILLIS = 2_000L

/**
 * Frames the warm-up stays composed before the animation starts: composed and drawn, drawn again, then removed (the
 * Home warm-ups of 1.1.1 used the same count).
 */
internal const val SPLASH_WARM_UP_FRAMES = 3

/** Frames the splash waits at most for the stored version before deciding (the 2-frame pre-roll of 1.1.1). */
private const val SPLASH_DECISION_FRAMES = 2

/** Test tag of the warm-up (tests only; it has no other semantics). */
internal const val SPLASH_WARM_UP_TAG = "splashWarmUp"

private const val WARM_UP_HALF_ALPHA = 0.5f
private const val WARM_UP_SUBPIXEL = 0.5f
private const val WARM_UP_ENTRANCE_SCALE = 0.98f

/** A screen transition's frame (#12): faded and moved by a fraction of a pixel. */
private val MidTransitionLayer: GraphicsLayerScope.() -> Unit = {
    alpha = WARM_UP_HALF_ALPHA
    translationX = WARM_UP_SUBPIXEL
}

/** A board entrance's frame (#16): moved by a fraction of a pixel and scaled (its fade is `fadeWithShadowRoom`). */
private val MidEntranceLayer: GraphicsLayerScope.() -> Unit = {
    translationY = WARM_UP_SUBPIXEL
    scaleX = WARM_UP_ENTRANCE_SCALE
    scaleY = WARM_UP_ENTRANCE_SCALE
}

// Fractions of `SplashDefaults`' keyframes: shapes scaling in and L / E moving (0.12), X spinning and scaling (0.28),
// tagline at half alpha (0.53), everything landed (1).
private val SplashWarmUpPoses = floatArrayOf(0.12f, 0.28f, 0.53f, 1f)

/** Stateless splash: the animation position (0..1) is read in the draw phase only; [logoOnly]: the held launch logo. */
@Composable
internal fun SplashScreen(progress: () -> Float, modifier: Modifier = Modifier, logoOnly: Boolean = false) {
    LexiSplash(
        progress = progress,
        tagline = stringResource(Res.string.splash_tagline),
        modifier = modifier,
        logoOnly = logoOnly,
    )
}

/** Opens (installs or version-checks) the content database; a failure is left to the screens that read content. */
private suspend fun ContentVersionRepository.warmUp() {
    try {
        contentVersion()
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        // The game screen reports unreadable content; the menu still opens.
    }
}
