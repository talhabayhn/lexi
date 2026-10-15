package dev.talhabayhan.lexi.core.domain.joker

import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.joker.JokerGrantReason.DAILY_GIFT
import dev.talhabayhan.lexi.core.domain.joker.JokerGrantReason.STREAK_BONUS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** GAME_RULES.md §7 (daily joker gift and streak bonus). */
class JokerGiftsTest {
    private val config = JokerGiftConfig()
    private val today = 20_741L
    private val startPack = JokerInventory(
        mapOf(
            JokerType.REVEAL_LETTER to 2,
            JokerType.REMOVE_DISTRACTORS to 1,
            JokerType.EXAMPLE to 3,
            JokerType.LISTEN to 1,
        ),
    )

    private fun inventory(vararg counts: Int) = JokerInventory(JokerType.entries.zip(counts.toList()).toMap())

    private fun grant(
        wallet: JokerWallet,
        day: Long = today,
        streakDays: Collection<Long> = emptyList(),
        giftConfig: JokerGiftConfig = config,
    ) = JokerGifts.grant(wallet, day, streakDays, giftConfig)

    @Test
    fun `gift defaults are 1 per day with cap 5 and 1 every 7 streak days`() {
        assertEquals(JokerGiftConfig(dailyGift = 1, cap = 5, streakBonusEvery = 7, streakBonus = 1), config)
    }

    @Test
    fun `first open of a day gives one joker and marks the day`() {
        val outcome = grant(JokerWallet(startPack))

        assertEquals(1, outcome.grants.size)
        assertEquals(DAILY_GIFT, outcome.grants.single().reason)
        val type = outcome.grants.single().type
        assertEquals(startPack.plus(type, 1), outcome.wallet.inventory)
        assertEquals(today, outcome.wallet.lastDailyGiftDay)
    }

    @Test
    fun `a day already gifted gives nothing`() {
        val wallet = JokerWallet(startPack, lastDailyGiftDay = today)

        val outcome = grant(wallet)

        assertEquals(emptyList(), outcome.grants)
        assertEquals(wallet, outcome.wallet)
    }

    @Test
    fun `the next day gives again`() {
        val outcome = grant(JokerWallet(startPack, lastDailyGiftDay = today - 1))

        assertEquals(1, outcome.grants.size)
        assertEquals(today, outcome.wallet.lastDailyGiftDay)
    }

    @Test
    fun `a clock moved back before the last gift gives nothing and keeps the last day`() {
        val wallet = JokerWallet(startPack, lastDailyGiftDay = today + 3)

        val outcome = grant(wallet)

        assertEquals(emptyList(), outcome.grants)
        assertEquals(wallet, outcome.wallet)
    }

    @Test
    fun `the gift only picks types below the cap`() {
        val wallet = JokerWallet(inventory(5, 5, 4, 7))

        val outcome = grant(wallet)

        assertEquals(listOf(JokerGrant(JokerType.EXAMPLE, DAILY_GIFT)), outcome.grants)
        assertEquals(inventory(5, 5, 5, 7), outcome.wallet.inventory)
    }

    @Test
    fun `with every type at the cap there is no gift but the day is used up`() {
        val wallet = JokerWallet(inventory(5, 5, 5, 6), lastDailyGiftDay = today - 1)

        val outcome = grant(wallet)

        assertEquals(emptyList(), outcome.grants)
        assertEquals(wallet.inventory, outcome.wallet.inventory)
        assertEquals(today, outcome.wallet.lastDailyGiftDay)
    }

    @Test
    fun `a gift of several jokers re-checks the cap after each pick`() {
        val outcome = grant(JokerWallet(inventory(4, 5, 5, 5)), giftConfig = config.copy(dailyGift = 3))

        assertEquals(listOf(JokerGrant(JokerType.REVEAL_LETTER, DAILY_GIFT)), outcome.grants)
        assertEquals(inventory(5, 5, 5, 5), outcome.wallet.inventory)
    }

    @Test
    fun `the picked type depends only on the day`() {
        val wallet = JokerWallet(JokerInventory())

        val picks = (0L until 40L).map { offset -> grant(wallet, day = today + offset).grants.single().type }

        assertEquals(picks, (0L until 40L).map { offset -> grant(wallet, day = today + offset).grants.single().type })
        assertEquals(JokerType.entries.toSet(), picks.toSet(), "every type is picked on some day")
    }

    @Test
    fun `golden daily gift picks`() {
        // Pinned on purpose (GAME_RULES.md §13, purpose joker-gift:daily): changing the RNG use changes these.
        val wallet = JokerWallet(JokerInventory())
        val picks = (0L until 6L).map { grant(wallet, day = today + it).grants.single().type }

        assertEquals(GOLDEN_DAILY_PICKS, picks)
    }

    // Streak bonus

    @Test
    fun `completing a 7-day streak gives a bonus joker once`() {
        val streak = (today - 6..today).toList()
        val wallet = JokerWallet(startPack, lastDailyGiftDay = today)

        val first = grant(wallet, streakDays = streak)

        assertEquals(listOf(STREAK_BONUS), first.grants.map { it.reason })
        assertEquals(today, first.wallet.lastStreakBonusDay)
        assertEquals(startPack.plus(first.grants.single().type, 1), first.wallet.inventory)
        assertEquals(emptyList(), grant(first.wallet, streakDays = streak).grants)
        val nextDay = grant(first.wallet, day = today + 1, streakDays = streak)
        assertEquals(listOf(DAILY_GIFT), nextDay.grants.map { it.reason }, "the run ending yesterday was rewarded")
    }

