package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe navigation destination for the first-run onboarding.
 */
@Serializable
public data object OnboardingRoute

/**
 * Adds the onboarding screen to the navigation graph. [onFinish] is invoked when onboarding finishes (a level chip,
 * or skip); [onStartLevelTest] by "Başla" of the level test intro, returns whether it navigated (the onboarding
 * stays below the test).
 */
public fun NavGraphBuilder.onboardingScreen(onFinish: () -> Unit, onStartLevelTest: () -> Boolean) {
    composable<OnboardingRoute> {
        OnboardingRoute(onFinish = onFinish, onStartLevelTest = onStartLevelTest)
    }
}

@Composable
internal fun OnboardingRoute(
    onFinish: () -> Unit,
    onStartLevelTest: () -> Boolean,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnFinish by rememberUpdatedState(onFinish)
    val currentOnStartLevelTest by rememberUpdatedState(onStartLevelTest)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    OnboardingEffect.NavigateToHome -> currentOnFinish()

                    OnboardingEffect.StartLevelTest -> if (currentOnStartLevelTest()) {
                        viewModel.onIntent(OnboardingIntent.LevelTestOpened)
                    }
                }
            }
        }
    }
    // After the level test has fully covered the screen, so the step change is never seen on the way out.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onIntent(OnboardingIntent.ScreenStopped) }

    // System back on the intro returns to the level choice, like its back button.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = state.currentStep == OnboardingStep.LevelTestIntro,
        onBackCompleted = { viewModel.onIntent(OnboardingIntent.Back) },
    )

    OnboardingScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}
