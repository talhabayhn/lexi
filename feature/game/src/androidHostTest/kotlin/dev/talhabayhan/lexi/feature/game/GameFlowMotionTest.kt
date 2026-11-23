package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMorphKeys
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMotion
import dev.talhabayhan.lexi.core.designsystem.motion.MorphOverlayHost
import dev.talhabayhan.lexi.core.designsystem.motion.MorphOverlayState
import dev.talhabayhan.lexi.core.designsystem.motion.morphAnchor
import dev.talhabayhan.lexi.core.designsystem.theme.LexiColors
import dev.talhabayhan.lexi.core.designsystem.theme.LexiMotion
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.game.components.ScoreCount
import dev.talhabayhan.lexi.feature.game.components.TurnSwap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Video-flow motion on the game board (`motion.md` #16 board entrance, #17 turn change and score count-up), checked on
 * the pixels of the paused frame clock: pieces are hidden while they wait, and every motion ends on exactly the frame
 * a board composed at rest draws.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xhdpi")
class GameFlowMotionTest {
    private lateinit var motion: LexiMotion
    private val flow: FlowMotion get() = motion.flow

    @Test
    fun `a board opened under the letter morph waits under the ink and is at rest and playable after it`() =
        runComposeUiTest {
            val morph = MorphOverlayState()
            var showGame by mutableStateOf(false)
            var generation by mutableIntStateOf(0)
            var state by mutableStateOf(GameSamples.design.copy(isStarting = true, inputEnabled = false))
            val intents = mutableListOf<GameIntent>()
            var finished = 0
            lateinit var colors: LexiColors
            mainClock.autoAdvance = false
            setContent {
                LexiTheme(reducedMotion = false) {
                    colors = LexiTheme.colors
                    motion = LexiTheme.motion
                    MorphOverlayHost(morph, onFinished = {
                        finished++
                        state = GameSamples.design
                    }) {
                        Box(Modifier.fillMaxSize()) {
                            Box(Modifier.offset(20.dp, 760.dp).size(350.dp, 56.dp).morphAnchor(PLAY))
                            if (showGame) {
                                key(generation) {
                                    GameScreen(state, clock = { GameSamples.designClock }, onIntent = { intents += it })
                                }
                            }
                        }
                    }
                }
            }
            mainClock.advanceTimeByFrame()
            val start = mainClock.currentTime
            assertTrue(runOnUiThread { morph.start(PLAY, FlowMorphKeys.WHEEL_MARKER, "K") })
            Snapshot.sendApplyNotifications()
            mainClock.advanceTimeBy(flow.inkExpand.endMillis + 3 * FRAME_MS)
            assertTrue(runOnUiThread { morph.covered })

            // The game is navigated to under the ink and becomes ready.
            update { showGame = true }
            update { morph.markReady() }
            assertEquals(colors.ink, pixelOfPass(colors), "everything is under the ink before the shrink")

            // The ink has landed on the marker; the bottom row (from 535 ms after the shrink start) still waits.
            advanceTo(start + flow.inkShrink.endMillis + 3 * FRAME_MS)
            assertEquals(colors.cream, pixelOfPass(colors), "the bottom row is hidden until its turn")
            // Hidden from screen readers under the ink: tap where the first key is.
            val firstKey = keys(useUnmergedTree = true).onFirst().fetchSemanticsNode().boundsInRoot.center
            onRoot().performTouchInput { click(firstKey) }
            mainClock.advanceTimeBy(FRAME_MS)
            assertEquals(0, intents.count { it is GameIntent.KeyTapped }, "no input before the board has landed")

            mainClock.advanceTimeBy(flow.boardSettledMillis.toLong())
            assertEquals(1, finished)
            assertEquals(colors.yellow, pixelOfPass(colors), "the bottom row has landed")
            val entered = onRoot().captureToImage()

            // The same board composed without a morph: the entrance ends exactly on it.
            update { generation++ }
            mainClock.advanceTimeBy(FRAME_MS)
            assertEquals(0, entered.differingPixels(onRoot().captureToImage()))

            mainClock.autoAdvance = true
            keys().onFirst().performClick()
            waitForIdle()
            assertTrue(GameIntent.KeyTapped(0) in intents, "the board takes input once the game has started")
        }

    @Test
    fun `a new turn first shows the previous keys and ends on the new board at rest`() = runComposeUiTest {
        var state by mutableStateOf(GameSamples.design)
        var generation by mutableIntStateOf(0)
        setBoard({ state }, { generation })
        val before = keysImage()

        update { state = nextTurn }
        assertEquals(0, before.differingPixels(keysImage()), "the previous keys stay until the re-deal")
        assertEquals(nextTurn.turn!!.keys.size, keys().fetchSemanticsNodes().size, "only the new keys are reachable")

        mainClock.advanceTimeBy(REDEAL_DONE_MS)
        val dealt = onRoot().captureToImage()
        update { generation++ }
        assertEquals(0, dealt.differingPixels(onRoot().captureToImage()), "the turn change ends on the board at rest")
    }

    @Test
    fun `a key hidden by the re-deal takes no tap until it starts to appear`() = runComposeUiTest {
        var state by mutableStateOf(GameSamples.design)
        val intents = mutableListOf<GameIntent>()
        setBoard({ state }, { 0 }, onIntent = { intents += it })

        update { state = nextTurn }
        keys().onFirst().performClick()
        mainClock.advanceTimeBy(FRAME_MS)
        assertEquals(0, intents.count { it is GameIntent.KeyTapped }, "the previous keys still show: no tap counts")

        mainClock.advanceTimeBy(REDEAL_DONE_MS)
        keys().onFirst().performClick()
        mainClock.advanceTimeBy(FRAME_MS)
        assertEquals(listOf<GameIntent>(GameIntent.KeyTapped(0)), intents.filterIsInstance<GameIntent.KeyTapped>())
    }

    @Test
    fun `under reduced motion a new turn is drawn at once`() = runComposeUiTest {
        var state by mutableStateOf(GameSamples.design)
        var generation by mutableIntStateOf(0)
        setBoard({ state }, { generation }, reducedMotion = true)

        update { state = nextTurn }
        mainClock.advanceTimeBy(CENTRE_LETTER_DONE_MS)
        val changed = keysImage()
        update { generation++ }
        assertEquals(0, changed.differingPixels(keysImage()))
    }

    @Test
    fun `the score counts up from the old value and ends on the new one`() = runComposeUiTest {
        var state by mutableStateOf(GameSamples.design)
        var generation by mutableIntStateOf(0)
        setBoard({ state }, { generation })
        val old = scoreImage("120")

        update { state = GameSamples.design.copy(score = 130) }
        assertEquals(0, old.differingPixels(scoreImage("130")), "the count starts on the old value")

        mainClock.advanceTimeBy((flow.scorePop.delayMillis + flow.scorePop.durationMillis / 4).toLong())
        assertTrue(old.differingPixels(scoreImage("130")) > 0, "the badge pops while it counts")

        mainClock.advanceTimeBy(maxOf(flow.scoreCountUp.endMillis, flow.scorePop.endMillis).toLong())
        val counted = scoreImage("130")
        update { generation++ }
        assertEquals(0, counted.differingPixels(scoreImage("130")), "the count ends on the new value")
    }

    @Test
    fun `the score count starts on the old value and ends on the new one with a pop`() = runComposeUiTest {
        setMotion()
        val count = ScoreCount(from = 120, to = 130, flow = flow, motion = motion)
        assertEquals("120", count.text())
        assertEquals(1f, count.scale())

        runBlocking { count.time.snapTo(flow.scorePop.delayMillis.toFloat()) }
        assertEquals(flow.scorePopScale, count.scale(), "the pop starts at 1.25")

        runBlocking { count.time.snapTo((flow.scoreCountUp.endMillis - 1).toFloat()) }
        assertEquals("130", count.text())

        runBlocking { count.time.snapTo(flow.scoreCountUp.endMillis.toFloat()) }
        assertNull(count.text(), "at rest the badge draws the state's value")
        assertEquals(1f, count.scale())
    }

    @Test
    fun `the re-deal waits for the previous pieces and staggers the new ones except in tier Lite`() = runComposeUiTest {
        setMotion()
        val turn = checkNotNull(nextTurn.turn)
        val full = swap(turn, stagger = true)
        val lite = swap(turn, stagger = false)
        assertEquals(0f, full.slotProgress(0))
        assertEquals(0f, full.keyProgress(0))

        val time = (flow.redealKey.delayMillis + flow.boardKeyStaggerMillis).toFloat()
        runBlocking {
            full.time.snapTo(time)
            lite.time.snapTo(time)
        }
        assertTrue(full.keyProgress(0) > 0f)
        assertEquals(0f, full.keyProgress(2), "the third key waits for its stagger")
        assertEquals(lite.keyProgress(0), lite.keyProgress(turn.keys.size - 1), "tier Lite deals them together")

        runBlocking { full.time.snapTo(full.totalMillis.toFloat()) }
        assertEquals(1f, full.slotProgress(turn.words.sumOf { it.size } - 1))
        assertEquals(1f, full.keyProgress(turn.keys.size - 1))
    }

    @Test
    fun `a re-deal interrupted mid-way (background, screen left) ends at rest`() = runComposeUiTest {
        setMotion()
        val turn = checkNotNull(nextTurn.turn)
        val swap = swap(turn, stagger = true)
        runBlocking {
            val job = launch(TestFrameClock()) { swap.run() }
            // A few frames in: the previous pieces still show and the new ones are hidden.
            repeat(3) { yield() }
            assertTrue(swap.ghostsShown, "cancelled mid-way")
            assertTrue(swap.keyProgress(turn.keys.size - 1) < 1f, "cancelled mid-way")
            job.cancelAndJoin()
        }
        assertFalse(swap.ghostsShown, "the previous turn's pieces are gone")
        assertEquals(1f, swap.keyProgress(turn.keys.size - 1), "every new key is fully drawn")
        assertEquals(1f, swap.slotProgress(turn.words.sumOf { it.size } - 1))
    }

    @Test
    fun `finishing a re-deal (the app went to the background) lands it at once`() = runComposeUiTest {
        setMotion()
        val turn = checkNotNull(nextTurn.turn)
        val swap = swap(turn, stagger = true)
        runBlocking { swap.finish() }
        assertFalse(swap.ghostsShown)
        assertEquals(1f, swap.keyProgress(0))
        assertEquals(1f, swap.slotProgress(0))
    }

    private fun ComposeUiTest.setMotion() {
        setContent { LexiTheme(reducedMotion = false) { motion = LexiTheme.motion } }
        waitForIdle()
    }

    private fun swap(turn: GameTurnUi, stagger: Boolean) = TurnSwap(
        outgoing = turn,
        flow = flow,
        motion = motion,
        stagger = stagger,
        clueOffsetPx = CLUE_OFFSET_PX,
        itemOffsetPx = ITEM_OFFSET_PX,
        cells = turn.words.sumOf { it.size },
        keys = turn.keys.size,
    )

    private fun ComposeUiTest.setBoard(
        state: () -> GameUiState,
        generation: () -> Int,
        reducedMotion: Boolean = false,
        onIntent: (GameIntent) -> Unit = {},
    ) {
        mainClock.autoAdvance = false
        setContent {
            LexiTheme(reducedMotion = reducedMotion) {
                motion = LexiTheme.motion
                key(generation()) {
                    GameScreen(state(), clock = { GameSamples.designClock }, onIntent = onIntent)
                }
            }
        }
        mainClock.advanceTimeBy(FRAME_MS)
    }

    /** Writes test-thread state the way the recomposer sees it, then lets one frame pass. */
    private fun ComposeUiTest.update(block: () -> Unit) {
        runOnUiThread {
            block()
            Snapshot.sendApplyNotifications()
        }
        mainClock.advanceTimeBy(FRAME_MS)
    }

    private fun ComposeUiTest.advanceTo(time: Long) {
        mainClock.advanceTimeBy(time - mainClock.currentTime)
    }

    private fun ComposeUiTest.keys(useUnmergedTree: Boolean = false) =
        onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag(GameTestTags.KEYS)), useUnmergedTree)

    private fun ComposeUiTest.keysImage(): ImageBitmap = onNode(hasTestTag(GameTestTags.KEYS)).captureToImage()

    private fun ComposeUiTest.scoreImage(value: String): ImageBitmap =
        onNode(hasContentDescription(value, substring = true)).captureToImage()

    /** A point on the PAS button clear of its label: 12dp inside the right end of the bottom row. */
    private fun ComposeUiTest.pixelOfPass(colors: LexiColors): Color {
        // Under the ink the board is hidden from screen readers (cleared semantics): the unmerged tree still has it.
        val row = onNode(hasTestTag(GameTestTags.ACTIONS), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val inset = with(density) { 12.dp.toPx() }
        val pixel = onRoot().captureToImage().toPixelMap()[
            (row.right - inset).roundToInt(),
            row.center.y.roundToInt(),
        ]
        return listOf(colors.ink, colors.cream, colors.yellow).firstOrNull { it.isCloseTo(pixel) } ?: pixel
    }

    private fun Color.isCloseTo(other: Color): Boolean =
        abs(red - other.red) < COLOR_TOLERANCE && abs(green - other.green) < COLOR_TOLERANCE &&
            abs(blue - other.blue) < COLOR_TOLERANCE

    private fun ImageBitmap.differingPixels(other: ImageBitmap): Int {
        assertEquals(width to height, other.width to other.height)
        val a = toPixelMap()
        val b = other.toPixelMap()
        var count = 0
        for (y in 0 until height) for (x in 0 until width) if (a[x, y] != b[x, y]) count++
        return count
    }

    private companion object {
        const val PLAY = "play"
        const val CLUE_OFFSET_PX = 30f
        const val ITEM_OFFSET_PX = 12f
        const val COLOR_TOLERANCE = 0.02f

        /** The longest re-deal (10 keys) is over well before this. */
        const val REDEAL_DONE_MS = 1_200L

        /** The centre letter's #17 slide (163 + 186 ms) is over. */
        const val CENTRE_LETTER_DONE_MS = 400L

        /** The next turn of the design sample: "B", "ekmek" → BREAD, other keys, nothing typed yet. */
        val nextTurn: GameUiState = GameSamples.design.let { design ->
            val turn = checkNotNull(design.turn)
            design.copy(
                wheel = design.wheel.map {
                    when (it.letter) {
                        'K' -> it.copy(state = WheelLetterState.Correct)
                        'L' -> it.copy(state = WheelLetterState.Current)
                        else -> it
                    }
                }.toImmutableList(),
                score = design.score + 10,
                turn = turn.copy(
                    letter = 'L',
                    clue = "limon",
                    letterCount = 5,
                    words = persistentListOf(
                        "LEMON".mapIndexed { index, letter ->
                            when (index) {
                                0 -> AnswerCellUi(letter, AnswerSlotState.Given)
                                1 -> AnswerCellUi(null, AnswerSlotState.Active)
                                else -> AnswerCellUi(null, AnswerSlotState.Empty)
                            }
                        }.toImmutableList(),
                    ),
                    keys = "MONEAPRTSU".map { KeyUi(it, LetterKeyState.Normal) }.toImmutableList(),
                ),
            )
        }
    }
}

/** A frame clock that advances 16 ms per frame, for running a [TurnSwap] outside the composition. */
private class TestFrameClock : androidx.compose.runtime.MonotonicFrameClock {
    private var nanos = 0L

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        yield()
        nanos += 16_000_000L
        return onFrame(nanos)
    }
}
