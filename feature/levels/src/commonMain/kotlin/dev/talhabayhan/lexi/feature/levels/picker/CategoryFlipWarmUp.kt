package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.intl.Locale
import dev.talhabayhan.lexi.core.designsystem.component.CategoryArt
import dev.talhabayhan.lexi.core.designsystem.component.CategoryArtKind
import dev.talhabayhan.lexi.core.designsystem.component.FlowDefaults
import dev.talhabayhan.lexi.core.designsystem.component.PickerCard
import dev.talhabayhan.lexi.core.designsystem.modifier.fadeWithShadowRoom
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipChoreography
import dev.talhabayhan.lexi.core.designsystem.motion.DialogFlipMachine
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.feature.levels.generated.resources.Res
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_mixed_fallback
import dev.talhabayhan.lexi.feature.levels.generated.resources.picker_progress
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

/**
 * Warm-up of "Çarkını seç" (#13 / #14 / #18), drawn by the splash's hidden warm-up on the first launch of each version
 * (`SplashRoute` in `:shared`), i.e. long before the first tap on a card. The real [PickerCard] (fill, border, s4
 * shadow, progress bar, art, texts) is posed mid-turn with the flip's own layer ([flipCard]: lift, tilt, Y turn with
 * perspective, the grown hard shadow), at rest, and mid-entrance (#18: faded with room for its shadow), and
 * the real dialog panel mid-open ([PickerDialogWarmUp]). Their GPU pipelines are built (on iOS after an install Metal
 * compiles each one the first time it is drawn, `docs/PERF.md`) and their code is run once, so the first picker and
 * the first flip of a version do not pay for either. No semantics, no input.
 */
@Composable
public fun CategoryFlipWarmUp(modifier: Modifier = Modifier) {
    val motion = LexiTheme.motion
    val name = stringResource(Res.string.picker_mixed_fallback)
    val card = remember(name) {
        PickerCategoryUi(id = CategoryId.MIXED, nameTr = name, nameEn = name, learned = 1, total = 2)
    }
    val state = remember(card) { CategoryPickerUiState(categories = persistentListOf(card), isLoaded = true) }
    val flip = remember(motion) {
        PickerDialogFlip(
            DialogFlipMachine(DialogFlipChoreography(motion.flow, motion.mechanical, motion.turn, motion.pop)),
        ).also {
            it.card = card
            it.request(open = true, tier = MotionTier.Full)
            // Lifted and half-way through the turn away: every layer of the moving card is drawn.
            it.advanceBy(
                motion.flow.cardTurnAway.delayMillis + motion.flow.cardTurnAway.durationMillis / 2L,
            )
        }
    }
    val progressText = stringResource(Res.string.picker_progress, card.learned, card.total)
    val shadowRoom = LexiTheme.shadows.fadeOutsets
    Box(modifier.clearAndSetSemantics {}) {
        // The grid's cards: at rest, and mid-entrance with the same fade as #18.
        Row {
            WarmUpCard(name, progressText, card.fraction, selected = false, Modifier)
            WarmUpCard(
                name,
                progressText,
                card.fraction,
                selected = false,
                Modifier.fadeWithShadowRoom(shadowRoom) { WARM_UP_ENTRANCE_ALPHA },
            )
        }
        WarmUpCard(
            name,
            progressText,
            card.fraction,
            selected = true,
            Modifier.flipCard(flip, motion.flow, LexiTheme.shadows.s4.color),
        )
        PickerDialogWarmUp(card, state, turkish = Locale.current.language == "tr")
    }
}

@Composable
private fun WarmUpCard(name: String, progressText: String, progress: Float, selected: Boolean, modifier: Modifier) {
    PickerCard(
        name = name,
        contentDescription = name,
        art = { artModifier -> CategoryArt(CategoryArtKind.Mixed, artModifier) },
        progressText = progressText,
        progress = progress,
        selected = selected,
        modifier = Modifier.width(FlowDefaults.WarmUpCardWidth).then(modifier),
    )
}

/** A card half-way through the grid entrance's fade (#18): drawn offscreen, like every entering card. */
private const val WARM_UP_ENTRANCE_ALPHA = 0.5f
