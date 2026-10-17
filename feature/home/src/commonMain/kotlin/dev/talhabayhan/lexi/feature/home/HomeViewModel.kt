package dev.talhabayhan.lexi.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.daily.DailyPlayState
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.stats.Stats
import dev.talhabayhan.lexi.core.domain.streak.GetStreakStatus
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.core.domain.time.civilDateOfEpochDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Home (`02-home`): streak, daily card, notebook and statistics numbers from the repositories; navigation as effects. */
internal class HomeViewModel(
    progress: ProgressRepository,
    getStreakStatus: GetStreakStatus,
    private val getDailyWheel: GetDailyWheel,
    private val daily: DailyRepository,
    private val appClock: AppClock,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = effectChannel.receiveAsFlow()

    private val today = MutableStateFlow(appClock.currentDay())

    init {
        viewModelScope.launch {
            combine(progress.observeAll(), getStreakStatus.observe(), today) { cards, streak, day ->
                val date = civilDateOfEpochDay(day)
                cards to (streak to date)
            }
                // A broken read keeps the last numbers (zeros at first); the menu itself still works.
                .catch { }
                .collect { (cards, streakAndDate) ->
                    val (streak, date) = streakAndDate
                    mutableState.update { current ->
                        current.copy(
                            streakDays = streak.currentStreak,
                            dueReviews = cards.count { it.isDue(today.value) },
                            learnedWords = Stats.learnedWords(cards),
                            dailyDay = date.day,
                            dailyMonth = date.month,
                        )
                    }
                }
        }

        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            today.flatMapLatest { day ->
                daily.observe(day).map { played ->
                    if (played != null) {
                        DailyCardState.AlreadyPlayed(played.score, calculateHoursUntilNext())
                    } else {
                        when (val playState = getDailyWheel(day)) {
                            is DailyPlayState.Ready -> DailyCardState.Available

                            is DailyPlayState.AlreadyPlayed ->
                                DailyCardState.AlreadyPlayed(playState.result.score, calculateHoursUntilNext())

                            is DailyPlayState.NoCards -> DailyCardState.Unavailable
                        }
                    }
                }
            }
                .catch { emit(DailyCardState.Unavailable) }
                .collect { cardState ->
                    mutableState.update { it.copy(dailyState = cardState) }
                }
        }
    }

    private fun AppClock.currentDay(): Long = epochDay(nowMs())

    private fun calculateHoursUntilNext(): Int = (appClock.msUntilNextDay() / 3_600_000L).toInt()

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Resumed -> {
                today.value = appClock.currentDay()
                val currentDaily = mutableState.value.dailyState
                if (currentDaily is DailyCardState.AlreadyPlayed) {
                    mutableState.update {
                        it.copy(dailyState = currentDaily.copy(hoursUntilNext = calculateHoursUntilNext()))
                    }
                }
            }

            HomeIntent.Play -> effectChannel.trySend(HomeEffect.NavigateToLevelSelect)

            HomeIntent.OpenDaily -> {
                if (mutableState.value.dailyState is DailyCardState.Available) {
                    effectChannel.trySend(HomeEffect.NavigateToDaily)
                }
            }

            HomeIntent.OpenNotebook -> effectChannel.trySend(HomeEffect.NavigateToNotebook)

            HomeIntent.OpenStats -> effectChannel.trySend(HomeEffect.NavigateToStats)

            HomeIntent.OpenSettings -> effectChannel.trySend(HomeEffect.NavigateToSettings)

            HomeIntent.OpenPractice -> effectChannel.trySend(HomeEffect.NavigateToPractice)

            HomeIntent.OpenExamPacks -> effectChannel.trySend(HomeEffect.NavigateToExamPacks)
        }
    }
}
