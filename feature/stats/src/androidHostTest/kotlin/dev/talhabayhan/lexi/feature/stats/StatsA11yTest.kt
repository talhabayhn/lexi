package dev.talhabayhan.lexi.feature.stats

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screen-reader semantics: the screen title is a heading. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class StatsA11yTest {
    @Test
    fun `the title is a heading`() = runComposeUiTest {
        setContent { LexiTheme(reducedMotion = true) { StatsScreen(StatsSampleState, onIntent = {}) } }

        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertAny(hasText("İstatistikler"))
        onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)).assertAny(hasText("SEVİYE İLERLEMESİ"))
    }
}
