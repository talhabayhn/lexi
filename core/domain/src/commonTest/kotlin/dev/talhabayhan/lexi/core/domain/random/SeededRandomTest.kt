package dev.talhabayhan.lexi.core.domain.random

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Golden values come from the reference SplitMix64 algorithm (Vigna, `splitmix64.c`); seed 1234567 is also the
 * published Rosetta Code vector. If any of these fail, wheels and daily seeds differ between app versions/devices.
 */
class SeededRandomTest {
    @Test
    fun `nextLong matches the reference SplitMix64 sequence for seed 0`() {
        val random = SeededRandom(0L)

        val actual = List(5) { random.nextLong() }

        val expected = listOf(
            0xE220A8397B1DCDAFuL,
            0x6E789E6AA1B965F4uL,
            0x06C45D188009454FuL,
            0xF88BB8A8724C81ECuL,
            0x1B39896A51A8749BuL,
        ).map { it.toLong() }
        assertEquals(expected, actual)
    }

    @Test
    fun `nextLong matches the reference SplitMix64 sequence for seed 1234567`() {
        val random = SeededRandom(1234567L)

        val actual = List(5) { random.nextLong().toULong() }

        val expected = listOf(
            6457827717110365317uL,
            3203168211198807973uL,
            9817491932198370423uL,
            4593380528125082431uL,
            16408922859458223821uL,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `nextInt with bound matches the golden sequence`() {
        val random = SeededRandom(42L)

        val actual = List(20) { random.nextInt(10) }

        assertEquals(listOf(1, 3, 1, 5, 5, 7, 2, 3, 7, 4, 3, 5, 4, 7, 8, 0, 7, 5, 2, 9), actual)
    }

    @Test
    fun `nextInt with power of two bound matches the golden sequence`() {
        val random = SeededRandom(42L)

        val actual = List(10) { random.nextInt(16) }

        assertEquals(listOf(11, 2, 4, 5, 0, 13, 3, 12, 5, 9), actual)
    }

    @Test
    fun `nextInt with bound stays in range`() {
        val random = SeededRandom(99L)
        for (bound in listOf(1, 2, 3, 7, 26, 1000, Int.MAX_VALUE)) {
            repeat(500) {
                val value = random.nextInt(bound)
                assertTrue(value in 0 until bound, "value $value out of [0, $bound)")
            }
        }
    }

    @Test
    fun `nextInt with bound has no modulo bias`() {
        // bound = 2/3 of 2^31: a plain `next31 % bound` would put 2/3 of the values below bound / 2.
        val bound = 1_431_655_765
        val random = SeededRandom(2024L)
        val samples = 20_000

        val lowerHalf = (1..samples).count { random.nextInt(bound) < bound / 2 }

        val ratio = lowerHalf.toDouble() / samples
        assertTrue(ratio in 0.48..0.52, "lower-half ratio $ratio shows modulo bias")
    }

    @Test
    fun `nextInt with bound is roughly uniform over small ranges`() {
        val random = SeededRandom(5L)
        val counts = IntArray(6)

        repeat(60_000) { counts[random.nextInt(6)]++ }

        counts.forEach { assertTrue(it in 9_500..10_500, "bucket count $it is not uniform") }
    }

    @Test
    fun `nextInt rejects a non-positive bound`() {
        assertFailsWith<IllegalArgumentException> { SeededRandom(1L).nextInt(0) }
        assertFailsWith<IllegalArgumentException> { SeededRandom(1L).nextInt(-3) }
    }

    @Test
    fun `nextInt with range stays in range and rejects empty ranges`() {
        val random = SeededRandom(3L)
        repeat(1_000) {
            val value = random.nextInt(-5, 5)
            assertTrue(value in -5 until 5)
        }
        assertFailsWith<IllegalArgumentException> { random.nextInt(4, 4) }
    }

    @Test
    fun `nextInt without bound uses the upper 32 bits`() {
        val expected = SeededRandom(0L).nextLong().ushr(32).toInt()

        assertEquals(expected, SeededRandom(0L).nextInt())
    }

    @Test
    fun `same seed gives the same sequence`() {
        val a = SeededRandom(-77L)
        val b = SeededRandom(-77L)

        assertEquals(List(100) { a.nextLong() }, List(100) { b.nextLong() })
    }

    @Test
    fun `shuffled matches the golden permutation`() {
        val shuffled = SeededRandom(7L).shuffled((0..9).toList())

        assertEquals(listOf(2, 6, 3, 1, 8, 5, 4, 7, 9, 0), shuffled)
    }

    @Test
    fun `shuffled is a permutation and leaves the input untouched`() {
        val input = (1..30).toList()

        val shuffled = SeededRandom(11L).shuffled(input)

        assertEquals((1..30).toList(), input)
        assertEquals(input.toSet(), shuffled.toSet())
        assertEquals(input.size, shuffled.size)
        assertNotEquals(input, shuffled)
    }

    @Test
    fun `shuffle produces every permutation of three items about equally often`() {
        val random = SeededRandom(13L)
        val counts = mutableMapOf<List<Char>, Int>()

        repeat(60_000) {
            val permutation = random.shuffled(listOf('a', 'b', 'c'))
            counts[permutation] = (counts[permutation] ?: 0) + 1
        }

        assertEquals(6, counts.size)
        counts.values.forEach { assertTrue(it in 9_500..10_500, "permutation count $it is not uniform") }
    }

    @Test
    fun `shuffle of empty and single element lists is a no-op`() {
        val random = SeededRandom(1L)

        assertEquals(emptyList(), random.shuffled(emptyList<Int>()))
        assertEquals(listOf(9), random.shuffled(listOf(9)))
    }

    @Test
    fun `pick returns the element at nextInt of the list size`() {
        val items = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")

        val picked = SeededRandom(42L).pick(items)

        assertEquals(items[SeededRandom(42L).nextInt(items.size)], picked)
        assertFailsWith<IllegalArgumentException> { SeededRandom(42L).pick(emptyList<String>()) }
    }

    @Test
    fun `derive depends only on the original seed and not on prior draws`() {
        val fresh = SeededRandom(42L)
        val used = SeededRandom(42L).apply { repeat(10) { nextLong() } }

        val expected = SeededRandom(Seeds.derive(42L, "wheel")).nextLong()

        assertEquals(expected, fresh.derive("wheel").nextLong())
        assertEquals(expected, used.derive("wheel").nextLong())
    }
}
