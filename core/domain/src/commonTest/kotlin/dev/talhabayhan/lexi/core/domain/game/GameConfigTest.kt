package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Defaults must match the ⚙ values in GAME_RULES.md exactly. */
class GameConfigTest {
    private val config = GameConfig()

    @Test
    fun `wrong answer is revealed for 1200 ms`() {
        assertEquals(1_200L, config.revealMs)
    }

    @Test
    fun `classic lasts 180 s and offers 120 180 240 300 s in settings`() {
        assertEquals(180_000L, config.timeLimitMs(GameMode.CLASSIC))
        assertEquals(listOf(120, 180, 240, 300), config.classicTimeLimitOptionsSeconds)
    }

    @Test
    fun `classic time limit follows the settings override`() {
        assertEquals(240_000L, config.copy(classicTimeLimitSeconds = 240).timeLimitMs(GameMode.CLASSIC))
    }

    @Test
    fun `hardcore and daily last 180 s and level test 120 s`() {
        assertEquals(180_000L, config.timeLimitMs(GameMode.HARDCORE))
        assertEquals(180_000L, config.timeLimitMs(GameMode.DAILY))
        assertEquals(120_000L, config.timeLimitMs(GameMode.LEVEL_TEST))
    }

    @Test
    fun `duel gives each player a 150 s clock`() {
        assertEquals(150_000L, config.timeLimitMs(GameMode.DUEL))
    }

    @Test
    fun `practice and review have no timer`() {
        assertNull(config.timeLimitMs(GameMode.PRACTICE))
        assertNull(config.timeLimitMs(GameMode.REVIEW))
    }

    @Test
    fun `time warning starts at 10 seconds left`() {
        assertEquals(10, config.timeWarningSeconds)
    }

    @Test
    fun `correct answer base is 10 points with level multipliers A1 1_0 to NAT 1_7`() {
        assertEquals(10, config.baseCorrectPoints)
        val expected = mapOf(
            Level.A1 to 100,
            Level.A2 to 110,
            Level.B1 to 120,
            Level.B2 to 130,
            Level.C1 to 150,
            Level.NAT to 170,
        )
        assertEquals(expected, Level.entries.associateWith(config::levelMultiplierPercent))
    }

    @Test
    fun `streak bonus is 2 per streak step capped at 10 with combos at 3 5 10`() {
        assertEquals(2, config.streakBonusStep)
        assertEquals(10, config.streakBonusCap)
        assertEquals(listOf(3, 5, 10), config.comboMilestones)
    }

    @Test
    fun `time bonus is 1 point per remaining second and hardcore multiplies by 1_5`() {
        assertEquals(1, config.timeBonusPointsPerSecond)
        assertEquals(150, config.hardcoreMultiplierPercent)
    }

    @Test
    fun `reveal letter costs 10 s or 5 points in untimed modes`() {
        assertEquals(JokerCost.Time(10_000L), config.jokerCost(JokerType.REVEAL_LETTER, GameMode.CLASSIC))
        assertEquals(JokerCost.Points(5), config.jokerCost(JokerType.REVEAL_LETTER, GameMode.PRACTICE))
    }

    @Test
    fun `remove distractors costs 15 s and is free in untimed modes`() {
        assertEquals(JokerCost.Time(15_000L), config.jokerCost(JokerType.REMOVE_DISTRACTORS, GameMode.HARDCORE))
        assertEquals(JokerCost.Free, config.jokerCost(JokerType.REMOVE_DISTRACTORS, GameMode.REVIEW))
    }

    @Test
    fun `remove distractors cost in untimed modes is one config value`() {
        val config = config.copy(removeDistractorsUntimedCostPoints = 7)

        assertEquals(JokerCost.Points(7), config.jokerCost(JokerType.REMOVE_DISTRACTORS, GameMode.PRACTICE))
    }

    @Test
    fun `level test joker ban is one config value`() {
        val config = config.copy(jokerFreeModes = setOf(GameMode.DAILY, GameMode.DUEL))

        assertTrue(config.isJokerAllowed(JokerType.EXAMPLE, GameMode.LEVEL_TEST, Level.A1))
    }

    @Test
    fun `recently seen window is 7 days`() {
        assertEquals(7, config.recentlySeenWindowDays)
    }

    @Test
    fun `example costs 5 points and listen costs 10 points`() {
        assertEquals(JokerCost.Points(5), config.jokerCost(JokerType.EXAMPLE, GameMode.CLASSIC))
        assertEquals(JokerCost.Points(10), config.jokerCost(JokerType.LISTEN, GameMode.CLASSIC))
    }

    @Test
    fun `jokers are not allowed in daily duel and level test`() {
        for (mode in listOf(GameMode.DAILY, GameMode.DUEL, GameMode.LEVEL_TEST)) {
            assertFalse(config.isJokerAllowed(JokerType.REVEAL_LETTER, mode, Level.A1), "$mode")
        }
        for (mode in listOf(GameMode.CLASSIC, GameMode.PRACTICE, GameMode.HARDCORE, GameMode.REVIEW)) {
            assertTrue(config.isJokerAllowed(JokerType.REVEAL_LETTER, mode, Level.A1), "$mode")
        }
    }

    @Test
    fun `listen joker is disabled at C1 and NAT`() {
        assertTrue(config.isJokerAllowed(JokerType.LISTEN, GameMode.CLASSIC, Level.B2))
        assertFalse(config.isJokerAllowed(JokerType.LISTEN, GameMode.CLASSIC, Level.C1))
        assertFalse(config.isJokerAllowed(JokerType.LISTEN, GameMode.CLASSIC, Level.NAT))
        assertTrue(config.isJokerAllowed(JokerType.EXAMPLE, GameMode.CLASSIC, Level.NAT))
    }

    @Test
    fun `joker start pack is 2 reveal 1 remove 3 example 1 listen`() {
        val expected = JokerInventory(
            mapOf(
                JokerType.REVEAL_LETTER to 2,
                JokerType.REMOVE_DISTRACTORS to 1,
                JokerType.EXAMPLE to 3,
                JokerType.LISTEN to 1,
            ),
        )
        assertEquals(expected, config.jokerStartPack)
        assertEquals(1, config.dailyLoginJokerBonus)
        assertEquals(1, config.rewardedAdJokerReward)
        assertEquals(3, config.proDailyJokerRefill)
    }

    @Test
    fun `config survives a JSON round trip`() {
        val json = Json.encodeToString(GameConfig.serializer(), config)

        assertEquals(config, Json.decodeFromString(GameConfig.serializer(), json))
    }
}
