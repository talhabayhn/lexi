package dev.talhabayhan.lexi.feature.home

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

/** Every button of the home sends its intent (the view model turns each into its navigation effect). */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class HomeScreenBehaviourTest {

    private fun clicking(target: ComposeUiTest.() -> Unit): List<HomeIntent> {
        val intents = mutableListOf<HomeIntent>()
        runComposeUiTest {
            setContent {
                LexiTheme(reducedMotion = true) { HomeScreen(HomeSampleState, onIntent = { intents += it }) }
            }
            target()
        }
        return intents
    }

    @Test
    fun `the gear sends open settings`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.OpenSettings),
            clicking { onNodeWithContentDescription("Ayarlar").performClick() },
        )
    }

    @Test
    fun `the play button sends play`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.Play),
            clicking { onNodeWithText("OYNA").performScrollTo().performClick() },
        )
    }

    @Test
    fun `the daily card sends open daily`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.OpenDaily),
            clicking { onNodeWithText("Herkes aynı çarkı çözüyor").performScrollTo().performClick() },
        )
    }

    @Test
    fun `the menu tiles send their intents`() {
        assertEquals(
            listOf(
                HomeIntent.OpenNotebook,
                HomeIntent.OpenStats,
                HomeIntent.OpenPractice,
                HomeIntent.OpenExamPacks,
            ),
            clicking {
                onNodeWithText("Kelime Defterim").performScrollTo().performClick()
                onNodeWithText("İstatistikler").performScrollTo().performClick()
                onNodeWithText("Pratik modu").performScrollTo().performClick()
                onNodeWithText("Sınav paketleri").performScrollTo().performClick()
            },
        )
    }
}
