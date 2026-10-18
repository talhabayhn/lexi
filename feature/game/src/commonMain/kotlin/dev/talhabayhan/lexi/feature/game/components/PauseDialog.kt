package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.talhabayhan.lexi.core.designsystem.component.ConfirmDialog
import dev.talhabayhan.lexi.core.designsystem.component.DialogButton
import dev.talhabayhan.lexi.core.designsystem.component.DialogPanel
import dev.talhabayhan.lexi.core.designsystem.component.FeedbackDefaults
import dev.talhabayhan.lexi.core.designsystem.component.ModalScrim
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.lexiContentWidth
import dev.talhabayhan.lexi.feature.game.GameIntent
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_message
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_quit
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_restart
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_resume
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_settings
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_title
import dev.talhabayhan.lexi.feature.game.generated.resources.game_quit_cancel
import dev.talhabayhan.lexi.feature.game.generated.resources.game_quit_confirm
import dev.talhabayhan.lexi.feature.game.generated.resources.game_quit_message
import dev.talhabayhan.lexi.feature.game.generated.resources.game_quit_title
import dev.talhabayhan.lexi.feature.game.generated.resources.game_restart_cancel
import dev.talhabayhan.lexi.feature.game.generated.resources.game_restart_confirm
import dev.talhabayhan.lexi.feature.game.generated.resources.game_restart_message
import dev.talhabayhan.lexi.feature.game.generated.resources.game_restart_title
import org.jetbrains.compose.resources.stringResource

/**
 * DESIGN-DEBT (`screen-map.md`: the pause menu has no design yet; needs design): built only from `16-components`, the
 * dialog panel over the 55 % [ModalScrim]. First step "Oyun duraklatıldı" with a vertical stack of 48dp buttons (gap 10):
 * DEVAM ET (primary: resume), YENİDEN BAŞLAT (only when [canRestart]), AYARLAR (settings over the paused game), PES ET.
 * Restarting and giving up can't be undone, so each asks first: "Yeniden başlatmak istiyor musun?" (Başlat /
 * Vazgeç) and "Pes etmek istiyor musun?" (Pes et: forfeit, recorded like every finished game / Vazgeç). The scrim does
 * not dismiss: the clock stays stopped until the player taps DEVAM ET.
 */
@Composable
internal fun PauseDialog(
    quitConfirmation: Boolean,
    restartConfirmation: Boolean,
    canRestart: Boolean,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        ModalScrim(onDismissRequest = {})
        val dialogModifier = Modifier
            .align(Alignment.Center)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .lexiContentWidth()
            .padding(horizontal = LexiTheme.dimens.screenPaddingHorizontal)
        when {
            quitConfirmation -> ConfirmDialog(
                title = stringResource(Res.string.game_quit_title),
                message = stringResource(Res.string.game_quit_message),
                confirmLabel = stringResource(Res.string.game_quit_confirm),
                dismissLabel = stringResource(Res.string.game_quit_cancel),
                onConfirm = { onIntent(GameIntent.Forfeit) },
                onDismiss = { onIntent(GameIntent.CancelQuit) },
                modifier = dialogModifier,
            )

            restartConfirmation -> ConfirmDialog(
                title = stringResource(Res.string.game_restart_title),
                message = stringResource(Res.string.game_restart_message),
                confirmLabel = stringResource(Res.string.game_restart_confirm),
                dismissLabel = stringResource(Res.string.game_restart_cancel),
                onConfirm = { onIntent(GameIntent.Restart) },
                onDismiss = { onIntent(GameIntent.CancelRestart) },
                modifier = dialogModifier,
            )

            else -> DialogPanel(
                title = stringResource(Res.string.game_pause_title),
                message = stringResource(Res.string.game_pause_message),
                modifier = dialogModifier,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(FeedbackDefaults.DialogButtonGap)) {
                    val fill = Modifier.fillMaxWidth()
                    DialogButton(
                        text = stringResource(Res.string.game_pause_resume),
                        onClick = { onIntent(GameIntent.Resume) },
                        primary = true,
                        modifier = fill,
                    )
                    if (canRestart) {
                        DialogButton(
                            text = stringResource(Res.string.game_pause_restart),
                            onClick = { onIntent(GameIntent.RequestRestart) },
                            primary = false,
                            modifier = fill,
                        )
                    }
                    DialogButton(
                        text = stringResource(Res.string.game_pause_settings),
                        onClick = { onIntent(GameIntent.OpenSettings) },
                        primary = false,
                        modifier = fill,
                    )
                    DialogButton(
                        text = stringResource(Res.string.game_pause_quit),
                        onClick = { onIntent(GameIntent.RequestQuit) },
                        primary = false,
                        modifier = fill,
                    )
                }
            }
        }
    }
}
