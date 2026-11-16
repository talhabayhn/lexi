package dev.talhabayhan.lexi.feature.home

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.goal.DailyGoalProgress
import dev.talhabayhan.lexi.core.domain.goal.GetDailyGoalProgress
import dev.talhabayhan.lexi.core.domain.joker.GrantDailyJokers
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.rating.RequestInAppReview
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import dev.talhabayhan.lexi.core.domain.reminder.ReminderPrePromptPolicy
import dev.talhabayhan.lexi.core.domain.reminder.ShouldShowReminderPrePrompt
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.stats.GetLevelCategories
import dev.talhabayhan.lexi.core.domain.streak.GetStreakStatus
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeCategoryRepository
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
import dev.talhabayhan.lexi.core.testing.FakeJokerRepository
import dev.talhabayhan.lexi.core.testing.FakeNotificationPermission
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeReviewRequester
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import dev.talhabayhan.lexi.core.testing.testAlphabetPool
import dev.talhabayhan.lexi.core.testing.testCard
import dev.talhabayhan.lexi.core.testing.testCategory
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val DAY_MS = 86_400_000L

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val clock = FakeAppClock()
    private val today = clock.epochDay(clock.nowMs())
    private val progress = FakeProgressRepository()
    private val streaks = FakeStreakRepository()
    private val jokers = FakeJokerRepository()
    private val cards = FakeCardRepository()
    private val contentVersion = FakeContentVersionRepository()
    private val daily = FakeDailyRepository()
    private val sessions = FakeSessionRepository()
    private val settings = FakeSettingsRepository()
    private val getStreakStatus = GetStreakStatus(streaks, clock)
    private val getDailyWheel = GetDailyWheel(cards, contentVersion, daily, clock)
    private val getDailyGoalProgress = GetDailyGoalProgress(sessions, settings, clock)
    private val grantDailyJokers = GrantDailyJokers(jokers, streaks, clock, GameConfig())
    private val categories = FakeCategoryRepository(
        listOf(
            testCategory("mixed", "Karışık", "Mixed", sort = 0),
            testCategory("food-drink", "Yiyecek ve içecek", "Food and drink", sort = 1),
            testCategory("home", "Ev ve eşyalar", "Home", setOf(Level.A1, Level.A2), sort = 2),
            testCategory("animals", "Hayvanlar", "Animals", setOf(Level.A2), sort = 3),
            testCategory("general", "Genel", "General", setOf(Level.NAT), sort = 9),
        ),
    )
    private val crashReporter = FakeCrashReporter()
    private val permission = FakeNotificationPermission(PermissionStatus.CAN_ASK)
    private val shouldShowPrePrompt = ShouldShowReminderPrePrompt(settings, sessions, permission, clock)
    private val reviewRequester = FakeReviewRequester()
    private val requestInAppReview =
        RequestInAppReview(settings, sessions, reviewRequester, FakeAnalyticsTracker(), clock)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        // As in the content: NAT cards are all in the neutral "general" bucket (no category selection).
        cards.cards = Level.entries.flatMap { level ->
            testAlphabetPool(level, category = if (level == Level.NAT) "general" else "food-drink")
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): HomeViewModel = HomeViewModel(
        progress,
        getStreakStatus,
        getDailyWheel,
        daily,
        clock,
        getDailyGoalProgress,
        grantDailyJokers,
        shouldShowPrePrompt,
        requestInAppReview,
        settings,
        cards,
        GetLevelCategories(cards, categories),
        crashReporter,
    )

    private fun runHomeTest(block: suspend TestScope.() -> Unit) = runTest {
        block()
    }

    private fun card(id: String, box: Int? = null, due: Long? = null, correct: Int = 0) =
        CardProgress(CardId(id), box = box, dueEpochDay = due, seen = 1, correct = correct)

    @Test
    fun `nothing played yet shows real zeros and today's date`() = runHomeTest {
        viewModel().state.test {
            val state = awaitItem()
            assertEquals(0, state.streakDays)
            assertEquals(0, state.dueReviews)
            assertEquals(15, state.dailyDay)
            assertEquals(10, state.dailyMonth)
            assertEquals(DailyCardState.Available, state.dailyState)
        }
    }

    @Test
    fun `numbers come from the repositories and follow their changes`() = runHomeTest {
        progress.save(
            listOf(
                card("a", correct = 2),
                card("b", box = 0, due = today, correct = 1),
                card("c", box = 1, due = today + 1),
            ),
        )
        streaks.days = listOf(today - 1, today)

        viewModel().state.test {
            val state = expectMostRecentItem()
            assertEquals(2, state.streakDays)
            assertEquals(1, state.dueReviews)

            streaks.days = listOf(today - 2, today - 1, today)
            assertEquals(3, awaitItem().streakDays)
        }
    }

    @Test
    fun `numbers move on to the new day when the screen is resumed after midnight`() = runHomeTest {
        progress.save(listOf(card("b", box = 0, due = today + 1, correct = 1)))
        streaks.days = listOf(today)
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(15, before.dailyDay)
            assertEquals(0, before.dueReviews)
            assertEquals(1, before.streakDays)

            clock.advanceBy(FakeAppClock.MS_PER_DAY)
            viewModel.onIntent(HomeIntent.Refresh)

            val after = expectMostRecentItem()
            assertEquals(16, after.dailyDay)
            assertEquals(1, after.dueReviews)
            // Yesterday was played, today is still open: the streak is alive.
            assertEquals(1, after.streakDays)
        }
    }

    @Test
    fun `streak and daily card recompute on refresh when midnight passes while app is open`() = runHomeTest {
        streaks.days = listOf(today)
        daily.record(DailyResult(today, Level.A1, score = 150, timeMs = 45000))
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(1, before.streakDays)
            assertEquals(15, before.dailyDay)
            assertEquals(
                DailyCardState.AlreadyPlayed(150, hoursUntilNext = 12, minutesUntilNext = 0),
                before.dailyState,
            )

            // Advance past midnight and drive periodic refresh
            val msToMidnight = clock.msUntilNextDay()
            clock.advanceBy(msToMidnight + 1000L)
            viewModel.onIntent(HomeIntent.Refresh)

            val after = expectMostRecentItem()
            assertEquals(16, after.dailyDay)
            assertEquals(1, after.streakDays) // streak alive from yesterday
            assertEquals(DailyCardState.Available, after.dailyState) // new daily available!
        }
    }

    @Test
    fun `streak badge resets on resume after a missed day`() = runHomeTest {
        // Played today - 1 (alive), then advance 2 days without playing (missed)
        streaks.days = listOf(today)
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(1, before.streakDays)

            clock.advanceBy(FakeAppClock.MS_PER_DAY * 2)
            viewModel.onIntent(HomeIntent.Refresh)

            val after = expectMostRecentItem()
            assertEquals(0, after.streakDays) // missed day resets streak
        }
    }

    @Test
    fun `daily wheel state is available when ready and sends NavigateToDaily on OpenDaily`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(DailyCardState.Available, awaitItem().dailyState)
        }
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDaily)
            assertEquals(HomeEffect.NavigateToDaily, awaitItem())
        }
    }

    @Test
    fun `daily wheel state is already played with score and minute resolution countdown`() = runHomeTest {
        daily.record(DailyResult(today, Level.A1, score = 320, timeMs = 85000))
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(
                DailyCardState.AlreadyPlayed(320, hoursUntilNext = 12, minutesUntilNext = 0),
                awaitItem().dailyState,
            )
        }
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDaily)
            expectNoEvents()
        }

        // Advance 2 hours 15 minutes, refresh -> 9 hours 45 minutes
        clock.advanceBy(2 * 3_600_000L + 15 * 60_000L)
        viewModel.onIntent(HomeIntent.Refresh)
        viewModel.state.test {
            assertEquals(
                DailyCardState.AlreadyPlayed(320, hoursUntilNext = 9, minutesUntilNext = 45),
                awaitItem().dailyState,
            )
        }
    }

    @Test
    fun `daily card countdown under one hour shows minute resolution`() = runHomeTest {
        daily.record(DailyResult(today, Level.A1, score = 200, timeMs = 30000))
        val viewModel = viewModel()
        val msUntilMidnight = clock.msUntilNextDay()
        clock.advanceBy(msUntilMidnight - 45 * 60_000L)
        viewModel.onIntent(HomeIntent.Refresh)

        viewModel.state.test {
            assertEquals(
                DailyCardState.AlreadyPlayed(200, hoursUntilNext = 0, minutesUntilNext = 45),
                expectMostRecentItem().dailyState,
            )
        }
    }

    @Test
    fun `daily card countdown handles day with DST transition`() = runHomeTest {
        val dstClock = object : AppClock {
            var now = 10_000_000L
            override fun nowMs(): Long = now
            override fun epochDay(atMs: Long): Long = 100L
            override fun msUntilNextDay(atMs: Long): Long = 82_800_000L - atMs // 23h DST spring day
        }
        val getDaily = GetDailyWheel(cards, contentVersion, daily, dstClock)
        daily.record(DailyResult(100L, Level.A1, score = 180, timeMs = 20000))
        val vm = HomeViewModel(
            progress,
            GetStreakStatus(streaks, dstClock),
            getDaily,
            daily,
            dstClock,
            GetDailyGoalProgress(sessions, settings, dstClock),
            GrantDailyJokers(jokers, streaks, dstClock, GameConfig()),
            shouldShowPrePrompt,
            requestInAppReview,
            settings,
            cards,
            GetLevelCategories(cards, categories),
            crashReporter,
        )
        vm.state.test {
            val state = awaitItem()
            val dailyState = state.dailyState as DailyCardState.AlreadyPlayed
            assertEquals(20, dailyState.hoursUntilNext)
            assertEquals(13, dailyState.minutesUntilNext)
        }
    }

    @Test
    fun `daily wheel state is unavailable when no cards exist and OpenDaily is inert`() = runHomeTest {
        cards.cards = emptyList()
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(DailyCardState.Unavailable, awaitItem().dailyState)
        }
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDaily)
            expectNoEvents()
        }
    }

    @Test
    fun `play starts a game of the stored selection right away`() = runHomeTest {
        settings.update {
            it.copy(selectedLevel = Level.A1, homeMode = GameMode.HARDCORE, homeCategory = CategoryId("food-drink"))
        }
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.Play)

            assertEquals(HomeEffect.StartGame(Level.A1, GameMode.HARDCORE, CategoryId("food-drink")), awaitItem())
        }
    }

    @Test
    fun `the notebook key opens the notebook`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenNotebook)

            assertEquals(HomeEffect.NavigateToNotebook, awaitItem())
        }
    }

    @Test
    fun `the statistics key opens the statistics`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenStats)

            assertEquals(HomeEffect.NavigateToStats, awaitItem())
        }
    }

    @Test
    fun `the gear opens the settings`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenSettings)

            assertEquals(HomeEffect.NavigateToSettings, awaitItem())
        }
    }

    @Test
    fun `the duel key opens the duel setup`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDuel)

            assertEquals(HomeEffect.NavigateToDuel, awaitItem())
        }
    }

    @Test
    fun `the exams key opens the exam packs page`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenExamPacks)

            assertEquals(HomeEffect.NavigateToExamPacks, awaitItem())
        }
    }

    @Test
    fun `the first time home is shown never opens the reminder pre-prompt`() = runHomeTest {
        sessions.record(session())
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(HomeIntent.Entered)
            expectNoEvents()
        }
    }

    @Test
    fun `returning to home after the second real game opens the reminder pre-prompt and not after the first`() =
        runHomeTest {
            val vm = viewModel()
            vm.effects.test {
                vm.onIntent(HomeIntent.Entered)
                sessions.record(session())
                vm.onIntent(HomeIntent.Entered)
                expectNoEvents()
                sessions.record(session())
                vm.onIntent(HomeIntent.Entered)
                assertEquals(HomeEffect.NavigateToReminderPrePrompt, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `after not now the pre-prompt comes once more after seven days and then never`() = runHomeTest {
        sessions.record(session())
        sessions.record(session())
        settings.recordReminderPrePromptAsked(clock.nowMs())
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(HomeIntent.Entered)
            sessions.record(session())
            vm.onIntent(HomeIntent.Entered)
            expectNoEvents()

            clock.advanceBy(ReminderPrePromptPolicy.SECOND_ASK_DELAY_MS)
            sessions.record(session())
            vm.onIntent(HomeIntent.Entered)
            assertEquals(HomeEffect.NavigateToReminderPrePrompt, awaitItem())

            settings.recordReminderPrePromptAsked(clock.nowMs())
            clock.advanceBy(ReminderPrePromptPolicy.SECOND_ASK_DELAY_MS)
            sessions.record(session())
            vm.onIntent(HomeIntent.Entered)
            expectNoEvents()
        }
    }

    @Test
    fun `an app foreground or a return from another screen without a new game never opens the pre-prompt`() =
        runHomeTest {
            // Regression: every STARTED (app foreground included) counted as a return to Home.
            sessions.record(session())
            val vm = viewModel()
            vm.effects.test {
                repeat(3) { vm.onIntent(HomeIntent.Entered) }
                expectNoEvents()
            }
        }

    @Test
    fun `returning to home after the fifth real game asks the store for a rating once per window`() = runHomeTest {
        settings.update { it.copy(reminderPrePromptAsks = 2, firstLaunchMs = clock.nowMs() - 5 * DAY_MS) }
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(HomeIntent.Entered)
            repeat(4) { sessions.record(session()) }
            vm.onIntent(HomeIntent.Entered)
            assertEquals(0, reviewRequester.requests)
            sessions.record(session())
            vm.onIntent(HomeIntent.Entered)
            assertEquals(1, reviewRequester.requests)
            sessions.record(session())
            vm.onIntent(HomeIntent.Entered)
            assertEquals(1, reviewRequester.requests)
            expectNoEvents()
        }
    }

    @Test
    fun `an app foreground or a failed game never asks for a rating`() = runHomeTest {
        settings.update { it.copy(reminderPrePromptAsks = 2, firstLaunchMs = clock.nowMs() - 5 * DAY_MS) }
        repeat(5) { sessions.record(session()) }
        val vm = viewModel()
        vm.effects.test {
            repeat(3) { vm.onIntent(HomeIntent.Entered) }
            sessions.record(session(completed = false))
            vm.onIntent(HomeIntent.Entered)
            assertEquals(0, reviewRequester.requests)
        }
    }

    @Test
    fun `the reminder pre-prompt takes precedence over the rating request`() = runHomeTest {
        settings.update { it.copy(firstLaunchMs = clock.nowMs() - 5 * DAY_MS) }
        repeat(4) { sessions.record(session()) }
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(HomeIntent.Entered)
            sessions.record(session())
            vm.onIntent(HomeIntent.Entered)
            assertEquals(HomeEffect.NavigateToReminderPrePrompt, awaitItem())
            assertEquals(0, reviewRequester.requests)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `returning to home after a level test does not open the reminder pre-prompt`() = runHomeTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(HomeIntent.Entered)
            sessions.record(session(mode = GameMode.LEVEL_TEST))
            vm.onIntent(HomeIntent.Entered)
            expectNoEvents()
        }
    }

    @Test
    fun `the reminder pre-prompt is skipped once shown or with the reminder on or the permission granted`() =
        runHomeTest {
            val vm = viewModel()
            vm.effects.test {
                vm.onIntent(HomeIntent.Entered)
                settings.update { it.copy(reminderPrePromptAsks = 2) }
                sessions.record(session())
                sessions.record(session())
                vm.onIntent(HomeIntent.Entered)
                expectNoEvents()
                settings.update { it.copy(reminderPrePromptAsks = 0, dailyReminder = true) }
                sessions.record(session())
                vm.onIntent(HomeIntent.Entered)
                expectNoEvents()
                settings.update { it.copy(dailyReminder = false) }
                permission.current = PermissionStatus.GRANTED
                sessions.record(session())
                vm.onIntent(HomeIntent.Entered)
                expectNoEvents()
            }
        }

    /** Every helper session is its own game (a repeated mode + seed + start is the same game, stored once). */
    private var nextSeed = 1L

    private fun session(mode: GameMode = GameMode.CLASSIC, day: Long = today, completed: Boolean = true) =
        SessionResult(
            mode = mode,
            level = Level.A1,
            category = CategoryId("basics"),
            seed = nextSeed++,
            startedAtMs = clock.nowMs() - 60_000L,
            endedAtMs = clock.nowMs(),
            epochDay = day,
            score = 200,
            correct = 20,
            wrong = 3,
            passed = 3,
            completed = completed,
        )

    @Test
    fun `daily goal progress reflects completed wheels and updates on session record`() = runHomeTest {
        settings.update { it.copy(dailyGoalWheels = 2) }
        val viewModel = viewModel()
        viewModel.state.test {
            val initial = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 0, goal = 2, reached = false), initial.dailyGoalProgress)

            sessions.record(session(completed = true))
            val afterOne = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 1, goal = 2, reached = false), afterOne.dailyGoalProgress)

            sessions.record(session(completed = true))
            val reached = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 2, goal = 2, reached = true), reached.dailyGoalProgress)
        }
    }

    @Test
    fun `daily goal progress updates when settings goal is changed`() = runHomeTest {
        sessions.record(session(completed = true))
        val viewModel = viewModel()
        viewModel.state.test {
            val initial = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 1, goal = 1, reached = true), initial.dailyGoalProgress)

            settings.update { it.copy(dailyGoalWheels = 3) }
            val updated = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 1, goal = 3, reached = false), updated.dailyGoalProgress)
        }
    }

    @Test
    fun `daily goal progress resets today count after midnight refresh`() = runHomeTest {
        settings.update { it.copy(dailyGoalWheels = 2) }
        sessions.record(session(day = today, completed = true))
        val viewModel = viewModel()
        viewModel.state.test {
            val before = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 1, goal = 2, reached = false), before.dailyGoalProgress)

            // Advance past midnight and trigger refresh
            val msToMidnight = clock.msUntilNextDay()
            clock.advanceBy(msToMidnight + 1000L)
            viewModel.onIntent(HomeIntent.Refresh)

            val after = expectMostRecentItem()
            assertEquals(DailyGoalProgress(done = 0, goal = 2, reached = false), after.dailyGoalProgress)
        }
    }

    @Test
    fun `daily gift notice shows once when grant is returned and dismisses on intent`() = runHomeTest {
        val vm = viewModel()
        vm.state.test {
            val state = expectMostRecentItem()
            assertEquals(DailyGiftNotice(DailyGiftNoticeType.Daily, 1), state.dailyGiftNotice)

            vm.onIntent(HomeIntent.DismissNotice)
            val dismissed = expectMostRecentItem()
            assertEquals(null, dismissed.dailyGiftNotice)
        }
    }

    @Test
    fun `no grant shows no daily gift notice`() = runHomeTest {
        // Pre-grant for today so GrantDailyJokers returns emptyList()
        grantDailyJokers()
        val vm = viewModel()
        vm.state.test {
            val state = expectMostRecentItem()
            assertEquals(null, state.dailyGiftNotice)
        }
    }

    @Test
    fun `daily gift notice is not re-shown on the same day after refresh`() = runHomeTest {
        val vm = viewModel()
        vm.state.test {
            val state = expectMostRecentItem()
            assertEquals(DailyGiftNotice(DailyGiftNoticeType.Daily, 1), state.dailyGiftNotice)

            // Dismiss the notice
            vm.onIntent(HomeIntent.DismissNotice)
            assertEquals(null, expectMostRecentItem().dailyGiftNotice)

            // Refresh on the same day: should not re-show
            vm.onIntent(HomeIntent.Refresh)
            expectNoEvents()
            assertEquals(null, vm.state.value.dailyGiftNotice)
        }
    }

    @Test
    fun `shown gift notice stays while visible and is gone after leaving and returning`() = runHomeTest {
        val vm = viewModel()
        vm.state.test {
            assertEquals(DailyGiftNotice(DailyGiftNoticeType.Daily, 1), expectMostRecentItem().dailyGiftNotice)
            vm.onIntent(HomeIntent.NoticeShown)
            expectNoEvents()
            assertEquals(DailyGiftNotice(DailyGiftNoticeType.Daily, 1), vm.state.value.dailyGiftNotice)

            vm.onIntent(HomeIntent.Left)
            assertEquals(null, expectMostRecentItem().dailyGiftNotice)

            vm.onIntent(HomeIntent.Entered)
            vm.onIntent(HomeIntent.Refresh)
            assertEquals(null, vm.state.value.dailyGiftNotice)
        }
    }

    @Test
    fun `leaving before the notice was shown keeps it`() = runHomeTest {
        val vm = viewModel()
        vm.state.test {
            expectMostRecentItem()
            vm.onIntent(HomeIntent.Left)
            assertEquals(DailyGiftNotice(DailyGiftNoticeType.Daily, 1), vm.state.value.dailyGiftNotice)
        }
    }

    @Test
    fun `next day gift shows again after the previous one was consumed`() = runHomeTest {
        val vm = viewModel()
        vm.state.test {
            expectMostRecentItem()
            vm.onIntent(HomeIntent.NoticeShown)
            vm.onIntent(HomeIntent.Left)
            assertEquals(null, expectMostRecentItem().dailyGiftNotice)

            clock.advanceBy(clock.msUntilNextDay() + 1000L)
            vm.onIntent(HomeIntent.Refresh)
            assertEquals(DailyGiftNotice(DailyGiftNoticeType.Daily, 1), expectMostRecentItem().dailyGiftNotice)
        }
    }

    // --- Level, mode and category on Home (02c-home-v2, home-v2-notes.md §2–§3) ---

    private fun ready(state: HomeUiState) = state.categories !is CategoriesState.Loading

    @Test
    fun `the stored selection is there from the first state`() = runHomeTest {
        settings.update {
            it.copy(selectedLevel = Level.A2, homeMode = GameMode.PRACTICE, homeCategory = CategoryId("food-drink"))
        }
        val first = viewModel().state.value

        assertEquals(Level.A2, first.selectedLevel)
        assertEquals(GameMode.PRACTICE, first.selectedMode)
        assertEquals(CategoryId("food-drink"), first.selectedCategory)
    }

    @Test
    fun `a new player lands on the onboarding's level choice`() = runHomeTest {
        // OnboardingRepository.saveSelectedLevel writes the same stored key that Settings.selectedLevel reads.
        settings.update { it.copy(selectedLevel = Level.B1) }

        val state = viewModel().state.value

        assertEquals(Level.B1, state.selectedLevel)
        assertEquals(GameMode.CLASSIC, state.selectedMode)
        assertEquals(null, state.selectedCategory)
    }

    @Test
    fun `a level stored just after home opened still wins until the player picks one`() = runHomeTest {
        val viewModel = viewModel()
        settings.update { it.copy(selectedLevel = Level.C1) }
        assertEquals(Level.C1, viewModel.state.value.selectedLevel)

        viewModel.onIntent(HomeIntent.SelectLevel(Level.A2))
        // A later stored value no longer moves Home's own choice.
        settings.update { it.copy(selectedLevel = Level.NAT) }
        assertEquals(Level.A2, viewModel.state.value.selectedLevel)
    }

    @Test
    fun `every level shows its card total and learned words`() = runHomeTest {
        progress.save(listOf(CardProgress(testAlphabetPool(Level.A1).first().id, seen = 1, correct = 1)))

        val levels = viewModel().state.value.levels

        assertEquals(Level.entries, levels.map { it.level })
        assertEquals(LevelProgressUi(Level.A1, 1, 26), levels.first { it.level == Level.A1 })
        assertEquals(LevelProgressUi(Level.B2, 0, 26), levels.first { it.level == Level.B2 })
    }

    @Test
    fun `changing the level resets the category to mixed and saves both`() = runHomeTest {
        settings.update { it.copy(selectedLevel = Level.A1, homeCategory = CategoryId("home")) }
        val viewModel = viewModel()

        viewModel.onIntent(HomeIntent.SelectLevel(Level.A2))

        val state = viewModel.state.value
        assertEquals(Level.A2, state.selectedLevel)
        assertEquals(null, state.selectedCategory)
        assertEquals(Level.A2, settings.settings.value.selectedLevel)
        assertEquals(null, settings.settings.value.homeCategory)
    }

    @Test
    fun `tapping the selected level keeps the category`() = runHomeTest {
        settings.update { it.copy(selectedLevel = Level.A1, homeCategory = CategoryId("food-drink")) }
        val viewModel = viewModel()

        viewModel.onIntent(HomeIntent.SelectLevel(Level.A1))

        assertEquals(CategoryId("food-drink"), viewModel.state.value.selectedCategory)
    }

    @Test
    fun `mode and category are saved and survive a restart`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.onIntent(HomeIntent.SelectMode(GameMode.HARDCORE))
        viewModel.onIntent(HomeIntent.OpenCategories)
        assertTrue(viewModel.state.value.categorySheetOpen)
        viewModel.onIntent(HomeIntent.SelectCategory(CategoryId("food-drink")))
        assertFalse(viewModel.state.value.categorySheetOpen)

        val restarted = viewModel().state.value
        assertEquals(GameMode.HARDCORE, restarted.selectedMode)
        assertEquals(CategoryId("food-drink"), restarted.selectedCategory)
    }

    @Test
    fun `only the home modes can be selected`() = runHomeTest {
        val viewModel = viewModel()

        viewModel.onIntent(HomeIntent.SelectMode(GameMode.DAILY))

        assertEquals(GameMode.CLASSIC, viewModel.state.value.selectedMode)
        assertEquals(GameMode.CLASSIC, settings.settings.value.homeMode)
    }

    @Test
    fun `the sheet lists the level's categories in order with total and learned words`() = runHomeTest {
        cards.cards = listOf(
            testCard("APPLE", category = "food-drink"),
            testCard("BREAD", category = "food-drink"),
            testCard("CHAIR", category = "home"),
            testCard("EAGLE", level = Level.A2, category = "animals"),
        )
        progress.save(listOf(CardProgress(cards.cards.first().id, seen = 1, correct = 1)))

        val state = viewModel().state.value

        assertTrue(ready(state))
        assertEquals(
            listOf(
                CategoryUi(CategoryId("food-drink"), "Yiyecek ve içecek", "Food and drink", learned = 1, total = 2),
                CategoryUi(CategoryId("home"), "Ev ve eşyalar", "Home", learned = 0, total = 1),
            ),
            (state.categories as CategoriesState.Loaded).categories,
        )
        assertTrue(state.showCategoryRow)
    }

    @Test
    fun `NAT has no categories so the row is hidden and the sheet never opens`() = runHomeTest {
        settings.update { it.copy(selectedLevel = Level.NAT, homeCategory = CategoryId("home")) }
        val viewModel = viewModel()

        val state = viewModel.state.value
        assertFalse(state.showCategoryRow)
        assertEquals(CategoriesState.Loaded(persistentListOf()), state.categories)
        viewModel.onIntent(HomeIntent.OpenCategories)
        assertFalse(viewModel.state.value.categorySheetOpen)
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.Play)
            assertEquals(HomeEffect.StartGame(Level.NAT, GameMode.CLASSIC, null), awaitItem())
        }
    }

    @Test
    fun `a stored category the level does not have becomes mixed`() = runHomeTest {
        settings.update { it.copy(selectedLevel = Level.A1, homeCategory = CategoryId("animals")) }
        val viewModel = viewModel()

        assertEquals(null, viewModel.state.value.selectedCategory)
        // Forgotten in storage too, so a later settings emission can't bring it back.
        assertEquals(null, settings.settings.value.homeCategory)
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.Play)
            assertEquals(HomeEffect.StartGame(Level.A1, GameMode.CLASSIC, null), awaitItem())
        }
    }

    @Test
    fun `switching the level away and back still reads the categories`() = runHomeTest {
        val viewModel = viewModel()

        viewModel.onIntent(HomeIntent.SelectLevel(Level.A2))
        viewModel.onIntent(HomeIntent.SelectLevel(Level.A1))

        assertTrue(viewModel.state.value.categories is CategoriesState.Loaded)
    }

    @Test
    fun `an unreadable category list falls back to mixed and retry reads it again`() = runHomeTest {
        settings.update { it.copy(homeCategory = CategoryId("food-drink")) }
        categories.failing = true
        val viewModel = viewModel()

        assertEquals(CategoriesState.Error, viewModel.state.value.categories)
        assertTrue(viewModel.state.value.showCategoryRow)
        assertTrue(crashReporter.recorded.isNotEmpty())
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.Play)
            assertEquals(HomeEffect.StartGame(Level.A1, GameMode.CLASSIC, null), awaitItem())
        }

        categories.failing = false
        viewModel.onIntent(HomeIntent.RetryCategories)

        val state = viewModel.state.value
        assertTrue(state.categories is CategoriesState.Loaded)
        assertEquals(CategoryId("food-drink"), state.playCategory)
    }

    @Test
    fun `an unreadable content keeps zero totals and home still works`() = runHomeTest {
        cards.failing = true
        val viewModel = viewModel()

        assertTrue(viewModel.state.value.levels.all { it.total == 0 })
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.Play)
            assertEquals(HomeEffect.StartGame(Level.A1, GameMode.CLASSIC, null), awaitItem())
        }
    }

    @Test
    fun `the classic time follows the setting`() = runHomeTest {
        val viewModel = viewModel()

        settings.update { it.copy(classicTimeLimitSeconds = 240) }

        assertEquals(240, viewModel.state.value.classicTimeLimitSeconds)
    }

    @Test
    fun `due reviews feed the notebook badge`() = runHomeTest {
        progress.save((1..120).map { card("c$it", box = 0, due = today) })

        assertEquals(120, viewModel().state.value.dueReviews)
    }
}
