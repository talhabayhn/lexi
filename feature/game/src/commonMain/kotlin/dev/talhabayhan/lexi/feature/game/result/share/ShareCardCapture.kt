package dev.talhabayhan.lexi.feature.game.result.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.talhabayhan.lexi.core.designsystem.capture.OffscreenCapture
import dev.talhabayhan.lexi.core.designsystem.component.ShareCardDefaults
import dev.talhabayhan.lexi.core.domain.share.ShareCard
import dev.talhabayhan.lexi.feature.game.result.ResultIntent

/**
 * Renders [card] off screen to a 1080 × 1350 PNG (ADR 0020) and sends [ResultIntent.SharePng] with the share text, or
 * [ResultIntent.ShareCaptureFailed]. Composed only while a share is running (`ResultUiState.isSharing`).
 */
@Composable
internal fun ShareCardCapture(card: ShareCard, onIntent: (ResultIntent) -> Unit, modifier: Modifier = Modifier) {
    val text = shareText(card)
    OffscreenCapture(
        size = ShareCardDefaults.Size,
        scale = ShareCardDefaults.CAPTURE_SCALE,
        onCaptured = { png ->
            onIntent(if (png != null) ResultIntent.SharePng(png, text) else ResultIntent.ShareCaptureFailed)
        },
        modifier = modifier,
    ) {
        ShareCard(card)
    }
}
