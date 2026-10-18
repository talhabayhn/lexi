package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.dashedBorder
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight

/** Sheet and empty-state geometry (`05-joker-sheet.html`, `16-components.html`). */
public object OverlayDefaults {
    /** Sheet `padding: 20px 20px 28px` (the screen paddings), gap 12, 4dp top border. */
    public val SheetPadding: PaddingValues
        @Composable get() = with(LexiTheme.dimens) {
            PaddingValues(
                start = screenPaddingHorizontal,
                top = screenPaddingTopGame,
                end = screenPaddingHorizontal,
                bottom = screenPaddingBottom,
            )
        }
    public val SheetGap: Dp = 12.dp

    /** Empty state: padding 14, gap 14, 56dp illustration (circle 36 + 2 × 2, square 30 + 2 × 2). */
    public val EmptyPadding: Dp = 14.dp
    public val EmptyGap: Dp = 14.dp
    internal val EmptyArtSize: Dp = 56.dp
    internal val EmptyCircleSize: Dp = 40.dp
    internal val EmptySquareSize: Dp = 34.dp
    internal const val EMPTY_LINE_HEIGHT: Double = 1.4
}

/**
 * Full-screen ink scrim at 55 % behind dialogs and sheets; a tap on it calls [onDismissRequest]. Place it at the root
 * of the screen (it fills its parent); back handling is the screen's (M4).
 */
@Composable
public fun ModalScrim(onDismissRequest: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .clearAndSetSemantics {}
            .background(LexiTheme.colors.scrim)
            .clickable(interactionSource = null, indication = null, onClick = onDismissRequest),
    )
}

/**
 * Modal bottom sheet host (motion.md #10): the scrim fades to 55 % and the sheet slides up from the bottom in 220 ms
 * (`mechanical`); under reduced motion the sheet appears without sliding. Fills its parent; [content] is laid out in
 * a cream panel with a 4dp ink top edge, padding 20/20/28 plus the bottom/side safe-drawing insets, gap 12, under a
 * title row with the close button.
 */
@Composable
public fun BottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    closeContentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val motion = LexiTheme.motion
    val duration = motion.bottomSheetDurationMillis
    Box(modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(duration, easing = motion.mechanical)),
            exit = fadeOut(tween(duration, easing = motion.mechanical)),
        ) {
            ModalScrim(onDismissRequest)
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .lexiContentWidth(),
            enter = if (motion.reducedMotion) {
                EnterTransition.None
            } else {
                slideInVertically(
                    tween(duration, easing = motion.mechanical),
                ) {
                    it
                }
            },
            exit = if (motion.reducedMotion) {
                ExitTransition.None
            } else {
                slideOutVertically(
                    tween(duration, easing = motion.mechanical),
                ) {
                    it
                }
            },
        ) {
            BottomSheetPanel(
                title = title,
                trailing = { SheetCloseButton(closeContentDescription, onClick = onDismissRequest) },
                content = content,
            )
        }
    }
}

/** The sheet panel alone (cream, 4dp top border, padding, gap 12) with its title row. */
@Composable
public fun BottomSheetPanel(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LexiTheme.colors
    val topBorder = LexiTheme.borders.thick
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { paneTitle = title }
            .clickable(interactionSource = null, indication = null) {}
            .background(colors.cream)
            .drawBehind { drawRect(colors.ink, size = Size(size.width, topBorder.toPx())) }
            .padding(top = topBorder)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .padding(OverlayDefaults.SheetPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(OverlayDefaults.SheetGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = LexiTheme.typography.heading24)
            trailing()
        }
        content()
    }
}

/** Close button for [BottomSheetPanel]'s title row (44dp square, ×). */
@Composable
public fun SheetCloseButton(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    NavSquareButton(LexiIcons.Close, contentDescription = contentDescription, onClick = onClick, modifier = modifier)
}

/** Empty state: dashed 3dp ink frame, overlapping yellow circle + blue square, message (14/600, lh 1.4). */
@Composable
public fun EmptyState(message: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    val border = LexiTheme.borders.regular
    Row(
        modifier = modifier
            .dashedBorder(border, colors.ink)
            .padding(border)
            .padding(OverlayDefaults.EmptyPadding),
        horizontalArrangement = Arrangement.spacedBy(OverlayDefaults.EmptyGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(OverlayDefaults.EmptyArtSize)) {
            val outline = BorderStroke(LexiTheme.borders.thin, colors.ink)
            DecorShape(DecorShapeKind.Circle, colors.yellow, Modifier.size(OverlayDefaults.EmptyCircleSize), outline)
            DecorShape(
                DecorShapeKind.Square,
                colors.blue,
                Modifier.align(Alignment.BottomEnd).size(OverlayDefaults.EmptySquareSize),
                outline,
            )
        }
        Text(
            message,
            style = LexiTheme.typography.caption14SemiBold.withCssLineHeight(OverlayDefaults.EMPTY_LINE_HEIGHT),
        )
    }
}

/**
 * [EmptyState] with an action, for error and dead-end states (retry, go back). DESIGN-DEBT: no design shows an empty
 * state with an action; built from the component library: the empty state over a full-width secondary [LexiButton]
 * ([actionText]), gap 16, and an optional underlined [LexiTextLink] under it ([secondaryText]).
 */
@Composable
public fun EmptyState(
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String? = null,
    onSecondary: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyState(message, Modifier.fillMaxWidth())
        LexiButton(
            text = actionText,
            onClick = onAction,
            variant = LexiButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
        if (secondaryText != null) LexiTextLink(text = secondaryText, onClick = onSecondary)
    }
}

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 500)
@Composable
private fun OverlaysPreview() {
    LexiTheme(reducedMotion = false) {
        Box(Modifier.fillMaxSize().background(LexiTheme.colors.cream)) {
            EmptyState(
                "Defterin henüz boş. Bir çark oyna; pas geçtiğin ve bilemediğin kelimeler buraya düşer.",
                Modifier.padding(LexiTheme.dimens.gap20),
            )
            BottomSheet(visible = true, onDismissRequest = {
            }, title = "Joker kullan", closeContentDescription = "Kapat") {
                LexiButton("Kısa reklam izle · +1 joker", onClick = {}, variant = LexiButtonVariant.Ink, shadow = null)
                Box(Modifier.height(LexiTheme.dimens.gap4))
            }
        }
    }
}
