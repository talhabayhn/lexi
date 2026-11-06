package dev.talhabayhan.lexi.feature.game.duel

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.testing.FakeCrashReporter
import dev.talhabayhan.lexi.core.testing.FakeSettingsRepository
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DuelSetupViewModelTest {
    private val config = GameConfig()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(settings: FakeSettingsRepository = FakeSettingsRepository()) =
        DuelSetupViewModel(settings, config, FakeCrashReporter()).also { runCurrent() }

    @Test
    fun `a first duel starts with default names A1 and 2_30 per player`() = runTest {
        val state = viewModel().state.value

        assertEquals(true, state.isLoaded)
        assertEquals(listOf("", ""), state.names)
        assertEquals(Level.A1, state.level)
        assertEquals(150, state.clockSeconds)
        assertEquals(listOf(90, 150, 240), state.clockOptionsSeconds)
    }

    @Test
    fun `the last match's names and level and clock come back`() = runTest {
        val settings = FakeSettingsRepository(
            Settings(duelPlayerNames = listOf("Ayşe", "Can"), duelLevel = Level.B2, duelClockSeconds = 240),
        )
        val state = viewModel(settings).state.value

        assertEquals(listOf("Ayşe", "Can"), state.names)
        assertEquals(Level.B2, state.level)
        assertEquals(240, state.clockSeconds)
    }

    @Test
    fun `Değiştir edits a name and Kaydet trims it and an empty name means the default`() = runTest {
        val vm = viewModel()
        vm.onIntent(DuelSetupIntent.EditName(1))
        assertEquals(1, vm.state.value.editingPlayer)
        vm.onIntent(DuelSetupIntent.NameChanged("  Can  "))
        vm.onIntent(DuelSetupIntent.SaveName)
        assertEquals(listOf("", "Can"), vm.state.value.names)
        assertNull(vm.state.value.editingPlayer)

        vm.onIntent(DuelSetupIntent.EditName(1))
        assertEquals("Can", vm.state.value.nameDraft)
        vm.onIntent(DuelSetupIntent.NameChanged("   "))
        vm.onIntent(DuelSetupIntent.SaveName)
        assertEquals(listOf("", ""), vm.state.value.names)
    }

    @Test
    fun `a name never gets longer than 12 characters`() = runTest {
        val vm = viewModel()
        vm.onIntent(DuelSetupIntent.EditName(0))
        vm.onIntent(DuelSetupIntent.NameChanged("Abcdefghijklmnop"))

        assertEquals("Abcdefghijkl", vm.state.value.nameDraft)
    }

    @Test
    fun `Vazgeç and back close the dialog without a change`() = runTest {
        val vm = viewModel()
        vm.onIntent(DuelSetupIntent.EditName(0))
        vm.onIntent(DuelSetupIntent.NameChanged("Ali"))
        vm.onIntent(DuelSetupIntent.CancelName)
        assertEquals(listOf("", ""), vm.state.value.names)

        vm.effects.test {
            vm.onIntent(DuelSetupIntent.EditName(0))
            vm.onIntent(DuelSetupIntent.Back)
            assertNull(vm.state.value.editingPlayer)
            vm.onIntent(DuelSetupIntent.Back)
            assertEquals(DuelSetupEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun `only the offered clocks can be picked`() = runTest {
        val vm = viewModel()
        vm.onIntent(DuelSetupIntent.SelectClock(90))
        vm.onIntent(DuelSetupIntent.SelectClock(100))

        assertEquals(90, vm.state.value.clockSeconds)
    }

    @Test
    fun `BAŞLA saves the setup for the next match and starts the duel`() = runTest {
        val settings = FakeSettingsRepository()
        val vm = viewModel(settings)
        vm.onIntent(DuelSetupIntent.EditName(0))
        vm.onIntent(DuelSetupIntent.NameChanged("Ayşe"))
        vm.onIntent(DuelSetupIntent.SaveName)
        vm.onIntent(DuelSetupIntent.SelectLevel(Level.C1))
        vm.onIntent(DuelSetupIntent.SelectClock(240))

        vm.effects.test {
            vm.onIntent(DuelSetupIntent.Start)
            vm.onIntent(DuelSetupIntent.Start)
            assertEquals(DuelSetupEffect.StartDuel(DuelRoute("C1", 240, "Ayşe", "")), awaitItem())
            expectNoEvents()
        }
        val stored = settings.settings.value
        assertEquals(listOf("Ayşe", ""), stored.duelPlayerNames)
        assertEquals(Level.C1, stored.duelLevel)
        assertEquals(240, stored.duelClockSeconds)
    }
}
