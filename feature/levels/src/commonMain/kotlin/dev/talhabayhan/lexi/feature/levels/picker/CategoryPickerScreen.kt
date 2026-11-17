package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.zIndex
import dev.talhabayhan.lexi.core.designsystem.component.CategoryArt
import dev.talhabayhan.lexi.core.designsystem.component.CategoryArtKind
import dev.talhabayhan.lexi.core.designsystem.component.ExamArt
import dev.talhabayhan.lexi.core.designsystem.component.ExamArtKind
import dev.talhabayhan.lexi.core.designsystem.component.FlowDefaults
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LevelChipRow
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.PickerCard
import dev.talhabayhan.lexi.core.designsystem.component.ScrollToTopPill
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMotion
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.motion.animateScrollToTop
import dev.talhabayhan.lexi.core.designsystem.motion.perspective
import dev.talhabayhan.lexi.core.designsystem.motion.rememberMotionTier
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.levels.generated.resources.Res
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_back
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_card_description
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_error
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_exam_ielts
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_exam_toefl
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_exam_yds_yokdil
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_exam_yks_dil
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_exams_header
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_exams_subtitle
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_level_description
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_mixed_fallback
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_progress
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_retry
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_scroll_top
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_soon
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_soon_description
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_title
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * "Çarkını seç" (`video-flow-notes.md` §2, `drafts/video-flow/cats_v3.png`): back + title and the six level chips stay
 * on top; below them a 2-column grid scrolls: Karışık and every category of the level, then "Sınav paketleri" (open
 * packs, then the locked upcoming exams), above the system-bar insets. "Başa dön" floats once the first screen is
 * scrolled past. The mode / time dialog ([PlayDialog]) opens over it with motion #13–#15 driven by [flip] (the tapped
 * card lifts, turns away and becomes the dialog). [entrance] plays motion #18 (the Route decides: first open per
 * session, tier Full).
 */
@Composable
internal fun CategoryPickerScreen(
    state: CategoryPickerUiState,
    onIntent: (CategoryPickerIntent) -> Unit,
    modifier: Modifier = Modifier,
    entrance: Boolean = false,
    flip: PickerDialogFlip = rememberPickerDialogFlip(state.dialog),
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    val gridState = rememberLazyGridState()
    val turkish = Locale.current.language == "tr"
    val dialogUp = state.dialog != null || flip.dialogShown
    val warmUpCard = rememberDialogWarmUp(state, gridState, entrance, dialogUp)
    Box(modifier.fillMaxSize()) {
        // Behind the opaque cream layer below, so it is never seen (like the NavHost's `FlowWarmUp`).
        if (warmUpCard != null) PickerDialogWarmUp(warmUpCard, state, turkish)
        Box(Modifier.fillMaxSize().background(colors.cream)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                    .lexiContentWidth()
                    // While the dialog is open only the dialog is read.
                    .then(if (dialogUp) Modifier.clearAndSetSemantics {} else Modifier),
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = dimens.screenPaddingHorizontal,
                        end = dimens.screenPaddingHorizontal,
                        top = dimens.screenPaddingTop,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(dimens.gap14),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavSquareButton(
                        icon = LexiIcons.Back,
                        contentDescription = stringResource(Res.string.picker_back),
                        onClick = { onIntent(CategoryPickerIntent.Back) },
                        size = IconButtonDefaults.NavSizeLarge,
                    )
                    Text(
                        stringResource(Res.string.picker_title),
                        style = LexiTheme.typography.title,
                        color = colors.ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.lexiHeading(),
                    )
                }
                val levelDescription = Level.entries.map {
                    stringResource(Res.string.picker_level_description, it.name)
                }
                LevelChipRow(
                    labels = Level.entries.map { it.name },
                    selectedIndex = Level.entries.indexOf(state.level),
                    onSelect = { onIntent(CategoryPickerIntent.SelectLevel(Level.entries[it])) },
                    contentDescriptions = levelDescription,
                    modifier = Modifier.padding(
                        start = dimens.screenPaddingHorizontal,
                        end = dimens.screenPaddingHorizontal,
                        top = dimens.gap16,
                    ),
                )
                if (state.hasError) {
                    ErrorState(onRetry = { onIntent(CategoryPickerIntent.Retry) })
                } else {
                    PickerGrid(state, gridState, turkish, entrance, flip, onIntent)
                }
            }
            // Behind the scrim while the dialog is open: not shown, so a screen reader cannot reach it either.
            if (!dialogUp) ScrollTopButton(gridState, Modifier.align(Alignment.BottomEnd))
            PlayDialog(state, turkish, onIntent, flip)
        }
    }
}

