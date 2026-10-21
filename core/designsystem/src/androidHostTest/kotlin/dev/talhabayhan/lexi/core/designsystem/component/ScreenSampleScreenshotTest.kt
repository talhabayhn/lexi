package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Components laid out like the screens that use them, at xhdpi (the 2x scale of the design PNGs), for crop-by-crop
 * comparison. Layout values are the screens' HTML; these are not the feature screens (M4+).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xhdpi")
class ScreenSampleScreenshotTest {

    @Test
    fun levelSelectComponents() {
        captureRoboImage("src/androidHostTest/screenshots/LevelSelectComponents.png") {
            LexiTheme(reducedMotion = false) { LevelSelectSample() }
        }
    }

    @Test
    fun settingsComponents() {
        captureRoboImage("src/androidHostTest/screenshots/SettingsComponents.png") {
            LexiTheme(reducedMotion = false) { SettingsSample() }
        }
    }

    @Test
    fun statsComponents() {
        captureRoboImage("src/androidHostTest/screenshots/StatsComponents.png") {
            LexiTheme(reducedMotion = false) { StatsSample() }
        }
    }

    @Test
    fun jokerSheetComponents() {
        captureRoboImage("src/androidHostTest/screenshots/JokerSheetComponents.png") {
            LexiTheme(reducedMotion = false) { JokerSheetSample() }
        }
    }

    @Test
    fun homeComponents() {
        captureRoboImage("src/androidHostTest/screenshots/HomeComponents.png") {
            LexiTheme(reducedMotion = false) { HomeSample() }
        }
    }

    /** The top of `13-logo-and-icon` (1330 wide) plus the option-1 icon at 180, 60 and 40. */
    @Test
    @Config(qualifiers = "w1330dp-h844dp-xhdpi")
    fun logoComponents() {
        captureRoboImage("src/androidHostTest/screenshots/LogoComponents.png") {
            LexiTheme(reducedMotion = false) { LogoSample() }
        }
    }

    @Test
    fun notebookComponents() {
        captureRoboImage("src/androidHostTest/screenshots/NotebookComponents.png") {
            LexiTheme(reducedMotion = false) { NotebookSample() }
        }
    }
}

@Composable
private fun SampleScreen(
    top: Dp,
    bottom: Dp,
    gap: Dp,
    background: Color = LexiTheme.colors.cream,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(start = 20.dp, top = top, end = 20.dp, bottom = bottom),
        verticalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}

@Composable
private fun SampleHeader(title: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        NavSquareButton(LexiIcons.Back, "Geri", onClick = {})
        Text(title, style = LexiTheme.typography.title)
    }
}

@Composable
private fun SettingsSection(label: String, rows: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = LexiTheme.typography.label12)
        ListCard(Modifier.fillMaxWidth(), content = rows)
    }
}

@Composable
private fun SettingsSample() {
    val colors = LexiTheme.colors
    SampleScreen(top = 24.dp, bottom = 20.dp, gap = 14.dp) {
        SampleHeader("Ayarlar")
        LexiButton(
            onClick = {},
            variant = LexiButtonVariant.Ink,
            height = 94.dp,
            shadow = LexiTheme.shadows.s5Red,
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            DecorShape(DecorShapeKind.Diamond, colors.yellow, Modifier.size(34.dp))
            Column(Modifier.weight(1f)) {
                Text("Lexi Pro", style = LexiTheme.typography.heading18, color = colors.cream)
                Text(
                    "Reklamsız · tüm seviyeler · sınav paketleri",
                    style = LexiTheme.typography.caption13,
                    color = colors.cream,
                )
            }
            Text("[FİYAT]", style = LexiTheme.typography.bodyStrong14, color = colors.yellow)
        }
        SettingsSection("OYUN") {
            ListRow("Ses efektleri", checked = true, onCheckedChange = {})
            ListRow("Titreşim", checked = true, onCheckedChange = {})
            ListRow("Doğru cevapta telaffuzu çal", checked = true, onCheckedChange = {})
            ListRow("Klasik mod süresi", onClick = {}) { ListRowValue("3:00") }
        }
        SettingsSection("ÖĞRENME") {
            ListRow("Günlük hatırlatma", onClick = {}) { ListRowValue("20:00") }
            ListRow("Günlük hedef", onClick = {}) { ListRowValue("1 çark") }
            ListRow("Kelime defteri tekrarları", checked = true, onCheckedChange = {})
        }
        SettingsSection("DİL") {
            ListRow("Arayüz dili", onClick = {}) { ListRowValue("Türkçe") }
            ListRow("İpucu dili", onClick = {}) { ListRowValue("Türkçe") }
            ListRow("İngilizce aksanı", onClick = {}) { ListRowValue("US") }
        }
    }
}

