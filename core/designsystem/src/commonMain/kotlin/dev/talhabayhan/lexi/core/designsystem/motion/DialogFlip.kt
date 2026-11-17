package dev.talhabayhan.lexi.core.designsystem.motion

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable

/**
 * What the category card, the scrim and the mode / time dialog look like at one moment of `motion.md` #13–#15. The
 * caller applies it in `graphicsLayer` only: [cardLift] (0..1, the pop may overshoot) drives the card's −6 dp rise,
 * −2° tilt and 4 → 10 dp shadow; the turns are `rotationY` degrees; [dialogGrow] 0 = the card's rectangle, 1 = the
 * dialog's own; [dialogScale] multiplies it (Lite); alphas are 0..1 ([scrimAlpha] of [FlowMotion.scrimAlpha]).
 */
@Immutable
public data class DialogFlipFrame(
    val cardLift: Float,
    val cardTurnDegrees: Float,
    val cardAlpha: Float,
    val scrimAlpha: Float,
    val dialogTurnDegrees: Float,
    val dialogGrow: Float,
    val dialogScale: Float,
    val dialogAlpha: Float,
    val contentAlpha: Float,
)

/** The two choreographies; a reversed run plays one of them backwards. */
public enum class DialogFlipTimeline { Open, Close }

/**
 * `motion.md` #13–#15 as pure functions of time per tier. Full: lift, Y flips and the grow from the card's
 * rectangle. Lite: same timing, no lift or tilt, the card stays, the dialog scales 0.92 → 1 and fades over the grow
 * step, rows enter together. Minimal: a [FlowMotion.reducedCrossfadeMillis] cross-fade of scrim and dialog.
 */
