package dev.talhabayhan.lexi.feature.onboarding

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.Level

@Immutable
internal enum class OnboardingStep {
    Step1,
    Step2,
    Step3,

    /** `01-onboarding`: level test button, "YA DA SEVİYENİ SEÇ" and the A1…NAT chips. */
    LevelChoice,

    /** What the level test is, before it starts (DESIGN-DEBT, built from `16-components`). */
    LevelTestIntro,
}

@Immutable
internal data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.Step1,
    val selectedLevel: Level = Level.A1,
)

internal sealed interface OnboardingIntent {
    data object Next : OnboardingIntent
    data object Skip : OnboardingIntent
    data object Back : OnboardingIntent

    /** A level chip of the level choice: saves the level and finishes the onboarding. */
    data class PickLevel(val level: Level) : OnboardingIntent

    /** "SEVİYE TESTİNE BAŞLA": opens the intro. */
    data object StartLevelTest : OnboardingIntent

    /** "Başla" of the intro: starts the level test run. */
    data object BeginLevelTest : OnboardingIntent

    /** The screen is no longer visible (the level test covers it, or the app went to the background). */
    data object ScreenStopped : OnboardingIntent
}

internal sealed interface OnboardingEffect {
    data object NavigateToHome : OnboardingEffect

    /** Open the level test run (`GAME_RULES.md` §9); nothing is saved before its result. */
    data object StartLevelTest : OnboardingEffect
}
