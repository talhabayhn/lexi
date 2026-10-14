package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KeyboardGeneratorTest {
    private fun keysRandom(seed: Long, letter: Char) = SeededRandom(seed).derive(KeyboardGenerator.seedPurpose(letter))

    private fun generate(answer: String, seed: Long = 42L): List<LetterKey> =
        KeyboardGenerator.generate(answer, keysRandom(seed, answer.first()))

    /** Answer keys uppercase, distractors lowercase, in key order. */
    private fun List<LetterKey>.encoded(): String =
        joinToString("") { if (it.isDistractor) it.letter.lowercase() else it.letter.toString() }

    private fun List<LetterKey>.answerLetters(): List<Char> = filterNot { it.isDistractor }.map { it.letter }

    private fun List<LetterKey>.distractors(): List<Char> = filter { it.isDistractor }.map { it.letter }

    @Test
    fun `key count is at least 10 then rounds up to a full row of 5 capped at 15`() {
        val expected = mapOf(1 to 10, 2 to 10, 5 to 10, 10 to 10, 11 to 15, 14 to 15, 15 to 15)

        assertEquals(expected, expected.keys.associateWith(KeyboardGenerator::keyCount))
    }

    @Test
    fun `key count rejects answers that cannot fit on the keyboard`() {
        assertFailsWith<IllegalArgumentException> { KeyboardGenerator.keyCount(0) }
        assertFailsWith<IllegalArgumentException> { KeyboardGenerator.keyCount(16) }
    }

    @Test
    fun `keys are the answer letter multiset plus distractors`() {
        val keys = generate("APPLE")

        assertEquals(10, keys.size)
        assertEquals("AELPP".toList(), keys.answerLetters().sorted())
        assertEquals(5, keys.distractors().size)
    }

    @Test
    fun `spaces in multi-word answers need no key`() {
        val keys = generate("GIVE UP")

        assertEquals(10, keys.size)
        assertEquals("EGIPUV".toList(), keys.answerLetters().sorted())
    }

    @Test
    fun `keyboards match the golden key sets`() {
        assertEquals("HElsIdNCTK", generate("KITCHEN", seed = 42L).encoded())
        assertEquals("UIaoEhGPVm", generate("GIVE UP", seed = 7L).encoded())
        assertEquals("sNBGECJHDFMIAKL", generate("ABCDEFGHIJKLMN", seed = 1L).encoded())
    }

    @Test
    fun `distractors are distinct letters that are not in the answer`() {
        for (seed in 1L..200L) {
            val keys = generate("BANANA", seed)
            val distractors = keys.distractors()
            assertEquals(distractors.size, distractors.toSet().size, "seed $seed")
            assertTrue(distractors.none { it in "BANANA" }, "seed $seed: $distractors")
        }
    }

    @Test
    fun `at least two distractors are near letters when available`() {
        for (answer in listOf("CAT", "KITCHEN", "WINDOW", "ZOO", "QUIZ")) {
            for (seed in 1L..100L) {
                val near = answer.flatMap { KeyboardGenerator.nearLetters(it) }.toSet()
                val nearDistractors = generate(answer, seed).distractors().count { it in near }
                assertTrue(nearDistractors >= 2, "$answer seed $seed")
            }
        }
    }

    @Test
    fun `a single distractor slot is still filled when the answer is long`() {
        val keys = generate("ABCDEFGHIJKLMN")

        assertEquals(15, keys.size)
        assertEquals(1, keys.distractors().size)
    }

    @Test
    fun `distractors are weighted by English letter frequency`() {
        // E and Z are not near any letter of DOG, so they can only come from the frequency-weighted draw.
        val counts = (1L..2_000L).flatMap { generate("DOG", it).distractors() }.groupingBy { it }.eachCount()

        val e = counts['E'] ?: 0
        val z = counts['Z'] ?: 0
        assertTrue(e > 10 * z, "E=$e Z=$z")
    }

    @Test
    fun `same seed gives the same keyboard`() {
        assertEquals(generate("KITCHEN", seed = 5L), generate("KITCHEN", seed = 5L))
    }

    @Test
    fun `keys are shuffled so answer letters are not always first`() {
        val keyboards = (1L..50L).map { generate("KITCHEN", it).encoded() }.toSet()

        assertTrue(keyboards.size > 40)
        assertTrue(keyboards.any { !it.startsWith("KITCHEN") && it.take(7).any(Char::isLowerCase) })
    }

    @Test
    fun `all keys start available`() {
        assertTrue(generate("KITCHEN").all { it.state == KeyState.AVAILABLE })
    }

    @Test
    fun `near letters are symmetric and every letter has some`() {
        for (letter in 'A'..'Z') {
            val near = KeyboardGenerator.nearLetters(letter)
            assertTrue(near.isNotEmpty(), "$letter")
            assertTrue(letter !in near)
            near.forEach { assertTrue(letter in KeyboardGenerator.nearLetters(it), "$letter/$it") }
        }
        assertTrue('K' in KeyboardGenerator.nearLetters('C'))
        assertTrue('E' in KeyboardGenerator.nearLetters('I'))
        assertTrue('W' in KeyboardGenerator.nearLetters('V'))
    }

    @Test
    fun `seed purpose is keys plus the slot letter`() {
        assertEquals("keys:K", KeyboardGenerator.seedPurpose('K'))
    }

    @Test
    fun `generate rejects answers longer than the keyboard`() {
        assertFailsWith<IllegalArgumentException> { generate("ABCDEFGHIJKLMNOP") }
    }
}
