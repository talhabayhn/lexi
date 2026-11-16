package dev.talhabayhan.lexi.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.daily.DailyPlayState
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.goal.GetDailyGoalProgress
import dev.talhabayhan.lexi.core.domain.joker.GrantDailyJokers
import dev.talhabayhan.lexi.core.domain.joker.JokerGrantReason
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.rating.RequestInAppReview
import dev.talhabayhan.lexi.core.domain.reminder.ShouldShowReminderPrePrompt
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.repository.DailyRepository
import dev.talhabayhan.lexi.core.domain.repository.ProgressRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.stats.GetLevelCategories
import dev.talhabayhan.lexi.core.domain.stats.Stats
import dev.talhabayhan.lexi.core.domain.streak.GetStreakStatus
import dev.talhabayhan.lexi.core.domain.telemetry.CrashReporter
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.core.domain.time.civilDateOfEpochDay
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Home (`02c-home-v2`): streak, daily card, due reviews from the repositories; the level, mode and category the player
 * picks here (stored, `home-v2-notes.md` §2) with each level's progress and the selected level's categories; navigation
 * and the game start as effects.
 */
internal class HomeViewModel(
    progress: ProgressRepository,
    getStreakStatus: GetStreakStatus,
    private val getDailyWheel: GetDailyWheel,
    private val daily: DailyRepository,
    private val appClock: AppClock,
    getDailyGoalProgress: GetDailyGoalProgress,
    private val grantDailyJokers: GrantDailyJokers,
    private val shouldShowReminderPrePrompt: ShouldShowReminderPrePrompt,
    private val requestInAppReview: RequestInAppReview,
    private val settings: SettingsRepository,
    private val cards: CardRepository,
    private val getLevelCategories: GetLevelCategories,
    private val crashReporter: CrashReporter,
) : ViewModel() {
    // The splash waits for the stored settings, so the first frame already shows the stored selection.
    private val mutableState = MutableStateFlow(HomeUiState().withSelection(settings.loaded.value ?: Settings()))
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

    /**
     * Until the player changes the selection here, a later stored value wins (e.g. the onboarding's level written
     * just before Home opened); afterwards Home's own state is the truth, so a slow write never flips a chip back.
     */
    private var selectionTouched = false

    /** The level whose categories are read; a change (or a retry) cancels the running read. */
    private val categoryLevel = MutableStateFlow(mutableState.value.selectedLevel)
    private val categoryReloads = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            combine(progress.observeAll(), getStreakStatus.observe(today), today) { answered, streak, day ->
                val date = civilDateOfEpochDay(day)
                answered to (streak to (date to day))
            }
                // A broken read keeps the last numbers (zeros at first); the menu itself still works.
                .catch { }
                .collect { (answered, streakAndDate) ->
                    val (streak, dateAndDay) = streakAndDate
                    val (date, day) = dateAndDay
                    mutableState.update { current ->
                        current.copy(
                            streakDays = streak.currentStreak,
                            dueReviews = answered.count { it.isDue(day) },
                            dailyDay = date.day,
                            dailyMonth = date.month,
                        )
                    }
                }
        }

        viewModelScope.launch {
            getDailyGoalProgress.observe(today)
                .catch { }
                .collect { goalProgress ->
                    mutableState.update { it.copy(dailyGoalProgress = goalProgress) }
                }
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

        viewModelScope.launch { observeLevelProgress(progress) }

        viewModelScope.launch {
            settings.settings.catch { }.collect { stored ->
                mutableState.update { current ->
                    if (selectionTouched) {
                        current.copy(classicTimeLimitSeconds = stored.classicTimeLimitSeconds)
                    } else {
                        current.withSelection(stored)
                    }
                }
                if (!selectionTouched && categoryLevel.value != stored.selectedLevel) {
                    categoryLevel.value = stored.selectedLevel
                    // A1 → A2 → A1 before the reader resumes would merge into "no change" (see selectLevel).
                    categoryReloads.update { it + 1 }
                }
            }
        }

        viewModelScope.launch { observeCategories(progress) }

        viewModelScope.launch {
            checkDailyGift()
        }
    }

    private fun AppClock.currentDay(): Long = epochDay(nowMs())

    /** Learned words per level against the level's card total; zeros when the content cannot be read. */
    private suspend fun observeLevelProgress(progress: ProgressRepository) {
        val index = try {
            cards.contentIndex()
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            crashReporter.recordException(e, "level content not read")
            return
        }
        progress.observeAll()
            // A broken progress read keeps the last numbers; Home itself still works.
            .catch { }
            .collect { answered ->
                val byLevel = Stats.levelProgress(answered, index.levelOf, index.totals)
                val levels = Level.entries.map { level ->
                    LevelProgressUi(level, byLevel[level]?.learned ?: 0, byLevel[level]?.total ?: 0)
                }.toImmutableList()
                mutableState.update { it.copy(levels = levels) }
            }
    }

    /**
     * The selected level's categories with the learned words (the sheet, `07-categories` data). An unreadable content
     * is [CategoriesState.Error]: the row then shows and plays Karışık; a stored category the level no longer has is
     * dropped (Karışık).
     */
    private suspend fun observeCategories(progress: ProgressRepository) {
        combine(categoryLevel, categoryReloads) { level, _ -> level }.collectLatest { level ->
            mutableState.update { it.copy(categories = CategoriesState.Loading) }
            val content = try {
                getLevelCategories(level)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                crashReporter.recordException(e, "category content not read")
                mutableState.update { it.copy(categories = CategoriesState.Error) }
                return@collectLatest
            }
            progress.observeAll()
                .catch { e -> crashReporter.recordException(e, "category progress not read") }
                .collect { answered ->
                    val list = content.words(answered).map { entry ->
                        val category = entry.category
                        CategoryUi(category.id, category.nameTr, category.nameEn, entry.learned, entry.total)
                    }.toImmutableList()
                    val dropped = mutableState.value.selectedCategory?.takeIf { id -> list.none { it.id == id } }
                    mutableState.update { current ->
                        val kept = current.selectedCategory?.takeIf { id -> list.any { it.id == id } }
                        current.copy(categories = CategoriesState.Loaded(list), selectedCategory = kept)
                    }
                    // Forget a stored category the level no longer has, so a later settings emission can't bring it back.
                    if (dropped != null && !selectionTouched) forgetCategory(dropped)
                }
        }
    }

    private fun HomeUiState.withSelection(stored: Settings): HomeUiState {
        val levelChanged = stored.selectedLevel != selectedLevel
        return copy(
            selectedLevel = stored.selectedLevel,
            selectedMode = stored.homeMode,
            selectedCategory = stored.homeCategory,
            categories = if (levelChanged) CategoriesState.Loading else categories,
            classicTimeLimitSeconds = stored.classicTimeLimitSeconds,
        )
    }

    /** Remembers the selection across restarts; a failed write only loses it for the next start. */
    private fun save(transform: (Settings) -> Settings) {
        selectionTouched = true
        viewModelScope.launch {
            try {
                settings.update(transform)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                crashReporter.recordException(e, "home selection not saved")
            }
        }
    }

    private fun forgetCategory(category: CategoryId) {
        viewModelScope.launch {
            try {
                settings.update { if (it.homeCategory == category) it.copy(homeCategory = null) else it }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                crashReporter.recordException(e, "home selection not saved")
            }
        }
    }

    private fun selectLevel(level: Level) {
        if (level == mutableState.value.selectedLevel) return
        mutableState.update {
            it.copy(selectedLevel = level, selectedCategory = null, categories = CategoriesState.Loading)
        }
        save { it.copy(selectedLevel = level, homeCategory = null) }
        categoryLevel.value = level
        // A1 → A2 → A1 before the reader resumes would merge into "no change" and leave the list on Loading.
        categoryReloads.update { it + 1 }
    }

    private fun selectMode(mode: GameMode) {
        if (mode !in Settings.HOME_MODES || mode == mutableState.value.selectedMode) return
        mutableState.update { it.copy(selectedMode = mode) }
        save { it.copy(homeMode = mode) }
    }

    private fun selectCategory(category: CategoryId?) {
        mutableState.update { it.copy(selectedCategory = category, categorySheetOpen = false) }
        save { it.copy(homeCategory = category) }
    }

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

            is HomeIntent.SelectLevel -> selectLevel(intent.level)

            is HomeIntent.SelectMode -> selectMode(intent.mode)

            HomeIntent.OpenCategories -> if (mutableState.value.showCategoryRow) {
                mutableState.update { it.copy(categorySheetOpen = true) }
            }

            HomeIntent.CloseCategories -> mutableState.update { it.copy(categorySheetOpen = false) }

            is HomeIntent.SelectCategory -> selectCategory(intent.category)

            HomeIntent.RetryCategories -> if (mutableState.value.categories == CategoriesState.Error) {
                categoryReloads.update { it + 1 }
            }

            HomeIntent.Play -> {
                val current = mutableState.value
                effectChannel.trySend(
                    HomeEffect.StartGame(current.selectedLevel, current.selectedMode, current.playCategory),
                )
            }

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
        }
    }
}
