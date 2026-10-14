package dev.talhabayhan.lexi.core.domain.random

/**
 * Platform-independent seed helpers. Never use `String.hashCode()` for seeds: it is not specified to be the same
 * on every Kotlin target.
 */
public object Seeds {
    private const val FNV_OFFSET_BASIS: Long = -0x340D631B7BDDDCDBL // 0xCBF29CE484222325
    private const val FNV_PRIME: Long = 0x100000001B3L

    /** FNV-1a 64-bit hash of the UTF-8 bytes of [text]. Used for sub-seed purposes and the daily seed. */
    public fun stableHash(text: String): Long {
        var hash = FNV_OFFSET_BASIS
        for (byte in text.encodeToByteArray()) {
            hash = (hash xor (byte.toLong() and 0xFF)) * FNV_PRIME
        }
        return hash
    }

    /** Sub-seed for [purpose]: `mix64(seed xor stableHash(purpose))`, e.g. `derive(seed, "keys:K")`. */
    public fun derive(seed: Long, purpose: String): Long = mix64(seed xor stableHash(purpose))

    /** SplitMix64 output finalizer (a bijective 64-bit mixer). */
    public fun mix64(value: Long): Long {
        var z = value
        z = (z xor (z ushr 30)) * -0x40A7B892E31B1A47L // 0xBF58476D1CE4E5B9
        z = (z xor (z ushr 27)) * -0x6B2FB644ECCEEE15L // 0x94D049BB133111EB
        return z xor (z ushr 31)
    }
}
