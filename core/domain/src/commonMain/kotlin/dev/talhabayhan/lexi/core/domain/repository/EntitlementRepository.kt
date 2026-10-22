package dev.talhabayhan.lexi.core.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * The player's Pro status (`PRODUCT.md` Monetisation). Features read only this interface; the store SDK (RevenueCat,
 * M7) replaces just the binding. Until then production is never Pro and debug builds may unlock it for testing.
 */
public interface EntitlementRepository {
    /** True while the player has an active Pro entitlement; emits again when it changes. */
    public val isPro: Flow<Boolean>
}
