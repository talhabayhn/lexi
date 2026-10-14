package dev.talhabayhan.lexi.core.domain.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Joker types (`GAME_RULES.md` §7). Each can be used at most once per slot. */
@Serializable
public enum class JokerType {
    /** "Harf aç": puts the correct letter into the next empty answer slot. */
    REVEAL_LETTER,

    /** "Çeldiricileri temizle": disables all distractor keys. */
    REMOVE_DISTRACTORS,

    /** "Örnek cümle": shows the example sentence with the answer blanked. */
    EXAMPLE,

    /** "Telaffuzu dinle": speaks the answer (TTS). */
    LISTEN,
}

/** What using a joker costs in a given mode. */
@Serializable
public sealed interface JokerCost {
    /** Deducted from the remaining time. */
    @Serializable
    @SerialName("time")
    public data class Time(val ms: Long) : JokerCost

    /** Deducted from the score. */
    @Serializable
    @SerialName("points")
    public data class Points(val points: Int) : JokerCost

    @Serializable
    @SerialName("free")
    public data object Free : JokerCost
}

/** Persisted joker counts per type; missing types count as 0. */
@Serializable
public data class JokerInventory(val counts: Map<JokerType, Int> = emptyMap()) {
    init {
        require(counts.values.all { it >= 0 }) { "joker counts must not be negative: $counts" }
    }

    public fun count(type: JokerType): Int = counts[type] ?: 0

    public fun plus(type: JokerType, amount: Int): JokerInventory {
        require(amount >= 0) { "amount must not be negative, was $amount" }
        return JokerInventory(counts + (type to count(type) + amount))
    }

    /** Inventory with one [type] joker less, or null if there is none left. */
    public fun consume(type: JokerType): JokerInventory? =
        if (count(type) > 0) JokerInventory(counts + (type to count(type) - 1)) else null
}
