package dev.talhabayhan.lexi.core.designsystem

import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class ToolchainProbeScreenshotTest {
    @Test
    fun toolchainProbe() {
        captureRoboImage("src/androidHostTest/screenshots/ToolchainProbe.png") {
            ToolchainProbe()
        }
    }
}
