package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.talhabayhan.lexi.core.designsystem.component.ConfirmDialog
import dev.talhabayhan.lexi.core.designsystem.component.ModalScrim
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.GameIntent
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_message
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_quit
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_resume
import dev.talhabayhan.lexi.feature.game.generated.resources.game_pause_title
import dev.talhabayhan.lexi.feature.game.generated.resources.game_quit_cancel
import dev.talhabayhan.lexi.feature.game.generated.resources.game_quit_confirm
import dev.talhabayhan.lexi.feature.game.generated.resources.game_quit_message
import dev.talhabayhan.lexi.feature.game.generated.resources.game_quit_title
import org.jetbrains.compose.resources.stringResource

/**
 * DESIGN-DEBT (`screen-map.md`: the pause menu has no design yet; needs design): built only from `16-components`, a
 * [ConfirmDialog] over the 55 % [ModalScrim]. Two steps: "Oyun duraklatıldı" with Devam et (resume) / Bırak, then,
 * because giving up can't be undone, "Çarkı bırakmak istiyor musun?" with Bırak (forfeit, recorded like every
 * finished game) / Vazgeç (back to the first step). "Yeniden başla" and "Ayarlar" from the screen-map hint wait for
 * the design (settings: M7). The scrim does not dismiss: the clock stays stopped until the player taps Devam et.
 */
@Composable
internal fun PauseDialog(quitConfirmation: Boolean, onIntent: (GameIntent) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        ModalScrim(onDismissRequest = {})
        val dialogModifier = Modifier
            .align(Alignment.Center)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = LexiTheme.dimens.screenPaddingHorizontal)
        if (quitConfirmation) {
            ConfirmDialog(
                title = stringResource(Res.string.game_quit_title),
                message = stringResource(Res.string.game_quit_message),
                confirmLabel = stringResource(Res.string.game_quit_confirm),
                dismissLabel = stringResource(Res.string.game_quit_cancel),
                onConfirm = { onIntent(GameIntent.Forfeit) },
                onDismiss = { onIntent(GameIntent.CancelQuit) },
                modifier = dialogModifier,
            )
        } else {
            ConfirmDialog(
                title = stringResource(Res.string.game_pause_title),
                message = stringResource(Res.string.game_pause_message),
                confirmLabel = stringResource(Res.string.game_pause_resume),
                dismissLabel = stringResource(Res.string.game_pause_quit),
                onConfirm = { onIntent(GameIntent.Resume) },
                onDismiss = { onIntent(GameIntent.RequestQuit) },
                modifier = dialogModifier,
            )
        }
    }
}
