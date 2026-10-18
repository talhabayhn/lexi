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
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.rate_body
import dev.talhabayhan.lexi.feature.game.generated.resources.rate_later
import dev.talhabayhan.lexi.feature.game.generated.resources.rate_title
import dev.talhabayhan.lexi.feature.game.generated.resources.rate_yes
import org.jetbrains.compose.resources.stringResource

/**
 * Rating prompt dialog (`COPY.md` §8, M8.4): ConfirmDialog-based prompt asking
 * "Enjoying Lexi?" / "Lexi hoşuna gidiyor mu?".
 */
@Composable
internal fun RatingPromptDialog(onRate: () -> Unit, onLater: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        ModalScrim(onDismissRequest = onLater)
        ConfirmDialog(
            title = stringResource(Res.string.rate_title),
            message = stringResource(Res.string.rate_body),
            confirmLabel = stringResource(Res.string.rate_yes),
            dismissLabel = stringResource(Res.string.rate_later),
            onConfirm = onRate,
            onDismiss = onLater,
            modifier = Modifier
                .align(Alignment.Center)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = LexiTheme.dimens.screenPaddingHorizontal),
        )
    }
}
