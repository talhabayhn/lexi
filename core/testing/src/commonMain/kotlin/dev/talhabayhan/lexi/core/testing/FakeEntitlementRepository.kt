package dev.talhabayhan.lexi.core.testing

import dev.talhabayhan.lexi.core.domain.repository.EntitlementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [EntitlementRepository]; set [pro] to simulate a purchase or an expired subscription. */
public class FakeEntitlementRepository(isPro: Boolean = false) : EntitlementRepository {
    public val pro: MutableStateFlow<Boolean> = MutableStateFlow(isPro)

    override val isPro: StateFlow<Boolean> = pro
}
