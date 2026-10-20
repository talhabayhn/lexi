package dev.talhabayhan.lexi.feature.settings.reminder

import androidx.compose.runtime.Immutable

/** DESIGN-DEBT (no design): the reminder pre-prompt shown before the OS notification dialog. */
@Immutable
internal data class ReminderPrePromptUiState(
    /** An answer is being handled (the OS dialog may be up); the buttons ignore further taps. */
    val busy: Boolean = false,
)

internal sealed interface ReminderPrePromptIntent {
    /** "Evet, hatırlat": asks the OS; granted turns the reminder on at 20:00. */
    data object Accept : ReminderPrePromptIntent

    /** "Şimdi değil": never asked proactively again. */
    data object Decline : ReminderPrePromptIntent
}

internal sealed interface ReminderPrePromptEffect {
    /** The answer is handled: leave the screen. */
    data object Done : ReminderPrePromptEffect
}
