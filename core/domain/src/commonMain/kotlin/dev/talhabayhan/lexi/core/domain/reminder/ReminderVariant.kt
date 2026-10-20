package dev.talhabayhan.lexi.core.domain.reminder

/**
 * Reminder notification copy (`docs/copy/COPY.md` §1). [key] is the COPY key prefix: the texts are `<key>_title` and
 * `<key>_body`. `notif_goal` is not used (owner decision, M7).
 */
public enum class ReminderVariant(public val key: String) {
    DAILY_1("notif_daily_1"),
    DAILY_2("notif_daily_2"),
    DAILY_3("notif_daily_3"),

    /** "{n} günlük serin sürüyor"; count = current streak. */
    STREAK_1("notif_streak_1"),

    /** No number in the copy. */
    STREAK_2("notif_streak_2"),

    /** "Yarın {n}. gün"; count = the streak day a wheel today would reach (a streak-bonus day). */
    STREAK_3("notif_streak_3"),

    /** count = due notebook cards. */
    REVIEW_1("notif_review_1"),

    /** count = due notebook cards. */
    REVIEW_2("notif_review_2"),
    COMEBACK_3D("notif_comeback_3d"),
    COMEBACK_7D("notif_comeback_7d"),
    COMEBACK_14D("notif_comeback_14d"),
    ;

    public val isDaily: Boolean get() = this == DAILY_1 || this == DAILY_2 || this == DAILY_3
    public val isStreak: Boolean get() = this == STREAK_1 || this == STREAK_2 || this == STREAK_3
    public val isReview: Boolean get() = this == REVIEW_1 || this == REVIEW_2
    public val isComeback: Boolean get() = this == COMEBACK_3D || this == COMEBACK_7D || this == COMEBACK_14D
}
