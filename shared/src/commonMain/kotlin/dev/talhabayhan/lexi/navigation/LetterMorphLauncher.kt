package dev.talhabayhan.lexi.navigation

import dev.talhabayhan.lexi.core.designsystem.motion.FlowMorphKeys
import dev.talhabayhan.lexi.core.designsystem.motion.MorphOverlayState
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier

/** What [LetterMorphLauncher] needs from the letter morph; [MorphOverlayState] in the app, a fake in tests. */
internal interface LetterMorph {
    /** Starts the morph from OYNA with [letter] as its big letter; false when it cannot (tier Minimal, one runs, OYNA not laid out). */
    fun start(tier: MotionTier, letter: String): Boolean

    fun cancel()

    suspend fun awaitCovered(): Boolean
}

/** What [LetterMorphLauncher] needs from the navigation ([LexiNavigator] and the NavController in the app). */
internal interface GameStartNavigation {
    fun canStartGame(): Boolean

    /** The current destination is the game (opened under the ink). */
    fun isOnGame(): Boolean

    fun popGame()
}

/**
 * The decisions around the video flow's OYNA (`motion.md` #16), kept apart from Compose so each rule is unit-tested:
 * - a start the navigator would reject starts no morph (no ink flash);
 * - a navigation that does not happen after all (the screen left RESUMED, the host went away) cancels the morph, so
 *   the ink never stays on screen;
 * - system back cancels it, and if the game was already opened under the ink, leaves that (unstarted) game again;
 * - the app going to the background ([onStop]) or the game failing to load cancels it and stays where it is.
 */
internal class LetterMorphLauncher(private val morph: LetterMorph, private val navigation: GameStartNavigation) {
    private var cancelledByBack = false

    /**
     * OYNA in [tier] with the wheel's first [letter]; [startGame] is [LexiNavigator.startGame] for the chosen game with the morph's cover wait and the
     * dropped callback. True when the game start was accepted (with or without a morph).
     */
    fun start(
        tier: MotionTier,
        letter: String,
        startGame: (awaitCover: (suspend () -> Boolean)?, onDropped: () -> Unit) -> Boolean,
    ): Boolean {
        if (!navigation.canStartGame()) return false
        cancelledByBack = false
        val morphing = morph.start(tier, letter)
        val awaitCover: (suspend () -> Boolean)? = if (morphing) morph::awaitCovered else null
        val accepted = startGame(awaitCover) { if (morphing) morph.cancel() }
        if (!accepted && morphing) morph.cancel()
        return accepted
    }

    /** System back while the morph runs. */
    fun onBack() {
        cancelledByBack = true
        morph.cancel()
    }

    /** The app went to the background (ON_STOP). */
    fun onStop() {
        morph.cancel()
    }

    /** The cancel fade is over. */
    fun onCancelled() {
        if (cancelledByBack && navigation.isOnGame()) navigation.popGame()
        cancelledByBack = false
    }
}

/** [LetterMorph] on the app's [MorphOverlayState]: from OYNA to the wheel marker, on the wheel's first letter. */
internal class OverlayLetterMorph(private val state: MorphOverlayState) : LetterMorph {
    override fun start(tier: MotionTier, letter: String): Boolean =
        state.start(FlowMorphKeys.PLAY_BUTTON, FlowMorphKeys.WHEEL_MARKER, letter, tier)

    override fun cancel() = state.cancel()

    override suspend fun awaitCovered(): Boolean = state.awaitCovered()
}
