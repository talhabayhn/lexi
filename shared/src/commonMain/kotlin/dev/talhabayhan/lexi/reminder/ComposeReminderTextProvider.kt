package dev.talhabayhan.lexi.reminder

import dev.talhabayhan.lexi.core.domain.reminder.ReminderText
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTextProvider
import dev.talhabayhan.lexi.core.domain.reminder.ReminderVariant
import dev.talhabayhan.lexi.shared.generated.resources.Res
import dev.talhabayhan.lexi.shared.generated.resources.notif_channel_name
import dev.talhabayhan.lexi.shared.generated.resources.notif_comeback_14d_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_comeback_14d_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_comeback_3d_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_comeback_3d_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_comeback_7d_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_comeback_7d_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_daily_1_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_daily_1_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_daily_2_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_daily_2_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_daily_3_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_daily_3_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_review_1_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_review_1_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_review_2_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_review_2_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_streak_1_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_streak_1_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_streak_2_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_streak_2_title
import dev.talhabayhan.lexi.shared.generated.resources.notif_streak_3_body
import dev.talhabayhan.lexi.shared.generated.resources.notif_streak_3_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

/** Resolves the reminder copy (`docs/copy/COPY.md` §1) from compose resources in the current app language. */
internal class ComposeReminderTextProvider : ReminderTextProvider {
    override suspend fun text(variant: ReminderVariant, count: Int?): ReminderText {
        val (title, body) = resourcesOf(variant)
        val n = count?.toString().orEmpty()
        return ReminderText(
            title = getString(title).replace(COUNT_TOKEN, n),
            body = getString(body).replace(COUNT_TOKEN, n),
        )
    }

    /** Name of the Android notification channel (also used as the settings label). */
    suspend fun channelName(): String = getString(Res.string.notif_channel_name)

    private fun resourcesOf(variant: ReminderVariant): Pair<StringResource, StringResource> = when (variant) {
        ReminderVariant.DAILY_1 -> Res.string.notif_daily_1_title to Res.string.notif_daily_1_body
        ReminderVariant.DAILY_2 -> Res.string.notif_daily_2_title to Res.string.notif_daily_2_body
        ReminderVariant.DAILY_3 -> Res.string.notif_daily_3_title to Res.string.notif_daily_3_body
        ReminderVariant.STREAK_1 -> Res.string.notif_streak_1_title to Res.string.notif_streak_1_body
        ReminderVariant.STREAK_2 -> Res.string.notif_streak_2_title to Res.string.notif_streak_2_body
        ReminderVariant.STREAK_3 -> Res.string.notif_streak_3_title to Res.string.notif_streak_3_body
        ReminderVariant.REVIEW_1 -> Res.string.notif_review_1_title to Res.string.notif_review_1_body
        ReminderVariant.REVIEW_2 -> Res.string.notif_review_2_title to Res.string.notif_review_2_body
        ReminderVariant.COMEBACK_3D -> Res.string.notif_comeback_3d_title to Res.string.notif_comeback_3d_body
        ReminderVariant.COMEBACK_7D -> Res.string.notif_comeback_7d_title to Res.string.notif_comeback_7d_body
        ReminderVariant.COMEBACK_14D -> Res.string.notif_comeback_14d_title to Res.string.notif_comeback_14d_body
    }

    private companion object {
        const val COUNT_TOKEN = "{n}"
    }
}
