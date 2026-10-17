package dev.talhabayhan.lexi.core.domain.daily

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DailySeedTest {
    @Test
    fun `same parameters produce exact same seed`() {
        val seed1 = DailySeed.seed(epochDay = 20_741L, contentVersion = 1L)
        val seed2 = DailySeed.seed(epochDay = 20_741L, contentVersion = 1L)
        assertEquals(seed1, seed2)
    }

    @Test
    fun `different epoch days produce different seeds`() {
        val seedDay1 = DailySeed.seed(epochDay = 20_741L, contentVersion = 1L)
        val seedDay2 = DailySeed.seed(epochDay = 20_742L, contentVersion = 1L)
        assertNotEquals(seedDay1, seedDay2)
    }

    @Test
    fun `different content versions produce different seeds`() {
        val seedV1 = DailySeed.seed(epochDay = 20_741L, contentVersion = 1L)
        val seedV2 = DailySeed.seed(epochDay = 20_741L, contentVersion = 2L)
        assertNotEquals(seedV1, seedV2)
    }

    @Test
    fun `golden daily seed values are pinned to exact literal Long values`() {
        // Pinned to prevent accidental drift across platforms/compiler versions.
        assertEquals(6034053112182483930L, DailySeed.seed(epochDay = 20_741L, contentVersion = 1L))
        assertEquals(5975572613751024269L, DailySeed.seed(epochDay = 20_742L, contentVersion = 1L))
    }
}
