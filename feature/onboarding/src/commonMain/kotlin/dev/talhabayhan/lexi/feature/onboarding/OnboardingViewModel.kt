package dev.talhabayhan.lexi.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.OnboardingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class OnboardingViewModel(private val onboarding: OnboardingRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingEffect> = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            if (onboarding.onboardingCompleted.first()) {
                effectChannel.trySend(OnboardingEffect.NavigateToHome)
            }
        }
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.Next -> {
                mutableState.update { current ->
                    when (current.currentStep) {
                        OnboardingStep.Step1 -> current.copy(currentStep = OnboardingStep.Step2)
                        OnboardingStep.Step2 -> current.copy(currentStep = OnboardingStep.Step3)
                        OnboardingStep.Step3 -> current.copy(currentStep = OnboardingStep.LevelChoice)
                        OnboardingStep.LevelChoice, OnboardingStep.LevelPicker -> current
                    }
                }
            }

            OnboardingIntent.Skip -> {
                viewModelScope.launch {
                    onboarding.saveSelectedLevel(Level.A1)
                    onboarding.setOnboardingCompleted(true)
                    effectChannel.trySend(OnboardingEffect.NavigateToHome)
                }
            }

            OnboardingIntent.OpenLevelPicker -> {
                mutableState.update { it.copy(currentStep = OnboardingStep.LevelPicker) }
            }

            OnboardingIntent.Back -> {
                mutableState.update { current ->
                    when (current.currentStep) {
                        OnboardingStep.LevelPicker -> current.copy(currentStep = OnboardingStep.LevelChoice)
                        OnboardingStep.LevelChoice -> current.copy(currentStep = OnboardingStep.Step3)
                        OnboardingStep.Step3 -> current.copy(currentStep = OnboardingStep.Step2)
                        OnboardingStep.Step2 -> current.copy(currentStep = OnboardingStep.Step1)
                        OnboardingStep.Step1 -> current
                    }
                }
            }

            is OnboardingIntent.PickLevel -> {
                mutableState.update { it.copy(selectedLevel = intent.level) }
                viewModelScope.launch {
                    onboarding.saveSelectedLevel(intent.level)
                    onboarding.setOnboardingCompleted(true)
                    effectChannel.trySend(OnboardingEffect.NavigateToHome)
                }
            }

            OnboardingIntent.StartLevelTest -> {
                if (LEVEL_TEST_AVAILABLE) {
                    // TODO(B5): Wire level test once implemented
                }
            }
        }
    }
}
