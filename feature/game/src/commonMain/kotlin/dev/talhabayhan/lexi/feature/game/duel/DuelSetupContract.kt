package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** `17-duel-setup`: the two names (as typed, "" = default), one shared level and the clock per player. */
@Immutable
internal data class DuelSetupUiState(
    /** False until the last match's setup is read from the settings. */
    val isLoaded: Boolean = false,
    val names: ImmutableList<String> = persistentListOf("", ""),
    val level: Level = Level.A1,
    val clockSeconds: Int = Settings.DEFAULT_DUEL_CLOCK_SECONDS,
    val clockOptionsSeconds: ImmutableList<Int> = persistentListOf(),
    /** The name dialog is open for this player (0-based); null when closed. */
    val editingPlayer: Int? = null,
    /** The dialog's text, already cut to [nameMaxLength]. */
    val nameDraft: String = "",
    val nameMaxLength: Int = 12,
)

internal sealed interface DuelSetupIntent {
    /** "Değiştir" on a player card: opens the name dialog with the current name. */
    data class EditName(val player: Int) : DuelSetupIntent

    data class NameChanged(val text: String) : DuelSetupIntent

    /** "Kaydet": the name is trimmed; empty means the default name. */
    data object SaveName : DuelSetupIntent

    /** "Vazgeç", the scrim or back: the dialog closes unchanged. */
    data object CancelName : DuelSetupIntent

    data class SelectLevel(val level: Level) : DuelSetupIntent

    data class SelectClock(val seconds: Int) : DuelSetupIntent

    /** BAŞLA: the setup is saved for the next match and the duel starts. */
    data object Start : DuelSetupIntent

    data object Back : DuelSetupIntent
}

internal sealed interface DuelSetupEffect {
    data class StartDuel(val route: DuelRoute) : DuelSetupEffect

    data object NavigateBack : DuelSetupEffect
}
