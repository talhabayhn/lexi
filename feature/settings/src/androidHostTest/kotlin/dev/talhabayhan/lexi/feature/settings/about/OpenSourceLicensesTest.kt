package dev.talhabayhan.lexi.feature.settings.about

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.domain.AppInfo
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpenSourceLicensesTest {

    @Test
    fun `licenses list is not empty`() {
        assertTrue(OPEN_SOURCE_LICENSES.isNotEmpty())
    }

    @Test
    fun `every license has non-blank name and license`() {
        for (entry in OPEN_SOURCE_LICENSES) {
            assertFalse(entry.name.isBlank(), "License entry name should not be blank")
            assertFalse(entry.license.isBlank(), "License name for ${entry.name} should not be blank")
        }
    }

    @Test
    fun `no duplicate license entries by name`() {
        val names = OPEN_SOURCE_LICENSES.map { it.name }
        val duplicates = names.groupBy { it }.filter { it.value.size > 1 }.keys
        assertTrue(duplicates.isEmpty(), "Found duplicate license entries: $duplicates")
    }

    @Test
    fun `jost font license is included with SIL Open Font License 1_1`() {
        val jost = OPEN_SOURCE_LICENSES.firstOrNull { it.name == "Jost" }
        assertTrue(jost != null, "Jost font must be in the licenses list")
        assertEquals("SIL Open Font License 1.1", jost.license)
    }

    @Test
    fun `licenses list contains only jost font license`() {
        assertEquals(1, OPEN_SOURCE_LICENSES.size)
        assertEquals("Jost", OPEN_SOURCE_LICENSES.first().name)
    }

    @Test
    fun `about ui state defaults to AppInfo constants`() {
        val state = AboutUiState()
        assertEquals(AppInfo.NAME, state.appName)
        assertEquals(AppInfo.VERSION_NAME, state.versionName)
        assertEquals(AppInfo.BUILD_NUMBER, state.buildNumber)
        assertEquals(OPEN_SOURCE_LICENSES.size, state.licenses.size)
    }

    @Test
    fun `about viewmodel emits navigate back on back intent`() = runTest {
        val viewModel = AboutViewModel()
        viewModel.effects.test {
            viewModel.onIntent(AboutIntent.Back)
            assertEquals(AboutEffect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `about viewmodel emits open url on open url intent`() = runTest {
        val viewModel = AboutViewModel()
        viewModel.effects.test {
            viewModel.onIntent(AboutIntent.OpenUrl("https://example.com"))
            assertEquals(AboutEffect.OpenUrl("https://example.com"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
