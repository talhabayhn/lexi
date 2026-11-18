package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

/**
 * [Haptics] on UIKit feedback generators (`docs/design/motion.md` haptic map). Call on the main thread.
 *
 * One generator per kind, created on first use and kept (Apple's guidance), instead of a new one per call: the splash
 * plays four light impacts in its first half second, and setting up a generator each time is main-thread work inside
 * that animation. The light one is created and prepared when this object is built (first composition, before the splash clock starts).
 */
@OptIn(ExperimentalForeignApi::class)
public class IosHaptics : Haptics {
    private val light = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
        .also { it.prepare() }
    private val soft by lazy { UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleSoft) }
    private val heavy by lazy { UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy) }
    private val notification by lazy { UINotificationFeedbackGenerator() }
    private val selection by lazy { UISelectionFeedbackGenerator() }

    override fun perform(kind: HapticKind) {
        when (kind) {
            HapticKind.Light -> light.impactOccurred()

            HapticKind.Soft -> soft.impactOccurred()

            HapticKind.Heavy -> heavy.impactOccurred()

            HapticKind.Success ->
                notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)

            HapticKind.Error ->
                notification.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)

            HapticKind.Tick -> selection.selectionChanged()
        }
    }
}
