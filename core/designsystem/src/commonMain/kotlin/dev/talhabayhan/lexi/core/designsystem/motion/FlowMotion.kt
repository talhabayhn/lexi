package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.motion.MotionStep.Companion.fromPrototype
import dev.talhabayhan.lexi.core.designsystem.motion.MotionStep.Companion.prototypeMillis

/**
 * Video-flow motion tokens, `docs/design/motion.md` #13–#17 (proposal on branch `redesign/video-flow`). Every step is
 * written with the prototype's own numbers (`docs/design/drafts/video-flow/flow.html`) and converted with
 * [MotionStep.fromPrototype] (÷ 0.86), so each value can be traced back to the prototype. Easings are the existing
 * [dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion] curves named in each comment. Press (#1), screen
 * transitions (#12) and the wheel jump (#6) keep their shipped tokens.
 */
@Immutable
public class FlowMotion internal constructor() {
    // #13 category card lift (`pop`): the card rises, tilts and its hard shadow grows before it flips.
    public val cardLift: MotionStep = fromPrototype(0, 120)
    public val cardLiftOffset: Dp = (-6).dp
    public val cardLiftRotationDegrees: Float = -2f
    public val cardRestShadow: Dp = 4.dp
    public val cardLiftShadow: Dp = 10.dp

    // #14 card → settings dialog. The card turns away on Y (`turn`, 0° → 90°) and is hidden; the dialog flips in from
    // the card rectangle (`mechanical`, −90° → 0°) while growing to its final rectangle (`mechanical`); its content
    // fades in (linear), then the rows rise in one after another (`mechanical`).
    public val cardTurnAway: MotionStep = fromPrototype(100, 160)
    public val scrimIn: MotionStep = fromPrototype(100, 220)
    public val scrimAlpha: Float = 0.55f
    public val dialogFlipIn: MotionStep = fromPrototype(250, 200)
    public val dialogGrow: MotionStep = fromPrototype(250, 300)
    public val dialogContentIn: MotionStep = fromPrototype(400, 140)
    public val dialogItemIn: MotionStep = fromPrototype(420, 200)
    public val dialogItemStaggerMillis: Int = prototypeMillis(35)
    public val dialogItemOffset: Dp = 10.dp
    public val flipDegrees: Float = 90f

    /** #13–#15 the Y flips are seen through the prototype's CSS `perspective(900px)`. */
    public val flipPerspective: Dp = 900.dp

    /** #14 the dialog takes input from here on. */
    public val dialogOpenSettledMillis: Int = prototypeMillis(700)

    // #15 dialog → card: content fades out (linear), the dialog collapses to the card rectangle (`turn`) and turns
    // away (`turn`, 0° → −90°), the card turns back (`mechanical`, 90° → 0°) and lands from its lift (`pop`).
    public val dialogContentOut: MotionStep = fromPrototype(0, 90)
    public val dialogCollapse: MotionStep = fromPrototype(60, 240)
    public val dialogFlipOut: MotionStep = fromPrototype(140, 160)
    public val scrimOut: MotionStep = fromPrototype(60, 220)
    public val cardTurnBack: MotionStep = fromPrototype(280, 180)
    public val cardLand: MotionStep = fromPrototype(420, 180)

    /** #15 the categories take input again from here on. */
    public val dialogCloseSettledMillis: Int = prototypeMillis(620)

    // #16 OYNA → game, letter morph. Times count from the tap; the button's own press (#1) plays first. The ink
    // rectangle grows from the button to the screen (`mechanical`) while its colour goes red → ink (linear, sRGB),
    // the big letter pops in (`pop`, scale 0.85 → 1, alpha 0 → 1), holds, then the ink shrinks into the wheel's
    // current-letter marker (`mechanical`) with the letter shrinking along. The shrink waits for the game to be
    // ready: [inkShrink] is its earliest start.
    public val inkExpand: MotionStep = fromPrototype(160, 300)
    public val inkColor: MotionStep = fromPrototype(160, 200)
    public val letterIn: MotionStep = fromPrototype(340, 200)
    public val letterStartScale: Float = 0.85f
    public val inkShrink: MotionStep = fromPrototype(660, 300)

