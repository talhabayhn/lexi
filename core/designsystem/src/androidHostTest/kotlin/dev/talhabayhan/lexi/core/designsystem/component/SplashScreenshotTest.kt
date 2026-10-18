package dev.talhabayhan.lexi.core.designsystem.component

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The splash at the size and scale of `docs/design/screens/14-splash.png` (390 × 844dp, xhdpi = 2×). The PNG is the
 * demo captured with every element landed (the white "Uygulamada" annotation box is not UI), i.e. progress 1; the mid
 * animation frame checks the keyframes.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class SplashScreenshotTest {

    @Test
    fun landed() {
        captureRoboImage("src/androidHostTest/screenshots/Splash.png") {
            LexiTheme(reducedMotion = false) { LexiSplash(progress = { 1f }, tagline = TAGLINE) }
        }
    }

    @Test
    fun midAnimation() {
        captureRoboImage("src/androidHostTest/screenshots/SplashMid.png") {
            LexiTheme(reducedMotion = false) { LexiSplash(progress = { 0.3f }, tagline = TAGLINE) }
        }
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun w320h568() = capture("Splash_w320h568")

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi", fontScale = 1.3f)
    fun w320h568FontScale130() = capture("Splash_w320h568_fontScale130")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi")
    fun w360h640() = capture("Splash_w360h640")

    @Test
    @Config(qualifiers = "tr-w360dp-h640dp-xhdpi", fontScale = 1.3f)
    fun w360h640FontScale130() = capture("Splash_w360h640_fontScale130")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi")
    fun w600h960() = capture("Splash_w600h960")

    @Test
    @Config(qualifiers = "tr-w600dp-h960dp-mdpi", fontScale = 1.3f)
    fun w600h960FontScale130() = capture("Splash_w600h960_fontScale130")

    private fun capture(name: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) {
                LexiViewport {
                    LexiSplash(progress = { 1f }, tagline = TAGLINE)
                }
            }
        }
    }

    private companion object {
        const val TAGLINE = "A'DAN Z'YE İNGİLİZCE"
    }
}
