package dev.talhabayhan.lexi.core.domain.game

import dev.talhabayhan.lexi.core.domain.card
import dev.talhabayhan.lexi.core.domain.model.Card
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Process-death snapshot (ADR 0019): seed + slots + jokers + coalesced events ⇒ the same state after replay. */
class SavedGameTest {
    private val engine = GameEngine()

    private val pool: List<Card> =
        listOf("APPLE", "ARM", "BREAD", "BOOK", "CHAIR", "CAT", "DOG", "DESK", "EGG", "EYE").map { card(it) }

    private fun goldenInitial(): GameState {
        val seed = 2024L
        val wheel = WheelGenerator.generate(
            pool = pool,
            level = Level.A1,
            category = CategoryId.MIXED,
            mode = GameMode.CLASSIC,
            random = SeededRandom(seed).derive(WheelGenerator.SEED_PURPOSE),
        )
        // The golden log was recorded before the given first letter (GAME_RULES.md §3).
        return engine.newGame(
            seed,
            GameMode.CLASSIC,
            Level.A1,
            wheel,
            GameConfig().jokerStartPack,
            prefillsFirstLetter = false,
            prefillsContainsLetter = false,
        )
    }

    /** Live play of the golden event log with a 100 ms ticker in front of every event, as the ViewModel does. */
    private fun liveGolden(): Pair<GameState, SavedGame> {
        val initial = goldenInitial()
        var state = initial
        var saved = SavedGame.of(initial)
        fun apply(event: GameEvent) {
            state = engine.reduce(state, event).state
            saved = saved.append(event)
        }
        for (event in GoldenTest.CLASSIC_EVENTS) {
            repeat(3) { apply(GameEvent.Tick(100L)) }
            apply(event)
        }
        return state to saved
    }

    @Test
    fun `replaying a saved game gives the live state`() {
        val (live, saved) = liveGolden()

        assertEquals(live, saved.restore(pool, engine))
    }

    @Test
    fun `saved game survives the JSON codec and replays to the live state`() {
        val (live, saved) = liveGolden()

        val decoded = SavedGameCodec.decodeOrNull(SavedGameCodec.encode(saved))

        assertEquals(saved, decoded)
        assertEquals(live, decoded?.restore(pool, engine))
    }

    @Test
    fun `replaying the golden event log reproduces the golden snapshot`() {
        val saved = GoldenTest.CLASSIC_EVENTS.fold(SavedGame.of(goldenInitial())) { s, event -> s.append(event) }

        val restored = SavedGameCodec.decodeOrNull(SavedGameCodec.encode(saved))?.restore(pool, engine)

        assertEquals(Json.decodeFromString(GameState.serializer(), GOLDEN_CLASSIC), restored)
    }

    @Test
    fun `snapshot saved before the given letter rule has no rule field and replays with the old rule`() {
        val saved = GoldenTest.CLASSIC_EVENTS.fold(SavedGame.of(goldenInitial())) { s, event -> s.append(event) }
        val text = SavedGameCodec.encode(saved)

        val decoded = checkNotNull(SavedGameCodec.decodeOrNull(text))

        assertFalse("prefillsFirstLetter" in text)
        assertFalse("prefillsContainsLetter" in text)
        assertFalse(decoded.prefillsFirstLetter)
        val restored = checkNotNull(decoded.restore(pool, engine))
        assertFalse(restored.prefillsFirstLetter)
        assertEquals(Json.decodeFromString(GameState.serializer(), GOLDEN_CLASSIC), restored)
    }

    @Test
    fun `snapshot with the given letter rule survives the codec and replays to the live state`() {
        val live = play("APPLE", "BREAD", start = false)
        var saved = SavedGame.of(live.state)
        fun send(event: GameEvent) {
            live.send(event)
            saved = saved.append(event)
        }
        send(GameEvent.Start(1_000L))
        send(GameEvent.KeyTapped(live.input.keys.indexOfFirst { it.letter == 'P' }))
        send(GameEvent.Tick(300L))

        val decoded = checkNotNull(SavedGameCodec.decodeOrNull(SavedGameCodec.encode(saved)))

        assertTrue(decoded.prefillsFirstLetter)
        val restored = decoded.restore(listOf(card("APPLE"), card("BREAD")), engine)
        assertEquals(live.state, restored)
        assertEquals("AP", restored?.currentInput?.typed)
    }

