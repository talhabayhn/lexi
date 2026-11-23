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

    /** A share sheet was shown: the result card ([ShareType.RESULT]) or Home's invite ([ShareType.INVITE]). */
    public data class Share(val type: ShareType) : AnalyticsEvent("share") {
        override val params: Map<String, Any> get() = mapOf("type" to type.code)
    }

    /** The `type` of [Share]. */
    public enum class ShareType(public val code: String) {
        RESULT("result"),
        INVITE("invite"),
    }

    /** The store's rating request was invoked (the OS decides whether the player sees it); `RequestInAppReview`. */
    public data object RatingPromptRequested : AnalyticsEvent("rating_prompt_requested")

    public data object ReminderEnabled : AnalyticsEvent("reminder_enabled")

    public data class JokerUsed(val type: JokerType) : AnalyticsEvent("joker_used") {
        override val params: Map<String, Any> get() = mapOf("type" to type.name.lowercase())
    }

    /** A hot-seat duel starts (the first handover card); [clockSeconds] is the clock per player. */
    public data class DuelStart(val clockSeconds: Int) : AnalyticsEvent("duel_start") {
        override val params: Map<String, Any> get() = mapOf("clock_s" to clockSeconds.toLong())
    }

    /** A hot-seat duel ends; never carries the players' names. */
    public data class DuelFinish(val result: DuelResultType) : AnalyticsEvent("duel_finish") {
        override val params: Map<String, Any> get() = mapOf("result_type" to result.code)
    }

    /** `duel_finish.result_type`: a winner on the rules, a draw, or a player gave up. */
    public enum class DuelResultType(public val code: String) {
        WIN("win"),
        DRAW("draw"),
        FORFEIT("forfeit"),
    }

    /**
     * A motion fail-safe fired: an animation that would otherwise have kept the screen covered or hidden was ended
     * early ([MotionFailSafeKind]). No personal data; tells whether the fail-safes are ever needed in the field.
     */
    public data class MotionFailSafe(val kind: MotionFailSafeKind) : AnalyticsEvent("motion_failsafe") {
        override val params: Map<String, Any> get() = mapOf("kind" to kind.code)
    }

    /** `motion_failsafe.kind`. */
    public enum class MotionFailSafeKind(public val code: String) {
        /** The letter morph waited too long for the game (`LetterMorphMachine.WAIT_LIMIT_MILLIS`) and was cancelled. */
        MORPH_TIMEOUT("morph_timeout"),
    }

    private companion object {
        const val PARAM_MODE = "mode"
        const val PARAM_LEVEL = "level"

        val GameMode.code: String get() = name.lowercase()
    }
}
