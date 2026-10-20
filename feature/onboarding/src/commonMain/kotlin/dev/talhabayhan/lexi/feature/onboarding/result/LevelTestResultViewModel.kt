package dev.talhabayhan.lexi.feature.onboarding.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.OnboardingRepository
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
 * completed and the app goes Home; no game starts. One tap finishes: later taps are ignored.
 */
internal class LevelTestResultViewModel(route: LevelTestResultRoute, private val onboarding: OnboardingRepository) :
    ViewModel() {
    private val mutableState = MutableStateFlow(LevelTestResultUiState(level = route.placement))
    val state: StateFlow<LevelTestResultUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<LevelTestResultEffect>(Channel.BUFFERED)
    val effects: Flow<LevelTestResultEffect> = effectChannel.receiveAsFlow()

    private var finishing = false

    fun onIntent(intent: LevelTestResultIntent) {
        when (intent) {
            LevelTestResultIntent.Start -> if (state.value.page == LevelTestResultPage.Result) finish(state.value.level)

            LevelTestResultIntent.ChangeLevel -> if (!finishing) {
                mutableState.update { it.copy(page = LevelTestResultPage.Picker) }
            }

            is LevelTestResultIntent.PickLevel -> {
                if (state.value.page == LevelTestResultPage.Picker) finish(intent.level)
            }

            LevelTestResultIntent.Back -> when {
                finishing -> Unit

                state.value.page == LevelTestResultPage.Picker ->
                    mutableState.update { it.copy(page = LevelTestResultPage.Result) }

                else -> effectChannel.trySend(LevelTestResultEffect.NavigateBack)
            }
        }
    }

    private fun finish(level: Level) {
        if (finishing) return
        finishing = true
        viewModelScope.launch {
            onboarding.saveSelectedLevel(level)
            onboarding.setOnboardingCompleted(true)
            effectChannel.send(LevelTestResultEffect.NavigateToHome)
        }
    }
}
