package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.model.Level

/** Wheel with one TODO slot per answer (STARTS rule, letter = first letter); answers must start with distinct letters. */
fun wheelOf(vararg answers: String, level: Level = Level.A1): Wheel =
    Wheel(answers.map { card(it, level = level) }.sortedBy { it.letter }.map { Slot(it.letter, it, SlotState.TODO) })

val allJokers = JokerInventory(JokerType.entries.associateWith { 9 })

/** Drives a [GameEngine] and records every effect. */
class Play(var state: GameState, private val engine: GameEngine) {
    val effects = mutableListOf<EngineEffect>()

    /** Effects of the most recent [send] call only. */
    var lastEffects: List<EngineEffect> = emptyList()
        private set

    fun send(vararg events: GameEvent): Play {
        val collected = mutableListOf<EngineEffect>()
        for (event in events) {
            val transition = engine.reduce(state, event)
            state = transition.state
            collected += transition.effects
        }
        lastEffects = collected
        effects += collected
        return this
    }

    val input: SlotInput get() = checkNotNull(state.currentInput) { "no current slot" }

    val currentLetter: Char? get() = state.currentSlot?.letter

    /** Taps the first available key for each letter of [word] (spaces skipped). */
    fun type(word: String): Play {
        for (letter in word.filter { it != ' ' }) {
            val index = input.keys.indexOfFirst { it.letter == letter && it.state == KeyState.AVAILABLE }
            check(index >= 0) { "no available key '$letter' in ${input.keys}" }
            send(GameEvent.KeyTapped(index))
        }
        return this
    }

    /** Types the current answer correctly. */
    fun answer(): Play = type(checkNotNull(state.currentSlot?.card).answer)

    /** Types a wrong word: the current answer with its letters rotated by one (answers must not be uniform). */
    fun answerWrong(): Play {
        val letters = checkNotNull(state.currentSlot?.card).answer.filter { it != ' ' }
        return type(letters.drop(1) + letters.first())
    }

    /** Wrong answer plus the reveal pause, so the next slot is current. */
    fun answerWrongAndWait(): Play = answerWrong().send(GameEvent.Tick(engine.config.revealMs))

    inline fun <reified T : EngineEffect> effectsOf(): List<T> = effects.filterIsInstance<T>()
}

fun play(
    vararg answers: String,
    mode: GameMode = GameMode.CLASSIC,
    seed: Long = 1L,
    jokers: JokerInventory = allJokers,
    config: GameConfig = GameConfig(),
    level: Level = Level.A1,
    wheel: Wheel = wheelOf(*answers, level = level),
    start: Boolean = true,
): Play {
    val engine = GameEngine(config)
    val game = Play(engine.newGame(seed = seed, mode = mode, level = level, wheel = wheel, jokers = jokers), engine)
    if (start) game.send(GameEvent.Start(timestampMs = 1_000L))
    return game
}
