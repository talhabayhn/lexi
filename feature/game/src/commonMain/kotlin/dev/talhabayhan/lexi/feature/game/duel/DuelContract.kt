package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.core.domain.game.MatchPhase
import dev.talhabayhan.lexi.feature.game.GameEffect
import dev.talhabayhan.lexi.feature.game.GameIntent
import dev.talhabayhan.lexi.feature.game.GameLoadError
import dev.talhabayhan.lexi.feature.game.GameUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Resting look of a hot-seat duel (`18-duel-handover`, `19-duel-game`). [board] is the active player's board, mapped
 * like a solo game ([dev.talhabayhan.lexi.feature.game.GameUiMapper]); the active clock is a separate flow, like solo.
 * Names are as typed ("" = the default name, which the screen takes from the resources).
 */
@Immutable
internal data class DuelUiState(
    /** True until both wheels are loaded (false once [loadError] is set). */
    val isLoading: Boolean = true,
    val loadError: GameLoadError? = null,
    /** HANDOVER shows the full-screen card and hides the board; PLAYING shows the board; FINISHED leaves. */
    val phase: MatchPhase = MatchPhase.HANDOVER,
    val board: GameUiState = GameUiState(),
    val players: ImmutableList<DuelPlayerUi> = persistentListOf(),
    /** 0-based index of the player whose turn it is (or who is up next during the handover). */
    val activePlayer: Int = 0,
    val handover: DuelHandoverUi? = null,
)

/** One player card of the duel HUD; the active card's clock comes from the clock flow instead of [remainingMs]. */
@Immutable
internal data class DuelPlayerUi(val name: String, val correct: Int, val remainingMs: Long)

/** The handover card (`18-duel-handover`) for [player]. */
@Immutable
internal data class DuelHandoverUi(
    val player: Int,
    val remainingMs: Long,
    /** Letters still to answer (not yet answered or passed). */
    val lettersLeft: Int,
    /** The player who just had the turn; null before the first turn. */
    val previous: DuelPreviousUi?,
    /** The previous turn ended because that player's clock ran out (announced with the turn change). */
    val previousTimeUp: Boolean = false,
    /** "PES ET" was tapped: the card asks "Pes etmek istiyor musun?" first. */
    val isQuitConfirmation: Boolean = false,
)

@Immutable
internal data class DuelPreviousUi(val player: Int, val correct: Int, val wrong: Int)

internal sealed interface DuelIntent {
    /** HAZIRIM on the handover card: the clock of the player up next starts (`MatchEvent.BeginTurn`). */
    data object Ready : DuelIntent

    /**
     * A board, pause dialog or lifecycle intent. On the handover card [GameIntent.RequestQuit], [GameIntent.CancelQuit]
     * and [GameIntent.Forfeit] are its "PES ET" confirmation; [GameIntent.Back] asks the same.
     */
    data class Board(val intent: GameIntent) : DuelIntent
}

internal sealed interface DuelEffect {
    /** Transient board motion (`motion.md` #2–#8), played like in a solo game. */
    data class Motion(val effect: GameEffect) : DuelEffect

    /** The match is over: show the duel result. Nothing is recorded. */
    data class NavigateToResult(val result: DuelResultRoute) : DuelEffect

    /** The match can't go on (it was dropped by a process death, `GAME_RULES.md` §8): back to Home. */
    data object Abandoned : DuelEffect

    /** "Geri dön" after a load error. */
    data object NavigateBack : DuelEffect

    /** "Ayarlar" of the pause dialog; the match stays paused underneath. */
    data object NavigateToSettings : DuelEffect
}
