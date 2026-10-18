package dev.talhabayhan.lexi.core.domain.platform

/**
 * External web links for legal and support pages (docs/RELEASE.md §8).
 *
 * TODO: Turkish UI may use the /tr/ path (e.g. https://talhabayhn.github.io/lexi/tr/privacy/)
 * when per-locale link dispatch is wired.
 */
public object AppLinks {
    public const val PRIVACY_POLICY: String = "https://talhabayhn.github.io/lexi/privacy/"
    public const val TERMS_OF_SERVICE: String = "https://talhabayhn.github.io/lexi/terms/"
    public const val SUPPORT: String = "https://talhabayhn.github.io/lexi/support/"
}
