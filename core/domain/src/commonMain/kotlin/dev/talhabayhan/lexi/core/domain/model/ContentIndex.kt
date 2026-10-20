package dev.talhabayhan.lexi.core.domain.model

/**
 * Slim, clue-free view of the level cards (no exam-pack-only cards): what the level select and the stats need to
 * relate progress to levels and letters without loading every card with its clue text.
 */
public class ContentIndex(
    /** Card count per level. */
    public val totals: Map<Level, Int>,
    /** Level of every level card. */
    public val levelOf: Map<CardId, Level>,

    /** Wheel letter of every level card. */
    public val letterOf: Map<CardId, Char>,
)
