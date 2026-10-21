package dev.talhabayhan.lexi.core.domain.telemetry

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyticsEventTest {
    @Test
    fun `names and parameters match docs ANALYTICS`() {
        assertEquals("app_open", AnalyticsEvent.AppOpen.name)
        assertEquals("onboarding_complete", AnalyticsEvent.OnboardingComplete.name)
        assertEquals("share", AnalyticsEvent.Share.name)
        assertEquals("reminder_enabled", AnalyticsEvent.ReminderEnabled.name)
        assertEquals(
            mapOf<String, Any>("level" to "NAT"),
            AnalyticsEvent.LevelTestComplete(Level.NAT).params,
        )
        assertEquals(
            mapOf<String, Any>("mode" to "hardcore", "level" to "B1"),
            AnalyticsEvent.GameStart(GameMode.HARDCORE, Level.B1).params,
        )
        assertEquals(
            mapOf<String, Any>("mode" to "daily", "level" to "A2", "correct" to 17L, "duration_s" to 95L),
            AnalyticsEvent.GameFinish(GameMode.DAILY, Level.A2, correct = 17, durationSeconds = 95).params,
        )
        assertEquals(
            mapOf<String, Any>("type" to "remove_distractors"),
            AnalyticsEvent.JokerUsed(JokerType.REMOVE_DISTRACTORS).params,
        )
    }

    @Test
    fun `a negative duration is sent as zero`() {
        val event = AnalyticsEvent.GameFinish(GameMode.CLASSIC, Level.A1, correct = 0, durationSeconds = -3)
        assertEquals(0L, event.params["duration_s"])
    }

    @Test
    fun `names and parameter keys fit Firebase limits`() {
        val events = listOf(
            AnalyticsEvent.AppOpen,
            AnalyticsEvent.OnboardingComplete,
            AnalyticsEvent.LevelTestComplete(Level.A1),
            AnalyticsEvent.GameStart(GameMode.REVIEW, Level.C1),
            AnalyticsEvent.GameFinish(GameMode.PRACTICE, Level.B2, correct = 1, durationSeconds = 1),
            AnalyticsEvent.Share,
            AnalyticsEvent.ReminderEnabled,
        ) + JokerType.entries.map(AnalyticsEvent::JokerUsed)
        val snake = Regex("[a-z][a-z0-9_]{0,39}")
        events.forEach { event ->
            assertTrue(snake.matches(event.name), event.name)
            event.params.forEach { (key, value) ->
                assertTrue(snake.matches(key), key)
                assertTrue((value is String && value.length <= 100) || value is Long, "$key=$value")
            }
        }
    }
}
