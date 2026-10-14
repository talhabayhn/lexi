package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class TouchTargetTest {

    @Test
    fun `switch row is one toggleable switch node that toggles from the label`() = runComposeUiTest {
        setContent {
            var checked by remember { mutableStateOf(false) }
            LexiTheme(reducedMotion = false) {
                ListRow(
                    label = "Titreşim",
                    checked = checked,
                    onCheckedChange = { checked = it },
                    modifier = Modifier.testTag(ROW),
                )
            }
        }

        onNodeWithTag(ROW)
            .assert(isToggleable())
            .assert(hasText("Titreşim"))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch))
            .assertIsOff()
        onNodeWithTag(ROW).performTouchInput { click(Offset(width * 0.1f, centerY)) }
        onNodeWithTag(ROW).assertIsOn()
    }

    @Test
    fun `standalone switch toggles from a tap just outside its 30dp height`() = runComposeUiTest {
        var toggles = 0
        setContent {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.padding(40.dp)) {
                    LexiSwitch(checked = false, onCheckedChange = { toggles++ }, modifier = Modifier.testTag(SWITCH))
                }
            }
        }

        // 6dp above the visual top edge: inside the 48dp minimum touch band.
        onNodeWithTag(SWITCH).performTouchInput { click(Offset(centerX, -6.dp.toPx())) }

        assertEquals(1, toggles)
    }

    @Test
    fun `toast action is tappable outside its 32dp height`() = runComposeUiTest {
        var undos = 0
        setContent {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.padding(40.dp)) {
                    Toast(
                        message = "\"egg\" kelime defterine eklendi",
                        style = ToastStyle.Info,
                        action = ToastAction("Geri al") { undos++ },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        onNode(hasText("Geri al")).performTouchInput { click(Offset(centerX, height + 6.dp.toPx())) }
        onNode(hasText("Geri al")).performClick()

        assertEquals(2, undos)
    }

    private companion object {
        const val ROW = "row"
        const val SWITCH = "switch"
    }
}
