package dev.talhabayhan.lexi.feature.game.duel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.MatchPhase
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.testing.FakeAnalyticsTracker
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.FakeSpeaker
import dev.talhabayhan.lexi.core.testing.testCard
import dev.talhabayhan.lexi.feature.game.GameEffect
import dev.talhabayhan.lexi.feature.game.GameIntent
import dev.talhabayhan.lexi.feature.game.GameLoadError
import dev.talhabayhan.lexi.feature.game.SeedSource
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
class DuelViewModelTest {
    private val config = GameConfig()

    /** Each clue is the answer, so a test can read the answer of the current turn from the board. */
    private val pool: List<Card> = listOf("APPLE", "BREAD", "CHAIR", "DOG", "EGG").map { testCard(it).copy(clue = it) }
    private val cards = FakeCardRepository(pool)
    private val settings = FakeSettingsRepository()
    private val appClock = FakeAppClock()
    private val speaker = FakeSpeaker()
    private val analytics = FakeAnalyticsTracker()
    private val crashReporter = FakeCrashReporter()
    private val route = DuelRoute(Level.A1, clockSeconds = 90, names = listOf("Ayşe", ""))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(
        handle: SavedStateHandle = SavedStateHandle(),
        repository: FakeCardRepository = cards,
    ) = DuelViewModel(
        route = route,
        handle = handle,
        loader = DuelLoader(repository, config, SeedSource { SEED }, cpu = StandardTestDispatcher(testScheduler)),
        settings = settings,
        appClock = appClock,
        speaker = speaker,
        timeSource = testScheduler.timeSource,
        analytics = analytics,
        crashReporter = crashReporter,
    )

    /** Loaded and resumed, on the first handover card. */
    private fun TestScope.loaded(handle: SavedStateHandle = SavedStateHandle()): DuelViewModel =
        viewModel(handle).also {
            runCurrent()
            it.onIntent(DuelIntent.Board(GameIntent.Foreground))
        }

    private fun TestScope.advance(ms: Long) {
        advanceTimeBy(ms)
        runCurrent()
    }

    private fun DuelViewModel.board(intent: GameIntent) = onIntent(DuelIntent.Board(intent))

    private val DuelViewModel.answer: String get() = checkNotNull(state.value.board.turn).clue

    /** Types [word] with the board's keys (the given first letter is skipped, like a player does). */
    private fun DuelViewModel.type(word: String) {
        val cells = checkNotNull(state.value.board.turn).words.flatten()
        val given = cells.indexOfFirst { it.state == AnswerSlotState.Given }
        val letters = if (given >= 0 &&
            word.getOrNull(given) == cells[given].letter
        ) {
            word.removeRange(given, given + 1)
        } else {
            word
        }
        letters.forEach { letter ->
            val keys = checkNotNull(state.value.board.turn).keys
            board(
                GameIntent.KeyTapped(
                    keys.indices.first {
                        keys[it].letter == letter &&
                            keys[it].state == LetterKeyState.Normal
                    },
                ),
            )
        }
    }

    /** Fills the answer with the available keys in an order that is not the answer. */
    private fun DuelViewModel.typeWrong() {
        val answer = answer
        while (state.value.board.inputEnabled && !checkNotNull(state.value.board.turn).isRevealing) {
            val turn = checkNotNull(state.value.board.turn)
            val cells = turn.words.flatten()
            val position = cells.indexOfFirst { it.letter == null }
            if (position < 0) break
            val keys = turn.keys.indices.filter { turn.keys[it].state == LetterKeyState.Normal }
            val pick = keys.firstOrNull { turn.keys[it].letter != answer[position] } ?: keys.first()
            board(GameIntent.KeyTapped(pick))
        }
    }

    private fun DuelViewModel.remainingMs(player: Int): Long = state.value.players[player].remainingMs

    private suspend fun ReceiveTurbine<DuelEffect>.awaitResult(): DuelResultRoute {
        while (true) {
            val effect = awaitItem()
            if (effect is DuelEffect.NavigateToResult) return effect.result
        }
    }

