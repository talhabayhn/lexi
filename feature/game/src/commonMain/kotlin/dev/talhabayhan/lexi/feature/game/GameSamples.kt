package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** The design's sample data (`04-game.html` / `source/04-game.dc.html`) for previews and screenshot tests. */
internal object GameSamples {
    // design sample, not user-facing
    private const val CLUE = "mutfak"
    private const val ANSWER = "KITCHEN"
    private const val TYPED = 3
    private const val KEYS = "CHENSAYORU"
    private const val SCORE = 120
    private const val CURRENT = 'K'

    /** `source/04-game.dc.html`: A,B,D,F,G,I,J correct, C,H passed, E wrong, K current. */
    private val passed = setOf('C', 'H')
    private const val WRONG = 'E'

    private fun wheel(current: WheelLetterState): List<WheelLetterUi> = ('A'..'Z').map { letter ->
        val state = when {
            letter in passed -> WheelLetterState.Passed
            letter == WRONG -> WheelLetterState.Wrong
            letter < CURRENT -> WheelLetterState.Correct
            letter == CURRENT -> current
            else -> WheelLetterState.Todo
        }
        WheelLetterUi(letter, state)
    }

    private val typingTurn = GameTurnUi(
        letter = CURRENT,
        rule = LetterRule.STARTS,
        clue = CLUE,
        partOfSpeech = PartOfSpeech.NOUN,
        level = Level.A1,
        letterCount = ANSWER.length,
        words = persistentListOf(
            ANSWER.mapIndexed { index, letter ->
                when {
                    index < TYPED -> AnswerCellUi(letter, AnswerSlotState.Filled)
                    index == TYPED -> AnswerCellUi(null, AnswerSlotState.Active)
                    else -> AnswerCellUi(null, AnswerSlotState.Empty)
                }
            }.toImmutableList(),
        ),
        // The design draws every key unused although K, I, T are typed; keep the design's look.
        keys = KEYS.map { KeyUi(it, LetterKeyState.Normal) }.toImmutableList(),
        isRevealing = false,
    )

    /** `04-game.png`: K I T typed, 1:47 left (62 % fill), score 120. */
    val design: GameUiState = GameUiState(
        isLoading = false,
        wheel = wheel(WheelLetterState.Current).toImmutableList(),
        score = SCORE,
        turn = typingTurn,
        inputEnabled = true,
    )

    val designClock: GameClockUi =
        GameClockUi(isTimed = true, remainingMs = 107_000L, fraction = 0.62f, wholeSeconds = 107, isWarning = false)

    /** Last 10 s (motion.md #8). */
    val warningClock: GameClockUi =
        GameClockUi(isTimed = true, remainingMs = 8_400L, fraction = 0.05f, wholeSeconds = 9, isWarning = true)

    /** A wrong guess ("KITCEHN"): the correct answer is shown in red boxes, the chip is a red square. */
    val wrongReveal: GameUiState = design.copy(
        wheel = wheel(WheelLetterState.Wrong).toImmutableList(),
        turn = typingTurn.copy(
            words = persistentListOf(ANSWER.map { AnswerCellUi(it, AnswerSlotState.Wrong) }.toImmutableList()),
            keys = KEYS.map {
                KeyUi(it, if (it in ANSWER) LetterKeyState.Used else LetterKeyState.Normal)
            }.toImmutableList(),
            isRevealing = true,
        ),
        inputEnabled = false,
    )

    /**
     * `05-joker-sheet.png`: the sheet over the game at 1:31, jokers 2 / 1 / 3 / 0 (Telaffuzu dinle disabled), Classic
     * costs. The design's background is a simplified board; this is the real game screen behind the scrim.
     */
    val jokerSheet: GameUiState = design.copy(
        jokers = persistentListOf(
            JokerUi(JokerType.REVEAL_LETTER, 2, JokerCostUi.Seconds(10), enabled = true),
            JokerUi(JokerType.REMOVE_DISTRACTORS, 1, JokerCostUi.Seconds(15), enabled = true),
            JokerUi(JokerType.EXAMPLE, 3, JokerCostUi.Points(5), enabled = true),
            JokerUi(JokerType.LISTEN, 0, JokerCostUi.Points(10), enabled = false),
        ),
        jokerAdReward = 1,
        isJokerSheetOpen = true,
    )

    val jokerSheetClock: GameClockUi =
        GameClockUi(isTimed = true, remainingMs = 91_000L, fraction = 0.51f, wholeSeconds = 91, isWarning = false)

    /** After the "Örnek cümle" joker (design debt: the example line in the clue card has no design). */
    val exampleJoker: GameUiState = design.copy(
        turn = typingTurn.copy(example = "We cook dinner in the _______."),
        jokers = jokerSheet.jokers.map { it.copy(enabled = false) }.toImmutableList(),
    )

    /** The pause dialog (design debt) over the design board. */
    val paused: GameUiState = design.copy(isPaused = true, inputEnabled = false)

    /** Second step of the pause dialog: confirm giving up. */
    val quitConfirmation: GameUiState = paused.copy(isQuitConfirmation = true)
}
