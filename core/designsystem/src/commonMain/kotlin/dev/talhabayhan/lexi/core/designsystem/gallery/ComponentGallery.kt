package dev.talhabayhan.lexi.core.designsystem.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiColors
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight

/**
 * Reproduction of the `16-components` canvas (1330 × 960) for side-by-side review with
 * `docs/design/screens/16-components.png`. Sample texts are the canvas copy (not app strings).
 * Header (palette, type samples) here, the three component columns in `GallerySections.kt`.
 */
@Composable
internal fun ComponentGallery(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(GallerySpec.canvasWidth)
            .height(GallerySpec.canvasHeight)
            .background(LexiTheme.colors.cream)
            .padding(horizontal = GallerySpec.canvasPaddingHorizontal, vertical = GallerySpec.canvasPaddingVertical),
        verticalArrangement = Arrangement.spacedBy(GallerySpec.canvasGap),
    ) {
        GalleryHeader()
        Row(horizontalArrangement = Arrangement.spacedBy(GallerySpec.columnGap)) {
            GalleryColumn { GalleryColumnOne() }
            GalleryColumn { GalleryColumnTwo() }
            GalleryColumn { GalleryColumnThree() }
        }
    }
}

@Composable
private fun RowScope.GalleryColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16),
        content = content,
    )
}

@Composable
private fun GalleryHeader() {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = LexiTheme.dimens.gap18),
            horizontalArrangement = Arrangement.spacedBy(GallerySpec.headerGap),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Bileşenler", // design sample, not user-facing
                style = typography.title.copy(fontSize = GallerySpec.titleSize).withCssLineHeight(1.0),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap10)) {
                gallerySwatches(colors).forEach { swatch -> Swatch(swatch) }
            }
            Spacer(Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2),
            ) {
                // design sample, not user-facing
                Text(
                    "Jost 800 · 28",
                    style = typography.title.withCssLineHeight(1.0),
                    textAlign = TextAlign.End,
                )
                Text("Jost 700 · 18", style = typography.heading18.copy(fontWeight = FontWeight.Bold))
                Text(
                    "Jost 500 · 15 — gövde metni",
                    style = typography.body15.copy(lineHeight = TextUnit.Unspecified, lineHeightStyle = null),
                )
                Text("ETİKET · 12 · +0.14em", style = typography.label12)
            }
        }
        Box(Modifier.fillMaxWidth().height(LexiTheme.borders.regular).background(colors.ink))
    }
}

private data class GallerySwatch(val name: String, val hex: String, val color: Color, val outlined: Boolean)

// design sample, not user-facing
private fun gallerySwatches(colors: LexiColors): List<GallerySwatch> = listOf(
    GallerySwatch("Krem", "#F2EBDD", colors.cream, outlined = true),
    GallerySwatch("Beyaz", "#FFFFFF", colors.white, outlined = true),
    GallerySwatch("Siyah", "#111111", colors.ink, outlined = false),
    GallerySwatch("Kırmızı", "#D9281C", colors.red, outlined = false),
    GallerySwatch("Mavi", "#1F4FBF", colors.blue, outlined = false),
    GallerySwatch("Sarı", "#F2C200", colors.yellow, outlined = false),
)

@Composable
private fun Swatch(swatch: GallerySwatch) {
    val typography = LexiTheme.typography
    Column(
        modifier = Modifier.width(GallerySpec.swatchWidth),
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap4),
    ) {
        val chip = Modifier.fillMaxWidth().height(GallerySpec.swatchHeight).background(swatch.color)
        Box(if (swatch.outlined) chip.border(LexiTheme.borders.thin, LexiTheme.colors.ink) else chip)
        Text(swatch.name, style = typography.caption12.copy(fontWeight = FontWeight.ExtraBold))
        Text(swatch.hex, style = typography.caption12.copy(fontSize = GallerySpec.hexSize))
    }
}

/** Canvas-only values of `16-components.html` (gallery chrome and demo sizes, not app UI tokens). */
internal object GallerySpec {
    val canvasWidth = 1330.dp
    val canvasHeight = 960.dp
    val columnGap = 32.dp
    val canvasPaddingHorizontal = 40.dp
    val canvasPaddingVertical = 36.dp
    val canvasGap = 22.dp
    val headerGap = 28.dp
    val swatchWidth = 92.dp
    val swatchHeight = 40.dp
    val titleSize = 36.sp
    val hexSize = 11.sp
    val demoButtonSize = 48.dp
    val demoChipSize = 44.dp
    const val NOTE_LINE_HEIGHT = 1.4
}

@Preview(widthDp = 1330, heightDp = 960)
@Composable
private fun ComponentGalleryPreview() {
    LexiTheme(reducedMotion = false) { ComponentGallery() }
}