    @Test
    fun `a streak that ended yesterday still counts for the bonus`() {
        val streak = (today - 7..today - 1).toList()

        val outcome = grant(JokerWallet(startPack, lastDailyGiftDay = today), streakDays = streak)

        assertEquals(listOf(STREAK_BONUS), outcome.grants.map { it.reason })
        assertEquals(today - 1, outcome.wallet.lastStreakBonusDay)
    }

    @Test
    fun `a 14-day streak gives a second bonus`() {
        val afterSeven = JokerWallet(startPack, lastDailyGiftDay = today, lastStreakBonusDay = today - 7)

        val streakOf13 = (today - 13..today - 1).toList()
        assertEquals(emptyList(), grant(afterSeven, streakDays = streakOf13).grants)

        val streakOf14 = (today - 13..today).toList()
        val outcome = grant(afterSeven, streakDays = streakOf14)
        assertEquals(listOf(STREAK_BONUS), outcome.grants.map { it.reason })
        assertEquals(today, outcome.wallet.lastStreakBonusDay)
    }

    @Test
    fun `a streak shorter than 7 days gives no bonus`() {
        val outcome = grant(JokerWallet(startPack, lastDailyGiftDay = today), streakDays = (today - 5..today).toList())

        assertEquals(emptyList(), outcome.grants)
        assertNull(outcome.wallet.lastStreakBonusDay)
    }

    @Test
    fun `a new streak reaching 7 days after a break gives a bonus again`() {
        val oldMilestone = today - 20
        val wallet = JokerWallet(startPack, lastDailyGiftDay = today, lastStreakBonusDay = oldMilestone)
        val days = (oldMilestone - 6..oldMilestone + 2) + (today - 6..today)

        val outcome = grant(wallet, streakDays = days)

        assertEquals(listOf(STREAK_BONUS), outcome.grants.map { it.reason })
    }

    @Test
    fun `only the latest milestone of a long streak is rewarded`() {
        val outcome = grant(JokerWallet(startPack, lastDailyGiftDay = today), streakDays = (today - 15..today).toList())

        assertEquals(1, outcome.grants.size)
        assertEquals(today - 2, outcome.wallet.lastStreakBonusDay, "the 14th day of the 16-day run")
    }

    @Test
    fun `the streak bonus respects the cap and is used up at the cap`() {
        val wallet = JokerWallet(inventory(5, 5, 5, 5), lastDailyGiftDay = today)

        val outcome = grant(wallet, streakDays = (today - 6..today).toList())

        assertEquals(emptyList(), outcome.grants)
        assertEquals(today, outcome.wallet.lastStreakBonusDay)
    }

    @Test
    fun `daily gift comes before a streak bonus of the same open`() {
        val outcome = grant(JokerWallet(inventory(5, 5, 5, 3)), streakDays = (today - 6..today).toList())

        assertEquals(
            listOf(JokerGrant(JokerType.LISTEN, DAILY_GIFT), JokerGrant(JokerType.LISTEN, STREAK_BONUS)),
            outcome.grants,
        )
        assertEquals(inventory(5, 5, 5, 5), outcome.wallet.inventory)
    }

    @Test
    fun `a streak bonus every 0 days is off`() {
        val outcome = grant(
            JokerWallet(startPack, lastDailyGiftDay = today),
            streakDays = (today - 6..today).toList(),
            giftConfig = config.copy(streakBonusEvery = 0),
        )

        assertEquals(emptyList(), outcome.grants)
    }

    @Test
    fun `milestone day is the day the current run reached a multiple of the period`() {
        assertNull(JokerGifts.streakMilestoneDay(emptyList(), today, 7))
        assertEquals(today, JokerGifts.streakMilestoneDay((today - 6..today).toList(), today, 7))
        assertEquals(today - 3, JokerGifts.streakMilestoneDay((today - 9..today).toList(), today, 7))
        assertNull(JokerGifts.streakMilestoneDay((today - 9..today - 2).toList(), today, 7), "run ended 2 days ago")
        assertNull(JokerGifts.streakMilestoneDay((today - 6..today).toList(), today, 0))
    }

    @Test
    fun `negative gift values are rejected`() {
        assertFailsWith<IllegalArgumentException> { JokerGiftConfig(dailyGift = -1) }
        assertFailsWith<IllegalArgumentException> { JokerGiftConfig(cap = -1) }
        assertFailsWith<IllegalArgumentException> { JokerGiftConfig(streakBonusEvery = -1) }
        assertFailsWith<IllegalArgumentException> { JokerGiftConfig(streakBonus = -1) }
        assertTrue(JokerGiftConfig(dailyGift = 0).dailyGift == 0)
    }

    private companion object {
        /** Empty inventory, days 20 741..20 746. */
        val GOLDEN_DAILY_PICKS = listOf(
            JokerType.REMOVE_DISTRACTORS,
            JokerType.LISTEN,
            JokerType.LISTEN,
            JokerType.REMOVE_DISTRACTORS,
            JokerType.REMOVE_DISTRACTORS,
            JokerType.LISTEN,
        )
    }
}
