package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import dev.talhabayhan.lexi.core.domain.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class GatedFeedbackTest {
    @Test
    fun hapticsFollowTheSetting() {
        val played = mutableListOf<HapticKind>()
        var on = false
        val haptics = GatedHaptics(Haptics { played += it }, { on })
        haptics.perform(HapticKind.Light)
        on = true
        haptics.perform(HapticKind.Error)
        assertEquals(listOf(HapticKind.Error), played)
    }

    @Test
    fun soundsFollowTheSetting() {
        val played = mutableListOf<SoundEffect>()
        var on = true
        val player = GatedSoundPlayer(SoundPlayer { played += it }, { on })
        player.play(SoundEffect.Tap)
        on = false
        player.play(SoundEffect.Wrong)
        assertEquals(listOf(SoundEffect.Tap), played)
    }

    @Test
    fun assetPathsAreLowercaseWavNames() {
        assertEquals("files/sounds/correct.wav", SoundEffect.Correct.assetPath)
        assertEquals(SoundEffect.entries.size, SoundEffect.entries.map { it.assetPath }.toSet().size)
    }
}

class LiveSettingsFeedbackTest {
    private class Store : SettingsRepository {
        val state = MutableStateFlow(Settings())
        override val settings: Flow<Settings> = state

        override suspend fun update(transform: (Settings) -> Settings) {
            state.value = transform(state.value)
        }
    }

    @Test
    fun `sound and haptics follow the stored switches as they change`() {
        val store = Store()
        val live = LiveSettings(store, CoroutineScope(Dispatchers.Unconfined))
        val sounds = mutableListOf<SoundEffect>()
        val haptics = mutableListOf<HapticKind>()
        val player = GatedSoundPlayer(SoundPlayer { sounds += it }) { live.current.soundEffects }
        val feedback = GatedHaptics(Haptics { haptics += it }) { live.current.haptics }

        player.play(SoundEffect.Tap)
        feedback.perform(HapticKind.Light)
        store.state.value = store.state.value.copy(soundEffects = false)
        player.play(SoundEffect.Wrong)
        feedback.perform(HapticKind.Error)
        store.state.value = store.state.value.copy(soundEffects = true, haptics = false)
        player.play(SoundEffect.Correct)
        feedback.perform(HapticKind.Heavy)
        live.close()

        assertEquals(listOf(SoundEffect.Tap, SoundEffect.Correct), sounds)
        assertEquals(listOf(HapticKind.Light, HapticKind.Error), haptics)
    }
}
