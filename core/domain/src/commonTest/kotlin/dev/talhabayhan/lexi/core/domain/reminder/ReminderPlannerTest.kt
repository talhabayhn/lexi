package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.COMEBACK_14D
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.COMEBACK_3D
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.COMEBACK_7D
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.DAILY_1
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.DAILY_2
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.DAILY_3
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.REVIEW_1
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.REVIEW_2
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.STREAK_1
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.STREAK_2
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant.STREAK_3
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ReminderPlannerTest {
    // 2026-10-15: T % 3 == 2 (DAILY_3), T is odd.
    private val t = 20_741L

    private fun input(
        enabled: Boolean = true,
        minuteOfDay: Int = 20 * 60,
        reviewReminders: Boolean = true,
        nowMinuteOfDay: Int = 12 * 60,
        playDays: Collection<Long> = listOf(t - 1),
        reviewDueDays: Collection<Long> = emptyList(),
        streakBonusEvery: Int = 7,
    ) = ReminderInput(
        enabled = enabled,
        minuteOfDay = minuteOfDay,
        reviewReminders = reviewReminders,
        today = t,
        nowMinuteOfDay = nowMinuteOfDay,
        playDays = playDays,
        reviewDueDays = reviewDueDays,
        streakBonusEvery = streakBonusEvery,
    )

    private fun List<PlannedReminder>.byDay() = associate { it.epochDay - t to it.variant }

    @Test
    fun `disabled reminder plans nothing`() {
        assertEquals(emptyList(), ReminderPlanner.plan(input(enabled = false)))
    }

    @Test
    fun `played today plans nothing today`() {
        val plan = ReminderPlanner.plan(input(playDays = listOf(t)))

        assertTrue(plan.none { it.epochDay == t })
        assertEquals(t + 1, plan.first().epochDay)
    }

    @Test
    fun `reminder time already passed today starts tomorrow`() {
        val plan = ReminderPlanner.plan(input(minuteOfDay = 9 * 60, nowMinuteOfDay = 9 * 60 + 1))

        assertEquals(t + 1, plan.first().epochDay)
    }

    @Test
    fun `reminder time equal to now counts as passed`() {
        val plan = ReminderPlanner.plan(input(minuteOfDay = 9 * 60, nowMinuteOfDay = 9 * 60))

        assertEquals(t + 1, plan.first().epochDay)
    }

    @Test
    fun `reminder time still ahead starts today at that minute`() {
        val plan = ReminderPlanner.plan(input(minuteOfDay = 21 * 60 + 30, nowMinuteOfDay = 21 * 60))

        assertEquals(t, plan.first().epochDay)
        assertTrue(plan.all { it.minuteOfDay == 21 * 60 + 30 })
    }

    @Test
    fun `comeback messages only at 3 7 and 14 days without play then stop`() {
        // Played yesterday (T-1), streak 1: T is 1 day without play, T+2 is 3 days.
        val plan = ReminderPlanner.plan(input(playDays = listOf(t - 1)))

        assertEquals(
            mapOf(0L to DAILY_3, 1L to DAILY_1, 2L to COMEBACK_3D, 6L to COMEBACK_7D, 13L to COMEBACK_14D),
            plan.byDay(),
        )
    }

    @Test
    fun `comeback series continues after a rebuild during the absence`() {
        // Last play T-5: T is 5 days without play; only the 7- and 14-day messages remain.
        val plan = ReminderPlanner.plan(input(playDays = listOf(t - 5)))

        assertEquals(mapOf(2L to COMEBACK_7D, 9L to COMEBACK_14D), plan.byDay())
    }

    @Test
    fun `nothing after the 14-day comeback message`() {
        assertEquals(emptyList(), ReminderPlanner.plan(input(playDays = listOf(t - 15))))
        assertEquals(emptyList(), ReminderPlanner.plan(input(playDays = listOf(t - 40))))
    }

    @Test
    fun `daily variants rotate by epoch day mod 3`() {
        val today = ReminderPlanner.plan(input(playDays = listOf(t - 1))).first()
        val afterPlayToday = ReminderPlanner.plan(input(playDays = listOf(t)))

        assertEquals(DAILY_3, today.variant) // 20741 % 3 == 2
        assertEquals(t + 1, afterPlayToday[0].epochDay)
        assertEquals(DAILY_1, afterPlayToday[0].variant) // 20742 % 3 == 0
        assertEquals(t + 2, afterPlayToday[1].epochDay)
        assertEquals(DAILY_2, afterPlayToday[1].variant) // 20743 % 3 == 1
        assertEquals(null, today.count)
    }

    @Test
    fun `streak of two or more days uses a streak variant with the streak length`() {
        val plan = ReminderPlanner.plan(input(playDays = listOf(t - 3, t - 2, t - 1)))

        val first = plan.first()
        assertEquals(t, first.epochDay)
        // T is odd: STREAK_2 first; its copy has no number.
        assertEquals(STREAK_2, first.variant)
        assertEquals(null, first.count)

        val evenDay = ReminderPlanner.plan(input(playDays = listOf(t - 2, t - 1, t))).first()
        assertEquals(STREAK_1, evenDay.variant)
        assertEquals(3, evenDay.count)
    }

    @Test
    fun `streak of one day uses no streak variant`() {
        assertEquals(DAILY_3, ReminderPlanner.plan(input(playDays = listOf(t - 1))).first().variant)
    }

    @Test
    fun `streak one day short of a bonus multiple uses streak_3 with the coming day number`() {
        val six = (1L..6L).map { t - it }
        val plan = ReminderPlanner.plan(input(playDays = six))

        assertEquals(STREAK_3, plan.first().variant)
        assertEquals(7, plan.first().count)

        val thirteen = (1L..13L).map { t - it }
        assertEquals(14, ReminderPlanner.plan(input(playDays = thirteen)).first().count)
    }

    @Test
    fun `streak_3 is off when the streak bonus is off`() {
        val six = (1L..6L).map { t - it }

        assertEquals(STREAK_2, ReminderPlanner.plan(input(playDays = six, streakBonusEvery = 0)).first().variant)
    }

    @Test
    fun `streak is broken two days after the last play so no streak variant`() {
        val plan = ReminderPlanner.plan(input(playDays = listOf(t - 3, t - 2, t - 1)))

        assertTrue(plan.byDay()[1L] in setOf(DAILY_1, DAILY_2, DAILY_3))
    }

    @Test
    fun `review variants need the review setting`() {
        val due = List(12) { t - 3 }

        assertTrue(ReminderPlanner.plan(input(reviewDueDays = due)).first().variant.isReview)
        assertEquals(DAILY_3, ReminderPlanner.plan(input(reviewDueDays = due, reviewReminders = false)).first().variant)
    }

    @Test
    fun `review_1 from 5 due cards with the due count`() {
        val plan = ReminderPlanner.plan(input(reviewDueDays = List(5) { t }))

        assertEquals(REVIEW_1, plan.first().variant)
        assertEquals(5, plan.first().count)
        assertEquals(DAILY_3, ReminderPlanner.plan(input(reviewDueDays = List(4) { t })).first().variant)
    }

    @Test
    fun `review_2 is used from 10 due cards`() {
        // T is odd: REVIEW_2 on odd days, REVIEW_1 on even days.
        val plan = ReminderPlanner.plan(input(reviewDueDays = List(10) { t - 1 }))

        assertEquals(REVIEW_2, plan[0].variant)
        assertEquals(10, plan[0].count)
        assertEquals(REVIEW_1, plan[1].variant)
    }

    @Test
    fun `due count grows on later days as cards fall due`() {
        val plan = ReminderPlanner.plan(input(reviewDueDays = List(4) { t } + List(3) { t + 1 }))

        assertEquals(DAILY_3, plan[0].variant)
        assertEquals(REVIEW_1, plan[1].variant)
        assertEquals(7, plan[1].count)
    }

    @Test
    fun `never the same message key on two consecutive days`() {
        // 5-9 due: review_1 every day would repeat, so every second day falls back to the daily variant.
        val plan = ReminderPlanner.plan(input(reviewDueDays = List(6) { t - 2 }))

        assertEquals(REVIEW_1, plan[0].variant)
        assertEquals(DAILY_1, plan[1].variant)
    }

    @Test
    fun `first planned day avoids the key assumed shown yesterday`() {
        // Played T-2; yesterday (T-1, 1 day without play) review_1 was shown. Today must not repeat it.
        val plan = ReminderPlanner.plan(input(playDays = listOf(t - 2), reviewDueDays = List(6) { t - 5 }))

        assertEquals(DAILY_3, plan.first().variant)
    }

    @Test
    fun `key shown earlier today is not repeated tomorrow after a late rebuild`() {
        val plan = ReminderPlanner.plan(
            input(playDays = listOf(t - 1), reviewDueDays = List(6) { t - 5 }, nowMinuteOfDay = 21 * 60),
        )

        assertEquals(t + 1, plan.first().epochDay)
        assertEquals(DAILY_1, plan.first().variant) // today's review_1 was due before now
    }

    @Test
    fun `a player who never completed a wheel gets two daily reminders then the comeback series`() {
        val plan = ReminderPlanner.plan(input(playDays = emptyList()))

        assertEquals(
            mapOf(0L to DAILY_3, 1L to DAILY_1, 2L to COMEBACK_3D, 6L to COMEBACK_7D, 13L to COMEBACK_14D),
            plan.byDay(),
        )
    }

    @Test
    fun `play days in the future are ignored`() {
        assertEquals(
            ReminderPlanner.plan(input(playDays = listOf(t - 1))),
            ReminderPlanner.plan(input(playDays = listOf(t - 1, t + 3))),
        )
    }

    @Test
    fun `stored reminder time outside the options is snapped to a valid option`() {
        assertEquals(8 * 60, ReminderPlanner.plan(input(minuteOfDay = 3 * 60, nowMinuteOfDay = 0)).first().minuteOfDay)
        assertEquals(21 * 60 + 30, ReminderPlanner.plan(input(minuteOfDay = 23 * 60)).first().minuteOfDay)
        assertEquals(7 * 60 + 30 + 30, ReminderPlanner.plan(input(minuteOfDay = 7 * 60 + 30)).first().minuteOfDay)
    }

    @Test
    fun `at most one reminder per day within today to today plus 14 and never repeating on consecutive days`() {
        val streaks = listOf(emptyList(), listOf(t), listOf(t - 1), (1L..6L).map { t - it }, (0L..12L).map { t - it })
        val dues = listOf(emptyList(), List(5) { t + 2 }, List(9) { t - 1 }, List(10) { t + 4 }, List(30) { t })
        val inputs = streaks.flatMap { days ->
            dues.flatMap { due ->
                listOf(0, 20 * 60, 23 * 60).flatMap { now ->
                    listOf(true, false).map { review ->
                        input(playDays = days, reviewDueDays = due, nowMinuteOfDay = now, reviewReminders = review)
                    }
                }
            }
        }
        for (case in inputs) {
            val plan = ReminderPlanner.plan(case)
            val planDays = plan.map { it.epochDay }
            assertEquals(planDays.distinct(), planDays)
            assertEquals(planDays.sorted(), planDays)
            assertTrue(plan.size <= ReminderPlanner.PLAN_DAYS)
            assertTrue(planDays.all { it in t until t + ReminderPlanner.PLAN_DAYS })
            plan.zipWithNext().filter { (a, b) -> b.epochDay == a.epochDay + 1 }.forEach { (a, b) ->
                assertNotEquals(a.variant, b.variant, "consecutive repeat in $plan")
            }
            if (!case.reviewReminders) assertTrue(plan.none { it.variant.isReview })
        }
    }

    @Test
    fun `reminder time options run from 08_00 to 21_30 in 30-minute steps`() {
        val options = ReminderTimes.optionsMinuteOfDay

        assertEquals(28, options.size)
        assertEquals(8 * 60, options.first())
        assertEquals(21 * 60 + 30, options.last())
        assertTrue(options.zipWithNext().all { (a, b) -> b - a == 30 })
        assertTrue(ReminderTimes.DEFAULT_MINUTE in options)
        assertEquals(1200, ReminderTimes.DEFAULT_MINUTE)
    }

    @Test
    fun `variants map to the COPY keys`() {
        assertEquals("notif_daily_1", DAILY_1.key)
        assertEquals("notif_streak_3", STREAK_3.key)
        assertEquals("notif_review_2", REVIEW_2.key)
        assertEquals("notif_comeback_14d", COMEBACK_14D.key)
        assertEquals(11, ReminderVariant.entries.size)
        assertTrue(ReminderVariant.entries.none { it.key == "notif_goal" })
        val comebacks = ReminderVariant.entries.filter { it.isComeback }.toSet()
        assertEquals(setOf(COMEBACK_3D, COMEBACK_7D, COMEBACK_14D), comebacks)
        assertEquals(setOf(STREAK_1, STREAK_2, STREAK_3), ReminderVariant.entries.filter { it.isStreak }.toSet())
        assertEquals(setOf(DAILY_1, DAILY_2, DAILY_3), ReminderVariant.entries.filter { it.isDaily }.toSet())
    }
}
