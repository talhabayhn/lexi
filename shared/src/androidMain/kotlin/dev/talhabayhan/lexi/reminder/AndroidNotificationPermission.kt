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
     * Shows the system dialog and returns when the player answered: [LaunchResult.SHOWN] for the caller that launched
     * it, [LaunchResult.JOINED] for a caller that waited for the same answer, [LaunchResult.NOT_SHOWN] at once without
     * an attached activity.
     */
    internal suspend fun launch(): LaunchResult = withContext(Dispatchers.Main) {
        pending?.let {
            it.await()
            return@withContext LaunchResult.JOINED
        }
        val active = launcher ?: return@withContext LaunchResult.NOT_SHOWN
        val answered = CompletableDeferred<Unit>()
        pending = answered
        try {
            active.launch(Manifest.permission.POST_NOTIFICATIONS)
            answered.await()
        } finally {
            if (pending === answered) pending = null
        }
        LaunchResult.SHOWN
    }

    internal enum class LaunchResult { SHOWN, JOINED, NOT_SHOWN }
}

/**
 * SharedPreferences file of [AndroidNotificationPermission]. Device-specific, so it is excluded from Auto Backup and
 * device transfer (`androidApp/src/main/res/xml/backup_rules.xml` and `data_extraction_rules.xml`).
 */
internal const val NOTIFICATION_PERMISSION_PREFS: String = "lexi_notification_permission"

/** [NotificationPermission] for Android: runtime permission on API 33+, the app-level switch below. */
public class AndroidNotificationPermission(context: Context) : NotificationPermission {
    private val context = context.applicationContext
    private val prefs = this.context.getSharedPreferences(NOTIFICATION_PERMISSION_PREFS, Context.MODE_PRIVATE)

    override suspend fun status(): PermissionStatus = status(rationale())

    override suspend fun request(): PermissionStatus {
        val rationaleBefore = rationale()
        val current = status(rationaleBefore)
        if (current != PermissionStatus.CAN_ASK) return current
        val launched = NotificationPermissionHost.launch()
        // Not shown (no activity attached): nothing to learn from the answer.
        if (launched == NotificationPermissionHost.LaunchResult.NOT_SHOWN) return status()
        val rationaleAfter = rationale()
        // Only the caller that launched the dialog counts it; a concurrent caller waited for the same dialog.
        val counts = launched == NotificationPermissionHost.LaunchResult.SHOWN
        if (counts && rationaleBefore == false && rationaleAfter == false && !permissionGranted()) {
            // Dismissed (back/outside: not a denial on API 33+) or refused without a dialog (a denial that was final
            // before this install's bookkeeping, e.g. restored from another device).
            val count = prefs.getInt(KEY_UNANSWERED, 0) + 1
            prefs.edit().putInt(KEY_UNANSWERED, count).apply()
        }
        return status(rationaleAfter)
    }

    /** Null without an attached activity (the system only answers for one). */
    private fun rationale(): Boolean? = NotificationPermissionHost.currentActivity()
        ?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)

    private fun permissionGranted(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    private fun status(rationale: Boolean?): PermissionStatus {
        // The rationale is true only between a first and a final denial; remember that it was seen.
        if (rationale == true && !prefs.getBoolean(KEY_RATIONALE_SEEN, false)) {
            prefs.edit().putBoolean(KEY_RATIONALE_SEEN, true).apply()
        }
        return resolveNotificationStatus(
            sdk = Build.VERSION.SDK_INT,
            permissionGranted = permissionGranted(),
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            rationaleSeenBefore = prefs.getBoolean(KEY_RATIONALE_SEEN, false),
            unansweredRequests = prefs.getInt(KEY_UNANSWERED, 0),
            // Without an activity the rationale is unknown: never conclude BLOCKED from that.
            shouldShowRationale = rationale ?: true,
        )
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
        // The former "asked" flag counted a dismissed dialog as a denial; it is no longer read.
        const val KEY_RATIONALE_SEEN = "rationale_seen"
        const val KEY_UNANSWERED = "unanswered_requests"
    }
}

/**
 * Pure mapping of the Android signals to [PermissionStatus]. Below API 33 there is no dialog: notifications are on or
 * the player has to enable them in the system settings. On 33+ the system wants a rationale only after a first denial
 * and stops wanting it once a second denial made it final, so a denied permission is final when the rationale was
 * seen before and is gone now. A dismissed dialog (back/outside) is no denial and leaves the rationale off, so it
 * stays [PermissionStatus.CAN_ASK]; only after [BLOCKED_AFTER_UNANSWERED] requests without any answer (dismissed
 * each time, or refused without a dialog) does the way out become the system settings.
 */
internal fun resolveNotificationStatus(
    sdk: Int,
    permissionGranted: Boolean,
    notificationsEnabled: Boolean,
    rationaleSeenBefore: Boolean,
    unansweredRequests: Int,
    shouldShowRationale: Boolean,
): PermissionStatus = when {
    permissionGranted && notificationsEnabled -> PermissionStatus.GRANTED
    sdk < Build.VERSION_CODES.TIRAMISU -> PermissionStatus.BLOCKED
    permissionGranted -> PermissionStatus.BLOCKED
    shouldShowRationale -> PermissionStatus.CAN_ASK
    rationaleSeenBefore -> PermissionStatus.BLOCKED
    unansweredRequests >= BLOCKED_AFTER_UNANSWERED -> PermissionStatus.BLOCKED
    else -> PermissionStatus.CAN_ASK
}

/** Requests without a denial or a grant (see [resolveNotificationStatus]) after which the status turns BLOCKED. */
internal const val BLOCKED_AFTER_UNANSWERED: Int = 2
