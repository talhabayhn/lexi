package dev.talhabayhan.lexi.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.BottomBarItem
import dev.talhabayhan.lexi.core.designsystem.component.DailyCardStatus
import dev.talhabayhan.lexi.core.designsystem.component.DailyWheelCard
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.DuelHomeBox
import dev.talhabayhan.lexi.core.designsystem.component.FitLine
import dev.talhabayhan.lexi.core.designsystem.component.FlowDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiLogo
import dev.talhabayhan.lexi.core.designsystem.component.LexiLogoSize
import dev.talhabayhan.lexi.core.designsystem.component.PlayCard
import dev.talhabayhan.lexi.core.designsystem.component.RuledBottomBar
import dev.talhabayhan.lexi.core.designsystem.component.SlidingToast
import dev.talhabayhan.lexi.core.designsystem.component.StudyTile
import dev.talhabayhan.lexi.core.designsystem.component.WithCappedFontScale
import dev.talhabayhan.lexi.core.designsystem.component.studyTileMinWidth
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Test handle of the ruled bottom bar (its real top edge, not the first label). */
internal const val HOME_BOTTOM_BAR_TAG = "home-bottom-bar"

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
            // The viewport height lets the content column be at least as tall as the space above the bar, so the
            // flexible spacer before ÇALIŞ pushes it to the bottom (a fixed 24dp above the bar); when the content is
            // taller (short screen, large font) the spacer is zero and everything scrolls as one list.
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val viewportHeight = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = viewportHeight)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                        )
                        .lexiContentWidth()
                        .padding(
                            start = dimens.screenPaddingHorizontal,
                            end = dimens.screenPaddingHorizontal,
                            top = dimens.screenPaddingTop,
                            bottom = dimens.gap24,
                        ),
                    verticalArrangement = Arrangement.spacedBy(dimens.gap16),
                ) {
                    Header(state.totalScore)
                    PlayCard(
                        label = stringResource(Res.string.home_play_label),
                        playLabel = stringResource(Res.string.home_play),
                        contentDescription = stringResource(Res.string.home_play_description),
                        onClick = { onIntent(HomeIntent.Play) },
                    )
                    DailyCard(state.dailyState, onOpen = { onIntent(HomeIntent.OpenDaily) })
                    DuelHomeBox(
                        title = stringResource(Res.string.home_tile_duel),
                        subtitle = stringResource(Res.string.home_tile_duel_subtitle),
                        onClick = { onIntent(HomeIntent.OpenDuel) },
                    )
                    Spacer(Modifier.weight(1f))
                    Column(
                        modifier = Modifier.padding(top = dimens.gap8),
                        verticalArrangement = Arrangement.spacedBy(dimens.gap16),
                    ) {
                        Text(
                            stringResource(Res.string.home_study_header),
                            style = LexiTheme.typography.label12,
                            color = colors.ink,
                            modifier = Modifier.lexiHeading(),
                        )
                        StudyTiles(state, onIntent)
                    }
                }
            }
            RuledBottomBar(
                items = listOf(
                    BottomBarItem(
                        icon = LexiIcons.Stats,
                        label = stringResource(Res.string.home_bar_stats),
                        onClick = { onIntent(HomeIntent.OpenStats) },
                        contentDescription = stringResource(Res.string.home_bar_stats),
                    ),
                    BottomBarItem(
                        icon = LexiIcons.Invite,
                        label = stringResource(Res.string.home_bar_invite),
                        onClick = { onIntent(HomeIntent.Invite(inviteMessage)) },
                        contentDescription = stringResource(Res.string.home_bar_invite_description),
                    ),
                    // Also the macrobenchmark's "Home is shown" marker (`By.desc("Settings")`, `LexiJourney`).
                    BottomBarItem(
                        icon = LexiIcons.Settings,
                        label = stringResource(Res.string.home_settings),
                        onClick = { onIntent(HomeIntent.OpenSettings) },
                        contentDescription = stringResource(Res.string.home_settings),
                    ),
                ),
                modifier = Modifier
                    .testTag(HOME_BOTTOM_BAR_TAG)
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

/**
 * Logo left, total points right ("12.480" over "toplam puan"); the points are one node read with their label. The
 * text follows the font size up to 1.3× and each line shrinks instead of wrapping, so the header stays two short lines
 * and never pushes the play card down.
 */
@Composable
private fun Header(totalScore: Long) {
    val colors = LexiTheme.colors
    val separator = stringResource(Res.string.home_thousands_separator)
    val total = remember(totalScore, separator) { groupThousands(totalScore, separator) }
    val description = stringResource(Res.string.home_total_points_description, total)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LexiLogo(size = LexiLogoSize.Header)
        WithCappedFontScale(FlowDefaults.CONTROL_FONT_SCALE_CAP) {
            // Only as wide as the points (the spoken node's focus box), but never wider than the room beside the logo.
            Column(
                modifier = Modifier.weight(
                    1f,
                    fill = false,
                ).padding(start = LexiTheme.dimens.gap12).clearAndSetSemantics {
                    contentDescription = description
                },
                horizontalAlignment = Alignment.End,
            ) {
                FitLine(total, LexiTheme.typography.heading24.copy(textAlign = TextAlign.End), colors.ink)
                FitLine(
                    stringResource(Res.string.home_total_points),
                    LexiTheme.typography.caption12SemiBold.copy(textAlign = TextAlign.End),
                    colors.inkMuted,
                )
            }
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
            StudyTile(
                title = notebook,
                subtitle = pluralStringResource(Res.plurals.home_tile_notebook_due, state.dueReviews, state.dueReviews),
                marker = DecorShapeKind.Circle,
                markerColor = colors.blue,
                onClick = { onIntent(HomeIntent.OpenNotebook) },
                modifier = modifier,
            )
        },
        { modifier ->
            StudyTile(
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
    val minWidth = studyTileMinWidth(listOf(notebook, exams, examSubtitle))
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

/**
 * The daily joker gift as an info toast below the header; dismissible by tap or upward swipe.
 *
 * Tap dismisses immediately. An upward drag tracks the finger via [translationY] on [graphicsLayer]
 * (no layout changes). On release below ~1/3 of its height it springs back; past that threshold
 * or on an upward fling, it animates out using row 11 motion tokens ([LexiMotion.toastDurationMillis] 180 ms,
 * [LexiMotion.mechanical] easing) and dispatches dismissal. With reduced motion, dismissal is instant.
 */
@Composable
private fun GiftToast(notice: DailyGiftNotice, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val message = when (notice.type) {
        DailyGiftNoticeType.Daily -> stringResource(Res.string.home_joker_gift_daily, notice.amount)
        DailyGiftNoticeType.Streak -> stringResource(Res.string.home_joker_gift_streak, notice.amount)
        DailyGiftNoticeType.Combined -> stringResource(Res.string.home_joker_gift_combined, notice.amount)
    }
    val dismissLabel = stringResource(Res.string.home_notice_dismiss)
    val motion = LexiTheme.motion
    val dimens = LexiTheme.dimens
    val density = LocalDensity.current
    val viewConfiguration = LocalViewConfiguration.current
    val dragOffsetY = remember { Animatable(0f) }
    var itemHeightPx by remember { mutableFloatStateOf(0f) }
    var isDismissing by remember { mutableStateOf(false) }
    val latestOnDismiss by rememberUpdatedState(onDismiss)

    SlidingToast(
        message = message,
        onDismissed = {
            if (!isDismissing) {
                isDismissing = true
                latestOnDismiss()
            }
        },
        visibleMillis = NOTICE_VISIBLE_MILLIS,
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { itemHeightPx = it.height.toFloat() }
            .focusable()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                onClick(label = dismissLabel) {
                    if (!isDismissing) {
                        isDismissing = true
                        latestOnDismiss()
                    }
                    true
                }
            }
            .pointerInput(density, motion, viewConfiguration) {
                coroutineScope {
                    var dragJob: Job? = null
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (isDismissing) return@awaitEachGesture
                        dragJob?.cancel()
                        val startY = down.position.y
                        var isDrag = false
                        val velocityTracker = VelocityTracker()
                        velocityTracker.addPosition(down.uptimeMillis, down.position)

                        val fallbackThresholdPx = with(density) { dimens.gap20.toPx() }
                        val thresholdPx = if (itemHeightPx > 0f) itemHeightPx / 3f else fallbackThresholdPx
                        val tapSlopPx = (thresholdPx * 0.35f).coerceAtMost(viewConfiguration.touchSlop)
                        val flingThresholdPx = with(density) { (dimens.minTouchTarget * 5).toPx() }

                        val pointerId = down.id
                        var gestureEndedCleanly = false

                        try {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                                if (change.changedToUp()) {
                                    gestureEndedCleanly = true
                                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                                    val velocityY = velocityTracker.calculateVelocity().y
                                    change.consume()
                                    if (!isDrag) {
                                        if (!isDismissing) {
                                            isDismissing = true
                                            latestOnDismiss()
                                        }
                                    } else {
                                        val isPastThreshold = -dragOffsetY.value >= thresholdPx
                                        val isUpwardFling = velocityY <= -flingThresholdPx
                                        if (isPastThreshold || isUpwardFling) {
                                            if (!isDismissing) {
                                                isDismissing = true
                                                dragJob?.cancel()
                                                launch {
                                                    if (motion.reducedMotion) {
                                                        latestOnDismiss()
                                                    } else {
                                                        val slideDistancePx =
                                                            with(density) { motion.toastSlideDistance.toPx() }
                                                        val exitDistance =
                                                            if (itemHeightPx > 0f) {
                                                                itemHeightPx + slideDistancePx
                                                            } else {
                                                                slideDistancePx * 4f
                                                            }
                                                        dragOffsetY.animateTo(
                                                            targetValue = -exitDistance,
                                                            animationSpec = tween(
                                                                durationMillis = motion.toastDurationMillis,
                                                                easing = motion.mechanical,
                                                            ),
                                                        )
                                                        latestOnDismiss()
                                                    }
                                                }
                                            }
                                        } else if (!isDismissing) {
                                            dragJob?.cancel()
                                            dragJob = launch {
                                                if (motion.reducedMotion) {
                                                    dragOffsetY.snapTo(0f)
                                                } else {
                                                    dragOffsetY.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(
                                                            durationMillis = motion.toastDurationMillis,
                                                            easing = motion.mechanical,
                                                        ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    break
                                }

                                if (change.isConsumed) {
                                    break
                                }

                                val currentY = change.position.y
                                val deltaY = currentY - startY
                                velocityTracker.addPosition(change.uptimeMillis, change.position)

                                if (!isDrag && (deltaY < -tapSlopPx || deltaY > tapSlopPx)) {
                                    isDrag = true
                                }

                                if (isDrag && !isDismissing) {
                                    change.consume()
                                    val target = deltaY.coerceAtMost(0f)
                                    dragJob?.cancel()
                                    dragJob = launch { dragOffsetY.snapTo(target) }
                                }
                            }
                        } finally {
                            if (!gestureEndedCleanly && !isDismissing) {
                                dragJob?.cancel()
                                launch {
                                    if (motion.reducedMotion) {
                                        dragOffsetY.snapTo(0f)
                                    } else {
                                        dragOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(
                                                durationMillis = motion.toastDurationMillis,
                                                easing = motion.mechanical,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .graphicsLayer { translationY = dragOffsetY.value },
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
