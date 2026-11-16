package dev.talhabayhan.lexi.core.domain.platform

/**
 * External web links (docs/RELEASE.md §8): legal and support pages and the landing page. The site serves English at
 * the root and Turkish under `/tr/`; callers pick the language with the `turkish` flag, which comes from the app's UI
 * language ([dev.talhabayhan.lexi.core.domain.settings.AppLanguage.isTurkish]), not from the device alone.
 */
public object AppLinks {
    private const val SITE: String = "https://talhabayhn.github.io/lexi/"
    private const val SITE_TR: String = SITE + "tr/"

    public const val PRIVACY_POLICY: String = SITE + "privacy/"
    public const val TERMS_OF_SERVICE: String = SITE + "terms/"
    public const val SUPPORT: String = SITE + "support/"
    public const val LANDING: String = SITE

    public const val PRIVACY_POLICY_TR: String = SITE_TR + "privacy/"
    public const val TERMS_OF_SERVICE_TR: String = SITE_TR + "terms/"
    public const val SUPPORT_TR: String = SITE_TR + "support/"
    public const val LANDING_TR: String = SITE_TR

    public fun privacyPolicy(turkish: Boolean): String = if (turkish) PRIVACY_POLICY_TR else PRIVACY_POLICY

    public fun termsOfService(turkish: Boolean): String = if (turkish) TERMS_OF_SERVICE_TR else TERMS_OF_SERVICE

    public fun support(turkish: Boolean): String = if (turkish) SUPPORT_TR else SUPPORT

    /** The landing page that offers both stores; the link on the share card and in the share text. */
    public fun landing(turkish: Boolean): String = if (turkish) LANDING_TR else LANDING
}
