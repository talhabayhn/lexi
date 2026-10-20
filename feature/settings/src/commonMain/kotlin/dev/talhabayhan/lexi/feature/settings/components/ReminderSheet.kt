package dev.talhabayhan.lexi.feature.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.BottomSheet
import dev.talhabayhan.lexi.core.designsystem.component.JokerRowDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.ListCard
import dev.talhabayhan.lexi.core.designsystem.component.ListDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ListRow
import dev.talhabayhan.lexi.core.designsystem.component.OverlayDefaults
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalHaptics
import dev.talhabayhan.lexi.core.designsystem.feedback.LocalSoundPlayer
import dev.talhabayhan.lexi.core.designsystem.format.formatTimeOfDay
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.reminder.ReminderTimes
import dev.talhabayhan.lexi.feature.settings.SettingsIntent
import dev.talhabayhan.lexi.feature.settings.generated.resources.Res
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_daily_reminder
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_reminder_close
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_reminder_denied_hint
import dev.talhabayhan.lexi.feature.settings.generated.resources.settings_reminder_open_settings
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

/**
 * DESIGN-DEBT (no design; built from `16-components` like the joker sheet): the daily reminder sheet (motion.md #10).
 * A switch row, then the time list ([ReminderTimes.optionsMinuteOfDay], scrolling, the selected time in view on open;
 * faded and not selectable while the reminder is off) and, when the OS blocks notifications, the hint with a button to
 * the system settings. Fills its parent; renders nothing while not [visible].
 */
@Composable
internal fun ReminderSheet(
    visible: Boolean,
    enabled: Boolean,
    minuteOfDay: Int,
    blocked: Boolean,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        BottomSheet(
            visible = visible,
            onDismissRequest = { onIntent(SettingsIntent.CloseReminderSheet) },
            title = stringResource(Res.string.settings_daily_reminder),
            closeContentDescription = stringResource(Res.string.settings_reminder_close),
        ) {
            ListCard(Modifier.fillMaxWidth()) {
                ListRow(
                    label = stringResource(Res.string.settings_daily_reminder),
                    checked = enabled,
                    onCheckedChange = { onIntent(SettingsIntent.SetReminderEnabled(it)) },
                )
            }
            if (blocked) {
                Text(
                    text = stringResource(Res.string.settings_reminder_denied_hint),
                    style = LexiTheme.typography.body14,
                )
                LexiButton(
                    text = stringResource(Res.string.settings_reminder_open_settings),
                    onClick = { onIntent(SettingsIntent.OpenNotificationSettings) },
                    variant = LexiButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            TimeList(
                selectedMinute = ReminderTimes.normalize(minuteOfDay),
                enabled = enabled,
                onSelect = { onIntent(SettingsIntent.SetReminderTime(it)) },
            )
        }
    }
}

/** The framed, scrolling list of reminder times; the selected row is ink with a yellow time and a check mark. */
@Composable
private fun TimeList(selectedMinute: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
    val colors = LexiTheme.colors
    val scroll = rememberScrollState()
    var selectedTop by remember { mutableIntStateOf(NOT_PLACED) }
    var rowHeight by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        // On open: the selected time, with the row before it peeking in. Instant, so reduced motion needs no branch.
        snapshotFlow { selectedTop }.first { it != NOT_PLACED }
        scroll.scrollTo((selectedTop - rowHeight).coerceAtLeast(0))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = OverlayDefaults.SheetListMaxHeight)
            .alpha(if (enabled) 1f else JokerRowDefaults.DISABLED_ALPHA)
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink))
            .verticalScroll(scroll)
            .selectableGroup(),
    ) {
        ReminderTimes.optionsMinuteOfDay.forEachIndexed { index, minute ->
            if (index > 0) {
                Box(Modifier.fillMaxWidth().height(LexiTheme.borders.thin).background(colors.ink))
            }
            val selected = minute == selectedMinute
            TimeRow(
                minute = minute,
                selected = selected,
                enabled = enabled,
                onClick = { onSelect(minute) },
                modifier = if (selected) {
                    Modifier.onPlaced {
                        rowHeight = it.size.height
                        selectedTop = it.positionInParent().y.toInt()
                    }
                } else {
                    Modifier
                },
            )
        }
    }
}

@Composable
private fun TimeRow(
    minute: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LexiTheme.colors
    val haptics = LocalHaptics.current
    val sounds = LocalSoundPlayer.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ListDefaults.RowMinHeight)
            .background(if (selected) colors.ink else Color.Transparent)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = null,
                indication = null,
            ) {
                haptics.perform(HapticKind.Light)
                sounds.play(SoundEffect.Tap)
                onClick()
            }
            .padding(ListDefaults.RowPadding),
        horizontalArrangement = Arrangement.spacedBy(ListDefaults.RowGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatTimeOfDay(minute),
            style = LexiTheme.typography.body16SemiBold,
            color = if (selected) colors.yellow else colors.ink,
            modifier = Modifier.weight(1f),
        )
        // Not only color: the chosen time also carries a check mark (and the radio state for screen readers).
        if (selected) {
            Icon(
                imageVector = LexiIcons.Check,
                contentDescription = null,
                tint = colors.yellow,
                modifier = Modifier.size(ListDefaults.ChevronSize),
            )
        }
    }
}

private const val NOT_PLACED = -1

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ReminderSheetPreview() {
    LexiTheme(reducedMotion = false) {
        ReminderSheet(visible = true, enabled = true, minuteOfDay = 20 * 60, blocked = false, onIntent = {})
    }
}
