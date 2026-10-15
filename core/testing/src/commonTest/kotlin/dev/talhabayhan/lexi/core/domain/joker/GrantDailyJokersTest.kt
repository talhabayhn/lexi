package dev.talhabayhan.lexi.core.domain.joker

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.joker.JokerGrantReason.DAILY_GIFT
import dev.talhabayhan.lexi.core.domain.joker.JokerGrantReason.STREAK_BONUS
import dev.talhabayhan.lexi.core.testing.FakeAppClock
import dev.talhabayhan.lexi.core.testing.FakeAppClock.Companion.MS_PER_DAY
import dev.talhabayhan.lexi.core.testing.FakeJokerRepository
import dev.talhabayhan.lexi.core.testing.FakeStreakRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Lives in core:testing (not core:domain) because it needs the repository fakes (see FinishGameTest).
/** GAME_RULES.md §7: daily joker gift on the first app open of a local day, streak bonus, cap. */
class GrantDailyJokersTest {
    private val config = GameConfig()
    private val clock = FakeAppClock()
    private val today = clock.epochDay(clock.nowMs())
    private val jokers = FakeJokerRepository()
    private val streaks = FakeStreakRepository()
    private val grantDailyJokers = GrantDailyJokers(jokers, streaks, clock, config)

    private fun inventory(vararg counts: Int) = JokerInventory(JokerType.entries.zip(counts.toList()).toMap())

    private val total: Int get() = JokerType.entries.sumOf { jokers.inventory.value!!.count(it) }

    @Test
    fun `first app open grants the start pack and one daily joker`() = runTest {
        val grants = grantDailyJokers()

        assertEquals(listOf(DAILY_GIFT), grants.map { it.reason })
        assertEquals(config.jokerStartPack.plus(grants.single().type, 1), jokers.inventory.value)
        assertEquals(today, jokers.wallet.lastDailyGiftDay)
    }

    @Test
    fun `second open on the same day grants nothing`() = runTest {
        grantDailyJokers()
        val before = jokers.inventory.value
        clock.advanceBy(MS_PER_DAY / 3)

        assertEquals(emptyList(), grantDailyJokers())
        assertEquals(before, jokers.inventory.value)
    }

    @Test
    fun `next day grants one more`() = runTest {
        grantDailyJokers()
        val before = total
        clock.advanceBy(MS_PER_DAY)

        assertEquals(listOf(DAILY_GIFT), grantDailyJokers().map { it.reason })
        assertEquals(before + 1, total)
        assertEquals(today + 1, jokers.wallet.lastDailyGiftDay)
    }

    @Test
    fun `the local day follows the player's time zone`() = runTest {
        // 23:30 UTC is already the next day at UTC+2.
        val lateEvening = (today + 1) * MS_PER_DAY - MS_PER_DAY / 48
        val zoned = FakeAppClock(currentMs = lateEvening, utcOffsetMs = 7_200_000L)
        GrantDailyJokers(jokers, streaks, zoned, config)()

        assertEquals(today + 1, jokers.wallet.lastDailyGiftDay)
    }

    @Test
    fun `the cap is respected`() = runTest {
        jokers.save(inventory(5, 5, 2, 5))

        assertEquals(listOf(JokerGrant(JokerType.EXAMPLE, DAILY_GIFT)), grantDailyJokers())
        assertEquals(inventory(5, 5, 3, 5), jokers.inventory.value)
    }

    @Test
    fun `with every type at the cap the day is used up without a gift`() = runTest {
        jokers.save(inventory(5, 5, 5, 5))

        assertEquals(emptyList(), grantDailyJokers())
        assertEquals(today, jokers.wallet.lastDailyGiftDay)

        jokers.save(inventory(0, 5, 5, 5))
        assertEquals(emptyList(), grantDailyJokers(), "using a joker later that day doesn't reopen the gift")
    }

    @Test
    fun `moving the clock back neither grants twice nor farms`() = runTest {
        grantDailyJokers()
        clock.advanceBy(2 * MS_PER_DAY)
        grantDailyJokers()
        val before = jokers.inventory.value

        clock.advanceBy(-MS_PER_DAY)
        assertEquals(emptyList(), grantDailyJokers(), "yesterday after today's gift")
        clock.advanceBy(-MS_PER_DAY)
        assertEquals(emptyList(), grantDailyJokers(), "a day already gifted")
        assertEquals(before, jokers.inventory.value)
        assertEquals(today + 2, jokers.wallet.lastDailyGiftDay)

        clock.advanceBy(3 * MS_PER_DAY)
        assertEquals(listOf(DAILY_GIFT), grantDailyJokers().map { it.reason }, "a new day after the last gift")
    }

    @Test
    fun `a 7-day streak gives a bonus once and a 14-day streak another`() = runTest {
        grantDailyJokers()
        streaks.days = (today - 6..today).toList()

        assertEquals(listOf(STREAK_BONUS), grantDailyJokers().map { it.reason })
        assertEquals(emptyList(), grantDailyJokers())

        // Days 8..14: the app is opened (daily gift only), then the day's wheel is completed.
        repeat(7) { index ->
            clock.advanceBy(MS_PER_DAY)
            jokers.save(JokerInventory())
            assertEquals(listOf(DAILY_GIFT), grantDailyJokers().map { it.reason }, "day ${index + 8}")
            streaks.days = streaks.days + (today + index + 1)
        }
        // Day 14 was played after the open: the bonus comes with the next open.
        clock.advanceBy(MS_PER_DAY)
        jokers.save(JokerInventory())
        assertEquals(listOf(DAILY_GIFT, STREAK_BONUS), grantDailyJokers().map { it.reason })
        assertEquals(today + 7, jokers.wallet.lastStreakBonusDay)
    }

    @Test
    fun `the picked type is the same for the same day`() = runTest {
        val first = grantDailyJokers()

        val other = FakeJokerRepository()
        val second = GrantDailyJokers(other, FakeStreakRepository(), FakeAppClock(), config)()

        assertEquals(first, second)
    }
}
