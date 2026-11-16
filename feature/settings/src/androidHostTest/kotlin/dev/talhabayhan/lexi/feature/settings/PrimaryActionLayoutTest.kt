package dev.talhabayhan.lexi.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import dev.talhabayhan.lexi.feature.settings.reminder.ReminderPrePromptScreen
import dev.talhabayhan.lexi.feature.settings.reminder.ReminderPrePromptUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * The primary actions of the settings module are fully on screen without scrolling on a 320 × 568 phone, at 100 %,
 * 130 % and 200 % font scale, in Turkish and English (the pattern of `OnboardingLayoutTest`).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class PrimaryActionLayoutChecks(private val labels: Labels) {
    class Labels(val yes: String, val no: String, val openSettings: String)

    @Test
    fun `reminder pre-prompt keeps both answers on screen`() {
        assertVisible(labels.yes, labels.no) { ReminderPrePromptScreen(ReminderPrePromptUiState(), onIntent = {}) }
    }

    @Test
    fun `reminder sheet keeps open settings on screen when notifications are blocked`() {
        assertVisible(labels.openSettings) {
            SettingsScreen(
                state = SettingsUiState(
                    dailyReminder = false,
                    reminderSheetVisible = true,
                    notificationsBlocked = true,
                ),
                onIntent = {},
            )
        }
    }

    private fun assertVisible(vararg labels: String, screen: @Composable () -> Unit) = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { LexiViewport { screen() } } }
        val root = onRoot().getUnclippedBoundsInRoot()
        labels.forEach { label ->
            val bounds = onNodeWithText(label).getUnclippedBoundsInRoot()
            assertTrue(bounds.top >= 0.dp, "$label starts above the screen: ${bounds.top}")
            assertTrue(bounds.bottom <= root.bottom, "$label is cut off: bottom ${bounds.bottom} > ${root.bottom}")
        }
    }
}

private val TR = PrimaryActionLayoutChecks.Labels(
    yes = "Evet, hatırlat",
    no = "Şimdi değil",
    openSettings = "Ayarları aç",
)
private val EN = PrimaryActionLayoutChecks.Labels(
    yes = "Yes, remind me",
    no = "Not now",
    openSettings = "Open settings",
)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutTr100Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutTr130Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutTr200Test : PrimaryActionLayoutChecks(TR)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi")
class PrimaryActionLayoutEn100Test : PrimaryActionLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 1.3f)
class PrimaryActionLayoutEn130Test : PrimaryActionLayoutChecks(EN)

@Config(sdk = [36], qualifiers = "en-w320dp-h568dp-xhdpi", fontScale = 2.0f)
class PrimaryActionLayoutEn200Test : PrimaryActionLayoutChecks(EN)
