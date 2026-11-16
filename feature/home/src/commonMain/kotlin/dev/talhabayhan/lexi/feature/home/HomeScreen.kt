package dev.talhabayhan.lexi.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.BottomBarItem
import dev.talhabayhan.lexi.core.designsystem.component.DailyCardStatus
import dev.talhabayhan.lexi.core.designsystem.component.DailyWheelCard
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.DuelHomeBox
import dev.talhabayhan.lexi.core.designsystem.component.FlowDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiLogo
import dev.talhabayhan.lexi.core.designsystem.component.LexiLogoSize
import dev.talhabayhan.lexi.core.designsystem.component.MenuTile
import dev.talhabayhan.lexi.core.designsystem.component.PlayCard
import dev.talhabayhan.lexi.core.designsystem.component.PlayCardMode
import dev.talhabayhan.lexi.core.designsystem.component.RuledBottomBar
import dev.talhabayhan.lexi.core.designsystem.component.SlidingToast
import dev.talhabayhan.lexi.core.designsystem.component.menuTileMinWidth
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.platform.AppLinks
import dev.talhabayhan.lexi.feature.home.generated.resources.Res
import dev.talhabayhan.lexi.feature.home.generated.resources.home_bar_invite
import dev.talhabayhan.lexi.feature.home.generated.resources.home_bar_invite_description
import dev.talhabayhan.lexi.feature.home.generated.resources.home_bar_stats
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_badge_done
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_badge_solve
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_card_title
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_desc_done
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_desc_solve
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_desc_unavailable
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_done_hours_minutes
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_done_minutes
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_title
import dev.talhabayhan.lexi.feature.home.generated.resources.home_daily_unavailable
import dev.talhabayhan.lexi.feature.home.generated.resources.home_invite_message
import dev.talhabayhan.lexi.feature.home.generated.resources.home_joker_gift_combined
import dev.talhabayhan.lexi.feature.home.generated.resources.home_joker_gift_daily
import dev.talhabayhan.lexi.feature.home.generated.resources.home_joker_gift_streak
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_classic
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_classic_caption
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_hardcore
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_hardcore_caption
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_practice
import dev.talhabayhan.lexi.feature.home.generated.resources.home_mode_practice_caption
import dev.talhabayhan.lexi.feature.home.generated.resources.home_notice_dismiss
import dev.talhabayhan.lexi.feature.home.generated.resources.home_play
import dev.talhabayhan.lexi.feature.home.generated.resources.home_play_description
import dev.talhabayhan.lexi.feature.home.generated.resources.home_play_label
import dev.talhabayhan.lexi.feature.home.generated.resources.home_settings
import dev.talhabayhan.lexi.feature.home.generated.resources.home_study_header
import dev.talhabayhan.lexi.feature.home.generated.resources.home_thousands_separator
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_duel
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_duel_subtitle
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_exams
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_exams_subtitle
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_notebook
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_notebook_due
import dev.talhabayhan.lexi.feature.home.generated.resources.home_total_points
import dev.talhabayhan.lexi.feature.home.generated.resources.home_total_points_description
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Home of the video flow (`docs/design/video-flow-notes.md` §1, `drafts/video-flow/home_v5.png`): header (logo + total
 * points, no streak), the play card, "Günün çarkı", "İki kişi, tek telefon", ÇALIŞ (Defter, Sınavlar) and the ruled
 * bottom bar (İstatistik, Davet et, Ayarlar). The content scrolls above the bar, inside the system-bar insets; the
 * play card is the first thing under the header, so it is never cut off. No entrance animation (the splash animates).
 * The daily gift is a floating toast shown only while [noticeVisible]; it never affects the layout.
 */
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    noticeVisible: Boolean = true,
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val inviteMessage = stringResource(
        Res.string.home_invite_message,
        AppLinks.landing(turkish = Locale.current.language == "tr"),
    )
    Box(modifier.fillMaxSize().background(colors.cream)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .lexiContentWidth()
                    .padding(
                        start = dimens.screenPaddingHorizontal,
                        end = dimens.screenPaddingHorizontal,
                        top = dimens.screenPaddingTop,
                        bottom = dimens.screenPaddingBottomList,
                    ),
                verticalArrangement = Arrangement.spacedBy(dimens.gap16),
            ) {
                Header(state.totalScore)
                PlayCard(
                    label = stringResource(Res.string.home_play_label),
                    modes = listOf(
                        PlayCardMode(
                            stringResource(Res.string.home_mode_classic),
                            stringResource(Res.string.home_mode_classic_caption, state.classicMinutes),
                        ),
                        PlayCardMode(
                            stringResource(Res.string.home_mode_practice),
                            stringResource(Res.string.home_mode_practice_caption),
                        ),
                        PlayCardMode(
                            stringResource(Res.string.home_mode_hardcore),
                            stringResource(Res.string.home_mode_hardcore_caption),
                        ),
                    ),
                    playLabel = stringResource(Res.string.home_play),
                    contentDescription = stringResource(Res.string.home_play_description, state.classicMinutes),
                    onClick = { onIntent(HomeIntent.Play) },
                )
                DailyCard(state.dailyState, onOpen = { onIntent(HomeIntent.OpenDaily) })
                DuelHomeBox(
                    title = stringResource(Res.string.home_tile_duel),
                    subtitle = stringResource(Res.string.home_tile_duel_subtitle),
                    onClick = { onIntent(HomeIntent.OpenDuel) },
                )
                Spacer(Modifier.height(dimens.gap8))
                Text(
                    stringResource(Res.string.home_study_header),
                    style = LexiTheme.typography.label12,
                    color = colors.ink,
                    modifier = Modifier.lexiHeading(),
                )
                StudyTiles(state, onIntent)
            }
            RuledBottomBar(
                items = listOf(
                    BottomBarItem(LexiIcons.Stats, stringResource(Res.string.home_bar_stats), {
                        onIntent(HomeIntent.OpenStats)
                    }),
                    BottomBarItem(
                        icon = LexiIcons.Invite,
                        label = stringResource(Res.string.home_bar_invite),
                        onClick = { onIntent(HomeIntent.Invite(inviteMessage)) },
                        contentDescription = stringResource(Res.string.home_bar_invite_description),
                    ),
                    BottomBarItem(LexiIcons.Settings, stringResource(Res.string.home_settings), {
                        onIntent(HomeIntent.OpenSettings)
                    }),
                ),
                modifier = Modifier
                    .background(colors.cream)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
                    ),
            )
        }
        val notice = state.dailyGiftNotice
        if (notice != null && noticeVisible) {
            GiftToast(
                notice = notice,
                onDismiss = { onIntent(HomeIntent.DismissNotice) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .lexiContentWidth()
                    .padding(
                        start = dimens.screenPaddingHorizontal,
                        end = dimens.screenPaddingHorizontal,
                        top = dimens.screenPaddingTop + FlowDefaults.HomeHeaderHeight + dimens.gap8,
                    ),
            )
        }
    }
}

