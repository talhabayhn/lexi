package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/**
 * Type-safe navigation destination for the first-run onboarding.
 */
@Serializable
public data object OnboardingRoute

/**
 * Adds the onboarding screen to the navigation graph.
 * [onFinish] is invoked when onboarding finishes (via CTA or skip).
 */
public fun NavGraphBuilder.onboardingScreen(onFinish: () -> Unit) {
    composable<OnboardingRoute> {
        OnboardingRoute(onFinish = onFinish)
    }
}

@Composable
internal fun OnboardingRoute(onFinish: () -> Unit, viewModel: OnboardingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnFinish by rememberUpdatedState(onFinish)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    OnboardingEffect.NavigateToHome -> currentOnFinish()
                }
            }
        }
    }

    OnboardingScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}
