package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Toast colors (`16-components` § BİLDİRİMLER). */
public enum class ToastStyle {
    /** Blue with white text and a white circle: "Doğru! KITCHEN +10". */
    Success,

    /** White with a yellow leaf, usually with an undo action. */
    Info,
}

/** The underlined action of an info toast ("Geri al") and what it does. */
@Immutable
public class ToastAction(public val label: String, public val onClick: () -> Unit)

/** Toast and dialog geometry (`16-components.html`). */
public object FeedbackDefaults {
    /** Toast: `padding: 10px 14px`, gap 10, 20dp marker + 2dp border (content-box = 24). */
    public val ToastPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    public val ToastGap: Dp = 10.dp
    public val ToastMarkerSize: Dp = 24.dp

    /**
     * Toast action: 32 high, padding 0 6. Compose hit testing extends small pointer targets to the platform minimum
     * touch size (48dp ≥ `minTouchTarget`), so the action stays tappable without making the toast taller.
     */
    public val ToastActionHeight: Dp = 32.dp
    public val ToastActionPadding: PaddingValues = PaddingValues(horizontal = 6.dp)

    /** Dialog: padding 18, gap 12, 36dp red square, buttons 48dp with gap 10. */
    public val DialogPadding: Dp = 18.dp
    public val DialogGap: Dp = 12.dp
    public val DialogIconSize: Dp = 36.dp
    public val DialogButtonGap: Dp = 10.dp
}

/**
 * Toast (motion.md #11: slides down 16dp from the top in 180 ms, auto-dismiss after 2.5 s — hosted by the screen).
 * 3dp border, s4 (the canvas value; components.md says s5), marker, message and either [trailing] text ("+10") or an
 * underlined [action] ("Geri al"). Announced politely as a live region.
 */
@Composable
public fun Toast(
    message: String,
    modifier: Modifier = Modifier,
    style: ToastStyle = ToastStyle.Success,
    trailing: String? = null,
    action: ToastAction? = null,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    val success = style == ToastStyle.Success
    val content = if (success) colors.white else colors.ink
    Row(
        modifier = modifier
            .semantics { liveRegion = LiveRegionMode.Polite }
            .lexiSurface(
                color = if (success) colors.blue else colors.white,
                border = BorderStroke(LexiTheme.borders.regular, colors.ink),
                shadow = LexiTheme.shadows.s4,
            )
            .padding(FeedbackDefaults.ToastPadding),
        horizontalArrangement = Arrangement.spacedBy(FeedbackDefaults.ToastGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DecorShape(
            kind = if (success) DecorShapeKind.Circle else DecorShapeKind.Leaf,
            color = if (success) colors.white else colors.yellow,
            border = BorderStroke(LexiTheme.borders.thin, colors.ink),
            modifier = Modifier.size(FeedbackDefaults.ToastMarkerSize),
        )
        Text(
            text = message,
            style = if (success) typography.bodyStrong16 else typography.bodyBold15,
            color = content,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) Text(trailing, style = typography.bodyStrong16, color = content)
        if (action != null) {
            PressableSurface(
                onClick = action.onClick,
                modifier = Modifier.height(FeedbackDefaults.ToastActionHeight),
                color = Color.Transparent,
                border = null,
                shadow = null,
            ) {
                Text(
                    action.label,
                    style = typography.link14,
                    color = content,
                    modifier = Modifier.padding(FeedbackDefaults.ToastActionPadding),
                )
            }
        }
    }
}

/**
 * Confirmation dialog panel: white, 3dp border, s8, padding 18, red square, title (22/800), message (14/500 lh 1.4)
 * and two 48dp buttons — flat Secondary [dismissLabel] and Primary [confirmLabel] with s4. Place it over a scrim
 * ([ModalScrim]); it does not dismiss itself.
 */
@Composable
public fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DialogPanel(title = title, message = message, modifier = modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FeedbackDefaults.DialogButtonGap)) {
            DialogButton(dismissLabel, onDismiss, primary = false, modifier = Modifier.weight(1f))
            DialogButton(confirmLabel, onConfirm, primary = true, modifier = Modifier.weight(1f))
        }
    }
}

/**
 * The dialog panel of [ConfirmDialog] with any [actions] under the message (a column, gap 12), e.g. a stack of
 * [DialogButton]s. Place it over a scrim ([ModalScrim]); it does not dismiss itself.
 */
@Composable
public fun DialogPanel(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit,
) {
    val colors = LexiTheme.colors
    val typography = LexiTheme.typography
    Column(
        modifier = modifier
            .semantics { paneTitle = title }
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink), LexiTheme.shadows.s8)
            .padding(FeedbackDefaults.DialogPadding),
        verticalArrangement = Arrangement.spacedBy(FeedbackDefaults.DialogGap),
    ) {
        Box(
            Modifier
                .size(FeedbackDefaults.DialogIconSize)
                .clearAndSetSemantics {}
                .lexiSurface(colors.red, BorderStroke(LexiTheme.borders.regular, colors.ink)),
        )
        Text(title, style = typography.heading22)
        Text(message, style = typography.body14)
        actions()
    }
}

/** A 48dp dialog button (15/800): [primary] = red Primary with s4, else flat Secondary. Width from [modifier]. */
@Composable
public fun DialogButton(text: String, onClick: () -> Unit, primary: Boolean, modifier: Modifier = Modifier) {
    LexiButton(
        text = text,
        onClick = onClick,
        variant = if (primary) LexiButtonVariant.Primary else LexiButtonVariant.Secondary,
        textStyle = LexiTheme.typography.bodyStrong15,
        height = LexiButtonDefaults.SmallHeight,
        shadow = if (primary) LexiTheme.shadows.s4 else null,
        modifier = modifier,
    )
}

// design sample, not user-facing
@Preview(widthDp = 420)
@Composable
private fun FeedbackPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            modifier = Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap16),
        ) {
            Toast("Doğru! KITCHEN", trailing = "+10", modifier = Modifier.fillMaxWidth())
            Toast(
                "\"egg\" kelime defterine eklendi",
                style = ToastStyle.Info,
                action = ToastAction("Geri al") {},
                modifier = Modifier.fillMaxWidth(),
            )
            ConfirmDialog(
                title = "Çarkı bırakmak istiyor musun?",
                message = "Bu çarktaki puanın kaydedilmez. Pas geçtiğin kelimeler yine de defterine eklenir.",
                confirmLabel = "Bırak",
                dismissLabel = "Devam et",
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
