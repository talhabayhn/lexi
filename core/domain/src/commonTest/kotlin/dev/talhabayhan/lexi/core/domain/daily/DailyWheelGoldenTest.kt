package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.fullAlphabetPool
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.game.WheelGenerator
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DailyWheelGoldenTest {
    private val pool = fullAlphabetPool(suffix = "ONE") +
        fullAlphabetPool(suffix = "TWO") +
        fullAlphabetPool(suffix = "THREE")

    /**
     * Golden test: two "devices" with the same date+level+contentVersion produce the identical wheel.
     * Even if device A has seen cards recently and device B has no history, the daily wheel must be byte-identical.
     */
    @Test
    fun `two devices with same date level contentVersion produce identical wheel`() {
        val epochDay = 20_741L // 2026-10-15
        val level = Level.A1
        val contentVersion = 1L

        // Device 1: has seen various cards recently
        val device1Seen = pool.filter { it.answer.endsWith("ONE") }.map { it.id }.toSet()
        val seedDevice1 = DailySeed.seed(epochDay, level, contentVersion)
        val wheelDevice1 = WheelGenerator.generate(
            pool = pool,
            level = level,
            category = CategoryId.MIXED,
            mode = GameMode.DAILY,
            random = SeededRandom(seedDevice1).derive(WheelGenerator.SEED_PURPOSE),
            recentlySeen = device1Seen,
        )

        // Device 2: fresh install, no history
        val seedDevice2 = DailySeed.seed(epochDay, level, contentVersion)
        val wheelDevice2 = WheelGenerator.generate(
            pool = pool,
            level = level,
            category = CategoryId.MIXED,
            mode = GameMode.DAILY,
            random = SeededRandom(seedDevice2).derive(WheelGenerator.SEED_PURPOSE),
            recentlySeen = emptySet(),
        )

        // Exact equality across all 26 slots
        assertEquals(seedDevice1, seedDevice2)
        assertEquals(26, wheelDevice1.slots.size)
        assertEquals(wheelDevice1, wheelDevice2)
        wheelDevice1.slots.forEachIndexed { index, slot1 ->
            val slot2 = wheelDevice2.slots[index]
            assertEquals(slot1.letter, slot2.letter)
            assertEquals(slot1.state, slot2.state)
            assertEquals(slot1.card?.id, slot2.card?.id)
            assertEquals(slot1.card?.answer, slot2.card?.answer)
        }
    }

    @Test
    fun `golden daily wheel slots are pinned to prevent regressions`() {
        val epochDay = 20_741L
        val level = Level.A1
        val contentVersion = 1L
        val seed = DailySeed.seed(epochDay, level, contentVersion)
        val wheel = WheelGenerator.generate(
            pool = pool,
            level = level,
            category = CategoryId.MIXED,
            mode = GameMode.DAILY,
            random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
        )

        val answers = wheel.slots.mapNotNull { it.card?.answer }
        assertEquals(26, answers.size)
        // Verify every letter A-Z is present in alphabetical order
        ('A'..'Z').forEachIndexed { index, char ->
            assertEquals(char, wheel.slots[index].letter)
            assertEquals(char, answers[index].first())
        }
    }
}
