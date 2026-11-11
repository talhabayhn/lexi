package dev.talhabayhan.lexi.feature.game.duel

import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeSpeaker
import dev.talhabayhan.lexi.core.testing.testCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DuelReviewViewModelTest {
    private val answers = listOf("APPLE", "BREAD", "CHAIR", "DOG", "EGG", "FISH")
    private val pool = answers.map { testCard(it) }
    private val ids = pool.map { it.id.value }
    private val speaker = FakeSpeaker()
    private val crashReporter = FakeCrashReporter()

    /** Both players saw the same six letters with different outcomes; the second one never reached F. */
    private val outcome = DuelOutcome(
        level = Level.A1,
        clockSeconds = 300,
        end = DuelEnd.TIME_UP,
        winner = 0,
        players = listOf(
            DuelPlayerOutcome("selinay", 3, 2, 1, 4_000L, DuelSlotOutcome.parse("cwcpwc"), ids),
            DuelPlayerOutcome("talha", 1, 1, 3, 0L, DuelSlotOutcome.parse("ppwcpo"), ids),
        ),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Opens [player]'s review the way the result screen does: through the result route and back. */
    private fun TestScope.review(player: Int, repository: FakeCardRepository = FakeCardRepository(pool)) =
        DuelReviewViewModel(
            route = DuelReviewRoute.of(DuelResultRoute.of(outcome).outcome, player),
            cards = repository,
            speaker = speaker,
            crashReporter = crashReporter,
        ).also { runCurrent() }

    private fun DuelReviewViewModel.wordsBy(outcome: ReviewOutcome): List<String> =
        state.value.groups.firstOrNull { it.outcome == outcome }?.rows?.map { it.word }.orEmpty()

    @Test
    fun `the first player's review lists their correct wrong and passed cards in wheel order`() = runTest {
        val vm = review(player = 0)

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals("selinay", state.name)
        assertEquals(0, state.player)
        assertEquals(DuelEnd.TIME_UP, state.end)
        assertEquals(
            listOf(ReviewOutcome.Correct, ReviewOutcome.Wrong, ReviewOutcome.Passed),
            state.groups.map { it.outcome },
        )
        assertEquals(listOf("apple", "chair", "fish"), vm.wordsBy(ReviewOutcome.Correct))
        assertEquals(listOf("bread", "egg"), vm.wordsBy(ReviewOutcome.Wrong))
        assertEquals(listOf("dog"), vm.wordsBy(ReviewOutcome.Passed))
    }

    @Test
    fun `the second player's review shows their own outcomes and leaves out the letter they never reached`() = runTest {
        val vm = review(player = 1)

        assertEquals("talha", vm.state.value.name)
        assertEquals(1, vm.state.value.player)
        assertEquals(listOf("dog"), vm.wordsBy(ReviewOutcome.Correct))
        assertEquals(listOf("chair"), vm.wordsBy(ReviewOutcome.Wrong))
        assertEquals(listOf("apple", "bread", "egg"), vm.wordsBy(ReviewOutcome.Passed))
        assertTrue(vm.state.value.groups.none { it.outcome == ReviewOutcome.TimeUp })
        assertTrue(vm.state.value.groups.flatMap { it.rows }.none { it.word == "fish" })
    }

    @Test
    fun `rows carry the clue and the example of the card`() = runTest {
        val vm = review(player = 0)

        val row = vm.state.value.groups.first().rows.first()
        assertEquals(pool[0].clue, row.clue)
        assertEquals(pool[0].example, row.example)
        assertEquals('A', row.letter)
    }

    @Test
    fun `a result route saved before 1_0_12 has no card ids and opens an empty review`() = runTest {
        val old = DuelResultRoute.of(outcome).copy(cards1 = emptyList(), cards2 = emptyList())

        val vm = DuelReviewViewModel(
            route = DuelReviewRoute.of(old.outcome, 0),
            cards = FakeCardRepository(pool),
            speaker = speaker,
            crashReporter = crashReporter,
        )
        runCurrent()

        assertFalse(vm.state.value.isLoading)
        assertFalse(vm.state.value.reviewError)
        assertTrue(vm.state.value.groups.isEmpty())
    }

    @Test
    fun `an unreadable content shows the error and Retry reads again`() = runTest {
        val repository = FakeCardRepository(pool).apply { failing = true }
        val vm = review(player = 0, repository = repository)
        assertTrue(vm.state.value.reviewError)

        repository.failing = false
        vm.onIntent(DuelReviewIntent.Retry)
        runCurrent()

        assertFalse(vm.state.value.reviewError)
        assertEquals(3, vm.state.value.groups.size)
    }

    @Test
    fun `toggling a group expands and collapses it and the speaker reads the word`() = runTest {
        val vm = review(player = 0)

        vm.onIntent(DuelReviewIntent.ToggleGroup(ReviewOutcome.Correct))
        assertEquals(setOf(ReviewOutcome.Correct), vm.state.value.expandedGroups)
        vm.onIntent(DuelReviewIntent.ToggleGroup(ReviewOutcome.Correct))
        assertTrue(vm.state.value.expandedGroups.isEmpty())

        vm.onIntent(DuelReviewIntent.Speak("apple"))
        assertEquals(listOf("apple"), speaker.spoken)
    }
}
