package dev.talhabayhan.lexi.core.domain.stats

import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import kotlinx.serialization.Serializable

/** What statistics need of a played session (`session` table); [epochDay] is in the player's time zone. */
@Serializable
public data class SessionRecord(val epochDay: Long, val completed: Boolean, val correct: Int)

/** [percent] is rounded half up; null when nothing was answered. */
public data class Accuracy(val correct: Int, val answered: Int) {
    public val percent: Int? get() = percentOrNull(correct, answered)
}

public data class DayCount(val epochDay: Long, val words: Int)

public data class LevelProgress(val learned: Int, val total: Int)

public data class LetterAccuracy(val letter: Char, val correct: Int, val attempts: Int) {
    public val percent: Int? get() = percentOrNull(correct, attempts)
}

/** Statistics screen calculators (`GAME_RULES.md` §11). Pure; "today" is a parameter. */
public object Stats {
    public const val MIN_LETTER_ATTEMPTS: Int = 5
    public const val WORDS_PER_DAY_DAYS: Int = 7

    /** Cards ever answered correctly. */
    public fun learnedWords(progress: Collection<CardProgress>): Int = progress.count { it.correct > 0 }

    /** Correct / answered (passes are not answers). */
    public fun accuracy(progress: Collection<CardProgress>): Accuracy {
        val correct = progress.sumOf { it.correct }
        return Accuracy(correct = correct, answered = correct + progress.sumOf { it.wrong })
    }

    /** Longest run of consecutive days with ≥ 1 completed wheel. */
    public fun longestStreak(sessions: Collection<SessionRecord>): Int {
        val days = sessions.filter { it.completed }.map { it.epochDay }.distinct().sorted()
        var longest = 0
        var run = 0
        var previous: Long? = null
        for (day in days) {
            run = if (previous != null && day == previous + 1) run + 1 else 1
            longest = maxOf(longest, run)
            previous = day
        }
        return longest
    }

    /**
     * Length of the run of consecutive [days] (epoch days with a completed wheel, any order) that ends today. A run
     * ending yesterday still counts: today's wheel is not played yet and the streak is not lost until the day is over.
     */
    public fun currentStreak(days: Collection<Long>, today: Long): Int {
        val played = days.toSet()
        var day = if (today in played) today else today - 1
        var length = 0
        while (day in played) {
            length++
            day--
        }
        return length
    }

    public fun completedWheels(sessions: Collection<SessionRecord>): Int = sessions.count { it.completed }

    /** Correct answers per day for the last [days] days ending today, oldest first (completed or not). */
    public fun wordsPerDay(
        sessions: Collection<SessionRecord>,
        todayEpochDay: Long,
        days: Int = WORDS_PER_DAY_DAYS,
    ): List<DayCount> {
        val byDay = sessions.groupBy { it.epochDay }.mapValues { (_, list) -> list.sumOf { it.correct } }
        return (todayEpochDay - days + 1..todayEpochDay).map { DayCount(it, byDay[it] ?: 0) }
    }

    /** Learned cards per level against the level's total card count, for every level in [totals]. */
    public fun levelProgress(
        progress: Collection<CardProgress>,
        levelOf: Map<CardId, Level>,
        totals: Map<Level, Int>,
    ): Map<Level, LevelProgress> {
        val learned = progress.filter { it.correct > 0 }.mapNotNull { levelOf[it.cardId] }.groupingBy { it }.eachCount()
        return totals.mapValues { (level, total) -> LevelProgress(learned = learned[level] ?: 0, total = total) }
    }

    /** Letters with ≥ [minAttempts] answers, lowest accuracy first (ties by letter). */
    public fun hardestLetters(
        progress: Collection<CardProgress>,
        letterOf: Map<CardId, Char>,
        minAttempts: Int = MIN_LETTER_ATTEMPTS,
    ): List<LetterAccuracy> = progress
        .mapNotNull { card -> letterOf[card.cardId]?.let { it to card } }
        .groupBy({ it.first }, { it.second })
        .map { (letter, cards) ->
            val correct = cards.sumOf { it.correct }
            LetterAccuracy(letter, correct = correct, attempts = correct + cards.sumOf { it.wrong })
        }
        .filter { it.attempts >= minAttempts }
        // Exact comparison of correct/attempts without floating point: a/b < c/d ⇔ a·d < c·b.
        .sortedWith { a, b ->
            val byAccuracy = (a.correct.toLong() * b.attempts).compareTo(b.correct.toLong() * a.attempts)
            if (byAccuracy != 0) byAccuracy else a.letter.compareTo(b.letter)
        }
}

private fun percentOrNull(part: Int, whole: Int): Int? = if (whole == 0) null else (part * 200 + whole) / (2 * whole)
