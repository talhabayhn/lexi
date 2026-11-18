package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeUp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Every button of the video-flow Home sends its intent (the view model turns each into its effect). */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class HomeScreenBehaviourTest {

    private fun clicking(state: HomeUiState = HomeSampleState, target: ComposeUiTest.() -> Unit): List<HomeIntent> {
        val intents = mutableListOf<HomeIntent>()
        runComposeUiTest {
            setContent { LexiTheme(reducedMotion = true) { HomeScreen(state, onIntent = { intents += it }) } }
            target()
        }
        return intents
    }

    @Test
    fun `the play card is one button that sends play`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.Play),
            clicking {
                onNode(hasContentDescription("Oyna.", substring = true))
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                    .performClick()
                // The mode boxes are information inside the one button, not buttons of their own.
                onNodeWithText("Hardcore").assertDoesNotExist()
            },
        )
    }

    @Test
    fun `the bottom bar sends stats, invite with the Turkish landing link and settings`() {
        val intents = clicking {
            onNodeWithContentDescription("İstatistik").performClick()
            onNodeWithContentDescription("Arkadaşını davet et").performClick()
            onNodeWithContentDescription("Ayarlar").performClick()
        }

        assertEquals(HomeIntent.OpenStats, intents[0])
        val invite = intents[1] as HomeIntent.Invite
        assertTrue(invite.message.startsWith("Lexi ile İngilizce kelime çalışıyorum"), invite.message)
        assertTrue(invite.message.endsWith("https://talhabayhn.github.io/lexi/tr/"), invite.message)
        assertEquals(HomeIntent.OpenSettings, intents[2])
    }

    @Test
    fun `the duel box and the study tiles send their intents`() {
        assertEquals(
            listOf(HomeIntent.OpenDuel, HomeIntent.OpenNotebook, HomeIntent.OpenExamPacks),
            clicking {
                onNodeWithText("İki kişi, tek telefon", substring = true).performScrollTo().performClick()
                onNodeWithText("Defter", substring = true).performScrollTo().performClick()
                onNodeWithText("Sınavlar", substring = true).performScrollTo().performClick()
            },
        )
    }

    @Test
    fun `the daily card opens the daily wheel only while it is unsolved`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.OpenDaily),
            clicking {
                onNodeWithContentDescription("Günün çarkı, herkes aynı çarkı çözüyor, çözülmedi").performClick()
            },
        )
        clicking(HomeSampleState.copy(dailyState = DailyCardState.AlreadyPlayed(310, 6, 42))) {
            onNodeWithContentDescription("Günün çarkı, tamamlandı, 310 puan")
                .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        }
        clicking(HomeSampleState.copy(dailyState = DailyCardState.Unavailable)) {
            onNodeWithContentDescription("Günün çarkı, şu an kullanılamıyor")
                .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        }
    }

    @Test
    fun `total points are grouped and read with their label, and Home shows no streak`() {
        clicking {
            onNodeWithContentDescription("Toplam puan: 12.480").assertExists()
            onNode(hasText("seri", substring = true)).assertDoesNotExist()
        }
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
    fun `tapping the gift notice via touch dismisses it`() = runComposeUiTest {
        val intents = mutableListOf<HomeIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(
                    HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)),
                    onIntent = { intents += it },
                )
            }
        }

        onNode(hasText("Günün hediyesi", substring = true)).performTouchInput {
            click()
        }

        assertEquals(listOf<HomeIntent>(HomeIntent.DismissNotice), intents)
    }

    @Test
    fun `the gift toast floats and leaves home's positions unchanged`() = runComposeUiTest {
        var notice by mutableStateOf<DailyGiftNotice?>(null)
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(HomeSampleState.copy(dailyGiftNotice = notice), onIntent = {})
            }
        }
        fun bounds() = listOf(
            onNode(hasContentDescription("Oyna.", substring = true)).getBoundsInRoot(),
            onNodeWithContentDescription("İstatistik").getBoundsInRoot(),
        )

        val before = bounds()
        notice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)
        waitForIdle()
        onNode(hasText("Günün hediyesi", substring = true)).assertExists()
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

    @Test
    fun `swipe-up dismiss animates out with toast duration`() = runComposeUiTest {
        val intents = mutableListOf<HomeIntent>()
        setContent {
            LexiTheme(reducedMotion = false) {
                HomeScreen(
                    HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)),
                    onIntent = { intents += it },
                )
            }
        }
        mainClock.autoAdvance = false
        // Let SlidingToast entrance finish (180 ms)
        mainClock.advanceTimeBy(300)

        onNode(hasText("Günün hediyesi", substring = true)).performTouchInput {
            swipeUp()
        }

        // Mid-animation: not dismissed yet
        mainClock.advanceTimeBy(50)
        assertTrue(intents.isEmpty())

        // After motion.toastDurationMillis (180 ms + slack): dismissed
        mainClock.advanceTimeBy(200)
        assertEquals(listOf<HomeIntent>(HomeIntent.DismissNotice), intents)
    }

    @Test
    fun `swipe-up with reduced motion dismisses instantly`() = runComposeUiTest {
        val intents = mutableListOf<HomeIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(
                    HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)),
                    onIntent = { intents += it },
                )
            }
        }
        onNode(hasText("Günün hediyesi", substring = true)).performTouchInput {
            swipeUp()
        }
        assertEquals(listOf<HomeIntent>(HomeIntent.DismissNotice), intents)
    }

    @Test
    fun `drag below threshold springs back and does not dismiss`() = runComposeUiTest {
        val intents = mutableListOf<HomeIntent>()
        setContent {
            LexiTheme(reducedMotion = false) {
                HomeScreen(
                    HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)),
                    onIntent = { intents += it },
                )
            }
        }
        onNode(hasText("Günün hediyesi", substring = true)).performTouchInput {
            val h = bottom - top
            down(center)
            moveBy(Offset(0f, -h * 0.15f), delayMillis = 300)
            up()
        }
        waitForIdle()
        assertTrue(intents.isEmpty())
        onNode(hasText("Günün hediyesi", substring = true)).assertExists()
    }

    @Test
    fun `upward fling dismisses the gift notice`() = runComposeUiTest {
        val intents = mutableListOf<HomeIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(
                    HomeSampleState.copy(dailyGiftNotice = DailyGiftNotice(DailyGiftNoticeType.Daily, 1)),
                    onIntent = { intents += it },
                )
            }
        }
        onNode(hasText("Günün hediyesi", substring = true)).performTouchInput {
            swipeUp(durationMillis = 50)
        }
        assertEquals(listOf<HomeIntent>(HomeIntent.DismissNotice), intents)
    }
}
