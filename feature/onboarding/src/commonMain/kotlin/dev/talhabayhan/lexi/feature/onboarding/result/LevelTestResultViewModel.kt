package dev.talhabayhan.lexi.feature.onboarding.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.OnboardingRepository
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The level test result (`GAME_RULES.md` §9, owner decisions 3 and 4). The level is saved through
 * [OnboardingRepository.saveSelectedLevel] (what the level select preselects), then the onboarding is marked
 * completed and the app goes Home; no game starts. One tap finishes: later taps are ignored. A failed save is
 * reported and the page stays, so the player can tap again.
 */
internal class LevelTestResultViewModel(
    route: LevelTestResultRoute,
    private val onboarding: OnboardingRepository,
    private val analytics: AnalyticsTracker,
    private val crashReporter: CrashReporter,
    private val savedState: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        LevelTestResultUiState(
            level = route.placement,
            page = savedState.get<String>(PAGE_KEY)?.let { name ->
                LevelTestResultPage.entries.firstOrNull { it.name == name }
            } ?: LevelTestResultPage.Result,
        ),
    )
    val state: StateFlow<LevelTestResultUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<LevelTestResultEffect>(Channel.BUFFERED)
    val effects: Flow<LevelTestResultEffect> = effectChannel.receiveAsFlow()

    private var finishing = false

    fun onIntent(intent: LevelTestResultIntent) {
        when (intent) {
            LevelTestResultIntent.Start -> if (state.value.page == LevelTestResultPage.Result) finish(state.value.level)

            LevelTestResultIntent.ChangeLevel -> if (!finishing) {
                setPage(LevelTestResultPage.Picker)
            }

            is LevelTestResultIntent.PickLevel -> {
                if (state.value.page == LevelTestResultPage.Picker) finish(intent.level)
            }

            LevelTestResultIntent.Back -> when {
                finishing -> Unit

                state.value.page == LevelTestResultPage.Picker ->
                    setPage(LevelTestResultPage.Result)

                else -> effectChannel.trySend(LevelTestResultEffect.NavigateBack)
            }
        }
    }

    private fun setPage(page: LevelTestResultPage) {
        savedState[PAGE_KEY] = page.name
        mutableState.update { it.copy(page = page) }
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
                // Not stored (e.g. a full disk): stay on this page so the player can try again.
                crashReporter.recordException(e, "level test result not saved")
                finishing = false
                return@launch
            }
            analytics.track(AnalyticsEvent.OnboardingComplete)
            effectChannel.send(LevelTestResultEffect.NavigateToHome)
        }
    }
}

private const val PAGE_KEY = "page"
