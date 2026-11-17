package dev.talhabayhan.lexi.navigation

import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The OYNA rules around the letter morph (#16): starts, drops, back, background, load errors. */
class LetterMorphLauncherTest {
    private class FakeMorph(var startable: Boolean = true) : LetterMorph {
        var starts = 0
        var cancels = 0

        override fun start(tier: MotionTier): Boolean {
            if (!startable || tier == MotionTier.Minimal) return false
            starts++
            return true
        }

        override fun cancel() {
            cancels++
        }

        override suspend fun awaitCovered(): Boolean = true
    }

    private class FakeNavigation(var canStart: Boolean = true, var onGame: Boolean = false) : GameStartNavigation {
        var pops = 0

        override fun canStartGame() = canStart

        override fun isOnGame() = onGame

        override fun popGame() {
            pops++
        }
    }

    private val morph = FakeMorph()
    private val navigation = FakeNavigation()
    private val launcher = LetterMorphLauncher(morph, navigation)

    /** The last start's arguments, as LexiNavigator.startGame would receive them. */
    private var awaitCover: (suspend () -> Boolean)? = null
    private var onDropped: (() -> Unit)? = null

    private fun start(tier: MotionTier = MotionTier.Full, accepted: Boolean = true): Boolean =
        launcher.start(tier) { cover, dropped ->
            awaitCover = cover
            onDropped = dropped
            accepted
        }

    @Test
    fun `an accepted start morphs and navigates under the ink`() {
        assertTrue(start())
        assertEquals(1, morph.starts)
        assertNotNull(awaitCover, "the navigation waits for the ink")
        assertEquals(0, morph.cancels)
    }

    @Test
    fun `a start the navigator would reject shows no morph at all`() {
        navigation.canStart = false
        assertFalse(start())
        assertEquals(0, morph.starts)
        assertNull(onDropped, "the navigator is not asked")
    }

    @Test
    fun `a start rejected after all cancels the morph`() {
        assertFalse(start(accepted = false))
        assertEquals(1, morph.cancels)
    }

    @Test
    fun `a navigation that does not happen after the cover cancels the morph`() {
        start()
        checkNotNull(onDropped).invoke()
        assertEquals(1, morph.cancels, "no ink left pulsing on screen")
    }

    @Test
    fun `minimal navigates without a morph and has nothing to cancel`() {
        assertTrue(start(MotionTier.Minimal))
        assertEquals(0, morph.starts)
        assertNull(awaitCover, "the cross-fade navigates as soon as the game is prepared")
        checkNotNull(onDropped).invoke()
        assertEquals(0, morph.cancels)
    }

    @Test
    fun `back before the game was opened cancels the morph and stays on the dialog's screen`() {
        start()
        launcher.onBack()
        assertEquals(1, morph.cancels)
        launcher.onCancelled()
        assertEquals(0, navigation.pops)
    }

    @Test
    fun `back after the game was opened under the ink leaves that unstarted game again`() {
        start()
        navigation.onGame = true
        launcher.onBack()
        launcher.onCancelled()
        assertEquals(1, navigation.pops)
    }

    @Test
    fun `going to the background cancels the morph and keeps the game it opened`() {
        start()
        navigation.onGame = true
        launcher.onStop()
        launcher.onCancelled()
        assertEquals(1, morph.cancels)
        assertEquals(0, navigation.pops, "the game starts when the app comes back")
    }

    @Test
    fun `a load error cancel keeps the game screen and its error`() {
        start()
        navigation.onGame = true
        // The game screen cancels the morph itself (LetterMorphEntrance); only the end of the fade reaches here.
        launcher.onCancelled()
        assertEquals(0, navigation.pops)
    }

    @Test
    fun `a back of an earlier run does not leak into the next one`() {
        start()
        launcher.onBack()
        start()
        navigation.onGame = true
        launcher.onCancelled()
        assertEquals(0, navigation.pops)
    }
}
