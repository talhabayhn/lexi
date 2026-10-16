package dev.talhabayhan.lexi.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.CountChip
import dev.talhabayhan.lexi.core.designsystem.component.JokerRowDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ListCard
import dev.talhabayhan.lexi.core.designsystem.component.ListRow
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.ProBanner
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.AppInfo
import dev.talhabayhan.lexi.feature.settings.generated.resources.Res
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_accent
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_back
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_classic_time
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_clue_language
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_daily_goal
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_daily_reminder
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_footer
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_haptics
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_language
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_pro_subtitle
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_pro_title
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_review_reminders
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_section_game
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_section_language
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_section_learning
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_soon
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_sound
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_speak_on_correct
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * `11-settings`: back button and title, the Lexi Pro card, then the OYUN, ÖĞRENME and DİL cards. The sound, haptics and
 * speak-on-correct switches work; every other row (Pro, reminders, goal, languages, accent, classic time) is shown
 * faded with a "YAKINDA" chip until M7. The page scrolls on small screens; the footer links wait for M7 too.
 */
@Composable
internal fun SettingsScreen(state: SettingsUiState, onIntent: (SettingsIntent) -> Unit, modifier: Modifier = Modifier) {
    val dimens = LexiTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(
                start = dimens.screenPaddingHorizontal,
                top = dimens.screenPaddingTop,
                end = dimens.screenPaddingHorizontal,
                bottom = dimens.screenPaddingBottomList,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.gap14),
    ) {
        Header(onBack = { onIntent(SettingsIntent.Back) })
        // DESIGN-DEBT: the price needs the store (M7); the card shows the "soon" chip in its place.
        ProBanner(
            title = stringResource(Res.string.settings_pro_title),
            subtitle = stringResource(Res.string.settings_pro_subtitle),
        ) {
            CountChip(stringResource(Res.string.settings_soon))
        }
        Section(Res.string.settings_section_game) {
            ListRow(
                label = stringResource(Res.string.settings_sound),
                checked = state.soundEffects,
                onCheckedChange = { onIntent(SettingsIntent.SetSoundEffects(it)) },
            )
            ListRow(
                label = stringResource(Res.string.settings_haptics),
                checked = state.haptics,
                onCheckedChange = { onIntent(SettingsIntent.SetHaptics(it)) },
            )
            ListRow(
                label = stringResource(Res.string.settings_speak_on_correct),
                checked = state.speakOnCorrect,
                onCheckedChange = { onIntent(SettingsIntent.SetSpeakOnCorrect(it)) },
            )
            SoonRow(Res.string.settings_classic_time)
        }
        Section(Res.string.settings_section_learning) {
            SoonRow(Res.string.settings_daily_reminder)
            SoonRow(Res.string.settings_daily_goal)
            SoonRow(Res.string.settings_review_reminders)
        }
        Section(Res.string.settings_section_language) {
            SoonRow(Res.string.settings_language)
            SoonRow(Res.string.settings_clue_language)
            SoonRow(Res.string.settings_accent)
        }
        Text(
            text = stringResource(Res.string.settings_footer, AppInfo.NAME),
            style = LexiTheme.typography.caption12,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavSquareButton(
            icon = LexiIcons.Back,
            contentDescription = stringResource(Res.string.settings_back),
            onClick = onBack,
        )
        Text(stringResource(Res.string.settings_title), style = LexiTheme.typography.title)
    }
}

@Composable
private fun Section(title: StringResource, rows: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6)) {
        Text(stringResource(title), style = LexiTheme.typography.label12)
        ListCard(Modifier.fillMaxWidth(), content = rows)
    }
}

/** A row that arrives in M7: faded label (the disabled look of the game inputs), full-strength "YAKINDA" chip. */
@Composable
private fun SoonRow(label: StringResource) {
    ListRow(modifier = Modifier.semantics { disabled() }) {
        Text(
            text = stringResource(label),
            style = LexiTheme.typography.body16SemiBold,
            modifier = Modifier.weight(1f).alpha(JokerRowDefaults.DISABLED_ALPHA),
        )
        CountChip(stringResource(Res.string.settings_soon))
    }
}

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun SettingsScreenPreview() {
    LexiTheme(reducedMotion = false) { SettingsScreen(state = SettingsUiState(), onIntent = {}) }
}
