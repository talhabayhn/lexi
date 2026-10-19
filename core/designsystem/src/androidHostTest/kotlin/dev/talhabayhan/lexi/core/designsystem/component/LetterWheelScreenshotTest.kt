package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Descender letters (J, Q) in the wheel centre with both rule labels: the descender must never touch the label, also
 * at a large font size (the centre shrinks its texts to fit).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xhdpi")
class LetterWheelScreenshotTest {

    @Test
    fun descenderJ() = capture('J', "LetterWheel_J")

    @Test
    fun descenderQ() = capture('Q', "LetterWheel_Q")

    @Test
    @Config(fontScale = 2.0f)
    fun descenderJFontScale200() = capture('J', "LetterWheel_J_fontScale200")

    @Test
    @Config(fontScale = 2.0f)
    fun descenderQFontScale200() = capture('Q', "LetterWheel_Q_fontScale200")

    private fun capture(letter: Char, name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = true) {
                Column(
                    modifier = Modifier.background(LexiTheme.colors.cream).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val letters = ('A'..'Z').map { c ->
                        WheelLetterUi(c, if (c == letter) WheelLetterState.Current else WheelLetterState.Todo)
                    }
                    // design sample, not user-facing
                    LetterWheel(letters = letters, centerLetter = letter, ruleLabel = "İLE BAŞLAR")
                    LetterWheel(letters = letters, centerLetter = letter, ruleLabel = "İÇİNDE GEÇER")
                }
            }
        }
    }
}