/** The dialog is warmed up once per app process (shader cache and loaded classes outlive the screen). */
private object DialogWarmUpState {
    var done = false
}

/**
 * The card to pose the dialog warm-up with, for a few frames, once the picker is idle: loaded, the grid entrance
 * (#18) landed, nothing scrolling, no dialog up, tier not Minimal (reduced motion has no flip to warm). Null otherwise
 * and for good once it ran. A player who taps a card before it ran gets the flip as before.
 */
@Composable
private fun rememberDialogWarmUp(
    state: CategoryPickerUiState,
    gridState: LazyGridState,
    entrance: Boolean,
    dialogUp: Boolean,
): PickerCategoryUi? {
    val tier = rememberMotionTier()
    val card = state.categories.firstOrNull()
    val ready = state.isLoaded && card != null && !dialogUp && tier != MotionTier.Minimal && !DialogWarmUpState.done
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(ready) {
        if (!ready) return@LaunchedEffect
        try {
            delay(if (entrance) WARM_UP_AFTER_ENTRANCE_MILLIS else WARM_UP_IDLE_MILLIS)
            snapshotFlow { gridState.isScrollInProgress }.first { !it }
            shown = true
            // Three frames: composed and measured, drawn once (shaders compiled), and one more to be safe.
            repeat(WARM_UP_FRAMES) { withFrameMillis { } }
            DialogWarmUpState.done = true
        } finally {
            shown = false
        }
    }
    return if (shown && ready) card else null
}

private const val WARM_UP_AFTER_ENTRANCE_MILLIS = 1_100L
private const val WARM_UP_IDLE_MILLIS = 400L
private const val WARM_UP_FRAMES = 3