private val HomeUiState.classicMinutes: Int get() = classicTimeLimitSeconds / SECONDS_PER_MINUTE

private const val SECONDS_PER_MINUTE = 60

/** Logo left, total points right ("12.480" over "toplam puan"); the points are one node read with their label. */
@Composable
private fun Header(totalScore: Long) {
    val colors = LexiTheme.colors
    val separator = stringResource(Res.string.home_thousands_separator)
    val total = remember(totalScore, separator) { groupThousands(totalScore, separator) }
    val description = stringResource(Res.string.home_total_points_description, total)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LexiLogo(size = LexiLogoSize.Header)
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier.padding(start = LexiTheme.dimens.gap12).clearAndSetSemantics {
                contentDescription = description
            },
            horizontalAlignment = Alignment.End,
        ) {
            Text(total, style = LexiTheme.typography.heading24, color = colors.ink, textAlign = TextAlign.End)
            Text(
                stringResource(Res.string.home_total_points),
                style = LexiTheme.typography.caption12SemiBold,
                color = colors.inkMuted,
                textAlign = TextAlign.End,
            )
        }
    }
}

/** "12480" → "12.480" (TR) / "12,480" (EN): digits in groups of three from the right. */
internal fun groupThousands(value: Long, separator: String): String {
    val digits = value.coerceAtLeast(0L).toString()
    return digits.reversed().chunked(DIGIT_GROUP).joinToString(separator).reversed()
}

private const val DIGIT_GROUP = 3

