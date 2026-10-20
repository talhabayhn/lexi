package dev.talhabayhan.lexi.feature.onboarding.result

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Level test result (DESIGN-DEBT, `06-result` header style): A1 and NAT with their flavour line, B1 without one, the
 * picker, English and a 1.3 font scale.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class LevelTestResultScreenshotTest {
    @Test
    fun resultA1() = capture("LevelTestResultA1", LevelTestResultUiState(level = Level.A1))

    @Test
    fun resultB1() = capture("LevelTestResultB1", LevelTestResultUiState(level = Level.B1))

    @Test
    fun resultNat() = capture("LevelTestResultNat", LevelTestResultUiState(level = Level.NAT))

    @Test
    fun picker() = capture(
        "LevelTestResultPicker",
        LevelTestResultUiState(level = Level.B1, page = LevelTestResultPage.Picker),
    )

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun resultB1En() = capture("LevelTestResultB1En", LevelTestResultUiState(level = Level.B1))

    @Test
    @Config(fontScale = 1.3f)
    fun resultC1FontScale130() = capture("LevelTestResultC1_fontScale130", LevelTestResultUiState(level = Level.C1))

    private fun capture(name: String, state: LevelTestResultUiState) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LevelTestResultScreen(state = state, onIntent = {})
            }
        }
    }
}
