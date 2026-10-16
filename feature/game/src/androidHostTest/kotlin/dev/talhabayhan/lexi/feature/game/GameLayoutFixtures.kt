package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Game states with clues of one, two and three lines (at the design's 30sp) and a 15-key turn, for layout tests. */
internal object GameLayoutFixtures {
    private val turn = requireNotNull(GameSamples.design.turn)

    /** The design sample: one line. */
    const val ONE_LINE: String = "mutfak"

    /** Two lines at 30sp in the 390dp design's clue area. */
    const val TWO_LINES: String = "bir şeyi yapmaktan vazgeçmek"

    /** Three lines at 30sp (the longest clues of the content are ~58 characters). */
    const val THREE_LINES: String = "(soygun, saldırı) kısa sürede sel gibi patlak veren çokluk"

    fun withClue(clue: String, example: String? = null): GameUiState =
        GameSamples.design.copy(turn = turn.copy(clue = clue, example = example))

    val twoLineClue: GameUiState = withClue(TWO_LINES)
    val threeLineClue: GameUiState = withClue(THREE_LINES)

    /** A 12-letter answer: 15 keys in three rows, three typed. */
    private const val LONG_ANSWER = "THUNDERSTORM"
    private const val LONG_KEYS = "THUNDERSTORMBKY"
    private const val TYPED = 3

    val fifteenKeys: GameUiState = GameSamples.design.copy(
        turn = turn.copy(
            letter = 'T',
            clue = "gök gürültülü fırtına",
            letterCount = LONG_ANSWER.length,
            words = persistentListOf(
                LONG_ANSWER.mapIndexed { index, letter ->
                    when {
                        index < TYPED -> AnswerCellUi(letter, AnswerSlotState.Filled)
                        index == TYPED -> AnswerCellUi(null, AnswerSlotState.Active)
                        else -> AnswerCellUi(null, AnswerSlotState.Empty)
                    }
                }.toImmutableList(),
            ),
            keys = LONG_KEYS.map { KeyUi(it, LetterKeyState.Normal) }.toImmutableList(),
        ),
    )
}
