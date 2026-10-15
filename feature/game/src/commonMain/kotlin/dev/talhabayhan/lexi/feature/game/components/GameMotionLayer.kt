package dev.talhabayhan.lexi.feature.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Constraints
import dev.talhabayhan.lexi.core.designsystem.component.ComboBadge
import dev.talhabayhan.lexi.core.designsystem.component.ComboBurst
import dev.talhabayhan.lexi.core.designsystem.component.GameHudDefaults
import dev.talhabayhan.lexi.core.designsystem.modifier.lexiSurface
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.game_combo_badge
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Overlay of the game screen for motion that crosses the layout (`motion.md` #2 letters in the air, #7 combo over the
 * wheel). Fills the screen, takes no touches; positions come from [GameMotion] and are read in the layout phase.
 */
@Composable
internal fun GameMotionLayer(motion: GameMotion, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().onPlaced(motion::onOverlayPlaced)) {
        motion.flights.forEach { flight -> key(flight) { FlyingLetter(flight) } }
        motion.combo?.let { combo -> key(combo) { Combo(combo, motion) } }
    }
}

/** #2: a copy of the key (white, 3dp border, s3, key letter) between the key and the cell. Decorative. */
@Composable
private fun FlyingLetter(flight: LetterFlight) {
    val colors = LexiTheme.colors
    Box(
        modifier = Modifier
            .clearAndSetSemantics {}
            .layout { measurable, _ ->
                val bounds = flight.bounds
                val placeable = measurable.measure(
                    Constraints.fixed(bounds.width.roundToInt(), bounds.height.roundToInt()),
                )
                layout(placeable.width, placeable.height) {
                    placeable.place(bounds.left.roundToInt(), bounds.top.roundToInt())
                }
            }
            .graphicsLayer {
                scaleX = flight.scale
                scaleY = flight.scale
            }
            .lexiSurface(colors.white, BorderStroke(LexiTheme.borders.regular, colors.ink), LexiTheme.shadows.s3),
        contentAlignment = Alignment.Center,
    ) {
        Text(flight.letter.toString(), style = LexiTheme.typography.key)
    }
}

/**
 * #7 in the HUD row, vertically centred on it and right-aligned next to the score circle (over the empty end of the
 * timer bar), so it stays off the centre letter and the wheel chips (the strip under the HUD is too thin for it).
 * DESIGN-DEBT: `15-motion` shows badge and burst alone; their place in the game has no design (`screen-map.md`).
 */
@Composable
private fun Combo(combo: ComboAnimation, motion: GameMotion) {
    val scoreClearance = GameHudDefaults.BarHeight + LexiTheme.dimens.gap8
    Box(
        modifier = Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            val hud = motion.hudBounds()
            layout(placeable.width, placeable.height) {
                if (hud != null) {
                    placeable.place(
                        (hud.right - scoreClearance.toPx() - placeable.width).roundToInt(),
                        (hud.center.y - placeable.height / 2f).roundToInt(),
                    )
                }
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        if (combo.bursts) ComboBurst(progress = { combo.burstProgress })
        ComboBadge(
            text = stringResource(Res.string.game_combo_badge, combo.streak),
            modifier = Modifier.graphicsLayer {
                scaleX = combo.badgeScale
                scaleY = combo.badgeScale
                rotationZ = combo.badgeRotation
            },
        )
    }
}
