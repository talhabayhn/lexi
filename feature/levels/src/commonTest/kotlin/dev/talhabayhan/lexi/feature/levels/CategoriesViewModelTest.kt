package dev.talhabayhan.lexi.feature.levels

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeCategoryRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {
    private val cards = FakeCardRepository(
        listOf(
            testCard("APPLE", category = "food-drink"),
            testCard("BREAD", category = "food-drink"),
            testCard("CHAIR", category = "home"),
            testCard("DOCTOR", level = Level.A2, category = "food-drink"),
            testCard("EAGLE", level = Level.A2, category = "animals"),
        ),
    )
    private val categories = FakeCategoryRepository(
        listOf(
            testCategory("mixed", "Karışık", "Mixed", sort = 0),
            testCategory("food-drink", "Yiyecek ve içecek", "Food and drink", sort = 1),
            testCategory("home", "Ev ve eşyalar", "Home", setOf(Level.A1, Level.A2), sort = 2),
            // Listed for A1 but without a card there: left out.
            testCategory("travel", "Seyahat", "Travel", setOf(Level.A1, Level.A2), sort = 3),
            testCategory("animals", "Hayvanlar", "Animals", setOf(Level.A2), sort = 4),
        ),
    )
    private val progress = FakeProgressRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(level: Level = Level.A1, mode: GameMode = GameMode.CLASSIC) =
        CategoriesViewModel(CategoriesRoute(level, mode), cards, categories, progress)

    private fun answered(id: String, correct: Int) = CardProgress(CardId(id), seen = 1, correct = correct)

    @Test
    fun `lists the level's categories in order with total and learned words`() = runTest {
        progress.save(listOf(answered("a1-apple", 1), answered("a1-bread", 0), answered("a2-doctor", 2)))

        viewModel().state.test {
            val state = expectMostRecentItem()
            assertTrue(state.isLoaded)
            assertEquals(Level.A1, state.level)
            assertEquals(3, state.levelTotal)
            assertEquals(listOf("food-drink", "home"), state.categories.map { it.id.value })
            val food = state.categories[0]
            assertEquals(CategoryUi(CategoryId("food-drink"), "Yiyecek ve içecek", "Food and drink", 1, 2), food)
            assertEquals(0.5f, food.fraction)
            assertFalse(food.isComplete)
            assertEquals(0, state.categories[1].learned)
        }
    }

    @Test
    fun `a category with every word learned is complete`() = runTest {
        progress.save(listOf(answered("a1-chair", 3)))

        viewModel().state.test {
            val home = expectMostRecentItem().categories.single { it.id == CategoryId("home") }
            assertTrue(home.isComplete)
            assertEquals(1f, home.fraction)
        }
    }

    @Test
    fun `progress follows new answers`() = runTest {
        viewModel().state.test {
            assertEquals(0, expectMostRecentItem().categories[0].learned)

            progress.save(listOf(answered("a1-apple", 1)))

            assertEquals(1, awaitItem().categories[0].learned)
        }
    }

    @Test
    fun `the level tabs switch the categories and drop the old ones at once`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            expectMostRecentItem()

            viewModel.onIntent(CategoriesIntent.SelectLevel(Level.A2))

            val state = expectMostRecentItem()
            assertEquals(Level.A2, state.level)
            assertEquals(2, state.levelTotal)
            assertEquals(listOf("food-drink", "animals"), state.categories.map { it.id.value })
        }
    }

    @Test
    fun `a level without content shows no categories after loading`() = runTest {
        viewModel(level = Level.NAT).state.test {
            val state = expectMostRecentItem()
            assertTrue(state.isLoaded)
            assertEquals(0, state.levelTotal)
            assertTrue(state.categories.isEmpty())
        }
    }

    @Test
    fun `a category starts a game of its level and category in the mode of the level select`() = runTest {
        val viewModel = viewModel(mode = GameMode.HARDCORE)
        viewModel.effects.test {
            viewModel.onIntent(CategoriesIntent.OpenCategory(CategoryId("home")))
            assertEquals(
                CategoriesEffect.StartGame(Level.A1, GameMode.HARDCORE, CategoryId("home")),
                awaitItem(),
            )

            viewModel.onIntent(CategoriesIntent.SelectLevel(Level.A2))
            viewModel.onIntent(CategoriesIntent.OpenCategory(CategoryId("animals")))
            assertEquals(
                CategoriesEffect.StartGame(Level.A2, GameMode.HARDCORE, CategoryId("animals")),
                awaitItem(),
            )
        }
    }

    @Test
    fun `the mixed wheel starts without a category`() = runTest {
        val viewModel = viewModel(level = Level.A2)
        viewModel.effects.test {
            viewModel.onIntent(CategoriesIntent.PlayMixed)

            assertEquals(CategoriesEffect.StartGame(Level.A2, GameMode.CLASSIC, null), awaitItem())
        }
    }

    @Test
    fun `back navigates back`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(CategoriesIntent.Back)

            assertEquals(CategoriesEffect.NavigateBack, awaitItem())
        }
    }
}
