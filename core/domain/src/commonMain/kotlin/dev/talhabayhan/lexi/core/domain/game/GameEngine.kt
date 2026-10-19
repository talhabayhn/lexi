package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom

/**
 * Pure, deterministic reducer for one player's game (`GAME_RULES.md` §2–§7, §12). Time enters only through
 * [GameEvent.Tick]; randomness only through sub-seeds of [GameState.seed]. Same state + same event ⇒ same result
 * on every device. Ignored events return the very same state instance and no effects.
 */
public class GameEngine(public val config: GameConfig = GameConfig()) {
    public fun newGame(
        seed: Long,
        mode: GameMode,
        level: Level,
        wheel: Wheel,
        jokers: JokerInventory = JokerInventory(),
        prefillsFirstLetter: Boolean = true,
    ): GameState = GameState(
        seed = seed,
        mode = mode,
        level = level,
        wheel = wheel,
        jokers = jokers,
        prefillsFirstLetter = prefillsFirstLetter,
    )

    /**
     * Whether `UseJoker(type)` would be applied to [state] right now (`GAME_RULES.md` §7): the game is playing and
     * accepts input, a joker of [type] is in the inventory, the slot has used no joker yet, [type] is allowed in the
     * mode and at the card's level, a time cost is below the remaining time and Remove distractors has distractors
     * left. The UI uses it to enable the joker sheet rows; [reduce] applies exactly the same rule.
     */
    public fun canUseJoker(state: GameState, type: JokerType): Boolean {
        if (state.status != GameStatus.PLAYING || state.isPaused || state.isRevealing) return false
        val input = state.currentInput ?: return false
        val card = state.currentSlot?.card ?: return false
        return state.jokers.count(type) > 0 && isJokerAccepted(state, input, card, type)
    }

    /** One joker per slot in total, any type, also across laps (owner decision), plus the mode/level/cost rules. */
    private fun isJokerAccepted(state: GameState, input: SlotInput, card: Card, type: JokerType): Boolean {
        val cost = config.jokerCost(type, state.mode)
        return input.jokersUsed.isEmpty() &&
            config.isJokerAllowed(type, state.mode, card.level) &&
            !(cost is JokerCost.Time && (state.remainingMs ?: 0L) <= cost.ms) &&
            !(type == JokerType.REMOVE_DISTRACTORS && !input.hasEnabledDistractors)
    }

    public fun reduce(state: GameState, event: GameEvent): Transition<GameState> {
        val effects = mutableListOf<EngineEffect>()
        val next = Step(effects).reduce(state, event)
        return if (effects.isEmpty() && next == state) Transition(state, emptyList()) else Transition(next, effects)
    }

