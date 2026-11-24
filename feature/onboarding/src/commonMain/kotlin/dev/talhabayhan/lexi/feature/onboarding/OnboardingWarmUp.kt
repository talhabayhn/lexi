package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.talhabayhan.lexi.core.designsystem.component.inertWarmUp

/**
 * One-time warm-up of the onboarding's first page ([OnboardingStep.Step1]) composed, measured and drawn like the real
 * screen, so its first-use GPU pipelines and glyphs at their real size are built before the first onboarding frame.
 * The splash draws it hidden on the first launch of each version (`SplashRoute`, `docs/PERF.md`). Inert: no semantics,
 * no touch, no focus; its intents go nowhere.
 */
@Composable
public fun OnboardingWarmUp(modifier: Modifier = Modifier) {
    Box(modifier.inertWarmUp()) {
        OnboardingScreen(state = OnboardingUiState(currentStep = OnboardingStep.Step1), onIntent = {})
    }
}
