package dev.talhabayhan.lexi.tools.contentcompiler

import dev.talhabayhan.lexi.core.domain.model.AnswerRules
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import java.util.Locale

/** Per-row card rules from `CONTENT.md` §2–§3. Answer and letter rules are the shared ones from `core:domain`. */
object CardRules {
    val COLUMNS = "id,answer,letter,rule,pos,clue_tr,example_en,example_tr,difficulty,tags,category".split(',')
    const val MAX_CLUE_LENGTH = 60

    /** Below this many letters the clue check needs a whole-word match (short answers hide inside Turkish words). */
    const val CLUE_WORD_START_MIN_LETTERS = 4

    val SLUG = Regex("[a-z0-9]+(-[a-z0-9]+)*")
    private val TAG = Regex("[a-z0-9]+([-:][a-z0-9]+)*")
    private val POS = PartOfSpeech.entries.map { it.name.lowercase(Locale.ROOT) }.toSet()

    /** Validates one row and returns the card, or null when it has errors. */
    fun parse(
        row: CsvRow,
        level: Level,
        fromPack: Boolean,
        categories: Map<String, CategoryEntry>,
        blocklist: Blocklist,
        diagnostics: Diagnostics,
    ): CardEntry? {
        var ok = true
        fun error(message: String) {
            ok = false
            diagnostics.error(row.file, row.line, message)
        }
        fun warning(message: String) = diagnostics.warning(row.file, row.line, message)

        val id = row["id"]
        val levelPrefix = level.name.lowercase(Locale.ROOT) + "-"
        when {
            !SLUG.matches(id) -> error("id '$id' must be a lowercase slug (a-z, 0-9, single dashes)")
            !id.startsWith(levelPrefix) -> error("id '$id' must start with '$levelPrefix' (level folder ${level.name})")
        }

        val answer = row["answer"]
        val answerValid = AnswerRules.isValid(answer)
        if (!answerValid) {
            error(
                "answer '$answer' must be uppercase A-Z words separated by single spaces, " +
                    "${AnswerRules.MIN_LETTERS}-${AnswerRules.MAX_LETTERS} letters",
            )
        } else if (SLUG.matches(id) && id.startsWith(levelPrefix)) {
            val expected = levelPrefix + answer.lowercase(Locale.ROOT).replace(' ', '-')
            if (id != expected && !Regex(Regex.escape(expected) + "-\\d+").matches(id)) {
                warning(
                    "id '$id' does not follow <level>-<answer>[-n] ('$expected'); fine if the answer was corrected later",
                )
            }
        }

        val letterText = row["letter"]
        val letter = letterText.singleOrNull()?.takeIf { it in 'A'..'Z' }
        if (letter == null) error("letter '$letterText' must be a single uppercase A-Z letter")

        val rule = LetterRule.entries.firstOrNull { it.name == row["rule"] }
        if (rule == null) error("rule '${row["rule"]}' must be STARTS or CONTAINS")

        if (answerValid && letter != null && rule != null && !rule.isSatisfiedBy(answer, letter)) {
            when {
                rule == LetterRule.STARTS -> error("answer '$answer' does not start with '$letter' (rule STARTS)")

                answer.first() == letter ->
                    error("answer '$answer' starts with '$letter' (CONTAINS needs the letter after the first position)")

                else -> error("answer '$answer' does not contain '$letter' (rule CONTAINS)")
            }
        }

        val pos = row["pos"]
        if (pos !in POS) error("pos '$pos' must be one of ${POS.joinToString("/")}")

        val clue = row["clue_tr"]
        when {
            clue.isEmpty() -> error("clue_tr must not be empty")

            clue.length > MAX_CLUE_LENGTH -> error(
                "clue_tr is longer than $MAX_CLUE_LENGTH characters (${clue.length})",
            )
        }
        if (answerValid && clue.isNotEmpty()) {
            val leaks = if (AnswerRules.letterCount(answer) >= CLUE_WORD_START_MIN_LETTERS) {
                TextMatch.containsWordStart(clue, answer)
            } else {
                TextMatch.containsWords(clue, answer)
            }
            if (leaks) error("clue_tr contains the answer '$answer'")
        }

        val exampleEn = row["example_en"]
        if (exampleEn.isEmpty()) {
            error("example_en must not be empty")
        } else if (answerValid && !TextMatch.containsWords(exampleEn, answer)) {
            error("example_en must contain the answer '$answer' as written (the Example joker blanks it)")
        }
        val exampleTr = row["example_tr"]
        if (exampleTr.isEmpty()) error("example_tr must not be empty")

        val difficulty = row["difficulty"].toIntOrNull()?.takeIf { it in 1..5 }
        if (difficulty == null) error("difficulty '${row["difficulty"]}' must be an integer 1-5")

        val tags = row["tags"].takeIf { it.isNotEmpty() }?.split('|').orEmpty()
        tags.filterNot {
            TAG.matches(it)
        }.forEach { error("tag '$it' must be lowercase a-z/0-9 with - or : (e.g. exam:lgs)") }
        tags.groupBy { it }.filterValues { it.size > 1 }.keys.forEach { warning("tag '$it' is repeated") }

        val categoryId = row["category"]
        val category = categories[categoryId]
        when {
            categoryId == CategoryId.MIXED.value -> error(
                "virtual category 'mixed' cannot hold cards; use a real category",
            )

            category == null -> error("unknown category '$categoryId' (see categories.csv)")

            level !in category.levels -> warning(
                "category '$categoryId' is not listed for ${level.name} in categories.csv",
            )
        }

        blocklist.check(row, diagnostics).also { if (!it) ok = false }

        if (!ok || letter == null || rule == null || difficulty == null) return null
        return CardEntry(
            id = id,
            level = level,
            categoryId = categoryId,
            answer = answer,
            letter = letter,
            rule = rule.name,
            pos = pos,
            clueTr = clue,
            exampleEn = exampleEn,
            exampleTr = exampleTr,
            difficulty = difficulty,
            tags = tags,
            fromPack = fromPack,
            source = Source(row.file, row.line),
        )
    }
}
