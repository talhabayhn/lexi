package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.DecorShape
import dev.talhabayhan.lexi.core.designsystem.component.DecorShapeKind
import dev.talhabayhan.lexi.core.designsystem.component.DialogButton
import dev.talhabayhan.lexi.core.designsystem.component.DuelDefaults
import dev.talhabayhan.lexi.core.designsystem.component.FeedbackDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.ModalScrim
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.PressableSurface
import dev.talhabayhan.lexi.core.designsystem.component.SearchFieldDefaults
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.format.formatClock
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiHeading
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_clock_option_minutes
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_level_label
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_name_cancel
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_name_hint
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_name_save
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_name_title
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_player_change
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_player_change_description
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_player_label
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_rules_note
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_setup_back
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_setup_eyebrow
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_setup_subtitle
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_setup_title
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_start
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_time_label
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

/**
 * `17-duel-setup`: back button and eyebrow, headline, the two player cards (name + "Değiştir"), the shared level, the
 * clock per player, the rules note and BAŞLA pinned at the bottom. The part above BAŞLA scrolls when it does not fit
 * (small phones, large fonts). The name dialog ([state] `editingPlayer`) is DESIGN-DEBT: no design, built from the
 * dialog panel of `16-components` and the search field's input look.
 */
@Composable
internal fun DuelSetupScreen(
    state: DuelSetupUiState,
    onIntent: (DuelSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LexiTheme.colors
    val dimens = LexiTheme.dimens
    Box(modifier.fillMaxSize().background(colors.cream)) {
        DecorShape(
            kind = DecorShapeKind.Circle,
            color = colors.blue,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = DuelDefaults.SetupCircleBleed, y = DuelDefaults.SetupCircleTop)
                .size(DuelDefaults.SetupCircleSize),
        )
        DecorShape(
            kind = DecorShapeKind.HalfPill,
            color = colors.red,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = -DuelDefaults.SetupPillBleed, y = -DuelDefaults.SetupPillBottom)
                .size(DuelDefaults.SetupPillWidth, DuelDefaults.SetupPillHeight),
        )
        val dialogOpen = state.editingPlayer != null
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .lexiContentWidth()
                // Behind the dialog the screen is inert for screen readers.
                .then(if (dialogOpen) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    // Room under the last row keeps its hard shadow out of the scroll clip.
                    .padding(
                        start = dimens.screenPaddingHorizontal,
                        top = dimens.screenPaddingTop,
                        end = dimens.screenPaddingHorizontal,
                        bottom = LexiTheme.shadows.s4.offset,
                    ),
                verticalArrangement = Arrangement.spacedBy(dimens.gap16),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimens.gap12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavSquareButton(
                        icon = LexiIcons.Back,
                        contentDescription = stringResource(Res.string.duel_setup_back),
                        onClick = { onIntent(DuelSetupIntent.Back) },
                    )
                    Text(stringResource(Res.string.duel_setup_eyebrow), style = LexiTheme.typography.label13)
                }
                Column(verticalArrangement = Arrangement.spacedBy(dimens.gap4)) {
                    Text(
                        stringResource(Res.string.duel_setup_title),
                        style = LexiTheme.typography.display,
                        modifier = Modifier.lexiHeading(),
                    )
                    Text(stringResource(Res.string.duel_setup_subtitle), style = LexiTheme.typography.body15)
                }
                Column(verticalArrangement = Arrangement.spacedBy(dimens.gap10)) {
                    repeat(PLAYERS) { index ->
                        PlayerCard(
                            index = index,
                            name = state.names.getOrElse(index) { "" },
                            onChange = { onIntent(DuelSetupIntent.EditName(index)) },
                        )
                    }
                }
                Section(stringResource(Res.string.duel_level_label)) {
                    SegmentedControl(
                        segments = Level.entries.map { Segment(it.name) },
                        selectedIndex = Level.entries.indexOf(state.level),
                        onSelect = { onIntent(DuelSetupIntent.SelectLevel(Level.entries[it])) },
                        dividerWidth = LexiTheme.borders.regular,
                        titleStyle = LexiTheme.typography.bodyStrong16,
                        scrollable = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Section(stringResource(Res.string.duel_time_label)) {
                    SegmentedControl(
                        segments = state.clockOptionsSeconds.map { Segment(clockOptionLabel(it)) },
                        selectedIndex = state.clockOptionsSeconds.indexOf(state.clockSeconds),
                        onSelect = { onIntent(DuelSetupIntent.SelectClock(state.clockOptionsSeconds[it])) },
                        dividerWidth = LexiTheme.borders.regular,
                        titleStyle = LexiTheme.typography.bodyStrong16,
                        scrollable = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.gap10)) {
                    DecorShape(
                        kind = DecorShapeKind.Leaf,
                        color = colors.yellow,
                        border = BorderStroke(LexiTheme.borders.thin, colors.ink),
                        modifier = Modifier.padding(
                            top = DuelDefaults.RulesMarkerTop,
                        ).size(DuelDefaults.RulesMarkerSize),
                    )
                    Text(
                        stringResource(Res.string.duel_rules_note),
                        style = LexiTheme.typography.body14,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            LexiButton(
                text = stringResource(Res.string.duel_start),
                onClick = { onIntent(DuelSetupIntent.Start) },
                textStyle = LexiTheme.typography.button28,
                height = DuelDefaults.SetupStartHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = dimens.screenPaddingHorizontal,
                        top = dimens.gap16,
                        end = dimens.screenPaddingHorizontal,
                        bottom = dimens.screenPaddingBottom,
                    ),
            )
        }
        val editing = state.editingPlayer
        if (editing != null) {
            NameDialog(state = state, onIntent = onIntent)
        }
    }
}

private const val PLAYERS = 2

@Composable
private fun Section(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
        Text(label, style = LexiTheme.typography.label13, modifier = Modifier.lexiHeading())
        content()
    }
}

