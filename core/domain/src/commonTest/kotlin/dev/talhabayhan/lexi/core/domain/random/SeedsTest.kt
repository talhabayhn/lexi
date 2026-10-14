package dev.talhabayhan.lexi.core.domain.random

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SeedsTest {
    @Test
    fun `stableHash is FNV-1a 64 over UTF-8 and matches the reference vectors`() {
        assertEquals(0xCBF29CE484222325uL.toLong(), Seeds.stableHash(""))
        assertEquals(0xAF63DC4C8601EC8CuL.toLong(), Seeds.stableHash("a"))
        assertEquals(0x85944171F73967E8uL.toLong(), Seeds.stableHash("foobar"))
        assertEquals(0xF03BE534248BD2BAuL.toLong(), Seeds.stableHash("keys:K"))
    }

    @Test
    fun `stableHash encodes non-ASCII text as UTF-8`() {
        // U+0131 (dotless i) is 0xC4 0xB1 in UTF-8.
        val expected = (0xCBF29CE484222325uL.toLong() xor 0xC4L) * 0x100000001B3L xor 0xB1L
        assertEquals(expected * 0x100000001B3L, Seeds.stableHash("ı"))
    }

    @Test
    fun `derive mixes seed and purpose with the SplitMix64 finalizer`() {
        assertEquals(3542783700709295849L, Seeds.derive(42L, "wheel"))
    }

    @Test
    fun `derive differs per purpose and per seed`() {
        assertNotEquals(Seeds.derive(42L, "wheel"), Seeds.derive(42L, "keys:A"))
        assertNotEquals(Seeds.derive(42L, "keys:A"), Seeds.derive(42L, "keys:B"))
        assertNotEquals(Seeds.derive(42L, "wheel"), Seeds.derive(43L, "wheel"))
    }
}
