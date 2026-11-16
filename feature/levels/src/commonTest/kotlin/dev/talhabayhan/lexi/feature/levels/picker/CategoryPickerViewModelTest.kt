package dev.talhabayhan.lexi.feature.levels.picker

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.ExamPackCard
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeCategoryRepository
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeExamPackRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
import dev.talhabayhan.lexi.core.testing.testCard
import dev.talhabayhan.lexi.core.testing.testCategory
import dev.talhabayhan.lexi.core.testing.testExamPack
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryPickerViewModelTest {
    private val crashReporter = FakeCrashReporter()
    private val apple = testCard("APPLE", category = "food-drink")
    private val cards = FakeCardRepository(
        listOf(
            apple,
            testCard("BREAD", category = "food-drink"),
            testCard("CHAIR", category = "home"),
            testCard("DOCTOR", level = Level.A2, category = "food-drink"),
            testCard("IDIOM", level = Level.NAT, category = "general"),
        ),
    )
    private val categories = FakeCategoryRepository(
        listOf(
            testCategory("mixed", "Karışık", "Mixed", sort = 0),
            testCategory("food-drink", "Yiyecek ve içecek", "Food and drink", setOf(Level.A1, Level.A2), sort = 2),
            testCategory("daily-life", "Günlük hayat", "Daily life", setOf(Level.A1, Level.A2), sort = 1),
            testCategory("home", "Ev ve eşyalar", "Home", setOf(Level.A1), sort = 3),
            // Listed for A1 but without a card there: still shown (0 / 0), never hidden.
            testCategory("travel", "Seyahat", "Travel", setOf(Level.A1), sort = 4),
            testCategory("general", "Genel", "General", setOf(Level.NAT), sort = 99),
        ),
    )
    private val examPacks = FakeExamPackRepository(
        packs = listOf(testExamPack("lgs", sort = 0, titleTr = "LGS İngilizce"), testExamPack("yds", isPro = true)),
        cards = mapOf(ExamPackId("lgs") to listOf(ExamPackCard(apple, unit = null))),
    )
    private val progress = FakeProgressRepository()
    private val settings = FakeSettingsRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(level: Level = Level.A1) = CategoryPickerViewModel(
        route = CategoryPickerRoute(level),
        cards = cards,
        categories = categories,
        examPacks = examPacks,
        progress = progress,
        settings = settings,
        config = GameConfig(),
        crashReporter = crashReporter,
    )

    private fun learned(id: String) = CardProgress(CardId(id), seen = 1, correct = 1)

    @Test
    fun `mixed comes first then every category of the level in sort order, empty ones included`() = runTest {
        progress.save(listOf(learned("a1-apple"), learned("a1-chair")))

        val state = viewModel().state.value

        assertEquals(
            listOf("mixed", "daily-life", "food-drink", "home", "travel"),
            state.categories.map {
                it.id.value
            },
        )
        assertEquals(
            PickerCategoryUi(CategoryId.MIXED, "Karışık", "Mixed", learned = 2, total = 3),
            state.categories[0],
        )
        assertEquals(0 to 0, state.categories[1].let { it.learned to it.total })
        assertEquals(1 to 2, state.categories[2].let { it.learned to it.total })
        assertEquals(0 to 0, state.categories[4].let { it.learned to it.total })
    }

    @Test
    fun `NAT shows only the mixed card`() = runTest {
        val state = viewModel(Level.NAT).state.value

        assertEquals(listOf(CategoryId.MIXED), state.categories.map { it.id })
        assertEquals(1, state.categories[0].total)
    }

    @Test
    fun `the stored mode, time and category are preselected`() = runTest {
        settings.update {
            it.copy(
                selectedLevel = Level.A1,
                playMode = GameMode.HARDCORE,
                classicTimeLimitSeconds = 420,
                playCategory = CategoryId("home"),
            )
        }

        val state = viewModel().state.value

        assertEquals(GameMode.HARDCORE, state.mode)
        assertEquals(420, state.timeSeconds)
        assertEquals(CategoryId("home"), state.selectedCategory)
    }

    @Test
    fun `a stored category of another level or a removed time option falls back to the defaults`() = runTest {
        settings.update {
            it.copy(selectedLevel = Level.A2, playCategory = CategoryId("home"), classicTimeLimitSeconds = 240)
        }

        val state = viewModel(Level.A1).state.value

        assertEquals(CategoryId.MIXED, state.selectedCategory)
        assertEquals(180, state.timeSeconds)
        assertEquals(Level.A1, settings.settings.value.selectedLevel)
    }

    @Test
    fun `changing the level resets the category to mixed and is remembered`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(CategoryPickerIntent.OpenCategory(CategoryId("home")))
        viewModel.onIntent(CategoryPickerIntent.CloseDialog)
        assertEquals(CategoryId("home"), settings.settings.value.playCategory)

        viewModel.onIntent(CategoryPickerIntent.SelectLevel(Level.A2))

        val state = viewModel.state.value
        assertEquals(Level.A2, state.level)
        assertEquals(CategoryId.MIXED, state.selectedCategory)
        assertEquals(listOf("mixed", "daily-life", "food-drink"), state.categories.map { it.id.value })
        assertEquals(Level.A2, settings.settings.value.selectedLevel)
        assertNull(settings.settings.value.playCategory)
    }

    @Test
    fun `opening a card remembers it and opens the dialog, mode and time are remembered`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(CategoryPickerIntent.OpenCategory(CategoryId("food-drink")))
        viewModel.onIntent(CategoryPickerIntent.SelectMode(GameMode.HARDCORE))
        viewModel.onIntent(CategoryPickerIntent.SelectTime(300))

        val state = viewModel.state.value
        assertEquals(CategoryId("food-drink"), state.dialog?.id)
        assertEquals(
            Settings().copy(
                selectedLevel = Level.A1,
                playCategory = CategoryId("food-drink"),
                playMode = GameMode.HARDCORE,
                classicTimeLimitSeconds = 300,
            ),
            settings.settings.value,
        )
    }

    @Test
    fun `practice disables the time row and plays untimed`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(CategoryPickerIntent.OpenCategory(CategoryId.MIXED))
        viewModel.onIntent(CategoryPickerIntent.SelectMode(GameMode.PRACTICE))

        viewModel.onIntent(CategoryPickerIntent.SelectTime(420))

        assertEquals(false, viewModel.state.value.timeEnabled)
        assertEquals(180, viewModel.state.value.timeSeconds)
        assertEquals(180, settings.settings.value.classicTimeLimitSeconds)
        viewModel.effects.test {
            viewModel.onIntent(CategoryPickerIntent.Play)
            assertEquals(CategoryPickerEffect.StartGame(Level.A1, GameMode.PRACTICE, null, null), awaitItem())
        }
    }

    @Test
    fun `play starts the opened category with the chosen mode and time and closes the dialog`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(CategoryPickerIntent.OpenCategory(CategoryId("home")))
        viewModel.onIntent(CategoryPickerIntent.SelectTime(420))

        viewModel.effects.test {
            viewModel.onIntent(CategoryPickerIntent.Play)

            assertEquals(
                CategoryPickerEffect.StartGame(Level.A1, GameMode.CLASSIC, CategoryId("home"), 420),
                awaitItem(),
            )
        }
        assertNull(viewModel.state.value.dialog)
    }

    @Test
    fun `an unknown time is ignored and play without an open dialog does nothing`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(CategoryPickerIntent.OpenCategory(CategoryId.MIXED))
        viewModel.onIntent(CategoryPickerIntent.SelectTime(240))
        assertEquals(180, viewModel.state.value.timeSeconds)
        viewModel.onIntent(CategoryPickerIntent.CloseDialog)

        viewModel.effects.test {
            viewModel.onIntent(CategoryPickerIntent.Play)
            expectNoEvents()
        }
    }

    @Test
    fun `the open pack starts its wheel and the locked exams are not tappable`() = runTest {
        val viewModel = viewModel()
        val state = viewModel.state.value
        assertEquals(listOf("lgs"), state.packs.map { it.id.value })
        assertEquals(listOf("yds-yokdil", "yks-dil", "toefl", "ielts"), state.lockedExams)

        viewModel.effects.test {
            viewModel.onIntent(CategoryPickerIntent.OpenPack(ExamPackId("toefl")))
            viewModel.onIntent(CategoryPickerIntent.OpenPack(ExamPackId("yds")))
            expectNoEvents()

            viewModel.onIntent(CategoryPickerIntent.OpenPack(ExamPackId("lgs")))
            assertEquals(CategoryPickerEffect.StartPack(ExamPackId("lgs")), awaitItem())
        }
    }

    @Test
    fun `back closes the dialog first, then leaves`() = runTest {
        val viewModel = viewModel()
        viewModel.onIntent(CategoryPickerIntent.OpenCategory(CategoryId.MIXED))

        viewModel.effects.test {
            viewModel.onIntent(CategoryPickerIntent.Back)
            expectNoEvents()
            assertNull(viewModel.state.value.dialog)

            viewModel.onIntent(CategoryPickerIntent.Back)
            assertEquals(CategoryPickerEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `an unreadable content shows the error and retry reads it again`() = runTest {
        cards.failing = true
        val viewModel = viewModel()
        assertTrue(viewModel.state.value.hasError)
        assertEquals(listOf("picker content not read"), crashReporter.recorded.map { it.second })

        viewModel.onIntent(CategoryPickerIntent.OpenCategory(CategoryId.MIXED))
        assertNull(viewModel.state.value.dialog)

        cards.failing = false
        viewModel.onIntent(CategoryPickerIntent.Retry)

        assertEquals(false, viewModel.state.value.hasError)
        assertEquals(5, viewModel.state.value.categories.size)
    }
}
