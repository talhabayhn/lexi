package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Card rotation across games (`GAME_RULES.md` §1): never-seen cards first, then the least recently seen, so a letter's
 * cards are all used before any repeats. Games are simulated by recording every wheel card as seen at the game's time.
 */
class WheelRotationTest {
    private val config = GameConfig()

    /** A letter-index suffix made of letters only (answers are A–Z words). */
    private fun suffix(i: Int) = "${'A' + i / 26}${'A' + i % 26}"

    /** Per letter: [starts] STARTS cards and [contains] CONTAINS cards. */
    private fun letterCards(letter: Char, starts: Int, contains: Int, category: String = "food-drink"): List<Card> {
        val prefix = if (letter == 'Q') 'A' else 'Q'
        return List(starts) { card("${letter}S${suffix(it)}", category = category) } +
            List(contains) { card("${prefix}C${suffix(it)}$letter", letter = letter, category = category) }
    }

    /** Random pools: every letter gets 1–30 cards (thin letters included), some letters mixing in CONTAINS cards. */
    private fun randomPool(seed: Long): List<Card> {
        val random = SeededRandom(seed).derive("pool")
        return ('A'..'Z').flatMap { letter ->
            val size = 1 + random.nextInt(30)
            val contains = if (random.nextInt(3) == 0) random.nextInt(size) else 0
            letterCards(letter, size - contains, contains)
        }
    }

    private fun simulate(
        pool: List<Card>,
        games: Int,
        stepMs: Long,
        seed: Long = 1L,
        category: CategoryId = CategoryId.MIXED,
        mode: GameMode = GameMode.CLASSIC,
    ): List<Wheel> {
        val lastSeen = mutableMapOf<CardId, Long>()
        var now = START
        return List(games) { game ->
            val random = SeededRandom(seed * 1_000 + game).derive(WheelGenerator.SEED_PURPOSE)
            val wheel = WheelGenerator.generate(
                pool,
                Level.A1,
                category,
                mode,
                random,
                CardHistory(lastSeen.toMap(), now),
                config,
            )
            wheel.playableSlots.forEach { lastSeen[it.card!!.id] = now }
            now += stepMs
            wheel
        }
    }

    private fun picksOf(wheels: List<Wheel>, letter: Char): List<Card> =
        wheels.mapNotNull { wheel -> wheel.slots.single { it.letter == letter }.card }

    @Test
    fun `property - every card of a letter is used before any card repeats`() {
        for (seed in 1L..30L) {
            val pool = randomPool(seed)
            val wheels = simulate(pool, games = 35, stepMs = HOUR, seed = seed)
            for (letter in 'A'..'Z') {
                val cards = pool.filter { it.letter == letter }.toSet()
                val firstCycle = picksOf(wheels, letter).take(cards.size)
                assertEquals(cards, firstCycle.toSet(), "seed $seed letter $letter")
            }
        }
    }

