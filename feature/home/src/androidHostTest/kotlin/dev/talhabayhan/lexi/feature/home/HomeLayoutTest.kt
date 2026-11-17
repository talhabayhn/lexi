package dev.talhabayhan.lexi.feature.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.text.LocalTextLayoutProbe
import dev.talhabayhan.lexi.core.designsystem.text.WordSplitRecorder
import dev.talhabayhan.lexi.core.designsystem.text.textWordProblems
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.LexiViewport
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertTrue

/**
 * Home at 320 × 568, 360 × 640, 390 × 844 and 412 × 915, font scale 1.0 / 1.3 / 2.0, Turkish and English: the play
 * card and the bottom bar are fully on screen without scrolling (the rest scrolls), every button is at least 48dp high
 * (`video-flow-notes.md` §1; the onboarding lesson: primary actions never cut off), and no label (points, mode boxes,
 * badge, bar labels, tiles) breaks a word across lines or is cut off.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class HomeLayoutChecks(private val play: String, private val barItems: List<String>) {
    @Test
    @Config(qualifiers = "+w320dp-h568dp")
    fun w320h568() = check()

    @Test
    @Config(qualifiers = "+w360dp-h640dp")
    fun w360h640() = check()

    @Test
    @Config(qualifiers = "+w390dp-h844dp")
    fun w390h844() = check()

    @Test
    @Config(qualifiers = "+w412dp-h915dp")
    fun w412h915() = check()

    private fun check() {
        check(HomeSampleState)
        // The completed daily card: the longer description and the "Tamamlandı" badge.
        val played = DailyCardState.AlreadyPlayed(score = 1840, hoursUntilNext = 5, minutesUntilNext = 12)
        check(HomeSampleState.copy(dailyState = played))
    }

    private fun check(state: HomeUiState) = runComposeUiTest {
        val recorder = WordSplitRecorder()
        setContent {
            CompositionLocalProvider(LocalTextLayoutProbe provides recorder) {
                LexiTheme(reducedMotion = true) { LexiViewport { HomeScreen(state, onIntent = {}) } }
            }
        }
        val rootHeight = onRoot().fetchSemanticsNode().size.height.toFloat()
        val bars = barItems.map { label ->
            onNode(hasContentDescription(label).or(hasText(label))).fetchSemanticsNode().also { node ->
                assertOnScreen(node, label, bottomLimit = rootHeight)
            }
        }
        // The play card ends above the bottom bar: visible without scrolling.
        val barTop = bars.minOf { it.positionInRoot.y }
        assertOnScreen(onNode(hasContentDescription(play, substring = true)).fetchSemanticsNode(), play, barTop)
        assertButtonsTallEnough()
        assertNoWordSplits(recorder)
    }

    /** Every label, also those hidden behind a component's content description, keeps its words whole. */
    private fun ComposeUiTest.assertNoWordSplits(recorder: WordSplitRecorder) {
        waitForIdle()
        val problems = recorder.problems + onRoot(useUnmergedTree = true).fetchSemanticsNode().textWordProblems()
        assertTrue(problems.isEmpty(), "words broken or cut: ${problems.distinct()}")
    }

    private fun assertOnScreen(node: SemanticsNode, label: String, bottomLimit: Float) {
        val top = node.positionInRoot.y
        val bottom = top + node.size.height
        assertTrue(top >= 0f, "$label starts above the screen: $top")
        assertTrue(bottom <= bottomLimit, "$label is cut off: bottom $bottom > $bottomLimit")
    }

    private fun ComposeUiTest.assertButtonsTallEnough() {
        val minPx = MIN_TOUCH_DP * onRoot().fetchSemanticsNode().layoutInfo.density.density
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.OnClick)).fetchSemanticsNodes().forEach { node ->
            val label = node.config.getOrNull(SemanticsProperties.ContentDescription)
                ?: node.config.getOrNull(SemanticsProperties.Text)
            assertTrue(node.size.height >= minPx - 1f, "$label is only ${node.size.height}px high")
        }
    }

    private companion object {
        const val MIN_TOUCH_DP = 48f
    }
}

private const val PLAY_TR = "Oyna."
private const val PLAY_EN = "Play."
private val BAR_TR = listOf("İstatistik", "Arkadaşını davet et", "Ayarlar")
private val BAR_EN = listOf("Stats", "Invite a friend", "Settings")

@Config(sdk = [36], qualifiers = "tr-xhdpi")
class HomeLayoutTr100Test : HomeLayoutChecks(PLAY_TR, BAR_TR)

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 1.3f)
class HomeLayoutTr130Test : HomeLayoutChecks(PLAY_TR, BAR_TR)

@Config(sdk = [36], qualifiers = "tr-xhdpi", fontScale = 2.0f)
class HomeLayoutTr200Test : HomeLayoutChecks(PLAY_TR, BAR_TR)

@Config(sdk = [36], qualifiers = "en-xhdpi")
class HomeLayoutEn100Test : HomeLayoutChecks(PLAY_EN, BAR_EN)

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 1.3f)
class HomeLayoutEn130Test : HomeLayoutChecks(PLAY_EN, BAR_EN)

@Config(sdk = [36], qualifiers = "en-xhdpi", fontScale = 2.0f)
class HomeLayoutEn200Test : HomeLayoutChecks(PLAY_EN, BAR_EN)
