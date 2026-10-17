package dev.talhabayhan.lexi.core.domain.daily

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.fullAlphabetPool
import dev.talhabayhan.lexi.core.domain.game.EngineEffect
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.KeyState
import dev.talhabayhan.lexi.core.domain.game.Slot
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.game.WheelGenerator
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DailyWheelGoldenTest {
    private val pool = Level.entries.flatMap { level ->
        fullAlphabetPool(level = level, suffix = "ONE") +
            fullAlphabetPool(level = level, suffix = "TWO")
    }

    /**
     * Golden test: two "devices" with the same date+contentVersion produce the identical wheel.
     * Even if device A has seen cards recently and device B has no history, the daily wheel must be byte-identical,
     * including identical level assignments and card picks.
     */
    @Test
    fun `two devices with same date contentVersion produce identical wheel`() {
        val epochDay = 20_741L // 2026-10-15
        val contentVersion = 1L

        val seedDevice1 = DailySeed.seed(epochDay, contentVersion)
        val wheelDevice1 = WheelGenerator.daily(
            pool = pool,
            random = SeededRandom(seedDevice1).derive(WheelGenerator.SEED_PURPOSE),
        )

        val seedDevice2 = DailySeed.seed(epochDay, contentVersion)
        val wheelDevice2 = WheelGenerator.daily(
            pool = pool,
            random = SeededRandom(seedDevice2).derive(WheelGenerator.SEED_PURPOSE),
        )

        assertEquals(seedDevice1, seedDevice2)
        assertEquals(26, wheelDevice1.slots.size)
        assertEquals(wheelDevice1, wheelDevice2)
        wheelDevice1.slots.forEachIndexed { index, slot1 ->
            val slot2 = wheelDevice2.slots[index]
            assertEquals(slot1.letter, slot2.letter)
            assertEquals(slot1.state, slot2.state)
            assertEquals(slot1.card?.id, slot2.card?.id)
            assertEquals(slot1.card?.level, slot2.card?.level)
            assertEquals(slot1.card?.answer, slot2.card?.answer)
        }
    }

    @Test
    fun `golden pinned 26 card ids and level assignment for 2 fixed dates`() {
        val contentVersion = 1L

        // Date 1: 2026-10-15 (epochDay = 20_741L)
        val s1 = DailySeed.seed(20_741L, contentVersion)
        assertEquals(6034053112182483930L, s1)
        val w1 = WheelGenerator.daily(pool, SeededRandom(s1).derive(WheelGenerator.SEED_PURPOSE))
        val expectedLevels1 = listOf(
            Level.A2, Level.B1, Level.B2, Level.A1, Level.C1, Level.NAT,
            Level.A2, Level.A1, Level.A2, Level.C1, Level.A2, Level.B1,
            Level.A1, Level.C1, Level.NAT, Level.B1, Level.A1, Level.A2,
            Level.NAT, Level.B2, Level.B1, Level.B2, Level.B1, Level.A1,
            Level.A1, Level.B2,
        )
        val expectedCards1 = listOf(
            "a2-atwo", "b1-btwo", "b2-ctwo", "a1-dtwo", "c1-etwo", "nat-fone",
            "a2-gtwo", "a1-hone", "a2-ione", "c1-jone", "a2-kone", "b1-ltwo",
            "a1-mtwo", "c1-ntwo", "nat-oone", "b1-ptwo", "a1-qtwo", "a2-rtwo",
            "nat-sone", "b2-tone", "b1-utwo", "b2-vone", "b1-wtwo", "a1-xtwo",
            "a1-ytwo", "b2-zone",
        )
        assertEquals(expectedLevels1, w1.slots.map { it.card?.level })
        assertEquals(expectedCards1, w1.slots.map { it.card?.id?.value })

        // Date 2: 2026-10-16 (epochDay = 20_742L)
        val s2 = DailySeed.seed(20_742L, contentVersion)
        assertEquals(5975572613751024269L, s2)
        val w2 = WheelGenerator.daily(pool, SeededRandom(s2).derive(WheelGenerator.SEED_PURPOSE))
        val expectedLevels2 = listOf(
            Level.A1, Level.C1, Level.B1, Level.B1, Level.A2, Level.B2,
            Level.B2, Level.NAT, Level.B1, Level.NAT, Level.A1, Level.A2,
            Level.B2, Level.A2, Level.NAT, Level.B1, Level.A1, Level.C1,
            Level.C1, Level.A2, Level.A1, Level.B2, Level.A2, Level.A1,
            Level.B1, Level.A1,
        )
        val expectedCards2 = listOf(
            "a1-atwo", "c1-btwo", "b1-cone", "b1-done", "a2-eone", "b2-ftwo",
            "b2-gtwo", "nat-hone", "b1-itwo", "nat-jone", "a1-ktwo", "a2-lone",
            "b2-mtwo", "a2-none", "nat-oone", "b1-pone", "a1-qone", "c1-rone",
            "c1-stwo", "a2-ttwo", "a1-utwo", "b2-vtwo", "a2-wone", "a1-xone",
            "b1-ytwo", "a1-ztwo",
        )
        assertEquals(expectedLevels2, w2.slots.map { it.card?.level })
        assertEquals(expectedCards2, w2.slots.map { it.card?.id?.value })
    }

    @Test
    fun `distribution counts exactly 6 5 5 4 3 3`() {
        val epochDay = 20_741L
        val contentVersion = 1L
        val seed = DailySeed.seed(epochDay, contentVersion)
        val wheel = WheelGenerator.daily(
            pool = pool,
            random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
        )

        val levelCounts = wheel.slots.mapNotNull { it.card?.level }.groupingBy { it }.eachCount()
        val expected = mapOf(
            Level.A1 to 6,
            Level.A2 to 5,
            Level.B1 to 5,
            Level.B2 to 4,
            Level.C1 to 3,
            Level.NAT to 3,
        )
        assertEquals(expected, levelCounts)
        assertEquals(26, wheel.slots.size)
    }

    @Test
    fun `every slot card matches its letter rule`() {
        val testPool = pool + listOf(
            card(answer = "BOX", letter = 'X', rule = LetterRule.CONTAINS, level = Level.A1, id = "a1-box"),
            card(answer = "QUIZ", letter = 'Q', rule = LetterRule.STARTS, level = Level.B1, id = "b1-quiz"),
        )
        val seed = DailySeed.seed(20_741L, 1L)
        val wheel = WheelGenerator.daily(
            pool = testPool,
            random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
        )

        wheel.slots.forEach { slot ->
            val card = checkNotNull(slot.card)
            assertEquals(slot.letter, card.letter)
            assertTrue(card.rule.isSatisfiedBy(card.answer, slot.letter))
        }
    }

    @Test
    fun `different dates give different wheels`() {
        val contentVersion = 1L
        val seedDay1 = DailySeed.seed(20_741L, contentVersion)
        val wheelDay1 = WheelGenerator.daily(
            pool = pool,
            random = SeededRandom(seedDay1).derive(WheelGenerator.SEED_PURPOSE),
        )

        val seedDay2 = DailySeed.seed(20_742L, contentVersion)
        val wheelDay2 = WheelGenerator.daily(
            pool = pool,
            random = SeededRandom(seedDay2).derive(WheelGenerator.SEED_PURPOSE),
        )

        assertNotEquals(seedDay1, seedDay2)
        assertNotEquals(wheelDay1, wheelDay2)
        val levels1 = wheelDay1.slots.map { it.card?.level }
        val levels2 = wheelDay2.slots.map { it.card?.level }
        assertNotEquals(levels1, levels2)
    }

    @Test
    fun `scoring per level in daily mode`() {
        val config = GameConfig()
        val engine = GameEngine(config)
        val expectedBasePoints = mapOf(
            Level.A1 to 5,
            Level.A2 to 7,
            Level.B1 to 10,
            Level.B2 to 13,
            Level.C1 to 16,
            Level.NAT to 20,
        )

        expectedBasePoints.forEach { (level, expectedPoints) ->
            val card = card(answer = "WORD", letter = 'W', level = level)
            val wheel = Wheel(listOf(Slot('W', card, SlotState.TODO)))
            var state = engine.newGame(seed = 42L, mode = GameMode.DAILY, level = level, wheel = wheel)
            state = engine.reduce(state, GameEvent.Start(1_000L)).state
            val input = checkNotNull(state.currentInput)
            "WORD".forEach { char ->
                val keyIndex = input.keys.indices.first {
                    input.keys[it].letter == char && input.keys[it].state == KeyState.AVAILABLE
                }
                state = engine.reduce(state, GameEvent.KeyTapped(keyIndex)).state
            }
            assertEquals(expectedPoints, state.score, "Incorrect score for level $level")
        }
    }

    @Test
    fun `pass without penalty in daily mode`() {
        val config = GameConfig()
        val engine = GameEngine(config)
        val cardA = card(answer = "APPLE", letter = 'A', level = Level.A1)
        val cardB = card(answer = "BREAD", letter = 'B', level = Level.B1)
        val wheel = Wheel(listOf(Slot('A', cardA, SlotState.TODO), Slot('B', cardB, SlotState.TODO)))

        var state = engine.newGame(seed = 42L, mode = GameMode.DAILY, level = Level.A1, wheel = wheel)
        state = engine.reduce(state, GameEvent.Start(1_000L)).state

        // 1. Pass slot A: score remains 0, streak resets to 0, Passed effect emitted
        val passTransition = engine.reduce(state, GameEvent.Pass)
        state = passTransition.state
        assertEquals(0, state.score)
        assertEquals(0, state.streak)
        assertTrue(passTransition.effects.any { it is EngineEffect.Passed && it.letter == 'A' })

        // 2. Answer slot B correctly: base points for B1 is 10, streak becomes 1 (bonus 0) -> score = 10
        "BREAD".forEach { char ->
            val input = checkNotNull(state.currentInput)
            val keyIndex = input.keys.indices.first {
                input.keys[it].letter == char && input.keys[it].state == KeyState.AVAILABLE
            }
            state = engine.reduce(state, GameEvent.KeyTapped(keyIndex)).state
        }
        assertEquals(10, state.score)
        assertEquals(1, state.streak)

        // 3. Lap 2: Slot A comes back (passed-then-correct). Answer correctly: base points for A1 is 5.
        // Streak was 1, now becomes 2. Streak bonus = 2 * (2 - 1) = 2. Points = 5 + 2 = 7.
        // Total score = 10 + 7 = 17.
        "APPLE".forEach { char ->
            val input = checkNotNull(state.currentInput)
            val keyIndex = input.keys.indices.first {
                input.keys[it].letter == char && input.keys[it].state == KeyState.AVAILABLE
            }
            state = engine.reduce(state, GameEvent.KeyTapped(keyIndex)).state
        }
        assertEquals(17, state.score)
    }
}
