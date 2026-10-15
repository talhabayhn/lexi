package dev.talhabayhan.lexi.tools.contentcompiler

import java.text.Normalizer
import java.util.Locale

/** Case- and diacritic-insensitive word matching for answers, clues and blocklist terms (`CONTENT.md` §3). */
object TextMatch {
    private val MARKS = Regex("\\p{Mn}+")
    private val SPACES = Regex("\\s+")

    /** Lowercase, strip diacritics (`ş`→`s`, `İ`/`ı`→`i`, `é`→`e`), collapse whitespace. */
    fun normalize(text: String): String = Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(MARKS, "")
        .replace('ı', 'i')
        .replace(SPACES, " ")

    /** [phrase] occurs in [text] as whole word(s). */
    fun containsWords(text: String, phrase: String): Boolean =
        pattern(phrase, wholeWord = true).containsMatchIn(normalize(text))

    /** [phrase] occurs in [text] at the start of a word (`zebra` matches `zebralar`). */
    fun containsWordStart(text: String, phrase: String): Boolean =
        pattern(phrase, wholeWord = false).containsMatchIn(normalize(text))

    private fun pattern(phrase: String, wholeWord: Boolean): Regex {
        val words = normalize(phrase).trim().split(' ').joinToString("\\s+") { Regex.escape(it) }
        val end = if (wholeWord) "(?![\\p{L}\\p{N}])" else ""
        return Regex("(?<![\\p{L}\\p{N}])$words$end")
    }
}
