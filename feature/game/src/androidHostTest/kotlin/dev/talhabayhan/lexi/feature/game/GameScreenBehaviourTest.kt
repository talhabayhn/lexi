package dev.talhabayhan.lexi.feature.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterUi
import dev.talhabayhan.lexi.core.designsystem.theme.DefaultLexiDimens
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.feature.game.components.GameColumn
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class GameScreenBehaviourTest {

    /** Regression (review of 30c4bbf): `inputEnabled` was mapped but the controls stayed tappable during a reveal. */
    @Test
    fun `keys and backspace and pass and joker are disabled during the reveal`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.wrongReveal, clock = { GameSamples.designClock }, onIntent = { intents += it })
            }
        }

        onNodeWithText("S").assertIsNotEnabled().performClick()
        onNodeWithContentDescription("Harf sil").assertIsNotEnabled().performClick()
        onNodeWithContentDescription("Joker kullan").assertIsNotEnabled().performClick()
        onNodeWithText("PAS").assertIsNotEnabled().performClick()

        assertEquals(emptyList(), intents.filter { it !is GameIntent.Foreground && it !is GameIntent.Background })
    }

    @Test
    fun `controls are enabled while typing`() = runComposeUiTest {
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.design, clock = { GameSamples.designClock }, onIntent = {})
            }
        }

        onNodeWithText("S").assertIsEnabled()
        onNodeWithContentDescription("Harf sil").assertIsEnabled()
        onNodeWithContentDescription("Joker kullan").assertIsEnabled()
        onNodeWithText("PAS").assertIsEnabled()
    }

    @Test
    fun `a joker row uses its joker and a joker without inventory is disabled`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.jokerSheet, clock = {
                    GameSamples.jokerSheetClock
                }, onIntent = { intents += it })
            }
        }

        onNodeWithText("Harf aç").performClick()
        onNodeWithText("Telaffuzu dinle").assertIsNotEnabled()
        onNodeWithContentDescription("Kapat").performClick()
        onNodeWithText("Süre işlemeye devam ediyor · 1:31").assertExists()

        assertEquals(
            listOf(GameIntent.UseJoker(JokerType.REVEAL_LETTER), GameIntent.CloseJokers),
            intents.filter { it is GameIntent.UseJoker || it is GameIntent.CloseJokers },
        )
    }

    @Test
    fun `the pause dialog resumes and restarts and opens settings or confirms giving up`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.paused, clock = { GameSamples.designClock }, onIntent = { intents += it })
            }
        }

        onNodeWithText("DEVAM ET").performClick()
        onNodeWithText("YENİDEN BAŞLAT").performClick()
        onNodeWithText("AYARLAR").performClick()
        onNodeWithText("PES ET").performClick()

        assertEquals(
            listOf(GameIntent.Resume, GameIntent.RequestRestart, GameIntent.OpenSettings, GameIntent.RequestQuit),
            intents.filter { it !is GameIntent.Foreground && it !is GameIntent.Background },
        )
    }

    @Test
    fun `the restart confirmation restarts or goes back and the daily offers no restart`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        var state by mutableStateOf(GameSamples.restartConfirmation)
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(state, clock = { GameSamples.designClock }, onIntent = { intents += it })
            }
        }

        onNodeWithText("Başlat").performClick()
        onNodeWithText("Vazgeç").performClick()
        state = GameSamples.paused.copy(canRestart = false)
        waitForIdle()
        onNodeWithText("YENİDEN BAŞLAT").assertDoesNotExist()

        assertEquals(
            listOf(GameIntent.Restart, GameIntent.CancelRestart),
            intents.filter { it !is GameIntent.Foreground && it !is GameIntent.Background },
        )
    }

    /** Regression (review of 30c4bbf): the column reported more height than its constraints allow. */
    @Test
    fun `the game column never reports more height than its bounds`() = runComposeUiTest {
        setContent {
            Box(Modifier.size(390.dp, 300.dp)) {
                GameColumn(
                    gap = 16.dp,
                    answerTopMargin = 4.dp,
                    keysHeight = 166.dp,
                    keysActionsGap = 12.dp,
                    maxWheelSize = 320.dp,
                    hud = { Spacer(Modifier.height(48.dp)) },
                    wheel = { Spacer(Modifier.size(it)) },
                    clue = { Spacer(Modifier.height(120.dp)) },
                    answer = { Spacer(Modifier.height(48.dp)) },
                    keys = { Spacer(Modifier.height(108.dp)) },
                    actions = { Spacer(Modifier.height(56.dp)) },
                    modifier = Modifier.testTag(COLUMN),
                )
            }
        }

        onNodeWithTag(COLUMN).assertHeightIsEqualTo(300.dp)
    }

    @Test
    fun `screen readers go through the board as HUD then clue wheel answer keys actions`() = runComposeUiTest {
        setGame(GameSamples.design)

        // Top to bottom the rows are HUD, wheel, clue, answer, keys, actions; the reading order puts the clue first.
        val rows = listOf(
            GameTestTags.HUD,
            GameTestTags.WHEEL,
            GameTestTags.CLUE,
            GameTestTags.ANSWER,
            GameTestTags.KEYS,
            GameTestTags.ACTIONS,
        )
        val groups = rows.associateWith { tag ->
            val node = onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
            generateSequence(node) { it.parent }.first { SemanticsProperties.TraversalIndex in it.config }
        }
        val column = groups.values.map { it.parent }.distinctBy { it?.id }.single()
        assertEquals(true, column?.config?.getOrNull(SemanticsProperties.IsTraversalGroup))
        groups.values.forEach { assertEquals(true, it.config.getOrNull(SemanticsProperties.IsTraversalGroup)) }

        assertEquals(
            listOf(
                GameTestTags.HUD,
                GameTestTags.CLUE,
                GameTestTags.WHEEL,
                GameTestTags.ANSWER,
                GameTestTags.KEYS,
                GameTestTags.ACTIONS,
            ),
            rows.sortedBy { groups.getValue(it).config[SemanticsProperties.TraversalIndex] },
        )
    }

    @Test
    fun `the wheel is one summary node and the answer row one node`() = runComposeUiTest {
        setGame(GameSamples.design)

        onNodeWithContentDescription("Harf K, İLE BAŞLAR. 26 harf: 7 doğru, 1 yanlış, 2 pas, 16 kalan").assertExists()
        // The 26 chips (geometric order) are hidden from screen readers; their states are in the summary.
        val chips = onAllNodes(
            hasAnyAncestor(hasTestTag(GameTestTags.WHEEL)) and hasContentDescription(", ", substring = true) and
                !hasContentDescription("Harf", substring = true),
            useUnmergedTree = true,
        )
        chips.assertCountEquals(26)
        chips.assertAll(SemanticsMatcher.keyIsDefined(SemanticsProperties.HideFromAccessibility))
        onNodeWithTag(GameTestTags.ANSWER).assertContentDescriptionEquals("Cevap, 7 harf, 3 dolu: K I T")
    }

    @Test
    fun `the timer is labelled and never a live region`() = runComposeUiTest {
        var clock by mutableStateOf(GameSamples.designClock)
        setGame(GameSamples.design) { clock }

        onNodeWithContentDescription("Kalan süre: 1 dakika 47 saniye")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
        clock = GameSamples.warningClock
        waitForIdle()
        onNodeWithContentDescription("Kalan süre: 9 saniye")
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion))
    }

    @Test
    fun `the time warning is announced once and time-up after it`() = runComposeUiTest {
        var clock by mutableStateOf(GameSamples.designClock)
        setGame(GameSamples.design) { clock }
        val announcement = onNodeWithTag(GameTestTags.ANNOUNCEMENT)
        announcement.assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        assertEquals("", announcement.spoken())

        clock = clockAt(seconds = 10)
        waitForIdle()
        val warning = announcement.rawDescription()
        assertEquals("10 saniye kaldı", announcement.spoken())
        for (seconds in 9 downTo 1) {
            clock = clockAt(seconds)
            waitForIdle()
            // Unchanged text: the live region is not read again while the clock ticks.
            assertEquals(warning, announcement.rawDescription(), "at $seconds s")
        }
        clock = clockAt(seconds = 0)
        waitForIdle()
        assertEquals("Süre doldu", announcement.spoken())
    }

    @Test
    fun `answers and jokers are announced once each`() = runComposeUiTest {
        var state by mutableStateOf(GameSamples.jokerSheet.copy(isJokerSheetOpen = false))
        setGame(state = { state })
        val announcement = onNodeWithTag(GameTestTags.ANNOUNCEMENT)

        state = state.copy(
            jokers = state.jokers.map {
                if (it.type == JokerType.REVEAL_LETTER) it.copy(count = it.count - 1) else it
            }.toImmutableList(),
        )
        waitForIdle()
        assertEquals("Joker kullanıldı: Harf aç", announcement.spoken())

        state =
            state.copy(wheel = state.wheel.withStates('K' to WheelLetterState.Passed, 'L' to WheelLetterState.Current))
        waitForIdle()
        assertEquals("K: pas", announcement.spoken())
        val passed = announcement.rawDescription()

        // A state change without an event (typing) keeps the text, so nothing is read again.
        state = state.copy(score = state.score + 1)
        waitForIdle()
        assertEquals(passed, announcement.rawDescription())

        state =
            state.copy(wheel = state.wheel.withStates('L' to WheelLetterState.Correct, 'M' to WheelLetterState.Current))
        waitForIdle()
        assertEquals("L: doğru", announcement.spoken())
    }

    @Test
    fun `the board is hidden from screen readers while the pause dialog is open`() = runComposeUiTest {
        var state by mutableStateOf(GameSamples.paused)
        setGame(state = { state })

        onNodeWithTag(GameTestTags.HUD).assertDoesNotExist()
        onNodeWithContentDescription("Duraklat").assertDoesNotExist()
        onNodeWithText("PAS").assertDoesNotExist()
        onNodeWithTag(GameTestTags.ANNOUNCEMENT)
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.HideFromAccessibility))
        // The title is a heading and the first thing a screen reader reaches.
        onNodeWithText("Oyun duraklatıldı").assert(isHeading())
        assertEquals("Oyun duraklatıldı", firstSpokenText())

        state = GameSamples.design
        waitForIdle()
        onNodeWithTag(GameTestTags.HUD).assertExists()
        onNodeWithText("PAS").assertExists()
    }

    @Test
    fun `the board is hidden from screen readers while the joker sheet is open`() = runComposeUiTest {
        val intents = mutableListOf<GameIntent>()
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(GameSamples.jokerSheet, clock = {
                    GameSamples.jokerSheetClock
                }, onIntent = { intents += it })
            }
        }

        onNodeWithTag(GameTestTags.HUD).assertDoesNotExist()
        onNodeWithText("Joker kullan").assert(isHeading())
        onNodeWithContentDescription("Kapat").performClick()
        assertEquals(listOf(GameIntent.CloseJokers), intents.filterIsInstance<GameIntent.CloseJokers>())
    }

    @Test
    @Config(qualifiers = "tr-w320dp-h568dp-xhdpi")
    fun `every control of the compact board is at least the minimum touch target`() = runComposeUiTest {
        setGame(GameSamples.design)

        val controls = onAllNodes(hasClickAction()).fetchSemanticsNodes()
        // Pause, 10 keys, joker, backspace, PAS.
        assertEquals(14, controls.size)
        val min = with(density) { DefaultLexiDimens.minTouchTarget.toPx() }
        controls.forEach { node ->
            val size = node.size
            assertTrue(size.width >= min && size.height >= min, "${node.config} is ${size.width} x ${size.height}")
        }
    }

    @Test
    fun `the joker sheet and the pause dialog follow a 200 percent font size without cutting text`() {
        listOf(
            GameSamples.jokerSheet.copy(showJokerAd = true) to
                listOf("Harf aç", "Telaffuzu dinle", "Kısa reklam izle · +1 joker"),
            GameSamples.paused to listOf("DEVAM ET", "YENİDEN BAŞLAT", "AYARLAR", "PES ET"),
            GameSamples.restartConfirmation to listOf("Vazgeç", "Başlat"),
            GameSamples.quitConfirmation to listOf("Vazgeç", "Pes et"),
        ).forEach { (state, labels) ->
            runComposeUiTest {
                setContent {
                    val density = Density(LocalDensity.current.density, fontScale = 2f)
                    CompositionLocalProvider(LocalDensity provides density) {
                        LexiTheme(reducedMotion = true) {
                            GameScreen(state, clock = { GameSamples.jokerSheetClock }, onIntent = {})
                        }
                    }
                }
                labels.forEach { label ->
                    val text = onNodeWithText(label, useUnmergedTree = true).performScrollTo()
                    val layout = text.textLayout()
                    // Not capped like the board.
                    assertEquals(2f, layout.layoutInput.density.fontScale, label)
                    assertFalse(layout.didOverflowHeight, "$label overflows")
                    assertFalse(layout.isLineEllipsized(layout.lineCount - 1), "$label is cut")
                    // The label stays inside its button (or joker row).
                    val node = text.fetchSemanticsNode()
                    val button = generateSequence(node) { it.parent }.first { SemanticsActions.OnClick in it.config }
                    val inside = button.boundsInRoot.inflate(1f)
                    assertTrue(inside.contains(node.boundsInRoot.topLeft), "$label starts outside its button")
                    assertTrue(inside.contains(node.boundsInRoot.bottomRight), "$label ends outside its button")
                }
            }
        }
    }

    private fun ComposeUiTest.setGame(state: GameUiState, clock: () -> GameClockUi = { GameSamples.designClock }) =
        setGame({ state }, clock)

    private fun ComposeUiTest.setGame(
        state: () -> GameUiState,
        clock: () -> GameClockUi = { GameSamples.designClock },
    ) {
        setContent {
            LexiTheme(reducedMotion = true) {
                GameScreen(state(), clock = clock, onIntent = {})
            }
        }
    }

    /** The first node a screen reader reaches in a linear sweep: the first speaking, visible node in tree order. */
    private fun ComposeUiTest.firstSpokenText(): String? {
        fun SemanticsNode.speaking(): Sequence<SemanticsNode> = sequence {
            val config = this@speaking.config
            val speaks = SemanticsProperties.Text in config ||
                config.getOrNull(SemanticsProperties.ContentDescription).orEmpty().any { it.isNotEmpty() }
            if (SemanticsProperties.HideFromAccessibility !in config && speaks) yield(this@speaking)
            children.forEach { yieldAll(it.speaking()) }
        }
        // The merged tree, like a screen reader: no descendants of cleared nodes, merged buttons.
        val first = onRoot().fetchSemanticsNode().speaking().first()
        return first.config.getOrNull(SemanticsProperties.Text)?.joinToString()
            ?: first.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
    }

    private fun SemanticsNodeInteraction.rawDescription(): String =
        fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()

    /** The announcement as spoken (without the silent mark that lets an identical text be read again). */
    private fun SemanticsNodeInteraction.spoken(): String = rawDescription().removeSuffix("​")

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single()
    }

    private fun List<WheelLetterUi>.withStates(vararg changes: Pair<Char, WheelLetterState>) =
        changes.toMap().let { byLetter -> map { it.copy(state = byLetter[it.letter] ?: it.state) }.toImmutableList() }

    private fun clockAt(seconds: Int): GameClockUi = GameClockUi(
        isTimed = true,
        remainingMs = seconds * 1_000L,
        fraction = seconds / 180f,
        wholeSeconds = seconds,
        isWarning = seconds <= 10,
    )

    private companion object {
        const val COLUMN = "column"
    }
}
