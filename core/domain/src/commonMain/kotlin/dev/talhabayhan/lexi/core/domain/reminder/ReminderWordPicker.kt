package dev.talhabayhan.lexi.core.domain.reminder

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.random.SeededRandom

/**
 * Chooses the words that the next reminders teach ("kitchen — mutfak"). Pure and deterministic: the same ids and seed
 * give the same words on every device. Due notebook words come first, then the other answered words; no word appears
 * twice. OS notifications have fixed text, so [RescheduleReminders] picks one word per reminder of the coming week.
 */
public object ReminderWordPicker {
    /** Reminders that show a word are scheduled for today .. today + 6. */
    public const val WINDOW_DAYS: Int = 7

    /** Longest meaning shown in a notification; longer ones end with an ellipsis. */
    public const val MAX_MEANING_CHARS: Int = 48

    /** Extra candidates drawn so skipped (sensitive, removed) cards can be replaced. */
    public const val SPARE_CANDIDATES: Int = 8

    /** Content tags that keep a card out of notifications, which show on the lock screen. */
    public val EXCLUDED_TAGS: Set<String> = setOf("sensitive", "mature")

    private const val ELLIPSIS = "…"

    /** Up to [count] distinct ids: [due] (shuffled) first, then [answered] without the due ones (shuffled). */
    public fun pick(due: Collection<CardId>, answered: Collection<CardId>, count: Int, seed: Long): List<CardId> {
        if (count <= 0) return emptyList()
        val rng = SeededRandom(seed)
        val dueSorted = due.distinct().sortedBy { it.value }
        val dueSet = dueSorted.toSet()
        val rest = answered.distinct().filter { it !in dueSet }.sortedBy { it.value }
        return (rng.derive("due").shuffled(dueSorted) + rng.derive("answered").shuffled(rest)).take(count)
    }

    /** A card may appear in a notification: no excluded tag, a real answer and clue. */
    public fun isShowable(card: Card): Boolean =
        card.tags.none { it in EXCLUDED_TAGS } && card.answer.isNotBlank() && card.clue.isNotBlank()

    /** The English answer as written in the notification: "KITCHEN" ⇒ "kitchen". */
    public fun displayWord(answer: String): String = answer.trim().lowercase()

    /** The clue (Turkish meaning), trimmed and cut to [MAX_MEANING_CHARS]. */
    public fun displayMeaning(clue: String): String {
        val text = clue.trim()
        return if (text.length <= MAX_MEANING_CHARS) text else text.take(MAX_MEANING_CHARS - 1).trimEnd() + ELLIPSIS
    }
}