public class DialogFlipChoreography(
    private val flow: FlowMotion,
    private val mechanical: Easing,
    private val turn: Easing,
    private val pop: Easing,
) {
    /** Length of [timeline] in [tier]: the open one ends when the dialog takes input, the close one when the grid does. */
    public fun durationMillis(timeline: DialogFlipTimeline, tier: MotionTier): Long = when {
        tier == MotionTier.Minimal -> flow.reducedCrossfadeMillis.toLong()
        timeline == DialogFlipTimeline.Open -> flow.dialogOpenSettledMillis.toLong()
        else -> flow.dialogCloseSettledMillis.toLong()
    }

    public fun frameAt(timeline: DialogFlipTimeline, elapsedMillis: Long, tier: MotionTier): DialogFlipFrame =
        when (tier) {
            MotionTier.Minimal -> crossfade(timeline, elapsedMillis)
            MotionTier.Lite -> lite(timeline, elapsedMillis)
            MotionTier.Full -> full(timeline, elapsedMillis)
        }

    /** 0..1 rise + fade of the dialog's row [index] (header 0, mode 1, …). */
    public fun itemProgress(timeline: DialogFlipTimeline, elapsedMillis: Long, tier: MotionTier, index: Int): Float {
        if (timeline == DialogFlipTimeline.Close) return 1f
        return when (tier) {
            MotionTier.Minimal -> 1f

            MotionTier.Lite -> mechanical.transform(flow.dialogItemIn.progressAt(elapsedMillis))

            MotionTier.Full -> mechanical.transform(
                flow.dialogItemIn.staggered(index, flow.dialogItemStaggerMillis).progressAt(elapsedMillis),
            )
        }
    }

    private fun full(timeline: DialogFlipTimeline, t: Long): DialogFlipFrame = when (timeline) {
        DialogFlipTimeline.Open -> {
            val cardTurn = flow.flipDegrees * turn.transform(flow.cardTurnAway.progressAt(t))
            DialogFlipFrame(
                cardLift = pop.transform(flow.cardLift.progressAt(t)),
                cardTurnDegrees = cardTurn,
                cardAlpha = if (cardTurn >= flow.flipDegrees) 0f else 1f,
                scrimAlpha = flow.scrimIn.progressAt(t),
                dialogTurnDegrees = -flow.flipDegrees * (1f - mechanical.transform(flow.dialogFlipIn.progressAt(t))),
                dialogGrow = mechanical.transform(flow.dialogGrow.progressAt(t)),
                dialogScale = 1f,
                dialogAlpha = if (t >= flow.dialogFlipIn.delayMillis) 1f else 0f,
                contentAlpha = flow.dialogContentIn.progressAt(t),
            )
        }

        DialogFlipTimeline.Close -> {
            val dialogTurn = -flow.flipDegrees * turn.transform(flow.dialogFlipOut.progressAt(t))
            val cardTurning = t >= flow.cardTurnBack.delayMillis
            DialogFlipFrame(
                cardLift = 1f - pop.transform(flow.cardLand.progressAt(t)),
                cardTurnDegrees = flow.flipDegrees * (1f - mechanical.transform(flow.cardTurnBack.progressAt(t))),
                cardAlpha = if (cardTurning) 1f else 0f,
                scrimAlpha = 1f - flow.scrimOut.progressAt(t),
                dialogTurnDegrees = dialogTurn,
                dialogGrow = 1f - turn.transform(flow.dialogCollapse.progressAt(t)),
                dialogScale = 1f,
                dialogAlpha = if (dialogTurn <= -flow.flipDegrees) 0f else 1f,
                contentAlpha = 1f - flow.dialogContentOut.progressAt(t),
            )
        }
    }

    private fun lite(timeline: DialogFlipTimeline, t: Long): DialogFlipFrame {
        val shown = when (timeline) {
            DialogFlipTimeline.Open -> mechanical.transform(flow.dialogGrow.progressAt(t))
            DialogFlipTimeline.Close -> 1f - turn.transform(flow.dialogCollapse.progressAt(t))
        }
        val scrim = when (timeline) {
            DialogFlipTimeline.Open -> flow.scrimIn.progressAt(t)
            DialogFlipTimeline.Close -> 1f - flow.scrimOut.progressAt(t)
        }
        val content = when (timeline) {
            DialogFlipTimeline.Open -> flow.dialogContentIn.progressAt(t)
            DialogFlipTimeline.Close -> 1f - flow.dialogContentOut.progressAt(t)
        }
        return DialogFlipFrame(
            cardLift = 0f,
            cardTurnDegrees = 0f,
            cardAlpha = 1f,
            scrimAlpha = scrim,
            dialogTurnDegrees = 0f,
            dialogGrow = 1f,
            dialogScale = flow.liteFlipStartScale + (1f - flow.liteFlipStartScale) * shown,
            dialogAlpha = shown.coerceIn(0f, 1f),
            contentAlpha = content,
        )
    }

    private fun crossfade(timeline: DialogFlipTimeline, t: Long): DialogFlipFrame {
        val fade = MotionStep(0, flow.reducedCrossfadeMillis).progressAt(t)
        val shown = if (timeline == DialogFlipTimeline.Open) fade else 1f - fade
        return DialogFlipFrame(
            cardLift = 0f,
            cardTurnDegrees = 0f,
            cardAlpha = 1f,
            scrimAlpha = shown,
            dialogTurnDegrees = 0f,
            dialogGrow = 1f,
            dialogScale = 1f,
            dialogAlpha = shown,
            contentAlpha = 1f,
        )
    }
}

/** Where the dialog flip is: at rest (closed or open) or playing one of the timelines forwards or backwards. */
public enum class DialogFlipPhase { Closed, Opening, Open, Closing }

/**
 * The dialog flip's interruption rules as a pure state machine: [tick] advances by frame deltas. Opening and closing
 * from rest play their own timeline; a request against the running direction plays the **same** timeline backwards
 * from where it is (a close mid-open turns the dialog back into the card, a reopen mid-close turns it back up), so
 * nothing ever jumps. The tier is latched when a run starts from rest.
 */
public class DialogFlipMachine(private val choreography: DialogFlipChoreography) {
    public var timeline: DialogFlipTimeline = DialogFlipTimeline.Close
        private set

