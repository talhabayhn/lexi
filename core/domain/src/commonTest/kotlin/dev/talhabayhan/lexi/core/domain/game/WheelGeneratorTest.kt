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
        history = recentHistory(recentlySeen),
    )

    /** [cards] seen one day before [NOW] (inside the recently-seen window). */
    private fun recentHistory(cards: Collection<Card>) = CardHistory(cards.associate { it.id to NOW - DAY }, NOW)

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
    fun `recently seen cards are used when a letter has no unseen card and the most recent one comes last`() {
        val daysAgo = mapOf("ONE" to 3, "SIX" to 2, "TWO" to 1)
        val history =
            CardHistory(triplePool.associate { it.id to NOW - daysAgo.getValue(it.answer.drop(1)) * DAY }, NOW)

        for (seed in 1L..20L) {
            val wheel = WheelGenerator.generate(
                triplePool,
                Level.A1,
                CategoryId.MIXED,
                GameMode.CLASSIC,
                wheelRandom(seed),
                history,
            )
            assertEquals(26, wheel.playableSlots.size)
            assertTrue(wheel.slots.none { it.card!!.answer.endsWith("TWO") }, "seed $seed")
        }
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
    fun `review wheel never shows an answer twice`() {
        // Content keeps answers unique per level only: the same word can be due at two levels and two letters.
        val due = listOf(
            card("JUNGLE", level = Level.A2),
            card("JUNGLE", letter = 'U', level = Level.A1),
            card("MUG", letter = 'U', level = Level.A1),
        )

        val answers = (1L..30L).map { seed ->
            WheelGenerator.review(due, wheelRandom(seed)).slots.map { it.letter to it.card?.answer }
        }.toSet()

        assertEquals(setOf(listOf('J' to "JUNGLE", 'U' to "MUG")), answers)
    }

    @Test
    fun `review wheel leaves out a letter whose due cards are all already on the wheel`() {
        val due = listOf(card("JUNGLE", level = Level.A2), card("JUNGLE", letter = 'U', level = Level.A1))

        val wheel = WheelGenerator.review(due, wheelRandom(1L))

        assertEquals(listOf('J' to "JUNGLE"), wheel.slots.map { it.letter to it.card?.answer })
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
                CardHistory(seen.associateWith { NOW - DAY }, NOW),
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
        WheelGenerator.pack(pool, wheelRandom(seed), recentHistory(recentlySeen))

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

    // region thin category fill (GAME_RULES.md §1, owner decision 2026-11-17)

    private val falseFriends = CategoryId("false-friends")

    /** [letters] category cards (one per letter, distinct answers), all A1 like [triplePool]. */
    private fun categoryCards(letters: Iterable<Char>, category: String = "false-friends"): List<Card> =
        letters.map { card("${it}CAT", category = category) }

    @Test
    fun `a one-card category fills every other letter from the level`() {
        val own = card("EVENTUALLY", category = "false-friends")
        val pool = triplePool + own

        for (seed in 1L..50L) {
            val wheel = generate(pool = pool, seed = seed, category = falseFriends)
            assertEquals(26, wheel.playableSlots.size, "seed $seed")
            assertEquals(own, wheel.slots[wheel.indexOf('E')].card, "seed $seed")
            assertTrue(wheel.slots.filter { it.letter != 'E' }.all { it.card!!.category != falseFriends })
            val picked = wheel.playableSlots.map { it.card!! }
            assertEquals(picked.size, picked.distinctBy { it.id }.size, "seed $seed")
            assertEquals(picked.size, picked.distinctBy { it.answer }.size, "seed $seed")
        }
    }

    @Test
    fun `a thin category keeps its own card on every letter it has`() {
        // Like Müzik B1: 9 letters, some with two cards.
        val ownLetters = "BCDGHMPRS".toList()
        val own = categoryCards(ownLetters) + categoryCards("BMS".toList()).map {
            it.copy(id = CardId("${it.id.value}-2"), answer = "${it.answer}S")
        }

        for (seed in 1L..50L) {
            val wheel = generate(pool = triplePool + own, seed = seed, category = falseFriends)
            assertEquals(26, wheel.playableSlots.size, "seed $seed")
            for (slot in wheel.slots) {
                assertEquals(
                    slot.letter in ownLetters,
                    slot.card!!.category == falseFriends,
                    "seed $seed ${slot.letter}",
                )
            }
        }
    }

    @Test
    fun `a thin category wheel picks like a mixed wheel of its own cards plus the level's other letters`() {
        // One nextInt per playable letter in A-Z order: the fill draws exactly like a mixed wheel over the same cards.
        val ownLetters = "AEKQZ".toList()
        val own = categoryCards(ownLetters)
        val equivalent = own + triplePool.filter { it.letter !in ownLetters }

        for (seed in 1L..50L) {
            assertEquals(
                generate(pool = equivalent, seed = seed, category = CategoryId.MIXED),
                generate(pool = triplePool + own, seed = seed, category = falseFriends),
                "seed $seed",
            )
        }
    }

    @Test
    fun `a category at the minimum letter count is unchanged`() {
        val own = categoryCards('A'..'M')
        assertEquals(GameConfig().minCategoryLetters, own.size)

        for (seed in 1L..50L) {
            val wheel = generate(pool = triplePool + own, seed = seed, category = falseFriends)
            // The level's cards change nothing: the wheel is the one built from the category alone.
            assertEquals(generate(pool = own, seed = seed, category = falseFriends), wheel, "seed $seed")
            assertEquals(13, wheel.playableSlots.size)
            assertTrue(wheel.slots.filter { it.letter > 'M' }.all { it.state == SlotState.SKIPPED })
        }
    }

    @Test
    fun `a category at the minimum keeps letters without a category card skipped even with history`() {
        val own = categoryCards('A'..'M')
        val wheel = generate(pool = triplePool + own, category = falseFriends, recentlySeen = triplePool.toSet())

        assertEquals(13, wheel.playableSlots.size)
        assertTrue(wheel.playableSlots.all { it.card!!.category == falseFriends })
    }

    @Test
    fun `the minimum letter count comes from the config`() {
        val own = categoryCards('A'..'M')
        val wheel = WheelGenerator.generate(
            pool = triplePool + own,
            level = Level.A1,
            category = falseFriends,
            mode = GameMode.CLASSIC,
            random = wheelRandom(42L),
            config = GameConfig(minCategoryLetters = 14),
        )

        assertEquals(26, wheel.playableSlots.size)
        assertFailsWith<IllegalArgumentException> { GameConfig(minCategoryLetters = 0) }
        assertFailsWith<IllegalArgumentException> { GameConfig(minCategoryLetters = 27) }
    }

    @Test
    fun `a category without cards stays empty`() {
        val wheel = generate(pool = triplePool, category = falseFriends)

        assertTrue(wheel.playableSlots.isEmpty())
        assertEquals(26, wheel.slots.size)
    }

    @Test
    fun `a thin category fills only from its own level`() {
        val pool = fullAlphabetPool(level = Level.B1, suffix = "BB").filter { it.letter < 'N' } +
            fullAlphabetPool(level = Level.B2, suffix = "CC") +
            card("ECAT", level = Level.B1, category = "false-friends")

        val wheel = generate(pool = pool, level = Level.B1, category = falseFriends)

        assertEquals(13, wheel.playableSlots.size)
        assertTrue(wheel.playableSlots.all { it.card!!.level == Level.B1 })
    }

    @Test
    fun `a filled letter avoids recently seen level cards and prefers starts cards`() {
        val own = card("EVENTUALLY", category = "false-friends")
        val seen = triplePool.filterNot { it.answer.endsWith("TWO") }.toSet()

        for (seed in 1L..20L) {
            val wheel = generate(pool = triplePool + own, seed = seed, category = falseFriends, recentlySeen = seen)
            assertTrue(
                wheel.slots.filter { it.letter != 'E' }.all { it.card!!.answer.endsWith("TWO") },
                "seed $seed",
            )
        }
        val withContains = mixedRulePool + card("EVENTUALLY", category = "false-friends")
        for (seed in 1L..20L) {
            val wheel = generate(pool = withContains, seed = seed, category = falseFriends)
            assertTrue(wheel.slots.all { it.rule == LetterRule.STARTS }, "seed $seed")
        }
    }

    @Test
    fun `a filled letter never takes the answer of a category card`() {
        // ANNOY is the category's card on Y (CONTAINS); the level also has an ANNOY on A, which comes first.
        val own = card("ANNOY", letter = 'Y', category = "false-friends", id = "a1-annoy-ff")
        val pool = triplePool + card("ANNOY", id = "a1-annoy")

        for (seed in 1L..50L) {
            val wheel = generate(pool = pool + own, seed = seed, category = falseFriends)
            assertNotEquals("ANNOY", wheel.slots[wheel.indexOf('A')].card!!.answer, "seed $seed")
            assertEquals(own, wheel.slots[wheel.indexOf('Y')].card, "seed $seed")
        }
    }

    @Test
    fun `a thin category wheel is deterministic per seed whatever the pool order`() {
        val pool = triplePool + categoryCards("BCDGHMPRS".toList())

        for (seed in 1L..20L) {
            val wheel = generate(pool = pool, seed = seed, category = falseFriends)
            assertEquals(wheel, generate(pool = pool.reversed(), seed = seed, category = falseFriends))
            assertEquals(
                wheel,
                generate(pool = SeededRandom(seed).shuffled(pool), seed = seed, category = falseFriends),
            )
        }
        assertNotEquals(
            generate(pool = pool, seed = 1L, category = falseFriends),
            generate(pool = pool, seed = 2L, category = falseFriends),
        )
    }

    @Test
    fun `property - a category wheel is filled exactly when it has fewer letters than the minimum`() {
        val random = SeededRandom(2026L)
        repeat(200) { run ->
            val count = random.nextInt(27)
            val ownLetters = random.shuffled(('A'..'Z').toList()).take(count).toSet()
            val own = categoryCards(ownLetters)
            val seed = random.nextLong()

            val wheel = generate(pool = triplePool + own, seed = seed, category = falseFriends)

            val expected = if (count in 1 until GameConfig().minCategoryLetters) 26 else count
            assertEquals(expected, wheel.playableSlots.size, "run $run, $count letters")
            assertTrue(wheel.slots.filter { it.letter in ownLetters }.all { it.card!!.category == falseFriends })
            val answers = wheel.playableSlots.map { it.card!!.answer }
            assertEquals(answers.size, answers.toSet().size, "run $run")
        }
    }

    // endregion

    private fun firstLetterOf(pool: List<Card>, category: CategoryId): Char? {
        val level = pool.filter { it.level == Level.A1 }
        val own = if (category == CategoryId.MIXED) level else level.filter { it.category == category }
        return WheelGenerator.firstLetter(
            categoryLetters = own.mapTo(mutableSetOf()) { it.letter },
            levelLetters = level.mapTo(mutableSetOf()) { it.letter },
        )
    }

    private fun Wheel.openingLetter(): Char? = playableSlots.firstOrNull()?.letter

    @Test
    fun `the first letter of a thin category is the filled wheel's first letter`() {
        val pool = triplePool + card("EVENTUALLY", category = "false-friends")

        assertEquals('A', firstLetterOf(pool, falseFriends))
        for (seed in 1L..20L) {
            assertEquals('A', generate(pool = pool, seed = seed, category = falseFriends).openingLetter(), "seed $seed")
        }
    }

    @Test
    fun `the first letter of a category at the minimum is its own first letter`() {
        val pool = triplePool + categoryCards('D'..'P')

        assertEquals('D', firstLetterOf(pool, falseFriends))
        assertEquals('D', generate(pool = pool, category = falseFriends).openingLetter())
    }

    @Test
    fun `the first letter follows the minimum from the config`() {
        val own = setOf('D', 'E')

        assertEquals('A', WheelGenerator.firstLetter(own, ('A'..'Z').toSet()))
        assertEquals('D', WheelGenerator.firstLetter(own, ('A'..'Z').toSet(), GameConfig(minCategoryLetters = 2)))
    }

    @Test
    fun `the first letter of the mixed wheel and of an empty category`() {
        assertEquals('A', firstLetterOf(triplePool, CategoryId.MIXED))
        assertNull(firstLetterOf(triplePool, falseFriends))
        assertNull(generate(pool = triplePool, category = falseFriends).openingLetter())
    }

    private companion object {
        const val DAY = 86_400_000L
        const val NOW = 1_000 * DAY
    }
}
