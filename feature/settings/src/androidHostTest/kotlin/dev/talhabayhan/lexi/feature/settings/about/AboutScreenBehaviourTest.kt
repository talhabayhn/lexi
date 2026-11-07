package dev.talhabayhan.lexi.feature.settings.about

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
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
class AboutScreenBehaviourTest {

    @Test
    fun `back button emits Back intent`() = runComposeUiTest {
        val intents = mutableListOf<AboutIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                AboutScreen(AboutUiState(), onIntent = { intents += it })
            }
        }

        onNodeWithContentDescription("Geri").performClick()
        assertEquals(listOf<AboutIntent>(AboutIntent.Back), intents)
    }

    @Test
    fun `rate, legal and support rows emit their intents`() = runComposeUiTest {
        val intents = mutableListOf<AboutIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                AboutScreen(AboutUiState(storePageUrl = "https://store.example/lexi"), onIntent = { intents += it })
            }
        }

        onNodeWithText(RATE_LABEL).performClick()
        onNodeWithText("Gizlilik Politikası").performClick()
        onNodeWithText("Kullanım Koşulları").performClick()
        onNodeWithText("Destek").performClick()

        assertEquals(
            listOf<AboutIntent>(
                AboutIntent.Rate,
                AboutIntent.OpenPrivacy,
                AboutIntent.OpenTerms,
                AboutIntent.OpenSupport,
            ),
            intents,
        )
    }

    private companion object {
        const val RATE_LABEL = "Uygulamayı puanla"
    }
}
