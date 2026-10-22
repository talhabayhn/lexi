package dev.talhabayhan.lexi.core.data.entitlement

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StaticEntitlementRepositoryTest {
    @Test
    fun `emits the fixed status once`() = runTest {
        assertEquals(listOf(false), StaticEntitlementRepository(isPro = false).isPro.toList())
        assertEquals(listOf(true), StaticEntitlementRepository(isPro = true).isPro.toList())
    }
}