    /** One reduction; collects effects in order. Never escapes [reduce]. */
    private inner class Step(private val fx: MutableList<EngineEffect>) {
        fun reduce(state: GameState, event: GameEvent): GameState = when {
            state.status == GameStatus.FINISHED -> state
            event is GameEvent.Start -> if (state.status == GameStatus.NOT_STARTED) state.start(event) else state
            event == GameEvent.Forfeit && state.status == GameStatus.NOT_STARTED -> state.forfeitBeforeStart()
            state.status == GameStatus.NOT_STARTED -> state
            else -> state.reducePlaying(event)
        }

        private fun GameState.reducePlaying(event: GameEvent): GameState = when (event) {
            is GameEvent.Start -> this
            GameEvent.Forfeit -> forfeit()
            GameEvent.Pause -> if (isPaused) this else copy(isPaused = true)
            GameEvent.Resume -> if (isPaused) copy(isPaused = false) else this
            is GameEvent.Tick -> if (isPaused || event.elapsedMs <= 0) this else tick(event.elapsedMs)
            is GameEvent.KeyTapped -> ifAcceptingInput { tapKey(event.keyIndex) }
            GameEvent.Backspace -> ifAcceptingInput { backspace() }
            GameEvent.Pass -> ifAcceptingInput { pass() }
            is GameEvent.UseJoker -> ifAcceptingInput { useJoker(event.type) }
        }

        private inline fun GameState.ifAcceptingInput(block: GameState.() -> GameState): GameState =
            if (isPaused || isRevealing || currentInput == null) this else block()

        // region turn flow (§3)

        private fun GameState.start(event: GameEvent.Start): GameState {
            val started = copy(
                status = GameStatus.PLAYING,
                startedAtMs = event.timestampMs,
                remainingMs = config.timeLimitMs(mode),
            )
            val first = wheel.slots.indexOfFirst { it.state.isOpen }
            return if (first < 0) started.finish(GameOverReason.EMPTY_WHEEL) else started.makeCurrent(first, lap = 1)
        }

        private fun GameState.makeCurrent(index: Int, lap: Int): GameState {
            val slot = wheel.slots[index]
            val input = inputs[index] ?: newInput(slot)
            fx += EngineEffect.Advanced(toLetter = slot.letter, lap = lap)
            return copy(
                wheel = wheel.withState(index, SlotState.CURRENT),
                currentIndex = index,
                lap = lap,
                inputs = inputs + (index to input),
            )
        }

        private fun GameState.newInput(slot: Slot): SlotInput {
            val card = checkNotNull(slot.card) { "slot ${slot.letter} is not playable" }
            val random = SeededRandom(seed).derive(KeyboardGenerator.seedPurpose(slot.letter))
            val input = SlotInput(
                keys = KeyboardGenerator.generate(card.answer, random),
                cells = List(card.answerLetters) { null },
            )
            val prefill = prefillsFirstLetter && card.rule == LetterRule.STARTS && card.answerLetters > 1
            return if (prefill) input.withGivenFirstLetter(card) else input
        }

        /** Locks the answer's first letter in cell 0, using the first key with that letter (the key set is unchanged). */
        private fun SlotInput.withGivenFirstLetter(card: Card): SlotInput {
            val letter = card.answer.first()
            val keyIndex = keys.indices.first { keys[it].letter == letter }
            return copy(
                keys = keys.replaced(keyIndex, keys[keyIndex].copy(state = KeyState.USED)),
                cells = cells.replaced(0, PlacedLetter(letter, keyIndex, revealed = false, given = true)),
            )
        }

        /** Next TODO/PASSED slot clockwise (wrapping after Z = next lap), or game over when none is left. */
        private fun GameState.advance(): GameState {
            val from = checkNotNull(currentIndex)
            val size = wheel.slots.size
            for (step in 1..size) {
                val position = from + step
                val index = position % size
                if (wheel.slots[index].state.isOpen) {
                    return makeCurrent(index, lap = if (position >= size) lap + 1 else lap)
                }
            }
            return finish(GameOverReason.ALL_ANSWERED)
        }

        private fun GameState.tapKey(keyIndex: Int): GameState {
            val input = checkNotNull(currentInput)
            val key = input.keys.getOrNull(keyIndex) ?: return this
            val cell = input.cells.indexOf(null)
            if (key.state != KeyState.AVAILABLE || cell < 0) return this
            fx += EngineEffect.LetterPlaced(cellIndex = cell, letter = key.letter)
            return withInput(
                input.copy(
                    keys = input.keys.replaced(keyIndex, key.copy(state = KeyState.USED)),
                    cells = input.cells.replaced(cell, PlacedLetter(key.letter, keyIndex, revealed = false)),
                    placementOrder = input.placementOrder + cell,
                ),
            ).submitIfFull()
        }

        private fun GameState.backspace(): GameState {
            val input = checkNotNull(currentInput)
            val cell = input.placementOrder.lastOrNull() ?: return this
            val placed = checkNotNull(input.cells[cell])
            return withInput(
                input.copy(
                    keys = input.keys.replaced(
                        placed.keyIndex,
                        input.keys[placed.keyIndex].copy(state = KeyState.AVAILABLE),
                    ),
                    cells = input.cells.replaced(cell, null),
                    placementOrder = input.placementOrder.dropLast(1),
                ),
            )
        }

        private fun GameState.pass(): GameState {
            val index = checkNotNull(currentIndex)
            fx += EngineEffect.Passed(wheel.slots[index].letter)
            return copy(wheel = wheel.withState(index, SlotState.PASSED), streak = 0)
                .withInput(checkNotNull(currentInput).clearedTyping().copy(wasPassed = true))
                .advance()
        }

        private fun GameState.submitIfFull(): GameState =
            if (status == GameStatus.PLAYING && currentInput?.isFull == true) submit() else this

        /** Case-insensitive match against the answer without its fixed spaces. */
        private fun GameState.submit(): GameState {
            val index = checkNotNull(currentIndex)
            val card = checkNotNull(wheel.slots[index].card)
            val input = checkNotNull(currentInput)
            return if (input.typed.equals(card.answer.replace(" ", ""), ignoreCase = true)) {
                correct(index, card, input)
            } else {
                fx += EngineEffect.Wrong(letter = card.letter, card = card.id)
                val wrong =
                    copy(
                        wheel = wheel.withState(index, SlotState.WRONG),
                        streak = 0,
                        revealRemainingMs = config.revealMs,
                    )
                if (wrong.isRevealing) wrong else wrong.endReveal()
            }
        }

        /** Owner decision: every TODO, PASSED or CURRENT slot counts as WRONG (and goes to SRS as missed). */
        private fun GameState.forfeit(): GameState {
            val slots = wheel.slots.map {
                if (it.state.isOpen || it.state == SlotState.CURRENT) it.copy(state = SlotState.WRONG) else it
            }
            return copy(wheel = Wheel(slots)).finish(GameOverReason.FORFEIT)
        }

        /** Giving up before the first turn (duel handover) still ends the game; no time was used. */
        private fun GameState.forfeitBeforeStart(): GameState = copy(remainingMs = config.timeLimitMs(mode)).forfeit()

        private fun GameState.endReveal(): GameState {
            val done = copy(revealRemainingMs = 0L)
            return if (mode.endsOnFirstWrong) done.finish(GameOverReason.WRONG_IN_HARDCORE) else done.advance()
        }

        // endregion

        // region scoring (§6)

        private fun GameState.correct(index: Int, card: Card, input: SlotInput): GameState {
            val newStreak = streak + 1
            val bonus = if (input.jokersUsed.isEmpty()) streakBonus(newStreak) else 0
            val points = if (mode.isScored) basePoints(card.level) + bonus else 0
            fx += EngineEffect.Correct(letter = card.letter, card = card.id, points = points)
            if (newStreak in config.comboMilestones) fx += EngineEffect.ComboReached(newStreak)
            return copy(
                wheel = wheel.withState(index, SlotState.CORRECT),
                score = score + points,
                streak = newStreak,
                longestStreak = maxOf(longestStreak, newStreak),
            ).advance()
        }

        private fun GameState.basePoints(level: Level): Int = when (mode) {
            GameMode.DAILY -> config.dailyBasePoints(level)
            else -> percentOf(config.baseCorrectPoints, config.levelMultiplierPercent(level))
        }

        private fun streakBonus(streak: Int): Int = minOf(config.streakBonusStep * (streak - 1), config.streakBonusCap)

        private fun GameState.finish(reason: GameOverReason): GameState {
            // A passed slot that is current again on a later lap was never answered: it stays PASSED (§10, time up).
            val index = currentIndex
            val passedAgain = currentSlot?.state == SlotState.CURRENT && currentInput?.wasPassed == true
            val finalWheel = if (passedAgain && index != null) wheel.withState(index, SlotState.PASSED) else wheel
            val slots = finalWheel.slots
            val completed = reason == GameOverReason.ALL_ANSWERED
            val remaining = remainingMs
            val timeBonus = if (completed && mode.isScored && remaining != null) {
                (remaining / MS_PER_SECOND).toInt() * config.timeBonusPointsPerSecond
            } else {
                0
            }
            val total = score + timeBonus
            val summary = GameSummary(
                reason = reason,
                score = if (mode == GameMode.HARDCORE) percentOf(total, config.hardcoreMultiplierPercent) else total,
                timeBonus = timeBonus,
                completed = completed,
                correct = slots.count { it.state == SlotState.CORRECT },
                wrong = slots.count { it.state == SlotState.WRONG },
                passed = slots.count { it.state == SlotState.PASSED },
                skipped = slots.count { it.state == SlotState.SKIPPED },
                unanswered = slots.count { it.state == SlotState.TODO || it.state == SlotState.CURRENT },
                longestStreak = longestStreak,
                remainingMs = remaining,
                correctCards = slots.filter { it.state == SlotState.CORRECT }.mapNotNull { it.card?.id },
                missedCards = slots
                    .filter { it.state == SlotState.WRONG || it.state == SlotState.PASSED }
                    .mapNotNull { it.card?.id },
            )
            fx += EngineEffect.GameOver(summary)
            return copy(
                wheel = finalWheel,
                status = GameStatus.FINISHED,
                isPaused = false,
                revealRemainingMs = 0L,
                currentIndex = null,
                summary = summary,
            )
        }

        // endregion

        // region timer (§5)

        /** The reveal of a wrong answer uses up ticks first; the clock stands still meanwhile (owner decision). */
        private fun GameState.tick(elapsedMs: Long): GameState {
            if (!isRevealing) return spendClock(elapsedMs)
            val used = minOf(elapsedMs, revealRemainingMs)
            return spendReveal(used).let { if (it.isOver) it else it.spendClock(elapsedMs - used) }
        }

        private fun GameState.spendReveal(ms: Long): GameState {
            val revealing = copy(revealRemainingMs = revealRemainingMs - minOf(ms, revealRemainingMs))
            return if (revealing.isRevealing) revealing else revealing.endReveal()
        }

        /** Charges [ms] to the clock; no-op in untimed modes. */
        private fun GameState.spendClock(ms: Long): GameState = when {
            ms <= 0 || remainingMs == null -> this
            else -> spendTime(ms)
        }

        private val GameState.isOver: Boolean get() = status == GameStatus.FINISHED

        private fun GameState.spendTime(ms: Long): GameState {
            val before = checkNotNull(remainingMs)
            val after = maxOf(0L, before - ms)
            // One warning per transition, for the lowest second reached (none when the time is up).
            val second = wholeSeconds(after)
            if (second in 1..config.timeWarningSeconds && second < wholeSeconds(before)) {
                fx += EngineEffect.TimeWarning(secondsLeft = second)
            }
            val spent = copy(remainingMs = after)
            return if (after == 0L) spent.finish(GameOverReason.TIME_UP) else spent
        }

        private fun wholeSeconds(ms: Long): Int = ((ms + MS_PER_SECOND - 1) / MS_PER_SECOND).toInt()

        // endregion

        // region jokers (§7)

        private fun GameState.useJoker(type: JokerType): GameState {
            val card = checkNotNull(currentSlot?.card)
            val input = checkNotNull(currentInput)
            val inventory = jokers.consume(type)
            val cost = config.jokerCost(type, mode)
            if (inventory == null || !isJokerAccepted(this, input, card, type)) return this

            fx += EngineEffect.JokerApplied(type)
            val paid = copy(jokers = inventory).withInput(input.copy(jokersUsed = input.jokersUsed + type)).pay(cost)
            return when (type) {
                JokerType.REVEAL_LETTER -> paid.revealLetter(card)
                JokerType.REMOVE_DISTRACTORS -> paid.removeDistractors()
                JokerType.EXAMPLE, JokerType.LISTEN -> paid
            }
        }

        private fun GameState.pay(cost: JokerCost): GameState = when (cost) {
            is JokerCost.Time -> spendTime(cost.ms)
            is JokerCost.Points -> if (mode.isScored) copy(score = maxOf(0, score - cost.points)) else this
            JokerCost.Free -> this
        }

        /**
         * Puts the correct letter into the first cell that is empty or wrong (never the given first letter, which is
         * already correct); typed letters from that cell on are returned to their keys first. The revealed letter is
         * locked.
         */
        private fun GameState.revealLetter(card: Card): GameState {
            val input = checkNotNull(currentInput)
            val target = card.answer.replace(" ", "")
            val cell = input.cells.indices.first { input.cells[it]?.letter != target[it] }
            val keys = input.keys.toMutableList()
            val cells = input.cells.toMutableList()
            for (index in cell until cells.size) {
                val placed = cells[index]
                if (placed != null && !placed.isLocked) {
                    keys[placed.keyIndex] = keys[placed.keyIndex].copy(state = KeyState.AVAILABLE)
                    cells[index] = null
                }
            }
            val keyIndex = keys.indices.first {
                keys[it].letter == target[cell] && keys[it].state == KeyState.AVAILABLE
            }
            keys[keyIndex] = keys[keyIndex].copy(state = KeyState.USED)
            cells[cell] = PlacedLetter(target[cell], keyIndex, revealed = true)
            fx += EngineEffect.LetterPlaced(cellIndex = cell, letter = target[cell])
            return withInput(
                input.copy(
                    keys = keys,
                    cells = cells,
                    placementOrder = input.placementOrder.filter { cells[it]?.isLocked == false },
                ),
            ).submitIfFull()
        }

        /** Disables every distractor key; distractor letters already placed are removed from their cells. */
        private fun GameState.removeDistractors(): GameState {
            val input = checkNotNull(currentInput)
            val cells = input.cells.map { placed -> placed?.takeUnless { input.keys[it.keyIndex].isDistractor } }
            return withInput(
                input.copy(
                    keys = input.keys.map { if (it.isDistractor) it.copy(state = KeyState.DISABLED) else it },
                    cells = cells,
                    placementOrder = input.placementOrder.filter { cells[it] != null },
                ),
            )
        }

        // endregion

        private fun GameState.withInput(input: SlotInput): GameState =
            copy(inputs = inputs + (checkNotNull(currentIndex) to input))
    }
}

