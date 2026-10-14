package dev.talhabayhan.lexi.core.domain.random

/**
 * Deterministic SplitMix64 generator (Steele, Lea & Flood 2014; reference `splitmix64.c` by S. Vigna).
 *
 * Own implementation so the sequence never depends on `kotlin.random` internals or the platform: only 64-bit
 * integer arithmetic, which wraps identically on JVM and Kotlin/Native. Same seed ⇒ same values on every device.
 *
 * Instances are mutable streams; create them from a seed inside a pure function and never share them across
 * purposes. Use [derive] (or [Seeds.derive]) for independent sub-streams (`GAME_RULES.md` § Randomness).
 */
public class SeededRandom(
    /** The seed this stream was created with; [derive] is based on it, not on the current position. */
    public val seed: Long,
) {
    private var state: Long = seed

    /** Next 64 uniformly distributed bits. */
    public fun nextLong(): Long {
        state += GOLDEN_GAMMA
        return Seeds.mix64(state)
    }

    /** Next 32 uniformly distributed bits (upper half of [nextLong]). */
    public fun nextInt(): Int = (nextLong() ushr 32).toInt()

    /**
     * Uniform value in `[0, bound)` without modulo bias: draws 31 bits and rejects the incomplete last block
     * (same algorithm as `java.util.Random.nextInt(bound)`, but on SplitMix64 output).
     */
    public fun nextInt(bound: Int): Int {
        require(bound > 0) { "bound must be positive, was $bound" }
        val mask = bound - 1
        var bits = next31()
        if (bound and mask == 0) {
            // Power of two: take the high bits.
            return ((bound.toLong() * bits) shr 31).toInt()
        }
        var value = bits % bound
        // `bits - value + mask` overflows (wraps negative) exactly when bits falls into the incomplete last block.
        while (bits - value + mask < 0) {
            bits = next31()
            value = bits % bound
        }
        return value
    }

    /** Uniform value in `[from, until)`. */
    public fun nextInt(from: Int, until: Int): Int {
        require(from < until) { "empty range [$from, $until)" }
        val span = until.toLong() - from
        require(span <= Int.MAX_VALUE) { "range [$from, $until) is too large" }
        return from + nextInt(span.toInt())
    }

    /** Uniformly chosen element of [items]. */
    public fun <T> pick(items: List<T>): T {
        require(items.isNotEmpty()) { "cannot pick from an empty list" }
        return items[nextInt(items.size)]
    }

    /** Fisher–Yates shuffle in place, from the last index down (`j = nextInt(i + 1)`). */
    public fun <T> shuffle(items: MutableList<T>) {
        for (i in items.lastIndex downTo 1) {
            val j = nextInt(i + 1)
            val tmp = items[i]
            items[i] = items[j]
            items[j] = tmp
        }
    }

    /** Shuffled copy of [items]; the input is not modified. */
    public fun <T> shuffled(items: List<T>): List<T> = items.toMutableList().also(::shuffle)

    /** Independent stream for [purpose], derived from [seed] (not from the current position). */
    public fun derive(purpose: String): SeededRandom = SeededRandom(Seeds.derive(seed, purpose))

    private fun next31(): Int = (nextLong() ushr 33).toInt()

    private companion object {
        const val GOLDEN_GAMMA: Long = -0x61C8864680B583EBL // 0x9E3779B97F4A7C15
    }
}
