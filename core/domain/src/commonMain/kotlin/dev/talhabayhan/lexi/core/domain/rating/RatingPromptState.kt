package dev.talhabayhan.lexi.core.domain.rating

/**
 * Persisted state of the rating prompt (`COPY.md` §8).
 */
public data class RatingPromptState(
    val hasRated: Boolean = false,
    val refusalCount: Int = 0,
    val lastPromptDay: Long? = null,
    val lastRefusalDay: Long? = null,
) {
    public fun recordRated(): RatingPromptState = copy(hasRated = true)

    public fun recordDeclined(day: Long): RatingPromptState = copy(
        refusalCount = refusalCount + 1,
        lastRefusalDay = day,
        lastPromptDay = day,
    )

    public fun recordShown(day: Long): RatingPromptState = copy(
        lastPromptDay = day,
    )
}
