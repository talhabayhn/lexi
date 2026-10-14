package dev.talhabayhan.lexi.core.domain.leveltest

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlinx.serialization.Serializable

/** One asked card of the level test; a pass counts as not [correct]. */
@Serializable
public data class LevelTestAnswer(val cardId: CardId, val level: Level, val letter: Char, val correct: Boolean)

@Serializable
public data class LevelTestState(
    val seed: Long,
    val currentLevel: Level = LevelTest.START_LEVEL,
    /** Correct answers in a row at [currentLevel]. */
    val correctInARow: Int = 0,
    val answers: List<LevelTestAnswer> = emptyList(),
)

/**
 * Adaptive onboarding staircase (`GAME_RULES.md` §9). Pure; the caller shows each card from [nextCard] (timer:
 * `GameConfig.levelTestTimeLimitSeconds`) and reports the answer with [record].
 */
public object LevelTest {
    public val START_LEVEL: Level = Level.A2
    public const val CORRECT_IN_A_ROW_TO_GO_UP: Int = 2
    public const val MIN_SLOTS: Int = 12
    public const val MAX_SLOTS: Int = 16
    public const val MIN_ASKED: Int = 3
    public const val PASS_PERCENT: Int = 60

    public fun start(seed: Long): LevelTestState = LevelTestState(seed = seed)

    /** Seed purpose of the n-th pick (`GAME_RULES.md` §13). */
    public fun seedPurpose(index: Int): String = "level-test:$index"

    /**
     * Ends after [MIN_SLOTS] answers once the current level has ≥ [MIN_ASKED] asked cards (so it can be judged),
     * always after [MAX_SLOTS], and early when [pool] has no unasked card of the current level ([result] then
     * judges what was answered).
     */
    public fun isFinished(state: LevelTestState, pool: List<Card>): Boolean =
        reachedEnd(state) || nextCard(state, pool) == null

    private fun reachedEnd(state: LevelTestState): Boolean {
        val asked = state.answers.size
        val askedAtCurrent = state.answers.count { it.level == state.currentLevel }
        return asked >= MAX_SLOTS || (asked >= MIN_SLOTS && askedAtCurrent >= MIN_ASKED)
    }

    /**
     * Seeded pick of an unasked card of the current level, preferring letters not used yet (candidates sorted by
     * id, one `nextInt` on the `level-test:<index>` sub-seed). Null when the level has no unasked card.
     */
    public fun nextCard(state: LevelTestState, pool: List<Card>): Card? {
        val asked = state.answers.mapTo(mutableSetOf()) { it.cardId }
        val usedLetters = state.answers.mapTo(mutableSetOf()) { it.letter }
        val candidates = pool
            .filter { it.level == state.currentLevel && it.id !in asked }
            .sortedBy { it.id.value }
        if (candidates.isEmpty()) return null
        val fresh = candidates.filter { it.letter !in usedLetters }
        val random = SeededRandom(state.seed).derive(seedPurpose(state.answers.size))
        return random.pick(fresh.ifEmpty { candidates })
    }

    /** 2 correct in a row at a level ⇒ one level up; a wrong answer or pass ⇒ one level down (A1…NAT). */
    public fun record(state: LevelTestState, card: Card, correct: Boolean): LevelTestState {
        require(!reachedEnd(state)) { "the level test is finished" }
        require(card.level == state.currentLevel) {
            "card ${card.id} is ${card.level}, the test is at ${state.currentLevel}"
        }
        val answers = state.answers + LevelTestAnswer(card.id, state.currentLevel, card.letter, correct)
        val levels = Level.entries
        val index = state.currentLevel.ordinal
        return when {
            !correct -> state.copy(currentLevel = levels[maxOf(index - 1, 0)], correctInARow = 0, answers = answers)

            state.correctInARow + 1 >= CORRECT_IN_A_ROW_TO_GO_UP ->
                state.copy(
                    currentLevel = levels[minOf(index + 1, levels.lastIndex)],
                    correctInARow = 0,
                    answers = answers,
                )

            else -> state.copy(correctInARow = state.correctInARow + 1, answers = answers)
        }
    }

    /**
     * Highest level with ≥ [PASS_PERCENT] % correct among ≥ [MIN_ASKED] asked cards; if no level qualifies, the
     * level below [START_LEVEL] (A1).
     */
    public fun result(state: LevelTestState): Level {
        val qualifying = state.answers.groupBy { it.level }.filter { (_, asked) ->
            asked.size >= MIN_ASKED && asked.count { it.correct } * 100 >= PASS_PERCENT * asked.size
        }.keys
        return qualifying.maxOrNull() ?: Level.entries[maxOf(START_LEVEL.ordinal - 1, 0)]
    }
}
