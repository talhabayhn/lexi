package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class AccessibilitySemanticsTest {

    private val isHeading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    @Test
    fun `sheet and dialog titles are headings and their children stay individually findable`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.padding(8.dp)) {
                    BottomSheetPanel(title = "Joker kullan", modifier = Modifier.testTag("sheet")) {
                        LexiButton("Reklam izle", onClick = {})
                    }
                }
            }
        }
        onNodeWithText("Joker kullan").assert(isHeading)
        onNodeWithText("Reklam izle").assertIsDisplayed()
        onNodeWithTag("sheet").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.PaneTitle))
    }

    @Test
    fun `dialog title is a heading and both buttons are separate nodes`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                ConfirmDialog("Bırak?", "Mesaj", "Bırak", "Devam", onConfirm = {}, onDismiss = {})
            }
        }
        onNodeWithText("Bırak?").assert(isHeading)
        onNodeWithText("Devam").assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        onNodeWithText("Mesaj").assertIsDisplayed()
    }

    @Test
    fun `labelled switch exposes switch role label and state`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                LexiSwitch(checked = true, onCheckedChange = {}, label = "Titreşim", modifier = Modifier.testTag("sw"))
            }
        }
        onNodeWithTag("sw")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            .assert(hasContentDescription("Titreşim"))
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.ToggleableState))
    }

    @Test
    fun `switch and toast action keep a touch target of at least 44dp`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.padding(40.dp)) {
                    LexiSwitch(checked = false, onCheckedChange = {}, label = "x", modifier = Modifier.testTag("sw"))
                }
            }
        }
        val min = with(density) { 44.dp.toPx() }
        val bounds = onNodeWithTag("sw").fetchSemanticsNode().touchBoundsInRoot
        assertTrue(bounds.width >= min && bounds.height >= min, "switch touch bounds $bounds")
    }

    @Test
    fun `toast action is a button with a label and touch bounds of at least 44dp`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.padding(40.dp)) {
                    Toast("Eklendi", style = ToastStyle.Info, action = ToastAction("Geri al") {})
                }
            }
        }
        val node = onNode(
            hasText("Geri al"),
        ).assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .fetchSemanticsNode()
        val min = with(density) { 44.dp.toPx() }
        assertTrue(node.touchBoundsInRoot.height >= min, "toast action touch bounds ${node.touchBoundsInRoot}")
    }

    @Test
    fun `progress bar exposes description and state`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                ProgressBar(
                    progress = 0.5f,
                    contentDescription = "İlerleme",
                    stateDescription = "yüzde 50",
                    modifier = Modifier.testTag("pb"),
                )
            }
        }
        val config = onNodeWithTag("pb").fetchSemanticsNode().config
        assertEquals("yüzde 50", config[SemanticsProperties.StateDescription])
        assertEquals(listOf("İlerleme"), config[SemanticsProperties.ContentDescription])
        assertEquals(0.5f, config[SemanticsProperties.ProgressBarRangeInfo].current)
    }

    @Test
    fun `letter keys expose a merged label`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = false) {
                Box {
                    LetterKey('K', onClick = {}, modifier = androidx.compose.ui.Modifier.testTag("n"))
                    LetterKey('K', onClick = {}, state = LetterKeyState.Used, modifier = Modifier.testTag("u"))
                }
            }
        }
        val normal = onNodeWithTag("n").fetchSemanticsNode().config[SemanticsProperties.ContentDescription]
        val used = onNodeWithTag("u").fetchSemanticsNode().config
        assertTrue(normal.single().contains("K"))
        assertTrue(used[SemanticsProperties.ContentDescription].single().contains("K"))
        assertTrue(used.contains(SemanticsProperties.Disabled))
    }

    @Test
    fun `live announcement is a polite live region`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = false) { LiveAnnouncement("Doğru", Modifier.testTag("live")) } }
        val config = onNodeWithTag("live").fetchSemanticsNode().config
        assertEquals(androidx.compose.ui.semantics.LiveRegionMode.Polite, config[SemanticsProperties.LiveRegion])
        assertEquals(listOf("Doğru"), config[SemanticsProperties.ContentDescription])
    }
}
