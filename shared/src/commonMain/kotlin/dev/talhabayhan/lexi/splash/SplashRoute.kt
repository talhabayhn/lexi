package dev.talhabayhan.lexi.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.talhabayhan.lexi.core.designsystem.component.LexiSplash
import dev.talhabayhan.lexi.core.designsystem.component.SplashDefaults
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.repository.ContentVersionRepository
import dev.talhabayhan.lexi.core.domain.repository.OnboardingRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.time.AppClock
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
 * Pre-roll (`docs/PERF.md` § iOS first launch): before the 1.2 s clock starts, the logo is drawn at a few of its own
 * poses ([SplashWarmUp]) behind the splash's opaque cream for [SPLASH_WARM_UP_FRAMES] frames, while the visible splash
 * still shows its first frame (plain cream, like the launch screen). After a fresh install Metal builds each GPU
 * pipeline the first time it is drawn; without the pre-roll that happened inside the animation and made it stutter. The
 * animation itself (keyframes, duration, easing, haptics) is unchanged; it only starts these frames later. Not with
 * reduced motion (a static logo has nothing to stutter).
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
    var warmingUp by remember { mutableStateOf(!motion.reducedMotion) }
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
                // The pre-roll: drawn in the first frames, then removed before the clock starts.
                repeat(SPLASH_WARM_UP_FRAMES) { withFrameNanos { } }
                warmingUp = false
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
        // Behind the splash's opaque background, so it is never seen.
        if (warmingUp) SplashWarmUp(stringResource(Res.string.splash_tagline))
        SplashScreen(progress = { progress.value })
    }
}

/**
 * The splash's drawing at poses that between them use every kind of layer the animation uses: the shapes and the X
 * scaling in, the X turned, the tagline half faded in, and the landed logo. Drawn once each; no semantics, no input.
 */
@Composable
private fun SplashWarmUp(tagline: String) {
    Box(Modifier.fillMaxSize().clearAndSetSemantics { testTag = SPLASH_WARM_UP_TAG }) {
        for (pose in SplashWarmUpPoses) LexiSplash(progress = { pose }, tagline = tagline)
    }
}

/** Frames the pre-roll stays drawn before the animation starts (drawn once, plus one to be safe, like `GameWarmUp`). */
internal const val SPLASH_WARM_UP_FRAMES = 2

/** Test tag of the pre-roll (tests only; it has no other semantics). */
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
