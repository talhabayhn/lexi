package dev.talhabayhan.lexi.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Lexi's inline stroke icons (`docs/design/tokens.md` § Iconography), converted 1:1 from the design SVGs: 24 × 24
 * viewBox (arrow 24 × 16), square caps, miter joins, stroke width as in the SVG. Size them like the design
 * (`width="18"` = `Modifier.size(18.dp)`; the stroke scales with the viewBox, as in the browser) and tint them at
 * the call site (`Icon(…, tint = …)`): every path is drawn black. No Material icons, no emoji.
 */
public object LexiIcons {
    /** Chevron left: back navigation (`NavSquareButton`). */
    public val Back: ImageVector by lazy { strokeIcon("Back", 3f, "M15 5l-7 7 7 7") }

    /** Chevron right: list rows with a value. */
    public val ChevronRight: ImageVector by lazy { strokeIcon("ChevronRight", 3f, "M9 5l7 7-7 7") }

    /** Arrow right (24 × 16): pass motion (`15-motion`). */
    public val ArrowRight: ImageVector by lazy {
        strokeIcon("ArrowRight", 3f, "M2 8h18M14 2l6 6-6 6", viewportHeight = 16f)
    }

    /** Close (×): sheets and dialogs. */
    public val Close: ImageVector by lazy { strokeIcon("Close", 3f, "M6 6l12 12M18 6 6 18") }

    /** Pause: game top bar. */
    public val Pause: ImageVector by lazy { strokeIcon("Pause", 3f, "M8 5L8 19", "M16 5L16 19") }

    /** Play (filled triangle). */
    public val Play: ImageVector by lazy { filledIcon("Play", "M7 4v16l13-8z") }

    /** Settings (hub with eight spokes). */
    public val Settings: ImageVector by lazy {
        strokeIcon(
            "Settings",
            2.4f,
            "M8.5 12a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0-7 0z",
            "M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1 7 17M17 7l2.1-2.1",
        )
    }

    /** Light bulb: joker. */
    public val Bulb: ImageVector by lazy {
        strokeIcon(
            "Bulb",
            2.4f,
            "M9 18h6",
            "M10 21h4",
            "M12 3a6 6 0 0 0-3.5 10.9c.6.5 1 1.2 1 2.1h5c0-.9.4-1.6 1-2.1A6 6 0 0 0 12 3z",
        )
    }

    /** Backspace: delete the last letter. */
    public val Backspace: ImageVector by lazy {
        strokeIcon("Backspace", 2.4f, "M21 5H9l-6 7 6 7h12z", "M13 9L17 15", "M17 9L13 15")
    }

    /** Speaker: pronounce the word (TTS). */
    public val Speaker: ImageVector by lazy {
        strokeIcon("Speaker", 2.4f, "M4 9h4l5-4v14l-5-4H4z", "M17 9a4 4 0 0 1 0 6")
    }

    /** Magnifier: search. */
    public val Search: ImageVector by lazy {
        strokeIcon("Search", 2.6f, "M4.5 11a6.5 6.5 0 1 0 13 0a6.5 6.5 0 1 0-13 0z", "M16 16l5 5")
    }

    /** Check mark (✓): completed goals and selection. */
    public val Check: ImageVector by lazy { strokeIcon("Check", 3f, "M5 13l4 4L19 7") }

    /** Four tiles: the category picker. */
    public val Categories: ImageVector by lazy {
        strokeIcon("Categories", 2.5f, "M4 4h6v6H4z", "M14 4h6v6h-6z", "M4 14h6v6H4z", "M14 14h6v6h-6z")
    }
}

private const val VIEWPORT = 24f

private fun strokeIcon(
    name: String,
    strokeWidth: Float,
    vararg paths: String,
    viewportHeight: Float = VIEWPORT,
): ImageVector = ImageVector.Builder(
    name = "LexiIcons.$name",
    defaultWidth = VIEWPORT.dp,
    defaultHeight = viewportHeight.dp,
    viewportWidth = VIEWPORT,
    viewportHeight = viewportHeight,
).apply {
    paths.forEach { path ->
        addPath(
            pathData = addPathNodes(path),
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = strokeWidth,
            strokeLineCap = StrokeCap.Square,
            strokeLineJoin = StrokeJoin.Miter,
        )
    }
}.build()

private fun filledIcon(name: String, path: String): ImageVector = ImageVector.Builder(
    name = "LexiIcons.$name",
    defaultWidth = VIEWPORT.dp,
    defaultHeight = VIEWPORT.dp,
    viewportWidth = VIEWPORT,
    viewportHeight = VIEWPORT,
).apply {
    addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black))
}.build()
