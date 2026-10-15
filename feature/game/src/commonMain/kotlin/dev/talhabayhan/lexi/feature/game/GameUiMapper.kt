package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.JokerCost
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.game.KeyState
import dev.talhabayhan.lexi.core.domain.game.SlotState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** Pure mapping of engine state to the screen's resting look (ADR 0019 §2). */
internal object GameUiMapper {
    private const val MS_PER_SECOND = 1_000L
    private const val BLANK = '_'

    /** `05-joker-sheet` order: Harf aç, Çeldiricileri temizle, Örnek cümle, Telaffuzu dinle. */
    private val SHEET_ORDER = listOf(
        JokerType.REVEAL_LETTER,
        JokerType.REMOVE_DISTRACTORS,
        JokerType.EXAMPLE,
        JokerType.LISTEN,
    )

    /**
     * [previous] keeps the last turn visible after game over (the engine clears the current slot when it finishes)
     * until the screen navigates away. [engine] decides which jokers can be used ([GameEngine.canUseJoker]). The
     * screen flags (joker sheet, quit confirmation) are owned by the ViewModel and stay false here.
     */
    fun map(state: GameState, engine: GameEngine, previous: GameUiState? = null): GameUiState {
        val wheel = state.wheel.slots
            .filter { it.isPlayable }
            .map { WheelLetterUi(it.letter, it.state.toWheelState()) }
            .toImmutableList()
        if (state.status == GameStatus.NOT_STARTED) return GameUiState(isLoading = true, wheel = wheel)
        val turn = turnOf(state) ?: previous?.turn
        return GameUiState(
            isLoading = false,
            wheel = wheel,
            score = state.score,
            turn = turn,
            isPaused = state.isPaused,
            inputEnabled = state.status == GameStatus.PLAYING &&
                !state.isPaused &&
                !state.isRevealing &&
                state.currentInput != null,
            jokers = jokersOf(state, engine),
            jokerAdReward = engine.config.rewardedAdJokerReward,
        )
    }

    /** The sheet's jokers in design order with inventory, cost in this mode and [GameEngine.canUseJoker]. */
    private fun jokersOf(state: GameState, engine: GameEngine): ImmutableList<JokerUi> = SHEET_ORDER.map { type ->
        val cost = when (val cost = engine.config.jokerCost(type, state.mode)) {
            is JokerCost.Time -> JokerCostUi.Seconds((cost.ms / MS_PER_SECOND).toInt())
            is JokerCost.Points -> JokerCostUi.Points(cost.points)
            JokerCost.Free -> JokerCostUi.Free
        }
        JokerUi(type, state.jokers.count(type), cost, engine.canUseJoker(state, type))
    }.toImmutableList()

    /**
     * [example] with every occurrence of [answer] (whole words, any case) blanked letter by letter; the spaces of a
     * multi-word answer stay (`CONTENT.md` §2: the example contains the answer as written).
     */
    fun blankExample(example: String, answer: String): String {
        val words = answer.trim().split(' ').filter { it.isNotEmpty() }
        val pattern = words.joinToString(separator = "\\s+", prefix = "\\b", postfix = "\\b") { Regex.escape(it) }
        return Regex(pattern, RegexOption.IGNORE_CASE).replace(example) { match ->
            match.value.map { if (it.isLetter()) BLANK else it }.joinToString("")
        }
    }

    /** Clock for the HUD; before `Start` the full [timeLimitMs] is shown. Null limit = untimed mode. */
    fun clock(state: GameState, timeLimitMs: Long?, warningSeconds: Int): GameClockUi {
        if (timeLimitMs == null || timeLimitMs <= 0) return GameClockUi.Untimed
        val remaining = (state.remainingMs ?: timeLimitMs).coerceIn(0L, timeLimitMs)
        val seconds = ((remaining + MS_PER_SECOND - 1) / MS_PER_SECOND).toInt()
        return GameClockUi(
            isTimed = true,
            remainingMs = remaining,
            fraction = remaining.toFloat() / timeLimitMs,
            wholeSeconds = seconds,
            isWarning = seconds <= warningSeconds,
        )
    }

    private fun turnOf(state: GameState): GameTurnUi? {
        val slot = state.currentSlot ?: return null
        val card = slot.card ?: return null
        val input = state.currentInput ?: return null
        val answer = card.answer.replace(" ", "")
        val firstEmpty = input.cells.indexOf(null)
        val cells = input.cells.mapIndexed { index, placed ->
            when {
                state.isRevealing -> AnswerCellUi(answer[index], AnswerSlotState.Wrong)
                placed == null && index == firstEmpty -> AnswerCellUi(null, AnswerSlotState.Active)
                placed == null -> AnswerCellUi(null, AnswerSlotState.Empty)
                placed.revealed -> AnswerCellUi(placed.letter, AnswerSlotState.JokerFilled)
                else -> AnswerCellUi(placed.letter, AnswerSlotState.Filled)
            }
        }
        return GameTurnUi(
            letter = slot.letter,
            rule = card.rule,
            clue = card.clue,
            partOfSpeech = card.partOfSpeech,
            level = card.level,
            letterCount = card.answerLetters,
            words = cells.splitIntoWords(card.answer),
            keys = input.keys.map { KeyUi(it.letter, it.state.toKeyState()) }.toImmutableList(),
            isRevealing = state.isRevealing,
            example = if (JokerType.EXAMPLE in input.jokersUsed) blankExample(card.example, card.answer) else null,
        )
    }

    private fun List<AnswerCellUi>.splitIntoWords(answer: String): ImmutableList<ImmutableList<AnswerCellUi>> {
        var start = 0
        return answer.split(' ').map { word ->
            subList(start, start + word.length).toImmutableList().also { start += word.length }
        }.toImmutableList()
    }

    private fun SlotState.toWheelState(): WheelLetterState = when (this) {
        SlotState.TODO, SlotState.SKIPPED -> WheelLetterState.Todo
        SlotState.CURRENT -> WheelLetterState.Current
        SlotState.CORRECT -> WheelLetterState.Correct
        SlotState.WRONG -> WheelLetterState.Wrong
        SlotState.PASSED -> WheelLetterState.Passed
    }

    private fun KeyState.toKeyState(): LetterKeyState = when (this) {
        KeyState.AVAILABLE -> LetterKeyState.Normal
        KeyState.USED -> LetterKeyState.Used
        KeyState.DISABLED -> LetterKeyState.RemovedByJoker
    }
}
