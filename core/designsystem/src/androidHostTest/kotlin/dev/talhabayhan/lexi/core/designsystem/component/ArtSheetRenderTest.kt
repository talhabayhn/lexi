package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Renders the review contact sheet of every category and exam card (real card size, then the art alone at 0.75x) as a
 * plain PNG into `build/art-sheet/`. A generator, not a check: it only runs with `LEXI_ART_SHEET=1`
 * (`LEXI_ART_SHEET=1 ./gradlew :core:designsystem:testAndroidHostTest --tests '*ArtSheetRenderTest*'`). Copy it to `docs/design/drafts/video-flow/actual/art-sheet.png`, see
 * `docs/design/video-flow-notes.md`). It is a design-review aid, not a golden: nothing is compared or recorded.
 * Names are the Turkish `content/categories.csv` names, spelled here because the sheet is not user-facing.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w1000dp-h2200dp-xhdpi")
class ArtSheetRenderTest {
    @Test
    fun rendersTheContactSheet() = runComposeUiTest {
        assumeTrue("set LEXI_ART_SHEET=1 to render the sheet", System.getenv("LEXI_ART_SHEET") == "1")
        setContent { LexiTheme(reducedMotion = false) { Sheet() } }
        val bitmap = onNodeWithTag(TAG).captureToImage().asAndroidBitmap()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val image = BufferedImage(bitmap.width, bitmap.height, BufferedImage.TYPE_INT_RGB)
        image.setRGB(0, 0, bitmap.width, bitmap.height, pixels, 0, bitmap.width)
        OUTPUT.parentFile.mkdirs()
        assertTrue(ImageIO.write(image, "png", OUTPUT))
    }

    @Composable
    private fun Sheet() {
        Column(
            Modifier.testTag(TAG).width(SHEET_WIDTH).background(LexiTheme.colors.cream).padding(SHEET_PADDING),
            verticalArrangement = Arrangement.spacedBy(SECTION_GAP),
        ) {
            Heading("Categories (${CategoryArtKind.entries.size}), card size")
            Cards {
                CategoryArtKind.entries.forEach { kind ->
                    PickerCard(
                        name = categoryName(kind),
                        contentDescription = "",
                        art = { CategoryArt(kind, it) },
                        modifier = Modifier.width(CARD_WIDTH),
                        progressText = "0 / 0",
                        onClick = {},
                    )
                }
            }
            Heading("Exam packs (${ExamArtKind.entries.size}), open then locked")
            Cards {
                ExamArtKind.entries.forEach { kind ->
                    PickerCard(
                        name = examName(kind),
                        contentDescription = "",
                        art = { ExamArt(kind, it) },
                        modifier = Modifier.width(CARD_WIDTH),
                        progressText = "0 / 0",
                        role = androidx.compose.ui.semantics.Role.Button,
                        onClick = {},
                    )
                }
                ExamArtKind.entries.forEach { kind ->
                    PickerCard(
                        name = examName(kind),
                        contentDescription = "",
                        art = { ExamArt(kind, it, locked = true) },
                        modifier = Modifier.width(CARD_WIDTH),
                        locked = true,
                        lockedLabel = "YAKINDA",
                    )
                }
            }
            Heading("Bottom bar icons: real size (24 dp) and 4x")
            Cards(gap = SMALL_GAP) {
                listOf(LexiIcons.Stats, LexiIcons.Invite, LexiIcons.Settings).forEach { icon ->
                    Icon(icon, null, Modifier.size(ICON_SIZE), tint = LexiTheme.colors.ink)
                }
                listOf(LexiIcons.Stats, LexiIcons.Invite, LexiIcons.Settings).forEach { icon ->
                    Icon(icon, null, Modifier.size(ICON_SIZE * ICON_ZOOM), tint = LexiTheme.colors.ink)
                }
            }
            Heading("Art alone at 0.75x")
            Cards(gap = SMALL_GAP) {
                CategoryArtKind.entries.forEach { CategoryArt(it, Modifier.size(SMALL_ART_WIDTH, SMALL_ART_HEIGHT)) }
                ExamArtKind.entries.forEach { ExamArt(it, Modifier.size(SMALL_ART_WIDTH, SMALL_ART_HEIGHT)) }
            }
        }
    }

    @Composable
    private fun Heading(text: String) {
        Box { Text(text, style = LexiTheme.typography.bodyStrong14, color = LexiTheme.colors.ink) }
    }

    @Composable
    private fun Cards(gap: androidx.compose.ui.unit.Dp = CARD_GAP, content: @Composable () -> Unit) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) { content() }
    }

    private fun categoryName(kind: CategoryArtKind) = when (kind) {
        CategoryArtKind.Mixed -> "Karışık"
        CategoryArtKind.DailyLife -> "Günlük hayat"
        CategoryArtKind.FoodDrink -> "Yiyecek ve içecek"
        CategoryArtKind.Home -> "Ev ve eşyalar"
        CategoryArtKind.FamilyPeople -> "Aile ve insanlar"
        CategoryArtKind.ColorsNumbers -> "Renkler ve sayılar"
        CategoryArtKind.BodyHealth -> "Vücut ve sağlık"
        CategoryArtKind.SchoolWork -> "Okul ve iş"
        CategoryArtKind.Animals -> "Hayvanlar"
        CategoryArtKind.CityTransport -> "Şehir ve ulaşım"
        CategoryArtKind.TimeWeather -> "Zaman ve hava"
        CategoryArtKind.Travel -> "Seyahat"
        CategoryArtKind.Sports -> "Spor"
        CategoryArtKind.Music -> "Müzik"
        CategoryArtKind.FilmTv -> "Film ve dizi"
        CategoryArtKind.Business -> "İş İngilizcesi"
        CategoryArtKind.Technology -> "Teknoloji"
        CategoryArtKind.FalseFriends -> "Yalancı eşdeğerler"
        CategoryArtKind.Idioms -> "Deyimler"
        CategoryArtKind.PhrasalVerbs -> "Phrasal verbs"
        CategoryArtKind.ClothesShopping -> "Giysi ve alışveriş"
        CategoryArtKind.Hobbies -> "Hobiler ve eğlence"
        CategoryArtKind.Verbs -> "Fiiller"
        CategoryArtKind.Adjectives -> "Sıfatlar ve zarflar"
        CategoryArtKind.Economy -> "Para ve ekonomi"
        CategoryArtKind.Feelings -> "Duygu ve karakter"
        CategoryArtKind.SocietyLaw -> "Toplum ve hukuk"
        CategoryArtKind.Ideas -> "Fikirler ve kavramlar"
    }

    private fun examName(kind: ExamArtKind) = when (kind) {
        ExamArtKind.Lgs -> "LGS"
        ExamArtKind.YdsYokdil -> "YDS / YÖKDİL"
        ExamArtKind.YksDil -> "YKS-DİL"
        ExamArtKind.Toefl -> "TOEFL"
        ExamArtKind.Ielts -> "IELTS"
    }

    private companion object {
        const val TAG = "artSheet"
        val SHEET_WIDTH = 1000.dp
        val SHEET_PADDING = 24.dp
        val SECTION_GAP = 16.dp
        val CARD_WIDTH = 169.dp
        val CARD_GAP = 14.dp
        val SMALL_GAP = 8.dp
        val ICON_SIZE = 24.dp
        const val ICON_ZOOM = 4
        val SMALL_ART_WIDTH = 122.dp
        val SMALL_ART_HEIGHT = 71.dp

        /** Relative to the module directory (the test working directory). */
        val OUTPUT = File("build/art-sheet/art-sheet.png")
    }
}
