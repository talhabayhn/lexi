package dev.talhabayhan.lexi.feature.levels.picker

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * "Çarkını seç" and the mode / time dialog at the size of `drafts/video-flow/cats_v3.png` (390 × 844dp, xhdpi = 2×).
 * Goldens are recorded on CI (`screenshots-record`), never on macOS.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class CategoryPickerScreenshotTest {

    @Test
    fun picker() = capture("CategoryPicker", PickerSampleState)

    @Test
    fun pickerNat() = capture(
        "CategoryPickerNat",
        PickerSampleState.copy(level = Level.NAT, categories = persistentListOf(PickerSampleState.categories[0])),
    )

    @Test
    fun pickerError() = capture("CategoryPickerError", PickerSampleState.copy(hasError = true))

    @Test
    fun dialogClassic() = capture("PlayDialogClassic", PickerSampleState.withDialog(GameMode.CLASSIC))

    @Test
    fun dialogPractice() = capture("PlayDialogPractice", PickerSampleState.withDialog(GameMode.PRACTICE))

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun dialogHardcoreEn() = capture("PlayDialogHardcoreEn", PickerSampleState.withDialog(GameMode.HARDCORE))

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun pickerEn() = capture("CategoryPickerEn", PickerSampleState)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
    fun pickerSmallLargeFont() = capture("CategoryPicker_w320h568_fontScale200", PickerSampleState)

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 2.0f)
    fun dialogSmallLargeFont() = capture("PlayDialog_w320h568_fontScale200", PickerSampleState.withDialog())

    private fun capture(name: String, state: CategoryPickerUiState) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) { CategoryPickerScreen(state = state, onIntent = {}) }
        }
    }
}
