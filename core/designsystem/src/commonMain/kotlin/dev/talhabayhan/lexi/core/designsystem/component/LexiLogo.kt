package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.resized
import dev.talhabayhan.lexi.core.domain.AppInfo

/** Block sizes of the full logo (`13-logo-and-icon`, `14-splash`, `02-home`). */
public enum class LexiLogoSize(
    internal val block: Dp,
    internal val border: Dp,
    internal val letter: Dp,
    internal val gap: Dp,
) {
    /** 130dp blocks, 5dp border, 86 letters, gap 12 (logo board). */
    Hero(130.dp, 5.dp, 86.dp, 12.dp),

    /** 74dp blocks, 4dp border, 48 letters, gap 8 (splash). */
    Splash(74.dp, 4.dp, 48.dp, 8.dp),

    /** 64dp blocks, 3dp border, 40 letters, gap 6 (home). */
    Menu(64.dp, 3.dp, 40.dp, 6.dp),

    /** 46dp blocks, 3dp border, 28 letters, gap 5 (home on short phones; DESIGN-DEBT, scaled from [Menu]). */
    MenuCompact(46.dp, 3.dp, 28.dp, 5.dp),
}

/** Logo geometry that is not a size preset (`13-logo-and-icon.html`). */
public object LexiLogoDefaults {
    /** Wordmark: three 22dp shapes, gap 4, then "lexi" with gap 14. */
    internal val WordmarkShape: Dp = 22.dp
    internal val WordmarkShapeGap: Dp = 4.dp
    public val WordmarkGap: Dp = 14.dp

    /** Shape-only mark for sizes below 24dp: four 26dp shapes with 2dp borders, gap 3. */
    internal val MarkShape: Dp = 26.dp
    internal val MarkGap: Dp = 3.dp

    /** App icon option 1 on its 180dp design canvas. */
    public val IconSize: Dp = 180.dp
    internal const val ICON_DESIGN: Float = 180f
    internal const val ICON_BORDER: Float = 3f
    internal const val ICON_CIRCLE: Float = 100f
    internal const val ICON_CIRCLE_INSET: Float = 40f
    internal const val ICON_CIRCLE_BORDER: Float = 5f
    internal const val ICON_LETTER: Float = 64f
    internal const val ICON_LEAF: Float = 34f
    internal const val ICON_LEAF_INSET: Float = 18f

    /** Below this size the icon drops the "L" (`13-logo-and-icon`: the 40dp icon has none). */
    public val IconLetterMinSize: Dp = 60.dp

    /** Home-screen sizes, this one and below, drop the leaf (`13-logo-and-icon`: the 60dp and 40dp icons have none). */
    public val IconCompactMaxSize: Dp = 60.dp
}

/**
 * The four-block logo: the brand letters ([AppInfo.NAME]) in the four game states — red square, yellow circle, blue
 * leaf, ink block. One image for accessibility, announced as the app name. [blockLayer] transforms each block in the
 * draw phase (the splash animation of `14-splash`), given the block's index 0–3.
 */
@Composable
public fun LexiLogo(
    modifier: Modifier = Modifier,
    size: LexiLogoSize = LexiLogoSize.Menu,
    blockLayer: (GraphicsLayerScope.(index: Int) -> Unit)? = null,
) {
    val colors = LexiTheme.colors
    val letters = AppInfo.NAME.uppercase()
    val letterStyle = LexiTheme.typography.logo40.resized(with(LocalDensity.current) { size.letter.toSp() })
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = AppInfo.NAME
            role = Role.Image
        },
        horizontalArrangement = Arrangement.spacedBy(size.gap),
    ) {
        letters.forEachIndexed { i, letter ->
            val (kind, fill, content) = logoBlock(i)
            Box(
                modifier = Modifier
                    .then(if (blockLayer != null) Modifier.graphicsLayer { blockLayer(i) } else Modifier)
                    .size(size.block)
                    .lexiSurface(
                        color = fill,
                        border = if (fill == colors.ink) null else BorderStroke(size.border, colors.ink),
                        shape = kind.shape(),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(letter.toString(), CssCenteredLetter, style = letterStyle, color = content)
            }
        }
    }
}

/**
 * The letters' CSS line box is taller than their block's content box (e.g. 92 in 90 for the icon "L"). CSS centers the
 * overflowing box; Compose would clamp it to the block and shift the glyph down, so measure it unbounded.
 */
private val CssCenteredLetter: Modifier = Modifier.wrapContentSize(unbounded = true)

@Composable
private fun logoBlock(index: Int): Triple<DecorShapeKind, Color, Color> {
    val colors = LexiTheme.colors
    return when (index % 4) {
        0 -> Triple(DecorShapeKind.Square, colors.red, colors.white)
        1 -> Triple(DecorShapeKind.Circle, colors.yellow, colors.ink)
        2 -> Triple(DecorShapeKind.Leaf, colors.blue, colors.white)
        else -> Triple(DecorShapeKind.Square, colors.ink, colors.cream)
    }
}

