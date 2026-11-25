package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The video-flow components side by side (`home_v5.png`, `cats_v3.png`). Goldens are recorded on CI
 * (`screenshots-record`), never on macOS.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h1000dp-mdpi")
class FlowComponentsScreenshotTest {

    @Test
    fun flowComponents() {
        captureRoboImage("src/androidHostTest/screenshots/FlowComponents.png") { Sample() }
    }

    @Test
    @Config(qualifiers = "w320dp-h1000dp-mdpi", fontScale = 2.0f)
    fun flowComponentsNarrowLargeFont() {
        captureRoboImage("src/androidHostTest/screenshots/FlowComponents_w320_fontScale200.png") { Sample() }
    }

    @Composable
    private fun Sample() {
        LexiTheme(reducedMotion = false) {
            Column(
                Modifier.fillMaxSize().background(LexiTheme.colors.cream).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PlayCard(
                    label = "SEVİYENİ, KATEGORİNİ VE MODUNU SEÇ",
                    playLabel = "OYNA",
                    contentDescription = "Oyna",
                    onClick = {},
                )
                DailyWheelCard("Günün çarkı", "Herkes aynı çarkı çözüyor", DailyCardStatus.Solve, "Çöz", "", {})
                DailyWheelCard(
                    "Günün çarkı",
                    "1840 puan · yeni çark 5 sa 12 dk",
                    DailyCardStatus.Completed,
                    "Tamamlandı",
                    "",
                    {},
                )
                LevelChipRow(listOf("A1", "A2", "B1", "B2", "C1", "NAT"), selectedIndex = 3, onSelect = {})
                SegmentedControl(
                    listOf(Segment("3:00"), Segment("5:00"), Segment("7:00")),
                    selectedIndex = 0,
                    onSelect = {},
                    role = Role.RadioButton,
                    enabled = false,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerCard(
                        name = "Günlük hayat",
                        contentDescription = "",
                        art = { CategoryArt(CategoryArtKind.DailyLife, it) },
                        modifier = Modifier.weight(1f),
                        progressText = "120 / 240",
                        progress = 0.5f,
                        onClick = {},
                    )
                    PickerCard(
                        name = "YDS / YÖKDİL",
                        contentDescription = "",
                        art = { ExamArt(ExamArtKind.YdsYokdil, it, locked = true) },
                        modifier = Modifier.weight(1f),
                        locked = true,
                        lockedLabel = "YAKINDA",
                    )
                }
                ScrollToTopPill("Başa dön", onClick = {})
                RuledBottomBar(
                    listOf(
                        BottomBarItem(LexiIcons.Stats, "İstatistik", {}),
                        BottomBarItem(LexiIcons.Invite, "Davet et", {}),
                        BottomBarItem(LexiIcons.Settings, "Ayarlar", {}),
                    ),
                )
            }
        }
    }
}
