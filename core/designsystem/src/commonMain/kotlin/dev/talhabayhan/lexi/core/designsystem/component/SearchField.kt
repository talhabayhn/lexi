package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme

/** Search field and count chip geometry (`08-notebook`). */
public object SearchFieldDefaults {
    /** `height: 48px`. */
    public val Height: Dp = 48.dp

    /** `padding: 0 12px`, gap 8, icon 18. */
    public val Padding: PaddingValues = PaddingValues(horizontal = 12.dp)
    public val Gap: Dp = 8.dp
    public val IconSize: Dp = 18.dp

    /** The HTML input keeps Chrome's default `padding: 1px 2px`. */
    public val InputInset: Dp = 2.dp

    /** Count chip on the blue CTA: `padding: 3px 8px`, 2dp border. */
    public val ChipPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
}

/**
 * Search field of `08-notebook`: 48dp white box with a 3dp ink border, magnifier, 15sp text and an ink-muted
 * [placeholder]. The text lives in [state] (the caller observes it), so typing never round-trips through a view model.
 */
@Composable
public fun SearchField(
    state: TextFieldState,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
) {
    val colors = LexiTheme.colors
    val style = LexiTheme.typography.body15.copy(color = colors.ink)
    BasicTextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SearchFieldDefaults.Height)
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
            .padding(SearchFieldDefaults.Padding)
            .semantics { contentDescription = placeholder },
        textStyle = style,
        cursorBrush = SolidColor(colors.ink),
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = { onSearch() },
        decorator = { inner ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(SearchFieldDefaults.Gap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    LexiIcons.Search,
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(SearchFieldDefaults.IconSize),
                )
                Box(
                    modifier = Modifier.weight(1f).padding(horizontal = SearchFieldDefaults.InputInset),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (state.text.isEmpty()) {
                        Text(
                            placeholder,
                            style = style,
                            color = colors.inkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    inner()
                }
            }
        },
    )
}

/** Small yellow count chip with a 2dp ink border ("24 kelime" on the notebook CTA); 14sp bold. */
@Composable
public fun CountChip(text: String, modifier: Modifier = Modifier) {
    val colors = LexiTheme.colors
    Text(
        text = text,
        style = LexiTheme.typography.caption14.copy(fontWeight = FontWeight.Bold),
        color = colors.ink,
        maxLines = 1,
        modifier = modifier
            .lexiSurface(colors.yellow, BorderStroke(LexiTheme.borders.thin, colors.ink))
            .padding(SearchFieldDefaults.ChipPadding),
    )
}

// design sample, not user-facing
@Preview(widthDp = 390)
@Composable
private fun SearchFieldPreview() {
    LexiTheme(reducedMotion = false) {
        Column(
            Modifier.background(LexiTheme.colors.cream).padding(LexiTheme.dimens.gap20).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14),
        ) {
            SearchField(rememberTextFieldState(), "Kelime ara (İngilizce veya Türkçe)")
            CountChip("24 kelime")
        }
    }
}
