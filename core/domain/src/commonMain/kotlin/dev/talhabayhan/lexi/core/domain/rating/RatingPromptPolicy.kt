package dev.talhabayhan.lexi.core.domain.rating

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.time.AppClock

/**
 * Trigger policy for the rating prompt (`COPY.md` §8).
 *
 * Rules:
 * - Ask only after a good moment: >= 3 completed wheels, a result with >= 18 correct or a 3+ day streak
 * - Never in the first session (sessionCount >= 2)
 * - Never right after a wrong answer (GameOverReason.WRONG_IN_HARDCORE)
 * - Never during Daily, level test or duel
 * - Never twice within 120 days
 * - "Not now" = ask again no sooner than 30 days
 * - After 3 refusals stop permanently
 * - Once user rated, stop permanently
 */
public class RatingPromptPolicy(
    private val clock: AppClock,
    private val enabled: Boolean = RATING_PROMPT_ENABLED,
) {
    public fun shouldPrompt(
        state: RatingPromptState,
        sessionCount: Int,
        completedWheels: Int,
        mode: GameMode,
        reason: GameOverReason,
        correct: Int,
        streakDays: Int,
    ): Boolean = shouldPrompt(
        state = state,
        sessionCount = sessionCount,
        completedWheels = completedWheels,
        mode = mode,
        reason = reason,
        correct = correct,
        streakDays = streakDays,
        todayEpochDay = clock.epochDay(clock.nowMs()),
        enabled = enabled,
    )

    public companion object {
        /**
         * Rating prompt is disabled in this build because the store listing does not exist yet.
         * Tests exercise the trigger logic with this flag set to true.
         */
        public const val RATING_PROMPT_ENABLED: Boolean = false

        public const val MIN_COMPLETED_WHEELS: Int = 3
        public const val MIN_CORRECT_FOR_GOOD_MOMENT: Int = 18
        public const val MIN_STREAK_FOR_GOOD_MOMENT: Int = 3
        public const val MIN_SESSIONS: Int = 2
        public const val PERIOD_DAYS: Long = 120L
        public const val REFUSAL_PERIOD_DAYS: Long = 30L
        public const val MAX_REFUSALS: Int = 3

        public fun shouldPrompt(
            state: RatingPromptState,
            sessionCount: Int,
            completedWheels: Int,
            mode: GameMode,
            reason: GameOverReason,
            correct: Int,
            streakDays: Int,
            todayEpochDay: Long,
            enabled: Boolean = RATING_PROMPT_ENABLED,
        ): Boolean {
            if (!enabled) return false
            if (state.hasRated) return false
            if (state.refusalCount >= MAX_REFUSALS) return false

            // Never during Daily, level test, or duel
            if (mode == GameMode.DAILY || mode == GameMode.LEVEL_TEST || mode == GameMode.DUEL) return false

            // Never in the first session
            if (sessionCount < MIN_SESSIONS) return false

            // Must have completed at least 3 wheels
            if (completedWheels < MIN_COMPLETED_WHEELS) return false

            // Never right after a wrong answer
            if (reason == GameOverReason.WRONG_IN_HARDCORE) return false

            // Good moment: >= 18 correct or >= 3 day streak
            val isGoodMoment = correct >= MIN_CORRECT_FOR_GOOD_MOMENT || streakDays >= MIN_STREAK_FOR_GOOD_MOMENT
            if (!isGoodMoment) return false

            // Period checks
            val lastPrompt = state.lastPromptDay
            if (lastPrompt != null && (todayEpochDay - lastPrompt) < PERIOD_DAYS) return false

            val lastRefusal = state.lastRefusalDay
            if (lastRefusal != null && (todayEpochDay - lastRefusal) < REFUSAL_PERIOD_DAYS) return false

            return true
        }
    }
}
