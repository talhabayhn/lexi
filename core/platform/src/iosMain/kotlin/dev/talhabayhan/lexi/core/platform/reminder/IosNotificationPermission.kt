package dev.talhabayhan.lexi.core.platform.reminder

import dev.talhabayhan.lexi.core.domain.reminder.NotificationPermission
import dev.talhabayhan.lexi.core.domain.reminder.PermissionStatus
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenNotificationSettingsURLString
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** [NotificationPermission] on `UNUserNotificationCenter` (alert + sound, no badge). */
public class IosNotificationPermission : NotificationPermission {
    private val center: UNUserNotificationCenter get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun status(): PermissionStatus = suspendCoroutine { cont ->
        center.getNotificationSettingsWithCompletionHandler { settings ->
            cont.resume(statusOf(settings?.authorizationStatus ?: UNAuthorizationStatusNotDetermined))
        }
    }

    override suspend fun request(): PermissionStatus {
        if (status() != PermissionStatus.CAN_ASK) return status()
        suspendCoroutine<Unit> { cont ->
            center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { _, _ ->
                cont.resume(Unit)
            }
        }
        return status()
    }

    override fun openSystemSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenNotificationSettingsURLString)
            ?: NSURL.URLWithString(UIApplicationOpenSettingsURLString)
            ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    internal companion object {
        /** Authorized, provisional and ephemeral all deliver notifications. */
        fun statusOf(raw: UNAuthorizationStatus): PermissionStatus = when {
            raw == UNAuthorizationStatusNotDetermined -> PermissionStatus.CAN_ASK
            raw == UNAuthorizationStatusDenied -> PermissionStatus.BLOCKED
            else -> PermissionStatus.GRANTED
        }
    }
}
