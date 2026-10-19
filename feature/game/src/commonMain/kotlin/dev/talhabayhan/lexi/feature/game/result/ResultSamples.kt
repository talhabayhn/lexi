package dev.talhabayhan.lexi.feature.game.result

import dev.talhabayhan.lexi.core.designsystem.component.ReviewOutcome
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.GameOverReason
import dev.talhabayhan.lexi.core.domain.model.Level
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/** The design's sample data (`06-result.html`) for previews and screenshot tests; content, not UI copy. */
internal object ResultSamples {

    private fun sampleState(
        level: Level = Level.A1,
        mode: GameMode = GameMode.CLASSIC,
        reason: GameOverReason = GameOverReason.ALL_ANSWERED,
        score: Int = 0,
        longestStreak: Int = 0,
        remainingSeconds: Int? = null,
        groups: ImmutableList<ReviewGroupUi> = persistentListOf(),
        isLoading: Boolean = false,
        reviewError: Boolean = false,
    ): ResultUiState {
        val correctCount = groups.find { it.outcome == ReviewOutcome.Correct }?.rows?.size ?: 0
        val wrongCount = groups.find { it.outcome == ReviewOutcome.Wrong }?.rows?.size ?: 0
        val passedCount = groups.find { it.outcome == ReviewOutcome.Passed }?.rows?.size ?: 0
        val timeUpAdded = groups.find { it.outcome == ReviewOutcome.TimeUp }
            ?.rows
            ?.count { it.isAddedToNotebook } ?: 0
        val addedCount = wrongCount + passedCount + timeUpAdded
        val allRows = groups.flatMap { it.rows }.toPersistentList()
        return ResultUiState(
            level = level,
            mode = mode,
            reason = reason,
            score = score,
            longestStreak = longestStreak,
            remainingSeconds = remainingSeconds,
            correct = correctCount,
            wrong = wrongCount,
            passed = passedCount,
            addedToNotebook = addedCount,
            groups = groups,
            rows = allRows,
            isLoading = isLoading,
            reviewError = reviewError,
        )
    }

    /** `06-result.png`: A1 classic, wheel done, 340 points, best streak ×1, 0:12 left, 1 / 1 / 1, 2 to review. */
    val design: ResultUiState = sampleState(
        level = Level.A1,
        mode = GameMode.CLASSIC,
        reason = GameOverReason.ALL_ANSWERED,
        score = 340,
        longestStreak = 1,
        remainingSeconds = 12,
        groups = persistentListOf(
            ReviewGroupUi(
                outcome = ReviewOutcome.Correct,
                rows = persistentListOf(
                    ReviewRowUi('A', "apple", "elma", "An apple a day.", ReviewOutcome.Correct, Level.A1),
                ),
            ),
            ReviewGroupUi(
                outcome = ReviewOutcome.Wrong,
                rows = persistentListOf(
                    ReviewRowUi('E', "egg", "yumurta", "I eat an egg.", ReviewOutcome.Wrong, Level.A1),
                ),
            ),
            ReviewGroupUi(
                outcome = ReviewOutcome.Passed,
                rows = persistentListOf(
                    ReviewRowUi('C', "chair", "sandalye", "Sit on the chair.", ReviewOutcome.Passed, Level.A1),
                ),
            ),
        ),
    )

    /** A long Doğru group (8 rows): the first 5 and "+3 kelime daha · Daha fazla göster →" while collapsed. */
    val longGroup: ResultUiState = sampleState(
        level = Level.A1,
        mode = GameMode.CLASSIC,
        reason = GameOverReason.ALL_ANSWERED,
        score = 410,
        longestStreak = 8,
        remainingSeconds = 20,
        groups = persistentListOf(
            ReviewGroupUi(
                outcome = ReviewOutcome.Correct,
                rows = listOf(
                    Triple("apple", "elma", "An apple a day."),
                    Triple("bread", "ekmek", "Fresh bread."),
                    Triple("chair", "sandalye", "Sit on the chair."),
                    Triple("dog", "köpek", "The dog barks."),
                    Triple("fish", "balık", "A big fish."),
                    Triple("green", "yeşil", "A green leaf."),
                    Triple("house", "ev", "Our house is old."),
                    Triple("key", "anahtar", "Where is the key?"),
                ).map { (word, clue, example) ->
                    ReviewRowUi(word.first().uppercaseChar(), word, clue, example, ReviewOutcome.Correct, Level.A1)
                }.toPersistentList(),
            ),
            ReviewGroupUi(
                outcome = ReviewOutcome.Wrong,
                rows = persistentListOf(
                    ReviewRowUi('E', "egg", "yumurta", "I eat an egg.", ReviewOutcome.Wrong, Level.A1),
                ),
            ),
        ),
    )

