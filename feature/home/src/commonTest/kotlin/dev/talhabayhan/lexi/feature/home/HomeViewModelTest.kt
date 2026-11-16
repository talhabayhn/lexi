package dev.talhabayhan.lexi.feature.home

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.config.FeatureFlags
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
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
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.time.AppClock
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
import dev.talhabayhan.lexi.core.testing.FakeExamPackRepository
import dev.talhabayhan.lexi.core.testing.FakeJokerRepository
import dev.talhabayhan.lexi.core.testing.FakeNotificationPermission
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeReviewRequester
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeShareService
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import dev.talhabayhan.lexi.core.testing.testAlphabetPool
import dev.talhabayhan.lexi.core.testing.testExamPack
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
    private val getDailyWheel = GetDailyWheel(cards, contentVersion, daily, clock)
    private val grantDailyJokers = GrantDailyJokers(jokers, streaks, clock, GameConfig())
    private val examPacks = FakeExamPackRepository()
    private val permission = FakeNotificationPermission(PermissionStatus.CAN_ASK)
    private val shouldShowPrePrompt = ShouldShowReminderPrePrompt(settings, sessions, permission, clock)
    private val reviewRequester = FakeReviewRequester()
    private var share = FakeShareService()
    private val analytics = FakeAnalyticsTracker()
    private val requestInAppReview =
        RequestInAppReview(settings, sessions, reviewRequester, FakeAnalyticsTracker(), clock)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        cards.cards = Level.entries.flatMap { testAlphabetPool(it) }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): HomeViewModel = HomeViewModel(
        progress = progress,
        sessions = sessions,
        settings = settings,
        getDailyWheel = getDailyWheel,
        daily = daily,
        appClock = clock,
        grantDailyJokers = grantDailyJokers,
        shouldShowReminderPrePrompt = shouldShowPrePrompt,
        requestInAppReview = requestInAppReview,
        examPacks = examPacks,
        flags = FeatureFlags(),
        shareService = share,
        analytics = analytics,
    )

    private fun runHomeTest(block: suspend TestScope.() -> Unit) = runTest {
        block()
    }

    private fun card(id: String, box: Int? = null, due: Long? = null, correct: Int = 0) =
        CardProgress(CardId(id), box = box, dueEpochDay = due, seen = 1, correct = correct)

    @Test
    fun `exam tile lists only packs playable in this build`() = runHomeTest {
        examPacks.packs = listOf(testExamPack("lgs", sort = 0), testExamPack("yds", isPro = true, sort = 1))
        viewModel().state.test {
            var state = awaitItem()
            while (state.examPackNames.isEmpty()) state = awaitItem()
            assertEquals(listOf("lgs"), state.examPackNames)
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
            progress = progress,
            sessions = sessions,
            settings = settings,
            getDailyWheel = getDaily,
            daily = daily,
            appClock = dstClock,
            grantDailyJokers = GrantDailyJokers(jokers, streaks, dstClock, GameConfig()),
            shouldShowReminderPrePrompt = shouldShowPrePrompt,
            requestInAppReview = requestInAppReview,
            examPacks = examPacks,
            flags = FeatureFlags(),
            shareService = share,
            analytics = analytics,
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
    fun `nothing played yet shows real zeros - A1 and 3 min`() = runHomeTest {
        viewModel().state.test {
            val state = expectMostRecentItem()
            assertEquals(0L, state.totalScore)
            assertEquals(0, state.dueReviews)
            assertEquals(Level.A1, state.selectedLevel)
            assertEquals(180, state.classicTimeLimitSeconds)
            assertEquals(DailyCardState.Available, state.dailyState)
        }
    }

    @Test
    fun `total score and due reviews come from the repositories and follow their changes`() = runHomeTest {
        progress.save(listOf(card("a", correct = 2), card("b", box = 0, due = today, correct = 1)))
        sessions.record(session())
        viewModel().state.test {
            val state = expectMostRecentItem()
            assertEquals(200L, state.totalScore)
            assertEquals(1, state.dueReviews)

            sessions.record(session())
            assertEquals(400L, expectMostRecentItem().totalScore)
        }
    }

    @Test
    fun `due reviews move on to the new day when the screen is refreshed after midnight`() = runHomeTest {
        progress.save(listOf(card("b", box = 0, due = today + 1, correct = 1)))
        val viewModel = viewModel()
        viewModel.state.test {
            assertEquals(0, expectMostRecentItem().dueReviews)

            clock.advanceBy(FakeAppClock.MS_PER_DAY)
            viewModel.onIntent(HomeIntent.Refresh)

            assertEquals(1, expectMostRecentItem().dueReviews)
        }
    }

    @Test
    fun `play opens the category picker on the stored level`() = runHomeTest {
        settings.update { it.copy(selectedLevel = Level.B2) }
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.Play)

            assertEquals(HomeEffect.NavigateToCategoryPicker(Level.B2), awaitItem())
        }
    }

    @Test
    fun `invite shares the message and tracks share with type invite`() = runHomeTest {
        val viewModel = viewModel()

        viewModel.onIntent(HomeIntent.Invite("Sen de dene: https://talhabayhn.github.io/lexi/tr/"))

        assertEquals(listOf("Sen de dene: https://talhabayhn.github.io/lexi/tr/"), share.sharedTexts)
        assertEquals(
            listOf<AnalyticsEvent>(AnalyticsEvent.Share(AnalyticsEvent.ShareType.INVITE)),
            analytics.events,
        )
    }

    @Test
    fun `an invite whose sheet could not be shown is not tracked`() = runHomeTest {
        share = FakeShareService(result = false)
        val viewModel = viewModel()

        viewModel.onIntent(HomeIntent.Invite("message"))

        assertEquals(1, share.sharedTexts.size)
        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun `the notebook tile opens the notebook`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenNotebook)

            assertEquals(HomeEffect.NavigateToNotebook, awaitItem())
        }
    }

    @Test
    fun `the statistics tile opens the statistics`() = runHomeTest {
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
    fun `the duel box opens the duel setup`() = runHomeTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(HomeIntent.OpenDuel)

            assertEquals(HomeEffect.NavigateToDuel, awaitItem())
        }
    }

    @Test
    fun `the exam packs tile opens the exam packs page`() = runHomeTest {
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
}
