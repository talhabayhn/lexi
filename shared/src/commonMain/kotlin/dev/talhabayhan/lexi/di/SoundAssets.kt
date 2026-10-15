package dev.talhabayhan.lexi.di

import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.platform.feedback.assetPath
import dev.talhabayhan.lexi.shared.generated.resources.Res

/** Bytes of the effect's WAV in shared's Compose resources (`files/sounds`, `tools/sounds/generate_sounds.py`). */
internal suspend fun readSoundAsset(effect: SoundEffect): ByteArray = Res.readBytes(effect.assetPath)