@Composable
private fun PickerGrid(
    state: CategoryPickerUiState,
    gridState: LazyGridState,
    turkish: Boolean,
    entrance: Boolean,
    flip: PickerDialogFlip,
    onIntent: (CategoryPickerIntent) -> Unit,
) {
    val dimens = LexiTheme.dimens
    val flow = LexiTheme.motion.flow
    val shadowColor = LexiTheme.shadows.s4.color
    val navigationBar = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()
    val enter = rememberGridEntrance(entrance, ready = state.isLoaded && state.categories.isNotEmpty())
    val mixedFallback = stringResource(Res.string.picker_mixed_fallback)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = dimens.screenPaddingHorizontal,
            end = dimens.screenPaddingHorizontal,
            top = dimens.gap16,
            bottom = FlowDefaults.GridBottomRoom + navigationBar.calculateBottomPadding(),
        ),
        horizontalArrangement = Arrangement.spacedBy(FlowDefaults.GridColumnGap),
        verticalArrangement = Arrangement.spacedBy(FlowDefaults.GridRowGap),
    ) {
        itemsIndexed(state.categories, key = { _, card ->
            "category:${card.id.value}"
        }, contentType = { _, _ -> CARD }) {
                index,
                card,
            ->
            val name = (if (turkish) card.nameTr else card.nameEn).ifEmpty { if (card.isMixed) mixedFallback else "" }
            DisposableEffect(flip, card.id) { onDispose { flip.cardRects.remove(card.id) } }
            // Only the card the dialog belongs to recomposes when a flip starts or ends.
            val flipping by remember(flip, card.id) {
                derivedStateOf { flip.dialogShown && flip.card?.id == card.id }
            }
            PickerCard(
                name = name,
                contentDescription = stringResource(Res.string.picker_card_description, name, card.learned, card.total),
                art = { modifier ->
                    CategoryArt(CategoryArtKind.forId(card.id.value) ?: CategoryArtKind.Mixed, modifier)
                },
                progressText = stringResource(Res.string.picker_progress, card.learned, card.total),
                progress = card.fraction,
                selected = card.id == state.selectedCategory,
                // The grid takes taps only while no dialog is up (the dialog's own layers handle a reopen).
                onClick = { if (!flip.dialogShown) onIntent(CategoryPickerIntent.OpenCategory(card.id)) },
                modifier = Modifier
                    // The laid-out rectangle (before the animated layers) the dialog grows from.
                    .onGloballyPositioned { flip.cardRects[card.id] = it.rectInRoot() }
                    .then(enter.modifier(index))
                    .then(
                        if (flipping) {
                            Modifier.flipCard(flip, flow, shadowColor)
                        } else {
                            Modifier
                        },
                    ),
            )
        }
        // The exam section waits for the level's cards: shown alone first, it would be the first visible item, and
        // the grid would keep it in place when the cards are inserted above it (opening scrolled to the exams).
        if (!state.isLoaded || state.categories.isEmpty()) return@LazyVerticalGrid
        item(key = EXAMS_HEADER, span = { GridItemSpan(maxLineSpan) }, contentType = HEADER) { ExamsHeader() }
        itemsIndexed(state.packs, key = { _, pack ->
            "pack:${pack.id.value}"
        }, contentType = { _, _ -> CARD }) { _, pack ->
            val title = if (turkish) pack.titleTr else pack.titleEn
            PickerCard(
                name = title,
                contentDescription = stringResource(
                    Res.string.picker_card_description,
                    title,
                    pack.learned,
                    pack.total,
                ),
                art = { modifier -> ExamArt(ExamArtKind.forId(pack.id.value) ?: ExamArtKind.Lgs, modifier) },
                progressText = stringResource(Res.string.picker_progress, pack.learned, pack.total),
                progress = pack.fraction,
                role = Role.Button,
                onClick = { onIntent(CategoryPickerIntent.OpenPack(pack.id)) },
            )
        }
        itemsIndexed(state.lockedExams, key = { _, id -> "locked:$id" }, contentType = { _, _ -> CARD }) { _, id ->
            val title = stringResource(lockedTitle(id))
            PickerCard(
                name = title,
                contentDescription = stringResource(Res.string.picker_soon_description, title),
                art = { modifier -> ExamArt(ExamArtKind.forId(id) ?: ExamArtKind.Lgs, modifier, locked = true) },
                locked = true,
                lockedLabel = stringResource(Res.string.picker_soon),
            )
        }
    }
}

/**
 * Motion #13 / #14 / #15 on the tapped card, read in the layer and draw phase only: it rises and tilts with `cardLift`
 * (Full), its hard shadow grows 4 → 10 dp (an extra ink rectangle behind the card's own s4 shadow, only while lifted),
 * it turns on Y and hides once edge-on. Drawn above its neighbours while it moves.
 */
private fun Modifier.flipCard(flip: PickerDialogFlip, flow: FlowMotion, shadowColor: Color): Modifier = this
    .zIndex(1f)
    .graphicsLayer {
        val frame = flip.frame()
        translationY = frame.cardLift * flow.cardLiftOffset.toPx()
        rotationZ = frame.cardLift * flow.cardLiftRotationDegrees
        rotationY = frame.cardTurnDegrees
        perspective(flow.flipPerspective)
        alpha = frame.cardAlpha
    }
    .drawBehind {
        val lift = flip.frame().cardLift
        if (lift > 0f) {
            val shift = lerp(flow.cardRestShadow, flow.cardLiftShadow, lift).toPx()
            drawRect(shadowColor, Offset(shift, shift), size)
        }
    }

private const val CARD = "card"
private const val HEADER = "header"
private const val EXAMS_HEADER = "examsHeader"

private fun lockedTitle(id: String): StringResource = when (id) {
    "yds-yokdil" -> Res.string.picker_exam_yds_yokdil
    "yks-dil" -> Res.string.picker_exam_yks_dil
    "toefl" -> Res.string.picker_exam_toefl
    else -> Res.string.picker_exam_ielts
}

