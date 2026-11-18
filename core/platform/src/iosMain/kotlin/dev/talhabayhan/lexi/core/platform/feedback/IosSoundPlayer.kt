package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.Foundation.NSData
import platform.Foundation.create
import kotlin.concurrent.AtomicReference

/**
 * [SoundPlayer] on preloaded `AVAudioPlayer`s. The audio session uses the ambient category, so the device silent
 * switch mutes the effects and other apps' audio keeps playing.
 *
 * The session category and the players are set up on [loading] (a background dispatcher), never on the main thread:
 * the player is created while the splash plays, and `AVAudioPlayer` + `prepareToPlay` (audio hardware and codec set-up,
 * slowest on the first launch after install) on the main thread made the splash stutter (`docs/PERF.md` § iOS first
 * launch). Each ready player is published in an immutable map; [play] (main thread) skips an effect that is not
 * ready yet. [scope] only owns the loading job.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
public class IosSoundPlayer(
    scope: CoroutineScope,
    private val readAsset: suspend (SoundEffect) -> ByteArray,
    loading: CoroutineDispatcher = Dispatchers.Default,
) : SoundPlayer {
    private val players = AtomicReference<Map<SoundEffect, AVAudioPlayer>>(emptyMap())

    init {
        scope.launch {
            withContext(loading) {
                AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, error = null)
                for (effect in SoundEffect.entries) {
                    runCatching {
                        val player = AVAudioPlayer(data = readAsset(effect).toNSData(), error = null)
                        player.prepareToPlay()
                        // The only writer: each effect is playable as soon as it is ready (Tap first, for the splash).
                        players.value = players.value + (effect to player)
                    }
                }
            }
        }
    }

    override fun play(effect: SoundEffect) {
        val player = players.value[effect] ?: return
        player.currentTime = 0.0
        player.play()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = if (isEmpty()) null else pinned.addressOf(0), length = size.toULong())
}
