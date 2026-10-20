package dev.talhabayhan.lexi.feature.settings

import androidx.compose.runtime.Composable
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.settings.reminder.ReminderPrePromptScreen
import dev.talhabayhan.lexi.feature.settings.reminder.ReminderPrePromptUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Settings with the reminder on/off, the reminder sheet (also blocked) and the pre-prompt, TR and EN. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class ReminderScreenshotTest {
    private val on = SettingsUiState(dailyReminder = true)
    private val off = SettingsUiState(dailyReminder = false)
    private val sheetOn = on.copy(reminderSheetVisible = true)
    private val sheetFar = on.copy(reminderSheetVisible = true, reminderMinuteOfDay = 21 * 60)
    private val sheetOff = off.copy(reminderSheetVisible = true)
    private val sheetDenied = off.copy(reminderSheetVisible = true, notificationsBlocked = true)

    @Test
    fun settingsReminderOff() = settings("SettingsReminderOff", off)

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun settingsReminderOffEn() = settings("SettingsReminderOffEn", off)

    @Test
    fun reminderSheet() = settings("ReminderSheet", sheetOn)

    @Test
    fun reminderSheetLateTime() = settings("ReminderSheetLateTime", sheetFar)

    @Test
    fun reminderSheetOff() = settings("ReminderSheetOff", sheetOff)

    @Test
    fun reminderSheetDenied() = settings("ReminderSheetDenied", sheetDenied)

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun reminderSheetEn() = settings("ReminderSheetEn", sheetOn)

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun reminderSheetDeniedEn() = settings("ReminderSheetDeniedEn", sheetDenied)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun reminderSheetDenied320() = settings("ReminderSheetDenied_w320h568", sheetDenied)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun reminderSheetDenied320Font130() = settings("ReminderSheetDenied_w320h568_fontScale130", sheetDenied)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun reminderSheet320() = settings("ReminderSheet_w320h568", sheetOn)

    @Test
    fun prePrompt() = prePrompt("ReminderPrePrompt")

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun prePromptEn() = prePrompt("ReminderPrePromptEn")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun prePrompt320() = prePrompt("ReminderPrePrompt_w320h568")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun prePrompt320Font130() = prePrompt("ReminderPrePrompt_w320h568_fontScale130")

    private fun settings(name: String, state: SettingsUiState) = capture(name) {
        SettingsScreen(state = state, onIntent = {})
    }

    private fun prePrompt(name: String) = capture(name) {
        ReminderPrePromptScreen(state = ReminderPrePromptUiState(), onIntent = {})
    }

    private fun capture(name: String, content: @Composable () -> Unit) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false, content = content)
        }
    }
}