    /** Position in [timeline], 0..its duration. */
    public var positionMillis: Long = 0L
        private set

    public var tier: MotionTier = MotionTier.Full
        private set

    private var forward = true
    private var running = false

    init {
        positionMillis = choreography.durationMillis(DialogFlipTimeline.Close, tier)
    }

    /** Closed at rest: the grid takes input. */
    public val phase: DialogFlipPhase
        get() = when {
            !running && atClosedRest() -> DialogFlipPhase.Closed
            !running -> DialogFlipPhase.Open
            showingDialog() -> DialogFlipPhase.Opening
            else -> DialogFlipPhase.Closing
        }

    /** Something is moving: a frame clock is needed. */
    public val isRunning: Boolean get() = running

    /** The dialog is at rest and takes input. */
    public val dialogTakesInput: Boolean get() = phase == DialogFlipPhase.Open

    /** The dialog (or its scrim) is on screen at all. */
    public val dialogShown: Boolean get() = phase != DialogFlipPhase.Closed

    /** Starts (or reverses towards) the open dialog; [tier] applies when it starts from rest. */
    public fun open(tier: MotionTier) {
        when (phase) {
            DialogFlipPhase.Open, DialogFlipPhase.Opening -> Unit
            DialogFlipPhase.Closed -> startRun(DialogFlipTimeline.Open, tier)
            DialogFlipPhase.Closing -> forward = timeline == DialogFlipTimeline.Open
        }
    }

    /** Starts (or reverses towards) the closed state; [tier] applies when it starts from rest. */
    public fun close(tier: MotionTier) {
        when (phase) {
            DialogFlipPhase.Closed, DialogFlipPhase.Closing -> Unit
            DialogFlipPhase.Open -> startRun(DialogFlipTimeline.Close, tier)
            DialogFlipPhase.Opening -> forward = timeline == DialogFlipTimeline.Close
        }
    }

    /** Jumps to rest without motion (a screen restored with the dialog open, or tests). */
    public fun snapTo(open: Boolean) {
        snapTo(open, tier)
    }

    /** [snapTo] showing the rest look of [tier] (e.g. a screen restored with the dialog open under reduced motion). */
    public fun snapTo(open: Boolean, tier: MotionTier) {
        this.tier = tier
        running = false
        forward = true
        timeline = if (open) DialogFlipTimeline.Open else DialogFlipTimeline.Close
        positionMillis = choreography.durationMillis(timeline, tier)
    }

    /** Advances by [deltaMillis] of frame time; returns whether it is still running. */
    public fun tick(deltaMillis: Long): Boolean {
        if (!running) return false
        val duration = choreography.durationMillis(timeline, tier)
        positionMillis = (positionMillis + if (forward) deltaMillis else -deltaMillis).coerceIn(0L, duration)
        when {
            forward && positionMillis >= duration -> running = false

            !forward && positionMillis <= 0L -> {
                // Played backwards to its start: the rest state the other timeline ends in.
                running = false
                forward = true
                timeline =
                    if (timeline == DialogFlipTimeline.Open) DialogFlipTimeline.Close else DialogFlipTimeline.Open
                positionMillis = choreography.durationMillis(timeline, tier)
            }
        }
        return running
    }

    public fun frame(): DialogFlipFrame = choreography.frameAt(timeline, positionMillis, tier)

    public fun itemProgress(index: Int): Float = choreography.itemProgress(timeline, positionMillis, tier, index)

    private fun startRun(next: DialogFlipTimeline, tier: MotionTier) {
        this.tier = tier
        timeline = next
        positionMillis = 0L
        forward = true
        running = true
    }

    /** At rest after the close timeline (or before any run). */
    private fun atClosedRest(): Boolean = timeline == DialogFlipTimeline.Close

    /** The run heads towards (or stays at) the open dialog. */
    private fun showingDialog(): Boolean = (timeline == DialogFlipTimeline.Open) == forward
}
