package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameEngine
import dev.talhabayhan.lexi.core.domain.game.GameEvent
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameState
import dev.talhabayhan.lexi.core.domain.game.GameStatus
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.game.Slot
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.game.Wheel
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import dev.talhabayhan.lexi.core.testing.testCard
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameUiMapperTest {
    private val config = GameConfig()
    private val engine = GameEngine(config)
    private val apple = testCard("APPLE")
    private val iceCream = testCard("ICE CREAM", letter = 'C')
    private val wheel = Wheel(
        listOf(
            Slot('A', apple, SlotState.TODO),
            Slot('B', null, SlotState.SKIPPED),
            Slot('C', iceCream, SlotState.TODO),
        ),
    )

    private fun started(
        jokers: JokerInventory = JokerInventory(),
        mode: GameMode = GameMode.CLASSIC,
        level: Level = Level.A1,
    ): GameState = engine.reduce(engine.newGame(SEED, mode, level, wheel, jokers), GameEvent.Start(0L)).state

    private fun GameState.after(vararg events: GameEvent): GameState =
        events.fold(this) { state, event -> engine.reduce(state, event).state }

    private fun GameState.typed(word: String): GameState = after(*keyTaps(this, word).toTypedArray())

    @Test
    fun `a game that has not started shows the first turn as Start will without input`() {
        val ui = GameUiMapper.map(engine.newGame(SEED, GameMode.CLASSIC, Level.A1, wheel), engine)

        assertFalse(ui.isLoading)
        assertTrue(ui.isStarting)
        assertFalse(ui.inputEnabled)
        assertEquals(listOf('A', 'C'), ui.wheel.map { it.letter })
        assertEquals(GameUiMapper.map(started(), engine).turn, ui.turn)
        assertEquals(WheelLetterState.Current, ui.wheel[0].state)
    }

    @Test
    fun `a game that would end at Start stays loading`() {
        val empty = Wheel(wheel.slots.map { it.copy(card = null, state = SlotState.SKIPPED) })
        val ui = GameUiMapper.map(engine.newGame(SEED, GameMode.CLASSIC, Level.A1, empty), engine)

        assertTrue(ui.isLoading)
        assertNull(ui.turn)
    }

    @Test
    fun `the current slot shows its clue and rule with the given letter and empty boxes and unused keys`() {
        val ui = GameUiMapper.map(started(), engine)

        val turn = checkNotNull(ui.turn)
        assertFalse(ui.isLoading)
        assertTrue(ui.inputEnabled)
        assertEquals(WheelLetterState.Current, ui.wheel[0].state)
        assertEquals(WheelLetterState.Todo, ui.wheel[1].state)
        assertEquals('A', turn.letter)
        assertEquals(LetterRule.STARTS, turn.rule)
        assertEquals(apple.clue, turn.clue)
        assertEquals(PartOfSpeech.NOUN, turn.partOfSpeech)
        assertEquals(Level.A1, turn.level)
        assertEquals(5, turn.letterCount)
        assertEquals(
            listOf(AnswerSlotState.Given, AnswerSlotState.Active) + List(3) { AnswerSlotState.Empty },
            turn.words.single().map { it.state },
        )
        assertEquals('A', turn.words.single().first().letter)
        assertEquals(10, turn.keys.size)
        assertEquals(1, turn.keys.count { it.state == LetterKeyState.Used })
    }

    @Test
    fun `typed letters fill the boxes in order and use their keys`() {
        val ui = GameUiMapper.map(started().typed("AP"), engine)

        val cells = checkNotNull(ui.turn).words.single()
        assertEquals(listOf('A', 'P', null, null, null), cells.map { it.letter })
        assertEquals(
            listOf(AnswerSlotState.Given, AnswerSlotState.Filled, AnswerSlotState.Active) +
                List(2) { AnswerSlotState.Empty },
            cells.map { it.state },
        )
        assertEquals(2, checkNotNull(ui.turn).keys.count { it.state == LetterKeyState.Used })
    }

    @Test
    fun `only the last typed letter is deletable and it moves back as letters are removed`() {
        fun deletable(state: GameState) =
            checkNotNull(GameUiMapper.map(state, engine).turn).words.single().map { it.deletable }

        val typed = started().typed("APP")
        assertEquals(listOf(false, false, true, false, false), deletable(typed))
        val oneBack = typed.after(GameEvent.Backspace)
        assertEquals(listOf(false, true, false, false, false), deletable(oneBack))
        // Only the locked given letter is left: nothing to delete, exactly like Backspace.
        assertEquals(List(5) { false }, deletable(oneBack.after(GameEvent.Backspace)))
    }

    @Test
    fun `revealed letters are never deletable and no cell is deletable during the wrong answer reveal`() {
        val pack = JokerInventory(mapOf(JokerType.REVEAL_LETTER to 1))
        val revealed = started(jokers = pack).typed("A").after(GameEvent.UseJoker(JokerType.REVEAL_LETTER))
        val cells = checkNotNull(GameUiMapper.map(revealed, engine).turn).words.single()
        assertTrue(cells.none { it.deletable && it.state != AnswerSlotState.Filled })
        assertTrue(cells.filter { it.state == AnswerSlotState.JokerFilled }.none { it.deletable })

        val revealing = checkNotNull(GameUiMapper.map(started().typed("APPEL"), engine).turn)
        assertTrue(revealing.words.single().none { it.deletable })
    }

    @Test
    fun `a contains slot shows its letter given at the first occurrence and the first cell active`() {
        val ui = GameUiMapper.map(started().after(GameEvent.Pass), engine)

        val words = checkNotNull(ui.turn).words
        assertEquals(listOf(null, 'C', null), words[0].map { it.letter })
        assertEquals(
            listOf(AnswerSlotState.Active, AnswerSlotState.Given, AnswerSlotState.Empty),
            words[0].map { it.state },
        )
        assertTrue(words[1].all { it.state == AnswerSlotState.Empty })
        assertEquals(1, checkNotNull(ui.turn).keys.count { it.state == LetterKeyState.Used })
    }

    @Test
    fun `a wrong answer shows the correct one in red and blocks input`() {
        val ui = GameUiMapper.map(started().typed("APPEL"), engine)

        val turn = checkNotNull(ui.turn)
        assertTrue(turn.isRevealing)
        assertFalse(ui.inputEnabled)
        assertEquals("APPLE", turn.words.single().map { it.letter }.joinToString(""))
        assertTrue(turn.words.single().all { it.state == AnswerSlotState.Wrong })
        assertEquals(WheelLetterState.Wrong, ui.wheel[0].state)
    }

    @Test
    fun `a pass turns the letter into a leaf and moves to the next one`() {
        val ui = GameUiMapper.map(started().after(GameEvent.Pass), engine)

        assertEquals(WheelLetterState.Passed, ui.wheel[0].state)
        assertEquals(WheelLetterState.Current, ui.wheel[1].state)
        assertEquals('C', checkNotNull(ui.turn).letter)
        assertEquals(LetterRule.CONTAINS, checkNotNull(ui.turn).rule)
    }

    @Test
    fun `a multi-word answer is split into words without the space`() {
        val ui = GameUiMapper.map(started().after(GameEvent.Pass), engine)

        assertEquals(listOf(3, 5), checkNotNull(ui.turn).words.map { it.size })
        assertEquals(8, checkNotNull(ui.turn).letterCount)
    }

    @Test
    fun `a correct answer turns the letter blue and adds the points`() {
        val ui = GameUiMapper.map(started().typed("APPLE"), engine)

        assertEquals(WheelLetterState.Correct, ui.wheel[0].state)
        assertEquals(10, ui.score)
    }

    @Test
    fun `a paused game blocks input`() {
        val ui = GameUiMapper.map(started().after(GameEvent.Pause), engine)

        assertTrue(ui.isPaused)
        assertFalse(ui.inputEnabled)
    }

    @Test
    fun `after game over the last turn stays visible`() {
        val playing = started()
        val before = GameUiMapper.map(playing, engine)
        val finished = playing.after(GameEvent.Forfeit)
        assertEquals(GameStatus.FINISHED, finished.status)

        val ui = GameUiMapper.map(finished, engine, previous = before)

        assertEquals(before.turn, ui.turn)
        assertFalse(ui.inputEnabled)
        assertEquals(WheelLetterState.Wrong, ui.wheel[0].state)
    }

    @Test
    fun `the clock shows the full time before start and the remaining part afterwards`() {
        val limit = checkNotNull(config.timeLimitMs(GameMode.CLASSIC))
        val notStarted = engine.newGame(SEED, GameMode.CLASSIC, Level.A1, wheel)

        assertEquals(
            GameClockUi(isTimed = true, remainingMs = limit, fraction = 1f, wholeSeconds = 180, isWarning = false),
            GameUiMapper.clock(notStarted, limit, config.timeWarningSeconds),
        )
        val clock = GameUiMapper.clock(started().after(GameEvent.Tick(72_500L)), limit, config.timeWarningSeconds)
        assertEquals(107_500L, clock.remainingMs)
        assertEquals(108, clock.wholeSeconds)
        assertEquals(107_500f / 180_000f, clock.fraction)
        assertFalse(clock.isWarning)
    }

    @Test
    fun `the last ten seconds are a warning`() {
        val limit = checkNotNull(config.timeLimitMs(GameMode.CLASSIC))
        val state = started()

        assertFalse(GameUiMapper.clock(state.after(GameEvent.Tick(169_999L)), limit, 10).isWarning)
        assertTrue(GameUiMapper.clock(state.after(GameEvent.Tick(170_000L)), limit, 10).isWarning)
    }

    @Test
    fun `untimed modes have no clock`() {
        assertEquals(GameClockUi.Untimed, GameUiMapper.clock(started(), timeLimitMs = null, warningSeconds = 10))
    }

    @Test
    fun `the joker sheet lists the four jokers in design order with inventory and classic costs`() {
        val ui = GameUiMapper.map(started(jokers = pack), engine)

        assertEquals(
            listOf(
                JokerUi(JokerType.REVEAL_LETTER, 2, JokerCostUi.Seconds(10), enabled = true),
                JokerUi(JokerType.REMOVE_DISTRACTORS, 1, JokerCostUi.Seconds(15), enabled = true),
                JokerUi(JokerType.EXAMPLE, 3, JokerCostUi.Points(5), enabled = true),
                JokerUi(JokerType.LISTEN, 0, JokerCostUi.Points(10), false, JokerDisabledReason.NoneLeft),
            ),
            ui.jokers,
        )
        assertEquals(config.rewardedAdJokerReward, ui.jokerAdReward)
    }

    @Test
    fun `the joker help names the configured costs`() {
        val custom = GameConfig(
            revealLetterCostMs = 12_000L,
            removeDistractorsCostMs = 20_000L,
            exampleCostPoints = 7,
            listenCostPoints = 9,
        )

        assertEquals(JokerHelpUi(10, 15, 5, 10), GameUiMapper.map(started(), engine).jokerHelp)
        assertEquals(JokerHelpUi(12, 20, 7, 9), GameUiMapper.map(started(), GameEngine(custom)).jokerHelp)
    }

    @Test
    fun `unscored modes show their point costs as free because there is no score to take from`() {
        val ui = GameUiMapper.map(started(jokers = pack, mode = GameMode.PRACTICE), engine)

        assertEquals(List(4) { JokerCostUi.Free }, ui.jokers.map { it.cost })
        assertFalse(ui.isScored)
    }

    @Test
    fun `hardcore costs time like classic`() {
        val ui = GameUiMapper.map(started(jokers = pack, mode = GameMode.HARDCORE), engine)

        assertEquals(JokerCostUi.Seconds(10), ui.jokers[0].cost)
        assertEquals(JokerCostUi.Points(5), ui.jokers[2].cost)
        assertTrue(ui.isScored)
    }

    @Test
    fun `jokers are disabled after one was used on the slot and while input is blocked`() {
        val used = started(jokers = pack).after(GameEvent.UseJoker(JokerType.EXAMPLE))
        assertTrue(GameUiMapper.map(used, engine).jokers.none { it.enabled })

        val paused = started(jokers = pack).after(GameEvent.Pause)
        assertTrue(GameUiMapper.map(paused, engine).jokers.none { it.enabled })

        val revealing = started(jokers = pack).typed("APPEL")
        assertTrue(GameUiMapper.map(revealing, engine).jokers.none { it.enabled })
    }

    @Test
    fun `listen is disabled at C1 even with inventory`() {
        val c1Wheel = Wheel(listOf(Slot('A', testCard("APPLE", level = Level.C1), SlotState.TODO)))
        val state = engine.reduce(
            engine.newGame(SEED, GameMode.CLASSIC, Level.C1, c1Wheel, JokerInventory(mapOf(JokerType.LISTEN to 1))),
            GameEvent.Start(0L),
        ).state

        assertFalse(GameUiMapper.map(state, engine).jokers.single { it.type == JokerType.LISTEN }.enabled)
    }

    @Test
    fun `an enabled joker has no reason and an empty inventory says none left first`() {
        val ui = GameUiMapper.map(started(jokers = JokerInventory(mapOf(JokerType.EXAMPLE to 1))), engine)

        assertNull(ui.jokers.single { it.type == JokerType.EXAMPLE }.disabledReason)
        assertEquals(JokerDisabledReason.NoneLeft, ui.jokers.single { it.type == JokerType.LISTEN }.disabledReason)
    }

    @Test
    fun `a joker used on the letter makes the others with stock say used for letter`() {
        val used = started(jokers = pack).after(GameEvent.UseJoker(JokerType.EXAMPLE))
        val reasons = GameUiMapper.map(used, engine).jokers.map { it.disabledReason }

        assertEquals(List(3) { JokerDisabledReason.UsedForLetter } + JokerDisabledReason.NoneLeft, reasons)
    }

    @Test
    fun `daily has no jokers and listen is off at C1`() {
        val daily = GameUiMapper.map(started(jokers = pack, mode = GameMode.DAILY), engine)
        assertEquals(
            List(3) {
                JokerDisabledReason.ModeNotAllowed
            } + JokerDisabledReason.NoneLeft,
            daily.jokers.map { it.disabledReason },
        )

        val c1Wheel = Wheel(listOf(Slot('A', testCard("APPLE", level = Level.C1), SlotState.TODO)))
        val c1 = engine.reduce(
            engine.newGame(SEED, GameMode.CLASSIC, Level.C1, c1Wheel, JokerInventory(mapOf(JokerType.LISTEN to 1))),
            GameEvent.Start(0L),
        ).state
        val listen = GameUiMapper.map(c1, engine).jokers.single { it.type == JokerType.LISTEN }
        assertEquals(JokerDisabledReason.LevelNotAllowed, listen.disabledReason)
    }

    @Test
    fun `a paused game falls back to unavailable`() {
        val paused = started(jokers = pack).after(GameEvent.Pause)

        assertEquals(
            List(3) { JokerDisabledReason.Unavailable } + JokerDisabledReason.NoneLeft,
            GameUiMapper.map(paused, engine).jokers.map { it.disabledReason },
        )
    }

    @Test
    fun `the example joker shows the example with the answer blanked`() {
        assertNull(GameUiMapper.map(started(jokers = pack), engine).turn?.example)

        val ui = GameUiMapper.map(started(jokers = pack).after(GameEvent.UseJoker(JokerType.EXAMPLE)), engine)

        assertEquals("An example with _____.", ui.turn?.example)
    }

    @Test
    fun `blanking keeps spaces of multi-word answers and only matches whole words in any case`() {
        assertEquals("We had ___ _____ today.", GameUiMapper.blankExample("We had ice cream today.", "ICE CREAM"))
        assertEquals("Pineapple or _____? _____!", GameUiMapper.blankExample("Pineapple or apple? Apple!", "APPLE"))
    }

    private companion object {
        const val SEED = 7L

        /** The design's sheet: 2 / 1 / 3 / 0. */
        val pack = JokerInventory(
            mapOf(
                JokerType.REVEAL_LETTER to 2,
                JokerType.REMOVE_DISTRACTORS to 1,
                JokerType.EXAMPLE to 3,
                JokerType.LISTEN to 0,
            ),
        )
    }
}
