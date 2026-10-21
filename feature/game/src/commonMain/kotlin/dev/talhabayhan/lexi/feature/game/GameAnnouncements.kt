package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import dev.talhabayhan.lexi.core.designsystem.component.LiveAnnouncement
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.feature.game.components.title
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_correct
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_game_over
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_join
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_joker
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_lap
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_passed
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_time_warning
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_wrong
import dev.talhabayhan.lexi.feature.game.generated.resources.game_announce_wrong_answer
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** A game event a screen reader announces (politely, once). Derived from screen state, never from the engine. */
@Immutable
internal sealed interface GameAnnouncement {
    data class Correct(val letter: Char) : GameAnnouncement

    /** [answer]: the revealed correct answer ("KITCHEN"), when the board shows it. */
    data class Wrong(val letter: Char, val answer: String?) : GameAnnouncement

    data class Passed(val letter: Char) : GameAnnouncement

    data class JokerUsed(val type: JokerType) : GameAnnouncement

    /** The wheel wrapped after Z: the passed letters come again (`GAME_RULES.md` §2). */
    data object NewLap : GameAnnouncement

    data object GameOver : GameAnnouncement

    /** The clock entered the last seconds (motion.md #8); [seconds] left. */
    data class TimeWarning(val seconds: Int) : GameAnnouncement

    data object TimeUp : GameAnnouncement
}

/**
 * The events between two resting looks of the board, in the order they happen: the answer of the slot (correct,
 * wrong, pass), a joker, then the wheel's new lap or its end (every letter answered; time-up, forfeit and a Hardcore
 * miss end the game through their own announcement or the player's action). Nothing while loading or when either
 * state has no wheel; a new wheel (restart) announces nothing, because no letter of it changes to an answered state.
 */
internal fun boardAnnouncements(previous: GameUiState, current: GameUiState): List<GameAnnouncement> {
    if (previous.isLoading || current.isLoading || previous.wheel.isEmpty() || current.wheel.isEmpty()) {
        return emptyList()
    }
    val before = previous.wheel.associate { it.letter to it.state }
    val events = mutableListOf<GameAnnouncement>()
    current.wheel.forEach { item ->
        val old = before[item.letter] ?: return@forEach
        if (old == item.state) return@forEach
        when (item.state) {
            WheelLetterState.Correct -> events += GameAnnouncement.Correct(item.letter)
            WheelLetterState.Wrong -> events += GameAnnouncement.Wrong(item.letter, current.revealedAnswer())
            WheelLetterState.Passed -> events += GameAnnouncement.Passed(item.letter)
            WheelLetterState.Todo, WheelLetterState.Current -> Unit
        }
    }
    val counts = previous.jokers.associate { it.type to it.count }
    current.jokers.forEach { joker ->
        val old = counts[joker.type] ?: return@forEach
        if (joker.count < old) events += GameAnnouncement.JokerUsed(joker.type)
    }
    // The turn's letter, also while a wrong answer is revealed (no chip is current then).
    val oldLetter = previous.turn?.letter
    val newLetter = current.turn?.letter
    val newCurrent = current.wheel.firstOrNull { it.state == WheelLetterState.Current }?.letter
    when {
        previous.wheel.any { it.state.isOpen() } && current.wheel.none { it.state.isOpen() } ->
            events += GameAnnouncement.GameOver

        // PASSED → CURRENT behind the previous turn: the wheel wrapped after Z.
        oldLetter != null && newLetter != null && newCurrent == newLetter && newLetter < oldLetter &&
            before[newLetter] == WheelLetterState.Passed -> events += GameAnnouncement.NewLap
    }
    return events
}

/** Still to be played on this or a later lap. */
private fun WheelLetterState.isOpen(): Boolean =
    this == WheelLetterState.Todo || this == WheelLetterState.Current || this == WheelLetterState.Passed

/** The correct answer shown in red after a wrong guess, words separated by a space; null when not shown. */
private fun GameUiState.revealedAnswer(): String? {
    val turn = turn?.takeIf { it.isRevealing } ?: return null
    return turn.words.joinToString(ANSWER_WORD_SEPARATOR) { word -> word.mapNotNull { it.letter }.joinToString("") }
}

