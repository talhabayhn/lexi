package dev.talhabayhan.lexi.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res
import dev.talhabayhan.lexi.core.designsystem.generated.resources.jost_bold
import dev.talhabayhan.lexi.core.designsystem.generated.resources.jost_extrabold
import dev.talhabayhan.lexi.core.designsystem.generated.resources.jost_medium
import dev.talhabayhan.lexi.core.designsystem.generated.resources.jost_semibold
import org.jetbrains.compose.resources.Font
import kotlin.math.floor

/**
 * Jost type scale (`docs/design/tokens.md` § Typography). Values are the design HTML's CSS: font px = sp,
 * `letter-spacing: .14em` = `0.14.em`, unitless `line-height` = `em`, no line height = CSS `normal` as Chrome
 * computes it. Roles that the doc gives as a range carry the
 * font size as suffix (`heading18`, `heading20`) so a CSS declaration maps to exactly one token. A size/weight that
 * a screen needs and is missing here gets added here, with a comment naming the screen.
 *
 * Styles carry no color: text takes `LocalContentColor` (ink inside `LexiTheme`). `label*` styles are UPPERCASE
 * by copy: keep the string resources uppercase instead of calling `uppercase()` (Turkish dotted İ).
 */
@Immutable
public class LexiTypography internal constructor(
    /** 96 / 800, line-height 0.9: game centre letter. */
    public val wheelLetter: TextStyle,
    /** 76 / 800, line-height 0.95: result score. */
    public val scoreHero: TextStyle,
    /** 48 / 800: splash logo blocks. */
    public val logo48: TextStyle,
    /** 40 / 800: menu logo blocks. */
    public val logo40: TextStyle,
    /** 34 / 800, line-height 1.05: onboarding headline. */
    public val display: TextStyle,
    /** 30 / 800, line-height 1.05: game clue. */
    public val clue: TextStyle,
    /** 28 / 800: screen titles. */
    public val title: TextStyle,
    /** 30 / 800, +0.1em: OYNA (`02-home`). */
    public val button30: TextStyle,
    /** 24 / 800, +0.1em: BAŞLA (`03-level-select`). */
    public val button24: TextStyle,
    /** 22 / 800, +0.12em: PAS (`04-game`). */
    public val button22: TextStyle,
    /** 20 / 800, +0.06em: wide CTAs (`01-onboarding`). */
    public val button20: TextStyle,
    /** 16 / 800: compact buttons (`16-components`). */
    public val button16: TextStyle,
    /** 26 / 800: answer-slot letters. */
    public val slotLetter: TextStyle,
    /** 22 / 800: letter keys. */
    public val key: TextStyle,
    /** 20 / 800: card and section titles. */
    public val heading20: TextStyle,
    /** 18 / 800: card and section titles. */
    public val heading18: TextStyle,
    /** 17 / 800: list row labels. */
    public val bodyStrong17: TextStyle,
    /** 16 / 800: list row labels, toasts. */
    public val bodyStrong16: TextStyle,
    /** 15 / 800: dialog buttons, row values. */
    public val bodyStrong15: TextStyle,
    /** 14 / 800: chips, segments, level badges. */
    public val bodyStrong14: TextStyle,
    /** 16 / 500, line-height 1.5: paragraphs. */
    public val body16: TextStyle,
    /** 15 / 500, line-height 1.5: paragraphs. */
    public val body15: TextStyle,
    /** 14 / 500: secondary lines. */
    public val caption14: TextStyle,
    /** 13 / 500: secondary lines. */
    public val caption13: TextStyle,
    /** 12 / 500: meta lines. */
    public val caption12: TextStyle,
    /** 13 / 700, +0.14em, uppercase: section headers. */
    public val label13: TextStyle,
    /** 12 / 700, +0.14em, uppercase: "İPUCU · İSİM", "OYUN MODU". */
    public val label12: TextStyle,
    /** 13 / 800: wheel letters. */
    public val wheelChip: TextStyle,
    /** 30 / 800, line-height 1: stat numbers (`09-stats`, `06-result`). */
    public val statValue: TextStyle,
    /** 34 / 800, line-height 1: level-card titles (`03-level-select`). */
    public val levelTitle: TextStyle,
    /** 12 / 700: clue-card letter count "7 HARF" (`04-game`). */
    public val caption12Bold: TextStyle,
    /** 16 / 600: list row labels (`11-settings`). */
    public val body16SemiBold: TextStyle,
    /** 14 / 600: level-card subtitles, empty state (`03-level-select`). */
    public val caption14SemiBold: TextStyle,
    /** 13 / 600: stat labels, secondary row values (`09-stats`, `06-result`). */
    public val caption13SemiBold: TextStyle,
    /** 12 / 600: item captions (`16-components`). */
    public val caption12SemiBold: TextStyle,
    /** 13 / 700, +0.12em, uppercase: wheel rule "İLE BAŞLAR", "YA DA SEVİYENİ SEÇ". */
    public val wheelRule: TextStyle,
    /** 15 / 700, underlined: text links (`LexiTextLink`, "Atla"). */
    public val link15: TextStyle,
)

/** Static Jost instances shipped in `composeResources/font` (OFL, `files/licenses/jost-ofl.txt`). */
@Composable
internal fun rememberJostFontFamily(): FontFamily {
    val medium = Font(Res.font.jost_medium, FontWeight.Medium)
    val semiBold = Font(Res.font.jost_semibold, FontWeight.SemiBold)
    val bold = Font(Res.font.jost_bold, FontWeight.Bold)
    val extraBold = Font(Res.font.jost_extrabold, FontWeight.ExtraBold)
    return remember(medium, semiBold, bold, extraBold) { FontFamily(medium, semiBold, bold, extraBold) }
}

