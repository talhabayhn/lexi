package dev.talhabayhan.lexi.reminder

import android.os.Build
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationStatusTest {
    private val t = Build.VERSION_CODES.TIRAMISU

    private fun status(
        sdk: Int = t,
        granted: Boolean = false,
        enabled: Boolean = false,
        asked: Boolean = false,
        rationale: Boolean = false,
    ) = resolveNotificationStatus(sdk, granted, enabled, asked, rationale)

    @Test
    fun grantedWhenPermissionAndAppSwitchAreOn() {
        assertEquals(PermissionStatus.GRANTED, status(granted = true, enabled = true))
        assertEquals(PermissionStatus.GRANTED, status(sdk = 30, granted = true, enabled = true))
    }

    @Test
    fun neverAskedCanAsk() {
        assertEquals(PermissionStatus.CAN_ASK, status())
    }

    @Test
    fun firstDenialStillCanAskBecauseTheSystemWantsARationale() {
        assertEquals(PermissionStatus.CAN_ASK, status(asked = true, rationale = true))
    }

    @Test
    fun deniedWithoutRationaleAfterAskingIsBlocked() {
        assertEquals(PermissionStatus.BLOCKED, status(asked = true, rationale = false))
    }

    @Test
    fun grantedButDisabledInSystemSettingsIsBlocked() {
        assertEquals(PermissionStatus.BLOCKED, status(granted = true, enabled = false))
    }

    @Test
    fun belowTiramisuThereIsNoDialog() {
        assertEquals(PermissionStatus.BLOCKED, status(sdk = 30, granted = true, enabled = false))
    }
}
