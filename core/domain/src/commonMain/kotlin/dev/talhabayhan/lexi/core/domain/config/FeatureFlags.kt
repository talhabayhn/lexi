package dev.talhabayhan.lexi.core.domain.config

/**
 * Switches for features that are built but not shipped yet. One immutable value, provided by DI (the app module) and
 * injected where needed; defaults are the shipped behaviour. M7 can back it with Remote Config.
 */
public data class FeatureFlags(
    /** The joker sheet's "Kısa reklam izle · +1 joker" button. Off until the rewarded ad exists (M7). */
    public val rewardedAdJokers: Boolean = false,
)
