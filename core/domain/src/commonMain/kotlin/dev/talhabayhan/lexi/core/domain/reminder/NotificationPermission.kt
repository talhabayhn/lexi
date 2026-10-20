package dev.talhabayhan.lexi.core.domain.reminder

/**
 * OS notification permission (Android 13+ `POST_NOTIFICATIONS`, iOS `UNUserNotificationCenter` authorization).
 * Requested only when the player turns the reminder on or says yes on the pre-prompt, never at app start.
 */
public interface NotificationPermission {
    public suspend fun status(): PermissionStatus

    /** Shows the OS dialog when [PermissionStatus.CAN_ASK]; otherwise returns the current status without a dialog. */
    public suspend fun request(): PermissionStatus

    /** Opens the app's notification settings (the way back from [PermissionStatus.BLOCKED]). */
    public fun openSystemSettings()
}

public enum class PermissionStatus {
    GRANTED,

    /** Not granted yet and the OS dialog can still be shown. */
    CAN_ASK,

    /** Denied for good (or disabled in system settings): only the system settings can change it. */
    BLOCKED,
}
