package dev.talhabayhan.lexi.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.DefaultLexiDimens
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

        // Pro card + reminder, review reminders, ui language, clue language.
        assertEquals(5, onAllNodesWithText("YAKINDA", useUnmergedTree = true).fetchSemanticsNodes().size)
        onNodeWithText("Günlük hatırlatma").performClick()
        onNodeWithText("Arayüz dili").performClick()

        assertEquals(emptyList<SettingsIntent>(), intents)
    }

    @Test
    fun `live value rows emit cycle intents when tapped`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = { intents += it }) }
        }

        onNodeWithText("Klasik mod süresi").performClick()
        onNodeWithText("Günlük hedef").performClick()
        onNodeWithText("İngilizce aksanı").performClick()

        assertEquals(
            listOf(
                SettingsIntent.CycleClassicTime,
                SettingsIntent.CycleDailyGoal,
                SettingsIntent.CycleAccent,
            ),
            intents,
        )
    }

    @Test
    fun `footer links emit navigation intents when tapped`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        val minTarget = DefaultLexiDimens.minTouchTargetAccessible
        setContent {
            LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = { intents += it }) }
        }

        onNodeWithText(
            "Lexi 1.0.0",
        ).performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget).performClick()
        onNodeWithText(
            "Hakkında",
        ).performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget).performClick()
        onNodeWithText(
            "Gizlilik",
        ).performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget).performClick()
        onNodeWithText(
            "Kullanım koşulları",
        ).performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget).performClick()

        assertEquals(
            listOf(
                SettingsIntent.OpenAbout,
                SettingsIntent.OpenAbout,
                SettingsIntent.OpenPrivacy,
                SettingsIntent.OpenTerms,
            ),
            intents,
        )
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun `about link in english emits open about intent`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        val minTarget = DefaultLexiDimens.minTouchTargetAccessible
        setContent {
            LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = { intents += it }) }
        }

        onNodeWithText(
            "About",
        ).performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget).performClick()

        assertEquals(listOf<SettingsIntent>(SettingsIntent.OpenAbout), intents)
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h600dp-xhdpi", fontScale = 1.3f)
    fun `footer wraps cleanly on 320dp width with large font`() = runComposeUiTest {
        val minTarget = DefaultLexiDimens.minTouchTargetAccessible
        setContent {
            LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = {}) }
        }

        onNodeWithText("Lexi 1.0.0").performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget)
        onNodeWithText("Hakkında").performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget)
        onNodeWithText("Gizlilik").performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget)
        onNodeWithText(
            "Kullanım koşulları",
        ).performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget)
        onNodeWithText("Satın alımları geri yükle").performScrollTo().assertExists()
    }
}
