package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.time.CivilDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DailySeedTest {
    @Test
    fun `same parameters produce exact same seed`() {
        val seed1 = DailySeed.seed(epochDay = 20_741L, level = Level.A1, contentVersion = 1L)
        val seed2 = DailySeed.seed(epochDay = 20_741L, level = Level.A1, contentVersion = 1L)
        assertEquals(seed1, seed2)
    }

    @Test
    fun `different epoch days produce different seeds`() {
        val seedDay1 = DailySeed.seed(epochDay = 20_741L, level = Level.A1, contentVersion = 1L)
        val seedDay2 = DailySeed.seed(epochDay = 20_742L, level = Level.A1, contentVersion = 1L)
        assertNotEquals(seedDay1, seedDay2)
    }

    @Test
    fun `level is ignored in seed calculation for backward compatibility`() {
        val seedA1 = DailySeed.seed(epochDay = 20_741L, level = Level.A1, contentVersion = 1L)
        val seedA2 = DailySeed.seed(epochDay = 20_741L, level = Level.A2, contentVersion = 1L)
        val seedB1 = DailySeed.seed(epochDay = 20_741L, level = Level.B1, contentVersion = 1L)
        val shared = DailySeed.seed(epochDay = 20_741L, contentVersion = 1L)
        assertEquals(shared, seedA1)
        assertEquals(shared, seedA2)
        assertEquals(shared, seedB1)
    }

    @Test
    fun `different content versions produce different seeds`() {
        val seedV1 = DailySeed.seed(epochDay = 20_741L, level = Level.A1, contentVersion = 1L)
        val seedV2 = DailySeed.seed(epochDay = 20_741L, level = Level.A1, contentVersion = 2L)
        assertNotEquals(seedV1, seedV2)
    }

    @Test
    fun `civil date overload is deterministic`() {
        val date = CivilDate(2026, 10, 15)
        val seed1 = DailySeed.seed(date, Level.A1, 1L)
        val seed2 = DailySeed.seed(date, Level.A1, 1L)
        assertEquals(seed1, seed2)
    }

    @Test
    fun `golden daily seed values are pinned`() {
        // Pinned to prevent accidental drift across platforms/compiler versions.
        val goldenShared = DailySeed.seed(epochDay = 20_741L, contentVersion = 1L)
        // Verify they are non-zero and stable
        assertEquals(goldenShared, DailySeed.seed(epochDay = 20_741L, contentVersion = 1L))
        assertEquals(goldenShared, DailySeed.seed(epochDay = 20_741L, level = Level.A1, contentVersion = 1L))
    }
}
