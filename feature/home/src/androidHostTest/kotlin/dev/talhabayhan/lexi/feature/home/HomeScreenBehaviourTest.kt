package dev.talhabayhan.lexi.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runAndroidComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.component.HomeTierSize
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun `the footer keys are one button each and send their intents`() {
        assertEquals(
            listOf(HomeIntent.OpenNotebook, HomeIntent.OpenStats, HomeIntent.OpenExamPacks, HomeIntent.OpenDuel),
            clicking {
                listOf(
                    "Defter, 24 tekrar bekliyor",
                    "İstatistik",
                    "Sınavlar, sınav paketleri",
                    "Düello, iki oyuncu, tek telefon",
                ).forEach { description ->
                    onNodeWithContentDescription(description)
                        .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                        .performClick()
                }
            },
        )
    }

    @Test
    fun `the level chips are a radio group and send the level`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.SelectLevel(Level.B2)),
            clicking {
                onNodeWithContentDescription("A1, Başlangıç")
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
                    .assertIsSelected()
                onNodeWithContentDescription("B2, İyi orta").assertIsNotSelected().performClick()
            },
        )
    }

    @Test
    fun `the mode cells send the mode`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.SelectMode(GameMode.HARDCORE)),
            clicking { onNodeWithText("Hardcore", substring = true).performClick() },
        )
    }

    @Test
    fun `the category row opens the sheet`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.OpenCategories),
            clicking { onNodeWithContentDescription("Kategori: Karışık, değiştirmek için dokun").performClick() },
        )
    }

    @Test
    fun `a category card only selects and the close button closes the sheet`() {
        val intents = mutableListOf<HomeIntent>()
        runComposeUiTest {
            setContent {
                LexiTheme(reducedMotion = true) {
                    HomeScreen(HomeSampleState.copy(categorySheetOpen = true), onIntent = { intents += it })
                }
            }
            onNodeWithContentDescription("Ev ve eşyalar, 41 / 90 kelime")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
                .assertIsNotSelected()
                .performClick()
            onNodeWithContentDescription("Kapat").performClick()
        }
        assertEquals(
            listOf(HomeIntent.SelectCategory(CategoryId("home")), HomeIntent.CloseCategories),
            intents,
        )
    }

    @Test
    fun `the mixed wheel card is selected by default and reads its words`() = runComposeUiTest {
        val intents = mutableListOf<HomeIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(HomeSampleState.copy(categorySheetOpen = true), onIntent = { intents += it })
            }
        }
        onNodeWithContentDescription("Karışık çark, 412 / 850 kelime")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            .assertIsSelected()
            .performClick()
        assertEquals(listOf<HomeIntent>(HomeIntent.SelectCategory(null)), intents)
    }

    @Test
    fun `a failed category read offers retry in the sheet`() {
        val intents = mutableListOf<HomeIntent>()
        runComposeUiTest {
            setContent {
                LexiTheme(reducedMotion = true) {
                    HomeScreen(
                        HomeSampleState.copy(categories = CategoriesState.Error, categorySheetOpen = true),
                        onIntent = { intents += it },
                    )
                }
            }
            onNodeWithText("Kategoriler yüklenemedi.", substring = true).assertExists()
            onNodeWithText("Tekrar dene").performClick()
        }
        assertEquals(listOf<HomeIntent>(HomeIntent.RetryCategories), intents)
    }

    @Test
    fun `system back closes the category sheet`() {
        val intents = mutableListOf<HomeIntent>()
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                LexiTheme(reducedMotion = true) {
                    HomeScreen(HomeSampleState.copy(categorySheetOpen = true), onIntent = { intents += it })
                }
            }
            runOnUiThread { activity!!.onBackPressedDispatcher.onBackPressed() }
            waitForIdle()
        }
        assertEquals(listOf<HomeIntent>(HomeIntent.CloseCategories), intents)
    }

    @Test
    fun `NAT has no category row`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(HomeSampleState.copy(selectedLevel = Level.NAT), onIntent = {})
            }
        }
        onNodeWithText("Kategori").assertDoesNotExist()
        onNodeWithText("NAT · KLASİK · 26 HARF").assertExists()
    }

    @Test
    fun `the notebook badge caps at 99 and hides at 0`() = runComposeUiTest {
        var due by mutableStateOf(120)
        setContent {
            LexiTheme(reducedMotion = true) { HomeScreen(HomeSampleState.copy(dueReviews = due), onIntent = {}) }
        }
        onNodeWithText("99+", useUnmergedTree = true).assertExists()
        due = 0
        waitForIdle()
        onNodeWithText("99+", useUnmergedTree = true).assertDoesNotExist()
        onNodeWithContentDescription("Defter, tekrar yok").assertExists()
    }

    @Test
    fun `the daily card sends open daily`() {
        assertEquals(
            listOf<HomeIntent>(HomeIntent.OpenDaily),
            clicking {
                onNodeWithContentDescription("Günün çarkı, herkes aynı çarkı çözüyor, çözülmedi")
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                    .performClick()
            },
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
            onNodeWithContentDescription("Günün çarkı, tamamlandı, 310 puan").performClick()
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
            onNodeWithContentDescription("Günün çarkı, şu an kullanılamıyor").performClick()
        }
        assertEquals(emptyList(), intents)
    }

    @Test
    fun `the daily card reads its three states and is a button only while unsolved, plain text otherwise`() =
        runComposeUiTest {
            var daily by mutableStateOf<DailyCardState>(DailyCardState.Available)
            setContent {
                LexiTheme(reducedMotion = true) { HomeScreen(HomeSampleState.copy(dailyState = daily), onIntent = {}) }
            }
            onNodeWithContentDescription("Günün çarkı, herkes aynı çarkı çözüyor, çözülmedi")
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Disabled))

            daily = DailyCardState.AlreadyPlayed(1840, hoursUntilNext = 5, minutesUntilNext = 12)
            waitForIdle()
            onNodeWithContentDescription("Günün çarkı, tamamlandı, 1840 puan")
                .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Role))

            daily = DailyCardState.Unavailable
            waitForIdle()
            onNodeWithContentDescription("Günün çarkı, şu an kullanılamıyor")
                .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun `the daily card reads its state in English`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                HomeScreen(
                    HomeSampleState.copy(dailyState = DailyCardState.AlreadyPlayed(320, hoursUntilNext = 2)),
                    onIntent = {},
                )
            }
        }
        onNodeWithContentDescription("Daily wheel, completed, 320 points").assertExists()
    }

    @Test
    fun `OYNA follows the category row when everything fits`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { HomeScreen(HomeSampleState, onIntent = {}) } }
        val category = onNodeWithContentDescription("Kategori: Karışık, değiştirmek için dokun").getBoundsInRoot()
        val play = onNodeWithText("OYNA").getBoundsInRoot()
        val gap = play.top - category.bottom
        assertTrue(
            gap.value in 0f..HomeTierSize.Regular.blockGap.value + HomeTierSize.Regular.playHeight.value,
            "gap $gap",
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
        fun bounds() = listOf("OYNA", "ÇARKINI KUR", "A1 · KLASİK · 26 HARF")
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
