package dev.talhabayhan.lexi.core.platform.feedback

import dev.talhabayhan.lexi.core.domain.feedback.HapticKind
import dev.talhabayhan.lexi.core.domain.feedback.Haptics
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

/** [Haptics] on UIKit feedback generators (`docs/design/motion.md` haptic map). Call on the main thread. */
@OptIn(ExperimentalForeignApi::class)
public class IosHaptics : Haptics {
    override fun perform(kind: HapticKind) {
        when (kind) {
            HapticKind.Light -> impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)

            HapticKind.Soft -> impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleSoft)

            HapticKind.Heavy -> impact(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)

            HapticKind.Success ->
                UINotificationFeedbackGenerator().notificationOccurred(
                    UINotificationFeedbackType.UINotificationFeedbackTypeSuccess,
                )

            HapticKind.Error ->
                UINotificationFeedbackGenerator().notificationOccurred(
                    UINotificationFeedbackType.UINotificationFeedbackTypeError,
                )

            HapticKind.Tick -> UISelectionFeedbackGenerator().selectionChanged()
        }
    }

    private fun impact(style: UIImpactFeedbackStyle) {
        UIImpactFeedbackGenerator(style = style).impactOccurred()
    }
}
