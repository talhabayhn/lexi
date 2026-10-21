package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
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
            clicking { onNodeWithText("OYNA").performClick() },
        )
    }

    @Test
    fun `the daily card sends open daily`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.OpenDaily),
            clicking { onNodeWithText("Herkesle aynı çark: bildiğin kadarını çöz").performClick() },
        )
    }

    @Test
    fun `the daily card is inert when already played`() {
        val intents = mutableListOf<HomeIntent>()
        runComposeUiTest {
            setContent {
                LexiTheme(reducedMotion = true) {
                    HomeScreen(
                        HomeSampleState.copy(dailyState = DailyCardState.AlreadyPlayed(310, hoursUntilNext = 6)),
                        onIntent = { intents += it },
                    )
                }
            }
            onNodeWithText("Tamamlandı · 310 puan").performClick()
            onNodeWithText("Yeni çark: 6 sa 0 dk").assertExists()
        }
        assertEquals(emptyList(), intents)
    }

    @Test
    fun `the daily card is inert when unavailable`() {
        val intents = mutableListOf<HomeIntent>()
        runComposeUiTest {
            setContent {
                LexiTheme(reducedMotion = true) {
                    HomeScreen(
                        HomeSampleState.copy(dailyState = DailyCardState.Unavailable),
                        onIntent = { intents += it },
                    )
                }
            }
            onNodeWithText("Kullanılamıyor").performClick()
        }
        assertEquals(emptyList(), intents)
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
                onNodeWithText("Kelime Defterim").performClick()
                onNodeWithText("İstatistikler").performClick()
                onNodeWithText("Pratik modu").performClick()
                onNodeWithText("Sınav paketleri").performClick()
            },
        )
    }

    @Test
    fun `the gift notice is a dismiss button with a label`() = runComposeUiTest {
        val intents = mutableListOf<HomeIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(
                    HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)),
                    onIntent = { intents += it },
                )
            }
        }

        onNode(hasText("Günün hediyesi", substring = true))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
            .performClick()

        assertEquals(listOf<HomeIntent>(HomeIntent.DismissNotice), intents)
    }

    @Test
    fun `the gift toast floats and leaves home's sizes and positions unchanged`() = runComposeUiTest {
        var notice by mutableStateOf<DailyGiftNotice?>(null)
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(HomeSampleState.copy(dailyGiftNotice = notice), onIntent = {})
            }
        }
        fun bounds() = listOf("OYNA", "Herkesle aynı çark: bildiğin kadarını çöz", "Kelime Defterim")
            .map { onNodeWithText(it).getBoundsInRoot() }

        val before = bounds()
        notice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)
        waitForIdle()
        onNode(hasText("Günün hediyesi", substring = true)).assertExists()
        assertEquals(before, bounds())

        notice = null
        waitForIdle()
        onNode(hasText("Günün hediyesi", substring = true)).assertDoesNotExist()
        assertEquals(before, bounds())
    }

    @Test
    fun `the gift toast waits until the screen is resumed`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(
                    HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)),
                    onIntent = {},
                    noticeVisible = false,
                )
            }
        }
        onNode(hasText("Günün hediyesi", substring = true)).assertDoesNotExist()
    }
}
