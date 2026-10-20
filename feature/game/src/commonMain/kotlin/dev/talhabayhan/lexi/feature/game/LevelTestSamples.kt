package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Level test run samples (DESIGN-DEBT: no design, built from `04-game` with the "TEST" badge, no joker button and a
 * wheel of the asked letters) for previews and screenshot tests. The current card is the design's "mutfak" / KITCHEN.
 */
internal object LevelTestSamples {
    /** Six answered cards (letters in asked order) and the current K; 3 correct, 1:14 left. */
    private val asked = listOf(
        WheelLetterUi('M', WheelLetterState.Correct),
        WheelLetterUi('S', WheelLetterState.Correct),
        WheelLetterUi('B', WheelLetterState.Wrong),
        WheelLetterUi('T', WheelLetterState.Correct),
        WheelLetterUi('P', WheelLetterState.Passed),
        WheelLetterUi('D', WheelLetterState.Wrong),
    )
    private const val CORRECT = 3

    private fun wheel(current: WheelLetterState) = (asked + WheelLetterUi('K', current)).toImmutableList()

    private val turn: GameTurnUi = checkNotNull(GameSamples.design.turn).copy(level = Level.B1)

    /** Mid-test: typing the seventh card. */
    val midTest: GameUiState = GameSamples.design.copy(
        wheel = wheel(WheelLetterState.Current),
        score = 0,
        correct = CORRECT,
        isScored = false,
        turn = turn,
        canRestart = false,
        isLevelTest = true,
    )

    val midTestClock: GameClockUi =
        GameClockUi(isTimed = true, remainingMs = 74_000L, fraction = 0.62f, wholeSeconds = 74, isWarning = false)

    /** A wrong answer: the correct answer is revealed in red boxes, then the next card starts. */
    val reveal: GameUiState = midTest.copy(
        wheel = wheel(WheelLetterState.Wrong),
        turn = turn.copy(
            words = persistentListOf(
                "KITCHEN".map { AnswerCellUi(it, AnswerSlotState.Wrong) }.toImmutableList(),
            ),
            keys = turn.keys.map {
                KeyUi(it.letter, if (it.letter in "KITCHEN") LetterKeyState.Used else LetterKeyState.Normal)
            }.toImmutableList(),
            isRevealing = true,
        ),
        inputEnabled = false,
    )

    /** The pool has no card of the start level: only "Geri dön". */
    val unavailable: GameUiState =
        GameUiState(isLoading = false, loadError = GameLoadError.LevelTestUnavailable, isLevelTest = true)
}
