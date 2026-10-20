package dev.talhabayhan.lexi.reminder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dev.talhabayhan.lexi.core.domain.reminder.NotificationPermission
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * Bridge to the `POST_NOTIFICATIONS` ActivityResult launcher. [register] must run in `MainActivity.onCreate`, before
 * the activity is STARTED; a re-created activity registers again. [pending] is only touched on the main thread (the
 * result callback, `onDestroy` and [launch]'s body all run there); the fields are volatile for [currentActivity].
 *
 * Activity re-creation while the system dialog is up (rotation, language change): the pending request survives a
 * configuration change and the ActivityResult registry delivers the answer to the new activity's launcher, which
 * completes it, so the caller re-reads the real status after the answer. Any other destroy (no config change) also
 * clears the ViewModels, cancelling the waiting answer; the restored pre-prompt is shown again and a "Yes" there reads
 * the real status first (GRANTED after an earlier "Allow" ⇒ the reminder turns on without a second dialog).
 */
public object NotificationPermissionHost {
    @Volatile private var activity: WeakReference<ComponentActivity>? = null

    @Volatile private var launcher: ActivityResultLauncher<String>? = null

    @Volatile private var pending: CompletableDeferred<Unit>? = null

    public fun register(activity: ComponentActivity) {
        val registered = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            pending?.complete(Unit)
            pending = null
        }
        this.activity = WeakReference(activity)
        launcher = registered
        activity.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    if (launcher === registered) {
                        launcher = null
                        this@NotificationPermissionHost.activity = null
                        // A configuration change keeps the request: the new activity's launcher gets the answer.
                        if (!activity.isChangingConfigurations) {
                            pending?.complete(Unit)
                            pending = null
                        }
                    }
                }
            },
        )
    }

    internal fun currentActivity(): Activity? = activity?.get()

    /**
     * Shows the system dialog and returns when the player answered; a second caller waits for the same answer.
     * Returns false at once (no dialog shown) without an attached activity.
     */
    internal suspend fun launch(): Boolean = withContext(Dispatchers.Main) {
        pending?.let {
            it.await()
            return@withContext true
        }
        val active = launcher ?: return@withContext false
        val answered = CompletableDeferred<Unit>()
        pending = answered
        try {
            active.launch(Manifest.permission.POST_NOTIFICATIONS)
            answered.await()
        } finally {
            if (pending === answered) pending = null
        }
        true
    }
}

/** [NotificationPermission] for Android: runtime permission on API 33+, the app-level switch below. */
public class AndroidNotificationPermission(context: Context) : NotificationPermission {
    private val context = context.applicationContext
    private val prefs = this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override suspend fun status(): PermissionStatus {
        val sdk = Build.VERSION.SDK_INT
        val granted = sdk < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        val rationale = NotificationPermissionHost.currentActivity()?.let {
            it.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
        } ?: true
        return resolveNotificationStatus(
            sdk = sdk,
            permissionGranted = granted,
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            askedBefore = prefs.getBoolean(KEY_ASKED, false),
            shouldShowRationale = rationale,
        )
    }

    override suspend fun request(): PermissionStatus {
        val current = status()
        if (current != PermissionStatus.CAN_ASK) return current
        // Not shown (no activity attached): not "asked", or the next status would turn into BLOCKED.
        if (NotificationPermissionHost.launch()) prefs.edit().putBoolean(KEY_ASKED, true).apply()
        return status()
    }

    override fun openSystemSettings() {
        val notifications = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(notifications)
        } catch (_: android.content.ActivityNotFoundException) {
            val packageUri = Uri.fromParts("package", context.packageName, null)
            val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(details)
        }
    }

    private companion object {
        const val PREFS = "lexi_notification_permission"
        const val KEY_ASKED = "asked"
    }
}

/**
 * Pure mapping of the Android signals to [PermissionStatus]. Below API 33 there is no dialog: notifications are on or
 * the player has to enable them in the system settings. On 33+ a denied permission is final when it was asked before
 * and the system no longer wants a rationale (the player chose "don't allow" twice, or "don't ask again").
 */
internal fun resolveNotificationStatus(
    sdk: Int,
    permissionGranted: Boolean,
    notificationsEnabled: Boolean,
    askedBefore: Boolean,
    shouldShowRationale: Boolean,
): PermissionStatus = when {
    permissionGranted && notificationsEnabled -> PermissionStatus.GRANTED
    sdk < Build.VERSION_CODES.TIRAMISU -> PermissionStatus.BLOCKED
    permissionGranted -> PermissionStatus.BLOCKED
    askedBefore && !shouldShowRationale -> PermissionStatus.BLOCKED
    else -> PermissionStatus.CAN_ASK
}
