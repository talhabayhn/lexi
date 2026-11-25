package dev.talhabayhan.lexi.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.config.FeatureFlags
import dev.talhabayhan.lexi.core.domain.daily.DailyPlayState
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.joker.GrantDailyJokers
import dev.talhabayhan.lexi.core.domain.joker.JokerGrantReason
import dev.talhabayhan.lexi.core.domain.platform.ShareService
import dev.talhabayhan.lexi.core.domain.rating.RequestInAppReview
import dev.talhabayhan.lexi.core.domain.reminder.ShouldShowReminderPrePrompt
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.repository.ExamPackRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.stats.Stats
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Home of the video flow (`video-flow-notes.md` §1): total score, daily card, due reviews and the level "Çarkını seç"
 * opens on from the repositories; navigation as effects; the invite through the [shareService]. The streak, the daily
 * goal and the joker gifts still run (gift toast, reminders); only Home's UI shows no streak.
 */
internal class HomeViewModel(
    progress: ProgressRepository,
    sessions: SessionRepository,
    private val settings: SettingsRepository,
    private val getDailyWheel: GetDailyWheel,
    private val daily: DailyRepository,
    private val appClock: AppClock,
    private val grantDailyJokers: GrantDailyJokers,
    private val shouldShowReminderPrePrompt: ShouldShowReminderPrePrompt,
    private val requestInAppReview: RequestInAppReview,
    private val examPacks: ExamPackRepository,
    private val flags: FeatureFlags,
    private val shareService: ShareService,
    private val analytics: AnalyticsTracker,
) : ViewModel() {
    private val mutableState = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<HomeEffect>(Channel.BUFFERED)
    val effects: Flow<HomeEffect> = effectChannel.receiveAsFlow()

    private val today = MutableStateFlow(appClock.currentDay())

    /**
     * Completed real games when Home was last shown; null before its first appearance (app start or the end of the
     * onboarding), which is never a return from a game. [entryMutex] keeps two quick appearances in order.
     */
    private var realGamesSeen: Int? = null
    private val entryMutex = Mutex()
    private var noticeShown = false

    init {
        viewModelScope.launch {
            combine(progress.observeAll(), today) { cards, day -> cards.count { it.isDue(day) } }
                // A broken read keeps the last numbers (zeros at first); the menu itself still works.
                .catch { }
                .collect { due -> mutableState.update { it.copy(dueReviews = due) } }
        }

        viewModelScope.launch {
            sessions.observeRecords()
                .map(Stats::totalScore)
                .catch { }
                .collect { total -> mutableState.update { it.copy(totalScore = total) } }
        }

        viewModelScope.launch {
            settings.settings
                .map { it.selectedLevel }
                .distinctUntilChanged()
                .catch { }
                .collect { level -> mutableState.update { it.copy(selectedLevel = level) } }
        }

        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            today.flatMapLatest { day ->
                daily.observe(day).map { played ->
                    val (hours, minutes) = calculateTimeUntilNext()
                    if (played != null) {
                        DailyCardState.AlreadyPlayed(played.score, hours, minutes)
                    } else {
                        when (val playState = getDailyWheel(day)) {
                            is DailyPlayState.Ready -> DailyCardState.Available

                            is DailyPlayState.AlreadyPlayed ->
                                DailyCardState.AlreadyPlayed(playState.result.score, hours, minutes)

                            is DailyPlayState.NoCards -> DailyCardState.Unavailable
                        }
                    }
                }.catch { emit(DailyCardState.Unavailable) }
            }.collect { cardState ->
                mutableState.update { it.copy(dailyState = cardState) }
            }
        }

        viewModelScope.launch {
            // Only packs the player can open in this build (Pro packs stay out of release builds); a read failure keeps
            // the generic subtitle.
            val names = try {
                examPacks.packs().filter { flags.proEnabled || !it.isPro }.map { it.name }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                emptyList()
            }
            mutableState.update { it.copy(examPackNames = names) }
        }

        viewModelScope.launch {
            checkDailyGift()
        }
    }

    private fun AppClock.currentDay(): Long = epochDay(nowMs())

    private fun calculateTimeUntilNext(): Pair<Int, Int> {
        val ms = appClock.msUntilNextDay().coerceAtLeast(0L)
        val totalMinutes = ms / 60_000L
        val hours = (totalMinutes / 60).toInt()
        val minutes = (totalMinutes % 60).toInt()
        return hours to minutes
    }

    /**
     * Home appeared. Only when a real game was completed since its last appearance (back from a game's result) may the
     * pre-prompt open, at most once ever: an app start, an app foreground or a return from settings, notebook or stats
     * leaves the count unchanged.
     */
    private fun onEntered() {
        viewModelScope.launch {
            try {
                entryMutex.withLock {
                    val count = shouldShowReminderPrePrompt.realGameCount()
                    val previous = realGamesSeen
                    realGamesSeen = count
                    if (previous != null && count > previous) {
                        if (shouldShowReminderPrePrompt()) {
                            effectChannel.trySend(HomeEffect.NavigateToReminderPrePrompt)
                        } else {
                            // The store's own prompt (ADR 0024), never together with the reminder pre-prompt.
                            requestInAppReview()
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // No prompt is better than a broken home screen.
            }
        }
    }

    private suspend fun checkDailyGift() {
        try {
            val grants = grantDailyJokers()
            if (grants.isNotEmpty()) {
                val hasDaily = grants.any { it.reason == JokerGrantReason.DAILY_GIFT }
                val hasStreak = grants.any { it.reason == JokerGrantReason.STREAK_BONUS }
                val type = when {
                    hasDaily && hasStreak -> DailyGiftNoticeType.Combined
                    hasStreak -> DailyGiftNoticeType.Streak
                    else -> DailyGiftNoticeType.Daily
                }
                val amount = grants.size
                noticeShown = false
                mutableState.update { it.copy(dailyGiftNotice = DailyGiftNotice(type, amount)) }
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            // A gift error never blocks the home screen.
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Refresh, HomeIntent.Resumed -> {
                val nextDay = appClock.currentDay()
                if (today.value != nextDay) {
                    today.value = nextDay
                    viewModelScope.launch { checkDailyGift() }
                } else {
                    val currentDaily = mutableState.value.dailyState
                    if (currentDaily is DailyCardState.AlreadyPlayed) {
                        val (hours, minutes) = calculateTimeUntilNext()
                        if (currentDaily.hoursUntilNext != hours || currentDaily.minutesUntilNext != minutes) {
                            val updatedDaily = currentDaily.copy(
                                hoursUntilNext = hours,
                                minutesUntilNext = minutes,
                            )
                            mutableState.update { it.copy(dailyState = updatedDaily) }
                        }
                    }
                }
            }

            HomeIntent.Entered -> onEntered()

            HomeIntent.DismissNotice -> {
                noticeShown = false
                mutableState.update { it.copy(dailyGiftNotice = null) }
            }

            HomeIntent.NoticeShown -> noticeShown = mutableState.value.dailyGiftNotice != null

            HomeIntent.Left -> if (noticeShown) {
                noticeShown = false
                mutableState.update { it.copy(dailyGiftNotice = null) }
            }

            // The stored level once the store has delivered, even before this screen's collector did (a very early tap).
            HomeIntent.Play -> effectChannel.trySend(
                HomeEffect.NavigateToCategoryPicker(
                    settings.loaded.value?.selectedLevel ?: mutableState.value.selectedLevel,
                ),
            )

            HomeIntent.OpenDaily -> {
                if (mutableState.value.dailyState is DailyCardState.Available) {
                    effectChannel.trySend(HomeEffect.NavigateToDaily)
                }
            }

            HomeIntent.OpenNotebook -> effectChannel.trySend(HomeEffect.NavigateToNotebook)

            HomeIntent.OpenStats -> effectChannel.trySend(HomeEffect.NavigateToStats)

            HomeIntent.OpenSettings -> effectChannel.trySend(HomeEffect.NavigateToSettings)

            HomeIntent.OpenDuel -> effectChannel.trySend(HomeEffect.NavigateToDuel)

            HomeIntent.OpenExamPacks -> effectChannel.trySend(HomeEffect.NavigateToExamPacks)

            is HomeIntent.Invite -> invite(intent.message)
        }
    }

    /** One share sheet at a time; `share` (type invite) is tracked only when the sheet was shown. */
    private var inviting = false

    private fun invite(message: String) {
        if (inviting) return
        inviting = true
        viewModelScope.launch {
            val shown = try {
                shareService.shareText(message)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                false
            } finally {
                inviting = false
            }
            if (shown) analytics.track(AnalyticsEvent.Share(AnalyticsEvent.ShareType.INVITE))
        }
    }
}
