package dev.talhabayhan.lexi.feature.game.result

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.share.ShareCaption
import dev.talhabayhan.lexi.core.domain.share.ShareCell
import dev.talhabayhan.lexi.core.domain.share.SlotOutcome
import dev.talhabayhan.lexi.core.domain.time.civilDateOfEpochDay
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeShareService
import dev.talhabayhan.lexi.core.testing.FakeSpeaker
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import dev.talhabayhan.lexi.core.testing.testCard
import dev.talhabayhan.lexi.feature.game.GameRoute
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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ResultViewModelTest {
    private val answers = listOf("APPLE", "BREAD", "CHAIR", "DOG", "EGG", "FISH", "GOAT")
    private val cards = FakeCardRepository(answers.map { testCard(it) })
    private val speaker = FakeSpeaker()
    private val config = GameConfig()
    private val progress = FakeProgressRepository()
    private val clock = FakeAppClock()
    private val today = clock.epochDay(clock.nowMs())
    private val streaks = FakeStreakRepository(listOf(today - 2, today - 1, today))
    private var shareService = FakeShareService()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun route(
        missed: List<String> = answers.map { "a1-${it.lowercase()}" },
        passed: List<String> = listOf("a1-apple", "a1-chair"),
        mode: GameMode = GameMode.CLASSIC,
        remainingMs: Long? = 11_200L,
        recorded: Boolean = true,
        reason: GameOverReason = GameOverReason.TIME_UP,
    ) = ResultRoute(
        level = "A1",
        mode = mode.name,
        category = "food-drink",
        reason = reason.name,
        score = 340,
        correct = 19,
        wrong = 3,
        passed = 4,
        longestStreak = 6,
        remainingMs = remainingMs,
        missedCards = missed,
        passedCards = passed,
        recorded = recorded,
        wheelLetters = "ABCD",
        wheelOutcomes = listOf("CORRECT", "WRONG", "PASSED", "TIME_UP"),
    )

    private fun TestScope.viewModel(route: ResultRoute = route()) =
        ResultViewModel(route, cards, speaker, config, progress, clock, streaks, shareService).also { runCurrent() }

    @Test
    fun `the header and stats come from the route`() = runTest {
        val state = viewModel().state.value

        assertEquals(Level.A1, state.level)
        assertEquals(GameMode.CLASSIC, state.mode)
        assertEquals(GameOverReason.TIME_UP, state.reason)
        assertEquals(340, state.score)
        assertEquals(6, state.longestStreak)
        assertEquals(12, state.remainingSeconds, "rounded up like the game clock")
        assertEquals(listOf(19, 3, 4), listOf(state.correct, state.wrong, state.passed))
        assertEquals(7, state.addedToNotebook)
    }

    @Test
    fun `review rows keep the wheel order with word and clue and example and outcome`() = runTest {
        val groups = viewModel().state.value.groups

        assertEquals(2, groups.size)
        assertEquals(ReviewOutcome.Wrong, groups[0].outcome)
        assertEquals(listOf("bread", "dog", "egg", "fish", "goat"), groups[0].rows.map { it.word })
        assertEquals(ReviewOutcome.Passed, groups[1].outcome)
        assertEquals(listOf("apple", "chair"), groups[1].rows.map { it.word })
        assertEquals("ipucu", groups[1].rows.first().clue)
        assertEquals("An example with APPLE.", groups[1].rows.first().example)
    }

    @Test
    fun `words are listed in all four groups in order Dogru Yanlis Pas Sure bitti`() = runTest {
        val fourGroupsRoute = ResultRoute(
            level = "A1",
            mode = GameMode.CLASSIC.name,
            reason = GameOverReason.TIME_UP.name,
            score = 280,
            correct = 2,
            wrong = 2,
            passed = 2,
            longestStreak = 2,
            correctCards = listOf("a1-apple", "a1-bread"),
            wrongCards = listOf("a1-egg", "a1-fish"),
            passedCards = listOf("a1-chair", "a1-dog"),
            timeUpCards = listOf("a1-goat"),
        )
        val state = viewModel(fourGroupsRoute).state.value
        val groups = state.groups
        assertEquals(4, groups.size)
        assertEquals(ReviewOutcome.Correct, groups[0].outcome)
        assertEquals(listOf("apple", "bread"), groups[0].rows.map { it.word })
        assertEquals(ReviewOutcome.Wrong, groups[1].outcome)
        assertEquals(listOf("egg", "fish"), groups[1].rows.map { it.word })
        assertEquals(ReviewOutcome.Passed, groups[2].outcome)
        assertEquals(listOf("chair", "dog"), groups[2].rows.map { it.word })
        assertEquals(ReviewOutcome.TimeUp, groups[3].outcome)
        assertEquals(listOf("goat"), groups[3].rows.map { it.word })
        assertFalse(groups[3].rows.first().isAddedToNotebook)
    }

    @Test
    fun `time-up cards get added to notebook via AddToNotebook intent`() = runTest {
        val vm = viewModel(
            ResultRoute(
                level = "A1",
                mode = GameMode.CLASSIC.name,
                reason = GameOverReason.TIME_UP.name,
                score = 280,
                correct = 0,
                wrong = 0,
                passed = 0,
                longestStreak = 0,
                timeUpCards = listOf("a1-goat"),
            ),
        )
        var state = vm.state.value
        assertFalse(state.groups.first().rows.first().isAddedToNotebook)
        assertEquals(0, state.addedToNotebook)

        vm.onIntent(ResultIntent.AddToNotebook("a1-goat"))
        runCurrent()

        state = vm.state.value
        assertTrue(state.groups.first().rows.first().isAddedToNotebook)
        assertEquals(1, state.addedToNotebook)
        val stored = progress.get(listOf(CardId("a1-goat")))[CardId("a1-goat")]
        assertEquals(0, stored?.box)
    }

    @Test
    fun `every review row stays in its group and every group starts collapsed`() = runTest {
        val vm = viewModel()
        assertEquals(7, vm.state.value.rows.size)
        assertEquals(7, vm.state.value.groups.sumOf { it.rows.size })
        assertTrue(vm.state.value.expandedGroups.isEmpty())
    }

    @Test
    fun `show more opens one group and show less closes it again`() = runTest {
        val vm = viewModel()

        vm.onIntent(ResultIntent.ToggleGroup(ReviewOutcome.Wrong))
        assertEquals(setOf(ReviewOutcome.Wrong), vm.state.value.expandedGroups)
        vm.onIntent(ResultIntent.ToggleGroup(ReviewOutcome.Passed))
        assertEquals(setOf(ReviewOutcome.Wrong, ReviewOutcome.Passed), vm.state.value.expandedGroups)

        vm.onIntent(ResultIntent.ToggleGroup(ReviewOutcome.Wrong))

        assertEquals(setOf(ReviewOutcome.Passed), vm.state.value.expandedGroups)
        assertEquals(7, vm.state.value.rows.size, "toggling never changes the rows or counts")
    }

    @Test
    fun `a game without misses has no rows and adds nothing`() = runTest {
        val state = viewModel(route(missed = emptyList(), passed = emptyList())).state.value

        assertFalse(state.isLoading)
        assertTrue(state.rows.isEmpty())
        assertEquals(0, state.addedToNotebook)
    }

    @Test
    fun `nothing is reported as added when the game could not be stored`() = runTest {
        assertEquals(0, viewModel(route(recorded = false)).state.value.addedToNotebook)
    }

    @Test
    fun `untimed modes show no time left`() = runTest {
        assertNull(viewModel(route(mode = GameMode.PRACTICE, remainingMs = null)).state.value.remainingSeconds)
    }

    @Test
    fun `unscored modes show the correct answers instead of the score`() = runTest {
        assertFalse(viewModel(route(mode = GameMode.PRACTICE, remainingMs = null)).state.value.isScored)
        assertFalse(viewModel(route(mode = GameMode.REVIEW, remainingMs = null)).state.value.isScored)
        assertTrue(viewModel(route(mode = GameMode.HARDCORE)).state.value.isScored)
        assertTrue(viewModel(route(mode = GameMode.CLASSIC)).state.value.isScored)
    }

    @Test
    fun `practice results still report the cards added to the notebook`() = runTest {
        val state = viewModel(route(mode = GameMode.PRACTICE, remainingMs = null)).state.value

        assertEquals(answers.size, state.addedToNotebook)
        assertEquals(7, state.rows.size)
    }

    @Test
    fun `a failed read of the review cards shows the error and retry loads them`() = runTest {
        cards.failing = true
        val vm = viewModel()

        assertTrue(vm.state.value.reviewError)
        assertFalse(vm.state.value.isLoading)
        assertTrue(vm.state.value.rows.isEmpty())
        // The counts are still shown.
        assertEquals(19, vm.state.value.correct)

        cards.failing = false
        vm.onIntent(ResultIntent.Retry)
        runCurrent()

        assertFalse(vm.state.value.reviewError)
        assertEquals(7, vm.state.value.rows.size)
    }

    @Test
    fun `retry does nothing when the review cards were read`() = runTest {
        val vm = viewModel()
        val reads = cards.reads

        vm.onIntent(ResultIntent.Retry)
        runCurrent()

        assertEquals(reads, cards.reads)
    }

    @Test
    fun `cards that left the content are skipped`() = runTest {
        val gameRoute = route(
            missed = listOf("a1-apple", "a1-gone", "a1-bread"),
            passed = listOf("a1-apple"),
        )
        val state = viewModel(gameRoute).state.value

        assertEquals(listOf("bread", "apple"), state.rows.map { it.word })
    }

    @Test
    fun `the speaker reads a row and stops when the screen closes`() = runTest {
        val vm = viewModel()

        vm.onIntent(ResultIntent.Speak("chair"))
        ViewModelStore().apply { put("result", vm) }.clear()

        assertEquals(listOf("chair"), speaker.spoken)
        assertEquals(1, speaker.stopCount)
    }

    @Test
    fun `play again starts the same level and mode and category`() = runTest {
        assertEquals(
            GameRoute(level = "A1", mode = "CLASSIC", category = "food-drink"),
            viewModel().playAgain,
        )
    }

    @Test
    fun `a finished daily cannot be played again`() = runTest {
        val vm = viewModel(route(mode = GameMode.DAILY))

        assertFalse(vm.state.value.canPlayAgain)
        assertNull(vm.playAgain)
    }

    @Test
    fun `other modes can be played again`() = runTest {
        GameMode.entries.filter { it != GameMode.DAILY }.forEach { mode ->
            assertTrue(viewModel(route(mode = mode)).state.value.canPlayAgain, mode.name)
        }
    }

    @Test
    fun `the share card comes from the wheel of the route and the current streak`() = runTest {
        val card = assertNotNull(viewModel().state.value.shareCard)

        assertEquals(
            listOf(
                ShareCell('A', SlotOutcome.CORRECT),
                ShareCell('B', SlotOutcome.WRONG),
                ShareCell('C', SlotOutcome.PASSED),
                ShareCell('D', SlotOutcome.TIME_UP),
            ),
            card.cells,
        )
        assertEquals(1 to 4, card.correct to card.total)
        assertEquals(Level.A1, card.level)
        assertEquals(civilDateOfEpochDay(today), card.date)
        assertEquals(12, card.remainingSeconds)
        assertTrue(card.timeUp)
        assertEquals(3, card.streakDays)
        assertEquals(ShareCaption.Streak(3), card.caption)
    }

    @Test
    fun `a daily share card has no level and the daily caption`() = runTest {
        val card = assertNotNull(viewModel(route(mode = GameMode.DAILY)).state.value.shareCard)

        assertNull(card.level)
        assertEquals(ShareCaption.Daily(1), card.caption)
    }

    @Test
    fun `share asks for the capture and hands the png and text to the share sheet`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(ResultIntent.Share)
            assertTrue(vm.state.value.isSharing)

            val png = byteArrayOf(1, 2, 3)
            vm.onIntent(ResultIntent.SharePng(png, "caption and message"))
            runCurrent()

            val (sharedPng, sharedText) = shareService.shared.single()
            assertContentEquals(png, sharedPng)
            assertEquals("caption and message", sharedText)
            assertFalse(vm.state.value.isSharing)
            expectNoEvents()
        }
    }

    @Test
    fun `a double tap on share shares once`() = runTest {
        shareService = FakeShareService()
        val vm = viewModel()

        vm.onIntent(ResultIntent.Share)
        vm.onIntent(ResultIntent.Share)
        vm.onIntent(ResultIntent.SharePng(byteArrayOf(1), "text"))
        vm.onIntent(ResultIntent.SharePng(byteArrayOf(1), "text"))
        runCurrent()

        assertEquals(1, shareService.shared.size)
        assertFalse(vm.state.value.isSharing)
    }

    @Test
    fun `an image without a running share is ignored`() = runTest {
        val vm = viewModel()

        vm.onIntent(ResultIntent.SharePng(byteArrayOf(1), "text"))
        runCurrent()

        assertTrue(shareService.shared.isEmpty())
    }

    @Test
    fun `a share sheet that could not open reports the failure`() = runTest {
        shareService = FakeShareService(result = false)
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(ResultIntent.Share)
            vm.onIntent(ResultIntent.SharePng(byteArrayOf(1), "text"))
            runCurrent()

            assertEquals(ResultEffect.ShareFailed, awaitItem())
            assertFalse(vm.state.value.isSharing)
        }
    }

    @Test
    fun `a card that could not be captured reports the failure and allows a new try`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(ResultIntent.Share)
            vm.onIntent(ResultIntent.ShareCaptureFailed)

            assertEquals(ResultEffect.ShareFailed, awaitItem())
            assertFalse(vm.state.value.isSharing)
            vm.onIntent(ResultIntent.Share)
            assertTrue(vm.state.value.isSharing)
        }
    }

    @Test
    fun `a share tapped before the card is built waits for the card instead of failing`() = runTest {
        val vm = ResultViewModel(route(), cards, speaker, config, progress, clock, streaks, shareService)
        vm.effects.test {
            vm.onIntent(ResultIntent.Share)

            assertTrue(vm.state.value.isSharing)
            assertNull(vm.state.value.shareCard)
            runCurrent()
            assertNotNull(vm.state.value.shareCard)
            assertTrue(vm.state.value.isSharing)
            expectNoEvents()
        }
    }
}
