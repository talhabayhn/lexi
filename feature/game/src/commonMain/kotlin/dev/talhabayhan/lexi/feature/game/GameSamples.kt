package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
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
}
