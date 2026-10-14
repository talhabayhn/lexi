package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The game components laid out like `04-game.html` (padding 20/20/28, gap 16) at xhdpi, i.e. the scale of
 * `04-game.png`, so every component can be compared with its crop of the design. Not the game screen (M4).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w390dp-h844dp-xhdpi")
class GameSampleScreenshotTest {

    @Test
    fun gameComponents() {
        captureRoboImage("src/androidHostTest/screenshots/GameComponents.png") {
            LexiTheme(reducedMotion = false) { GameSample() }
        }
    }
}

internal val GameWheelLetters: List<WheelLetterUi> = ('A'..'Z').mapIndexed { i, c ->
    val state = when {
        i == 2 || i == 7 -> WheelLetterState.Passed
        i == 4 -> WheelLetterState.Wrong
        i < 10 -> WheelLetterState.Correct
        i == 10 -> WheelLetterState.Current
        else -> WheelLetterState.Todo
    }
    WheelLetterUi(c, state)
}

@Composable
private fun GameSample() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.height(48.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NavSquareButton(
                LexiIcons.Pause,
                "Duraklat",
                onClick = {},
                size = IconButtonDefaults.NavSizeLarge,
                iconSize = IconButtonDefaults.IconSizeSmall,
                containerColor = LexiTheme.colors.cream,
            )
            TimerBar(progress = 0.62f, timeText = "1:47", modifier = Modifier.weight(1f))
            ScoreBadge("120")
        }
        LetterWheel(
            letters = GameWheelLetters,
            centerLetter = 'K',
            ruleLabel = "İLE BAŞLAR",
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        ClueCard(label = "İPUCU · İSİM", clue = "mutfak", level = "A1", letterCount = "7 HARF")
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(AnswerSlotDefaults.Gap, Alignment.CenterHorizontally),
        ) {
            "KIT".forEach { AnswerSlot(it, AnswerSlotState.Filled) }
            AnswerSlot(null, AnswerSlotState.Active)
            repeat(3) { AnswerSlot(null, AnswerSlotState.Empty) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(LetterKeyDefaults.GridGap)) {
            "CHENSAYORU".chunked(LetterKeyDefaults.COLUMNS).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(LetterKeyDefaults.GridGap)) {
                    row.forEach { LetterKey(it, onClick = {}, modifier = Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ActionCircleButton(LexiIcons.Bulb, "Joker kullan", onClick = {})
            IconSquareButton(LexiIcons.Backspace, "Harf sil", onClick = {})
            LexiButton(
                text = "PAS",
                onClick = {},
                variant = LexiButtonVariant.Pass,
                textStyle = LexiTheme.typography.button22,
                height = LexiButtonDefaults.LargeHeight,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
