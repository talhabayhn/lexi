package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.AnswerRules
import dev.talhabayhan.lexi.core.domain.random.SeededRandom

/**
 * Builds the letter keys of one slot (`GAME_RULES.md` §4). Pure and deterministic; pass
 * `SeededRandom(seed).derive(seedPurpose(letter))` as `random` (`GAME_RULES.md` §13).
 *
 * Algorithm (the order of draws is part of the contract):
 * 1. Answer letters = answer without spaces, in order; key count = [keyCount].
 * 2. Candidates = A–Z letters not in the answer, in A–Z order (distractors are distinct).
 * 3. Draw `min(2, distractors needed, near candidates)` from the candidates that are [nearLetters] of an answer
 *    letter, then the rest from the remaining candidates — both weighted by English letter frequency, without
 *    replacement (`nextInt(totalWeight)` over cumulative weights in A–Z order).
 * 4. Keys = answer letters + distractors, then Fisher–Yates shuffled with the same [SeededRandom].
 */
public object KeyboardGenerator {
    public const val COLUMNS: Int = 5
    public const val MIN_KEYS: Int = 10
    public const val MAX_KEYS: Int = 15
    public const val MIN_NEAR_DISTRACTORS: Int = 2

    public fun seedPurpose(letter: Char): String = "keys:$letter"

    /** `max(10, ceil(answerLetters / 5) * 5)`, capped at 15 (2 or 3 rows of 5). */
    public fun keyCount(answerLetters: Int): Int {
        require(answerLetters in AnswerRules.MIN_LETTERS..AnswerRules.MAX_LETTERS) {
            "answer must have ${AnswerRules.MIN_LETTERS}-${AnswerRules.MAX_LETTERS} letters, has $answerLetters"
        }
        val fullRows = (answerLetters + COLUMNS - 1) / COLUMNS * COLUMNS
        return fullRows.coerceIn(MIN_KEYS, MAX_KEYS)
    }

    public fun generate(answer: String, random: SeededRandom): List<LetterKey> {
        require(AnswerRules.isValid(answer)) { "answer '$answer' is not a valid card answer" }
        val letters = answer.filter { it != ' ' }.toList()
        val needed = keyCount(letters.size) - letters.size
        val candidates = ('A'..'Z').filter { it !in letters }
        val near = letters.flatMapTo(mutableSetOf()) { nearLetters(it) }
        val nearCandidates = candidates.filter { it in near }
        val nearCount = minOf(MIN_NEAR_DISTRACTORS, needed, nearCandidates.size)
        val nearPicks = random.weightedDistinct(nearCandidates, nearCount)
        val restPicks = random.weightedDistinct(candidates - nearPicks.toSet(), needed - nearPicks.size)
        val keys = letters.map { LetterKey(it, isDistractor = false) } +
            (nearPicks + restPicks).map { LetterKey(it, isDistractor = true) }
        return random.shuffled(keys)
    }

    /** Visually or phonetically similar letters (symmetric), e.g. C/K, I/E, V/W. */
    public fun nearLetters(letter: Char): Set<Char> = NEAR[letter].orEmpty()

    private fun SeededRandom.weightedDistinct(pool: List<Char>, count: Int): List<Char> {
        val remaining = pool.toMutableList()
        return List(count) {
            var ticket = nextInt(remaining.sumOf(::frequency))
            val index = remaining.indexOfFirst { letter ->
                ticket -= frequency(letter)
                ticket < 0
            }
            remaining.removeAt(index)
        }
    }

    private fun frequency(letter: Char): Int = ENGLISH_FREQUENCY[letter - 'A']

    private val NEAR_PAIRS = listOf(
        "CK", "CS", "CG", "KQ", "QG", "KX", "IE", "EY", "IY", "EA", "AO", "OU", "OQ", "UV", "VW", "UW", "WM",
        "MN", "NH", "BP", "BD", "DP", "DT", "TF", "FV", "FP", "GJ", "SZ", "XZ", "LI", "LR", "RP", "JY",
    )

    private val NEAR: Map<Char, Set<Char>> = NEAR_PAIRS
        .flatMap { listOf(it[0] to it[1], it[1] to it[0]) }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, partners) -> partners.toSet() }

    /** English letter frequency A–Z in hundredths of a percent (E = 12.70 %). */
    private val ENGLISH_FREQUENCY = intArrayOf(
        817, 149, 278, 425, 1270, 223, 202, 609, 697, 15, 77, 403, 241,
        675, 751, 193, 10, 599, 633, 906, 276, 98, 236, 15, 197, 7,
    )
}
