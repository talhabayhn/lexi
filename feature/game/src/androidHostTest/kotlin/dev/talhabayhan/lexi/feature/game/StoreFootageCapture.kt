package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LexiSplash
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.LetterRule
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.model.PartOfSpeech
import dev.talhabayhan.lexi.feature.game.components.rememberGameMotion
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Store footage, not a regression test: the real game screen played frame by frame (≈30 fps) through two answers
 * (KITCHEN, LEMON) with the app's own motion (#2 fly, #3 correct, #7 combo). Frames, a sound timeline and the
 * splash (`splash_*.png`) go to `build/store-footage/<lang>/`; `tools/store-graphics/video.py` turns them into the
 * store videos.
 *
 * Runs only with `LEXI_STORE_FOOTAGE=1` (`.github/workflows/store-graphics.yml`), so `build` and the Roborazzi
 * verify task skip it.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class StoreFootageCapture {

    @Test
    fun turkish() = capture("tr")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun english() = capture("en")

    /** The app's splash (`motion.md` splash timeline, 1200 ms) as the videos' closing card. */
    @Test
    fun splashTurkish() = splash("tr", "A'DAN Z'YE İNGİLİZCE")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun splashEnglish() = splash("en", "FROM A TO Z IN ENGLISH")

    private fun splash(lang: String, tagline: String) {
        assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")
        val frames = (SPLASH_MS / STEP_MS).toInt()
        for (frame in 0..frames) {
            val progress = frame.toFloat() / frames
            captureRoboImage("build/store-footage/$lang/splash_%04d.png".format(frame)) {
                LexiTheme(reducedMotion = false) { LexiSplash(progress = { progress }, tagline = tagline) }
            }
        }
    }

    private fun capture(lang: String) {
        assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")
        runComposeUiTest { Footage(this, File("build/store-footage/$lang"), lang).play() }
    }

    private class Footage(private val test: ComposeUiTest, private val dir: File, lang: String) {
        private val kitchen = KITCHEN.localized(lang)
        private val lemon = LEMON.localized(lang)
        private val mirror = MIRROR.localized(lang)
        private var state by mutableStateOf(turnState('K', kitchen, typed = 0, score = 120, passedSoFar = PASSED))
        private var clock by mutableStateOf(clockAt(START_MS))
        private val harness = MotionHarness()
        private val timeline = StringBuilder()
        private var frame = 0
        private var elapsedMs = 0L

        fun play() {
            dir.mkdirs()
            dir.listFiles { file -> file.name.startsWith("frame_") }?.forEach { it.delete() }
            test.mainClock.autoAdvance = false
            test.setContent {
                CompositionLocalProvider(
                    LocalHaptics provides RecordingFeedback().hapticsPlayer,
                    LocalSoundPlayer provides RecordingFeedback().soundPlayer,
                ) {
                    LexiTheme(reducedMotion = false) {
                        harness.motion = rememberGameMotion(LexiTheme.motion)
                        harness.scope = rememberCoroutineScope()
                        GameScreen(state = state, clock = { clock }, onIntent = {}, motion = harness.motion)
                    }
                }
            }
            hold(IDLE_MS)
            type('K', kitchen, score = 120, passedSoFar = PASSED)
            answer('K', next = 'L', nextWord = lemon, score = 130, passedSoFar = PASSED)
            hold(IDLE_MS)
            type('L', lemon, score = 130, passedSoFar = PASSED)
            answer('L', next = 'M', nextWord = mirror, score = 140, passedSoFar = PASSED)
            effect(GameEffect.ComboReached(3), "combo")
            hold(COMBO_MS)
            File(dir, "timeline.txt").writeText(timeline.toString())
        }

        private fun type(letter: Char, word: Word, score: Int, passedSoFar: Set<Char>) {
            for (index in word.answer.indices) {
                val next = turnState(letter, word, typed = index + 1, score = score, passedSoFar = passedSoFar)
                val keyIndex = keyIndexOf(word, index)
                update(next)
                effect(GameEffect.LetterPlaced(index, keyIndex, word.answer[index]), "place")
                hold(LETTER_MS)
            }
        }

        private fun answer(letter: Char, next: Char, nextWord: Word, score: Int, passedSoFar: Set<Char>) {
            update(turnState(next, nextWord, typed = 0, score = score, passedSoFar = passedSoFar, done = letter))
            effect(GameEffect.Correct(letter, points = 10), "correct")
            effect(GameEffect.Advanced(next), null)
            hold(CORRECT_MS)
        }

        private fun update(next: GameUiState) {
            test.runOnUiThread {
                state = next
                Snapshot.sendApplyNotifications()
            }
        }

        private fun effect(effect: GameEffect, sound: String?) {
            test.runOnUiThread { harness.scope.launch { harness.motion.play(effect) } }
            if (sound != null) timeline.appendLine("$elapsedMs $sound")
        }

        /** Captures frames for [durationMs] of screen time, ticking the HUD clock once per second. */
        private fun hold(durationMs: Long) {
            val end = elapsedMs + durationMs
            while (elapsedMs < end) {
                test.mainClock.advanceTimeBy(STEP_MS)
                elapsedMs += STEP_MS
                val tick = clockAt(START_MS - elapsedMs)
                if (tick.wholeSeconds != clock.wholeSeconds) {
                    test.runOnUiThread {
                        clock = tick
                        Snapshot.sendApplyNotifications()
                    }
                }
                test.onRoot().captureRoboImage(File(dir, "frame_%04d.png".format(frame++)).path)
            }
        }

        private fun keyIndexOf(word: Word, cell: Int): Int {
            val earlier = word.answer.take(cell).count { it == word.answer[cell] }
            return word.keys.withIndex().filter { it.value == word.answer[cell] }[earlier].index
        }
    }

    /**
     * [clue] is the app's Turkish clue; [clueEn] an English definition used only for the English store set (the
     * owner's choice: English listings show no Turkish text).
     */
    private data class Word(
        val clue: String,
        val clueEn: String,
        val answer: String,
        val keys: String,
        val pos: PartOfSpeech,
    ) {
        fun localized(lang: String): Word = if (lang == "en") copy(clue = clueEn) else this
    }

    private companion object {
        // Sample cards (not user-facing strings of the app); clues are Turkish in every locale, like the game.
        val KITCHEN = Word("mutfak", "the room where you cook", "KITCHEN", "CKHENSITOR", PartOfSpeech.NOUN)
        val LEMON = Word("limon", "a sour yellow fruit", "LEMON", "OALMRTEISN", PartOfSpeech.NOUN)
        val PASSED = setOf('C', 'H')
        val MIRROR = Word("ayna", "you see yourself in it", "MIRROR", "RIMAOTRSEL", PartOfSpeech.NOUN)

        const val STEP_MS = 33L
        const val START_MS = 112_000L
        const val TOTAL_MS = 180_000L
        const val IDLE_MS = 700L
        const val LETTER_MS = 264L
        const val CORRECT_MS = 660L
        const val COMBO_MS = 1_500L
        const val SPLASH_MS = 1_200L

        fun clockAt(remainingMs: Long): GameClockUi = GameClockUi(
            isTimed = true,
            remainingMs = remainingMs,
            fraction = remainingMs.toFloat() / TOTAL_MS,
            wholeSeconds = ((remainingMs + 999) / 1000).toInt(),
            isWarning = false,
        )

        /** The design board (`04-game`) with [current] as the current letter and [typed] letters of [word] placed. */
        fun turnState(
            current: Char,
            word: Word,
            typed: Int,
            score: Int,
            passedSoFar: Set<Char>,
            done: Char? = null,
        ): GameUiState {
            val wheel = ('A'..'Z').map { letter ->
                val chip = when {
                    letter in passedSoFar -> WheelLetterState.Passed
                    letter == 'E' -> WheelLetterState.Wrong
                    letter < current -> WheelLetterState.Correct
                    letter == current -> WheelLetterState.Current
                    else -> WheelLetterState.Todo
                }
                WheelLetterUi(letter, if (letter == done) WheelLetterState.Correct else chip)
            }
            val usedKeys = BooleanArray(word.keys.length)
            word.answer.take(typed).forEach { letter ->
                val index = word.keys.indices.first { word.keys[it] == letter && !usedKeys[it] }
                usedKeys[index] = true
            }
            val cells = word.answer.mapIndexed { index, letter ->
                when {
                    index < typed -> AnswerCellUi(letter, AnswerSlotState.Filled)
                    index == typed -> AnswerCellUi(null, AnswerSlotState.Active)
                    else -> AnswerCellUi(null, AnswerSlotState.Empty)
                }
            }
            return GameSamples.design.copy(
                wheel = wheel.toImmutableList(),
                score = score,
                turn = GameTurnUi(
                    letter = current,
                    rule = LetterRule.STARTS,
                    clue = word.clue,
                    partOfSpeech = word.pos,
                    level = Level.A1,
                    letterCount = word.answer.length,
                    words = persistentListOf(cells.toImmutableList()),
                    keys = word.keys.mapIndexed { index, key ->
                        KeyUi(key, if (usedKeys[index]) LetterKeyState.Used else LetterKeyState.Normal)
                    }.toImmutableList(),
                    isRevealing = false,
                ),
            )
        }
    }
}
