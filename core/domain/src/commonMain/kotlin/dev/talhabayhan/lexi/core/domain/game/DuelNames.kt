package dev.talhabayhan.lexi.core.domain.game

/** Duel player names (`GAME_RULES.md` §8): free text, trimmed, at most `GameConfig.duelNameMaxLength` characters. */
public object DuelNames {
    /**
     * [raw] trimmed, line breaks and tabs turned into spaces, cut to [maxLength] characters. "" means the default name
     * ("1. Oyuncu").
     */
    public fun sanitize(raw: String, maxLength: Int): String = cut(raw.trim(), maxLength).trim()

    /**
     * [raw] as typed (only line breaks and tabs become spaces) cut to [maxLength] characters without splitting a
     * surrogate pair: an emoji counts as one character.
     */
    public fun cut(raw: String, maxLength: Int): String {
        val out = StringBuilder()
        var count = 0
        var i = 0
        while (i < raw.length && count < maxLength) {
            val pair = raw[i].isHighSurrogate() && i + 1 < raw.length && raw[i + 1].isLowSurrogate()
            val step = if (pair) 2 else 1
            if (step == 1 && raw[i].isWhitespace()) out.append(' ') else out.append(raw, i, i + step)
            i += step
            count++
        }
        return out.toString()
    }
}
