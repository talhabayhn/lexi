package dev.talhabayhan.lexi.core.domain.telemetry

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.model.Level

/** Product analytics (Firebase Analytics on both platforms, ADR 0023). Fire and forget: never throws. */
public interface AnalyticsTracker {
    public fun track(event: AnalyticsEvent)
}

/**
 * The closed event list of `docs/ANALYTICS.md`. Parameter values are enum codes ([String]) or counts ([Long]) only:
 * no personal data, no free text, no IDs.
 */
public sealed class AnalyticsEvent(public val name: String) {
    public open val params: Map<String, Any> get() = emptyMap()

    public data object AppOpen : AnalyticsEvent("app_open")

    public data object OnboardingComplete : AnalyticsEvent("onboarding_complete")

    public data class LevelTestComplete(val level: Level) : AnalyticsEvent("level_test_complete") {
        override val params: Map<String, Any> get() = mapOf(PARAM_LEVEL to level.name)
    }

    public data class GameStart(val mode: GameMode, val level: Level) : AnalyticsEvent("game_start") {
        override val params: Map<String, Any> get() = mapOf(PARAM_MODE to mode.code, PARAM_LEVEL to level.name)
    }

    public data class GameFinish(val mode: GameMode, val level: Level, val correct: Int, val durationSeconds: Long) :
        AnalyticsEvent("game_finish") {
        override val params: Map<String, Any>
            get() = mapOf(
                PARAM_MODE to mode.code,
                PARAM_LEVEL to level.name,
                "correct" to correct.toLong(),
                "duration_s" to durationSeconds.coerceAtLeast(0),
            )
    }

    public data object Share : AnalyticsEvent("share")

    public data object ReminderEnabled : AnalyticsEvent("reminder_enabled")

    public data class JokerUsed(val type: JokerType) : AnalyticsEvent("joker_used") {
        override val params: Map<String, Any> get() = mapOf("type" to type.name.lowercase())
    }

    private companion object {
        const val PARAM_MODE = "mode"
        const val PARAM_LEVEL = "level"

        val GameMode.code: String get() = name.lowercase()
    }
}
