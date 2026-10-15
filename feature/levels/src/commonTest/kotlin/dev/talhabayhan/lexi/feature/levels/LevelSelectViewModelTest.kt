package dev.talhabayhan.lexi.feature.levels

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeCardRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class LevelSelectViewModelTest {
    private val cards = FakeCardRepository(
        listOf(
            testCard("APPLE"),
            testCard("BREAD"),
            testCard("CHAIR"),
            testCard("DOCTOR", level = Level.A2),
            testCard("EAGLE", level = Level.A2),
        ),
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

    private fun viewModel() = LevelSelectViewModel(cards, progress, settings)

    private fun answered(id: String, correct: Int) = CardProgress(CardId(id), seen = 1, correct = correct)

    @Test
    fun `shows every level with its card total and learned words`() = runTest {
        progress.save(listOf(answered("a1-apple", 1), answered("a1-bread", 0), answered("a2-doctor", 3)))

        viewModel().state.test {
            val levels = expectMostRecentItem().levels
            assertEquals(Level.entries, levels.map { it.level })
            assertEquals(LevelProgressUi(Level.A1, learned = 1, total = 3), levels[0])
            assertEquals(LevelProgressUi(Level.A2, learned = 1, total = 2), levels[1])
            // Levels without content are empty, not missing.
            assertEquals(LevelProgressUi(Level.NAT, learned = 0, total = 0), levels[5])
            assertEquals(0f, levels[5].fraction)
        }
    }

    @Test
    fun `progress follows new answers`() = runTest {
        viewModel().state.test {
            assertEquals(0, expectMostRecentItem().levels[0].learned)

            progress.save(listOf(answered("a1-chair", 1)))

            assertEquals(1, awaitItem().levels[0].learned)
        }
    }

    @Test
    fun `classic time follows the setting`() = runTest {
        viewModel().state.test {
            assertEquals(180, expectMostRecentItem().classicTimeLimitSeconds)

            settings.update { it.copy(classicTimeLimitSeconds = 240) }

            assertEquals(240, awaitItem().classicTimeLimitSeconds)
        }
    }

    @Test
    fun `A1 and classic are preselected and the selection survives progress updates`() = runTest {
        val viewModel = viewModel()
        viewModel.state.test {
            val initial = expectMostRecentItem()
            assertEquals(Level.A1, initial.selectedLevel)
            assertEquals(GameMode.CLASSIC, initial.selectedMode)

            viewModel.onIntent(LevelSelectIntent.SelectLevel(Level.B2))
            viewModel.onIntent(LevelSelectIntent.SelectMode(GameMode.HARDCORE))
            progress.save(listOf(answered("a1-apple", 1)))

            val latest = expectMostRecentItem()
            assertEquals(Level.B2, latest.selectedLevel)
            assertEquals(GameMode.HARDCORE, latest.selectedMode)
            assertEquals(1, latest.levels[0].learned)
        }
    }

    @Test
    fun `start opens a game of the selection`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(LevelSelectIntent.Start)
            assertEquals(LevelSelectEffect.StartGame(Level.A1, GameMode.CLASSIC), awaitItem())

            viewModel.onIntent(LevelSelectIntent.SelectLevel(Level.C1))
            viewModel.onIntent(LevelSelectIntent.SelectMode(GameMode.PRACTICE))
            viewModel.onIntent(LevelSelectIntent.Start)
            assertEquals(LevelSelectEffect.StartGame(Level.C1, GameMode.PRACTICE), awaitItem())
        }
    }

    @Test
    fun `back navigates back`() = runTest {
        val viewModel = viewModel()
        viewModel.effects.test {
            viewModel.onIntent(LevelSelectIntent.Back)

            assertEquals(LevelSelectEffect.NavigateBack, awaitItem())
        }
    }
}