    @Test
    fun `snapshot saved before the contains letter rule replays contains slots with empty cells`() {
        // JSON as written by the app before the contains letter rule: given first letter, no contains field.
        val text = """{"seed":1,"mode":"CLASSIC","level":"A1",""" +
            """"slots":[{"letter":"A","rule":"CONTAINS","cardId":"a1-cat","state":"TODO"}],""" +
            """"jokers":{},"events":[{"type":"start","timestampMs":1000}],"prefillsFirstLetter":true}"""
        val cat = card("CAT", letter = 'A', rule = LetterRule.CONTAINS)

        val decoded = checkNotNull(SavedGameCodec.decodeOrNull(text))
        val restored = checkNotNull(decoded.restore(listOf(cat), engine))

        assertFalse(decoded.prefillsContainsLetter)
        assertFalse(restored.prefillsContainsLetter)
        assertTrue(checkNotNull(restored.currentInput).cells.all { it == null })
    }

    @Test
    fun `snapshot with the contains letter rule survives the codec and replays to the live state`() {
        val cat = card("CAT", letter = 'A', rule = LetterRule.CONTAINS)
        val live = play(wheel = Wheel(listOf(Slot('A', cat, SlotState.TODO))), start = false)
        var saved = SavedGame.of(live.state)
        fun send(event: GameEvent) {
            live.send(event)
            saved = saved.append(event)
        }
        send(GameEvent.Start(1_000L))
        send(GameEvent.KeyTapped(live.input.keys.indexOfFirst { it.letter == 'C' }))

        val decoded = checkNotNull(SavedGameCodec.decodeOrNull(SavedGameCodec.encode(saved)))

        assertTrue(decoded.prefillsContainsLetter)
        val restored = decoded.restore(listOf(cat), engine)
        assertEquals(live.state, restored)
        assertEquals("CA", restored?.currentInput?.typed)
    }

    @Test
    fun `consecutive ticks are coalesced into one`() {
        val saved = SavedGame.of(play("APPLE", start = false).state)
            .append(GameEvent.Start(1_000L))
            .append(GameEvent.Tick(100L))
            .append(GameEvent.Tick(100L))
            .append(GameEvent.Tick(37L))
            .append(GameEvent.Pause)
            .append(GameEvent.Tick(50L))

        assertEquals(
            listOf(GameEvent.Start(1_000L), GameEvent.Tick(237L), GameEvent.Pause, GameEvent.Tick(50L)),
            saved.events,
        )
    }

    @Test
    fun `ticks without elapsed time are not saved`() {
        val saved = SavedGame.of(play("APPLE", start = false).state)
            .append(GameEvent.Tick(100L))
            .append(GameEvent.Tick(0L))
            .append(GameEvent.Tick(-5L))
            .append(GameEvent.Tick(20L))

        assertEquals(listOf<GameEvent>(GameEvent.Tick(120L)), saved.events)
    }

    @Test
    fun `slots keep letter rule card id and initial state and skipped slots have no card`() {
        val saved = SavedGame.of(goldenInitial())

        assertEquals(26, saved.slots.size)
        assertEquals(SavedSlot('A', LetterRule.STARTS, CardId("a1-apple"), SlotState.TODO), saved.slots[0])
        assertEquals(SavedSlot('Z', rule = null, cardId = null, state = SlotState.SKIPPED), saved.slots[25])
        assertEquals(GameConfig().jokerStartPack, saved.jokers)
    }

    @Test
    fun `restore gives null when a card is missing from the content`() {
        val (_, saved) = liveGolden()

        assertNull(saved.restore(pool.filterNot { it.answer == "CAT" }, engine))
    }

    @Test
    fun `restore gives null when a card now serves another letter or rule`() {
        val (_, saved) = liveGolden()
        val changed = pool.map { if (it.answer == "CAT") card("SCAT", letter = 'C', id = it.id.value) else it }

        assertNull(saved.restore(changed, engine))
    }

    @Test
    fun `a saved game is taken from the state before Start`() {
        assertFailsWith<IllegalArgumentException> { SavedGame.of(play("APPLE").state) }
    }

    @Test
    fun `undecodable text gives null`() {
        assertNull(SavedGameCodec.decodeOrNull("{not json"))
        assertNull(SavedGameCodec.decodeOrNull("""{"seed":1}"""))
    }
}
