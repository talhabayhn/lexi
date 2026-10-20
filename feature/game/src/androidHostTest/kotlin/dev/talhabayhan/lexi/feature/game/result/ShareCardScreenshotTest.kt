package dev.talhabayhan.lexi.feature.game.result

import com.github.takahirom.roborazzi.captureRoboImage
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.share.ShareCard
import dev.talhabayhan.lexi.feature.game.result.share.ShareCard
import dev.talhabayhan.lexi.feature.game.result.share.ShareCardSamples
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** `docs/design/screens/12-share-card.png` at its size and scale (432 × 540dp, xhdpi = 2×), Turkish like the design. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "tr-w432dp-h540dp-xhdpi")
class ShareCardScreenshotTest {

    /** The design: "KLASİK · 14 EKİM 2026", 21/26, "A1 · 34 sn kala bitti · 7 gün seri", with the link placeholder. */
    @Test
    fun designSample() = capture("ShareCard", ShareCardSamples.design, ShareCardSamples.DESIGN_LINK)

    /** Daily: "GÜNÜN ÇARKI", no level segment; no link yet (`StoreLinks.SHARE_LINK_LABEL` is blank). */
    @Test
    fun dailyWithoutLink() = capture("ShareCardDaily", ShareCardSamples.daily, link = "")

    /** Time up without a streak: "süre bitti", unanswered letters as cream circles. */
    @Test
    fun timeUp() = capture("ShareCardTimeUp", ShareCardSamples.timeUp, link = "")

    @Test
    @Config(qualifiers = "en-w432dp-h540dp-xhdpi")
    fun designSampleEnglish() = capture("ShareCardEnglish", ShareCardSamples.design, ShareCardSamples.DESIGN_LINK)

    @Test
    @Config(qualifiers = "en-w432dp-h540dp-xhdpi")
    fun dailyEnglish() = capture("ShareCardDailyEnglish", ShareCardSamples.daily, link = "")

    private fun capture(name: String, card: ShareCard, link: String) {
        captureRoboImage("src/androidHostTest/screenshots/$name.png") {
            LexiTheme(reducedMotion = false) { ShareCard(card, link = link) }
        }
    }
}
