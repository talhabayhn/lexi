package dev.talhabayhan.lexi.feature.game.components

import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.feature.game.GameEffect

/**
 * Haptic and sound of a game effect (`motion.md` catalogue), played by [GameMotion] when the effect's animation starts.
 * The settings gates ("Titreşim", "Ses efektleri") sit in the injected `Haptics` / `SoundPlayer`; reduced motion does
 * not silence anything. TTS is not here: the ViewModel speaks through `Speaker` (setting "Doğru cevapta telaffuz").
 *
 * A letter key, PAS and the joker rows are buttons: their press already plays #1 (light haptic + "tık") through
 * `PressableSurface`, which is also the light haptic of #2, so [GameEffect.LetterPlaced] adds nothing. The effects a
 * press causes (#3/#4 after the last letter, #5 after PAS) follow right after it.
 */
internal data class GameFeedback(val haptic: HapticKind?, val sound: SoundEffect?) {
    companion object {
        /** The feedback of [effect], or null when it has none (#2, #6, navigation). */
        fun of(effect: GameEffect): GameFeedback? = when (effect) {
            // #3: success + two rising notes (the word is spoken after them by the ViewModel).
            is GameEffect.Correct -> GameFeedback(HapticKind.Success, SoundEffect.Correct)

            // #4: error + descending "bzzt".
            is GameEffect.Wrong -> GameFeedback(HapticKind.Error, SoundEffect.Wrong)

            // #5: soft + "vınn".
            is GameEffect.Passed -> GameFeedback(HapticKind.Soft, SoundEffect.Pass)

            // #7: heavy + short fanfare.
            is GameEffect.ComboReached -> GameFeedback(HapticKind.Heavy, SoundEffect.Combo)

            // #8: tick + "tik" each second of the last ten.
            is GameEffect.TimeWarning -> GameFeedback(HapticKind.Tick, SoundEffect.Tick)

            is GameEffect.LetterPlaced,
            is GameEffect.Advanced,
            is GameEffect.NavigateToResult,
            GameEffect.Abandoned,
            GameEffect.NavigateBack,
            GameEffect.NavigateToSettings,
            -> null
        }
    }
}
