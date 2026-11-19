package dev.talhabayhan.lexi.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.DpSize
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.talhabayhan.lexi.core.designsystem.component.HiddenPreDraw
import dev.talhabayhan.lexi.core.designsystem.component.LexiSplash
import dev.talhabayhan.lexi.core.designsystem.component.SplashDefaults
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
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
 * App warm-up (`docs/PERF.md`): on the **first launch of each app version** ([Settings.warmedUpVersionCode] differs
 * from [AppInfo.BUILD_NUMBER]), before the 1.2 s clock starts, the app's first-use drawing ([AppWarmUp]: the splash's
 * poses, the game board, the category card mid-flip, the dialog mid-open, the picker cards at rest and entering) is
 * drawn for [SPLASH_WARM_UP_FRAMES] frames above the splash, under an opaque cream cover ([HiddenPreDraw]) over the top
 * half, which the visible first frame shows as plain cream (only the I peeks in from below), so that frame looks
 * unchanged. After an install or update Metal builds each GPU pipeline the first time it is drawn and caches it; doing
 * that here keeps it out of every animation. The version is then stored and every later launch skips the warm-up (its
 * clock starts within [SPLASH_DECISION_FRAMES] frames, never later than the 2-frame pre-roll of 1.1.1). A version
 * that cannot be read by then counts as warmed (no wait; the next launch tries again). Not with reduced motion.
 * (Up to 1.1.1 these pre-draws sat *behind* full-screen opaque backgrounds, which made Skia drop them unseen.) The
 * animation itself (keyframes, duration, easing, haptics) is unchanged.
 */
@Composable
internal fun SplashRoute(
    onFinished: (Boolean) -> Unit,
    content: ContentVersionRepository = koinInject(),
    onboarding: OnboardingRepository = koinInject(),
    analytics: AnalyticsTracker = koinInject(),
    settings: SettingsRepository = koinInject(),
    clock: AppClock = koinInject(),
) {
    val motion = LexiTheme.motion
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    val currentOnFinished by rememberUpdatedState(onFinished)
    val progress = remember { Animatable(if (motion.reducedMotion) 1f else 0f) }
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
                delay(motion.splashReducedHoldMillis.toLong())
            } else {
                val warmed = settings.warmedUpVersionOrNull()
                if (warmed != null && warmed != AppInfo.BUILD_NUMBER) {
                    // First launch of this version: drawn for a few frames, removed before the clock starts.
                    warmingUp = true
                    repeat(SPLASH_WARM_UP_FRAMES) { withFrameNanos { } }
                    warmingUp = false
                    StartupTrace.mark("warm-up ran")
                    // A short DataStore write; the splash's finish waits for it (far shorter than the animation).
                    launch { settings.recordWarmUp() }
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
                progress.animateTo(1f, tween(motion.splashDurationMillis, easing = motion.linear))
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
        SplashScreen(progress = { progress.value })
        // Above the splash (a pre-draw behind its full-screen cream is dropped), hidden under the cream of its first frame.
        if (warmingUp) SplashWarmUpLayer()
    }
}

/** The covered warm-up drawn above the splash's first frame (see [SplashRoute]); fills the splash's own size. */
@Composable
internal fun SplashWarmUpLayer() {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val screen = DpSize(maxWidth, maxHeight)
        HiddenPreDraw(
            cover = LexiTheme.colors.cream,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(SPLASH_WARM_UP_HEIGHT_FRACTION),
        ) {
            AppWarmUp(stringResource(Res.string.splash_tagline), screen)
        }
    }
}

/**
 * Everything the first launch of a version warms up, drawn inside [HiddenPreDraw]: the splash at poses that between
 * them use every kind of layer its animation uses (shapes and X scaling in, X turned, tagline half faded in, landed
 * logo), the game board ([GameWarmUp]) and the video flow ([FlowWarmUp]: card art turned, the real category card
 * mid-flip, the dialog mid-open, the picker cards at rest and entering, the letter morph's ink). The splash poses fill
 * the covered top half; the game board and the flow are laid out at the size of the real [screen] and drawn at half
 * scale into it. (Laid out in the half itself, the letter wheel got a size <= 0 and Skia's paragraph builder aborted
 * the app on iOS: `SplashWarmUpIosTest`.) No semantics but its test tag, no input, its intents go nowhere.
 */
@Composable
private fun AppWarmUp(tagline: String, screen: DpSize) {
    Box(Modifier.fillMaxSize().clearAndSetSemantics { testTag = SPLASH_WARM_UP_TAG }) {
        for (pose in SplashWarmUpPoses) LexiSplash(progress = { pose }, tagline = tagline)
        Box(
            Modifier
                .wrapContentSize(Alignment.TopStart, unbounded = true)
                .requiredSize(screen)
                .graphicsLayer {
                    scaleX = SPLASH_WARM_UP_HEIGHT_FRACTION
                    scaleY = SPLASH_WARM_UP_HEIGHT_FRACTION
                    transformOrigin = TransformOrigin(0f, 0f)
                },
        ) {
            GameWarmUp(Modifier.fillMaxSize())
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

/**
 * Frames the warm-up stays composed before the animation starts: composed and drawn, drawn again, then removed (the
 * Home warm-ups of 1.1.1 used the same count).
 */
internal const val SPLASH_WARM_UP_FRAMES = 3

/** Frames the splash waits at most for the stored version before deciding (the 2-frame pre-roll of 1.1.1). */
private const val SPLASH_DECISION_FRAMES = 2

/**
 * The warm-up covers the top half of the screen: plain cream in the splash's first frame on every screen size (the
 * shapes start at scale 0, L and E off screen, X at scale 0; only the I, from below, is on screen).
 */
private const val SPLASH_WARM_UP_HEIGHT_FRACTION = 0.5f

/** Test tag of the warm-up (tests only; it has no other semantics). */
internal const val SPLASH_WARM_UP_TAG = "splashWarmUp"

// Fractions of `SplashDefaults`' keyframes: shapes scaling in and L / E moving (0.12), X spinning and scaling (0.28),
// tagline at half alpha (0.53), everything landed (1).
private val SplashWarmUpPoses = floatArrayOf(0.12f, 0.28f, 0.53f, 1f)

/** Stateless splash: the animation position (0..1) is read in the draw phase only. */
@Composable
internal fun SplashScreen(progress: () -> Float, modifier: Modifier = Modifier) {
    LexiSplash(progress = progress, tagline = stringResource(Res.string.splash_tagline), modifier = modifier)
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
