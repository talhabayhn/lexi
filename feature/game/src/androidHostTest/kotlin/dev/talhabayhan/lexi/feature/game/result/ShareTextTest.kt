package dev.talhabayhan.lexi.feature.game.result

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import dev.talhabayhan.lexi.feature.game.result.share.ShareCardSamples
import dev.talhabayhan.lexi.feature.game.result.share.shareLink
import dev.talhabayhan.lexi.feature.game.result.share.shareText
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertEquals

/** The share link (card footer and share text) is the landing page in the UI language. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w390dp-h844dp-xhdpi")
class ShareTextTest {
    @Test
    fun `a Turkish UI shares the Turkish landing page`() = runComposeUiTest {
        var link = ""
        var text = ""
        setContent {
            link = shareLink()
            text = shareText(ShareCardSamples.design)
        }
        waitForIdle()
        assertEquals("https://talhabayhn.github.io/lexi/tr/", link)
        assertEquals(true, text.endsWith("Lexi'de oynuyorum: $link"), text)
    }

    @Test
    @Config(qualifiers = "en-w390dp-h844dp-xhdpi")
    fun `an English UI shares the English landing page`() = runComposeUiTest {
        var link = ""
        var text = ""
        setContent {
            link = shareLink()
            text = shareText(ShareCardSamples.design)
        }
        waitForIdle()
        assertEquals("https://talhabayhn.github.io/lexi/", link)
        assertEquals(true, text.endsWith("Playing on Lexi: $link"), text)
    }
}
