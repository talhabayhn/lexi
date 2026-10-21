package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import kotlin.math.roundToInt

/** Chart geometry from `09-stats.html` / `source/09-stats.dc.html` (`max = 50, H = 120`). */
public object BarChartDefaults {
    public val Height: Dp = 130.dp
    public val BarGap: Dp = 10.dp
    public val MaxBarHeight: Dp = 120.dp
    public const val MAX_VALUE: Int = 50

    /** Dashed guide lines at these offsets from the top, 1dp `gridLine`, dash 3 / gap 2 (measured on the PNG). */
    internal val GuideLines: List<Dp> = listOf(30.dp, 80.dp)
    internal val GuideDash: Dp = 3.dp
    internal val GuideGap: Dp = 2.dp

    /** Gap between the value label and the highlighted bar; between chart and day labels. */
    internal val ValueGap: Dp = 4.dp
    public val LabelGap: Dp = 10.dp
}

/**
 * Weekly bar chart: 7 blue bars (height = value / [maxValue] × 120dp) on a 2dp ink baseline with two dashed guide
 * lines; the [highlightIndex] bar (today) gets a 3dp inset ink outline, its value above it and a bold day label.
 * The whole chart is one image for accessibility ([contentDescription], e.g. "Pzt 18, Sal 26, …").
 */
@Composable
public fun BarChart(
    values: List<Int>,
    labels: List<String>,
    highlightIndex: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    maxValue: Int = BarChartDefaults.MAX_VALUE,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Column(
        modifier = modifier.clearAndSetSemantics { this.contentDescription = contentDescription },
        verticalArrangement = Arrangement.spacedBy(BarChartDefaults.LabelGap),
    ) {
        val baseline = LexiTheme.borders.thin
        val hairlineWidth = LexiTheme.borders.hairline
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarChartDefaults.Height + baseline)
                .drawBehind {
                    val dash = PathEffect.dashPathEffect(
                        floatArrayOf(BarChartDefaults.GuideDash.toPx(), BarChartDefaults.GuideGap.toPx()),
                    )
                    val hairline = hairlineWidth.toPx()
                    BarChartDefaults.GuideLines.forEach { top ->
                        val y = top.toPx() + hairline / 2
                        drawLine(colors.gridLine, Offset(0f, y), Offset(size.width, y), hairline, pathEffect = dash)
                    }
                    drawRect(colors.ink, Offset(0f, size.height - baseline.toPx()), Size(size.width, baseline.toPx()))
                }
                .padding(bottom = baseline),
            horizontalArrangement = Arrangement.spacedBy(BarChartDefaults.BarGap),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { i, value ->
                val highlighted = i == highlightIndex
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    if (highlighted) {
                        Text(
                            value.toString(),
                            style = typography.wheelChip,
                            modifier = Modifier.padding(bottom = BarChartDefaults.ValueGap),
                        )
                    }
                    // Math.round(v / max * H) in the source.
                    val height = (
                        BarChartDefaults.MaxBarHeight.value * value.coerceIn(
                            0,
                            maxValue,
                        ) / maxValue
                        ).roundToInt().dp
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(height)
                            .background(colors.blue)
                            .then(
                                if (highlighted) Modifier.border(LexiTheme.borders.regular, colors.ink) else Modifier,
                            ),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(BarChartDefaults.BarGap)) {
            labels.forEachIndexed { i, label ->
                Text(
                    label,
                    style = if (i == highlightIndex) typography.caption12ExtraBold else typography.caption12,
                    textAlign = TextAlign.Center,
                    // Never break a weekday letter by letter at large font sizes (it may overhang its column a little).
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun BarChartPreview() {
    LexiTheme(reducedMotion = false) {
        Box(Modifier.background(LexiTheme.colors.white).padding(LexiTheme.dimens.gap14)) {
            BarChart(
                values = listOf(18, 26, 12, 31, 22, 40, 28),
                labels = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"),
                highlightIndex = 6,
                contentDescription = "Pzt 18, Sal 26, Çar 12, Per 31, Cum 22, Cmt 40, Paz 28",
            )
        }
    }
}
