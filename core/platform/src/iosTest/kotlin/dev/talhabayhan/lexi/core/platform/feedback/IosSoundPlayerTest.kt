package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import platform.Foundation.NSThread
import kotlin.concurrent.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosSoundPlayerTest {

    // Regression (iOS first-launch splash stutter): the players were read and prepared on the main thread while the
    // splash played. An unconfined scope runs on the caller (the test's main thread) until the player moves the work.
    @Test
    fun `the sounds are read and prepared off the main thread`() = runBlocking {
        assertTrue(NSThread.isMainThread, "precondition: the test starts on the main thread")
        val readOnMain = AtomicReference<List<Boolean>>(emptyList())
        val player = IosSoundPlayer(
            scope = CoroutineScope(Dispatchers.Unconfined),
            readAsset = {
                readOnMain.value = readOnMain.value + NSThread.isMainThread
                ByteArray(0)
            },
        )
        // Not ready yet (or never, with these empty assets): play does nothing instead of failing.
        player.play(SoundEffect.Tap)

        withTimeout(TIMEOUT_MILLIS) {
            while (readOnMain.value.size < SoundEffect.entries.size) delay(POLL_MILLIS)
        }

        assertEquals(List(SoundEffect.entries.size) { false }, readOnMain.value)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val POLL_MILLIS = 10L
    }
}
