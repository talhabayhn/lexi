package dev.talhabayhan.lexi.feature.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.DefaultLexiDimens
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.AppInfo
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
    /** The selectable time row of the sheet (the settings row with the same text is a button). */
    private fun ComposeUiTest.timeRow(time: String) = onNode(hasText(time) and isSelectable())

    private val aboutFooter = "${AppInfo.NAME} ${AppInfo.VERSION_NAME}"

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
    fun `only the Pro card is marked soon`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = {}) } }

        assertEquals(1, onAllNodesWithText("YAKINDA", useUnmergedTree = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `reminder row shows the time when on and Kapali when off and opens the sheet`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        var state by mutableStateOf(SettingsUiState(dailyReminder = true, reminderMinuteOfDay = 9 * 60 + 30))
        setContent { LexiTheme(reducedMotion = true) { SettingsScreen(state, onIntent = { intents += it }) } }

        onNodeWithText("09:30").assertExists()
        state = state.copy(dailyReminder = false)
        onNodeWithText("Kapalı").assertExists()
        onNodeWithText("Günlük hatırlatma").performClick()

        assertEquals(listOf<SettingsIntent>(SettingsIntent.OpenReminderSheet), intents)
    }

    @Test
    fun `review reminders is a live switch`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                SettingsScreen(SettingsUiState(reviewReminders = true), onIntent = { intents += it })
            }
        }

        onNodeWithText("Kelime defteri tekrarları").assertIsOn()
        onNodeWithText("Kelime defteri tekrarları").performClick()

        assertEquals(listOf<SettingsIntent>(SettingsIntent.SetReviewReminders(false)), intents)
    }

    @Test
    fun `clue language is read as one fixed value and is not clickable`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = { intents += it }) }
        }

        val row = onNodeWithContentDescription("Türkçe, ipucu dili")
        row.assertHasNoClickAction()
        row.performClick()

        assertEquals(emptyList<SettingsIntent>(), intents)
    }

    @Test
    fun `the sheet is hidden until opened`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = {}) } }

        onNodeWithContentDescription("Kapat").assertDoesNotExist()
    }

    @Test
    fun `sheet switch and time rows emit intents and the chosen time is selected`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                SettingsScreen(
                    SettingsUiState(
                        dailyReminder = true,
                        reminderMinuteOfDay = 8 * 60 + 30,
                        reminderSheetVisible = true,
                    ),
                    onIntent = { intents += it },
                )
            }
        }

        timeRow("08:30").assertIsSelected()
        timeRow("09:00").assertIsNotSelected()
        timeRow("09:00").performClick()
        onNodeWithContentDescription("Kapat").performClick()

        assertEquals(
            listOf(SettingsIntent.SetReminderTime(9 * 60), SettingsIntent.CloseReminderSheet),
            intents,
        )
    }

    @Test
    fun `the blocked sheet shows the hint and opens the system settings`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                SettingsScreen(
                    SettingsUiState(reminderSheetVisible = true, notificationsBlocked = true),
                    onIntent = { intents += it },
                )
            }
        }

        onNodeWithText("Bildirimler kapalı. Ayarlar'dan açabilirsin.").assertExists()
        onNodeWithText("Ayarları aç").performClick()

        assertEquals(listOf<SettingsIntent>(SettingsIntent.OpenNotificationSettings), intents)
    }

    @Test
    fun `the unblocked sheet shows no hint and the time rows are not selectable while the reminder is off`() =
        runComposeUiTest {
            val intents = mutableListOf<SettingsIntent>()
            setContent {
                LexiTheme(reducedMotion = true) {
                    SettingsScreen(
                        SettingsUiState(dailyReminder = false, reminderSheetVisible = true),
                        onIntent = { intents += it },
                    )
                }
            }

            onNodeWithText("Ayarları aç").assertDoesNotExist()
            timeRow("09:00").assertIsNotEnabled()
        }

    @Test
    fun `live value rows emit cycle intents when tapped`() = runComposeUiTest {
        val intents = mutableListOf<SettingsIntent>()
        setContent {
            LexiTheme(reducedMotion = true) { SettingsScreen(SettingsUiState(), onIntent = { intents += it }) }
        }

        onNodeWithText("Klasik mod süresi").performClick()
        onNodeWithText("Günlük hedef").performClick()
        onNodeWithText("Arayüz dili").performClick()
        onNodeWithText("İngilizce aksanı").performClick()

        assertEquals(
            listOf(
                SettingsIntent.CycleClassicTime,
                SettingsIntent.CycleDailyGoal,
                SettingsIntent.CycleAppLanguage,
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
            aboutFooter,
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

        onNodeWithText(aboutFooter).performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget)
        onNodeWithText("Hakkında").performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget)
        onNodeWithText("Gizlilik").performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget)
        onNodeWithText(
            "Kullanım koşulları",
        ).performScrollTo().assertHeightIsAtLeast(minTarget).assertWidthIsAtLeast(minTarget)
        onNodeWithText("Satın alımları geri yükle").performScrollTo().assertExists()
    }
}
