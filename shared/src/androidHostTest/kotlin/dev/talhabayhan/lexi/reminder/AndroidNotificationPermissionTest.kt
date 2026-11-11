package dev.talhabayhan.lexi.reminder

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.ComponentActivity
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AndroidNotificationPermissionTest {
    private val app: Application = RuntimeEnvironment.getApplication()
    private val prefs = app.getSharedPreferences(NOTIFICATION_PERMISSION_PREFS, Context.MODE_PRIVATE)
    private lateinit var controller: ActivityController<ComponentActivity>
    private lateinit var activity: ComponentActivity
    private var destroyed = false

    @Before
    fun setUp() {
        controller = Robolectric.buildActivity(ComponentActivity::class.java).create()
        activity = controller.get()
        NotificationPermissionHost.register(activity)
        controller.start().resume()
    }

    @After
    fun tearDown() {
        if (!destroyed) destroyActivity()
    }

    private fun destroyActivity() {
        controller.pause().stop().destroy()
        destroyed = true
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun requestAsync(permission: AndroidNotificationPermission): Deferred<PermissionStatus> =
        CoroutineScope(Dispatchers.Main).async { permission.request() }

    /** Back or a tap outside the dialog: the result is a denial, and the system still shows no rationale. */
    private fun dismissDialog() {
        val requested = shadowOf(activity).lastRequestedPermission
        activity.onRequestPermissionsResult(
            requested.requestCode,
            requested.requestedPermissions,
            IntArray(requested.requestedPermissions.size) { PackageManager.PERMISSION_DENIED },
        )
        idle()
    }

    @Test
    fun twoConcurrentRequestsCountOneDismissedDialogOnce() {
        val permission = AndroidNotificationPermission(app)

        val first = requestAsync(permission)
        val second = requestAsync(permission)
        idle()
        val requested = shadowOf(activity).lastRequestedPermission.requestedPermissions
        assertEquals(listOf(Manifest.permission.POST_NOTIFICATIONS), requested.toList())
        dismissDialog()

        assertTrue(first.isCompleted && second.isCompleted)
        assertEquals(PermissionStatus.CAN_ASK, first.getCompleted())
        assertEquals(PermissionStatus.CAN_ASK, second.getCompleted())
        assertEquals(1, prefs.getInt("unanswered_requests", 0))
    }

    @Test
    fun aLegacyAskedFlagWithoutRationaleIsBlockedAtOnce() {
        // Asked under the old bookkeeping and finally denied: the system would refuse without a dialog.
        prefs.edit().putBoolean("asked", true).commit()
        val permission = AndroidNotificationPermission(app)

        val result = requestAsync(permission)
        idle()

        assertEquals(PermissionStatus.BLOCKED, result.getCompleted())
        assertEquals(null, shadowOf(activity).lastRequestedPermission)
        assertEquals(BLOCKED_AFTER_UNANSWERED, prefs.getInt("unanswered_requests", 0))
        assertFalse(prefs.contains("asked"))
    }

    @Test
    fun aLegacyAskedFlagWithARationaleStillCanAskAndADismissalIsNoDenial() {
        prefs.edit().putBoolean("asked", true).commit()
        shadowOf(app.packageManager)
            .setShouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS, true)
        val permission = AndroidNotificationPermission(app)

        val result = requestAsync(permission)
        idle()
        dismissDialog()

        assertEquals(PermissionStatus.CAN_ASK, result.getCompleted())
        assertEquals(0, prefs.getInt("unanswered_requests", 0))
        assertFalse(prefs.contains("asked"))
    }

    @Test
    fun aLegacyAskedFlagWaitsForAKnownRationale() {
        prefs.edit().putBoolean("asked", true).commit()
        destroyActivity()

        val result = CoroutineScope(Dispatchers.Main).async { AndroidNotificationPermission(app).status() }
        idle()

        // No attached activity: the rationale is unknown, so neither BLOCKED nor the migration yet.
        assertEquals(PermissionStatus.CAN_ASK, result.getCompleted())
        assertTrue(prefs.getBoolean("asked", false))
        assertEquals(0, prefs.getInt("unanswered_requests", 0))
    }
}
