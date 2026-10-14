package dev.talhabayhan.lexi.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** Category id from `content/categories.csv`, e.g. `food-drink`. */
@Serializable
@JvmInline
public value class CategoryId(public val value: String) {
    public companion object {
        /** Virtual category: all categories of the level (`CONTENT.md` §4). */
        public val MIXED: CategoryId = CategoryId("mixed")
    }
}

/** Secondary content axis (`CONTENT.md` §4). Names are content, not UI strings. */
@Serializable
public data class Category(
    val id: CategoryId,
    val nameTr: String,
    val nameEn: String,
    val levels: Set<Level>,
    val sort: Int,
    val isPro: Boolean,
)
