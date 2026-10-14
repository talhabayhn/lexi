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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
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
        val (peak, settled) = timerCounterWidths(reducedMotion = false)
        assertTrue(peak > settled * 1.1f, "peak $peak vs settled $settled")
    }

    @Test
    fun `timer counter does not scale under reduced motion`() = runComposeUiTest {
        val (peak, settled) = timerCounterWidths(reducedMotion = true)
        assertEquals(settled, peak, absoluteTolerance = 0.5f)
    }

    /** Width of the new counter text at the pulse peak (240 ms after the change) and once settled, in px. */
    private fun ComposeUiTest.timerCounterWidths(reducedMotion: Boolean): Pair<Float, Float> {
        var time by mutableStateOf("0:10")
        setLexiContent(reducedMotion) {
            TimerBar(progress = 0.1f, timeText = time, warning = true, modifier = Modifier.width(300.dp))
        }
        mainClock.autoAdvance = false
        mainClock.advanceTimeBy(1_000)
        runOnUiThread {
            time = "0:09"
            Snapshot.sendApplyNotifications()
        }
        mainClock.advanceTimeBy(FRAME + 240)
        val peak = onNodeWithText("0:09").fetchSemanticsNode().boundsInRoot.width
        mainClock.advanceTimeBy(1_000)
        val settled = onNodeWithText("0:09").fetchSemanticsNode().boundsInRoot.width
        return peak to settled
    }

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
    }
}
