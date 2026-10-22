package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.fullAlphabetPool
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WheelGeneratorTest {
    /** Three A1 cards per letter: `<L>ONE`, `<L>SIX`, `<L>TWO` (sorted by id in that order). */
    private val triplePool: List<Card> = listOf("ONE", "TWO", "SIX").flatMap { fullAlphabetPool(suffix = it) }

    private fun wheelRandom(seed: Long) = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE)

    private fun generate(
        pool: List<Card> = triplePool,
        seed: Long = 42L,
        level: Level = Level.A1,
        category: CategoryId = CategoryId.MIXED,
        mode: GameMode = GameMode.CLASSIC,
        recentlySeen: Set<Card> = emptySet(),
    ): Wheel = WheelGenerator.generate(
        pool = pool,
        level = level,
        category = category,
        mode = mode,
        random = wheelRandom(seed),
        recentlySeen = recentlySeen.map { it.id }.toSet(),
    )

    @Test
    fun `wheel has 26 slots A to Z all TODO`() {
        val wheel = generate()

        assertEquals(('A'..'Z').toList(), wheel.slots.map { it.letter })
        assertTrue(wheel.slots.all { it.state == SlotState.TODO })
    }

    @Test
    fun `wheel picks match the golden sequence for seed 42`() {
        val wheel = generate(seed = 42L)

        val picked = wheel.slots.joinToString("") { it.card?.answer?.substring(1, 2).orEmpty() }
        assertEquals("SSTSSTSOTOTOSSSOTSSOOOTSST", picked)
    }

    @Test
    fun `same seed and pool give the same wheel regardless of pool order`() {
        val reversed = generate(pool = triplePool.reversed())

        assertEquals(generate(), reversed)
        assertEquals(generate(), generate(pool = SeededRandom(1L).shuffled(triplePool)))
    }

    @Test
    fun `different seeds give different wheels`() {
        assertNotEquals(generate(seed = 1L), generate(seed = 2L))
    }

    @Test
    fun `letter without a card becomes a skipped slot`() {
        val pool = triplePool.filterNot { it.letter == 'Q' || it.letter == 'X' }

        val wheel = generate(pool = pool)

        assertEquals(26, wheel.slots.size)
        assertEquals(SlotState.SKIPPED, wheel.slots[wheel.indexOf('Q')].state)
        assertNull(wheel.slots[wheel.indexOf('X')].card)
        assertEquals(24, wheel.playableSlots.size)
    }

    @Test
    fun `wheel uses only cards of the requested level`() {
        val pool = fullAlphabetPool(level = Level.A1) + fullAlphabetPool(level = Level.B2, suffix = "OB")

        val wheel = generate(pool = pool, level = Level.B2)

        assertTrue(wheel.slots.all { it.card?.level == Level.B2 })
    }

    @Test
    fun `category restricts the pool and a missing category letter is skipped`() {
        val animals = fullAlphabetPool(category = "animals", suffix = "AN").filterNot { it.letter == 'Z' }
        val pool = fullAlphabetPool(category = "home") + animals

        val wheel = generate(pool = pool, category = CategoryId("animals"))

        assertTrue(wheel.playableSlots.all { it.card?.category == CategoryId("animals") })
        assertEquals(SlotState.SKIPPED, wheel.slots[wheel.indexOf('Z')].state)
    }

    @Test
    fun `mixed category uses all categories of the level`() {
        val pool = fullAlphabetPool(category = "home").filter { it.letter < 'N' } +
            fullAlphabetPool(category = "animals", suffix = "AN").filter { it.letter >= 'N' }

        val wheel = generate(pool = pool, category = CategoryId.MIXED)

        assertEquals(26, wheel.playableSlots.size)
    }

    @Test
    fun `recently seen cards are avoided in solo modes`() {
        val seen = triplePool.filterNot { it.answer.endsWith("TWO") }.toSet()

        for (seed in 1L..20L) {
            val wheel = generate(seed = seed, recentlySeen = seen)
            assertTrue(wheel.slots.all { it.card?.answer?.endsWith("TWO") == true }, "seed $seed")
        }
    }

    @Test
    fun `recently seen cards are used when a letter has no unseen card`() {
        val wheel = generate(recentlySeen = triplePool.toSet())

        assertEquals(26, wheel.playableSlots.size)
        assertEquals(generate(recentlySeen = emptySet()), wheel)
    }

    @Test
    fun `daily ignores card history so every player gets the same wheel`() {
        val seen = triplePool.filterNot { it.answer.endsWith("TWO") }.toSet()

        val withHistory = generate(mode = GameMode.DAILY, recentlySeen = seen)

        assertEquals(generate(mode = GameMode.DAILY), withHistory)
    }

    @Test
    fun `duel ignores card history`() {
        val seen = triplePool.filterNot { it.answer.endsWith("TWO") }.toSet()

        assertEquals(generate(mode = GameMode.DUEL), generate(mode = GameMode.DUEL, recentlySeen = seen))
    }

    @Test
    fun `pool card ids must be unique`() {
        val duplicate = card("APPLE", id = "a1-dup")
        val other = card("ARM", id = "a1-dup")

        assertFailsWith<IllegalArgumentException> { generate(pool = listOf(duplicate, other)) }
        assertFailsWith<IllegalArgumentException> { WheelGenerator.review(listOf(duplicate, other), wheelRandom(1L)) }
    }

    @Test
    fun `cards seen within the last 7 days are recently seen`() {
        val day = 86_400_000L
        val now = 100 * day
        val lastSeen = mapOf(
            CardId("today") to now - 1,
            CardId("six-days") to now - 6 * day,
            CardId("seven-days") to now - 7 * day,
            CardId("old") to now - 30 * day,
        )

        val recent = WheelGenerator.recentlySeen(lastSeen, now, GameConfig())

        assertEquals(setOf(CardId("today"), CardId("six-days")), recent)
    }

    @Test
    fun `review mode must use the review wheel`() {
        assertFailsWith<IllegalArgumentException> { generate(mode = GameMode.REVIEW) }
    }

    @Test
    fun `review wheel only has letters with due cards in A-Z order`() {
        val due = listOf(card("ZEBRA"), card("APPLE"), card("BOX", letter = 'X'), card("MILK"))

        val wheel = WheelGenerator.review(due, wheelRandom(7L))

        assertEquals(listOf('A', 'M', 'X', 'Z'), wheel.slots.map { it.letter })
        assertTrue(wheel.slots.all { it.state == SlotState.TODO })
    }

    @Test
    fun `review wheel has one card per letter picked by seed`() {
        val due = listOf(card("APPLE"), card("ARM"), card("ANT"), card("BREAD"))

        val wheel = WheelGenerator.review(due, wheelRandom(7L))

        assertEquals(2, wheel.slots.size)
        assertEquals(wheel, WheelGenerator.review(due.reversed(), wheelRandom(7L)))
        val picks = (1L..30L).map { WheelGenerator.review(due, wheelRandom(it)).slots.first().card?.answer }.toSet()
        assertEquals(setOf("APPLE", "ARM", "ANT"), picks)
    }

    @Test
    fun `review wheel mixes levels and categories`() {
        val due = listOf(card("APPLE", level = Level.A1), card("BREAD", level = Level.C1, category = "home"))

        val wheel = WheelGenerator.review(due, wheelRandom(1L))

        assertEquals(listOf(Level.A1, Level.C1), wheel.slots.map { it.card?.level })
    }

    @Test
    fun `review wheel without due cards is empty`() {
        assertTrue(WheelGenerator.review(emptyList(), wheelRandom(1L)).slots.isEmpty())
    }

    /** Per letter: one STARTS card `<L>START` plus two CONTAINS cards `Q<L>`/`QQ<L>` (letter Q: `A<L>`/`AA<L>`). */
    private val mixedRulePool: List<Card> = ('A'..'Z').flatMap { letter ->
        val prefix = if (letter == 'Q') 'A' else 'Q'
        listOf(
            card("${letter}START"),
            card("$prefix$letter", letter = letter),
            card("$prefix$prefix$letter", letter = letter),
        )
    }

    @Test
    fun `a letter with a starts card never gets a contains card`() {
        for (seed in 1L..50L) {
            val wheel = generate(pool = mixedRulePool, seed = seed)
            assertTrue(wheel.slots.all { it.rule == LetterRule.STARTS }, "seed $seed")
        }
    }

    @Test
    fun `a letter without a starts card gets a contains card`() {
        val pool = mixedRulePool.filterNot { it.letter == 'J' && it.rule == LetterRule.STARTS }

        val picks = (1L..30L).map { generate(pool = pool, seed = it).slots[9].card?.answer }.toSet()

        assertEquals(setOf("QJ", "QQJ"), picks)
    }

    @Test
    fun `contains cards are used when every starts card of the letter was seen recently`() {
        val seen = mixedRulePool.filter { it.letter == 'J' && it.rule == LetterRule.STARTS }.toSet()

        val wheel = generate(pool = mixedRulePool, recentlySeen = seen)

        assertEquals(LetterRule.CONTAINS, wheel.slots[9].rule)
        assertTrue(wheel.slots.filter { it.letter != 'J' }.all { it.rule == LetterRule.STARTS })
    }

    @Test
    fun `minimum starts candidates comes from the config`() {
        val config = GameConfig(minStartsCandidates = 2)
        fun ruleOfA(seed: Long) = WheelGenerator.generate(
            mixedRulePool,
            Level.A1,
            CategoryId.MIXED,
            GameMode.CLASSIC,
            wheelRandom(seed),
            config = config,
        ).slots.first().rule

        // One STARTS card is below the minimum of 2, so all three cards of the letter are candidates.
        assertEquals(setOf(LetterRule.STARTS, LetterRule.CONTAINS), (1L..40L).map(::ruleOfA).toSet())
    }

    @Test
    fun `property - contains only when the eligible starts candidates are fewer than the minimum`() {
        for (seed in 1L..200L) {
            val random = SeededRandom(seed).derive("pool")
            val pool = mixedRulePool.filter { random.nextInt(3) != 0 }
            val seen = pool.filter { random.nextInt(4) == 0 }.map { it.id }.toSet()
            val wheel = WheelGenerator.generate(
                pool,
                Level.A1,
                CategoryId.MIXED,
                GameMode.CLASSIC,
                wheelRandom(seed),
                seen,
            )
            for (slot in wheel.slots) {
                val candidates = pool.filter { it.letter == slot.letter }
                val eligible = candidates.filter { it.id !in seen }.ifEmpty { candidates }
                val eligibleStarts = eligible.count { it.rule == LetterRule.STARTS }
                when {
                    candidates.isEmpty() -> assertEquals(SlotState.SKIPPED, slot.state, "seed $seed ${slot.letter}")

                    eligibleStarts >= GameConfig().minStartsCandidates ->
                        assertEquals(LetterRule.STARTS, slot.rule, "seed $seed ${slot.letter}")

                    else -> assertTrue(slot.card in eligible, "seed $seed ${slot.letter}")
                }
            }
        }
    }

    @Test
    fun `daily prefers starts cards`() {
        val pool = Level.entries.flatMap { level ->
            mixedRulePool.map { card(it.answer, letter = it.letter, level = level) }
        }
        for (seed in 1L..30L) {
            val wheel = WheelGenerator.daily(pool, wheelRandom(seed))
            assertTrue(wheel.slots.all { it.rule == LetterRule.STARTS }, "seed $seed")
        }
        val noStartsJ = pool.filterNot { it.letter == 'J' && it.rule == LetterRule.STARTS }
        assertEquals(LetterRule.CONTAINS, WheelGenerator.daily(noStartsJ, wheelRandom(1L)).slots[9].rule)
    }

    @Test
    fun `review prefers due starts cards`() {
        val due = listOf(card("BOX", letter = 'X'), card("XRAY"), card("TAXI", letter = 'X'))

        val picks = (1L..30L).map { WheelGenerator.review(due, wheelRandom(it)).slots.single().card?.answer }.toSet()

        assertEquals(setOf("XRAY"), picks)
        assertEquals("BOX", WheelGenerator.review(due.take(1), wheelRandom(1L)).slots.single().card?.answer)
    }

    // region exam pack wheel (GAME_RULES.md §1)

    private fun pack(pool: List<Card>, seed: Long = 42L, recentlySeen: Set<Card> = emptySet()): Wheel =
        WheelGenerator.pack(pool, wheelRandom(seed), recentlySeen.map { it.id }.toSet())

    @Test
    fun `pack wheel picks exactly like a regular wheel over the same single-level pool`() {
        for (seed in 1L..20L) {
            assertEquals(generate(seed = seed), pack(triplePool, seed), "seed $seed")
        }
    }

    @Test
    fun `pack wheel mixes the levels of its cards`() {
        val pool = fullAlphabetPool(level = Level.A2).filter { it.letter < 'N' } +
            fullAlphabetPool(level = Level.C1, suffix = "OC").filter { it.letter >= 'N' }

        val wheel = pack(pool)

        assertEquals(26, wheel.playableSlots.size)
        assertEquals(setOf(Level.A2, Level.C1), wheel.playableSlots.mapNotNull { it.card?.level }.toSet())
    }

    @Test
    fun `pack wheel is deterministic and ignores the pool order`() {
        val pool = fullAlphabetPool(level = Level.B1) + fullAlphabetPool(level = Level.B2, suffix = "OB")

        assertEquals(pack(pool), pack(pool.reversed()))
        assertEquals(pack(pool), pack(SeededRandom(7L).shuffled(pool)))
        assertNotEquals(pack(pool, seed = 1L), pack(pool, seed = 2L))
    }

    @Test
    fun `pack wheel skips letters without a pack card`() {
        val wheel = pack(triplePool.filterNot { it.letter == 'X' })

        assertEquals(SlotState.SKIPPED, wheel.slots[wheel.indexOf('X')].state)
        assertEquals(25, wheel.playableSlots.size)
    }

    @Test
    fun `pack wheel avoids recently seen cards and prefers starts cards`() {
        val seen = triplePool.filterNot { it.answer.endsWith("TWO") }.toSet()
        for (seed in 1L..20L) {
            assertTrue(
                pack(triplePool, seed, seen).slots.all {
                    it.card?.answer?.endsWith("TWO") == true
                },
                "seed $seed",
            )
            assertTrue(pack(mixedRulePool, seed).slots.all { it.rule == LetterRule.STARTS }, "seed $seed")
        }
    }

    @Test
    fun `pack pool card ids must be unique`() {
        assertFailsWith<IllegalArgumentException> { pack(triplePool + triplePool.first()) }
    }

    // endregion
}