    @Test
    fun `the match opens on the first player's handover card and nothing runs until HAZIRIM`() = runTest {
        val vm = loaded()
        advance(5_000)

        val state = vm.state.value
        assertEquals(MatchPhase.HANDOVER, state.phase)
        assertEquals(0, state.activePlayer)
        assertEquals(listOf("Ayşe", ""), state.players.map { it.name })
        val handover = assertNotNull(state.handover)
        assertEquals(90_000L, handover.remainingMs)
        assertNull(handover.previous)
        assertEquals(listOf(90_000L, 90_000L), state.players.map { it.remainingMs })
        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.DuelStart(90)), analytics.events)
    }

    @Test
    fun `HAZIRIM starts only the active player's clock`() = runTest {
        val vm = loaded()
        vm.onIntent(DuelIntent.Ready)
        advance(3_000)

        assertEquals(MatchPhase.PLAYING, vm.state.value.phase)
        assertEquals(87_000L, vm.clock.value.remainingMs)
        assertEquals(90_000L, vm.remainingMs(1))
        assertFalse(vm.state.value.board.jokers.any { it.enabled })
        assertFalse(vm.state.value.board.canRestart)
    }

    @Test
    fun `HAZIRIM is ignored in the background`() = runTest {
        val vm = viewModel()
        runCurrent()
        vm.onIntent(DuelIntent.Ready)

        assertEquals(MatchPhase.HANDOVER, vm.state.value.phase)
    }

    @Test
    fun `a correct answer keeps the turn and a pass hands it over with the previous player's line`() = runTest {
        val vm = loaded()
        vm.onIntent(DuelIntent.Ready)
        vm.type(vm.answer)
        runCurrent()
        assertEquals(MatchPhase.PLAYING, vm.state.value.phase)
        assertEquals(1, vm.state.value.players[0].correct)

        advance(1_000)
        vm.board(GameIntent.Pass)
        val state = vm.state.value
        assertEquals(MatchPhase.HANDOVER, state.phase)
        assertEquals(1, state.activePlayer)
        assertEquals(DuelPreviousUi(player = 0, correct = 1, wrong = 0), state.handover?.previous)
        assertEquals(false, state.handover?.previousTimeUp)

        val stopped = vm.remainingMs(0)
        advance(4_000)
        assertEquals(stopped, vm.remainingMs(0))
        assertEquals(90_000L, vm.state.value.handover?.remainingMs)
    }

    @Test
    fun `a wrong answer ends the turn once its reveal is over`() = runTest {
        val vm = loaded()
        vm.onIntent(DuelIntent.Ready)
        vm.typeWrong()
        runCurrent()
        assertEquals(MatchPhase.PLAYING, vm.state.value.phase)
        assertTrue(checkNotNull(vm.state.value.board.turn).isRevealing)

        advance(config.revealMs + 200)
        assertEquals(MatchPhase.HANDOVER, vm.state.value.phase)
        assertEquals(DuelPreviousUi(player = 0, correct = 0, wrong = 1), vm.state.value.handover?.previous)
    }

    @Test
    fun `the second player's game starts on their first turn with a full clock`() = runTest {
        val vm = loaded()
        vm.onIntent(DuelIntent.Ready)
        advance(2_000)
        vm.board(GameIntent.Pass)
        vm.onIntent(DuelIntent.Ready)
        advance(1_000)

        assertEquals(1, vm.state.value.activePlayer)
        assertEquals(89_000L, vm.clock.value.remainingMs)
        assertEquals(88_000L, vm.remainingMs(0))
    }

    @Test
    fun `the background pauses the active clock and the match stays paused on return`() = runTest {
        val vm = loaded()
        vm.onIntent(DuelIntent.Ready)
        advance(1_000)
        vm.board(GameIntent.Background)
        advance(10_000)
        vm.board(GameIntent.Foreground)
        advance(10_000)

        assertTrue(vm.state.value.board.isPaused)
        assertEquals(89_000L, vm.clock.value.remainingMs)

        vm.board(GameIntent.Resume)
        advance(1_000)
        assertEquals(88_000L, vm.clock.value.remainingMs)
    }

    @Test
    fun `system back pauses a running board and asks before giving up on the handover card`() = runTest {
        val vm = loaded()
        vm.board(GameIntent.Back)
        assertEquals(true, vm.state.value.handover?.isQuitConfirmation)
        vm.board(GameIntent.Back)
        assertEquals(false, vm.state.value.handover?.isQuitConfirmation)

        vm.onIntent(DuelIntent.Ready)
        vm.board(GameIntent.Back)
        assertTrue(vm.state.value.board.isPaused)
        // On the pause dialog back does nothing: the clock never restarts without a tap.
        vm.board(GameIntent.Back)
        assertTrue(vm.state.value.board.isPaused)
        assertEquals(MatchPhase.PLAYING, vm.state.value.phase)
    }

    @Test
    fun `HAZIRIM is ignored while the give-up confirmation is open`() = runTest {
        val vm = loaded()
        vm.board(GameIntent.RequestQuit)
        vm.onIntent(DuelIntent.Ready)

        assertEquals(MatchPhase.HANDOVER, vm.state.value.phase)
    }

    @Test
    fun `giving up on the handover card ends the match and the other player wins`() = runTest {
        val vm = loaded()
        vm.effects.test {
            vm.onIntent(DuelIntent.Ready)
            vm.board(GameIntent.Pass)
            // Player 2 gives up before their first turn.
            vm.board(GameIntent.RequestQuit)
            vm.board(GameIntent.Forfeit)

            val outcome = awaitResult().outcome
            assertEquals(DuelEnd.FORFEIT, outcome.end)
            assertEquals(0, outcome.winner)
            assertEquals(listOf("Ayşe", ""), outcome.players.map { it.name })
            assertEquals(90, outcome.clockSeconds)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(
            AnalyticsEvent.DuelFinish(AnalyticsEvent.DuelResultType.FORFEIT),
            analytics.events.last(),
        )
    }

    @Test
    fun `Forfeit without the confirmation is ignored`() = runTest {
        val vm = loaded()
        vm.board(GameIntent.Forfeit)

        assertEquals(MatchPhase.HANDOVER, vm.state.value.phase)
    }

    @Test
    fun `giving up from the pause dialog ends the match`() = runTest {
        val vm = loaded()
        vm.effects.test {
            vm.onIntent(DuelIntent.Ready)
            vm.board(GameIntent.Pause)
            vm.board(GameIntent.RequestQuit)
            assertTrue(vm.state.value.board.isQuitConfirmation)
            vm.board(GameIntent.Forfeit)

            val outcome = awaitResult().outcome
            assertEquals(DuelEnd.FORFEIT, outcome.end)
            assertEquals(1, outcome.winner)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `both clocks at zero end the match with a time-up result and the time-up is announced at the handover`() =
        runTest {
            val vm = loaded()
            vm.effects.test {
                vm.onIntent(DuelIntent.Ready)
                vm.type(vm.answer)
                advance(90_000)
                assertEquals(MatchPhase.HANDOVER, vm.state.value.phase)
                assertEquals(true, vm.state.value.handover?.previousTimeUp)

                vm.onIntent(DuelIntent.Ready)
                advance(90_000)

                val outcome = awaitResult().outcome
                assertEquals(DuelEnd.TIME_UP, outcome.end)
                assertEquals(0, outcome.winner)
                assertEquals(listOf(1, 0), outcome.players.map { it.correct })
                assertEquals(listOf(0L, 0L), outcome.players.map { it.remainingMs })
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(
                AnalyticsEvent.DuelFinish(AnalyticsEvent.DuelResultType.WIN),
                analytics.events.last(),
            )
        }

    @Test
    fun `a finished turn plays its motion`() = runTest {
        val vm = loaded()
        vm.effects.test {
            vm.onIntent(DuelIntent.Ready)
            vm.board(GameIntent.Pass)
            var passed = false
            while (!passed) passed = (awaitItem() as? DuelEffect.Motion)?.effect is GameEffect.Passed
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a match recreated after a process death is dropped`() = runTest {
        val handle = SavedStateHandle()
        loaded(handle)
        assertEquals(true, handle.get<Boolean>(DuelViewModel.KEY_ACTIVE))

        val recreated = viewModel(handle)
        recreated.effects.test {
            assertEquals(DuelEffect.Abandoned, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a level without cards shows the no-cards error and a read failure offers a retry`() = runTest {
        val empty = viewModel(repository = FakeCardRepository())
        runCurrent()
        assertEquals(GameLoadError.NoCards, empty.state.value.loadError)

        val failing = FakeCardRepository(pool).apply { this.failing = true }
        val vm = viewModel(repository = failing)
        runCurrent()
        assertEquals(GameLoadError.Unreadable, vm.state.value.loadError)
        failing.failing = false
        vm.board(GameIntent.Retry)
        runCurrent()
        assertNull(vm.state.value.loadError)
        assertEquals(MatchPhase.HANDOVER, vm.state.value.phase)
    }

    private companion object {
        const val SEED = 42L
    }
}
