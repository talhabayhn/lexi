package dev.talhabayhan.lexi.feature.home

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {
    @Test
    fun `initial state is ready`() = runTest {
        HomeViewModel().state.test {
            assertEquals(HomeUiState(isReady = true), awaitItem())
        }
    }
}
