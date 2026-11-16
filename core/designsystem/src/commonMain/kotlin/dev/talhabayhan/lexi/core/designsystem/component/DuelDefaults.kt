package dev.talhabayhan.lexi.core.designsystem.component

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Geometry of the hot-seat duel screens: `17-duel-setup`, `18-duel-handover` and
 * `20-duel-result`. Spacing that equals a `LexiDimens` gap or a border token is read from there instead.
 */
public object DuelDefaults {
    // 17-duel-setup

    /** Blue circle 140 at `right: -60px; top: 90px`. */
    public val SetupCircleSize: Dp = 140.dp
    public val SetupCircleBleed: Dp = 60.dp
    public val SetupCircleTop: Dp = 90.dp

    /**
     * The headline "Sırayla yarışın" stays on one line: it keeps clear of the circle's visible 80dp (minus the 20dp
     * screen padding), shrinks down to 28sp (the size at font scale 1, it does not grow) and follows the system font
     * size up to 1.3× like the other headings.
     */
    public val SetupTitleEndReserve: Dp = 60.dp
    public val SetupTitleMinSize: TextUnit = 28.sp
    public const val SETUP_TITLE_FONT_SCALE_CAP: Float = 1.3f

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

    /**
     * End inset of the winner / draw headline so it never runs under the circle: the circle shows 86dp at the end
     * (176 − 90), minus the 20dp screen padding, plus an 8dp gap. The design breaks "1. Oyuncu / kazandı" there too.
     */
    public val ResultTitleEndInset: Dp = 74.dp

    /** Smallest scale the headline shrinks to (two lines) before it ellipsizes: 16-character single-word names. */
    public const val RESULT_TITLE_MIN_SCALE: Float = 0.5f

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
