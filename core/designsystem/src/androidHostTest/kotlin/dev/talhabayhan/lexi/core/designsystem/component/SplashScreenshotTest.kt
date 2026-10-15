package dev.talhabayhan.lexi.core.designsystem.component

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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

    private companion object {
        const val TAGLINE = "A'DAN Z'YE İNGİLİZCE"
    }
}
