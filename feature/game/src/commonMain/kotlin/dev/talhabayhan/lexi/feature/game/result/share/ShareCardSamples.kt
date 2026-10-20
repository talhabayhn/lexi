package dev.talhabayhan.lexi.feature.game.result.share

import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.share.ShareCaption
import dev.talhabayhan.lexi.core.domain.share.ShareCard
import dev.talhabayhan.lexi.core.domain.share.ShareCell
import dev.talhabayhan.lexi.core.domain.share.SlotOutcome
import dev.talhabayhan.lexi.core.domain.time.CivilDate

/** The design's sample data (`12-share-card.html`) for previews and screenshot tests; content, not UI copy. */
internal object ShareCardSamples {
    /** The design's footer placeholder; the app shows `StoreLinks.SHARE_LINK_LABEL` (blank for now). */
    const val DESIGN_LINK: String = "[MAĞAZA LİNKİ]"

    private val passed = setOf('C', 'H', 'X')
    private val wrong = setOf('E', 'M', 'Q')

    /** A–Z as in the design: C, H, X passed; E, M, Q wrong; the rest correct. */
    val designCells: List<ShareCell> = ('A'..'Z').map { letter ->
        val outcome = when (letter) {
            in passed -> SlotOutcome.PASSED
            in wrong -> SlotOutcome.WRONG
            else -> SlotOutcome.CORRECT
        }
        ShareCell(letter, outcome)
    }

    /**
     * `12-share-card.png`: 14 October 2026, "21/26", 34 s left, 7-day streak (the design's score; its grid has 20
     * blue cells). Classic A1, so the details line reads "A1 · 34 sn kala bitti · 7 gün seri" like the design.
     */
    val design: ShareCard = ShareCard(
        mode = GameMode.CLASSIC,
        level = Level.A1,
        date = CivilDate(2026, 10, 14),
        correct = 21,
        total = 26,
        remainingSeconds = 34,
        timeUp = false,
        streakDays = 7,
        cells = designCells,
        caption = ShareCaption.Time(34),
    )

    /** The Daily wheel of the design's header ("GÜNÜN ÇARKI"): no level segment. */
    val daily: ShareCard = design.copy(mode = GameMode.DAILY, level = null, caption = ShareCaption.Daily(21))

    /** A time-up game without a streak and with never-answered letters (cream circles). */
    val timeUp: ShareCard = design.copy(
        correct = 15,
        remainingSeconds = 0,
        timeUp = true,
        streakDays = 0,
        cells = designCells.mapIndexed { i, cell ->
            if (i >= FIRST_UNANSWERED) cell.copy(outcome = SlotOutcome.TIME_UP) else cell
        },
        caption = ShareCaption.Score(15, 26),
    )

    private const val FIRST_UNANSWERED = 20
}
