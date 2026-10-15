package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.feature.game.components.GameMotion
import dev.talhabayhan.lexi.feature.game.components.rememberGameMotion
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Resting looks right after an effect, built from the design sample (`04-game`: K current, "KIT" typed). */
internal object MotionSamples {
    /** #2: "C" (key 0) has just been placed in cell 3; the key is used, cell 4 is the next one. */
    val letterPlaced: GameUiState = GameSamples.design.let { design ->
        val turn = checkNotNull(design.turn)
        design.copy(
            turn = turn.copy(
                words = persistentListOf(
                    turn.words.single().mapIndexed { index, cell ->
                        when (index) {
                            3 -> AnswerCellUi('C', AnswerSlotState.Filled)
                            4 -> AnswerCellUi(null, AnswerSlotState.Active)
                            else -> cell
                        }
                    }.toImmutableList(),
                ),
                keys = turn.keys.mapIndexed { index, key ->
                    if (index == 0) key.copy(state = LetterKeyState.Used) else key
                }.toImmutableList(),
            ),
        )
    }

    /** #3 / #5: K became [state] and L is current. */
    fun answered(state: WheelLetterState): GameUiState = GameSamples.design.copy(
        wheel = GameSamples.design.wheel.map {
            when (it.letter) {
                'K' -> it.copy(state = state)
                'L' -> it.copy(state = WheelLetterState.Current)
                else -> it
            }
        }.toImmutableList(),
        turn = GameSamples.design.turn?.copy(letter = 'L'),
    )

    val warningAt10: GameClockUi = GameSamples.warningClock.copy(
        remainingMs = 9_400L,
        wholeSeconds = 10,
        fraction = 0.06f,
    )
}

internal class RecordingFeedback {
    val haptics = mutableListOf<HapticKind>()
    val sounds = mutableListOf<SoundEffect>()
    val hapticsPlayer = Haptics { haptics += it }
    val soundPlayer = SoundPlayer { sounds += it }
}

/** Handle to a game screen under test whose motion can be played from the test thread. */
internal class MotionHarness {
    lateinit var motion: GameMotion
    lateinit var scope: CoroutineScope
    var clock: GameClockUi by mutableStateOf(GameSamples.designClock)
}

/**
 * Sets the stateless game screen with recorded feedback; the frame clock is paused, so tests step it with
 * `mainClock.advanceTimeBy`.
 */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.setGameScreen(
    state: GameUiState,
    reducedMotion: Boolean = false,
    feedback: RecordingFeedback = RecordingFeedback(),
    clock: GameClockUi = GameSamples.designClock,
): MotionHarness {
    val harness = MotionHarness().apply { this.clock = clock }
    mainClock.autoAdvance = false
    setContent {
        CompositionLocalProvider(
            LocalHaptics provides feedback.hapticsPlayer,
            LocalSoundPlayer provides feedback.soundPlayer,
        ) {
            LexiTheme(reducedMotion = reducedMotion) {
                harness.motion = rememberGameMotion(LexiTheme.motion)
                harness.scope = rememberCoroutineScope()
                GameScreenUnderTest(state, harness)
            }
        }
    }
    mainClock.advanceTimeBy(FRAME_MS)
    return harness
}

@Composable
private fun GameScreenUnderTest(state: GameUiState, harness: MotionHarness) {
    GameScreen(state = state, clock = { harness.clock }, onIntent = {}, motion = harness.motion)
}

/** Starts [effect] on the screen's motion (like `GameRoute` does) and lets one frame pass. */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.play(harness: MotionHarness, effect: GameEffect) {
    runOnUiThread { harness.scope.launch { harness.motion.play(effect) } }
    mainClock.advanceTimeBy(FRAME_MS)
}

/** Changes the HUD clock from the test thread. */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.setClock(harness: MotionHarness, clock: GameClockUi) {
    runOnUiThread {
        harness.clock = clock
        Snapshot.sendApplyNotifications()
    }
}

internal const val FRAME_MS: Long = 16L
