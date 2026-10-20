package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** Share card geometry (`12-share-card.html`); gaps, borders and the s6 shadow are theme tokens. */
public object ShareCardDefaults {
    /** The card: 432 × 540 (4:5), rendered to 1080 × 1350 px at [CAPTURE_SCALE]. */
    public val Size: DpSize = DpSize(432.dp, 540.dp)

    /** Pixels per dp of the shared PNG: 432 × 540dp → 1080 × 1350 px, the same on every device. */
    public const val CAPTURE_SCALE: Float = 2.5f

    /** `padding: 28px` of the card. */
    public val Padding: Dp = 28.dp

    /** Yellow circle: 180 + 2 × 4 border (CSS content-box), `right: -60px; top: -60px`. */
    public val DecorCircleSize: Dp = 188.dp
    public val DecorCircleOverhang: Dp = 60.dp

    /** The letter grid: 7 equal columns, 40dp high cells (border-box, 2dp border). */
    public const val GRID_COLUMNS: Int = 7
    public val CellHeight: Dp = 40.dp
}
