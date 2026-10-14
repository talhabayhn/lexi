package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
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
        Box(Modifier.fillMaxWidth().height(48.dp))
        LetterWheel(
            letters = GameWheelLetters,
            centerLetter = 'K',
            ruleLabel = "İLE BAŞLAR",
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}
