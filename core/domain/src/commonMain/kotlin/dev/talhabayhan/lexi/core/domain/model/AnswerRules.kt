package dev.talhabayhan.lexi.core.domain.model

/** Answer format shared by cards, the keyboard and `tools:content-compiler` (`CONTENT.md` §2). */
public object AnswerRules {
    public const val MIN_LETTERS: Int = 2
    public const val MAX_LETTERS: Int = 14

    private val PATTERN = Regex("[A-Z]+( [A-Z]+)*")

    /** Letters to type; spaces are fixed gaps. */
    public fun letterCount(answer: String): Int = answer.count { it != ' ' }

    /** Uppercase A–Z words separated by single spaces, with [MIN_LETTERS]–[MAX_LETTERS] letters. */
    public fun isValid(answer: String): Boolean =
        PATTERN.matches(answer) && letterCount(answer) in MIN_LETTERS..MAX_LETTERS
}
