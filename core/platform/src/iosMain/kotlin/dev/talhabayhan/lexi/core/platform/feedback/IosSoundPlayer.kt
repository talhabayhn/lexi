package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.Foundation.NSData
import platform.Foundation.create

/**
 * [SoundPlayer] on preloaded `AVAudioPlayer`s. The audio session uses the ambient category, so the device silent
 * switch mutes the effects and other apps' audio keeps playing. [scope] should run on the main dispatcher.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
public class IosSoundPlayer(scope: CoroutineScope, private val readAsset: suspend (SoundEffect) -> ByteArray) :
    SoundPlayer {
    private val players = mutableMapOf<SoundEffect, AVAudioPlayer>()

    init {
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, error = null)
        scope.launch {
            for (effect in SoundEffect.entries) {
                runCatching {
                    val player = AVAudioPlayer(data = readAsset(effect).toNSData(), error = null)
                    player.prepareToPlay()
                    players[effect] = player
                }
            }
        }
    }

    override fun play(effect: SoundEffect) {
        val player = players[effect] ?: return
        player.currentTime = 0.0
        player.play()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = if (isEmpty()) null else pinned.addressOf(0), length = size.toULong())
}
