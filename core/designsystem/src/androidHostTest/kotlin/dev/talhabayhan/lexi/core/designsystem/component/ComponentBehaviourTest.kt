package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res
import dev.talhabayhan.lexi.core.designsystem.generated.resources.answer_slot_active
import dev.talhabayhan.lexi.core.designsystem.generated.resources.answer_slot_empty
import dev.talhabayhan.lexi.core.designsystem.generated.resources.answer_slot_given
import dev.talhabayhan.lexi.core.designsystem.generated.resources.answer_slot_joker
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_description
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_correct
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_current
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_passed
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_todo
import dev.talhabayhan.lexi.core.designsystem.generated.resources.wheel_letter_state_wrong
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LightLexiColors
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import org.jetbrains.compose.resources.stringResource
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class ComponentBehaviourTest {

    private val haptics = mutableListOf<HapticKind>()
    private val sounds = mutableListOf<SoundEffect>()

    private fun ComposeUiTest.setLexiContent(reducedMotion: Boolean = false, content: @Composable () -> Unit) {
        setContent {
            CompositionLocalProvider(
                LocalHaptics provides Haptics { haptics += it },
                LocalSoundPlayer provides SoundPlayer { sounds += it },
            ) {
                LexiTheme(reducedMotion = reducedMotion, content = content)
            }
        }
    }

    @Test
    fun `switch reports the new value with one light haptic and one tap`() = runComposeUiTest {
        val changes = mutableListOf<Boolean>()
        setLexiContent {
            LexiSwitch(checked = false, onCheckedChange = { changes += it }, modifier = Modifier.testTag(TAG))
        }

        onNodeWithTag(TAG).performClick()
        waitForIdle()

        assertEquals(listOf(true), changes)
        assertEquals(listOf(HapticKind.Light), haptics)
        assertEquals(listOf(SoundEffect.Tap), sounds)
    }

    @Test
    fun `segmented control selects the tapped tab with feedback`() = runComposeUiTest {
        var selected by mutableStateOf(0)
        setLexiContent {
            SegmentedControl(
                segments = listOf(Segment("Tekrar"), Segment("Tümü"), Segment("Zor")),
                selectedIndex = selected,
                onSelect = { selected = it },
            )
        }

        onNodeWithText("Zor").performClick()
        waitForIdle()

        assertEquals(2, selected)
        onNodeWithText("Zor").assertIsSelected()
        onNodeWithText("Tekrar").assertIsNotSelected()
        onNodeWithText("Zor").assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
        assertEquals(listOf(HapticKind.Light), haptics)
        assertEquals(listOf(SoundEffect.Tap), sounds)
    }

    @Test
    fun `timer counter pulses in the warning phase`() = runComposeUiTest {
        val (rest, peak) = timerCounterWidths(warning = true, reducedMotion = false)
        assertTrue(peak > rest * 1.08f, "peak $peak vs rest $rest")
    }

    @Test
    fun `timer counter does not pulse under reduced motion`() = runComposeUiTest {
        val (rest, peak) = timerCounterWidths(warning = true, reducedMotion = true)
        assertEquals(rest, peak, absoluteTolerance = 0.5f)
    }

    @Test
    fun `timer counter does not pulse outside the warning phase`() = runComposeUiTest {
        val (rest, peak) = timerCounterWidths(warning = false, reducedMotion = false)
        assertEquals(rest, peak, absoluteTolerance = 0.5f)
    }

    /** Counter width before the state change and at the first pulse peak (300 ms into the 600 ms loop), in px. */
    private fun ComposeUiTest.timerCounterWidths(warning: Boolean, reducedMotion: Boolean): Pair<Float, Float> {
        var warn by mutableStateOf(false)
        setLexiContent(reducedMotion) {
            TimerBar(progress = 0.1f, timeText = "0:09", warning = warn, modifier = Modifier.width(300.dp))
        }
        mainClock.autoAdvance = false
        mainClock.advanceTimeBy(1_000)
        val rest = onNodeWithText("0:09").fetchSemanticsNode().boundsInRoot.width
        runOnUiThread {
            warn = warning
            Snapshot.sendApplyNotifications()
        }
        mainClock.advanceTimeBy(FRAME + 300)
        val peak = onNodeWithText("0:09").fetchSemanticsNode().boundsInRoot.width
        return rest to peak
    }

    @Test
    fun `timer counter pulses on each second in the warning window but not on first composition`() = runComposeUiTest {
        var text by mutableStateOf("0:09")
        setLexiContent {
            TimerBar(progress = 0.1f, timeText = text, warning = true, modifier = Modifier.width(300.dp))
        }
        mainClock.autoAdvance = false
        mainClock.advanceTimeBy(FRAME + 300)
        val rest = onNodeWithText("0:09").fetchSemanticsNode().boundsInRoot.width
        mainClock.advanceTimeBy(1_000)
        assertEquals(rest, onNodeWithText("0:09").fetchSemanticsNode().boundsInRoot.width, absoluteTolerance = 0.5f)

        for (next in listOf("0:08", "0:07")) {
            runOnUiThread {
                text = next
                Snapshot.sendApplyNotifications()
            }
            mainClock.advanceTimeBy(FRAME + 300)
            val peak = onNodeWithText(next).fetchSemanticsNode().boundsInRoot.width
            assertTrue(peak > rest * 1.08f, "$next: peak $peak vs rest $rest")
            mainClock.advanceTimeBy(1_000)
        }
    }

    @Test
    fun `centre letter appears in place on game start and slides on advance`() = runComposeUiTest {
        var letter by mutableStateOf(' ')
        setLexiContent {
            LetterWheel(letters = emptyList(), centerLetter = letter, ruleLabel = "İLE BAŞLAR")
        }
        mainClock.autoAdvance = false

        fun offsetAfterOneFrame(next: Char): Float {
            runOnUiThread {
                letter = next
                Snapshot.sendApplyNotifications()
            }
            mainClock.advanceTimeBy(FRAME * 2)
            val early = onNodeWithText(next.toString(), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top
            mainClock.advanceTimeBy(1_000)
            return early - onNodeWithText(next.toString(), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top
        }

        assertEquals(0f, offsetAfterOneFrame('K'), absoluteTolerance = 0.5f)
        val slide = offsetAfterOneFrame('L')
        assertTrue(slide < -1f, "slide $slide")
    }

    @Test
    fun `timer fill turns from yellow to red in the warning phase`() = runComposeUiTest {
        var warn by mutableStateOf(false)
        setLexiContent(reducedMotion = true) {
            TimerBar(progress = 0.5f, timeText = "0:09", warning = warn, modifier = Modifier.width(300.dp).testTag(TAG))
        }
        // Inside the fill, above the counter: 5dp from the top-left inner corner.
        fun fillPixel() = onNodeWithTag(TAG).captureToImage().toPixelMap().let { it[it.width / 10, it.height / 10] }

        assertEquals(LightLexiColors.yellow, fillPixel())
        warn = true
        waitForIdle()
        assertEquals(LightLexiColors.red, fillPixel())
    }

    @Test
    fun `timer counter is ink normally and red in the warning phase`() = runComposeUiTest {
        var warn by mutableStateOf(false)
        setLexiContent(reducedMotion = true) {
            TimerBar(progress = 0f, timeText = "0:09", warning = warn, modifier = Modifier.width(300.dp))
        }
        fun counterPixels(color: Color): Int {
            val pixels = onNodeWithText("0:09").captureToImage().toPixelMap()
            var count = 0
            for (x in 0 until pixels.width) for (y in 0 until pixels.height) if (pixels[x, y].near(color)) count++
            return count
        }

        assertTrue(counterPixels(LightLexiColors.ink) > 50)
        assertEquals(0, counterPixels(LightLexiColors.red))
        warn = true
        waitForIdle()
        assertTrue(counterPixels(LightLexiColors.red) > 50)
        assertEquals(0, counterPixels(LightLexiColors.ink))
    }

    private fun Color.near(other: Color): Boolean =
        abs(red - other.red) < COLOR_TOLERANCE && abs(green - other.green) < COLOR_TOLERANCE &&
            abs(blue - other.blue) < COLOR_TOLERANCE

    @Test
    fun `bottom sheet slides up unless motion is reduced`() = runComposeUiTest {
        assertTrue(sheetOffsetAfterOneFrame(reducedMotion = false) > 1f)
    }

    @Test
    fun `bottom sheet appears in place under reduced motion`() = runComposeUiTest {
        assertEquals(0f, sheetOffsetAfterOneFrame(reducedMotion = true), absoluteTolerance = 0.5f)
    }

    /** How far the sheet title is below its final position one frame after the sheet was opened, in px. */
    private fun ComposeUiTest.sheetOffsetAfterOneFrame(reducedMotion: Boolean): Float {
        var visible by mutableStateOf(false)
        setLexiContent(reducedMotion) {
            Box(Modifier.fillMaxSize()) {
                BottomSheet(visible = visible, onDismissRequest = {
                }, title = "Joker kullan", closeContentDescription = "Kapat") {
                    Text("İçerik")
                }
            }
        }
        mainClock.autoAdvance = false
        runOnUiThread {
            visible = true
            Snapshot.sendApplyNotifications()
        }
        mainClock.advanceTimeBy(FRAME * 2)
        val early = onNodeWithText("Joker kullan").fetchSemanticsNode().boundsInRoot.top
        mainClock.advanceTimeBy(1_000)
        val settled = onNodeWithText("Joker kullan").fetchSemanticsNode().boundsInRoot.top
        return early - settled
    }

    @Test
    fun `game components announce letter and state`() = runComposeUiTest {
        setLexiContent {
            Column {
                WheelLetter('K', WheelLetterState.Correct, Modifier.testTag("wheel"))
                AnswerSlot(null, AnswerSlotState.Active, Modifier.testTag("slot"))
                LetterKey('E', onClick = {}, state = LetterKeyState.Used, modifier = Modifier.testTag("key"))
            }
        }

        onNodeWithTag("wheel").assertContentDescriptionEquals("K, correct")
        onNodeWithTag("slot").assertContentDescriptionEquals("empty, next")
        onNodeWithTag("key")
            .assertIsNotEnabled()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
    }

    @Test
    fun `labels resolved once announce exactly the formatted resource texts`() = runComposeUiTest {
        val expected = mutableListOf<String>()
        val actual = mutableListOf<String>()
        setLexiContent {
            val wheel = rememberWheelLetterLabels()
            val stateLabels = listOf(
                Res.string.wheel_letter_state_todo,
                Res.string.wheel_letter_state_current,
                Res.string.wheel_letter_state_correct,
                Res.string.wheel_letter_state_wrong,
                Res.string.wheel_letter_state_passed,
            )
            WheelLetterState.entries.zip(stateLabels).forEach { (state, label) ->
                expected += stringResource(Res.string.wheel_letter_description, "K", stringResource(label))
                actual += wheel.describe('K', state)
            }
            val slots = rememberAnswerSlotLabels()
            expected += stringResource(Res.string.answer_slot_empty)
            expected += stringResource(Res.string.answer_slot_active)
            expected += "E"
            expected += stringResource(
                Res.string.wheel_letter_description,
                "E",
                stringResource(Res.string.wheel_letter_state_wrong),
            )
            expected +=
                stringResource(Res.string.wheel_letter_description, "E", stringResource(Res.string.answer_slot_joker))
            expected +=
                stringResource(Res.string.wheel_letter_description, "E", stringResource(Res.string.answer_slot_given))
            listOf(
                AnswerSlotState.Empty,
                AnswerSlotState.Active,
                AnswerSlotState.Filled,
                AnswerSlotState.Wrong,
                AnswerSlotState.JokerFilled,
                AnswerSlotState.Given,
            ).forEach { actual += slots.describe("E", it) }
        }

        waitForIdle()
        assertEquals(expected, actual)
        assertEquals("K, correct", actual[2])
    }

    @Test
    fun `disabled button stays a button and the selected level card is announced`() = runComposeUiTest {
        setLexiContent {
            Column {
                LexiButton("Pasif", onClick = {}, enabled = false, modifier = Modifier.testTag("button"))
                LevelCard(
                    title = "A1",
                    subtitle = "Başlangıç",
                    progress = 0.5f,
                    progressLabel = "412 / 850 kelime",
                    marker = DecorShapeKind.Square,
                    markerColor = LexiTheme.colors.yellow,
                    onClick = {},
                    style = LevelCardStyle.Selected,
                    modifier = Modifier.testTag("level"),
                )
            }
        }

        onNodeWithTag("button")
            .assertIsNotEnabled()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        onNodeWithTag("level")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
    }

    private companion object {
        const val TAG = "subject"
        const val FRAME = 16L
        const val COLOR_TOLERANCE = 0.1f
    }
}