/** Jost's ascender and descender per em (hhea/typo 1070 / −375 at 1000 units per em, no line gap). */
private const val JOST_ASCENT = 1.07
private const val JOST_DESCENT = 0.375

/** Jost's natural line height in em: ascender + descender. */
private const val JOST_NATURAL_LINE_HEIGHT = JOST_ASCENT + JOST_DESCENT

/** Chrome's `line-height: normal`: ascent and descent each rounded to whole CSS px, so 12px text is 18px high. */
internal fun cssNormalLineHeight(fontSize: Float): Float =
    floor(JOST_ASCENT * fontSize + 0.5).toFloat() + floor(JOST_DESCENT * fontSize + 0.5).toFloat()

private fun cssLineHeightStyle(factor: Double): LineHeightStyle = if (factor < JOST_NATURAL_LINE_HEIGHT) {
    LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.Both, LineHeightStyle.Mode.Tight)
} else {
    LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
}

/**
 * Returns this style with a CSS unitless `line-height` ([factor] × font size). Like a CSS line box, the half-leading
 * is split evenly above and below the glyphs: below Jost's natural line height the box shrinks to exactly the
 * requested height and glyphs may overflow it (Compose `Mode.Tight` + `Trim.Both`, otherwise Compose pads the box);
 * above it `Trim.None` keeps the half-leading on the first and last line.
 */
internal fun TextStyle.withCssLineHeight(factor: Double): TextStyle =
    copy(lineHeight = factor.em, lineHeightStyle = cssLineHeightStyle(factor))

/**
 * Returns this style with CSS `line-height: normal` as the browser lays it out ([cssNormalLineHeight], in sp). Every
 * style without an explicit line height uses it, so stacked texts keep the design's vertical rhythm (Compose's own
 * metrics are up to 0.7dp shorter per line, which adds up down a screen).
 */
internal fun TextStyle.withCssNormalLineHeight(): TextStyle {
    val lineHeight = cssNormalLineHeight(fontSize.value)
    return copy(
        lineHeight = lineHeight.sp,
        lineHeightStyle = cssLineHeightStyle(lineHeight.toDouble() / fontSize.value),
    )
}

/** This style at another font size, keeping a unitless line height or recomputing the CSS `normal` one. */
internal fun TextStyle.resized(fontSize: TextUnit): TextStyle {
    val resized = copy(fontSize = fontSize)
    return if (lineHeight.isSp) resized.withCssNormalLineHeight() else resized
}

internal fun lexiTypography(jost: FontFamily): LexiTypography {
    fun style(
        size: Int,
        weight: FontWeight,
        lineHeight: Double? = null,
        letterSpacing: TextUnit = TextUnit.Unspecified,
    ): TextStyle {
        val style = TextStyle(fontFamily = jost, fontWeight = weight, fontSize = size.sp, letterSpacing = letterSpacing)
        return if (lineHeight != null) style.withCssLineHeight(lineHeight) else style.withCssNormalLineHeight()
    }

    val extraBold = FontWeight.ExtraBold
    return LexiTypography(
        wheelLetter = style(96, extraBold, lineHeight = 0.9),
        scoreHero = style(76, extraBold, lineHeight = 0.95),
        logo48 = style(48, extraBold),
        logo40 = style(40, extraBold),
        display = style(34, extraBold, lineHeight = 1.05),
        clue = style(30, extraBold, lineHeight = 1.05),
        title = style(28, extraBold),
        button30 = style(30, extraBold, letterSpacing = 0.1.em),
        button24 = style(24, extraBold, letterSpacing = 0.1.em),
        button22 = style(22, extraBold, letterSpacing = 0.12.em),
        button20 = style(20, extraBold, letterSpacing = 0.06.em),
        button16 = style(16, extraBold),
        slotLetter = style(26, extraBold),
        key = style(22, extraBold),
        heading20 = style(20, extraBold),
        heading18 = style(18, extraBold),
        bodyStrong17 = style(17, extraBold),
        bodyStrong16 = style(16, extraBold),
        bodyStrong15 = style(15, extraBold),
        bodyStrong14 = style(14, extraBold),
        body16 = style(16, FontWeight.Medium, lineHeight = 1.5),
        body15 = style(15, FontWeight.Medium, lineHeight = 1.5),
        caption14 = style(14, FontWeight.Medium),
        caption13 = style(13, FontWeight.Medium),
        caption12 = style(12, FontWeight.Medium),
        label13 = style(13, FontWeight.Bold, letterSpacing = 0.14.em),
        label12 = style(12, FontWeight.Bold, letterSpacing = 0.14.em),
        wheelChip = style(13, extraBold),
        statValue = style(30, extraBold, lineHeight = 1.0),
        levelTitle = style(34, extraBold, lineHeight = 1.0),
        caption12Bold = style(12, FontWeight.Bold),
        body16SemiBold = style(16, FontWeight.SemiBold),
        caption14SemiBold = style(14, FontWeight.SemiBold),
        caption13SemiBold = style(13, FontWeight.SemiBold),
        caption12SemiBold = style(12, FontWeight.SemiBold),
        wheelRule = style(13, FontWeight.Bold, letterSpacing = 0.12.em),
        link15 = style(15, FontWeight.Bold).copy(textDecoration = TextDecoration.Underline),
    )
}
