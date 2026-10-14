package dev.talhabayhan.lexi.core.domain.model

import kotlinx.serialization.Serializable

/** CEFR level of a card or game, in ascending order. `NAT` = native speaker level. Names match content folders. */
@Serializable
public enum class Level { A1, A2, B1, B2, C1, NAT }