    /** Game ending by time-up with all four review groups: Doğru, Yanlış, Pas, Süre bitti. */
    val allFourGroups: ResultUiState = sampleState(
        level = Level.A1,
        mode = GameMode.CLASSIC,
        reason = GameOverReason.TIME_UP,
        score = 280,
        longestStreak = 1,
        remainingSeconds = 0,
        groups = persistentListOf(
            ReviewGroupUi(
                outcome = ReviewOutcome.Correct,
                rows = persistentListOf(
                    ReviewRowUi('A', "apple", "elma", "An apple a day.", ReviewOutcome.Correct, Level.A1),
                ),
            ),
            ReviewGroupUi(
                outcome = ReviewOutcome.Wrong,
                rows = persistentListOf(
                    ReviewRowUi('E', "egg", "yumurta", "I eat an egg.", ReviewOutcome.Wrong, Level.A1),
                ),
            ),
            ReviewGroupUi(
                outcome = ReviewOutcome.Passed,
                rows = persistentListOf(
                    ReviewRowUi('C', "chair", "sandalye", "Sit on the chair.", ReviewOutcome.Passed, Level.A1),
                ),
            ),
            ReviewGroupUi(
                outcome = ReviewOutcome.TimeUp,
                rows = persistentListOf(
                    ReviewRowUi(
                        letter = 'W',
                        word = "window",
                        clue = "pencere",
                        example = "Open the window.",
                        outcome = ReviewOutcome.TimeUp,
                        level = Level.A1,
                        cardId = "a1-window",
                        isAddedToNotebook = false,
                    ),
                    ReviewRowUi(
                        letter = 'Z',
                        word = "zebra",
                        clue = "zebra",
                        example = "A wild zebra.",
                        outcome = ReviewOutcome.TimeUp,
                        level = Level.A1,
                        cardId = "a1-zebra",
                        isAddedToNotebook = true,
                    ),
                ),
            ),
        ),
    )

    /** Practice (design debt): unscored, untimed; the header shows the correct answers instead of "0 puan". */
    val practice: ResultUiState = design.copy(mode = GameMode.PRACTICE, score = 0, remainingSeconds = null)

    /** The review cards could not be read (design debt): the list offers "Tekrar dene". */
    val reviewError: ResultUiState = design.copy(
        groups = persistentListOf(),
        rows = persistentListOf(),
        reviewError = true,
    )

    /** Long texts (English UI): a B2 hardcore game lost on the first miss, multi-word answers, wrapping examples. */
    val longText: ResultUiState = sampleState(
        level = Level.B2,
        mode = GameMode.HARDCORE,
        reason = GameOverReason.WRONG_IN_HARDCORE,
        score = 1_284,
        longestStreak = 1,
        remainingSeconds = 97,
        groups = persistentListOf(
            ReviewGroupUi(
                outcome = ReviewOutcome.Correct,
                rows = persistentListOf(
                    ReviewRowUi(
                        'C',
                        "come up with",
                        "fikir üretmek, bulmak",
                        "She came up with a great idea for the project.",
                        ReviewOutcome.Correct,
                        Level.B2,
                    ),
                ),
            ),
            ReviewGroupUi(
                outcome = ReviewOutcome.Wrong,
                rows = persistentListOf(
                    ReviewRowUi(
                        'G',
                        "get along with",
                        "biriyle iyi geçinmek, anlaşmak",
                        "My sister and I didn't always get along with each other as children.",
                        ReviewOutcome.Wrong,
                        Level.B2,
                    ),
                ),
            ),
            ReviewGroupUi(
                outcome = ReviewOutcome.Passed,
                rows = persistentListOf(
                    ReviewRowUi(
                        'N',
                        "nevertheless",
                        "yine de, buna rağmen",
                        "The weather was terrible; nevertheless, the match went ahead as planned.",
                        ReviewOutcome.Passed,
                        Level.B2,
                    ),
                    ReviewRowUi(
                        'W',
                        "whereabouts",
                        "bulunduğu yer",
                        "Police are still trying to find out the whereabouts of the missing painting.",
                        ReviewOutcome.Passed,
                        Level.B2,
                    ),
                ),
            ),
        ),
    )
}
