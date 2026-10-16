package dev.talhabayhan.lexi.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.StreakRepository
import dev.talhabayhan.lexi.core.domain.stats.Stats
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.core.domain.time.civilDateOfEpochDay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** Home (`02-home`): streak, notebook and statistics numbers from the repositories; navigation as effects. */
internal class HomeViewModel(progress: ProgressRepository, streaks: StreakRepository, private val appClock: AppClock) :
    ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = effectChannel.receiveAsFlow()

    private val today = MutableStateFlow(appClock.currentDay())

    init {
        viewModelScope.launch {
            combine(progress.observeAll(), streaks.observeDays(), today) { cards, days, day ->
                val date = civilDateOfEpochDay(day)
                mutableState.value.copy(
                    streakDays = Stats.currentStreak(days, day),
                    dueReviews = cards.count { it.isDue(day) },
                    learnedWords = Stats.learnedWords(cards),
                    dailyDay = date.day,
                    dailyMonth = date.month,
                )
            }
                // A broken read keeps the last numbers (zeros at first); the menu itself still works.
                .catch { }
                .collect { mutableState.value = it }
        }
    }

    private fun AppClock.currentDay(): Long = epochDay(nowMs())

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Resumed -> today.value = appClock.currentDay()

            HomeIntent.Play -> effectChannel.trySend(HomeEffect.NavigateToLevelSelect)

            HomeIntent.OpenNotebook -> effectChannel.trySend(HomeEffect.NavigateToNotebook)

            // Destinations land with their milestones (see HomeIntent); until then the tiles are inert.
            HomeIntent.OpenDaily,
            HomeIntent.OpenSettings,
            HomeIntent.OpenStats,
            HomeIntent.OpenPractice,
            HomeIntent.OpenExamPacks,
            -> Unit
        }
    }
}
