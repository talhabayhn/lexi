package dev.talhabayhan.lexi.feature.notebook

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * English store stills, not regression tests: the notebook with English meanings in place of the app's Turkish
 * clues (owner's choice: English listings show no Turkish text). Written to `build/store-screens/en/` for
 * `tools/store-graphics`; runs only with `LEXI_STORE_FOOTAGE=1`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "en-w390dp-h844dp-xhdpi")
class StoreScreensCapture {

    @Before
    fun onlyForStore() = assumeTrue(System.getenv("LEXI_STORE_FOOTAGE") == "1")

    @Test
    fun notebook() {
        captureRoboImage("$DIR/Notebook.png") {
            LexiTheme(reducedMotion = false) { NotebookScreen(state = ENGLISH_STATE, onIntent = {}) }
        }
    }

    @Test
    fun wordSheet() {
        captureRoboImage("$DIR/NotebookWordSheet.png") {
            LexiTheme(reducedMotion = true) {
                NotebookScreen(
                    state = ENGLISH_STATE.copy(
                        detail = NotebookSampleDetail.copy(word = ENGLISH_STATE.words[2], exampleTranslation = ""),
                    ),
                    onIntent = {},
                )
            }
        }
    }

    private companion object {
        const val DIR = "build/store-screens/en"

        // Store sample content, not user-facing app strings.
        val MEANINGS = mapOf(
            "egg" to "you can boil or fry it",
            "mirror" to "you see yourself in it",
            "chair" to "a seat for one person",
            "horse" to "a large animal you can ride",
            "yesterday" to "the day before today",
            "neighbour" to "a person who lives next door",
        )

        val ENGLISH_STATE = NotebookSampleState.copy(
            words = NotebookSampleWords.map { it.copy(clue = MEANINGS.getValue(it.word)) }.toImmutableList(),
        )
    }
}
