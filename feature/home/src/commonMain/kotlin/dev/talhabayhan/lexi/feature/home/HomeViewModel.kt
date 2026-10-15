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

    init {
        viewModelScope.launch {
            combine(progress.observeAll(), streaks.observeDays()) { cards, days ->
                val today = appClock.epochDay(appClock.nowMs())
                val date = civilDateOfEpochDay(today)
                mutableState.value.copy(
                    streakDays = currentStreak(days, today),
                    dueReviews = cards.count { it.isDue(today) },
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

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Play -> effectChannel.trySend(HomeEffect.NavigateToLevelSelect)

            // Destinations land with their milestones (see HomeIntent); until then the tiles are inert.
            HomeIntent.OpenDaily,
            HomeIntent.OpenSettings,
            HomeIntent.OpenNotebook,
            HomeIntent.OpenStats,
            HomeIntent.OpenPractice,
            HomeIntent.OpenExamPacks,
            -> Unit
        }
    }
}

/**
 * Length of the run of consecutive [days] (epoch days, any order) that ends today. A run ending yesterday still counts:
 * today's wheel is not played yet and the streak is not lost until the day is over. Streak rules proper land in M6.
 */
internal fun currentStreak(days: Collection<Long>, today: Long): Int {
    val played = days.toSet()
    var day = if (today in played) today else today - 1
    var length = 0
    while (day in played) {
        length++
        day--
    }
    return length
}
