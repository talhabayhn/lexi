package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.Level

/** Level test button is hidden until B5. */
internal const val LEVEL_TEST_AVAILABLE: Boolean = false

@Immutable
internal enum class OnboardingStep {
    Step1,
    Step2,
    Step3,
    LevelChoice,
    LevelPicker,
}

@Immutable
internal data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.Step1,
    val selectedLevel: Level = Level.A1,
)

internal sealed interface OnboardingIntent {
    data object Next : OnboardingIntent
    data object Skip : OnboardingIntent
    data object OpenLevelPicker : OnboardingIntent
    data object Back : OnboardingIntent
    data class PickLevel(val level: Level) : OnboardingIntent
    data object StartLevelTest : OnboardingIntent
}

internal sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect
}