/** Compact horizontal wordmark: red square, yellow circle, blue leaf (22dp, no border) and "lexi" (40/800). */
@Composable
public fun LexiWordmark(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = AppInfo.NAME
            role = Role.Image
        },
        horizontalArrangement = Arrangement.spacedBy(LexiLogoDefaults.WordmarkGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(LexiLogoDefaults.WordmarkShapeGap)) {
            repeat(3) { i ->
                val (kind, fill) = logoBlock(i)
                DecorShape(kind, fill, Modifier.size(LexiLogoDefaults.WordmarkShape))
            }
        }
        Text(AppInfo.NAME.lowercase(), style = LexiTheme.typography.wordmark)
    }
}

/** Shape-only logo for tiny sizes (letters become unreadable below 24dp): four 26dp shapes. Decorative. */
@Composable
public fun LexiLogoMark(modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    Row(modifier.clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(LexiLogoDefaults.MarkGap)) {
        repeat(4) { i ->
            val (kind, fill) = logoBlock(i)
            DecorShape(
                kind = kind,
                color = fill,
                border = if (fill == colors.ink) null else BorderStroke(LexiTheme.borders.thin, colors.ink),
                modifier = Modifier.size(LexiLogoDefaults.MarkShape),
            )
        }
    }
}

/**
 * App icon, option 1 "Kompozisyon": cream square with a 3dp border, red left half, yellow circle with the "L" and a
 * blue leaf; all values scale from the 180dp design. Drawn square: the platform owns the corner mask. The "L" is left
 * out below [LexiLogoDefaults.IconLetterMinSize], the leaf at [LexiLogoDefaults.IconCompactMaxSize] and below.
 * [framed] = false drops the border for the launcher icons, where the platform mask is the outline: the iOS AppIcon is
 * rendered from it (`AppIconRenderTest`) and the Android adaptive icon in `androidApp` uses the same geometry.
 */
@Composable
public fun LexiAppIcon(
    modifier: Modifier = Modifier,
    size: Dp = LexiLogoDefaults.IconSize,
    showLetter: Boolean = size >= LexiLogoDefaults.IconLetterMinSize,
    showLeaf: Boolean = size > LexiLogoDefaults.IconCompactMaxSize,
    framed: Boolean = true,
) {
    val colors = LexiTheme.colors
    val s = size.value / LexiLogoDefaults.ICON_DESIGN
    val border = if (framed) BorderStroke((LexiLogoDefaults.ICON_BORDER * s).dp, colors.ink) else null
    Box(
        modifier
            .size(size)
            .clearAndSetSemantics {
                contentDescription = AppInfo.NAME
                role = Role.Image
            }
            .lexiSurface(colors.cream, border),
    ) {
        // Positions are relative to the padding box (inside the border), as in the HTML.
        Box(Modifier.width((LexiLogoDefaults.ICON_DESIGN / 2 * s).dp).fillMaxHeight().background(colors.red))
        Box(
            modifier = Modifier
                .offset((LexiLogoDefaults.ICON_CIRCLE_INSET * s).dp, (LexiLogoDefaults.ICON_CIRCLE_INSET * s).dp)
                .size((LexiLogoDefaults.ICON_CIRCLE * s).dp)
                .lexiSurface(
                    colors.yellow,
                    BorderStroke((LexiLogoDefaults.ICON_CIRCLE_BORDER * s).dp, colors.ink),
                    shape = LexiTheme.shapes.circle,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (showLetter) {
                val letterSize = with(LocalDensity.current) { (LexiLogoDefaults.ICON_LETTER * s).dp.toSp() }
                Text(
                    text = AppInfo.NAME.take(1).uppercase(),
                    modifier = CssCenteredLetter,
                    style = LexiTheme.typography.logo40.resized(letterSize),
                )
            }
        }
        if (showLeaf) {
            DecorShape(
                kind = DecorShapeKind.Leaf,
                color = colors.blue,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = (LexiLogoDefaults.ICON_LEAF_INSET * s).dp,
                        bottom = (LexiLogoDefaults.ICON_LEAF_INSET * s).dp,
                    )
                    .size((LexiLogoDefaults.ICON_LEAF * s).dp),
            )
        }
    }
}

@Preview(widthDp = 600)
@Composable
private fun LexiLogoPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap20),
        ) {
            LexiLogo(size = LexiLogoSize.Hero)
            LexiLogo()
            LexiWordmark()
            LexiLogoMark()
            Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16)) {
                LexiAppIcon()
                LexiAppIcon(size = LexiLogoDefaults.IconLetterMinSize)
            }
        }
    }
}
