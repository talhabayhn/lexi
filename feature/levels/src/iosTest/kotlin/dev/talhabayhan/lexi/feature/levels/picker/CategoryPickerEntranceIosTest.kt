package dev.talhabayhan.lexi.feature.levels.picker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.motion.LocalMotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionPolicy
import dev.talhabayhan.lexi.core.designsystem.motion.MotionTier
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Regression (iPhone 13 / 17, 1.1.2): "Çarkını seç" showed its header and level tabs but no category cards until the
 * app went to the background and back. The grid entrance (#18) faded each card in a `graphicsLayer` with `outsets`,
 * which Skia (iOS) drew with the alpha of the parent's last recording: 0. After the entrance the cards must be drawn
 * as fully as without one. Runs on the iOS simulator's Skia renderer (`iosSimulatorArm64Test`).
 */
@OptIn(ExperimentalTestApi::class)
class CategoryPickerEntranceIosTest {

    private val state = CategoryPickerUiState(
        level = Level.B2,
        categories = persistentListOf(
            PickerCategoryUi(CategoryId.MIXED, "Karışık", "Mixed", 412, 850),
            PickerCategoryUi(CategoryId("daily-life"), "Günlük hayat", "Daily life", 120, 240),
            PickerCategoryUi(CategoryId("business"), "İş İngilizcesi", "Business English", 40, 125),
            PickerCategoryUi(CategoryId("travel"), "Seyahat", "Travel", 20, 53),
        ),
        isLoaded = true,
    )

    /** Share of pixels in the grid's band that are not the screen's cream, once everything has settled. */
    private fun drawnShare(entrance: Boolean): Float {
        var share = 0f
        runComposeUiTest {
            val policy = MotionPolicy().apply { forcedTier = MotionTier.Full }
            var cream = Color.Unspecified
            setContent {
                CompositionLocalProvider(LocalMotionPolicy provides policy) {
                    LexiTheme(reducedMotion = false) {
                        cream = LexiTheme.colors.cream
                        Box(Modifier.size(390.dp, 844.dp)) {
                            CategoryPickerScreen(state, onIntent = {}, entrance = entrance)
                        }
                    }
                }
            }
            // Past the whole entrance (8 cards, staggered), on the test's frame clock.
            mainClock.advanceTimeBy(ENTRANCE_PLUS_MARGIN_MILLIS)
            waitForIdle()
            val image = onRoot().captureToImage().toPixelMap()
            var drawn = 0
            var total = 0
            for (y in (image.height * 0.3f).toInt() until (image.height * 0.7f).toInt() step 4) {
                for (x in 0 until image.width step 4) {
                    total++
                    val c = image[x, y]
                    if (abs(c.red - cream.red) + abs(c.green - cream.green) + abs(c.blue - cream.blue) > 0.06f) drawn++
                }
            }
            share = drawn.toFloat() / total
        }
        return share
    }

    @Test
    fun theCardsAreDrawnAfterTheGridEntrance() {
        val withoutEntrance = drawnShare(entrance = false)
        val withEntrance = drawnShare(entrance = true)
        assertTrue(withoutEntrance > 0.1f, "control: the cards cover part of the band ($withoutEntrance)")
        assertTrue(
            abs(withEntrance - withoutEntrance) < 0.01f,
            "after the entrance the cards must look as without it: $withEntrance vs $withoutEntrance",
        )
    }

    private companion object {
        const val ENTRANCE_PLUS_MARGIN_MILLIS = 3_000L
    }
}