@Composable
private fun DailyCard(daily: DailyCardState, onOpen: () -> Unit) {
    val title = stringResource(Res.string.home_daily_card_title)
    when (daily) {
        DailyCardState.Available -> DailyWheelCard(
            title = title,
            description = stringResource(Res.string.home_daily_title),
            status = DailyCardStatus.Solve,
            badgeLabel = stringResource(Res.string.home_daily_badge_solve),
            contentDescription = stringResource(Res.string.home_daily_desc_solve),
            onClick = onOpen,
        )

        is DailyCardState.AlreadyPlayed -> DailyWheelCard(
            title = title,
            description = if (daily.hoursUntilNext > 0) {
                stringResource(
                    Res.string.home_daily_done_hours_minutes,
                    daily.score,
                    daily.hoursUntilNext,
                    daily.minutesUntilNext,
                )
            } else {
                stringResource(Res.string.home_daily_done_minutes, daily.score, daily.minutesUntilNext)
            },
            status = DailyCardStatus.Completed,
            badgeLabel = stringResource(Res.string.home_daily_badge_done),
            contentDescription = stringResource(Res.string.home_daily_desc_done, daily.score),
            onClick = {},
        )

        DailyCardState.Unavailable -> DailyWheelCard(
            title = title,
            description = stringResource(Res.string.home_daily_unavailable),
            status = DailyCardStatus.Unavailable,
            badgeLabel = null,
            contentDescription = stringResource(Res.string.home_daily_desc_unavailable),
            onClick = {},
        )
    }
}

/**
 * Defter (white, blue circle, due count) and Sınavlar (ink, yellow diamond, the playable packs); side by side, or
 * stacked when a title would have to break (narrow phones with large fonts).
 */
@Composable
private fun StudyTiles(state: HomeUiState, onIntent: (HomeIntent) -> Unit) {
    val colors = LexiTheme.colors
    val notebook = stringResource(Res.string.home_tile_notebook)
    val exams = stringResource(Res.string.home_tile_exams)
    val examSubtitle = state.examPackNames.takeIf { it.isNotEmpty() }?.joinToString(" · ")
        ?: stringResource(Res.string.home_tile_exams_subtitle)
    val tiles: List<@Composable (Modifier) -> Unit> = listOf(
        { modifier ->
            MenuTile(
                title = notebook,
                subtitle = pluralStringResource(Res.plurals.home_tile_notebook_due, state.dueReviews, state.dueReviews),
                marker = DecorShapeKind.Circle,
                markerColor = colors.blue,
                onClick = { onIntent(HomeIntent.OpenNotebook) },
                modifier = modifier,
            )
        },
        { modifier ->
            MenuTile(
                title = exams,
                subtitle = examSubtitle,
                marker = DecorShapeKind.Diamond,
                markerColor = colors.yellow,
                onClick = { onIntent(HomeIntent.OpenExamPacks) },
                modifier = modifier,
                inverted = true,
            )
        },
    )
    val minWidth = menuTileMinWidth(listOf(notebook, exams))
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = LexiTheme.dimens.gap12
        if ((maxWidth - gap) / 2 >= minWidth) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(gap)) {
                tiles.forEach { tile -> tile(Modifier.weight(1f).fillMaxHeight()) }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                tiles.forEach { tile -> tile(Modifier.fillMaxWidth()) }
            }
        }
    }
}

/** The daily joker gift as an info toast below the header; tap to dismiss. */
@Composable
private fun GiftToast(notice: DailyGiftNotice, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val message = when (notice.type) {
        DailyGiftNoticeType.Daily -> stringResource(Res.string.home_joker_gift_daily, notice.amount)
        DailyGiftNoticeType.Streak -> stringResource(Res.string.home_joker_gift_streak, notice.amount)
        DailyGiftNoticeType.Combined -> stringResource(Res.string.home_joker_gift_combined, notice.amount)
    }
    SlidingToast(
        message = message,
        onDismissed = onDismiss,
        visibleMillis = NOTICE_VISIBLE_MILLIS,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(Res.string.home_notice_dismiss),
                role = Role.Button,
                onClick = onDismiss,
            ),
    )
}

/**
 * How long the daily-gift notice stays. Longer than the design's 2.5 s toast: a screen reader needs time to speak it
 * (the notice is a live region) and the user can still dismiss it with a tap.
 */
private const val NOTICE_VISIBLE_MILLIS = 6_000L

/** Design sample (`drafts/video-flow/home_v5.png`): previews, screenshot and layout tests, store captures. */
internal val HomeSampleState: HomeUiState = HomeUiState(
    totalScore = 12_480L,
    dueReviews = 24,
    examPackNames = listOf("LGS İngilizce"),
    selectedLevel = Level.B2,
)

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    LexiTheme(reducedMotion = false) { HomeScreen(HomeSampleState, onIntent = {}) }
}
