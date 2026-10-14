package dev.talhabayhan.lexi.core.designsystem.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * All icons at the sizes the designs use. Rendered at xhdpi (2x) like `16-components.png`: the first row repeats the
 * canvas' square back button and red joker circle (48 dp, icon 18) for a pixel comparison.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xhdpi")
class LexiIconsScreenshotTest {

    @Test
    fun icons() {
        captureRoboImage("src/androidHostTest/screenshots/LexiIcons.png") {
            LexiTheme(reducedMotion = false) { IconSheet() }
        }
    }
}

@Composable
private fun IconSheet() {
    val colors = LexiTheme.colors
    val icons: List<Pair<String, ImageVector>> = listOf(
        "Back" to LexiIcons.Back,
        "ChevronRight" to LexiIcons.ChevronRight,
        "ArrowRight" to LexiIcons.ArrowRight,
        "Close" to LexiIcons.Close,
        "Pause" to LexiIcons.Pause,
        "Play" to LexiIcons.Play,
        "Settings" to LexiIcons.Settings,
        "Bulb" to LexiIcons.Bulb,
        "Backspace" to LexiIcons.Backspace,
        "Speaker" to LexiIcons.Speaker,
        "Search" to LexiIcons.Search,
    )
    Column(
        modifier = Modifier.fillMaxSize().background(colors.cream).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier.size(48.dp).background(colors.white).border(3.dp, colors.ink),
                contentAlignment = Alignment.Center,
            ) {
                Icon(LexiIcons.Back, contentDescription = null, tint = colors.ink, modifier = Modifier.size(18.dp))
            }
            Box(
                Modifier
                    .size(48.dp)
                    .background(colors.red, LexiTheme.shapes.circle)
                    .border(3.dp, colors.ink, LexiTheme.shapes.circle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(LexiIcons.Bulb, contentDescription = null, tint = colors.white, modifier = Modifier.size(18.dp))
            }
        }
        icons.forEach { (name, icon) ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(name, style = LexiTheme.typography.caption13, modifier = Modifier.width(96.dp))
                listOf(16.dp, 18.dp, 22.dp).forEach { size -> IconCell(icon, size, colors.ink) }
                IconCell(icon, 22.dp, colors.yellow, background = colors.ink)
            }
        }
    }
}

@Composable
private fun IconCell(icon: ImageVector, size: Dp, tint: Color, background: Color = LexiTheme.colors.white) {
    val height = size * (icon.defaultHeight / icon.defaultWidth)
    Box(Modifier.size(40.dp).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size, height))
    }
}
