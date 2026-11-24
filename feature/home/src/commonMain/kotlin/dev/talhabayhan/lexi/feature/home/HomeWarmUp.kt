package dev.talhabayhan.lexi.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.talhabayhan.lexi.core.designsystem.component.inertWarmUp

/**
 * One-time warm-up of Home: the design sample ([HomeSampleState]) composed, measured and drawn like the real screen
 * (header logo, play card, rows, ruled bar, their shadows), so its first-use GPU pipelines and glyphs at their real
 * size are built before Home's first frame. The splash draws it hidden on the first launch of each version
 * (`SplashRoute`, `docs/PERF.md`). Inert: no semantics, no touch, no focus; its intents go nowhere.
 */
@Composable
public fun HomeWarmUp(modifier: Modifier = Modifier) {
    Box(modifier.inertWarmUp()) {
        HomeScreen(state = HomeSampleState, onIntent = {}, noticeVisible = false)
    }
}
