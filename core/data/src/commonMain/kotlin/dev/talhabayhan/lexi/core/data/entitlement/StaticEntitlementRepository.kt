package dev.talhabayhan.lexi.core.data.entitlement

import dev.talhabayhan.lexi.core.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Pro status fixed at startup until the store SDK exists (RevenueCat, M7, replaces only this binding): release builds
 * are never Pro; debug builds pass `isPro = true` to unlock Pro content for testing (`shared` platform modules).
 */
public class StaticEntitlementRepository(isPro: Boolean) : EntitlementRepository {
    override val isPro: Flow<Boolean> = flowOf(isPro)
}
