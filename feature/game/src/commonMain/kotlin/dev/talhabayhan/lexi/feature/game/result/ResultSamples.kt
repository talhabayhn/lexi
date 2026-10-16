package dev.talhabayhan.lexi.feature.game.result

import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.persistentListOf

/** The design's sample data (`06-result.html`) for previews and screenshot tests; content, not UI copy. */
internal object ResultSamples {
    // design sample, not user-facing
    private val chair = ReviewRowUi('C', "chair", "sandalye", "Sit on the chair.", ReviewOutcome.Passed)
    private val egg = ReviewRowUi('E', "egg", "yumurta", "I eat an egg.", ReviewOutcome.Wrong)
    private val horse = ReviewRowUi('H', "horse", "at", "The horse runs.", ReviewOutcome.Passed)
    private val mirror = ReviewRowUi('M', "mirror", "ayna", "Look in the mirror.", ReviewOutcome.Wrong)

    /** `06-result.png`: A1 classic, wheel done, 340 points, best streak ×6, 0:12 left, 19 / 3 / 4, 7 to review. */
    val design: ResultUiState = ResultUiState(
        level = Level.A1,
        mode = GameMode.CLASSIC,
        reason = GameOverReason.ALL_ANSWERED,
        score = 340,
        longestStreak = 6,
        remainingSeconds = 12,
        correct = 19,
        wrong = 3,
        passed = 4,
        addedToNotebook = 7,
        rows = persistentListOf(chair, egg, horse, mirror),
        hiddenRows = 3,
        isLoading = false,
    )

    /** Practice (design debt): unscored, untimed; the header shows the 19 correct answers instead of "0 puan". */
    val practice: ResultUiState = design.copy(mode = GameMode.PRACTICE, score = 0, remainingSeconds = null)

    /** The review cards could not be read (design debt): the list offers "Tekrar dene". */
    val reviewError: ResultUiState = design.copy(rows = persistentListOf(), hiddenRows = 0, reviewError = true)

    /** Long texts (English UI): a B2 hardcore game lost on the first miss, multi-word answers, wrapping examples. */
    val longText: ResultUiState = ResultUiState(
        level = Level.B2,
        mode = GameMode.HARDCORE,
        reason = GameOverReason.WRONG_IN_HARDCORE,
        score = 1_284,
        longestStreak = 12,
        remainingSeconds = 97,
        correct = 14,
        wrong = 1,
        passed = 11,
        addedToNotebook = 12,
        rows = persistentListOf(
            ReviewRowUi(
                'G',
                "get along with",
                "biriyle iyi geçinmek, anlaşmak",
                "My sister and I didn't always get along with each other as children.",
                ReviewOutcome.Wrong,
            ),
            ReviewRowUi(
                'N',
                "nevertheless",
                "yine de, buna rağmen",
                "The weather was terrible; nevertheless, the match went ahead as planned.",
                ReviewOutcome.Passed,
            ),
            ReviewRowUi(
                'W',
                "whereabouts",
                "bulunduğu yer",
                "Police are still trying to find out the whereabouts of the missing painting.",
                ReviewOutcome.Passed,
            ),
            ReviewRowUi('X', "anxiety", "kaygı", "Exams can cause a lot of anxiety.", ReviewOutcome.Passed),
        ),
        hiddenRows = 8,
        isLoading = false,
    )
}
