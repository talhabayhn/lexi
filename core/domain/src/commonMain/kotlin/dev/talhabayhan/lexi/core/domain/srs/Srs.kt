package dev.talhabayhan.lexi.core.domain.srs

import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.model.CardId
import kotlinx.serialization.Serializable

/**
 * Learning progress of one card (`progress` table, `CONTENT.md` §5). Every answered card has one; only cards that
 * were missed once are in the notebook (Leitner [box] 0–4). Days are epoch days in the player's time zone, computed
 * by the caller.
 */
@Serializable
public data class CardProgress(
    val cardId: CardId,
    /** Null = not in the notebook (never missed). */
    val box: Int? = null,
    /** Null when not in the notebook or mastered. */
    val dueEpochDay: Long? = null,
    val seen: Int = 0,
    val correct: Int = 0,
    val wrong: Int = 0,
    val lastSeenMs: Long? = null,
    val mastered: Boolean = false,
) {
    init {
        require(box == null || box in 0..Srs.MAX_BOX) { "box $box must be 0-${Srs.MAX_BOX}" }
    }

    public val inNotebook: Boolean get() = box != null

    public fun isDue(todayEpochDay: Long): Boolean {
        val due = dueEpochDay
        return box != null && !mastered && due != null && due <= todayEpochDay
    }
}

@Serializable
public enum class CardOutcome { CORRECT, WRONG, PASSED }

@Serializable
public data class CardResult(val cardId: CardId, val outcome: CardOutcome)

/** Leitner SRS for "Kelime Defterim" (`GAME_RULES.md` §10). Pure functions; "today" and "now" are parameters. */
public object Srs {
    public const val MAX_BOX: Int = 4

    /** Cards with at least this many wrong answers are on the "Zor" tab. */
    public const val HARD_WRONGS: Int = 2

    private val INTERVAL_DAYS = intArrayOf(0, 1, 3, 7, 21)

    /** Days until a card in [box] is due again: box 0 = today, 1 = 1 d, 2 = 3 d, 3 = 7 d, 4 = 21 d. */
    public fun intervalDays(box: Int): Int {
        require(box in 0..MAX_BOX) { "box $box must be 0-$MAX_BOX" }
        return INTERVAL_DAYS[box]
    }

    /**
     * One answer in any game (review included). WRONG or PASSED ⇒ box 0, due today (also for mastered cards).
     * CORRECT ⇒ box + 1 for notebook cards (correct in box 4 ⇒ mastered); cards outside the notebook stay outside.
     */
    public fun apply(
        progress: CardProgress?,
        cardId: CardId,
        outcome: CardOutcome,
        todayEpochDay: Long,
        nowMs: Long,
    ): CardProgress {
        val seen = (progress ?: CardProgress(cardId)).let {
            it.copy(
                seen = it.seen + 1,
                correct = it.correct + if (outcome == CardOutcome.CORRECT) 1 else 0,
                wrong = it.wrong + if (outcome == CardOutcome.WRONG) 1 else 0,
                lastSeenMs = nowMs,
            )
        }
        val box = seen.box
        return when {
            outcome != CardOutcome.CORRECT -> seen.copy(box = 0, dueEpochDay = todayEpochDay, mastered = false)
            box == null || seen.mastered -> seen
            box == MAX_BOX -> seen.copy(mastered = true, dueEpochDay = null)
            else -> seen.copy(box = box + 1, dueEpochDay = todayEpochDay + intervalDays(box + 1))
        }
    }

    /** Answered cards of a game from the final slot states, in wheel order (unanswered cards are left out). */
    public fun results(state: GameState): List<CardResult> = state.wheel.slots.mapNotNull { slot ->
        val card = slot.card ?: return@mapNotNull null
        when (slot.state) {
            SlotState.CORRECT -> CardResult(card.id, CardOutcome.CORRECT)
            SlotState.WRONG -> CardResult(card.id, CardOutcome.WRONG)
            SlotState.PASSED -> CardResult(card.id, CardOutcome.PASSED)
            else -> null
        }
    }

    /**
     * Applies a finished game to [progress]; returns only the changed entries. Modes in
     * [GameConfig.srsExcludedModes] change nothing.
     */
    public fun update(
        progress: Map<CardId, CardProgress>,
        game: GameState,
        todayEpochDay: Long,
        nowMs: Long,
        config: GameConfig,
    ): Map<CardId, CardProgress> {
        require(game.status == GameStatus.FINISHED) { "only finished games update the notebook" }
        if (game.mode in config.srsExcludedModes) return emptyMap()
        return results(game).associate { (cardId, outcome) ->
            cardId to apply(progress[cardId], cardId, outcome, todayEpochDay, nowMs)
        }
    }

    /** Due notebook cards, most overdue first (ties by id). */
    public fun dueCards(progress: Collection<CardProgress>, todayEpochDay: Long): List<CardId> = progress
        .filter { it.isDue(todayEpochDay) }
        .sortedWith(compareBy<CardProgress>({ it.dueEpochDay }, { it.cardId.value }))
        .map { it.cardId }

    /** Drives the home and notebook badges. */
    public fun dueCount(progress: Collection<CardProgress>, todayEpochDay: Long): Int =
        progress.count { it.isDue(todayEpochDay) }

    /** "Zor" tab: cards with ≥ [HARD_WRONGS] wrong answers, by id. */
    public fun hardCards(progress: Collection<CardProgress>): List<CardId> =
        progress.filter { it.wrong >= HARD_WRONGS }.map { it.cardId }.sortedBy { it.value }
}
