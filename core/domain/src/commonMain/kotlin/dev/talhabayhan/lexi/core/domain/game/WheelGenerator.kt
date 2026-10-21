package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
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
     * cards and [mode] uses card history; of the remaining cards, STARTS cards are preferred ([preferStarts]).
     * Letters without any card become [SlotState.SKIPPED] slots. Exactly one `nextInt` is drawn per non-skipped
     * letter, in A–Z order.
     */
    public fun generate(
        pool: List<Card>,
        level: Level,
        category: CategoryId,
        mode: GameMode,
        random: SeededRandom,
        recentlySeen: Set<CardId> = emptySet(),
        config: GameConfig = GameConfig(),
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
                Slot(letter, random.pick(unseen.ifEmpty { candidates }.preferStarts(config)), SlotState.TODO)
            }
        }
        return Wheel(slots)
    }

    /**
     * Fixed distribution of card levels across the 26 letters for Daily mode (`GAME_RULES.md` §5).
     * Easy to tune named constant: A1 x6, A2 x5, B1 x5, B2 x4, C1 x3, NAT x3.
     */
    public val DAILY_LEVEL_DISTRIBUTION: Map<Level, Int> = mapOf(
        Level.A1 to 6,
        Level.A2 to 5,
        Level.B1 to 5,
        Level.B2 to 4,
        Level.C1 to 3,
        Level.NAT to 3,
    )

    /**
     * Shared mixed-level 26-slot Daily wheel:
     * - Deterministically assigns a level to each letter A–Z by shuffling [distribution] with [random].
     * - For each letter, picks one card from that level's candidates in [pool] using [random], preferring STARTS
     *   cards ([preferStarts]).
     * - Ignores history so every player receives the identical wheel for the same date + contentVersion.
     */
    public fun daily(
        pool: List<Card>,
        random: SeededRandom,
        distribution: Map<Level, Int> = DAILY_LEVEL_DISTRIBUTION,
        config: GameConfig = GameConfig(),
    ): Wheel {
        require(pool.distinctBy { it.id }.size == pool.size) { "card ids in a pool must be unique" }
        require(distribution.values.sum() == 26) {
            "distribution must sum to 26 slots, was ${distribution.values.sum()}"
        }
        val levels = distribution.flatMap { (level, count) -> List(count) { level } }
        val assignedLevels = random.shuffled(levels)
        val byLevelAndLetter = pool
            .sortedBy { it.id.value }
            .groupBy { it.level to it.letter }

        val slots = ('A'..'Z').mapIndexed { index, letter ->
            val assignedLevel = assignedLevels[index]
            val candidates = byLevelAndLetter[assignedLevel to letter].orEmpty()
            if (candidates.isEmpty()) {
                Slot(letter, card = null, state = SlotState.SKIPPED)
            } else {
                Slot(letter, random.pick(candidates.preferStarts(config)), SlotState.TODO)
            }
        }
        return Wheel(slots)
    }

    /**
     * Review wheel from SRS-due cards (any level/category): only letters that have due cards, A–Z order, one card
     * per letter picked with [random], preferring STARTS cards ([preferStarts]). Empty when nothing is due.
     */
    public fun review(dueCards: List<Card>, random: SeededRandom, config: GameConfig = GameConfig()): Wheel {
        val byLetter = dueCards.candidatesByLetter()
        val slots = byLetter.keys.sorted().map { letter ->
            Slot(letter, random.pick(byLetter.getValue(letter).preferStarts(config)), SlotState.TODO)
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

/**
 * The STARTS cards of eligible candidates when there are at least [GameConfig.minStartsCandidates] of them, else all
 * candidates (CONTAINS only when needed, `GAME_RULES.md` §1). Keeps the order, so a pick stays one `nextInt` on the
 * same sorted list.
 */
internal fun List<Card>.preferStarts(config: GameConfig): List<Card> {
    val starts = filter { it.rule == LetterRule.STARTS }
    return if (starts.size >= config.minStartsCandidates) starts else this
}
