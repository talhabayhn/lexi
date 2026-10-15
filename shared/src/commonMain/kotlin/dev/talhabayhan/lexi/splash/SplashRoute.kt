package dev.talhabayhan.lexi.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
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
import dev.talhabayhan.lexi.shared.generated.resources.Res
import dev.talhabayhan.lexi.shared.generated.resources.splash_tagline
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Type-safe navigation destination of the splash, the start destination of the app. */
@Serializable
data object SplashRoute

/** [onFinished] runs when the logo animation is over and the content database is ready: the graph moves on to Home. */
fun NavGraphBuilder.splashScreen(onFinished: () -> Unit) {
    composable<SplashRoute> {
        SplashRoute(onFinished = onFinished)
    }
}

/**
 * Plays the splash (`docs/design/motion.md` #9: bg shapes, L, E, X, I, tagline, 1.2 s) while the bundled content
 * database is installed or version-checked in the background (the first read opens it). The app moves on when both are
 * done, so a fast start is never held up beyond the animation and a slow first-run copy is waited for. With reduced
 * motion the logo is shown statically for a short hold. Each landing gives a light haptic and a "tık".
 */
@Composable
internal fun SplashRoute(onFinished: () -> Unit, content: ContentVersionRepository = koinInject()) {
    val motion = LexiTheme.motion
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    val currentOnFinished by rememberUpdatedState(onFinished)
    val progress = remember { Animatable(if (motion.reducedMotion) 1f else 0f) }
    LaunchedEffect(content, motion) {
        coroutineScope {
            val ready = async { content.warmUp() }
            if (motion.reducedMotion) {
                delay(motion.splashReducedHoldMillis.toLong())
            } else {
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
        }
        currentOnFinished()
    }
    SplashScreen(progress = { progress.value })
}

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