private const val ANSWER_WORD_SEPARATOR = " "

/** Makes an identical follow-up announcement differ (screen readers skip silent characters), so it is read again. */
private const val REPEAT_MARK = "​"

/** The latest announcement; [serial] counts announcements so a repeated text still changes the live region. */
@Immutable
private data class AnnouncementBatch(val events: List<GameAnnouncement>, val serial: Int)

/** What the previous state looked like, for the transitions; not snapshot state (it never triggers composition). */
private class AnnouncementTracker {
    var board: GameUiState? = null
    var warning: Boolean = false
    var timeUp: Boolean = false

    fun next(board: GameUiState, warning: Boolean, timeUp: Boolean, seconds: Int): List<GameAnnouncement> {
        val previous = this.board
        val events = mutableListOf<GameAnnouncement>()
        if (previous != null) {
            if (warning && !this.warning && !timeUp) events += GameAnnouncement.TimeWarning(seconds)
            if (timeUp && !this.timeUp) events += GameAnnouncement.TimeUp
            // At time-up the board ends too: "Süre doldu" says it.
            events += boardAnnouncements(previous, board).filterNot { timeUp && it == GameAnnouncement.GameOver }
        }
        this.board = board
        this.warning = warning
        this.timeUp = timeUp
        return events
    }
}

/**
 * Screen-reader announcements of the game (polite live region, `LiveAnnouncement`): correct, wrong (with the answer),
 * pass, joker used, a new lap, game over, the time warning (once, when it starts) and time-up. They are derived from
 * transitions of [state] and [clock], so the engine stays untouched and deterministic. Polite: they wait for the
 * screen reader's current speech; the pronunciation of a correct word plays through its own channel. While a [modal]
 * overlay is open the node is hidden like the rest of the board (its text still changes, e.g. when a joker closes the
 * sheet, and is read once the overlay is gone).
 */
@Composable
internal fun GameAnnouncer(
    state: GameUiState,
    clock: () -> GameClockUi,
    modal: Boolean,
    modifier: Modifier = Modifier,
) {
    val warning by remember(clock) { derivedStateOf { clock().let { it.isTimed && it.isWarning } } }
    val timeUp by remember(clock) { derivedStateOf { clock().let { it.isTimed && it.remainingMs <= 0L } } }
    val tracker = remember { AnnouncementTracker() }
    var latest by remember { mutableStateOf(AnnouncementBatch(emptyList(), serial = 0)) }
    LaunchedEffect(state, warning, timeUp) {
        val events = tracker.next(state, warning, timeUp, clock().wholeSeconds)
        if (events.isNotEmpty()) latest = AnnouncementBatch(events, latest.serial + 1)
    }
    val texts = latest.events.map { it.text() }
    val text = texts.reduceOrNull { first, second -> stringResource(Res.string.game_announce_join, first, second) }
    LiveAnnouncement(
        text = text.orEmpty() + if (latest.serial % 2 == 1) REPEAT_MARK else "",
        modifier = modifier.then(if (modal) Modifier.semantics { hideFromAccessibility() } else Modifier),
    )
}

@Composable
private fun GameAnnouncement.text(): String = when (this) {
    is GameAnnouncement.Correct -> stringResource(Res.string.game_announce_correct, letter.toString())

    is GameAnnouncement.Wrong -> if (answer != null) {
        stringResource(Res.string.game_announce_wrong_answer, letter.toString(), answer)
    } else {
        stringResource(Res.string.game_announce_wrong, letter.toString())
    }

    is GameAnnouncement.Passed -> stringResource(Res.string.game_announce_passed, letter.toString())

    is GameAnnouncement.JokerUsed -> stringResource(Res.string.game_announce_joker, stringResource(type.title()))

    GameAnnouncement.NewLap -> stringResource(Res.string.game_announce_lap)

    GameAnnouncement.GameOver -> stringResource(Res.string.game_announce_game_over)

    is GameAnnouncement.TimeWarning -> pluralStringResource(Res.plurals.game_announce_time_warning, seconds, seconds)

    GameAnnouncement.TimeUp -> stringResource(Res.string.game_announce_time_up)
}
