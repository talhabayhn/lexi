package dev.talhabayhan.lexi.feature.notebook

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeCategoryRepository
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSpeaker
import dev.talhabayhan.lexi.core.testing.testCard
import dev.talhabayhan.lexi.core.testing.testCategory
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NotebookViewModelTest {
    private val crashReporter = FakeCrashReporter()
    private val clock = FakeAppClock()
    private val today = clock.epochDay(clock.nowMs())
    private val cards = FakeCardRepository(
        listOf(
            testCard("EGG").copy(clue = "yumurta"),
            testCard("MIRROR").copy(clue = "ayna"),
            testCard("CHAIR").copy(clue = "sandalye"),
            testCard("YESTERDAY", level = Level.A2).copy(clue = "dün"),
            testCard("HORSE").copy(clue = "at"),
            testCard("APPLE").copy(clue = "elma"),
        ),
    )
    private val categories = FakeCategoryRepository(listOf(testCategory("food-drink", "Yiyecek", "Food")))
    private val progress = FakeProgressRepository(
        listOf(
            // Due: overdue, today.
            CardProgress(CardId("a1-egg"), box = 0, dueEpochDay = today - 2, seen = 3, correct = 1, wrong = 2),
            CardProgress(CardId("a1-mirror"), box = 1, dueEpochDay = today, seen = 1, wrong = 1),
            // Not due yet.
            CardProgress(CardId("a1-chair"), box = 2, dueEpochDay = today + 3, seen = 2, correct = 1, wrong = 1),
            CardProgress(CardId("a2-yesterday"), box = 1, dueEpochDay = today + 1, seen = 4, wrong = 3),
            // Mastered: in the notebook, not due.
            CardProgress(CardId("a1-horse"), box = 4, dueEpochDay = null, mastered = true, seen = 5, correct = 4),
            // Not in the notebook (answered right the first time).
            CardProgress(CardId("a1-apple"), box = null, seen = 1, correct = 1),
        ),
    )
    private val speaker = FakeSpeaker()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = NotebookViewModel(progress, cards, categories, clock, speaker, crashReporter)

    private val NotebookViewModel.ids: List<String> get() = state.value.words.map { it.id.value }

    @Test
    fun `the tabs count the due and the notebook and the hard words`() {
        val vm = viewModel()

        val state = vm.state.value
        assertTrue(state.isLoaded)
        assertEquals(2, state.dueCount)
        // Apple never entered the notebook.
        assertEquals(5, state.allCount)
        // Egg (2 wrong), yesterday (3 wrong); chair has one wrong only.
        assertEquals(2, state.hardCount)
    }

    @Test
    fun `the review tab lists due words most overdue first`() {
        assertEquals(listOf("a1-egg", "a1-mirror"), viewModel().ids)
    }

    @Test
    fun `the all tab lists the soonest first and mastered words last`() {
        val vm = viewModel()
        vm.onIntent(NotebookIntent.SelectTab(NotebookTab.All))

        assertEquals(listOf("a1-egg", "a1-mirror", "a2-yesterday", "a1-chair", "a1-horse"), vm.ids)
    }

    @Test
    fun `the hard tab lists the most often wrong word first`() {
        val vm = viewModel()
        vm.onIntent(NotebookIntent.SelectTab(NotebookTab.Hard))

        assertEquals(listOf("a2-yesterday", "a1-egg"), vm.ids)
    }

    @Test
    fun `rows show the lower case word and the clue and strength and the next review`() {
        val vm = viewModel()
        vm.onIntent(NotebookIntent.SelectTab(NotebookTab.All))

        val byId = vm.state.value.words.associateBy { it.id.value }
        assertEquals("egg", byId.getValue("a1-egg").word)
        assertEquals("yumurta", byId.getValue("a1-egg").clue)
        assertEquals(1, byId.getValue("a1-egg").strength)
        assertEquals(NextReview.Today, byId.getValue("a1-egg").next)
        assertEquals(NextReview.Today, byId.getValue("a1-mirror").next)
        assertEquals(2, byId.getValue("a1-mirror").strength)
        assertEquals(NextReview.Tomorrow, byId.getValue("a2-yesterday").next)
        assertEquals(Level.A2, byId.getValue("a2-yesterday").level)
        assertEquals(NextReview.InDays(3), byId.getValue("a1-chair").next)
        assertEquals(3, byId.getValue("a1-chair").strength)
        assertEquals(NextReview.Mastered, byId.getValue("a1-horse").next)
        assertEquals(4, byId.getValue("a1-horse").strength)
    }

    @Test
    fun `search matches the English word or the Turkish clue in the selected tab`() {
        val vm = viewModel()
        vm.onIntent(NotebookIntent.SelectTab(NotebookTab.All))

        vm.onIntent(NotebookIntent.QueryChanged("  MIR "))
        assertEquals(listOf("a1-mirror"), vm.ids)
        assertEquals("  MIR ", vm.state.value.query)

        vm.onIntent(NotebookIntent.QueryChanged("sandal"))
        assertEquals(listOf("a1-chair"), vm.ids)

        // Apple is not in the notebook, so it is never found; the counts ignore the search.
        vm.onIntent(NotebookIntent.QueryChanged("apple"))
        assertTrue(vm.state.value.words.isEmpty())
        assertEquals(5, vm.state.value.allCount)
    }

    @Test
    fun `search folds the Turkish dotted and dotless i`() {
        cards.cards = cards.cards.map { if (it.answer == "EGG") it.copy(clue = "İğne ılık") else it }
        val vm = viewModel()
        vm.onIntent(NotebookIntent.SelectTab(NotebookTab.All))

        vm.onIntent(NotebookIntent.QueryChanged("iğne"))
        assertEquals(listOf("a1-egg"), vm.ids)
        vm.onIntent(NotebookIntent.QueryChanged("ILIK"))
        assertEquals(listOf("a1-egg"), vm.ids)
    }

    @Test
    fun `a card that left the content is hidden`() {
        cards.cards = cards.cards.filter { it.answer != "EGG" }

        assertEquals(listOf("a1-mirror"), viewModel().ids)
    }

    @Test
    fun `the review wheel starts only while something is due`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(NotebookIntent.StartReview)
            assertEquals(NotebookEffect.StartReview, awaitItem())
        }

        val none = NotebookViewModel(FakeProgressRepository(), cards, categories, clock, speaker, crashReporter)
        none.effects.test {
            none.onIntent(NotebookIntent.StartReview)
            expectNoEvents()
        }
    }

    @Test
    fun `a word opens its detail with the category and example and counters`() {
        val vm = viewModel()
        vm.onIntent(NotebookIntent.OpenWord(CardId("a1-egg")))

        val detail = assertNotNull(vm.state.value.detail)
        assertEquals("egg", detail.word.word)
        assertEquals("Yiyecek", detail.categoryNameTr)
        assertEquals("Food", detail.categoryNameEn)
        assertEquals("An example with EGG.", detail.example)
        assertEquals(0, detail.box)
        assertEquals(3, detail.seen)
        assertEquals(1, detail.correct)
        assertEquals(2, detail.wrong)

        vm.onIntent(NotebookIntent.CloseWord)
        assertNull(vm.state.value.detail)
    }

    @Test
    fun `speaking passes the word to the speaker and closing the sheet silences it`() {
        val vm = viewModel()
        vm.onIntent(NotebookIntent.Speak("egg"))
        assertEquals(listOf("egg"), speaker.spoken)

        vm.onIntent(NotebookIntent.OpenWord(CardId("a1-egg")))
        vm.onIntent(NotebookIntent.CloseWord)
        assertTrue(speaker.stopCount > 0)
    }

    @Test
    fun `removing a word takes it out of every tab and keeps its counters`() = runTest {
        val vm = viewModel()
        vm.onIntent(NotebookIntent.OpenWord(CardId("a1-egg")))
        vm.onIntent(NotebookIntent.RemoveWord(CardId("a1-egg")))

        assertNull(vm.state.value.detail)
        assertEquals(listOf("a1-mirror"), vm.ids)
        assertEquals(1, vm.state.value.dueCount)
        assertEquals(4, vm.state.value.allCount)
        assertEquals(1, vm.state.value.hardCount)
        val stored = progress.progress.getValue(CardId("a1-egg"))
        assertFalse(stored.inNotebook)
        assertEquals(2, stored.wrong)
    }

    @Test
    fun `an empty notebook is loaded and empty`() {
        val vm = NotebookViewModel(FakeProgressRepository(), cards, categories, clock, speaker, crashReporter)

        assertTrue(vm.state.value.isLoaded)
        assertTrue(vm.state.value.words.isEmpty())
        assertEquals(0, vm.state.value.dueCount)
    }

    @Test
    fun `back is an effect`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(NotebookIntent.Back)
            assertEquals(NotebookEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `an unreadable notebook shows the error and retry reads it again`() {
        progress.failing = true
        val viewModel = viewModel()

        assertTrue(viewModel.state.value.isLoaded)
        assertTrue(viewModel.state.value.hasError)
        assertTrue(viewModel.state.value.words.isEmpty())
        assertEquals(listOf("notebook not loaded"), crashReporter.recorded.map { it.second })

        progress.failing = false
        viewModel.onIntent(NotebookIntent.Retry)

        assertFalse(viewModel.state.value.hasError)
        assertEquals(listOf("a1-egg", "a1-mirror"), viewModel.ids)
    }

    @Test
    fun `unreadable words are an error and not an empty notebook`() {
        cards.failing = true
        val viewModel = viewModel()

        assertTrue(viewModel.state.value.hasError)
        assertTrue(viewModel.state.value.words.isEmpty())

        cards.failing = false
        viewModel.onIntent(NotebookIntent.Retry)

        assertFalse(viewModel.state.value.hasError)
        assertEquals(2, viewModel.state.value.dueCount)
    }

    @Test
    fun `retry does nothing without an error`() {
        val viewModel = viewModel()
        val reads = cards.reads

        viewModel.onIntent(NotebookIntent.Retry)

        assertEquals(reads, cards.reads)
        assertFalse(viewModel.state.value.hasError)
    }
}