@Composable
private fun ExamsHeader() {
    Column(
        Modifier.fillMaxWidth().padding(top = LexiTheme.dimens.gap20),
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap4),
    ) {
        Text(
            stringResource(Res.string.picker_exams_header),
            style = LexiTheme.typography.label12,
            color = LexiTheme.colors.inkMuted,
        )
        Text(
            stringResource(Res.string.picker_exams_subtitle),
            style = LexiTheme.typography.heading22,
            color = LexiTheme.colors.ink,
            modifier = Modifier.lexiHeading(),
        )
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(LexiTheme.dimens.screenPaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(Res.string.picker_error), style = LexiTheme.typography.body16, color = LexiTheme.colors.ink)
        LexiButton(
            text = stringResource(Res.string.picker_retry),
            onClick = onRetry,
            variant = LexiButtonVariant.Secondary,
        )
    }
}

/** "Başa dön": shown once the first screen of the grid is scrolled past; scrolls back to the top. */
@Composable
private fun ScrollTopButton(gridState: LazyGridState, modifier: Modifier) {
    val pastFirstScreen by remember(gridState) {
        derivedStateOf {
            val visible = gridState.layoutInfo.visibleItemsInfo.size
            visible > 0 && gridState.firstVisibleItemIndex >= visible
        }
    }
    if (!pastFirstScreen) return
    val scope = rememberCoroutineScope()
    val motion = LexiTheme.motion
    val minimal = rememberMotionTier() == MotionTier.Minimal
    ScrollToTopPill(
        label = stringResource(Res.string.picker_scroll_top),
        onClick = {
            // Motion #19: 400 ms ease-out whatever the distance (120 ms with reduced motion); a drag takes over.
            scope.launch {
                val flow = motion.flow
                gridState.animateScrollToTop(
                    durationMillis = if (minimal) flow.reducedCrossfadeMillis else flow.scrollTopMillis,
                    easing = motion.easeOut,
                    maxStretchPx = gridState.layoutInfo.viewportSize.height * flow.scrollTopMaxStretchViewports,
                )
            }
        },
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.End))
            .padding(end = LexiTheme.dimens.screenPaddingHorizontal, bottom = LexiTheme.dimens.gap20),
    )
}

/**
 * Motion #18 (`FlowMotion.gridCardIn`): the cards rise and fade in one after another, read only in the draw phase
 * (one clock for all cards). The run is registered with the session's frame monitor, so a slow device drops to Lite.
 */
private class GridEntrance(
    private val flow: FlowMotion?,
    private val clock: Animatable<Float, *>?,
    private val easing: Easing,
    private val offsetPx: Float,
) {
    fun modifier(index: Int): Modifier {
        if (flow == null || clock == null || index >= ENTRANCE_CARDS) return Modifier
        val step = flow.gridCardIn.staggered(index, flow.gridCardStaggerMillis)
        return Modifier.graphicsLayer {
            val fraction = easing.transform(step.progressAt(clock.value.toLong()))
            alpha = fraction
            translationY = (1f - fraction) * offsetPx
        }
    }
}

/** Only the cards of the first screen take part; later ones are never seen entering. */
private const val ENTRANCE_CARDS = 8

@Composable
private fun rememberGridEntrance(play: Boolean, ready: Boolean): GridEntrance {
    val motion = LexiTheme.motion
    val flow = motion.flow
    val offsetPx = with(LocalDensity.current) { flow.gridCardOffset.toPx() }
    if (!play) return remember { GridEntrance(null, null, motion.linear, 0f) }
    val clock = remember { Animatable(0f) }
    val monitor = LocalMotionPolicy.current.frameMonitor
    val end = flow.gridCardIn.staggered(ENTRANCE_CARDS - 1, flow.gridCardStaggerMillis).endMillis
    // Starts when the cards are there (content is read off the main thread), not on the first frame.
    LaunchedEffect(ready) {
        if (!ready) return@LaunchedEffect
        monitor.beginTransition()
        try {
            clock.animateTo(end.toFloat(), tween(end, easing = LinearEasing))
        } finally {
            monitor.endTransition()
        }
    }
    return remember(clock, offsetPx) { GridEntrance(flow, clock, motion.mechanical, offsetPx) }
}

/** The grid entrance (#18) plays on the first open of the screen per app session, in tier Full only. */
@Composable
internal fun rememberPlayGridEntrance(): Boolean {
    val tier = rememberMotionTier()
    return remember {
        val first = !GridEntranceSession.played
        GridEntranceSession.played = true
        first && tier == MotionTier.Full
    }
}

/** Process-wide: "first open per session". */
private object GridEntranceSession {
    var played: Boolean = false
}
