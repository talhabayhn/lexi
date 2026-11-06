package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.rating.ReviewRequester

/** Counts rating requests; also a silent [ReviewRequester] for previews and tests. */
public class FakeReviewRequester(
    override val storePageUrl: String = "https://store.example/lexi",
    private val available: Boolean = true,
) : ReviewRequester {
    /** How often [requestReview] was called. */
    public var requests: Int = 0
        private set

    override suspend fun requestReview(): Boolean {
        requests++
        return available
    }
}
