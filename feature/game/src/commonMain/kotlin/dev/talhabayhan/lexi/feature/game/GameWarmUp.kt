package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics

/**
 * One-time warm-up of the game board: the playing sample ([GameSamples.design]) composed, measured and drawn like the
 * real game screen, so the first-use costs (Kotlin/Native class init, the Jost glyph cache at chip and key sizes,
 * shaders, strings) are paid on Home instead of in the game's first frame. The host draws it for two frames under an
 * opaque background. It has no semantics, takes no touch (every pointer event is consumed before its keys see it) and
 * no focus; its intents go nowhere.
 */
@Composable
fun GameWarmUp(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clearAndSetSemantics {}
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                    }
                }
            }
            .focusProperties {
                canFocus = false
                onEnter = { cancelFocusChange() }
            }
            .focusGroup(),
    ) {
        GameScreen(state = GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
    }
}
