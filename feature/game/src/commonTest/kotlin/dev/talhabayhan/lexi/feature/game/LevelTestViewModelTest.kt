package dev.talhabayhan.lexi.feature.game

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
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

/** GAME_RULES.md §9: the level test run on the game screen (LevelTestRunner behind a ViewModel; records nothing). */
@OptIn(ExperimentalCoroutinesApi::class)
class LevelTestViewModelTest {
    private val config = GameConfig()

    /** Every level has a "<letter>ON" card per letter: "ON" typed after the given letter is right, "NO" is wrong. */
    private val pool: List<Card> = Level.entries.flatMap { level ->
        ('A'..'Z').map { testCard("${it}ON", level = level) }
    }
    private val cards = FakeCardRepository(pool)
    private val settings = FakeSettingsRepository()
    private val appClock = FakeAppClock()
    private val speaker = FakeSpeaker()
    private var seedsDrawn = 0

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(handle: SavedStateHandle = SavedStateHandle()) = LevelTestViewModel(
        handle = handle,
        cards = cards,
        settings = settings,
        appClock = appClock,
        speaker = speaker,
        config = config,
        timeSource = testScheduler.timeSource,
        seeds = SeedSource {
            seedsDrawn++
            SEED
        },
        cpu = StandardTestDispatcher(testScheduler),
    )

    /** Loaded and resumed: the first card is playing. */
    private fun TestScope.started(handle: SavedStateHandle = SavedStateHandle()) = viewModel(handle).also {
        runCurrent()
        it.onIntent(GameIntent.Foreground)
    }

    private fun TestScope.advance(ms: Long) {
        advanceTimeBy(ms)
        runCurrent()
    }

    private val LevelTestViewModel.remainingMs: Long get() = clock.value.remainingMs

    /** Types the open letters of the current "<letter>ON" card ("ON"), or a wrong answer ("NO"). */
    private fun LevelTestViewModel.answer(correct: Boolean) {
        (if (correct) "ON" else "NO").forEach { letter ->
            val keys = checkNotNull(state.value.turn).keys
            val index = keys.indices.first { keys[it].letter == letter && keys[it].state == LetterKeyState.Normal }
            onIntent(GameIntent.KeyTapped(index))
        }
    }

    private suspend fun ReceiveTurbine<LevelTestEffect>.awaitNonMotion(): LevelTestEffect {
        while (true) {
            val effect = awaitItem()
            if (effect !is LevelTestEffect.Motion) return effect
        }
    }

    @Test
    fun `the first card of the start level is shown and starts on resume`() = runTest {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)
        assertTrue(vm.state.value.isLoading)
        runCurrent()
        // Loaded, not resumed: the card looks ready but takes no input.
        val before = vm.state.value
        assertTrue(before.isStarting)
        assertFalse(before.inputEnabled)
        assertEquals(Level.A2, before.turn?.level)
        assertNotNull(handle.get<String>(LevelTestViewModel.KEY_RUN))

