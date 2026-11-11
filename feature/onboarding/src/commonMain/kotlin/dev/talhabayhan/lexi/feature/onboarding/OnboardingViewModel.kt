package dev.talhabayhan.lexi.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.OnboardingRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * First-run onboarding: three steps, then the level choice (`01-onboarding`). A level chip saves the level and
 * finishes; "SEVİYE TESTİNE BAŞLA" opens the intro, which starts reading the level test's cards right away
 * ([CardRepository.levelCards], kept in memory by the repository) so the test opens without a wait. Once the test
 * covers this screen ([OnboardingIntent.ScreenStopped]) the step goes back to the level choice, where "Pes et" and a
 * test that can't start return to (`GAME_RULES.md` §9, owner decision 2). The step and whether the level test was
 * opened live in [savedState], so a process death under the level test still returns to the level choice.
 */
internal class OnboardingViewModel(
    private val onboarding: OnboardingRepository,
    private val cards: CardRepository,
    private val settings: SettingsRepository,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        OnboardingUiState(
            currentStep = restoredStep(),
            analyticsConsent = settings.loaded.value?.analyticsConsent == true,
        ),
    )
    val state: StateFlow<OnboardingUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<OnboardingEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingEffect> = effectChannel.receiveAsFlow()

    private var warmUp: Job? = null
    private var finishing = false

    private var levelTestStarted: Boolean
        get() = savedState.get<Boolean>(LEVEL_TEST_STARTED_KEY) == true
        set(value) {
            savedState[LEVEL_TEST_STARTED_KEY] = value
        }

    init {
        // Restored under or after the level test: back to the level choice, as on a stop with the test opened.
        if (levelTestStarted) {
            levelTestStarted = false
            setStep(OnboardingStep.LevelChoice)
        }
        if (state.value.currentStep == OnboardingStep.LevelTestIntro) warmUpLevelCards()
        viewModelScope.launch {
            if (onboarding.onboardingCompleted.first()) {
                effectChannel.trySend(OnboardingEffect.NavigateToHome)
            }
        }
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.Next -> when (state.value.currentStep) {
                OnboardingStep.Step1 -> setStep(OnboardingStep.Step2)
                OnboardingStep.Step2 -> setStep(OnboardingStep.Step3)
                OnboardingStep.Step3 -> setStep(OnboardingStep.LevelChoice)
                OnboardingStep.LevelChoice, OnboardingStep.LevelTestIntro -> Unit
            }

            OnboardingIntent.Skip -> finish(Level.A1)

            is OnboardingIntent.SetAnalyticsConsent -> setAnalyticsConsent(intent.enabled)

            OnboardingIntent.OpenPrivacy -> effectChannel.trySend(OnboardingEffect.OpenUrl(AppLinks.PRIVACY_POLICY))

            OnboardingIntent.Back -> when (state.value.currentStep) {
                OnboardingStep.LevelTestIntro -> setStep(OnboardingStep.LevelChoice)
                OnboardingStep.LevelChoice -> setStep(OnboardingStep.Step3)
                OnboardingStep.Step3 -> setStep(OnboardingStep.Step2)
                OnboardingStep.Step2 -> setStep(OnboardingStep.Step1)
                OnboardingStep.Step1 -> Unit
            }

            is OnboardingIntent.PickLevel -> {
                mutableState.update { it.copy(selectedLevel = intent.level) }
                finish(intent.level)
            }

            OnboardingIntent.StartLevelTest -> {
                if (state.value.currentStep != OnboardingStep.LevelChoice) return
                setStep(OnboardingStep.LevelTestIntro)
                warmUpLevelCards()
            }

            OnboardingIntent.BeginLevelTest -> {
                if (state.value.currentStep != OnboardingStep.LevelTestIntro || finishing) return
                effectChannel.trySend(OnboardingEffect.StartLevelTest)
            }

            OnboardingIntent.LevelTestOpened -> levelTestStarted = true

            OnboardingIntent.ScreenStopped -> if (levelTestStarted) {
                levelTestStarted = false
                setStep(OnboardingStep.LevelChoice)
            }
        }
    }

    private fun restoredStep(): OnboardingStep = savedState.get<String>(STEP_KEY)?.let { name ->
        OnboardingStep.entries.firstOrNull { it.name == name }
    } ?: OnboardingStep.Step1

    private fun setStep(step: OnboardingStep) {
        savedState[STEP_KEY] = step.name
        mutableState.update { it.copy(currentStep = step) }
    }

    private fun setAnalyticsConsent(enabled: Boolean) {
        mutableState.update { it.copy(analyticsConsent = enabled) }
        viewModelScope.launch {
            try {
                settings.update { it.copy(analyticsConsent = enabled) }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // Not stored: the checkbox shows what is stored.
                mutableState.update { it.copy(analyticsConsent = settings.loaded.value?.analyticsConsent == true) }
            }
        }
    }

    private fun finish(level: Level) {
        if (finishing) return
        finishing = true
        viewModelScope.launch {
            try {
                onboarding.saveSelectedLevel(level)
                onboarding.setOnboardingCompleted(true)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // Not stored (e.g. a full disk): stay on this step so the player can try again.
                crashReporter.recordException(e, "onboarding not saved")
                finishing = false
                return@launch
            }
            analytics.track(AnalyticsEvent.OnboardingComplete)
            effectChannel.trySend(OnboardingEffect.NavigateToHome)
        }
    }

    /** Once per screen: a failed read is left to the level test, which reports it. */
    private fun warmUpLevelCards() {
        if (warmUp != null) return
        warmUp = viewModelScope.launch {
            try {
                cards.levelCards()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Nothing to do: the level test reads the cards again and shows its load error.
            }
        }
    }
}

private const val STEP_KEY = "step"
private const val LEVEL_TEST_STARTED_KEY = "levelTestStarted"
