package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect

/** How a card ended in the game; the chip shape tells it apart without colour (tokens.md). */
public enum class ReviewOutcome {
    /** Red square. */
    Wrong,

    /** Yellow leaf. */
    Passed,
}

/** Result screen geometry (`06-result.html`). */
public object ResultDefaults {
    /** Yellow background circle: 170 + 2 × 3 border (CSS content-box), `right: -50px; top: -50px`. */
    public val DecorCircleSize: Dp = 176.dp
    public val DecorCircleOverhang: Dp = 50.dp

    /** Stat tiles: 22dp markers + 2 × 2 border (content-box), gap 6, 3dp dividers. */
    public val StatMarkerSize: Dp = 26.dp
    public val StatGap: Dp = 6.dp

    /** CTA grid: 3 equal columns, TEKRAR OYNA spans 2. */
    public const val CTA_COLUMNS: Int = 3
    public const val CTA_PRIMARY_SPAN: Int = 2

    /** "Ana menüye dön": `padding: 6px 12px` (the touch target is extended by Compose's minimum touch size). */
    public val HomeLinkPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp)

    /** Rows shown before "+N kelime daha · Tümünü gör →" (the design shows 4 of 7). */
    public const val COLLAPSED_REVIEW_ROWS: Int = 4

    /** Review row: `padding: 10px 12px`, gap 12, 34dp chip (border-box, 2dp border). */
    public val RowPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    public val RowGap: Dp = 12.dp
    public val ChipSize: Dp = 34.dp
}

/**
 * A row of the "Tekrar edilecekler" list (inside a [ListCard]): letter chip (red square = wrong, yellow leaf =
 * passed), the word (19/800), "clue · *example*" (13/500) and the 44dp speaker button.
 */
@Composable
public fun ReviewRow(
    letter: Char,
    word: String,
    clue: String,
    example: String,
    outcome: ReviewOutcome,
    outcomeDescription: String,
    speakContentDescription: String,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier,
    level: String? = null,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Row(
        modifier = modifier.fillMaxWidth().padding(ResultDefaults.RowPadding),
        horizontalArrangement = Arrangement.spacedBy(ResultDefaults.RowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ResultDefaults.ChipSize)
                .clearAndSetSemantics { contentDescription = outcomeDescription },
            contentAlignment = Alignment.Center,
        ) {
            val wrong = outcome == ReviewOutcome.Wrong
            DecorShape(
                kind = if (wrong) DecorShapeKind.Square else DecorShapeKind.Leaf,
                color = if (wrong) colors.red else colors.yellow,
                modifier = Modifier.fillMaxSize(),
                border = BorderStroke(LexiTheme.borders.thin, colors.ink),
            )
            Text(letter.toString(), style = typography.bodyStrong15, color = if (wrong) colors.white else colors.ink)
        }
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6),
            ) {
                Text(word, style = typography.reviewWord)
                if (level != null) {
                    SmallLevelChip(level)
                }
            }
            Text(
                text = buildAnnotatedString {
                    append(clue)
                    append(" · ")
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(example) }
                },
                style = typography.caption13,
            )
        }
        IconSquareButton(
            icon = LexiIcons.Speaker,
            contentDescription = speakContentDescription,
            onClick = onSpeak,
            size = IconButtonDefaults.NavSize,
            iconSize = IconButtonDefaults.IconSize,
            containerColor = colors.cream,
            borderWidth = LexiTheme.borders.thin,
        )
    }
}

/** Last row of the review list: "+3 kelime daha · Tümünü gör →" (14/700, padding 10/12), a button. */
@Composable
public fun ReviewMoreRow(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    Text(
        text = text,
        style = LexiTheme.typography.bodyBold14,
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, interactionSource = null, indication = null) {
                haptics.perform(HapticKind.Light)
                sounds.play(SoundEffect.Tap)
                onClick()
            }
            .padding(ResultDefaults.RowPadding),
    )
}

/** Small level chip next to a reviewed word in Daily mode (A1..NAT). */
@Composable
public fun SmallLevelChip(level: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val (fill, content) = when (level.uppercase()) {
        "A1" -> colors.red to colors.white
        "A2", "C1" -> colors.white to colors.ink
        "B1" -> colors.yellow to colors.ink
        "B2" -> colors.blue to colors.white
        "NAT", "NATIVE" -> colors.ink to colors.yellow
        else -> colors.white to colors.ink
    }
    Text(
        text = level,
        style = LexiTheme.typography.label12,
        color = content,
        modifier = modifier
            .lexiSurface(fill, BorderStroke(LexiTheme.borders.thin, colors.ink))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun ReviewRowPreview() {
    LexiTheme(reducedMotion = false) {
        Column(Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20)) {
            ListCard(Modifier.fillMaxWidth(), shadow = LexiTheme.shadows.s6) {
                ReviewRow('C', "chair", "sandalye", "Sit on the chair.", ReviewOutcome.Passed, "pas", "chair", {})
                ReviewRow('E', "egg", "yumurta", "I eat an egg.", ReviewOutcome.Wrong, "yanlış", "egg", {})
                ReviewMoreRow("+3 kelime daha · Tümünü gör →", onClick = {})
            }
        }
    }
}
