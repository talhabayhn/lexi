package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A visually hidden node that makes screen readers announce [text] whenever it changes (live region, polite: it waits
 * for the current speech, never interrupts). Use it sparingly for events that have no focusable node of their own
 * (e.g. "correct", a timer milestone); never for continuous values such as a ticking clock. Empty [text] announces
 * nothing. The node is 1dp (a zero-size node can be dropped from the accessibility tree) and draws nothing.
 */
@Composable
public fun LiveAnnouncement(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(1.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = text
            },
    )
}