    /** #16 the big letter is drawn once at this size and scaled; it ends at [letterEndSize]. */
    public val letterStartSize: Float = 230f
    public val letterEndSize: Float = 20f

    /**
     * #16 not in the prototype (its game was always ready): when the game is still loading at the end of the hold, the
     * big letter pulses up to this scale, one pulse every [letterPulseMillis], until the shrink can start.
     */
    public val letterPulseScale: Float = 1.04f
    public val letterPulseMillis: Int = 700

    // #16 game board entrance, relative to the start of the shrink.
    public val boardTopBar: MotionStep = fromPrototype(20, 220)
    public val boardTopBarOffset: Dp = (-10).dp
    public val boardRingChip: MotionStep = fromPrototype(40, 200)
    public val boardRingChipStaggerMillis: Int = prototypeMillis(8)
    public val boardCentre: MotionStep = fromPrototype(80, 260)
    public val boardCentreStartScale: Float = 0.7f
    public val boardDecor: MotionStep = fromPrototype(100, 300)
    public val boardClue: MotionStep = fromPrototype(180, 240)
    public val boardClueOffset: Dp = 14.dp
    public val boardSlot: MotionStep = fromPrototype(240, 200)
    public val boardSlotStaggerMillis: Int = prototypeMillis(30)
    public val boardKey: MotionStep = fromPrototype(300, 200)
    public val boardKeyStaggerMillis: Int = prototypeMillis(20)
    public val boardBottomRow: MotionStep = fromPrototype(460, 220)
    public val boardItemOffset: Dp = 12.dp

    /** #16 everything has landed this long after the shrink started: input and the game clock may start. */
    public val boardSettledMillis: Int = prototypeMillis(780)

    // #17 in game, after a correct answer: score counts up (`mechanical`) with a pop (`pop`, 1.25 → 1); the centre
    // letter slides out down (`turn`) and the next one in from above (`mechanical`); the clue slides left out (`turn`)
    // and in from the right (`mechanical`); slots and keys are re-dealt with a stagger (`mechanical`).
    public val scoreCountUp: MotionStep = fromPrototype(50, 300)
    public val scorePop: MotionStep = fromPrototype(50, 200)
    public val scorePopScale: Float = 1.25f
    public val centreLetterOut: MotionStep = fromPrototype(0, 140)
    public val centreLetterIn: MotionStep = fromPrototype(140, 160)
    public val centreLetterOffset: Dp = 60.dp
    public val clueOut: MotionStep = fromPrototype(50, 120)
    public val clueIn: MotionStep = fromPrototype(170, 180)
    public val clueOffset: Dp = 30.dp
    public val redealSlotsOut: MotionStep = fromPrototype(0, 100)
    public val redealSlot: MotionStep = fromPrototype(200, 200)
    public val redealKey: MotionStep = fromPrototype(200, 200)

    /**
     * Tier Lite (and phase 2's placeholder dialog before the #14 flip lands): a Y flip becomes a scale from this value
     * to 1 with a fade, over the flip's grow step ([dialogGrow], `mechanical`).
     */
    public val liteFlipStartScale: Float = 0.92f

    // #18 "Çarkını seç" grid entrance (owner decision: first open per session, tier Full only): after the screen
    // transition starts, each card rises [gridCardOffset] and fades in (`mechanical`), one after another.
    public val gridCardIn: MotionStep = fromPrototype(140, 260)
    public val gridCardStaggerMillis: Int = prototypeMillis(45)
    public val gridCardOffset: Dp = 16.dp

    /** Reduced motion (tier [MotionTier.Minimal]): every flow transition becomes one cross-fade of this length. */
    public val reducedCrossfadeMillis: Int = 120

    /** Total letter-morph motion from the start of the expand to the end of the shrink (≈ 0.93 s). */
    public val letterMorphMillis: Int get() = inkShrink.endMillis - inkExpand.delayMillis
}
