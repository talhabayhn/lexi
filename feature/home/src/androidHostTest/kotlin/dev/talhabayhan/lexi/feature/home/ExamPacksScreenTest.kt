package dev.talhabayhan.lexi.feature.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
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
class ExamPacksScreenTest {

    @Test
    fun `shows the soon message and back goes back`() = runComposeUiTest {
        var backs = 0
        setContent { LexiTheme(reducedMotion = true) { ExamPacksScreen(onBack = { backs++ }) } }

        onNodeWithText("YAKINDA: LGS, YKS-Dil ve YDS çarkları hazırlanıyor.").assertIsDisplayed()
        onNodeWithContentDescription("Geri").performClick()

        assertEquals(1, backs)
    }

    @Test
    fun comingSoon() {
        captureRoboImage("src/androidHostTest/screenshots/ExamPacksSoon.png") {
            LexiTheme(reducedMotion = false) { ExamPacksScreen(onBack = {}) }
        }
    }
}
