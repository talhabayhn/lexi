package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import dev.talhabayhan.lexi.core.domain.feedback.SoundEffect
import dev.talhabayhan.lexi.core.domain.feedback.SoundPlayer
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
