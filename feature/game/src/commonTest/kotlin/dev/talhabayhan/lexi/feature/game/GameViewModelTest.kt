package dev.talhabayhan.lexi.feature.game

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.game.SavedGameCodec
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.FinishGame
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeJokerRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeSpeaker
import dev.talhabayhan.lexi.core.testing.testCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private fun TestScope.viewModel(handle: SavedStateHandle = routeHandle()): GameViewModel = GameViewModel(
        route = route,
        handle = handle,
        cards = cards,
        progress = progress,
        jokers = jokers,
        settings = settings,
        finishGame = FinishGame(sessions, progress, appClock, config),
        appClock = appClock,
        speaker = speaker,
        config = config,
        timeSource = testScheduler.timeSource,
        seeds = SeedSource { SEED },
    )

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

    @Test
    fun `the game starts when the loaded screen is resumed`() = runTest {
        val vm = viewModel()
        runCurrent()
        assertTrue(vm.state.value.isLoading)

        vm.onIntent(GameIntent.Foreground)

        assertFalse(vm.state.value.isLoading)
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
            vm.type("AP")
            assertEquals(GameEffect.LetterPlaced(0), awaitItem())
            assertEquals(GameEffect.LetterPlaced(1), awaitItem())

            vm.onIntent(GameIntent.Backspace)

            val cells = checkNotNull(vm.state.value.turn).words.single()
            assertEquals(listOf('A', null, null, null, null), cells.map { it.letter })
            assertEquals(AnswerSlotState.Active, cells[1].state)
        }
    }

    @Test
    fun `a correct answer scores and speaks the word and moves on`() = runTest {
        val vm = started()

        vm.type("APPLE")

        assertEquals(10, vm.state.value.score)
        assertEquals(listOf("apple"), speaker.spoken)
        assertEquals('B', vm.state.value.turn?.letter)
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
    fun `game over records the session exactly once and then navigates`() = runTest {
        val handle = routeHandle()
        val vm = started(handle)
        vm.effects.test {
            vm.type("APPLE")
            vm.type("BREAD")
            vm.type("CHAIR")
            runCurrent()

            skipUntil(GameEffect.NavigateToResult)
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
    fun `time up finishes the game once and stops the ticker`() = runTest {
        val vm = started()
        vm.effects.test {
            advance(180_000L)

            skipUntil(GameEffect.NavigateToResult)
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

    private companion object {
        const val SEED = 2026L
    }
}
