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
        rationaleSeen: Boolean = false,
        unanswered: Int = 0,
        rationale: Boolean = false,
    ) = resolveNotificationStatus(sdk, granted, enabled, rationaleSeen, unanswered, rationale)

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
    fun aDismissedDialogStillCanAsk() {
        // Back or a tap outside: no denial, the system shows no rationale, one unanswered request.
        assertEquals(PermissionStatus.CAN_ASK, status(unanswered = 1))
    }

    @Test
    fun firstDenialStillCanAskBecauseTheSystemWantsARationale() {
        assertEquals(PermissionStatus.CAN_ASK, status(rationaleSeen = true, rationale = true))
    }

    @Test
    fun aDismissalAfterTheFirstDenialStillCanAsk() {
        assertEquals(PermissionStatus.CAN_ASK, status(rationaleSeen = true, unanswered = 1, rationale = true))
    }

    @Test
    fun secondDenialAfterTheRationaleIsBlocked() {
        assertEquals(PermissionStatus.BLOCKED, status(rationaleSeen = true, rationale = false))
    }

    @Test
    fun repeatedRequestsWithoutAnyAnswerAreBlocked() {
        // Dismissed twice, or a final denial restored from elsewhere: the system refuses without a dialog.
        assertEquals(PermissionStatus.BLOCKED, status(unanswered = BLOCKED_AFTER_UNANSWERED))
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
