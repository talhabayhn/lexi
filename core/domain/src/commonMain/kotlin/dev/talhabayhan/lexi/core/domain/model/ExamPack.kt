package dev.talhabayhan.lexi.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** Exam pack id from `content/exam-packs/packs.csv` (`CONTENT.md` §1), e.g. `lgs`. */
@Serializable
@JvmInline
public value class ExamPackId(public val value: String)

/** Exam pack (`CONTENT.md` §1, `docs/content/EXAM_PACKS.md`); free or Pro. Names are content, not UI strings. */
@Serializable
public data class ExamPack(
    val id: ExamPackId,
    val name: String,
    val description: String,
    val isPro: Boolean,
    val sort: Int,
    val titleTr: String = name,
    val titleEn: String = name,
    val descriptionTr: String = description,
    val levelRange: String = "",
    val cardTarget: Int = 0,
    val freePreviewLetters: String = "",
) {
    /** A Pro pack is locked for a player without Pro ([hasPro] from `EntitlementRepository`); free packs never are. */
    public fun isLocked(hasPro: Boolean): Boolean = isPro && !hasPro
}

/** A card of an exam pack with its optional unit slug (e.g. `friendship`; the app maps it to a string). */
@Serializable
public data class ExamPackCard(val card: Card, val unit: String?)
