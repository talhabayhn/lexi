package dev.talhabayhan.lexi.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class SettingsScreenBehaviourTest {

    @Test
    fun `switches show their state and tapping a row asks for the opposite`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                SettingsScreen(SettingsUiState(soundEffects = true, haptics = false), onIntent = { intents += it })
            }
        }

        onNodeWithText("Ses efektleri").assertIsOn()
        onNodeWithText("Titreşim").assertIsOff()

        onNodeWithText("Ses efektleri").performClick()
        onNodeWithText("Titreşim").performClick()
        onNodeWithText("Doğru cevapta telaffuzu çal").performClick()

        assertEquals(
            listOf(
                SettingsIntent.SetSoundEffects(false),
                SettingsIntent.SetHaptics(true),
                SettingsIntent.SetSpeakOnCorrect(false),
            ),
            intents,
        )
    }

    @Test
    fun `back button asks to go back`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = { intents += it }) }
        }

        onNodeWithContentDescription("Geri").performClick()

        assertEquals(listOf<SettingsIntent>(SettingsIntent.Back), intents)
    }

    @Test
    fun `rows of later releases are marked soon and do nothing`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = { intents += it }) }
        }

        // Pro card + classic time, 3 learning rows, 3 language rows.
        assertEquals(8, onAllNodesWithText("YAKINDA", useUnmergedTree = true).fetchSemanticsNodes().size)
        onNodeWithText("Klasik mod süresi").performClick()
        onNodeWithText("Arayüz dili").performClick()

        assertEquals(emptyList<SettingsIntent>(), intents)
    }
}
