package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws [content] for warm-up frames, so its GPU pipelines are built (after an install Metal compiles each one the first
 * time it is drawn, `docs/PERF.md`), without it being seen: [content] is drawn **above** the screen, clipped to a box
 * inset [HiddenPreDrawInset] from the edges of [modifier]'s area, and covered by an opaque [cover] rectangle of that box.
 * [cover] must be the color the screen shows there in these frames (e.g. the cream around the splash's held logo, the
 * logo itself being drawn again above the cover), so the frame looks unchanged. Touches on it are consumed (it is only shown for a few frames); give [content] no semantics of
 * its own (`clearAndSetSemantics`, a test tag at most).
 *
 * Why not simply behind the screen's opaque background, as the warm-ups did up to 1.1.1: Skia's GPU backends (iOS Metal,
 * Android HWUI) turn an opaque draw that covers the whole render target into a clear and drop every draw recorded
 * before it in that frame, so a pre-draw behind a full-screen background never reaches the GPU and warms nothing. A
 * cover that does not span the whole target is an ordinary draw (Skia does no occlusion culling): the covered draws
 * still run. Place it **last**, above every full-screen draw of the frame.
 */
@Composable
public fun HiddenPreDraw(cover: Color, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .padding(HiddenPreDrawInset)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                }
            }
            .clipToBounds()
            .drawWithContent {
                drawContent()
                drawRect(cover)
            },
        content = content,
    )
}

/** Inset of [HiddenPreDraw]'s cover from the edges: enough that it never spans the whole render target. */
public val HiddenPreDrawInset: Dp = 1.dp

/**
 * Makes a warm-up copy of a real screen inert: no semantics, every pointer event consumed before its content sees it,
 * never focused. For screens composed only to be drawn hidden ([HiddenPreDraw]); their intents go nowhere.
 */
public fun Modifier.inertWarmUp(): Modifier = this
    .clearAndSetSemantics {}
    .pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
        }
    }
    .focusProperties {
        canFocus = false
        onEnter = { cancelFocusChange() }
    }
    .focusGroup()
