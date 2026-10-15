package dev.talhabayhan.lexi.feature.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
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
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeJokerRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeSpeaker
import dev.talhabayhan.lexi.core.testing.testCard
import dev.talhabayhan.lexi.feature.game.result.ResultRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private fun TestScope.viewModel(
        handle: SavedStateHandle = routeHandle(),
        jokerRepository: JokerRepository = jokers,
        sessionRepository: SessionRepository = sessions,
    ): GameViewModel = GameViewModel(
        route = route,
        handle = handle,
        cards = cards,
        progress = progress,
        jokers = jokerRepository,
        settings = settings,
        finishGame = FinishGame(sessionRepository, progress, appClock, config),
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

    /** Skips to the navigation to the result and returns its payload. */
    private suspend fun ReceiveTurbine<GameEffect>.awaitResult(): ResultRoute {
        while (true) {
            val effect = awaitItem()
            if (effect is GameEffect.NavigateToResult) return effect.result
        }
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

    // region joker sheet (05-joker-sheet)

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
        assertEquals('A', checkNotNull(ui.turn).words.single().first().letter)
        assertEquals(AnswerSlotState.JokerFilled, checkNotNull(ui.turn).words.single().first().state)
        assertEquals(170_000L, vm.remainingMs)
        assertEquals(1, ui.jokers.first { it.type == JokerType.REVEAL_LETTER }.count)
        assertTrue(ui.jokers.none { it.enabled }, "one joker per slot")
        // Saved without waiting for the scheduler (undispatched, NonCancellable).
        assertEquals(1, jokers.inventory.value?.count(JokerType.REVEAL_LETTER))
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
            assertEquals(2, result.wrong)
            assertEquals(listOf("a1-bread", "a1-chair"), result.missedCards)
            assertEquals(1, sessions.recordCalls)
            cancelAndIgnoreRemainingEvents()
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
