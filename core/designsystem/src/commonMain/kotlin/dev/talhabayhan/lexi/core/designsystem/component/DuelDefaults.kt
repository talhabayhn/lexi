package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Geometry of the hot-seat duel screens: `02b-home-duel` (the Home box), `17-duel-setup`, `18-duel-handover` and
 * `20-duel-result`. Spacing that equals a `LexiDimens` gap or a border token is read from there instead.
 */
public object DuelDefaults {
    // 02b-home-duel: the "İki kişi, tek telefon" box.
    /** `height: 84px`. */
    public val HomeBoxHeight: Dp = 84.dp

    /** DESIGN-DEBT: Compact / Tight Home tiers (derived: the box shrinks with the other tiers' tiles). */
    public val HomeBoxCompactHeight: Dp = 64.dp
    public val HomeBoxTightHeight: Dp = 44.dp

    /** DESIGN-DEBT: the one-line title of the tightest tier shrinks from 18sp down to this. */
    public val HomeBoxMinTitleSize: TextUnit = 13.sp
    public val HomeBoxTitleStep: TextUnit = 0.5.sp

    /** `padding: 0 16px; gap: 14px`. */
    public val HomeBoxPaddingHorizontal: Dp = 16.dp
    public val HomeBoxGap: Dp = 14.dp

    /** Marker: a 52 × 30 box with a 26dp square at left 0 and a 26dp circle at left 22, both 2dp from the top. */
    public val HomeBoxMarkerWidth: Dp = 52.dp
    public val HomeBoxMarkerHeight: Dp = 30.dp
    public val HomeBoxMarkerSize: Dp = 26.dp
    public val HomeBoxMarkerTop: Dp = 2.dp
    public val HomeBoxMarkerCircleStart: Dp = 22.dp

    /** Chevron `width="22"`. */
    public val HomeBoxChevronSize: Dp = 22.dp

    // 17-duel-setup

    /** Blue circle 140 at `right: -60px; top: 90px`. */
    public val SetupCircleSize: Dp = 140.dp
    public val SetupCircleBleed: Dp = 60.dp
    public val SetupCircleTop: Dp = 90.dp

    /** Red half pill 60 × 120 at `left: -30px; bottom: 200px`. */
    public val SetupPillWidth: Dp = 60.dp
    public val SetupPillHeight: Dp = 120.dp
    public val SetupPillBleed: Dp = 30.dp
    public val SetupPillBottom: Dp = 200.dp

    /** Player card: `padding: 12px 14px`, gap 12, marker 34 (border-box, 2dp border). */
    public val PlayerCardPaddingHorizontal: Dp = 14.dp
    public val PlayerCardPaddingVertical: Dp = 12.dp
    public val PlayerMarkerSize: Dp = 34.dp

    /** Rules note: leaf marker 14 (border-box, 2dp border) with `margin-top: 3px`, gap 10. */
    public val RulesMarkerSize: Dp = 14.dp
    public val RulesMarkerTop: Dp = 3.dp

    /** BAŞLA: `height: 72px`. */
    public val SetupStartHeight: Dp = 72.dp

    // 18-duel-handover

    /** Blue circle 220 at `right: -70px; top: -60px`. */
    public val HandoverCircleSize: Dp = 220.dp
    public val HandoverCircleEndBleed: Dp = 70.dp
    public val HandoverCircleTopBleed: Dp = 60.dp

    /** Red half pill 80 × 160 at `left: -40px; bottom: 230px`. */
    public val HandoverPillWidth: Dp = 80.dp
    public val HandoverPillHeight: Dp = 160.dp
    public val HandoverPillBleed: Dp = 40.dp
    public val HandoverPillBottom: Dp = 230.dp

    /** Eyebrow `margin-top: 8px`, yellow square 16. */
    public val HandoverEyebrowTop: Dp = 8.dp
    public val HandoverEyebrowMarkerSize: Dp = 16.dp

    /** Turn block `margin-top: 40px`; stat boxes `margin-top: 12px`, gap 10, `padding: 12px`. */
    public val HandoverTurnTop: Dp = 40.dp
    public val HandoverStatsTop: Dp = 12.dp
    public val HandoverStatPadding: Dp = 12.dp

    /** HAZIRIM: `height: 76px`; PES ET is drawn at 70 % opacity. */
    public val HandoverReadyHeight: Dp = 76.dp
    public const val HANDOVER_QUIT_ALPHA: Float = 0.7f

    /** Smallest scale the player name shrinks to on one line before it wraps (12-character names at 64sp). */
    public const val HANDOVER_NAME_MIN_SCALE: Float = 0.5f

    // 20-duel-result

    /** Yellow circle 170 + 2 × 3 border at `right: -90px; top: -80px`. */
    public val ResultCircleSize: Dp = 176.dp
    public val ResultCircleEndBleed: Dp = 90.dp
    public val ResultCircleTopBleed: Dp = 80.dp

    /** Headline spacing: score line `margin-top: 4px`, explanation `margin-top: 2px`. */
    public val ResultScoreTop: Dp = 4.dp
    public val ResultExplanationTop: Dp = 2.dp

    /** Player card: `padding: 12px 14px`, gap 10; marker 24 (border-box, 2dp border). */
    public val ResultCardPaddingHorizontal: Dp = 14.dp
    public val ResultCardPaddingVertical: Dp = 12.dp
    public val ResultMarkerSize: Dp = 24.dp

    /** KAZANAN badge `padding: 3px 8px`. */
    public val ResultBadgePaddingHorizontal: Dp = 8.dp
    public val ResultBadgePaddingVertical: Dp = 3.dp

    /** Stats row gap 16; outcome strip: 10dp shapes (border-box, 1dp border), gap 2. */
    public val ResultStatsGap: Dp = 16.dp
    public val ResultStripSize: Dp = 10.dp
    public val ResultStripBorder: Dp = 1.dp
    public val ResultStripGap: Dp = 2.dp

    /** TEKRAR OYNA `height: 64px`; "Ana menüye dön" `padding: 8px`. */
    public val ResultPlayAgainHeight: Dp = 64.dp
    public val ResultHomeLinkPadding: Dp = 8.dp
}
