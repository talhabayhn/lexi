package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One wheel slot as saved: card content is not stored, only its id ([cardId] null = SKIPPED). */
@Serializable
public data class SavedSlot(val letter: Char, val rule: LetterRule?, val cardId: CardId?, val state: SlotState)

/**
 * Event-sourced snapshot of a running game for process-death restore (ADR 0019): the initial state's seed, mode,
 * level, slots and joker inventory plus the applied events, consecutive ticks coalesced into one (tick additivity,
 * `GameEngineTickSplitTest`). Restore = load the cards → [restore] (replays the events, effects discarded) → the
 * caller appends `Pause`. Finished games are not saved.
 */
@Serializable
public data class SavedGame(
    val seed: Long,
    val mode: GameMode,
    val level: Level,
    val slots: List<SavedSlot>,
    val jokers: JokerInventory,
    val events: List<GameEvent> = emptyList(),
    /** [GameState.prefillsFirstLetter]; missing in snapshots saved before that rule, which then replay without it. */
    val prefillsFirstLetter: Boolean = false,
    /** [GameState.prefillsContainsLetter]; missing in snapshots saved before that rule, which then replay without it. */
    val prefillsContainsLetter: Boolean = false,
) {
    /** Card ids to load before [restore]. */
    public val cardIds: List<CardId> get() = slots.mapNotNull { it.cardId }

    /** Adds an applied [event]; a tick right after a tick is merged into it, ticks without elapsed time are dropped. */
    public fun append(event: GameEvent): SavedGame {
        if (event !is GameEvent.Tick) return copy(events = events + event)
        if (event.elapsedMs <= 0) return this
        val last = events.lastOrNull()
        return if (last is GameEvent.Tick) {
            copy(events = events.dropLast(1) + GameEvent.Tick(last.elapsedMs + event.elapsedMs))
        } else {
            copy(events = events + event)
        }
    }

    /**
     * Rebuilds the wheel from [cards] and replays [events] on [engine] (same `GameConfig` as the live game). Null when
     * a card is missing or now serves another letter or rule (content changed): the game is abandoned.
     */
    public fun restore(cards: Collection<Card>, engine: GameEngine): GameState? {
        val byId = cards.associateBy { it.id }
        val wheel = try {
            Wheel(
                slots.map { saved ->
                    val card = saved.cardId?.let { byId[it] ?: return null }
                    if (card != null && (card.letter != saved.letter || card.rule != saved.rule)) return null
                    Slot(saved.letter, card, saved.state)
                },
            )
        } catch (e: IllegalArgumentException) {
            return null
        }
        val initial = engine.newGame(
            seed = seed,
            mode = mode,
            level = level,
            wheel = wheel,
            jokers = jokers,
            prefillsFirstLetter = prefillsFirstLetter,
            prefillsContainsLetter = prefillsContainsLetter,
        )
        return events.fold(initial) { state, event -> engine.reduce(state, event).state }
    }

    public companion object {
        /** Snapshot of a new game ([GameEngine.newGame], before `Start`) with no events yet. */
        public fun of(initial: GameState): SavedGame {
            require(initial.status == GameStatus.NOT_STARTED) { "save the state from newGame, before Start" }
            return SavedGame(
                seed = initial.seed,
                mode = initial.mode,
                level = initial.level,
                slots = initial.wheel.slots.map { SavedSlot(it.letter, it.rule, it.card?.id, it.state) },
                jokers = initial.jokers,
                prefillsFirstLetter = initial.prefillsFirstLetter,
                prefillsContainsLetter = initial.prefillsContainsLetter,
            )
        }
    }
}

/** JSON codec of [SavedGame] (kotlinx-serialization-json over the same serializers as the golden tests). */
public object SavedGameCodec {
    private val json = Json

    public fun encode(game: SavedGame): String = json.encodeToString(SavedGame.serializer(), game)

    /** Null when [text] isn't a valid snapshot (e.g. from an older app version). */
    public fun decodeOrNull(text: String): SavedGame? = try {
        json.decodeFromString(SavedGame.serializer(), text)
    } catch (e: IllegalArgumentException) {
        null
    }
}