@Composable
private fun NotebookSample() {
    val colors = LexiTheme.colors
    SampleScreen(top = 24.dp, bottom = 20.dp, gap = 14.dp) {
        SampleHeader("Kelime Defterim")
        SegmentedControl(
            segments = listOf(Segment("Tekrar · 24"), Segment("Tümü · 412"), Segment("Zor · 31")),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier.fillMaxWidth(),
        )
        // The search field is not a design-system component (feature UI, M5): its 48dp slot is kept empty.
        Spacer(Modifier.height(48.dp))
        LexiButton(
            onClick = {},
            variant = LexiButtonVariant.Blue,
            height = LexiButtonDefaults.LargerHeight,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Tekrar çarkını başlat", style = LexiTheme.typography.heading18, color = colors.white)
            Text(
                "24 kelime",
                style = LexiTheme.typography.caption14.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .lexiSurface(colors.yellow, BorderStroke(2.dp, colors.ink))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        ListCard(Modifier.fillMaxWidth()) {
            WordRow("egg", "yumurta", "A1 · tekrar: bugün", 1, onSpeak = {}, speakContentDescription = "egg")
            WordRow("mirror", "ayna", "A1 · tekrar: bugün", 1, onSpeak = {}, speakContentDescription = "mirror")
            WordRow("chair", "sandalye", "A1 · tekrar: bugün", 2, onSpeak = {}, speakContentDescription = "chair")
            WordRow("horse", "at", "A1 · tekrar: bugün", 2, onSpeak = {}, speakContentDescription = "horse")
            WordRow("yesterday", "dün", "A1 · tekrar: yarın", 3, onSpeak = {}, speakContentDescription = "yesterday")
            WordRow("neighbour", "komşu", "A2 · tekrar: 3 gün sonra", 3, onSpeak = {
            }, speakContentDescription = "neighbour")
        }
    }
}

private data class LevelSample(
    val title: String,
    val subtitle: String,
    val progress: Float,
    val label: String,
    val marker: DecorShapeKind,
)

@Composable
private fun LevelSelectSample() {
    val colors = LexiTheme.colors
    val levels = listOf(
        LevelSample("A1", "Başlangıç", 0.48f, "412 / 850 kelime", DecorShapeKind.Square),
        LevelSample("A2", "Temel", 0.11f, "96 / 900 kelime", DecorShapeKind.Circle),
        LevelSample("B1", "Orta", 0f, "0 / 1200 kelime", DecorShapeKind.Leaf),
        LevelSample("B2", "İyi orta", 0f, "0 / 1400 kelime", DecorShapeKind.Square),
        LevelSample("C1", "İleri", 0f, "0 / 1500 kelime", DecorShapeKind.Circle),
        LevelSample("NAT", "Deyim · phrasal verb", 0f, "0 / 800 kelime", DecorShapeKind.Leaf),
    )
    val markerColors = listOf(colors.yellow, colors.blue, colors.red, colors.yellow, colors.blue, colors.red)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.cream)
            .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            NavSquareButton(LexiIcons.Back, "Geri", onClick = {})
            Text("Seviyeni seç", style = LexiTheme.typography.title)
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            levels.chunked(2).forEachIndexed { row, pair ->
                // Like LevelSelectScreen: a row as tall as its taller card, both cards filling it (LevelCard fills
                // the height it is given, so an unbounded row would stretch it to the page).
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    pair.forEachIndexed { column, level ->
                        val index = row * 2 + column
                        LevelCard(
                            title = level.title,
                            subtitle = level.subtitle,
                            progress = level.progress,
                            progressLabel = level.label,
                            marker = level.marker,
                            markerColor = markerColors[index],
                            onClick = {},
                            style = when (index) {
                                0 -> LevelCardStyle.Selected
                                5 -> LevelCardStyle.Inverted
                                else -> LevelCardStyle.Normal
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("OYUN MODU", style = LexiTheme.typography.label12)
            SegmentedControl(
                segments = listOf(
                    Segment("Klasik", "3:00"),
                    Segment("Pratik", "süresiz"),
                    Segment("Hardcore", "tek hata"),
                ),
                selectedIndex = 0,
                onSelect = {},
                height = ControlDefaults.SegmentHeightWithSubtitle,
                dividerWidth = LexiTheme.borders.regular,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.weight(1f))
        LexiButton(
            onClick = {},
            height = LexiButtonDefaults.StartHeight,
            shadow = LexiTheme.shadows.s6,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("BAŞLA", style = LexiTheme.typography.button24, color = Color.White)
            Text(
                "A1 · KLASİK · 26 HARF",
                style = LexiTheme.typography.caption14.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 0.06.em,
                ),
                color = Color.White,
            )
        }
    }
}

@Composable
private fun StatsSample() {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    SampleScreen(top = 24.dp, bottom = 20.dp, gap = 14.dp) {
        SampleHeader("İstatistikler")
        StatGrid(columns = 2, modifier = Modifier.fillMaxWidth()) {
            StatTile("412", "öğrenilen kelime", DecorShapeKind.Circle, colors.blue)
            StatTile("%78", "doğruluk", DecorShapeKind.Square, colors.red)
            StatTile("12 gün", "en uzun seri", DecorShapeKind.Leaf, colors.yellow)
            StatTile("63", "tamamlanan çark", DecorShapeKind.Square, colors.ink)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .lexiSurface(colors.white, BorderStroke(3.dp, colors.ink))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Bu hafta öğrenilen kelime",
                    style = typography.bodyStrong16,
                    modifier = Modifier.alignByBaseline(),
                )
                Text("toplam 177", style = typography.caption13SemiBold, modifier = Modifier.alignByBaseline())
            }
            BarChart(
                values = listOf(18, 26, 12, 31, 22, 40, 28),
                labels = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"),
                highlightIndex = 6,
                contentDescription = "Günlük öğrenilen kelime",
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("SEVİYE İLERLEMESİ", style = typography.label12)
            listOf(Triple("A1", 0.48f, "412 / 850"), Triple("A2", 0.11f, "96 / 900"), Triple("B1", 0f, "0 / 1200"))
                .forEach { (level, progress, count) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(level, style = typography.bodyStrong15, modifier = Modifier.width(34.dp))
                        ProgressBar(
                            progress = progress,
                            height = ControlDefaults.ProgressHeightThick,
                            fillColor = colors.ink,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            count,
                            style = typography.caption13SemiBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(64.dp),
                        )
                    }
                }
        }
    }
}

private data class JokerSample(
    val count: String,
    val title: String,
    val text: String,
    val cost: String,
    val marker: DecorShapeKind,
)

@Composable
private fun JokerSheetSample() {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val jokers = listOf(
        JokerSample("2", "Harf aç", "Sıradaki boş kutuya doğru harfi koyar", "−10 sn", DecorShapeKind.Circle),
        JokerSample("1", "Çeldiricileri temizle", "Yanlış harf butonlarını kaldırır", "−15 sn", DecorShapeKind.Square),
        JokerSample("3", "Örnek cümle", "Kelimeyi boşlukla bir cümlede gösterir", "−5 puan", DecorShapeKind.Leaf),
        JokerSample(
            "0",
            "Telaffuzu dinle",
            "Cevabı sesli okur (C1 ve üstünde kapalı)",
            "−10 puan",
            DecorShapeKind.Square,
        ),
    )
    val chipColors = listOf(colors.blue, colors.red, colors.yellow, colors.white)
    Box(Modifier.fillMaxSize().background(colors.cream)) {
        BottomSheet(visible = true, onDismissRequest = {}, title = "Joker kullan", closeContentDescription = "Kapat") {
            jokers.forEachIndexed { i, joker ->
                val enabled = i < 3
                PressableSurface(
                    onClick = {},
                    enabled = enabled,
                    shadow = if (enabled) LexiTheme.shadows.s3 else null,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp).alpha(if (enabled) 1f else 0.5f),
                    propagateMinConstraints = true,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            DecorShape(
                                joker.marker,
                                chipColors[i],
                                Modifier.fillMaxSize(),
                                BorderStroke(2.dp, colors.ink),
                            )
                            Text(
                                joker.count,
                                style = typography.bodyStrong17,
                                color = if (i < 2) colors.white else colors.ink,
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(joker.title, style = typography.bodyStrong17)
                            Text(joker.text, style = typography.caption13)
                        }
                        Text(
                            joker.cost,
                            style = typography.bodyStrong14,
                            modifier = Modifier
                                .lexiSurface(colors.cream, BorderStroke(2.dp, colors.ink))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
            LexiButton(
                text = "Kısa reklam izle · +1 joker",
                onClick = {},
                variant = LexiButtonVariant.Ink,
                shadow = null,
                leadingIcon = LexiIcons.Play,
                height = LexiButtonDefaults.LargeHeight,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun HomeSample() {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Box(Modifier.fillMaxSize().background(colors.cream)) {
        DecorShape(DecorShapeKind.Circle, colors.blue, Modifier.offset(x = 320.dp, y = 210.dp).size(140.dp))
        DecorShape(DecorShapeKind.HalfPill, colors.red, Modifier.offset(x = (-30).dp, y = 514.dp).size(60.dp, 120.dp))
        SampleScreen(top = 24.dp, bottom = 28.dp, gap = 20.dp, background = Color.Transparent) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                NavSquareButton(
                    LexiIcons.Settings,
                    "Ayarlar",
                    onClick = {},
                    iconSize = IconButtonDefaults.IconSizeMedium,
                )
            }
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LexiLogo()
                Text("A'DAN Z'YE İNGİLİZCE", style = typography.label15)
            }
            // Daily card: feature UI (M6), slot kept.
            Spacer(Modifier.height(106.dp))
            LexiButton(
                onClick = {},
                height = LexiButtonDefaults.PlayHeight,
                shadow = LexiTheme.shadows.s6,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("OYNA", style = typography.button30, color = colors.white)
                Text(
                    "A1 · KLASİK",
                    style = typography.bodyStrong14.copy(letterSpacing = 0.08.em),
                    color = colors.ink,
                    modifier = Modifier
                        .lexiSurface(colors.white, BorderStroke(2.dp, colors.ink))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuTile("Kelime Defterim", "24 tekrar bekliyor", DecorShapeKind.Circle, colors.blue, {
                    }, Modifier.weight(1f))
                    MenuTile("İstatistikler", "412 kelime öğrenildi", DecorShapeKind.Square, colors.red, {
                    }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuTile("Pratik modu", "Süresiz, baskısız", DecorShapeKind.Leaf, colors.yellow, {
                    }, Modifier.weight(1f))
                    MenuTile(
                        "Sınav paketleri",
                        "LGS · YKS-Dil · YDS",
                        DecorShapeKind.Diamond,
                        colors.yellow,
                        {},
                        Modifier.weight(1f),
                        inverted = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoSample() {
    val typography = LexiTheme.typography
    Column(
        modifier = Modifier.fillMaxSize().background(LexiTheme.colors.cream).padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(Modifier.width(709.83.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("ANA LOGO", style = typography.label13)
                LexiLogo(size = LexiLogoSize.Hero)
            }
            Column(Modifier.width(500.17.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("YATAY YAZI + KÜÇÜK KULLANIM", style = typography.label13)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .lexiSurface(LexiTheme.colors.white, BorderStroke(3.dp, LexiTheme.colors.ink))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LexiWordmark()
                    Spacer(Modifier.weight(1f))
                    Text("A'dan Z'ye İngilizce", style = typography.caption14)
                }
                LexiLogoMark()
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.Bottom) {
            LexiAppIcon()
            LexiAppIcon(size = 60.dp)
            LexiAppIcon(size = 40.dp)
        }
    }
}