    @Test
    fun `property - starts cards keep rotating with repeats at least pool size minus candidates apart`() {
        for (seed in 1L..20L) {
            val pool = randomPool(seed)
            // Days apart: older cards leave the 7-day window, so the window filter and the ordering mix.
            for (step in listOf(HOUR, 2 * DAY, 8 * DAY)) {
                val wheels = simulate(pool, games = 80, stepMs = step, seed = seed)
                for (letter in 'A'..'Z') {
                    val starts = pool.filter { it.letter == letter && it.rule == LetterRule.STARTS }
                    if (starts.size < 2 || starts.size != pool.count { it.letter == letter }) continue
                    val picks = picksOf(wheels, letter)
                    assertEquals(starts.toSet(), picks.take(starts.size).toSet(), "seed $seed $letter step $step")
                    val minGap = starts.size - minOf(config.leastRecentlySeenCandidates, starts.size - 1)
                    picks.withIndex().groupBy({ it.value }, { it.index }).values.forEach { indices ->
                        indices.zipWithNext { a, b ->
                            assertTrue(b - a > minGap, "seed $seed $letter step $step gap ${b - a}")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `property - a wheel never shows a card or a word twice`() {
        for (seed in 1L..30L) {
            simulate(randomPool(seed), games = 20, stepMs = HOUR, seed = seed).forEach { wheel ->
                val cards = wheel.playableSlots.map { it.card!! }
                assertEquals(cards.size, cards.distinctBy { it.id }.size, "seed $seed")
                assertEquals(cards.size, cards.distinctBy { it.answer }.size, "seed $seed")
            }
        }
    }

    @Test
    fun `a pack wheel does not repeat a word that exists in two levels`() {
        // EXIT is both the E card (A1) and an X card (B1): only one of them may be on the wheel.
        val pool = listOf(card("EXIT"), card("EXIT", letter = 'X', level = Level.B1), card("BOX", letter = 'X'))

        for (seed in 1L..20L) {
            val wheel = WheelGenerator.pack(pool, SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE))
            assertEquals(listOf("EXIT", "BOX"), wheel.playableSlots.map { it.card!!.answer }, "seed $seed")
        }
    }

    @Test
    fun `property - same seed and same history give the same wheel whatever the map order`() {
        for (seed in 1L..20L) {
            val pool = randomPool(seed)
            val random = SeededRandom(seed).derive("history")
            val entries = pool.filter { random.nextInt(2) == 0 }.map { it.id to START - random.nextInt(20) * DAY }
            fun wheel(history: Map<CardId, Long>) = WheelGenerator.generate(
                SeededRandom(seed).shuffled(pool),
                Level.A1,
                CategoryId.MIXED,
                GameMode.CLASSIC,
                SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
                CardHistory(history, START),
            )

            assertEquals(wheel(entries.toMap()), wheel(entries.reversed().toMap()), "seed $seed")
        }
    }

    @Test
    fun `wheels still vary between seeds once every card was seen`() {
        val pool = ('A'..'Z').flatMap { letterCards(it, starts = 10, contains = 0) }
        val history = CardHistory(pool.withIndex().associate { (i, card) -> card.id to START - DAY * 30 - i }, START)

        val wheels = (1L..10L).map {
            WheelGenerator.generate(
                pool,
                Level.A1,
                CategoryId.MIXED,
                GameMode.CLASSIC,
                SeededRandom(it).derive(WheelGenerator.SEED_PURPOSE),
                history,
            )
        }

        assertTrue(wheels.toSet().size > 1)
    }

    @Test
    fun `a never-seen card beats a card seen long ago`() {
        val pool = letterCards('A', starts = 2, contains = 0)
        val history = CardHistory(mapOf(pool[0].id to START - 365 * DAY), START)

        for (seed in 1L..20L) {
            val wheel = WheelGenerator.generate(
                pool,
                Level.A1,
                CategoryId.MIXED,
                GameMode.CLASSIC,
                SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
                history,
            )
            assertEquals(pool[1], wheel.slots.first().card, "seed $seed")
        }
    }

    @Test
    fun `a one-card letter repeats every game and a two-card letter alternates`() {
        val pool = letterCards('A', starts = 1, contains = 0) + letterCards('B', starts = 2, contains = 0)

        val wheels = simulate(pool, games = 10, stepMs = HOUR)

        assertTrue(picksOf(wheels, 'A').all { it == pool[0] })
        picksOf(wheels, 'B').zipWithNext { a, b -> assertNotEquals(a, b) }
    }

    @Test
    fun `a small category pool rotates its own cards only`() {
        val pool = letterCards('A', starts = 3, contains = 0, category = "animals") +
            letterCards('A', starts = 20, contains = 0, category = "home").map {
                it.copy(id = CardId("h-${it.id.value}"), answer = "${it.answer}H")
            }

        val picks = picksOf(simulate(pool, games = 9, stepMs = HOUR, category = CategoryId("animals")), 'A')

        assertTrue(picks.all { it.category == CategoryId("animals") })
        assertEquals(3, picks.take(3).toSet().size)
        picks.zipWithNext { a, b -> assertNotEquals(a, b) }
    }

    @Test
    fun `contains cards come only after the starts cards and rotate like them`() {
        val pool = letterCards('J', starts = 1, contains = 2)

        val picks = picksOf(simulate(pool, games = 4, stepMs = HOUR), 'J')

        // STARTS first; CONTAINS once the STARTS card was seen recently; then everything is recent, so the
        // STARTS rule applies to all cards again.
        assertEquals(LetterRule.STARTS, picks[0].rule)
        assertEquals(setOf(LetterRule.CONTAINS), picks.subList(1, 3).map { it.rule }.toSet())
        assertEquals(3, picks.take(3).toSet().size)
        assertEquals(LetterRule.STARTS, picks[3].rule)
    }

    @Test
    fun `history-free modes ignore the history`() {
        val pool = randomPool(7L)
        val history = CardHistory(pool.associate { it.id to START - DAY }, START)
        for (mode in GameMode.entries.filter { !it.usesCardHistory && it != GameMode.REVIEW }) {
            fun wheel(h: CardHistory) = WheelGenerator.generate(
                pool,
                Level.A1,
                CategoryId.MIXED,
                mode,
                SeededRandom(3L).derive(WheelGenerator.SEED_PURPOSE),
                h,
            )
            assertEquals(wheel(CardHistory.NONE), wheel(history), "$mode")
        }
    }

    private companion object {
        const val HOUR = 3_600_000L
        const val DAY = 24 * HOUR
        const val START = 1_000 * DAY
    }
}
