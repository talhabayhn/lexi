package dev.talhabayhan.lexi.core.designsystem.capture

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt

/**
 * Renders [content] off screen at a fixed [scale] (pixels per dp, font scale 1) and hands the PNG to [onCaptured]
 * once, or null when nothing could be captured (ADR 0020). The content is laid out at [size] in its own density, so
 * the image is the same on every device (432 × 540dp at 2.5 → 1080 × 1350 px), whatever the screen density or the
 * user's font size.
 *
 * Never visible and never in the accessibility tree: the host takes no space (it reports 0 × 0 to its parent, the
 * content is placed at 0, 0 behind it) and its drawing is only recorded into a graphics layer that is not drawn.
 * Compose it only while a capture is wanted; it captures after the content's first draw and one more frame.
 */
@Composable
public fun OffscreenCapture(
    size: DpSize,
    scale: Float,
    onCaptured: (png: ByteArray?) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val layer = rememberGraphicsLayer()
    val density = remember(scale) { Density(scale, fontScale = 1f) }
    val widthPx = (size.width.value * scale).roundToInt()
    val heightPx = (size.height.value * scale).roundToInt()
    val latestOnCaptured by rememberUpdatedState(onCaptured)
    val drawn = remember { CompletableDeferred<Unit>() }
    Box(
        modifier
            .clearAndSetSemantics {}
            .layout { measurable, _ ->
                val placeable = measurable.measure(Constraints.fixed(widthPx, heightPx))
                layout(0, 0) { placeable.place(0, 0) }
            }
            .drawWithContent {
                layer.record { this@drawWithContent.drawContent() }
                drawn.complete(Unit)
            },
    ) {
        CompositionLocalProvider(LocalDensity provides density, content = content)
    }
    LaunchedEffect(layer) {
        // Capture once the content has been drawn (recorded) and one more frame has passed for late text.
        val ready = withTimeoutOrNull(DRAW_TIMEOUT_MS) {
            drawn.await()
            withFrameNanos { }
        }
        latestOnCaptured(if (ready != null) capturePng(layer) else null)
    }
}

/**
 * The PNG of what [layer] recorded, encoded off the main thread; null when it recorded nothing or the platform could
 * not read or encode it.
 */
public suspend fun capturePng(layer: GraphicsLayer): ByteArray? {
    if (layer.size == IntSize.Zero) return null
    val bitmap = try {
        layer.toImageBitmap()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        return null
    }
    if (bitmap.width == 0 || bitmap.height == 0) return null
    return withContext(Dispatchers.Default) {
        try {
            bitmap.encodePng()?.takeIf { it.isNotEmpty() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}

/** PNG bytes of this bitmap, or null when the platform encoder fails. */
internal expect fun ImageBitmap.encodePng(): ByteArray?

/** Longest wait for the first draw of the content; no capture after it. */
private const val DRAW_TIMEOUT_MS = 2_000L
