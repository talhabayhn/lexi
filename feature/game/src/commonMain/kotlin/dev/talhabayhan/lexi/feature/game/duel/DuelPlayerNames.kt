package dev.talhabayhan.lexi.feature.game.duel

import androidx.compose.runtime.Composable
import dev.talhabayhan.lexi.feature.game.generated.resources.Res
import dev.talhabayhan.lexi.feature.game.generated.resources.duel_player_default
import org.jetbrains.compose.resources.stringResource

/** The name to show for player [index] (0-based): as typed, or "1. Oyuncu" / "2. Oyuncu" when left empty. */
@Composable
internal fun duelPlayerName(name: String, index: Int): String =
    name.ifEmpty { stringResource(Res.string.duel_player_default, index + 1) }
