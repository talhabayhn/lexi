package dev.talhabayhan.lexi.core.domain.platform

/**
 * Links to app store listings for ratings and reviews (`COPY.md` §8, M8.4).
 */
public object StoreLinks {
    /** Android Google Play Store market URL. */
    public const val ANDROID_STORE_URL: String = "market://details?id=dev.talhabayhan.lexi"

    /**
     * iOS App Store review link.
     *
     * TODO: Replace [APP_ID] placeholder once the Apple App Store ID is assigned.
     * Example: "https://apps.apple.com/app/id[APP_ID]?action=write-review"
     */
    public const val IOS_STORE_URL: String = "https://apps.apple.com/app/id[APP_ID]?action=write-review"

    /** Single constant store link for the app target. */
    public const val STORE_URL: String = ANDROID_STORE_URL
}
