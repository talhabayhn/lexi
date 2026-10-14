package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.modifier.hardShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiShadow
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Press states and hard shadows. Rendered at xhdpi (2x) like `16-components.png`, so the button crops can be compared
 * pixel for pixel with the design's "Birincil / İkincil / PAS" (idle) and "Basılı" (pressed) buttons.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xhdpi")
class PressableSurfaceScreenshotTest {

    @Test
    fun pressStatesAndHardShadows() {
        captureRoboImage("src/androidHostTest/screenshots/PressableSurface.png") {
            LexiTheme(reducedMotion = false) { PressSheet() }
        }
    }
}

@Composable
private fun PressSheet() {
    val colors = LexiTheme.colors
    val shadows = LexiTheme.shadows
    val button16 = LexiTheme.typography.button16
    Column(
        modifier = Modifier.fillMaxSize().background(colors.cream).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("IDLE", style = LexiTheme.typography.label12)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SampleButton("Birincil", colors.red, colors.white, button16.copy(letterSpacing = 0.06.em), progress = 0f)
            SampleButton("İkincil", colors.white, colors.ink, button16, progress = 0f)
            SampleButton("PAS", colors.yellow, colors.ink, button16.copy(letterSpacing = 0.1.em), progress = 0f)
        }
        Text("PRESSED · REDUCED MOTION · HALF", style = LexiTheme.typography.label12)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SampleButton("Basılı", colors.red, colors.white, button16, progress = 1f)
            SampleButton("Basılı", colors.red, colors.white, button16, progress = 1f, translate = false)
            SampleButton("Yarım", colors.red, colors.white, button16, progress = 0.5f)
        }
        Text("PRESSABLESURFACE (IDLE)", style = LexiTheme.typography.label12)
        PressableSurface(onClick = {}, modifier = Modifier.height(52.dp)) {
            Text("İkincil", style = button16, modifier = Modifier.padding(horizontal = 20.dp))
        }
        Text("HARD SHADOW s3 s4 s5 s6 s8", style = LexiTheme.typography.label12)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            listOf(shadows.s3, shadows.s4, shadows.s5, shadows.s6, shadows.s8).forEach { ShadowTile(it) }
        }
        Text("CIRCLE · LEAF · HALFPILL · s5Red · s4Red", style = LexiTheme.typography.label12)
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            ShadowTile(shadows.s5, LexiTheme.shapes.circle, colors.blue)
            ShadowTile(shadows.s5, LexiTheme.shapes.leaf, colors.yellow)
            ShadowTile(shadows.s4, LexiTheme.shapes.halfPill, colors.red)
            ShadowTile(shadows.s5Red)
            ShadowTile(shadows.s4Red, color = colors.ink)
        }
    }
}

@Composable
private fun SampleButton(
    text: String,
    fill: Color,
    textColor: Color,
    style: TextStyle,
    progress: Float,
    translate: Boolean = true,
) {
    PressableSurfaceLayout(
        pressProgress = { progress },
        translateOnPress = translate,
        shape = LexiTheme.shapes.square,
        color = fill,
        border = BorderStroke(LexiTheme.borders.regular, LexiTheme.colors.ink),
        shadow = LexiTheme.shadows.s5,
        contentAlignment = Alignment.Center,
        modifier = Modifier.height(52.dp),
    ) {
        Text(text, style = style, color = textColor, modifier = Modifier.padding(horizontal = 20.dp))
    }
}

@Composable
private fun ShadowTile(
    shadow: LexiShadow,
    shape: Shape = LexiTheme.shapes.square,
    color: Color = LexiTheme.colors.white,
) {
    Box(
        Modifier
            .size(44.dp)
            .hardShadow(shadow, shape)
            .background(color, shape)
            .border(LexiTheme.borders.regular, LexiTheme.colors.ink, shape),
    )
}
