package dev.talhabayhan.lexi.feature.stats

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ContentIndex
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.repository.CardRepository
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSessionRepository
import dev.talhabayhan.lexi.core.testing.testCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {
    private val clock = FakeAppClock()
    private val today = clock.epochDay(clock.nowMs())
    private val cards = FakeCardRepository(
        listOf(
            testCard("EGG"),
            testCard("APPLE"),
            testCard("XRAY"),
            testCard("QUEEN"),
            testCard("YOGA", level = Level.A2),
            testCard("VASE", level = Level.A2),
        ),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun progress(id: String, correct: Int, wrong: Int) =
        CardProgress(CardId(id), seen = correct + wrong, correct = correct, wrong = wrong)

    private fun session(daysAgo: Int, completed: Boolean, correct: Int) = SessionResult(
        mode = GameMode.CLASSIC,
        level = Level.A1,
        category = null,
        seed = 1,
        startedAtMs = 0,
        endedAtMs = 0,
        epochDay = today - daysAgo,
        score = 0,
        correct = correct,
        wrong = 0,
        passed = 0,
        completed = completed,
    )

    private suspend fun FakeSessionRepository.record(vararg sessions: SessionResult) = sessions.forEach { record(it) }

    private fun viewModel(
        progress: FakeProgressRepository = FakeProgressRepository(),
        sessions: FakeSessionRepository = FakeSessionRepository(),
        cards: CardRepository = this.cards,
    ) = StatsViewModel(progress, sessions, cards, clock)

    private val played = listOf(
        progress("a1-egg", correct = 4, wrong = 1),
        progress("a1-apple", correct = 2, wrong = 0),
        progress("a1-xray", correct = 1, wrong = 4),
        progress("a1-queen", correct = 2, wrong = 3),
        progress("a2-yoga", correct = 0, wrong = 6),
    )

    @Test
    fun `nothing played shows the empty state`() {
        val state = viewModel().state.value

        assertTrue(state.isLoaded)
        assertTrue(state.isEmpty)
        assertFalse(state.hasError)
        assertNull(state.accuracyPercent)
    }

    @Test
    fun `the numbers come from the stored progress and sessions`() = runTest {
        val sessions = FakeSessionRepository()
        sessions.record(
            session(daysAgo = 10, completed = true, correct = 7),
            session(daysAgo = 3, completed = false, correct = 3),
            session(daysAgo = 1, completed = true, correct = 10),
            session(daysAgo = 0, completed = true, correct = 5),
        )

        val state = viewModel(FakeProgressRepository(played), sessions).state.value

        assertFalse(state.isEmpty)
        assertEquals(4, state.learnedWords)
        // 9 correct of 23 answers.
        assertEquals(39, state.accuracyPercent)
        assertEquals(2, state.longestStreak)
        assertEquals(3, state.completedWheels)
    }

    @Test
    fun `the week lists the last seven days oldest first and ends today`() = runTest {
        val sessions = FakeSessionRepository()
        sessions.record(
            session(daysAgo = 10, completed = true, correct = 7),
            session(daysAgo = 3, completed = false, correct = 3),
            session(daysAgo = 1, completed = true, correct = 10),
            session(daysAgo = 0, completed = true, correct = 5),
        )

        val state = viewModel(FakeProgressRepository(played), sessions).state.value

        // 2026-10-15 is a Thursday (weekday 3): the week runs Friday ... Thursday.
        assertEquals(listOf(4, 5, 6, 0, 1, 2, 3), state.week.map { it.weekday })
        assertEquals(listOf(0, 0, 0, 3, 0, 10, 5), state.week.map { it.words })
        assertEquals(18, state.weekTotal)
    }

    @Test
    fun `levels count the learned cards against the level totals`() {
        val state = viewModel(FakeProgressRepository(played)).state.value

        assertEquals(
            listOf(LevelStatUi(Level.A1, 4, 4), LevelStatUi(Level.A2, 0, 2)),
            state.levels,
        )
        assertEquals(0f, state.levels[1].fraction)
        assertEquals(1f, state.levels[0].fraction)
    }

    @Test
    fun `hardest letters need five answers and come hardest first`() {
        val state = viewModel(FakeProgressRepository(played)).state.value

        // Apple (A) has only 2 answers; Y 0 %, X 20 %, Q 40 %, E 80 %.
        assertEquals(
            listOf(HardLetterUi('Y', 0), HardLetterUi('X', 20), HardLetterUi('Q', 40), HardLetterUi('E', 80)),
            state.hardLetters,
        )
    }

    @Test
    fun `at most four hardest letters are shown`() {
        val many = FakeProgressRepository(
            listOf("a1-egg", "a1-apple", "a1-xray", "a1-queen", "a2-yoga").map { progress(it, correct = 1, wrong = 5) },
        )

        assertEquals(4, viewModel(many).state.value.hardLetters.size)
    }

    @Test
    fun `no letter with enough answers leaves the hardest letters empty`() {
        val few = FakeProgressRepository(listOf(progress("a1-egg", correct = 2, wrong = 1)))

        val state = viewModel(few).state.value

        assertFalse(state.isEmpty)
        assertTrue(state.hardLetters.isEmpty())
    }

    @Test
    fun `unreadable content shows the error state`() {
        val broken = object : CardRepository {
            override suspend fun wheelPool(level: Level, category: CategoryId): List<Card> = error("broken")

            override suspend fun levelCards(): List<Card> = error("broken")

            override suspend fun cards(ids: Collection<CardId>): List<Card> = error("broken")

            override suspend fun contentIndex(): ContentIndex = error("broken")

            override suspend fun levelTotals(): Map<Level, Int> = error("broken")

            override suspend fun categoryOfCards(level: Level): Map<CardId, CategoryId> = error("broken")
        }

        val state = viewModel(cards = broken).state.value

        assertTrue(state.isLoaded)
        assertTrue(state.hasError)
    }

    @Test
    fun `new answers and sessions update the numbers`() = runTest {
        val store = FakeProgressRepository()
        val sessions = FakeSessionRepository()
        val viewModel = viewModel(store, sessions)
        assertTrue(viewModel.state.value.isEmpty)

        store.save(listOf(progress("a1-egg", correct = 1, wrong = 0)))
        sessions.record(session(daysAgo = 0, completed = true, correct = 1))

        val state = viewModel.state.value
        assertFalse(state.isEmpty)
        assertEquals(1, state.learnedWords)
        assertEquals(100, state.accuracyPercent)
        assertEquals(1, state.completedWheels)
    }

    @Test
    fun `back navigates back`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(StatsIntent.Back)

            assertEquals(StatsEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `retry after unreadable content reads it again`() {
        cards.failing = true
        val viewModel = viewModel(FakeProgressRepository(listOf(progress("a1-egg", correct = 1, wrong = 0))))
        assertTrue(viewModel.state.value.hasError)

        cards.failing = false
        viewModel.onIntent(StatsIntent.Retry)

        val state = viewModel.state.value
        assertTrue(state.isLoaded)
        assertFalse(state.hasError)
        assertEquals(1, state.learnedWords)
    }

    @Test
    fun `an unreadable progress shows the error and retry recovers`() {
        val store = FakeProgressRepository(listOf(progress("a1-egg", correct = 1, wrong = 0)))
        store.failing = true
        val viewModel = viewModel(store)
        assertTrue(viewModel.state.value.hasError)

        store.failing = false
        viewModel.onIntent(StatsIntent.Retry)

        assertFalse(viewModel.state.value.hasError)
        assertEquals(1, viewModel.state.value.learnedWords)
    }

    @Test
    fun `retry does nothing without an error`() {
        val viewModel = viewModel()
        val reads = cards.reads

        viewModel.onIntent(StatsIntent.Retry)

        assertEquals(reads, cards.reads)
    }

    @Test
    fun `practice and hardcore wheels count like classic ones`() = runTest {
        val sessions = FakeSessionRepository()
        sessions.record(
            session(daysAgo = 1, completed = true, correct = 3).copy(mode = GameMode.PRACTICE),
            session(daysAgo = 0, completed = true, correct = 2).copy(mode = GameMode.HARDCORE, score = 30),
        )

        val state = viewModel(sessions = sessions).state.value

        assertEquals(2, state.completedWheels)
        assertEquals(2, state.longestStreak)
        assertEquals(5, state.weekTotal)
    }
}