/** A player card: red square (player 1) / blue circle (player 2), "1. OYUNCU", the name and the "Değiştir" link. */
@Composable
private fun PlayerCard(index: Int, name: String, onChange: () -> Unit) {
    val colors = LexiTheme.colors
    val shown = duelPlayerName(name, index)
    val changeDescription = stringResource(Res.string.duel_player_change_description, shown)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink), LexiTheme.shadows.s4)
            .padding(
                horizontal = DuelDefaults.PlayerCardPaddingHorizontal,
                vertical = DuelDefaults.PlayerCardPaddingVertical,
            ),
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DecorShape(
            kind = if (index == 0) DecorShapeKind.Square else DecorShapeKind.Circle,
            color = if (index == 0) colors.red else colors.blue,
            border = BorderStroke(LexiTheme.borders.thin, colors.ink),
            modifier = Modifier.size(DuelDefaults.PlayerMarkerSize),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap2)) {
            Text(stringResource(Res.string.duel_player_label, index + 1), style = LexiTheme.typography.label12)
            Text(
                shown,
                style = LexiTheme.typography.heading22,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.lexiHeading(),
            )
        }
        PressableSurface(
            onClick = onChange,
            modifier = Modifier
                .heightIn(min = LexiTheme.dimens.minTouchTarget)
                // The link's padding widens the touch area to the right; the text stays at the card's padding.
                .offset(x = LexiTheme.dimens.gap8)
                .semantics { contentDescription = changeDescription },
            color = Color.Transparent,
            border = null,
            shadow = null,
        ) {
            Text(
                stringResource(Res.string.duel_player_change),
                style = LexiTheme.typography.caption13SemiBold,
                color = colors.inkMuted,
                modifier = Modifier.padding(horizontal = LexiTheme.dimens.gap8),
            )
        }
    }
}

/**
 * DESIGN-DEBT (no design, `screen-map.md`): the "Oyuncu adı" dialog from `16-components`' dialog panel look (white, 3dp,
 * s8, padding 18, gap 12, heading title, 48dp buttons) with the search field's input (white, 3dp, 48dp, 15sp, muted
 * hint). Focus goes to the field on open; the keyboard's Done saves; the scrim cancels.
 */
@Composable
private fun NameDialog(state: DuelSetupUiState, onIntent: (DuelSetupIntent) -> Unit) {
    val colors = LexiTheme.colors
    val title = stringResource(Res.string.duel_name_title)
    val hint = stringResource(Res.string.duel_name_hint)
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Box(Modifier.fillMaxSize()) {
        ModalScrim(onDismissRequest = { onIntent(DuelSetupIntent.CancelName) })
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .lexiContentWidth()
                .padding(horizontal = LexiTheme.dimens.screenPaddingHorizontal)
                .semantics {
                    paneTitle = title
                    isTraversalGroup = true
                }
                .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink), LexiTheme.shadows.s8)
                // Like the dialog panel: scroll outside the padding (the hard shadow lies in it), tall at large fonts.
                .verticalScroll(rememberScrollState())
                .padding(FeedbackDefaults.DialogPadding),
            verticalArrangement = Arrangement.spacedBy(FeedbackDefaults.DialogGap),
        ) {
            Text(title, style = LexiTheme.typography.heading22, modifier = Modifier.lexiHeading())
            val style = LexiTheme.typography.body15.copy(color = colors.ink)
            BasicTextField(
                value = state.nameDraft,
                onValueChange = { onIntent(DuelSetupIntent.NameChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SearchFieldDefaults.Height)
                    .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
                    .padding(SearchFieldDefaults.Padding)
                    .focusRequester(focus)
                    .semantics { contentDescription = hint },
                textStyle = style,
                singleLine = true,
                cursorBrush = SolidColor(colors.ink),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onIntent(DuelSetupIntent.SaveName) }),
                decorationBox = { inner ->
                    Box(Modifier.heightIn(min = SearchFieldDefaults.Height), contentAlignment = Alignment.CenterStart) {
                        if (state.nameDraft.isEmpty()) {
                            Text(hint, style = style, color = colors.inkMuted, maxLines = 1)
                        }
                        inner()
                    }
                },
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FeedbackDefaults.DialogButtonGap),
            ) {
                DialogButton(
                    stringResource(Res.string.duel_name_cancel),
                    { onIntent(DuelSetupIntent.CancelName) },
                    primary = false,
                    modifier = Modifier.weight(1f),
                )
                DialogButton(
                    stringResource(Res.string.duel_name_save),
                    { onIntent(DuelSetupIntent.SaveName) },
                    primary = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** "5 dk" / "5 min" for whole minutes (every offered clock); any other value falls back to "m:ss". */
@Composable
private fun clockOptionLabel(seconds: Int): String = if (seconds % SECONDS_PER_MINUTE == 0) {
    stringResource(Res.string.duel_clock_option_minutes, seconds / SECONDS_PER_MINUTE)
} else {
    formatClock(seconds)
}

private const val SECONDS_PER_MINUTE = 60

// design sample, not user-facing
internal val DuelSetupSample = DuelSetupUiState(
    isLoaded = true,
    level = Level.A1,
    clockSeconds = 300,
    clockOptionsSeconds = persistentListOf(180, 300, 420),
)

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun DuelSetupScreenPreview() {
    LexiTheme { DuelSetupScreen(state = DuelSetupSample, onIntent = {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun DuelSetupNameDialogPreview() {
    LexiTheme {
        DuelSetupScreen(state = DuelSetupSample.copy(editingPlayer = 0, nameDraft = "Ayşe"), onIntent = {})
    }
}
