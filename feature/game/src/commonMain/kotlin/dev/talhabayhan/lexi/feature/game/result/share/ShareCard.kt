package dev.talhabayhan.lexi.feature.game.result.share

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.LexiWordmark
import dev.talhabayhan.lexi.core.designsystem.component.LexiWordmarkSize
import dev.talhabayhan.lexi.core.designsystem.component.ShareCardDefaults
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetter
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.component.rememberWheelLetterLabels
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.platform.StoreLinks
import dev.talhabayhan.lexi.core.domain.share.ShareCaption
import dev.talhabayhan.lexi.core.domain.share.ShareCard
import dev.talhabayhan.lexi.core.domain.share.ShareCell
import dev.talhabayhan.lexi.core.domain.share.SlotOutcome
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.share_caption_daily
import dev.talhabayhan.lexi.feature.game.generated.resources.share_caption_score
import dev.talhabayhan.lexi.feature.game.generated.resources.share_caption_streak
import dev.talhabayhan.lexi.feature.game.generated.resources.share_caption_time
import dev.talhabayhan.lexi.feature.game.generated.resources.share_card_content_description
import dev.talhabayhan.lexi.feature.game.generated.resources.share_card_header
import dev.talhabayhan.lexi.feature.game.generated.resources.share_card_score
import dev.talhabayhan.lexi.feature.game.generated.resources.share_card_streak
import dev.talhabayhan.lexi.feature.game.generated.resources.share_card_time_left
import dev.talhabayhan.lexi.feature.game.generated.resources.share_card_time_up
import dev.talhabayhan.lexi.feature.game.generated.resources.share_message
import dev.talhabayhan.lexi.feature.game.generated.resources.share_message_link
import dev.talhabayhan.lexi.feature.game.generated.resources.share_mode_daily
import dev.talhabayhan.lexi.feature.game.generated.resources.share_months
import dev.talhabayhan.lexi.feature.game.result.label
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

/** Typographic separator of the card's lines, the same in every language (like the result screen's). */
private const val SEPARATOR = " · "

/**
 * `12-share-card`, 432 × 540: header (mode · date, the score, level · time · streak), the 7-column letter grid (blue
 * circle = correct, red square = wrong, yellow leaf = passed, cream circle = never answered) and the footer (wordmark
 * and [link]). The yellow circle is clipped by the card. A Daily has no level segment; time and streak segments are left
 * out when the card has none. A blank [link] leaves the footer with the wordmark only (same height, no gap).
 */
@Composable
internal fun ShareCard(card: ShareCard, modifier: Modifier = Modifier, link: String = StoreLinks.SHARE_LINK_LABEL) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val description = stringResource(Res.string.share_card_content_description, card.correct, card.total)
    Box(
        modifier
            .size(ShareCardDefaults.Size)
            .clearAndSetSemantics { contentDescription = description }
            .clipToBounds()
            .background(colors.cream),
    ) {
        DecorShape(
            kind = DecorShapeKind.Circle,
            color = colors.yellow,
            border = BorderStroke(LexiTheme.borders.thick, colors.ink),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = ShareCardDefaults.DecorCircleOverhang, y = -ShareCardDefaults.DecorCircleOverhang)
                .size(ShareCardDefaults.DecorCircleSize),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(ShareCardDefaults.Padding),
            verticalArrangement = Arrangement.spacedBy(dimens.gap18),
        ) {
            Header(card)
            LetterGrid(card.cells)
            // `margin-top: auto`: the footer sits at the bottom, the gap above it stays 18.
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomStart) {
                Footer(link)
            }
        }
    }
}

@Composable
private fun Header(card: ShareCard) {
    val typography = LexiTheme.typography
    val months = stringArrayResource(Res.array.share_months)
    val mode = stringResource(if (card.mode == GameMode.DAILY) Res.string.share_mode_daily else card.mode.label())
    val header = stringResource(
        Res.string.share_card_header,
        mode,
        card.date.day,
        months.getOrNull(card.date.month - 1).orEmpty(),
        card.date.year,
    )
    val details = buildList {
        card.level?.let { add(it.name) }
        card.remainingSeconds?.let { seconds ->
            add(
                if (card.timeUp) {
                    stringResource(Res.string.share_card_time_up)
                } else {
                    stringResource(Res.string.share_card_time_left, seconds)
                },
            )
        }
        if (card.showsStreak) {
            add(pluralStringResource(Res.plurals.share_card_streak, card.streakDays, card.streakDays))
        }
    }.joinToString(SEPARATOR)
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2)) {
        Text(header, style = typography.label13)
        Text(stringResource(Res.string.share_card_score, card.correct, card.total), style = typography.shareScore)
        if (details.isNotEmpty()) Text(details, style = typography.body15SemiBold)
    }
}

@Composable
private fun LetterGrid(cells: List<ShareCell>) {
    val colors = LexiTheme.colors
    val gap = LexiTheme.dimens.gap8
    val labels = rememberWheelLetterLabels()
    val columns = ShareCardDefaults.GRID_COLUMNS
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lexiSurface(
                color = colors.white,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow = LexiTheme.shadows.s6,
            )
            .padding(LexiTheme.dimens.gap16),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        cells.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                for (column in 0 until columns) {
                    val cell = row.getOrNull(column)
                    if (cell == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        // The column width (≈ 41.4) wins over the chip size, so circles are CSS-like ellipses.
                        WheelLetter(
                            letter = cell.letter,
                            state = cell.outcome.chipState(),
                            size = ShareCardDefaults.CellHeight,
                            textStyle = LexiTheme.typography.bodyStrong14,
                            scaleCurrent = false,
                            labels = labels,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Footer(link: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LexiWordmark(size = LexiWordmarkSize.ShareCard)
        if (link.isNotBlank()) Text(link, style = LexiTheme.typography.caption14SemiBold)
    }
}

private fun SlotOutcome.chipState(): WheelLetterState = when (this) {
    SlotOutcome.CORRECT -> WheelLetterState.Correct
    SlotOutcome.WRONG -> WheelLetterState.Wrong
    SlotOutcome.PASSED -> WheelLetterState.Passed
    SlotOutcome.TIME_UP -> WheelLetterState.Todo
}

/**
 * The text shared with the card: the caption, then "Lexi'de oynuyorum." or, with a [link], "Lexi'de oynuyorum: link".
 */
@Composable
internal fun shareText(card: ShareCard, link: String = StoreLinks.SHARE_LINK_LABEL): String {
    val caption = when (val c = card.caption) {
        is ShareCaption.Daily -> stringResource(Res.string.share_caption_daily, c.correct, card.total)
        is ShareCaption.Time -> stringResource(Res.string.share_caption_time, c.seconds)
        is ShareCaption.Streak -> stringResource(Res.string.share_caption_streak, c.days)
        is ShareCaption.Score -> stringResource(Res.string.share_caption_score, c.correct, c.total)
    }
    val message = if (link.isBlank()) {
        stringResource(Res.string.share_message)
    } else {
        stringResource(Res.string.share_message_link, link)
    }
    return caption + "\n" + message
}

@Preview(widthDp = 432, heightDp = 540)
@Composable
private fun ShareCardPreview() {
    LexiTheme(reducedMotion = false) { ShareCard(ShareCardSamples.design, link = ShareCardSamples.DESIGN_LINK) }
}
