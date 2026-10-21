package dev.talhabayhan.lexi.feature.game

import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.domain.game.JokerType
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals

class GameAnnouncementsTest {

    /**
     * The design board (K current, C and H passed, E wrong) with [changes] applied to the wheel and the turn on [turn]
     * (the current chip's letter by default).
     */
    private fun board(vararg changes: Pair<Char, WheelLetterState>, turn: Char? = null): GameUiState {
        val byLetter = changes.toMap()
        val wheel = GameSamples.design.wheel.map { it.copy(state = byLetter[it.letter] ?: it.state) }
        val letter = turn ?: wheel.firstOrNull { it.state == WheelLetterState.Current }?.letter ?: 'K'
        return GameSamples.design.copy(
            wheel = wheel.toImmutableList(),
            turn = GameSamples.design.turn?.copy(letter = letter),
        )
    }

    private val lettersLToY = ('L'..'Y').map { it to WheelLetterState.Correct }.toTypedArray()

    @Test
    fun `a correct answer is announced with its letter`() {
        val next = board('K' to WheelLetterState.Correct, 'L' to WheelLetterState.Current)

        assertEquals(listOf(GameAnnouncement.Correct('K')), boardAnnouncements(GameSamples.design, next))
    }

    @Test
    fun `a wrong answer is announced with the revealed answer`() {
        assertEquals(
            listOf(GameAnnouncement.Wrong('K', "KITCHEN")),
            boardAnnouncements(GameSamples.design, GameSamples.wrongReveal),
        )
    }

    @Test
    fun `a pass at the end of the wheel is announced with the new lap`() {
        val atZ = board('K' to WheelLetterState.Correct, *lettersLToY, 'Z' to WheelLetterState.Current)
        val wrapped = board(
            'K' to WheelLetterState.Correct,
            *lettersLToY,
            'Z' to WheelLetterState.Passed,
            'C' to WheelLetterState.Current,
        )
        // Within the lap, the next passed letter (C → H) is no new lap.
        val nextInLap = board(
            'K' to WheelLetterState.Correct,
            *lettersLToY,
            'Z' to WheelLetterState.Passed,
            'C' to WheelLetterState.Correct,
            'H' to WheelLetterState.Current,
        )

        assertEquals(
            listOf(GameAnnouncement.Passed('Z'), GameAnnouncement.NewLap),
            boardAnnouncements(atZ, wrapped),
        )
        assertEquals(listOf(GameAnnouncement.Correct('C')), boardAnnouncements(wrapped, nextInLap))
    }

    @Test
    fun `a new lap after a wrong answer at the end of the wheel is announced`() {
        val revealing = board('K' to WheelLetterState.Correct, *lettersLToY, 'Z' to WheelLetterState.Wrong, turn = 'Z')
        val wrapped = board(
            'K' to WheelLetterState.Correct,
            *lettersLToY,
            'Z' to WheelLetterState.Wrong,
            'C' to WheelLetterState.Current,
        )

        assertEquals(listOf(GameAnnouncement.NewLap), boardAnnouncements(revealing, wrapped))
    }

    @Test
    fun `a used joker is announced but an ad reward is not`() {
        val used = GameSamples.jokerSheet.copy(
            jokers = GameSamples.jokerSheet.jokers.map {
                if (it.type == JokerType.REVEAL_LETTER) it.copy(count = it.count - 1) else it
            }.toImmutableList(),
            isJokerSheetOpen = false,
        )
        val rewarded = used.copy(jokers = used.jokers.map { it.copy(count = it.count + 1) }.toImmutableList())

        assertEquals(
            listOf(GameAnnouncement.JokerUsed(JokerType.REVEAL_LETTER)),
            boardAnnouncements(GameSamples.jokerSheet, used),
        )
        assertEquals(emptyList(), boardAnnouncements(used, rewarded))
    }

    @Test
    fun `the end of the wheel is announced once`() {
        val answered = ('A'..'Y').map { it to WheelLetterState.Correct }.toTypedArray()
        val last = board(*answered, 'Z' to WheelLetterState.Current)
        val over = board(*answered, 'Z' to WheelLetterState.Correct, turn = 'Z')

        assertEquals(
            listOf(GameAnnouncement.Correct('Z'), GameAnnouncement.GameOver),
            boardAnnouncements(last, over),
        )
        assertEquals(emptyList(), boardAnnouncements(over, over))
    }

    @Test
    fun `typing pausing loading and a restart announce nothing`() {
        val restarted = GameSamples.design.copy(
            wheel = GameSamples.design.wheel.mapIndexed { i, item ->
                item.copy(state = if (i == 0) WheelLetterState.Current else WheelLetterState.Todo)
            }.toImmutableList(),
        )

        assertEquals(emptyList(), boardAnnouncements(GameSamples.design, GameSamples.paused))
        assertEquals(emptyList(), boardAnnouncements(GameSamples.design, GameSamples.exampleJoker))
        assertEquals(emptyList(), boardAnnouncements(GameUiState(), GameSamples.design))
        assertEquals(emptyList(), boardAnnouncements(GameSamples.design, restarted))
    }
}