        vm.onIntent(GameIntent.Foreground)
        val state = vm.state.value
        assertFalse(state.isStarting)
        assertTrue(state.inputEnabled)
        assertTrue(state.isLevelTest)
        assertFalse(state.isScored)
        assertFalse(state.canRestart)
        assertTrue(state.jokers.isEmpty())
        assertEquals(before.turn, state.turn)
        val letter = checkNotNull(state.turn).letter
        assertEquals(listOf(letter to WheelLetterState.Current), state.wheel.map { it.letter to it.state })
        assertEquals(120_000L, vm.remainingMs)
        advance(1_000L)
        assertEquals(119_000L, vm.remainingMs)
    }

    @Test
    fun `a correct answer goes to the next card and the wheel grows`() = runTest {
        val vm = started()
        vm.effects.test {
            val first = checkNotNull(vm.state.value.turn).letter
            vm.answer(correct = true)
            runCurrent()
            // Each card's start moves the wheel to its letter (Advanced), like a game's start.
            val effects = buildList { repeat(5) { add((awaitItem() as LevelTestEffect.Motion).effect) } }
            assertEquals(GameEffect.Advanced(first), effects[0])
            assertTrue(effects.subList(1, 3).all { it is GameEffect.LetterPlaced })
            val correct = effects[3] as GameEffect.Correct
            assertEquals(first, correct.letter)
            assertEquals(0, correct.points)
            assertEquals(GameEffect.Advanced(checkNotNull(vm.state.value.turn).letter), effects[4])

            val state = vm.state.value
            assertEquals(2, state.wheel.size)
            assertEquals(first to WheelLetterState.Correct, state.wheel[0].letter to state.wheel[0].state)
            assertEquals(WheelLetterState.Current, state.wheel[1].state)
            assertEquals(state.turn?.letter, state.wheel[1].letter)
            assertEquals(1, state.correct)
            assertTrue(state.inputEnabled)

            // Two in a row at A2: one level up; the badge still reads TEST (isLevelTest), never the card's level.
            vm.answer(correct = true)
            assertEquals(Level.B1, vm.state.value.turn?.level)
            assertEquals(3, vm.state.value.wheel.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a wrong answer is revealed without using the clock and then the next card starts`() = runTest {
        val vm = started()
        vm.answer(correct = false)
        assertTrue(checkNotNull(vm.state.value.turn).isRevealing)
        assertFalse(vm.state.value.inputEnabled)
        assertEquals(WheelLetterState.Wrong, vm.state.value.wheel.single().state)
        advance(config.revealMs)
        val state = vm.state.value
        assertEquals(listOf(WheelLetterState.Wrong, WheelLetterState.Current), state.wheel.map { it.state })
        assertFalse(checkNotNull(state.turn).isRevealing)
        assertEquals(Level.A1, state.turn?.level)
        assertEquals(120_000L, vm.remainingMs)
    }

    @Test
    fun `time up sends time up and then the level from the answers so far`() = runTest {
        val vm = started()
        vm.effects.test {
            advance(120_000L)
            var timeUp = false
            var finished: LevelTestEffect.Finished? = null
            while (finished == null) {
                when (val effect = awaitItem()) {
                    is LevelTestEffect.Motion -> if (effect.effect == GameEffect.TimeUp) timeUp = true
                    is LevelTestEffect.Finished -> finished = effect
                    else -> error("unexpected $effect")
                }
            }
            assertTrue(timeUp)
            // Nothing answered: the level below the start level.
            assertEquals(Level.A1, finished.level)
            assertFalse(vm.state.value.inputEnabled)
            assertEquals(0L, vm.remainingMs)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `background pauses the clock and foreground keeps the test paused`() = runTest {
        val vm = started()
        advance(3_000L)
        vm.onIntent(GameIntent.Background)
        assertTrue(vm.state.value.isPaused)
        assertEquals(117_000L, vm.remainingMs)
        advance(5_000L)
        vm.onIntent(GameIntent.Foreground)
        assertTrue(vm.state.value.isPaused)
        assertEquals(117_000L, vm.remainingMs)
        vm.onIntent(GameIntent.Resume)
        advance(1_000L)
        assertEquals(116_000L, vm.remainingMs)
    }

    @Test
    fun `a restored run comes back paused with the same board and time`() = runTest {
        val handle = SavedStateHandle()
        val first = started(handle)
        first.answer(correct = true)
        advance(2_050L)
        first.answer(correct = false)
        advance(config.revealMs)
        advance(500L)
        first.onIntent(GameIntent.Background)
        val saved = assertNotNull(handle.get<String>(LevelTestViewModel.KEY_RUN))

        val restoredHandle = SavedStateHandle(mapOf(LevelTestViewModel.KEY_RUN to saved))
        val restored = viewModel(restoredHandle)
        runCurrent()
        restored.onIntent(GameIntent.Foreground)

        assertEquals(1, seedsDrawn)
        assertEquals(first.state.value, restored.state.value)
        assertTrue(restored.state.value.isPaused)
        assertEquals(3, restored.state.value.wheel.size)
        assertEquals(first.remainingMs, restored.remainingMs)
        assertEquals(117_450L, restored.remainingMs)
        advance(2_000L)
        assertEquals(117_450L, restored.remainingMs)

        restored.onIntent(GameIntent.Resume)
        advance(1_000L)
        assertEquals(116_450L, restored.remainingMs)
        restored.answer(correct = true)
        assertEquals(4, restored.state.value.wheel.size)
    }

    @Test
    fun `a pool without cards of the start level shows the test error and back quits`() = runTest {
        cards.cards = pool.filter { it.level == Level.A1 }
        val handle = SavedStateHandle()
        val vm = viewModel(handle)
        runCurrent()
        vm.onIntent(GameIntent.Foreground)
        assertEquals(GameLoadError.LevelTestUnavailable, vm.state.value.loadError)
        assertFalse(vm.state.value.isLoading)
        assertNull(handle.get<String>(LevelTestViewModel.KEY_RUN))
        vm.effects.test {
            vm.onIntent(GameIntent.Retry)
            vm.onIntent(GameIntent.Leave)
            runCurrent()
            assertEquals(LevelTestEffect.Quit, awaitItem())
            expectNoEvents()
        }
        assertEquals(GameLoadError.LevelTestUnavailable, vm.state.value.loadError)
    }

    @Test
    fun `an unreadable content shows the read error and retry loads the test`() = runTest {
        cards.failing = true
        val vm = viewModel()
        runCurrent()
        assertEquals(GameLoadError.Unreadable, vm.state.value.loadError)
        cards.failing = false
        vm.onIntent(GameIntent.Retry)
        runCurrent()
        vm.onIntent(GameIntent.Foreground)
        assertNull(vm.state.value.loadError)
        assertTrue(vm.state.value.inputEnabled)
    }

    @Test
    fun `jokers and restart are refused`() = runTest {
        val vm = started()
        val before = vm.state.value
        vm.onIntent(GameIntent.OpenJokers)
        vm.onIntent(GameIntent.UseJoker(JokerType.REVEAL_LETTER))
        assertEquals(before, vm.state.value)
        vm.onIntent(GameIntent.Pause)
        vm.onIntent(GameIntent.RequestRestart)
        assertFalse(vm.state.value.isRestartConfirmation)
    }

    @Test
    fun `giving up asks for confirmation and quits without saving anything`() = runTest {
        val handle = SavedStateHandle()
        val vm = started(handle)
        vm.answer(correct = true)
        vm.effects.test {
            // Not offered while running.
            vm.onIntent(GameIntent.RequestQuit)
            assertFalse(vm.state.value.isQuitConfirmation)
            vm.onIntent(GameIntent.Back)
            assertTrue(vm.state.value.isPaused)
            vm.onIntent(GameIntent.RequestQuit)
            assertTrue(vm.state.value.isQuitConfirmation)
            vm.onIntent(GameIntent.Back)
            assertFalse(vm.state.value.isQuitConfirmation)
            vm.onIntent(GameIntent.RequestQuit)
            vm.onIntent(GameIntent.Forfeit)
            runCurrent()
            assertEquals(LevelTestEffect.Quit, awaitNonMotion())
            cancelAndIgnoreRemainingEvents()
        }
        assertNull(handle.get<String>(LevelTestViewModel.KEY_RUN))
        assertEquals(1, cards.reads)
        assertEquals(Settings(), settings.settings.value)
    }

    @Test
    fun `a whole test ends with its level and records nothing`() = runTest {
        val vm = started()
        vm.effects.test {
            // All correct: up two levels per two answers until NAT, then the early stop (GAME_RULES.md §9).
            var finished: LevelTestEffect.Finished? = null
            while (finished == null) {
                if (vm.state.value.inputEnabled) vm.answer(correct = true)
                runCurrent()
                while (finished == null) {
                    val effect = awaitItem()
                    if (effect is LevelTestEffect.Finished) finished = effect
                    if (effect is LevelTestEffect.Motion && effect.effect is GameEffect.Correct) break
                }
            }
            assertEquals(Level.NAT, finished.level)
            assertEquals(11, vm.state.value.wheel.size)
            assertEquals(11, vm.state.value.correct)
            assertFalse(vm.state.value.inputEnabled)
            cancelAndIgnoreRemainingEvents()
        }
        // The ViewModel has no recording dependency: one content read, settings untouched.
        assertEquals(1, cards.reads)
        assertEquals(Settings(), settings.settings.value)
        assertEquals(11, speaker.spoken.size)
    }

    private companion object {
        const val SEED = 7L
    }
}
