package dev.talhabayhan.lexi.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** Exam pack id from the private pack repo's `packs.csv` (`CONTENT.md` §1), e.g. `lgs`. */
@Serializable
@JvmInline
public value class ExamPackId(public val value: String)

/** Exam pack (Pro content, `CONTENT.md` §1). Names are content, not UI strings. */
@Serializable
public data class ExamPack(
    val id: ExamPackId,
    val name: String,
    val description: String,
    val isPro: Boolean,
    val sort: Int,
)

/** A card of an exam pack with its optional unit (e.g. "Ünite 3"). */
@Serializable
public data class ExamPackCard(val card: Card, val unit: String?)