private const val MS_PER_SECOND = 1_000L
private const val PERCENT = 100

/** `value × percent / 100`, rounded half up (non-negative values). */
internal fun percentOf(value: Int, percent: Int): Int = (value * percent + PERCENT / 2) / PERCENT

/**
 * Typed letters go back to their keys (owner decision: a passed slot comes back empty). Joker effects are paid and
 * survive: revealed letters stay locked in their cells and disabled distractor keys stay disabled; the given first
 * letter stays too.
 */
private fun SlotInput.clearedTyping(): SlotInput {
    val typed = placementOrder.mapTo(mutableSetOf()) { checkNotNull(cells[it]).keyIndex }
    return copy(
        keys = keys.mapIndexed { index, key -> if (index in typed) key.copy(state = KeyState.AVAILABLE) else key },
        cells = cells.map { placed -> placed?.takeIf { it.isLocked } },
        placementOrder = emptyList(),
    )
}

private val SlotInput.hasEnabledDistractors: Boolean
    get() = keys.any { it.isDistractor && it.state != KeyState.DISABLED }

private val SlotState.isOpen: Boolean get() = this == SlotState.TODO || this == SlotState.PASSED

private fun Wheel.withState(index: Int, state: SlotState): Wheel =
    Wheel(slots.replaced(index, slots[index].copy(state = state)))

private fun <T> List<T>.replaced(index: Int, value: T): List<T> = toMutableList().also { it[index] = value }
