package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.components.RatingPromptDialog
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class RatingPromptScreenshotTest {

    @Test
    fun ratingPromptDialog() {
        captureRoboImage("src/androidHostTest/screenshots/RatingPromptDialog.png") {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.fillMaxSize().background(LexiTheme.colors.cream)) {
                    RatingPromptDialog(onRate = {}, onLater = {})
                }
            }
        }
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun ratingPromptDialogEnglish() {
        captureRoboImage("src/androidHostTest/screenshots/RatingPromptDialogEnglish.png") {
            LexiTheme(reducedMotion = false) {
                Box(Modifier.fillMaxSize().background(LexiTheme.colors.cream)) {
                    RatingPromptDialog(onRate = {}, onLater = {})
                }
            }
        }
    }
}
