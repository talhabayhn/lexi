package dev.talhabayhan.lexi.feature.exams

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.ExamPackId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeEntitlementRepository
import dev.talhabayhan.lexi.core.testing.FakeExamPackRepository
import dev.talhabayhan.lexi.core.testing.FakeProgressRepository
import dev.talhabayhan.lexi.core.testing.packCards
import dev.talhabayhan.lexi.core.testing.testCard
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExamPacksViewModelTest {
    private val lgs = ExamPackId("lgs")
    private val yds = ExamPackId("yds-yokdil")
    private val examPacks = FakeExamPackRepository(
        packs = listOf(
            testExamPack("lgs", titleTr = "LGS İngilizce", titleEn = "LGS English", descriptionTr = "8. sınıf"),
            testExamPack("yds-yokdil", isPro = true, sort = 1, freePreviewLetters = "ABCDE"),
        ),
        cards = mapOf(
            lgs to packCards(listOf(testCard("APPLE", level = Level.A2), testCard("BREAD"), testCard("CHAIR"))),
            yds to packCards(listOf(testCard("ABSTRACT", level = Level.C1))),
        ),
    )
    private val entitlements = FakeEntitlementRepository()
    private val progress = FakeProgressRepository()
    private val crashReporter = FakeCrashReporter()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = ExamPacksViewModel(examPacks, entitlements, progress, crashReporter)

    @Test
    fun `lists the packs with real card counts and learned share and design badges`() = runTest {
        progress.save(
            listOf(
                CardProgress(CardId("a2-apple"), seen = 1, correct = 1),
                CardProgress(CardId("a1-bread"), seen = 1, correct = 0),
            ),
        )

        val state = viewModel().state.value

        assertTrue(state.isLoaded)
        val (free, pro) = state.packs
        assertEquals(ExamPackUi(lgs, "LGS", 0, "LGS İngilizce", "LGS English", "8. sınıf", 3, 33, false, false), free)
        assertEquals("YDS", pro.badge)
        assertEquals(3, pro.badgeIndex)
        assertEquals(1, pro.cardCount)
    }

    @Test
    fun `without pro the pro pack is locked and a tap shows the soon toast instead of a game`() = runTest {
        val vm = viewModel()
        assertTrue(vm.state.value.packs.single { it.id == yds }.locked)
        assertFalse(vm.state.value.packs.single { it.id == lgs }.locked)

        vm.effects.test {
            vm.onIntent(ExamPacksIntent.OpenPack(yds))
            assertTrue(vm.state.value.lockedNotice)
            expectNoEvents()

            vm.onIntent(ExamPacksIntent.DismissNotice)
            assertFalse(vm.state.value.lockedNotice)
        }
    }

    @Test
    fun `a free pack starts its wheel`() = runTest {
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(ExamPacksIntent.OpenPack(lgs))
            assertEquals(ExamPacksEffect.StartPack(lgs), awaitItem())
        }
        assertFalse(vm.state.value.lockedNotice)
    }

    @Test
    fun `pro unlocks the pro pack also while the screen is open`() = runTest {
        val vm = viewModel()
        assertTrue(vm.state.value.packs.single { it.id == yds }.locked)

        entitlements.pro.value = true

        assertFalse(vm.state.value.packs.single { it.id == yds }.locked)
        vm.effects.test {
            vm.onIntent(ExamPacksIntent.OpenPack(yds))
            assertEquals(ExamPacksEffect.StartPack(yds), awaitItem())
        }
    }

    @Test
    fun `a failed read shows the error and retry reads again`() = runTest {
        examPacks.failing = true
        val vm = viewModel()
        assertTrue(vm.state.value.hasError)
        assertTrue(vm.state.value.packs.isEmpty())
        assertEquals(1, crashReporter.recorded.size)

        examPacks.failing = false
        vm.onIntent(ExamPacksIntent.Retry)

        assertFalse(vm.state.value.hasError)
        assertEquals(2, vm.state.value.packs.size)
    }

    @Test
    fun `no packs is an empty loaded list and back navigates back`() = runTest {
        examPacks.packs = emptyList()
        val vm = viewModel()
        assertTrue(vm.state.value.isLoaded)
        assertTrue(vm.state.value.packs.isEmpty())

        vm.effects.test {
            vm.onIntent(ExamPacksIntent.Back)
            assertEquals(ExamPacksEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `percent rounds half up and an empty pack is 0`() {
        assertEquals(0, ExamPacksViewModel.percent(0, 0))
        assertEquals(50, ExamPacksViewModel.percent(1, 2))
        assertEquals(67, ExamPacksViewModel.percent(2, 3))
        assertEquals(1, ExamPacksViewModel.percent(1, 200))
    }

    @Test
    fun `a failed progress read still loads the packs with 0 percent`() = runTest {
        progress.failing = true

        val state = viewModel().state.value

        assertTrue(state.isLoaded)
        assertEquals(listOf(0, 0), state.packs.map { it.learnedPercent })
        assertEquals(1, crashReporter.recorded.size)
    }
}
