package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom

/**
 * The player's card history for a solo wheel (`GAME_RULES.md` §1): `lastSeenMs` per card (`ProgressRepository`) and
 * the time the wheel is built (passed in, never read from a clock).
 */
public data class CardHistory(val lastSeenMs: Map<CardId, Long>, val nowMs: Long) {
    public companion object {
        /** No history: every card counts as never seen. */
        public val NONE: CardHistory = CardHistory(emptyMap(), 0L)
    }
}

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
     * [CategoryId.MIXED]) is picked with [random]. When [mode] uses card history, [history] steers the pick
     * ([pickFresh]); otherwise it is ignored. Letters without any card become [SlotState.SKIPPED] slots. Exactly one
     * `nextInt` is drawn per non-skipped letter, in A–Z order.
     */
    public fun generate(
        pool: List<Card>,
        level: Level,
        category: CategoryId,
        mode: GameMode,
        random: SeededRandom,
        history: CardHistory = CardHistory.NONE,
        config: GameConfig = GameConfig(),
    ): Wheel {
        require(mode != GameMode.REVIEW) { "use review() for the review wheel" }
        val used = if (mode.usesCardHistory) history else CardHistory.NONE
        val cards = pool.filter { it.level == level && (category == CategoryId.MIXED || it.category == category) }
        return alphabetWheel(cards, random, used, config)
    }

    /**
     * Exam pack wheel (`GAME_RULES.md` §1): 26 slots A–Z over every card of the pack, whatever its level or
     * category, picked exactly like [generate] ([pickFresh], one `nextInt` per non-skipped letter in A–Z order).
     * Letters without a pack card become [SlotState.SKIPPED]. The pack is played with Classic rules, so [history]
     * always applies.
     */
    public fun pack(
        pool: List<Card>,
        random: SeededRandom,
        history: CardHistory = CardHistory.NONE,
        config: GameConfig = GameConfig(),
    ): Wheel = alphabetWheel(pool, random, history, config)

    private fun alphabetWheel(
        cards: List<Card>,
        random: SeededRandom,
        history: CardHistory,
        config: GameConfig,
    ): Wheel {
        val byLetter = cards.candidatesByLetter()
        val recent = recentlySeen(history.lastSeenMs, history.nowMs, config)
        // A word already on the wheel is not shown again. Safety net only: content validation forbids a repeated
        // answer within a level or a pack.
        val usedAnswers = mutableSetOf<String>()
        val slots = ('A'..'Z').map { letter ->
            val candidates = byLetter[letter].orEmpty()
            if (candidates.isEmpty()) {
                Slot(letter, card = null, state = SlotState.SKIPPED)
            } else {
                val fresh = candidates.filter { it.answer !in usedAnswers }.ifEmpty { candidates }
                val card = random.pick(fresh.pickFresh(history.lastSeenMs, recent, config))
                usedAnswers += card.answer
                Slot(letter, card, SlotState.TODO)
            }
        }
        return Wheel(slots)
    }

    /**
     * The candidates of one letter that the seeded pick chooses from (`GAME_RULES.md` §1), from a list sorted by id:
     * 1. eligible = not [recent] (seen within [GameConfig.recentlySeenWindowDays]), or all when that leaves none;
     * 2. STARTS preferred among the eligible ([preferStarts], the CONTAINS fallback);
     * 3. of those, the never-seen cards if any, else the [GameConfig.leastRecentlySeenCandidates] cards seen longest
     *    ago (ties by id), never the most recently seen one while there are others (no back-to-back repeats).
     * With no history this is exactly the step-2 list, so history-free wheels pick as before.
     */
    internal fun List<Card>.pickFresh(
        lastSeenMs: Map<CardId, Long>,
        recent: Set<CardId>,
        config: GameConfig,
    ): List<Card> {
        val eligible = filter { it.id !in recent }.ifEmpty { this }.preferStarts(config)
        val neverSeen = eligible.filter { it.id !in lastSeenMs }
        if (neverSeen.isNotEmpty()) return neverSeen
        // Stable sort: equal times keep the id order.
        val count = minOf(config.leastRecentlySeenCandidates, eligible.size - 1).coerceAtLeast(1)
        return eligible.sortedBy { lastSeenMs.getValue(it.id) }.take(count)
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
