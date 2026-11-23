package dev.talhabayhan.lexi.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.CategoryArt
import dev.talhabayhan.lexi.core.designsystem.component.CategoryArtKind
import dev.talhabayhan.lexi.core.designsystem.motion.LetterMorphFrame
import dev.talhabayhan.lexi.core.designsystem.motion.LetterMorphLayer
import dev.talhabayhan.lexi.core.designsystem.motion.perspective
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.levels.picker.CategoryFlipWarmUp

/**
 * Warm-up of the video flow's drawing (next to [dev.talhabayhan.lexi.feature.game.GameWarmUp], drawn by the splash's
 * hidden warm-up on the first launch of each version, `SplashRoute`): a category card's art and text turned on Y with
 * the flip's perspective, and the letter morph's ink with its big letter scaled. Their GPU pipelines are built before
 * the first flip or morph (on the emulator the scaled-text shader alone took ≈ 130 ms; on iOS after an install Metal
 * builds every pipeline the first time it is drawn, `docs/PERF.md`). The real picker cards (mid-flip, at rest,
 * entering) and the dialog mid-open ([CategoryFlipWarmUp]) are drawn too. Common code, so it runs on Android and iOS
 * alike. No semantics, no input.
 */
@Composable
internal fun FlowWarmUp(modifier: Modifier = Modifier) {
    val flow = LexiTheme.motion.flow
    Box(modifier.clearAndSetSemantics {}) {
        Column(
            Modifier
                .size(WARM_UP_CARD)
                .graphicsLayer {
                    rotationY = WARM_UP_TURN_DEGREES
                    perspective(flow.flipPerspective)
                },
        ) {
            CategoryArt(CategoryArtKind.Mixed, Modifier.size(WARM_UP_CARD))
            Text(WARM_UP_LETTER, style = LexiTheme.typography.bodyStrong14)
        }
        // The real card mid-flip and the dialog mid-open (the art-only card above misses their own layers).
        CategoryFlipWarmUp(Modifier.matchParentSize())
        val ink = LexiTheme.colors.ink
        LetterMorphLayer(WARM_UP_LETTER) { screen ->
            LetterMorphFrame(
                visible = true,
                inkRect = Rect(screen.center, screen.width / 2f),
                inkColor = ink,
                letterScale = WARM_UP_LETTER_SCALE,
                letterAlpha = 1f,
            )
        }
        // The pop's end: the big letter near its real size (above Skia's ≈ 256 px glyph atlas limit even at the
        // warm-up's half scale, so it is drawn as a path, another pipeline than the small letter) and fading in.
        LetterMorphLayer(WARM_UP_LETTER) { screen ->
            LetterMorphFrame(
                visible = true,
                inkRect = Rect(screen.center, screen.width / 2f),
                inkColor = ink,
                letterScale = 1f,
                letterAlpha = WARM_UP_LETTER_FADE,
            )
        }
    }
}

private const val WARM_UP_LETTER = "A"
private const val WARM_UP_TURN_DEGREES = 30f
private const val WARM_UP_LETTER_SCALE = 0.3f
private const val WARM_UP_LETTER_FADE = 0.5f
private val WARM_UP_CARD = 160.dp
