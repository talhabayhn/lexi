package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom

/**
 * Builds wheels from a card pool (`GAME_RULES.md` §1). Pure: the result depends only on the arguments, and the
 * pool order does not matter (candidates are sorted by card id before the seeded pick).
 *
 * Pass `SeededRandom(seed).derive(SEED_PURPOSE)` as `random` (`GAME_RULES.md` §13).
 */
public object WheelGenerator {
    public const val SEED_PURPOSE: String = "wheel"

    /**
     * Regular 26-slot wheel: for each letter A–Z one card of [level] (and [category], unless
     * [CategoryId.MIXED]) is picked with [random]. Cards in [recentlySeen] are avoided when the letter has other
     * cards and [mode] uses card history; letters without any card become [SlotState.SKIPPED] slots.
     * Exactly one `nextInt` is drawn per non-skipped letter, in A–Z order.
     */
    public fun generate(
        pool: List<Card>,
        level: Level,
        category: CategoryId,
        mode: GameMode,
        random: SeededRandom,
        recentlySeen: Set<CardId> = emptySet(),
    ): Wheel {
        require(mode != GameMode.REVIEW) { "use review() for the review wheel" }
        val avoid = if (mode.usesCardHistory) recentlySeen else emptySet()
        val byLetter = pool
            .filter { it.level == level && (category == CategoryId.MIXED || it.category == category) }
            .candidatesByLetter()
        val slots = ('A'..'Z').map { letter ->
            val candidates = byLetter[letter].orEmpty()
            if (candidates.isEmpty()) {
                Slot(letter, card = null, state = SlotState.SKIPPED)
            } else {
                val unseen = candidates.filter { it.id !in avoid }
                Slot(letter, random.pick(unseen.ifEmpty { candidates }), SlotState.TODO)
            }
        }
        return Wheel(slots)
    }

    /**
     * Review wheel from SRS-due cards (any level/category): only letters that have due cards, A–Z order, one card
     * per letter picked with [random]. Empty when nothing is due.
     */
    public fun review(dueCards: List<Card>, random: SeededRandom): Wheel {
        val byLetter = dueCards.candidatesByLetter()
        val slots = byLetter.keys.sorted().map { letter ->
            Slot(letter, random.pick(byLetter.getValue(letter)), SlotState.TODO)
        }
        return Wheel(slots)
    }

    /**
     * Card ids last seen less than [GameConfig.recentlySeenWindowDays] before [nowMs] (time is passed in, never
     * read from a clock).
     */
    public fun recentlySeen(lastSeenMs: Map<CardId, Long>, nowMs: Long, config: GameConfig): Set<CardId> {
        val windowMs = config.recentlySeenWindowDays * MS_PER_DAY
        return lastSeenMs.filterValues { nowMs - it < windowMs }.keys
    }

    private const val MS_PER_DAY: Long = 86_400_000L

    /** Cards grouped by wheel letter, each group sorted by (unique) id so the pool order never matters. */
    private fun List<Card>.candidatesByLetter(): Map<Char, List<Card>> {
        require(distinctBy { it.id }.size == size) { "card ids in a pool must be unique" }
        return sortedBy { it.id.value }.groupBy { it.letter }
    }
}
