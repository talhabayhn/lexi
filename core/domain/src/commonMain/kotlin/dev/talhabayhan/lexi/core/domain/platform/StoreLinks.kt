package dev.talhabayhan.lexi.core.domain.platform

/**
 * Links to app store listings for ratings and reviews (ADR 0024).
 */
public object StoreLinks {
    /** Google Play listing. */
    public const val ANDROID_STORE_URL: String = "https://play.google.com/store/apps/details?id=dev.talhabayhan.lexi"

    /** iOS App Store page that opens the write-a-review sheet. */
    public const val IOS_STORE_URL: String = "https://apps.apple.com/app/id6824403074?action=write-review"

    /**
     * The link printed on the share card's footer and appended to the share text (`12-share-card` "[MAĞAZA LİNKİ]").
     * One landing page for both stores; a blank value would leave the link out of the card and the share text.
     */
    public const val SHARE_LINK_LABEL: String = "https://talhabayhn.github.io/lexi"
}
