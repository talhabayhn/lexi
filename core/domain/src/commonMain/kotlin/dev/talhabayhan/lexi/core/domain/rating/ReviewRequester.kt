package dev.talhabayhan.lexi.core.domain.rating

/**
 * The store's own rating request (ADR 0024): Google Play In-App Review on Android, `SKStoreReviewController` on iOS.
 * The OS decides whether anything is shown and never says whether the player rated, so there is no result beyond
 * "the API was invoked".
 */
public interface ReviewRequester {
    /** The store listing, where "Rate Lexi" in About sends the player to write a review. */
    public val storePageUrl: String

    /** Asks the OS to show its review prompt. Returns true if the API was invoked, false if it was unavailable. */
    public suspend fun requestReview(): Boolean
}
