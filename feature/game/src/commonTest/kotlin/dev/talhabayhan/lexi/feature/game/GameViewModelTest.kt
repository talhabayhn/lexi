package dev.talhabayhan.lexi.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.domain.config.FeatureFlags
import dev.talhabayhan.lexi.core.domain.daily.GetDailyWheel
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.game.SavedGameCodec
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.JokerRepository
import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.session.FinishGame
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeContentVersionRepository
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeDailyRepository
import dev.talhabayhan.lexi.core.testing.FakeJokerRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeSpeaker
import dev.talhabayhan.lexi.core.testing.testCard
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val config = GameConfig()
    private val cards = FakeCardRepository(listOf(testCard("APPLE"), testCard("BREAD"), testCard("CHAIR")))
    private val progress = FakeProgressRepository()
    private val sessions = FakeSessionRepository(config)
    private val jokers = FakeJokerRepository()
    private val settings = FakeSettingsRepository()
    private val appClock = FakeAppClock()
    private val speaker = FakeSpeaker()
    private val analytics = FakeAnalyticsTracker()
    private val crashReporter = FakeCrashReporter()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun routeHandle(extra: Map<String, Any?> = emptyMap()) =
        SavedStateHandle(mapOf<String, Any?>("level" to "A1", "mode" to "CLASSIC") + extra)

    private val route = GameRoute(Level.A1, GameMode.CLASSIC)

    private fun TestScope.viewModel(
        handle: SavedStateHandle = routeHandle(),
        gameRoute: GameRoute = route,
        jokerRepository: JokerRepository = jokers,
        sessionRepository: SessionRepository = sessions,
        flags: FeatureFlags = FeatureFlags(),
        seeds: SeedSource = SeedSource { SEED },
        getDailyWheel: GetDailyWheel? = null,
        preparer: GamePreparer? = null,
    ): GameViewModel = GameViewModel(
        route = gameRoute,
        handle = handle,
        loader = loader(jokerRepository, seeds, getDailyWheel),
        jokers = jokerRepository,
        settings = settings,
        finishGame = FinishGame(sessionRepository, progress, appClock, config),
        appClock = appClock,
        speaker = speaker,
        config = config,
        timeSource = testScheduler.timeSource,
        analytics = analytics,
        crashReporter = crashReporter,
        flags = flags,
        preparer = preparer,
    )

    /** The loader's CPU work runs on the test scheduler too, so `runCurrent` completes a load. */
    private fun TestScope.loader(
        jokerRepository: JokerRepository = jokers,
        seeds: SeedSource = SeedSource { SEED },
        getDailyWheel: GetDailyWheel? = null,
    ) = GameLoader(
        cards = cards,
        progress = progress,
        jokers = jokerRepository,
        settings = settings,
        appClock = appClock,
        config = config,
        seeds = seeds,
        crashReporter = crashReporter,
        getDailyWheel = getDailyWheel,
        cpu = StandardTestDispatcher(testScheduler),
    )

    private fun TestScope.preparer(seeds: SeedSource = SeedSource { SEED }) =
        GamePreparer(loader(seeds = seeds), backgroundScope)

    /** Loaded and resumed: the game has started (A is current). */
    private fun TestScope.started(handle: SavedStateHandle = routeHandle()): GameViewModel = viewModel(handle).also {
        runCurrent()
        it.onIntent(GameIntent.Foreground)
    }

    private fun TestScope.advance(ms: Long) {
        advanceTimeBy(ms)
        runCurrent()
    }

    private val GameViewModel.remainingMs: Long get() = clock.value.remainingMs

    /** Effects are buffered: the first one of a started game is the advance to A. */
    private suspend fun ReceiveTurbine<GameEffect>.awaitStart() {
        assertEquals(GameEffect.Advanced('A'), awaitItem())
    }

    private suspend fun ReceiveTurbine<GameEffect>.skipUntil(expected: GameEffect) {
        while (awaitItem() != expected) Unit
    }

    /** Skips to the navigation to the result and returns its payload. */
    private suspend fun ReceiveTurbine<GameEffect>.awaitResult(): ResultRoute {
        while (true) {
            val effect = awaitItem()
            if (effect is GameEffect.NavigateToResult) return effect.result
        }
    }

    @Test
    fun `the game starts when the loaded screen is resumed`() = runTest {
        val handle = routeHandle()
        val vm = viewModel(handle)
        assertTrue(vm.state.value.isLoading)
        runCurrent()
        // Loaded, not resumed yet: the board shows the first turn but takes no input.
        assertFalse(vm.state.value.isLoading)
        assertTrue(vm.state.value.isStarting)
        assertEquals('A', vm.state.value.turn?.letter)
        assertFalse(vm.state.value.inputEnabled)
        val notStarted = handle.get<String>(GameViewModel.KEY_GAME)
        vm.onIntent(GameIntent.KeyTapped(0))
        assertEquals(notStarted, handle.get<String>(GameViewModel.KEY_GAME))
        assertTrue(analytics.events.isEmpty())

        vm.onIntent(GameIntent.Foreground)
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.GameStart(GameMode.CLASSIC, Level.A1)), analytics.events)

        assertFalse(vm.state.value.isLoading)
        assertFalse(vm.state.value.isStarting)
        assertTrue(vm.state.value.inputEnabled)
        assertEquals('A', vm.state.value.turn?.letter)
        assertEquals(listOf('A', 'B', 'C'), vm.state.value.wheel.map { it.letter })
        assertEquals(180_000L, vm.remainingMs)
    }

    @Test
    fun `ticks charge the clock every 100 ms while playing`() = runTest {
        val vm = started()

        advance(1_050L)

        assertEquals(179_000L, vm.remainingMs)
        assertEquals(179, vm.clock.value.wholeSeconds)
    }

    @Test
    fun `background charges the time so far and nothing while away and foreground stays paused`() = runTest {
        val vm = started()
        advance(550L)

        vm.onIntent(GameIntent.Background)
        assertEquals(179_450L, vm.remainingMs)
        assertTrue(vm.state.value.isPaused)
        assertEquals(1, speaker.stopCount)

        advance(60_000L)
        vm.onIntent(GameIntent.Foreground)
        advance(5_000L)
        assertTrue(vm.state.value.isPaused)
        assertEquals(179_450L, vm.remainingMs)

        vm.onIntent(GameIntent.Resume)
        advance(1_000L)
        assertFalse(vm.state.value.isPaused)
        assertEquals(178_450L, vm.remainingMs)
    }

    @Test
    fun `the pause button stops the clock until resume`() = runTest {
        val vm = started()
        advance(300L)

        vm.onIntent(GameIntent.Pause)
        advance(10_000L)
        assertEquals(179_700L, vm.remainingMs)

        vm.onIntent(GameIntent.Resume)
        advance(200L)
        assertEquals(179_500L, vm.remainingMs)
    }

    @Test
    fun `a wrong answer is revealed for 1200 ms without using the clock and then the wheel advances`() = runTest {
        val vm = started()
        vm.effects.test {
            vm.type("APPEL")

            skipUntil(GameEffect.Wrong('A'))
            val turn = checkNotNull(vm.state.value.turn)
            assertTrue(turn.isRevealing)
            assertEquals("APPLE", turn.words.single().map { it.letter }.joinToString(""))
            assertTrue(turn.words.single().all { it.state == AnswerSlotState.Wrong })
            assertFalse(vm.state.value.inputEnabled)

            advance(1_100L)
            assertEquals('A', vm.state.value.turn?.letter)
            advance(100L)
            assertEquals(GameEffect.Advanced('B'), awaitItem())
            assertEquals('B', vm.state.value.turn?.letter)
            assertEquals(WheelLetterState.Wrong, vm.state.value.wheel[0].state)
            assertEquals(180_000L, vm.remainingMs)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pass marks the letter passed and moves on`() = runTest {
        val vm = started()
        vm.effects.test {
            awaitStart()
            vm.onIntent(GameIntent.Pass)

            assertEquals(GameEffect.Passed('A'), awaitItem())
            assertEquals(GameEffect.Advanced('B'), awaitItem())
            assertEquals(WheelLetterState.Passed, vm.state.value.wheel[0].state)
            assertEquals('B', vm.state.value.turn?.letter)
        }
    }

    @Test
    fun `letters and backspace update the answer boxes`() = runTest {
        val vm = started()
        vm.effects.test {
            awaitStart()
            val keys = checkNotNull(vm.state.value.turn).keys.map { it.letter }
            vm.type("APP")
            assertEquals(
                GameEffect.LetterPlaced(cellIndex = 1, keyIndex = keys.indexOf('P'), letter = 'P'),
                awaitItem(),
            )
            assertEquals(
                GameEffect.LetterPlaced(cellIndex = 2, keyIndex = keys.lastIndexOf('P'), letter = 'P'),
                awaitItem(),
            )

            vm.onIntent(GameIntent.Backspace)
            vm.onIntent(GameIntent.Backspace)
            vm.onIntent(GameIntent.Backspace)

            // Backspace never removes the given first letter.
            val cells = checkNotNull(vm.state.value.turn).words.single()
            assertEquals(listOf('A', null, null, null, null), cells.map { it.letter })
            assertEquals(AnswerSlotState.Given, cells[0].state)
            assertEquals(AnswerSlotState.Active, cells[1].state)
        }
    }

    @Test
    fun `a correct answer scores and speaks the word the moment it is judged and moves on`() = runTest {
        val vm = started()
        vm.effects.test {
            awaitStart()

            vm.type("APPLE")

            // motion.md #3: spoken at once, without waiting for a sound or the scheduler.
            assertEquals(listOf("apple"), speaker.spoken)
            assertEquals(10, vm.state.value.score)
            assertEquals('B', vm.state.value.turn?.letter)
            val correct = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item)?.value }
                .filterIsInstance<GameEffect.Correct>().single()
            assertTrue(correct.spoken, "the correct sound gives way to the word")
        }
    }

    @Test
    fun `with speech off the correct effect keeps its sound`() = runTest {
        settings.update { it.copy(speakOnCorrect = false) }
        val vm = started()
        vm.effects.test {
            awaitStart()

            vm.type("APPLE")

            val correct = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item)?.value }
                .filterIsInstance<GameEffect.Correct>().single()
            assertFalse(correct.spoken)
        }
    }

    @Test
    fun `an engine that is not ready or has no voice keeps the correct sound and says nothing`() = runTest {
        speaker.ready = false
        val vm = started()
        vm.effects.test {
            awaitStart()

            vm.type("APPLE")

            val correct = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item)?.value }
                .filterIsInstance<GameEffect.Correct>().single()
            assertFalse(correct.spoken, "the correct sound plays")
            assertTrue(speaker.spoken.isEmpty(), "no late word over the sound")
        }
    }

    @Test
    fun `the speech engine is warmed up while the setting is on`() = runTest {
        started()
        assertTrue(speaker.warmUpCount > 0)
    }

    @Test
    fun `the speech engine is not warmed up while the setting is off`() = runTest {
        settings.update { it.copy(speakOnCorrect = false) }
        started()
        assertEquals(0, speaker.warmUpCount)
    }

    @Test
    fun `going to the background stops the word being spoken`() = runTest {
        val vm = started()
        vm.type("APPLE")

        vm.onIntent(GameIntent.Background)

        assertEquals(listOf("apple"), speaker.spoken)
        assertEquals(1, speaker.stopCount)
    }

    @Test
    fun `every correct word is spoken and the last one too`() = runTest {
        val vm = started()

        vm.type("APPLE")
        vm.type("BREAD")
        vm.type("CHAIR")

        assertEquals(listOf("apple", "bread", "chair"), speaker.spoken)
    }

    @Test
    fun `a letter of the reveal joker flies from the key it used`() = runTest {
        val vm = started()
        vm.effects.test {
            awaitStart()
            val keys = checkNotNull(vm.state.value.turn).keys.map { it.letter }

            vm.onIntent(GameIntent.UseJoker(JokerType.REVEAL_LETTER))

            assertEquals(
                GameEffect.LetterPlaced(cellIndex = 1, keyIndex = keys.indexOf('P'), letter = 'P'),
                awaitItem(),
            )
        }
    }

    @Test
    fun `nothing is spoken when the setting is off`() = runTest {
        settings.update { it.copy(speakOnCorrect = false) }
        val vm = started()

        vm.type("APPLE")

        assertTrue(speaker.spoken.isEmpty())
    }

    @Test
    fun `the classic time limit comes from the settings`() = runTest {
        settings.update { it.copy(classicTimeLimitSeconds = 120) }

        val vm = started()

        assertEquals(120_000L, vm.remainingMs)
    }

    @Test
    fun `the daily wheel ignores classic time setting and uses daily limit`() = runTest {
        settings.update { it.copy(classicTimeLimitSeconds = 120) }

        val vm = startedIn(GameMode.DAILY)

        assertEquals(config.dailyTimeLimitSeconds * 1_000L, vm.remainingMs)
    }

    @Test
    fun `the hardcore wheel ignores classic time setting and uses hardcore limit`() = runTest {
        settings.update { it.copy(classicTimeLimitSeconds = 120) }

        val vm = startedIn(GameMode.HARDCORE)

        assertEquals(config.hardcoreTimeLimitSeconds * 1_000L, vm.remainingMs)
    }

    @Test
    fun `the review wheel holds one card per letter of the due notebook cards of any level`() = runTest {
        val today = appClock.epochDay(appClock.nowMs())
        cards.cards = listOf(
            testCard("APPLE"),
            testCard("BREAD", level = Level.B1),
            testCard("CHAIR"),
            testCard("DOG"),
        )
        progress.save(
            listOf(
                CardProgress(CardId("a1-apple"), box = 0, dueEpochDay = today),
                CardProgress(CardId("b1-bread"), box = 1, dueEpochDay = today - 2),
                // Not due yet, not in the notebook: both stay out of the wheel.
                CardProgress(CardId("a1-chair"), box = 2, dueEpochDay = today + 3),
                CardProgress(CardId("a1-dog"), box = null),
            ),
        )
        val vm = viewModel(gameRoute = GameRoute(Level.A1, GameMode.REVIEW)).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }

        assertEquals(listOf('A', 'B'), vm.state.value.wheel.map { it.letter })
        // Stops the 100 ms ticker so the test's virtual time can end.
        vm.onIntent(GameIntent.Background)
    }

    @Test
    fun `a review without due cards is abandoned`() = runTest {
        val vm = viewModel(gameRoute = GameRoute(Level.A1, GameMode.REVIEW))
        vm.effects.test {
            runCurrent()
            assertEquals(GameEffect.Abandoned, awaitItem())
        }
    }

    @Test
    fun `a category wheel skips letters without a card and counts them as neither right nor wrong`() = runTest {
        cards.cards = listOf(
            testCard("APPLE"),
            testCard("BREAD", category = "home"),
            testCard("CHAIR", category = "home"),
        )
        val vm = viewModel(gameRoute = GameRoute(Level.A1, GameMode.CLASSIC, CategoryId("home"))).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }

        // A has no card in "home": its slot is SKIPPED, not shown, and the first turn is B.
        assertEquals(listOf('B', 'C'), vm.state.value.wheel.map { it.letter })
        assertEquals('B', vm.state.value.turn?.letter)
        vm.effects.test {
            vm.type("BREAD")
            vm.type("CHAIR")
            runCurrent()

            // Both playable letters answered: the wheel is complete although 24 slots were skipped.
            val result = awaitResult()
            assertEquals("home", result.category)
            assertEquals(listOf(2, 0, 0), listOf(result.correct, result.wrong, result.passed))
            val session = sessions.sessions.single()
            assertEquals(CategoryId("home"), session.category)
            assertTrue(session.completed)
            assertEquals(listOf(2, 0, 0), listOf(session.correct, session.wrong, session.passed))
        }
    }

    @Test
    fun `game over records the session exactly once and then navigates`() = runTest {
        val handle = routeHandle()
        val vm = started(handle)
        vm.effects.test {
            vm.type("APPLE")
            vm.type("BREAD")
            vm.type("CHAIR")
            runCurrent()

            awaitResult()
            assertEquals(1, sessions.recordCalls)
            val session = sessions.sessions.single()
            assertTrue(session.completed)
            assertEquals(3, session.correct)
            assertEquals(CategoryId.MIXED, session.category)
            assertNull(handle.get<String>(GameViewModel.KEY_GAME))

            vm.onIntent(GameIntent.Background)
            vm.onIntent(GameIntent.Foreground)
            vm.onIntent(GameIntent.Pass)
            vm.onIntent(GameIntent.Forfeit)
            advance(10_000L)
            assertEquals(1, sessions.recordCalls)
            expectNoEvents()
        }
    }

    @Test
    fun `game finish is tracked once with mode level correct answers and wall-clock seconds`() = runTest {
        val vm = started()
        vm.effects.test {
            appClock.advanceBy(42_500L)
            vm.type("APPLE")
            vm.type("BREAD")
            vm.type("CHAIR")
            runCurrent()
            awaitResult()

            vm.onIntent(GameIntent.Forfeit)
            advance(10_000L)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(
            listOf(
                AnalyticsEvent.GameStart(GameMode.CLASSIC, Level.A1),
                AnalyticsEvent.GameFinish(GameMode.CLASSIC, Level.A1, correct = 3, durationSeconds = 42),
            ),
            analytics.events,
        )
    }

    @Test
    fun `time up finishes the game once and stops the ticker`() = runTest {
        val vm = started()
        vm.effects.test {
            advance(180_000L)

            awaitResult()
            assertEquals(0L, vm.remainingMs)
            assertEquals(1, sessions.recordCalls)
            assertFalse(sessions.sessions.single().completed)
            advance(5_000L)
            assertEquals(1, sessions.recordCalls)
            expectNoEvents()
        }
    }

    @Test
    fun `the last ten seconds emit a time warning each second`() = runTest {
        val vm = started()
        vm.effects.test {
            awaitStart()
            advance(170_000L)
            assertEquals(GameEffect.TimeWarning(10), awaitItem())
            assertTrue(vm.clock.value.isWarning)
            advance(1_000L)
            assertEquals(GameEffect.TimeWarning(9), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `running out of time sends time up exactly once after the last warning and before the result`() = runTest {
        val vm = started()
        vm.effects.test {
            awaitStart()
            advance(179_000L)
            skipUntil(GameEffect.TimeWarning(1))

            advance(1_000L)
            advance(5_000L)

            val rest = buildList {
                while (true) {
                    val effect = awaitItem()
                    add(effect)
                    if (effect is GameEffect.NavigateToResult) break
                }
            }
            assertEquals(GameEffect.TimeUp, rest.first())
            assertEquals(1, rest.count { it == GameEffect.TimeUp })
            assertTrue(rest.none { it is GameEffect.TimeWarning }, "no tick with the buzzer")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a game that ends before the time runs out sends no time up`() = runTest {
        val vm = started()
        vm.effects.test {
            awaitStart()
            vm.type("APPLE")
            vm.type("BREAD")
            vm.type("CHAIR")

            val effects = buildList {
                while (true) {
                    val effect = awaitItem()
                    add(effect)
                    if (effect is GameEffect.NavigateToResult) break
                }
            }
            assertTrue(GameEffect.TimeUp !in effects)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a game restored from the SavedStateHandle comes back paused with the same board and time`() = runTest {
        val handle = routeHandle()
        val first = started(handle)
        first.type("APPLE")
        advance(3_050L)
        first.type("BR")
        first.onIntent(GameIntent.Background)
        val saved = assertNotNull(handle.get<String>(GameViewModel.KEY_GAME))
        val limit = assertNotNull(handle.get<Int>(GameViewModel.KEY_CLASSIC_LIMIT))
        val spokenBefore = speaker.spoken

        val restored = viewModel(
            routeHandle(mapOf(GameViewModel.KEY_GAME to saved, GameViewModel.KEY_CLASSIC_LIMIT to limit)),
        )
        runCurrent()
        restored.onIntent(GameIntent.Foreground)

        assertEquals(first.state.value, restored.state.value)
        assertTrue(restored.state.value.isPaused)
        assertEquals(first.remainingMs, restored.remainingMs)
        assertEquals(176_950L, restored.remainingMs)
        assertEquals(spokenBefore, speaker.spoken)
        advance(2_000L)
        assertEquals(176_950L, restored.remainingMs)

        restored.onIntent(GameIntent.Resume)
        advance(1_000L)
        assertEquals(175_950L, restored.remainingMs)
        restored.type("EAD")
        assertEquals('C', restored.state.value.turn?.letter)
        assertEquals(0, sessions.recordCalls)
        // Only the first screen started a wheel: the restored game is the same one.
        assertEquals(1, analytics.events.count { it is AnalyticsEvent.GameStart })
    }

    @Test
    fun `a restored game whose card left the content is abandoned`() = runTest {
        val handle = routeHandle()
        val first = started(handle)
        first.onIntent(GameIntent.Background)
        val saved = assertNotNull(handle.get<String>(GameViewModel.KEY_GAME))
        cards.cards = cards.cards.filter { it.answer != "BREAD" }

        val restored = viewModel(routeHandle(mapOf(GameViewModel.KEY_GAME to saved)))
        restored.effects.test {
            runCurrent()
            assertEquals(GameEffect.Abandoned, awaitItem())
        }
    }

    @Test
    fun `the start pack is granted on the first game and stored with the game`() = runTest {
        val handle = routeHandle()
        started(handle)

        assertEquals(config.jokerStartPack, jokers.inventory.value)
        val saved = checkNotNull(SavedGameCodec.decodeOrNull(checkNotNull(handle.get<String>(GameViewModel.KEY_GAME))))
        assertEquals(2, saved.jokers.count(JokerType.REVEAL_LETTER))
        assertEquals(listOf(SlotState.TODO, SlotState.SKIPPED), saved.slots.take(4).map { it.state }.distinct())
    }

    // region joker sheet (05-joker-sheet)

    @Test
    fun `the joker ad button is off by default and follows the feature flag`() = runTest {
        assertFalse(started().state.value.showJokerAd)

        val flagged = viewModel(flags = FeatureFlags(rewardedAdJokers = true)).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }
        assertTrue(flagged.state.value.showJokerAd)
    }

    @Test
    fun `the joker sheet opens without pausing and the clock keeps running`() = runTest {
        val vm = started()

        vm.onIntent(GameIntent.OpenJokers)
        advance(2_000L)

        assertTrue(vm.state.value.isJokerSheetOpen)
        assertFalse(vm.state.value.isPaused)
        assertEquals(178_000L, vm.remainingMs)
        assertEquals(
            listOf(2, 1, 3, 1),
            vm.state.value.jokers.map { it.count },
        )
        assertTrue(vm.state.value.jokers.all { it.enabled })

        vm.onIntent(GameIntent.CloseJokers)
        assertFalse(vm.state.value.isJokerSheetOpen)
    }

    @Test
    fun `using a joker applies it and closes the sheet and saves the inventory at once`() = runTest {
        val vm = started()
        vm.onIntent(GameIntent.OpenJokers)

        vm.onIntent(GameIntent.UseJoker(JokerType.REVEAL_LETTER))

        val ui = vm.state.value
        assertFalse(ui.isJokerSheetOpen)
        // The given first letter (GAME_RULES.md §3) is never revealed; the joker fills the second box.
        assertEquals('P', checkNotNull(ui.turn).words.single()[1].letter)
        assertEquals(AnswerSlotState.JokerFilled, checkNotNull(ui.turn).words.single()[1].state)
        assertEquals(170_000L, vm.remainingMs)
        assertEquals(1, ui.jokers.first { it.type == JokerType.REVEAL_LETTER }.count)
        assertTrue(ui.jokers.none { it.enabled }, "one joker per slot")
        // Saved without waiting for the scheduler (undispatched, NonCancellable).
        assertEquals(1, jokers.inventory.value?.count(JokerType.REVEAL_LETTER))
        assertEquals(AnalyticsEvent.JokerUsed(JokerType.REVEAL_LETTER), analytics.events.last())
    }

    @Test
    fun `a joker write is finished even when the screen is closed meanwhile`() = runTest {
        val slow = SlowJokerRepository(jokers)
        val vm = viewModel(jokerRepository = slow).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }
        vm.onIntent(GameIntent.UseJoker(JokerType.EXAMPLE))
        assertEquals(3, jokers.inventory.value?.count(JokerType.EXAMPLE))

        ViewModelStore().apply { put("game", vm) }.clear()
        advanceTimeBy(SlowJokerRepository.DELAY_MS + 1)
        runCurrent()

        assertEquals(2, jokers.inventory.value?.count(JokerType.EXAMPLE))
    }

    @Test
    fun `a refused joker leaves the sheet open and the inventory untouched`() = runTest {
        jokers.save(JokerInventory(mapOf(JokerType.EXAMPLE to 1)))
        val vm = started()
        vm.onIntent(GameIntent.OpenJokers)

        vm.onIntent(GameIntent.UseJoker(JokerType.REVEAL_LETTER))

        assertTrue(vm.state.value.isJokerSheetOpen)
        assertFalse(vm.state.value.jokers.first { it.type == JokerType.REVEAL_LETTER }.enabled)
        assertEquals(JokerInventory(mapOf(JokerType.EXAMPLE to 1)), jokers.inventory.value)
    }

    @Test
    fun `the listen joker speaks the answer and the example joker shows the blanked example`() = runTest {
        settings.update { it.copy(speakOnCorrect = false) }
        val vm = started()

        vm.onIntent(GameIntent.UseJoker(JokerType.LISTEN))
        assertEquals(listOf("apple"), speaker.spoken)

        vm.onIntent(GameIntent.Pass)
        vm.onIntent(GameIntent.UseJoker(JokerType.EXAMPLE))
        assertEquals("An example with _____.", vm.state.value.turn?.example)
    }

    @Test
    fun `the joker sheet does not open while input is blocked`() = runTest {
        val vm = started()
        vm.type("APPEL")

        vm.onIntent(GameIntent.OpenJokers)

        assertFalse(vm.state.value.isJokerSheetOpen)
    }

    @Test
    fun `going to the background closes the joker sheet and pauses`() = runTest {
        val vm = started()
        vm.onIntent(GameIntent.OpenJokers)

        vm.onIntent(GameIntent.Background)

        assertFalse(vm.state.value.isJokerSheetOpen)
        assertTrue(vm.state.value.isPaused)
    }

    // endregion

    // region pause and back

    @Test
    fun `back pauses a running game and keeps the pause dialog until resume`() = runTest {
        val vm = started()
        advance(500L)

        vm.onIntent(GameIntent.Back)
        assertTrue(vm.state.value.isPaused)
        advance(5_000L)
        vm.onIntent(GameIntent.Back)

        assertTrue(vm.state.value.isPaused)
        assertEquals(179_500L, vm.remainingMs)
        vm.onIntent(GameIntent.Resume)
        advance(500L)
        assertEquals(179_000L, vm.remainingMs)
    }

    @Test
    fun `back closes the joker sheet first without pausing`() = runTest {
        val vm = started()
        vm.onIntent(GameIntent.OpenJokers)

        vm.onIntent(GameIntent.Back)

        assertFalse(vm.state.value.isJokerSheetOpen)
        assertFalse(vm.state.value.isPaused)
    }

    @Test
    fun `giving up asks for confirmation and back or cancel return to the pause dialog`() = runTest {
        val vm = started()
        vm.onIntent(GameIntent.Pause)

        vm.onIntent(GameIntent.RequestQuit)
        assertTrue(vm.state.value.isQuitConfirmation)
        vm.onIntent(GameIntent.Back)
        assertFalse(vm.state.value.isQuitConfirmation)
        assertTrue(vm.state.value.isPaused)

        vm.onIntent(GameIntent.RequestQuit)
        vm.onIntent(GameIntent.CancelQuit)
        assertFalse(vm.state.value.isQuitConfirmation)

        vm.onIntent(GameIntent.RequestQuit)
        vm.onIntent(GameIntent.Resume)
        assertFalse(vm.state.value.isQuitConfirmation)
        assertFalse(vm.state.value.isPaused)
    }

    @Test
    fun `quit is only offered while paused`() = runTest {
        val vm = started()

        vm.onIntent(GameIntent.RequestQuit)

        assertFalse(vm.state.value.isQuitConfirmation)
    }

    @Test
    fun `a confirmed give up records the forfeit and shows its result`() = runTest {
        val vm = started()
        vm.effects.test {
            vm.type("APPLE")
            vm.onIntent(GameIntent.Pause)
            vm.onIntent(GameIntent.RequestQuit)
            vm.onIntent(GameIntent.Forfeit)
            runCurrent()

            val result = awaitResult()
            assertEquals("FORFEIT", result.reason)
            assertEquals(1, result.correct)
            assertEquals(
                AnalyticsEvent.GameFinish(GameMode.CLASSIC, Level.A1, correct = 1, durationSeconds = 0),
                analytics.events.last(),
            )
            assertEquals(2, result.wrong)
            assertEquals(listOf("a1-bread", "a1-chair"), result.missedCards)
            assertEquals(1, sessions.recordCalls)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `restart asks for confirmation and back or cancel return to the pause dialog`() = runTest {
        val vm = started()
        vm.onIntent(GameIntent.Pause)

        vm.onIntent(GameIntent.RequestRestart)
        assertTrue(vm.state.value.isRestartConfirmation)
        vm.onIntent(GameIntent.Back)
        assertFalse(vm.state.value.isRestartConfirmation)
        assertTrue(vm.state.value.isPaused)

        vm.onIntent(GameIntent.RequestRestart)
        vm.onIntent(GameIntent.CancelRestart)
        assertFalse(vm.state.value.isRestartConfirmation)

        vm.onIntent(GameIntent.RequestRestart)
        vm.onIntent(GameIntent.Resume)
        assertFalse(vm.state.value.isRestartConfirmation)
        assertFalse(vm.state.value.isPaused)
    }

    @Test
    fun `restart is only offered while paused and never needs a confirmation it did not ask`() = runTest {
        var seed = SEED
        val handle = routeHandle()
        val vm = viewModel(handle = handle, seeds = SeedSource { seed++ }).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }

        vm.onIntent(GameIntent.RequestRestart)
        assertFalse(vm.state.value.isRestartConfirmation)

        vm.onIntent(GameIntent.Pause)
        vm.onIntent(GameIntent.Restart)
        runCurrent()
        assertTrue(vm.state.value.isPaused)
        val saved = SavedGameCodec.decodeOrNull(checkNotNull(handle.get<String>(GameViewModel.KEY_GAME)))
        assertEquals(SEED, saved?.seed)
    }

    @Test
    fun `a confirmed restart drops the game unrecorded and starts a new wheel with the same config`() = runTest {
        var seed = SEED
        val handle = routeHandle()
        val vm = viewModel(handle = handle, seeds = SeedSource { seed++ }).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }
        val limit = handle.get<Int>(GameViewModel.KEY_CLASSIC_LIMIT)
        vm.effects.test {
            awaitStart()
            vm.type("APPLE")
            advance(5_000)
            vm.onIntent(GameIntent.Pause)
            vm.onIntent(GameIntent.RequestRestart)

            vm.onIntent(GameIntent.Restart)
            runCurrent()

            assertEquals(GameEffect.Advanced('A'), expectMostRecentItem())
            val state = vm.state.value
            assertFalse(state.isLoading)
            assertFalse(state.isPaused)
            assertFalse(state.isRestartConfirmation)
            assertTrue(state.inputEnabled)
            assertEquals(0, state.score)
            assertEquals('A', state.turn?.letter)
            assertEquals(config.classicTimeLimitSeconds * 1_000L, vm.remainingMs)
            assertEquals(0, sessions.recordCalls)
            val snapshot = checkNotNull(handle.get<String>(GameViewModel.KEY_GAME))
            val saved = checkNotNull(SavedGameCodec.decodeOrNull(snapshot))
            assertEquals(SEED + 1, saved.seed)
            assertEquals(GameMode.CLASSIC, saved.mode)
            assertEquals(Level.A1, saved.level)
            assertEquals(limit, handle.get<Int>(GameViewModel.KEY_CLASSIC_LIMIT))

            // The new game's clock runs.
            advance(1_000)
            assertEquals(config.classicTimeLimitSeconds * 1_000L - 1_000L, vm.remainingMs)
            cancelAndIgnoreRemainingEvents()
        }
        // A new wheel: a second game_start, and the dropped game sends no game_finish.
        assertEquals(2, analytics.events.count { it is AnalyticsEvent.GameStart })
        assertTrue(analytics.events.none { it is AnalyticsEvent.GameFinish })
    }

    @Test
    fun `the daily has no restart`() = runTest {
        val vm = startedIn(GameMode.DAILY)
        vm.onIntent(GameIntent.Pause)

        vm.onIntent(GameIntent.RequestRestart)

        assertFalse(vm.state.value.canRestart)
        assertFalse(vm.state.value.isRestartConfirmation)
    }

    @Test
    fun `settings open over the paused game and the game stays paused on return`() = runTest {
        val vm = started()
        vm.effects.test {
            awaitStart()
            vm.onIntent(GameIntent.OpenSettings)
            vm.onIntent(GameIntent.Pause)
            vm.onIntent(GameIntent.OpenSettings)
            assertEquals(GameEffect.NavigateToSettings, awaitItem())

            vm.onIntent(GameIntent.Background)
            advance(10_000)
            vm.onIntent(GameIntent.Foreground)
            advance(1_000)

            assertTrue(vm.state.value.isPaused)
            assertFalse(vm.state.value.inputEnabled)
            expectNoEvents()
        }
    }

    // endregion

    // region result payload, recording, snapshot

    @Test
    fun `the result lists exactly the cards that went into box 0 of the notebook`() = runTest {
        val vm = started()
        vm.effects.test {
            vm.onIntent(GameIntent.Pass)
            vm.type("BRADE")
            advance(config.revealMs)
            vm.type("CHAIR")
            advance(180_000L)

            val result = awaitResult()
            assertEquals("A1", result.level)
            assertEquals("CLASSIC", result.mode)
            assertEquals("TIME_UP", result.reason)
            assertEquals(1, result.correct)
            assertEquals(1, result.wrong)
            assertEquals(1, result.passed)
            assertEquals(1, result.longestStreak)
            assertEquals(0L, result.remainingMs)
            assertEquals(listOf("a1-apple", "a1-bread"), result.missedCards)
            assertEquals(listOf("a1-apple"), result.passedCards)
            assertTrue(result.recorded)
            val inBoxZero = progress.progress.values.filter { it.box == 0 }.map { it.cardId.value }
            assertEquals(result.missedCards.toSet(), inBoxZero.toSet())
            assertNull(progress.progress.getValue(CardId("a1-chair")).box)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the snapshot is kept while the game is recorded and removed afterwards`() = runTest {
        val handle = routeHandle()
        var snapshotWhileRecording: String? = null
        val watching = object : SessionRepository by sessions {
            override suspend fun record(session: SessionResult): Boolean {
                snapshotWhileRecording = handle.get<String>(GameViewModel.KEY_GAME)
                return sessions.record(session)
            }
        }
        val vm = viewModel(handle, sessionRepository = watching).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }
        vm.effects.test {
            vm.type("APPLE")
            vm.type("BREAD")
            vm.type("CHAIR")

            awaitResult()
            val snapshot = assertNotNull(snapshotWhileRecording)
            assertEquals(GameStatus.FINISHED, decodeFinished(snapshot))
            assertNull(handle.get<String>(GameViewModel.KEY_GAME))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed recording is retried once`() = runTest {
        sessions.failingRecords = 1
        val handle = routeHandle()
        val vm = started(handle)
        vm.effects.test {
            vm.type("APPLE")
            vm.type("BREAD")
            vm.type("CHAIR")

            val result = awaitResult()
            assertTrue(result.recorded)
            assertEquals(2, sessions.recordCalls)
            assertEquals(1, sessions.sessions.size)
            assertEquals(listOf("finishing the game failed"), crashReporter.recorded.map { it.second })
            assertNull(handle.get<String>(GameViewModel.KEY_GAME))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when recording fails twice the result is still shown and the snapshot is kept`() = runTest {
        sessions.failingRecords = 2
        val handle = routeHandle()
        val vm = started(handle)
        vm.effects.test {
            vm.type("APPLE")
            vm.type("BREAD")
            vm.type("CHAIR")

            val result = awaitResult()
            assertFalse(result.recorded)
            assertEquals(2, sessions.recordCalls)
            assertTrue(sessions.sessions.isEmpty())
            assertNotNull(handle.get<String>(GameViewModel.KEY_GAME))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a finished game restored before it was recorded is recorded after restore`() = runTest {
        sessions.failingRecords = 2
        val handle = routeHandle()
        val first = started(handle)
        first.type("APPLE")
        first.type("BREAD")
        first.type("CHAIR")
        runCurrent()
        val saved = assertNotNull(handle.get<String>(GameViewModel.KEY_GAME))
        val limit = assertNotNull(handle.get<Int>(GameViewModel.KEY_CLASSIC_LIMIT))

        val restoredHandle =
            routeHandle(mapOf(GameViewModel.KEY_GAME to saved, GameViewModel.KEY_CLASSIC_LIMIT to limit))
        val restored = viewModel(restoredHandle)
        restored.effects.test {
            runCurrent()
            val result = awaitResult()
            assertTrue(result.recorded)
            assertEquals(3, result.correct)
            assertEquals(1, sessions.sessions.size)
            assertNull(restoredHandle.get<String>(GameViewModel.KEY_GAME))
            // game_finish went out when the first screen finished the game; the restore doesn't send it again.
            assertEquals(1, analytics.events.count { it is AnalyticsEvent.GameFinish })
        }
    }

    // endregion

    @Test
    fun `keys and backspace and pass and the joker button are disabled during the reveal`() = runTest {
        val vm = started()
        vm.type("APPEL")
        val keysBefore = checkNotNull(vm.state.value.turn).keys

        assertFalse(vm.state.value.inputEnabled)
        vm.onIntent(GameIntent.KeyTapped(0))
        vm.onIntent(GameIntent.Backspace)
        vm.onIntent(GameIntent.Pass)

        assertEquals(keysBefore, checkNotNull(vm.state.value.turn).keys)
        assertEquals(WheelLetterState.Wrong, vm.state.value.wheel[0].state)
        advance(config.revealMs)
        assertTrue(vm.state.value.inputEnabled)
    }

    // region modes (GAME_RULES.md §5, §6)

    private fun TestScope.startedIn(mode: GameMode): GameViewModel {
        val dailyWheel = if (mode == GameMode.DAILY) {
            val allCards = Level.entries.flatMap { dev.talhabayhan.lexi.core.testing.testAlphabetPool(it) }
            GetDailyWheel(
                cards = FakeCardRepository(allCards),
                contentVersion = FakeContentVersionRepository(1),
                daily = FakeDailyRepository(),
                clock = appClock,
            )
        } else {
            null
        }
        return viewModel(gameRoute = GameRoute(Level.A1, mode), getDailyWheel = dailyWheel).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }
    }

    @Test
    fun `practice has no clock and never runs out of time`() = runTest {
        val vm = startedIn(GameMode.PRACTICE)
        vm.effects.test {
            awaitStart()
            advance(30 * 60_000L)

            assertEquals(GameClockUi.Untimed, vm.clock.value)
            assertTrue(vm.state.value.inputEnabled)
            assertEquals('A', vm.state.value.turn?.letter)
            expectNoEvents()
            vm.onIntent(GameIntent.Background)
        }
    }

    @Test
    fun `practice scores nothing but counts correct answers and still updates the notebook`() = runTest {
        val vm = startedIn(GameMode.PRACTICE)
        vm.effects.test {
            vm.type("APPLE")
            assertFalse(vm.state.value.isScored)
            assertEquals(1, vm.state.value.correct)
            assertEquals(0, vm.state.value.score)
            vm.type("BRADE")
            advance(config.revealMs)
            vm.type("CHAIR")

            val result = awaitResult()
            assertEquals("PRACTICE", result.mode)
            assertEquals("ALL_ANSWERED", result.reason)
            assertEquals(0, result.score)
            assertNull(result.remainingMs)
            assertEquals(listOf(2, 1, 0), listOf(result.correct, result.wrong, result.passed))
            assertTrue(result.recorded)
            // Recorded like any wheel (stats count it, GAME_RULES.md §11), but with 0 points.
            val session = sessions.sessions.single()
            assertEquals(GameMode.PRACTICE, session.mode)
            assertEquals(0, session.score)
            assertTrue(session.completed)
            // Still SRS (GAME_RULES.md §5): the wrong card goes into box 0.
            assertEquals(0, progress.progress.getValue(CardId("a1-bread")).box)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hardcore ends at the first wrong answer and scores times 1_5`() = runTest {
        val vm = startedIn(GameMode.HARDCORE)
        assertEquals(config.hardcoreTimeLimitSeconds * 1_000L, vm.remainingMs)
        assertTrue(vm.state.value.isScored)
        vm.effects.test {
            vm.type("APPLE")
            assertEquals(10, vm.state.value.score)
            vm.type("BRADE")

            val result = awaitResult()
            assertEquals("HARDCORE", result.mode)
            assertEquals("WRONG_IN_HARDCORE", result.reason)
            // 10 points × 1.5 on the final total.
            assertEquals(15, result.score)
            assertEquals(listOf(1, 1, 0), listOf(result.correct, result.wrong, result.passed))
            assertEquals(listOf("a1-bread"), result.missedCards)
            val session = sessions.sessions.single()
            assertEquals(GameMode.HARDCORE, session.mode)
            assertEquals(15, session.score)
            assertFalse(session.completed)
            assertEquals(0, progress.progress.getValue(CardId("a1-bread")).box)
            // The game is over at once: no reveal, no ticker, nothing recorded twice.
            advance(10_000L)
            assertEquals(1, sessions.recordCalls)
            expectNoEvents()
        }
    }

    // endregion

    // region load errors

    @Test
    fun `an unreadable content shows the load error and retry starts the game`() = runTest {
        cards.failing = true
        val vm = startedIn(GameMode.CLASSIC)

        assertEquals(GameLoadError.Unreadable, vm.state.value.loadError)
        assertFalse(vm.state.value.isLoading)
        assertEquals(0, sessions.recordCalls)
        assertEquals(listOf("game not loaded"), crashReporter.recorded.map { it.second })

        cards.failing = false
        vm.onIntent(GameIntent.Retry)
        assertTrue(vm.state.value.isLoading)
        runCurrent()

        assertNull(vm.state.value.loadError)
        assertFalse(vm.state.value.isLoading)
        assertEquals('A', vm.state.value.turn?.letter)
        assertEquals(180_000L, vm.remainingMs)
        vm.onIntent(GameIntent.Background)
    }

    @Test
    fun `a wheel without cards shows the no-cards error and records nothing and leave goes back`() = runTest {
        val vm = viewModel(gameRoute = GameRoute(Level.B2, GameMode.CLASSIC))
        vm.effects.test {
            runCurrent()
            vm.onIntent(GameIntent.Foreground)

            assertEquals(GameLoadError.NoCards, vm.state.value.loadError)
            assertEquals(0, sessions.recordCalls)
            assertEquals(1, cards.reads)
            // Nothing to retry: the content is there, it just has no card for this choice.
            vm.onIntent(GameIntent.Retry)
            runCurrent()
            assertEquals(1, cards.reads)

            vm.onIntent(GameIntent.Leave)
            assertEquals(GameEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `leave does nothing while the game loads normally`() = runTest {
        val vm = startedIn(GameMode.CLASSIC)
        vm.effects.test {
            awaitStart()
            vm.onIntent(GameIntent.Leave)
            vm.onIntent(GameIntent.Retry)
            runCurrent()
            expectNoEvents()
            assertEquals(1, cards.reads)
            vm.onIntent(GameIntent.Background)
        }
    }

    @Test
    fun `a saved game that cannot be read keeps its snapshot and retry restores it paused`() = runTest {
        val handle = routeHandle()
        val first = started(handle)
        first.type("APPLE")
        first.onIntent(GameIntent.Background)
        val saved = assertNotNull(handle.get<String>(GameViewModel.KEY_GAME))

        cards.failing = true
        val restoredHandle = routeHandle(mapOf(GameViewModel.KEY_GAME to saved))
        val restored = viewModel(restoredHandle).also {
            runCurrent()
            it.onIntent(GameIntent.Foreground)
        }
        assertEquals(GameLoadError.Unreadable, restored.state.value.loadError)
        assertEquals(saved, restoredHandle.get<String>(GameViewModel.KEY_GAME))

        cards.failing = false
        restored.onIntent(GameIntent.Retry)
        runCurrent()

        assertNull(restored.state.value.loadError)
        assertTrue(restored.state.value.isPaused)
        assertEquals('B', restored.state.value.turn?.letter)
        assertEquals(10, restored.state.value.score)
    }

    // region prepared before the navigation (GamePreparer)

    @Test
    fun `a prepared game is shown in the first state and starts on resume`() = runTest {
        val preparer = preparer()
        preparer.prepare(route)
        val handle = routeHandle()

        val vm = viewModel(handle = handle, preparer = preparer)

        vm.state.test {
            val first = awaitItem()
            assertFalse(first.isLoading)
            assertTrue(first.isStarting)
            assertEquals('A', first.turn?.letter)
            assertNotNull(handle.get<String>(GameViewModel.KEY_GAME))
            assertEquals(config.classicTimeLimitSeconds, handle.get<Int>(GameViewModel.KEY_CLASSIC_LIMIT))

            vm.onIntent(GameIntent.Foreground)
            assertTrue(awaitItem().inputEnabled)
            vm.onIntent(GameIntent.Background)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a prepared game is the same game as a loaded one`() = runTest {
        val preparer = preparer()
        preparer.prepare(route)
        val prepared = viewModel(handle = routeHandle(), preparer = preparer)
        val loaded = viewModel(handle = routeHandle()).also { runCurrent() }

        assertEquals(loaded.state.value, prepared.state.value)
    }

    @Test
    fun `without a preparation the game loads as before`() = runTest {
        val preparer = preparer()

        val vm = viewModel(preparer = preparer)

        vm.state.test {
            assertTrue(awaitItem().isLoading)
            runCurrent()
            assertTrue(awaitItem().isStarting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a failed preparation shows the read error`() = runTest {
        cards.failing = true
        val preparer = preparer()
        preparer.prepare(route)

        val vm = viewModel(preparer = preparer)

        vm.state.test {
            assertEquals(GameLoadError.Unreadable, awaitItem().loadError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a preparation still running after the cap is awaited with loading`() = runTest {
        val preparer = preparer()
        preparer.prepare(route, cap = Duration.ZERO)

        val vm = viewModel(preparer = preparer)

        vm.state.test {
            assertTrue(awaitItem().isLoading)
            runCurrent()
            assertTrue(awaitItem().isStarting)
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(preparer.take(route))
    }

    @Test
    fun `a second preparation replaces a stale one`() = runTest {
        var seed = 1L
        val preparer = preparer(seeds = SeedSource { seed++ })
        val practice = GameRoute(Level.A1, GameMode.PRACTICE)
        preparer.prepare(practice)
        preparer.prepare(route)

        assertNull(preparer.take(practice))
        val handle = routeHandle()
        viewModel(handle = handle, preparer = preparer).state.test {
            assertTrue(awaitItem().isStarting)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2L, SavedGameCodec.decodeOrNull(checkNotNull(handle.get<String>(GameViewModel.KEY_GAME)))?.seed)
    }

    @Test
    fun `discard drops only the preparation of its route`() = runTest {
        val preparer = preparer()
        preparer.prepare(route)

        preparer.discard(GameRoute(Level.A1, GameMode.PRACTICE))
        assertNotNull(preparer.take(route))

        preparer.prepare(route)
        preparer.discard(route)
        assertNull(preparer.take(route))
    }

    @Test
    fun `a cancelled preparation falls back to loading the game`() = runTest {
        val scope = CoroutineScope(backgroundScope.coroutineContext + Job())
        val preparer = GamePreparer(loader(), scope)
        preparer.prepare(route, cap = Duration.ZERO)
        val vm = viewModel(preparer = preparer)

        scope.cancel()
        runCurrent()

        assertTrue(vm.state.value.isStarting)
        assertEquals('A', vm.state.value.turn?.letter)
    }

    @Test
    fun `a saved game is restored even when a preparation for the route is waiting`() = runTest {
        val firstHandle = routeHandle()
        val first = started(firstHandle)
        val snapshot = checkNotNull(firstHandle.get<String>(GameViewModel.KEY_GAME))
        first.onIntent(GameIntent.Background)
        val preparer = preparer()
        preparer.prepare(route)

        val restored = viewModel(handle = routeHandle(mapOf(GameViewModel.KEY_GAME to snapshot)), preparer = preparer)
        runCurrent()

        assertTrue(restored.state.value.isPaused)
        assertNotNull(preparer.take(route))
    }

    // endregion

    // region DAILY mode tests

    @Test
    fun `daily mode without GetDailyWheel fails with Unreadable load error`() = runTest {
        val dailyRoute = GameRoute(Level.A1, GameMode.DAILY)
        val vm = viewModel(
            handle = routeHandle(mapOf("mode" to "DAILY")),
            gameRoute = dailyRoute,
            getDailyWheel = null,
        )
        runCurrent()
        assertEquals(GameLoadError.Unreadable, vm.state.value.loadError)
    }

    @Test
    fun `daily mode when already played sends Abandoned effect`() = runTest {
        val dailyRepo = FakeDailyRepository()
        val today = appClock.epochDay(appClock.nowMs())
        dailyRepo.record(dev.talhabayhan.lexi.core.domain.session.DailyResult(today, Level.A1, 200, 60000))
        val getDailyWheel = GetDailyWheel(cards, FakeContentVersionRepository(1), dailyRepo, appClock)

        val dailyRoute = GameRoute(Level.A1, GameMode.DAILY)
        val vm = viewModel(
            handle = routeHandle(mapOf("mode" to "DAILY")),
            gameRoute = dailyRoute,
            getDailyWheel = getDailyWheel,
        )
        vm.effects.test {
            runCurrent()
            assertEquals(GameEffect.Abandoned, awaitItem())
        }
    }

    @Test
    fun `daily mode when no cards available shows NoCards load error`() = runTest {
        val emptyCards = FakeCardRepository(emptyList())
        val getDailyWheel = GetDailyWheel(emptyCards, FakeContentVersionRepository(1), FakeDailyRepository(), appClock)

        val dailyRoute = GameRoute(Level.A1, GameMode.DAILY)
        val vm = viewModel(
            handle = routeHandle(mapOf("mode" to "DAILY")),
            gameRoute = dailyRoute,
            getDailyWheel = getDailyWheel,
        )
        runCurrent()
        assertEquals(GameLoadError.NoCards, vm.state.value.loadError)
    }

    @Test
    fun `daily mode when ready loads wheel and starts successfully`() = runTest {
        val alphabetCards = Level.entries.flatMap { dev.talhabayhan.lexi.core.testing.testAlphabetPool(it) }
        val poolRepo = FakeCardRepository(alphabetCards)
        val getDailyWheel = GetDailyWheel(poolRepo, FakeContentVersionRepository(1), FakeDailyRepository(), appClock)

        val dailyRoute = GameRoute(Level.A1, GameMode.DAILY)
        val vm = viewModel(
            handle = routeHandle(mapOf("mode" to "DAILY")),
            gameRoute = dailyRoute,
            getDailyWheel = getDailyWheel,
        )
        runCurrent()
        vm.onIntent(GameIntent.Foreground)
        assertNull(vm.state.value.loadError)
        assertFalse(vm.state.value.isLoading)
        assertEquals(26, vm.state.value.wheel.size)
    }

    // endregion

    private fun decodeFinished(snapshot: String): GameStatus {
        val saved = checkNotNull(SavedGameCodec.decodeOrNull(snapshot))
        return checkNotNull(saved.restore(cards.cards, GameEngine(config))).status
    }

    /** Delays every save, like a slow disk; the write must still happen after the ViewModel is cleared. */
    private class SlowJokerRepository(private val delegate: FakeJokerRepository) : JokerRepository by delegate {
        override suspend fun save(inventory: JokerInventory) {
            delay(DELAY_MS)
            delegate.save(inventory)
        }

        companion object {
            const val DELAY_MS = 1_000L
        }
    }

    private companion object {
        const val SEED = 2026L
    }
}
