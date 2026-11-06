package dev.talhabayhan.lexi.core.domain.rating

import dev.talhabayhan.lexi.core.domain.repository.SessionRepository
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsEvent
import dev.talhabayhan.lexi.core.domain.telemetry.AnalyticsTracker
import dev.talhabayhan.lexi.core.domain.time.AppClock
import kotlinx.coroutines.flow.first

/**
 * Asks the OS for a rating when [RatingPromptPolicy] allows it, and remembers the request so it happens at most once
 * per [RatingPromptPolicy.PERIOD_DAYS]. The caller picks the moment (Home, back from a real game, no other prompt).
 * Returns true if the OS API was invoked.
 */
public class RequestInAppReview(
    private val settings: SettingsRepository,
    private val sessions: SessionRepository,
    private val requester: ReviewRequester,
    private val analytics: AnalyticsTracker,
    private val clock: AppClock,
) {
    public suspend operator fun invoke(): Boolean {
        val current = settings.settings.first()
        val records = sessions.observeRecords().first()
        val today = clock.epochDay(clock.nowMs())
        // Upgraders have no stored first launch yet: their oldest game is the best evidence of the install day.
        val installDay = listOfNotNull(
            current.firstLaunchMs.takeIf { it > 0 }?.let(clock::epochDay),
            records.minOfOrNull { it.epochDay },
        ).minOrNull()
        val lastRequestDay = current.ratingLastRequestMs.takeIf { it > 0 }?.let(clock::epochDay)
        if (!RatingPromptPolicy.shouldRequest(records, installDay, lastRequestDay, today)) return false
        if (!requester.requestReview()) return false
        settings.update { it.copy(ratingLastRequestMs = clock.nowMs()) }
        analytics.track(AnalyticsEvent.RatingPromptRequested)
        return true
    }
}
