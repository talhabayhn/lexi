package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.gallery.ComponentGallery
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xxhdpi")
class ThemeScreenshotTest {

    /** The `16-components` canvas at its own size and the PNG's 2x scale, for a pixel-for-pixel comparison. */
    @Test
    @Config(qualifiers = "w1330dp-h960dp-xhdpi")
    fun componentGallery() {
        captureRoboImage("src/androidHostTest/screenshots/ComponentGallery.png") {
            LexiTheme(reducedMotion = false) { ComponentGallery() }
        }
    }

    @Test
    @Config(qualifiers = "w390dp-h1120dp-xxhdpi")
    fun typographyScale() {
        captureRoboImage("src/androidHostTest/screenshots/TokensTypography.png") {
            LexiTheme(reducedMotion = false) { TypographySheet() }
        }
    }

    @Test
    fun colorPalette() {
        captureRoboImage("src/androidHostTest/screenshots/TokensColors.png") {
            LexiTheme(reducedMotion = false) { ColorSheet() }
        }
    }
}

@Composable
private fun TypographySheet() {
    val t = LexiTheme.typography
    val styles: List<Pair<String, TextStyle>> = listOf(
        "wheelLetter 96/800" to t.wheelLetter,
        "scoreHero 76/800" to t.scoreHero,
        "logo48 48/800" to t.logo48,
        "logo40 40/800" to t.logo40,
        "display 34/800" to t.display,
        "clue 30/800" to t.clue,
        "title 28/800" to t.title,
        "button30 30/800 .1em" to t.button30,
        "button24 24/800 .1em" to t.button24,
        "button22 22/800 .12em" to t.button22,
        "button20 20/800 .06em" to t.button20,
        "button16 16/800" to t.button16,
        "slotLetter 26/800" to t.slotLetter,
        "key 22/800" to t.key,
        "heading20 20/800" to t.heading20,
        "heading18 18/800" to t.heading18,
        "bodyStrong17 17/800" to t.bodyStrong17,
        "bodyStrong16 16/800" to t.bodyStrong16,
        "bodyStrong15 15/800" to t.bodyStrong15,
        "bodyStrong14 14/800" to t.bodyStrong14,
        "body16 16/500 lh1.5" to t.body16,
        "body15 15/500 lh1.5" to t.body15,
        "caption14 14/500" to t.caption14,
        "caption13 13/500" to t.caption13,
        "caption12 12/500" to t.caption12,
        "label13 13/700 .14em" to t.label13,
        "label12 12/700 .14em" to t.label12,
        "wheelChip 13/800" to t.wheelChip,
    )
    Column(
        modifier = Modifier.fillMaxSize().background(LexiTheme.colors.cream).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        styles.forEach { (name, style) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name,
                    style = t.caption12,
                    color = LexiTheme.colors.inkMuted,
                    modifier = Modifier.size(150.dp, 16.dp),
                )
                Text(
                    text = if (style.fontSize.value > 34f) "Ağ" else "ÇARK İğş",
                    style = style,
                    modifier = Modifier.background(LexiTheme.colors.white),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ColorSheet() {
    val c = LexiTheme.colors
    val swatches: List<Triple<String, String, Color>> = listOf(
        Triple("cream", "#F2EBDD", c.cream),
        Triple("white", "#FFFFFF", c.white),
        Triple("ink", "#111111", c.ink),
        Triple("red", "#D9281C", c.red),
        Triple("blue", "#1F4FBF", c.blue),
        Triple("yellow", "#F2C200", c.yellow),
        Triple("inkMuted", "#5A5A5A", c.inkMuted),
        Triple("borderMuted", "#7A7468", c.borderMuted),
        Triple("disabledFill", "#E2DACB", c.disabledFill),
        Triple("gridLine", "#B8B0A0", c.gridLine),
        Triple("scrim", "#111111 @ 55%", c.scrim),
    )
    Column(
        modifier = Modifier.fillMaxSize().background(c.cream).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        swatches.forEach { (name, hex, color) ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(96.dp, 48.dp).background(c.white).border(2.dp, c.ink)) {
                    Box(Modifier.fillMaxSize().padding(2.dp).background(color))
                }
                Column(Modifier.fillMaxWidth()) {
                    Text(name, style = LexiTheme.typography.bodyStrong15)
                    Text(hex, style = LexiTheme.typography.caption13)
                }
            }
        }
    }
}
