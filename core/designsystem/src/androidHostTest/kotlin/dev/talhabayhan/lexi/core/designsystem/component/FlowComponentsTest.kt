package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/** Accessibility and behaviour of the video-flow components (`FlowComponents.kt`). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-mdpi")
@OptIn(ExperimentalTestApi::class)
class FlowComponentsTest {
    private val levels = listOf("A1", "A2", "B1", "B2", "C1", "NAT")

    @Test
    fun `level chips are a radio group and select on tap`() = runComposeUiTest {
        var selected by mutableIntStateOf(0)
        setContent {
            LexiTheme(reducedMotion = false) {
                LevelChipRow(levels, selectedIndex = selected, onSelect = { selected = it })
            }
        }

        onNodeWithText("B2").assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
        onNodeWithText("B2").performClick()

        assertEquals(3, selected)
        onNodeWithText("B2").assertIsSelected()
        onNodeWithText("B2").assertHeightIsAtLeast(40.dp)
    }

    @Test
    fun `a disabled segmented control selects nothing and cannot change`() = runComposeUiTest {
        var picked = -1
        setContent {
            LexiTheme(reducedMotion = false) {
                SegmentedControl(
                    segments = listOf(Segment("3:00"), Segment("5:00"), Segment("7:00")),
                    selectedIndex = 0,
                    onSelect = { picked = it },
                    role = Role.RadioButton,
                    enabled = false,
                )
            }
        }

        onNodeWithText("5:00").assertIsNotEnabled()
        onNodeWithText("3:00").assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, false))
        onNodeWithText("5:00").performClick()
        assertEquals(-1, picked)
    }

    @Test
    fun `a locked card is information only and an open one is a selected radio button`() = runComposeUiTest {
        var opened = 0
        setContent {
            LexiTheme(reducedMotion = false) {
                Column {
                    PickerCard(
                        name = "LGS",
                        contentDescription = "LGS İngilizce, 120 / 600",
                        art = { ExamArt(ExamArtKind.Lgs, it) },
                        modifier = Modifier.width(169.dp),
                        progressText = "120 / 600",
                        progress = 0.2f,
                        selected = true,
                        onClick = { opened++ },
                    )
                    PickerCard(
                        name = "TOEFL",
                        contentDescription = "TOEFL, yakında",
                        art = { ExamArt(ExamArtKind.Toefl, it, locked = true) },
                        modifier = Modifier.width(169.dp),
                        locked = true,
                        lockedLabel = "YAKINDA",
                        onClick = { opened++ },
                    )
                }
            }
        }

        onNodeWithContentDescription("LGS İngilizce, 120 / 600")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            .assertIsSelected()
            .performClick()
        onNodeWithContentDescription("TOEFL, yakında")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
            .performClick()

        assertEquals(1, opened)
    }

    @Test
    fun `bottom bar items are buttons at least 48dp high with their own description`() = runComposeUiTest {
        var invited = 0
        setContent {
            LexiTheme(reducedMotion = false) {
                RuledBottomBar(
                    listOf(
                        BottomBarItem(LexiIcons.Stats, "İstatistik", {}),
                        BottomBarItem(LexiIcons.Invite, "Davet et", { invited++ }, "Arkadaşını davet et"),
                        BottomBarItem(LexiIcons.Settings, "Ayarlar", {}),
                    ),
                )
            }
        }

        onNodeWithContentDescription("Arkadaşını davet et")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertHeightIsAtLeast(48.dp)
            .performClick()
        assertEquals(1, invited)
        assertEquals(1, onAllNodesWithText("İstatistik").fetchSemanticsNodes().size)
    }
}
