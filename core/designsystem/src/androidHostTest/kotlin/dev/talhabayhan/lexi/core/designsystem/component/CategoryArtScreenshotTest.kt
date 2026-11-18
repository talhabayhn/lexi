package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** All category arts (163 x 95 dp each) plus the 5 exam arts (LGS unlocked, the rest locked). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w880dp-h800dp-mdpi")
class CategoryArtScreenshotTest {

    @Test
    fun allArts() {
        captureRoboImage("src/androidHostTest/screenshots/CategoryArt.png") {
            LexiTheme(reducedMotion = false) {
                Column(
                    Modifier.background(LexiTheme.colors.white).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryArtKind.entries.chunked(5).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { CategoryArt(it, Modifier.size(163.dp, 95.dp)) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExamArtKind.entries.forEach {
                            ExamArt(it, Modifier.size(163.dp, 95.dp), locked = it != ExamArtKind.Lgs)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun newCategoryPreviews() {
        val newKinds = listOf(
            "clothes-shopping" to CategoryArtKind.ClothesShopping,
            "hobbies" to CategoryArtKind.Hobbies,
            "verbs" to CategoryArtKind.Verbs,
            "adjectives" to CategoryArtKind.Adjectives,
            "economy" to CategoryArtKind.Economy,
            "feelings" to CategoryArtKind.Feelings,
            "society-law" to CategoryArtKind.SocietyLaw,
            "ideas" to CategoryArtKind.Ideas,
        )
        newKinds.forEach { (name, kind) ->
            // A review aid, not a golden: always written, never compared (verify has no reference for it).
            captureRoboImage("build/art-previews/$name.png", roborazziOptions = PreviewOnly) {
                LexiTheme(reducedMotion = false) {
                    CategoryArt(kind, Modifier.size(163.dp, 95.dp))
                }
            }
        }
    }
}

private val PreviewOnly = RoborazziOptions(taskType = RoborazziTaskType.Record)
