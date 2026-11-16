package dev.talhabayhan.lexi.feature.exams

import androidx.compose.runtime.Immutable
import dev.talhabayhan.lexi.feature.exams.generated.resources.Res
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon_ielts_description
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon_ielts_title
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon_toefl_description
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon_toefl_title
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon_yds_description
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon_yds_title
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon_yks_description
import dev.talhabayhan.lexi.feature.exams.generated.resources.exams_soon_yks_title
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource

/** An upcoming exam under "Yakında" (`10-exam-packs`, DESIGN-DEBT): static, not a pack, never clickable. */
@Immutable
internal data class ComingSoonExamUi(val id: String, val title: StringResource, val description: StringResource)

/** The upcoming exams in display order; not read from content.db, so adding one needs no content change. */
internal val ComingSoonExams: ImmutableList<ComingSoonExamUi> = persistentListOf(
    ComingSoonExamUi("yds-yokdil", Res.string.exams_soon_yds_title, Res.string.exams_soon_yds_description),
    ComingSoonExamUi("yks-dil", Res.string.exams_soon_yks_title, Res.string.exams_soon_yks_description),
    ComingSoonExamUi("toefl", Res.string.exams_soon_toefl_title, Res.string.exams_soon_toefl_description),
    ComingSoonExamUi("ielts", Res.string.exams_soon_ielts_title, Res.string.exams_soon_ielts_description),
)
