package dev.talhabayhan.lexi.tools.contentcompiler

import java.text.Normalizer
import java.util.Locale

/**
 * Word matching for answers, clues and blocklist terms (`CONTENT.md` §3): [normalize] is case- and
 * diacritic-insensitive (answer leaks, English terms); [normalizeTurkish] keeps diacritics because they change
 * the meaning in Turkish (`sık`/`şık`/`sik` are different words).
 */
object TextMatch {
    private val TURKISH = Locale.forLanguageTag("tr")
    private val MARKS = Regex("\\p{Mn}+")
    private val SPACES = Regex("\\s+")

    /** Lowercase, strip diacritics (`ş`→`s`, `İ`/`ı`→`i`, `é`→`e`), collapse whitespace. */
    fun normalize(text: String): String = Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(MARKS, "")
        .replace('ı', 'i')
        .replace(SPACES, " ")

    /** Turkish case folding (`İ`→`i`, `I`→`ı`), diacritics kept, whitespace collapsed. */
    fun normalizeTurkish(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFC).lowercase(TURKISH).replace(SPACES, " ")

    /** [phrase] occurs in [text] as whole word(s). */
    fun containsWords(text: String, phrase: String): Boolean =
        pattern(phrase, wholeWord = true).containsMatchIn(normalize(text))

    /** Like [containsWords], but with [normalizeTurkish] (Turkish blocklist terms). */
    fun containsTurkishWords(text: String, phrase: String): Boolean =
        pattern(phrase, wholeWord = true, fold = ::normalizeTurkish).containsMatchIn(normalizeTurkish(text))

    /** [phrase] occurs in [text] at the start of a word (`zebra` matches `zebralar`). */
    fun containsWordStart(text: String, phrase: String): Boolean =
        pattern(phrase, wholeWord = false).containsMatchIn(normalize(text))

    private fun pattern(phrase: String, wholeWord: Boolean, fold: (String) -> String = ::normalize): Regex {
        val words = fold(phrase).trim().split(' ').joinToString("\\s+") { Regex.escape(it) }
        val end = if (wholeWord) "(?![\\p{L}\\p{N}])" else ""
        return Regex("(?<![\\p{L}\\p{N}])$words$end")
    }
}
