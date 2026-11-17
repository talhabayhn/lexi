package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.motion.FlowMotion
import dev.talhabayhan.lexi.core.designsystem.motion.MorphOverlayHost
import dev.talhabayhan.lexi.core.designsystem.motion.MorphOverlayState
import dev.talhabayhan.lexi.core.designsystem.motion.morphAnchor
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The game screen's side of the letter morph (`LetterMorphEntrance`): the clock gate. A game opened under the ink holds
 * its start until the morph is over or cancelled; a game that was not opened under one (no morph, or an activity
 * recreated mid-morph, whose new morph state is idle) is released at once, so a game can never stay unstarted and
 * never runs under the ink.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xhdpi")
class LetterMorphEntranceTest {
    private lateinit var flow: FlowMotion

    private fun androidx.compose.ui.test.ComposeUiTest.ui(block: () -> Unit) {
        runOnUiThread { block() }
        Snapshot.sendApplyNotifications()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.host(
        morph: MorphOverlayState,
        intents: MutableList<GameIntent>,
        isLoading: () -> Boolean = { false },
        hasError: () -> Boolean = { false },
        showGame: () -> Boolean = { true },
        onCancelled: () -> Unit = {},
    ) {
        mainClock.autoAdvance = false
        setContent {
            LexiTheme(reducedMotion = false) {
                flow = LexiTheme.motion.flow
                MorphOverlayHost(morph, onCancelled = onCancelled) {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.offset(20.dp, 700.dp).size(350.dp, 80.dp).morphAnchor(PLAY))
                        Box(Modifier.offset(177.dp, 120.dp).size(36.dp).morphAnchor(MARKER))
                        if (showGame()) LetterMorphEntrance(isLoading(), hasError(), onIntent = { intents += it })
                    }
                }
            }
        }
        mainClock.advanceTimeByFrame()
        waitForIdle()
    }

    @Test
    fun `a game opened without a morph is released at once`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        mainClock.autoAdvance = false
        setContent { LexiTheme(reducedMotion = false) { LetterMorphEntrance(false, false) { intents += it } } }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        assertEquals(listOf<GameIntent>(GameIntent.ReleaseStart), intents)
    }

    @Test
    fun `a game recreated mid-morph meets an idle morph state and starts at once`() = runComposeUiTest {
        // The activity was recreated: its fresh MorphOverlayState has nothing running although the game is open.
        val intents = mutableListOf<GameIntent>()
        host(MorphOverlayState(), intents)
        assertEquals(listOf<GameIntent>(GameIntent.ReleaseStart), intents, "never a hold without a running morph")
    }

    @Test
    fun `a game opened under the ink holds its clock until the morph is over`() = runComposeUiTest {
        val morph = MorphOverlayState()
        val intents = mutableListOf<GameIntent>()
        var show by mutableStateOf(false)
        host(morph, intents, showGame = { show })
        assertTrue(runOnUiThread { morph.start(PLAY, MARKER, "B") })
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(flow.inkExpand.endMillis + 3 * FRAME_MS)
        // Navigated to under the ink, then loaded.
        ui { show = true }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        assertEquals(listOf<GameIntent>(GameIntent.HoldStart), intents, "held, and not released while the morph runs")

        ui { morph.markReady() }
        mainClock.advanceTimeBy(flow.inkShrink.endMillis + flow.boardSettledMillis + 6 * FRAME_MS)
        waitForIdle()
        assertFalse(runOnUiThread { morph.isRunning })
        assertEquals(listOf<GameIntent>(GameIntent.HoldStart, GameIntent.ReleaseStart), intents)
    }

    @Test
    fun `a cancel under the ink releases the held clock exactly once`() = runComposeUiTest {
        val morph = MorphOverlayState()
        val intents = mutableListOf<GameIntent>()
        var show by mutableStateOf(false)
        host(morph, intents, showGame = { show })
        ui { morph.start(PLAY, MARKER, "B") }
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(flow.inkExpand.endMillis + 3 * FRAME_MS)
        ui { show = true }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        ui { morph.cancel() }
        Snapshot.sendApplyNotifications()
        mainClock.advanceTimeBy(flow.reducedCrossfadeMillis + 100L)
        waitForIdle()

        assertEquals(listOf<GameIntent>(GameIntent.HoldStart, GameIntent.ReleaseStart), intents)
        assertFalse(runOnUiThread { morph.isRunning })
    }

    @Test
    fun `a load error cancels the morph and releases the game`() = runComposeUiTest {
        val morph = MorphOverlayState()
        val intents = mutableListOf<GameIntent>()
        var show by mutableStateOf(false)
        var error by mutableStateOf(false)
        var cancelled = 0
        host(morph, intents, hasError = { error }, showGame = { show }, onCancelled = { cancelled++ })
        ui { morph.start(PLAY, MARKER, "B") }
        mainClock.advanceTimeBy(flow.inkExpand.endMillis + 3 * FRAME_MS)
        ui { show = true }
        mainClock.advanceTimeByFrame()
        waitForIdle()

        // The game cancels the morph itself: its error and its way back stay on screen, no clock was running.
        ui { error = true }
        mainClock.advanceTimeBy(flow.reducedCrossfadeMillis + 100L)
        waitForIdle()
        assertFalse(runOnUiThread { morph.isRunning })
        assertEquals(1, cancelled)
        assertEquals(listOf<GameIntent>(GameIntent.HoldStart, GameIntent.ReleaseStart), intents)
    }

    @Test
    fun `leaving the game mid-morph takes the morph down`() = runComposeUiTest {
        val morph = MorphOverlayState()
        val intents = mutableListOf<GameIntent>()
        var show by mutableStateOf(false)
        var cancelled = 0
        host(morph, intents, showGame = { show }, onCancelled = { cancelled++ })
        ui { morph.start(PLAY, MARKER, "B") }
        mainClock.advanceTimeBy(flow.inkExpand.endMillis + 3 * FRAME_MS)
        ui { show = true }
        mainClock.advanceTimeByFrame()
        waitForIdle()
        assertTrue(runOnUiThread { morph.isRunning })

        // Back out of the game (or the game's entry going away) while the ink still holds the screen.
        ui { show = false }
        mainClock.advanceTimeBy(flow.reducedCrossfadeMillis + 100L)
        waitForIdle()
        assertFalse(runOnUiThread { morph.isRunning }, "no ink is left over a screen that went away")
        assertEquals(1, cancelled)
    }

    private companion object {
        const val PLAY = "play"
        const val MARKER = "marker"
        const val FRAME_MS = 16L
    }
}
