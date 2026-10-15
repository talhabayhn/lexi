package dev.talhabayhan.lexi.core.platform.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * [SoundPlayer] on a [SoundPool]. Every effect is preloaded in the background from [readAsset] (the Compose
 * resource bytes of the effect's `assetPath`); [play] before loading finished is a silent no-op.
 */
public class AndroidSoundPlayer(
    context: Context,
    private val scope: CoroutineScope,
    private val readAsset: suspend (SoundEffect) -> ByteArray,
) : SoundPlayer,
    AutoCloseable {
    private val cacheDir = File(context.cacheDir, "sounds").apply { mkdirs() }
    private val pool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        ).build()
    private val ids = ConcurrentHashMap<SoundEffect, Int>()

    init {
        scope.launch {
            for (effect in SoundEffect.entries) {
                runCatching {
                    val file = File(cacheDir, "${effect.assetName}.wav")
                    file.writeBytes(readAsset(effect))
                    ids[effect] = pool.load(file.path, 1)
                }
            }
        }
    }

    override fun play(effect: SoundEffect) {
        val id = ids[effect] ?: return
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    override fun close() {
        scope.cancel()
        pool.release()
    }

    private companion object {
        const val MAX_STREAMS = 4
    }
}
