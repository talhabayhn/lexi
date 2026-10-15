package dev.talhabayhan.lexi.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.generated.resources.app_name
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.home.generated.resources.Res
import dev.talhabayhan.lexi.feature.home.generated.resources.home_play_a1_classic
import org.jetbrains.compose.resources.stringResource
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res as DesignRes

/**
 * Placeholder (M0): the app name and a temporary button that starts an A1 classic game. Replaced by the `02-home`
 * design in M4.
 */
@Composable
internal fun HomeScreen(state: HomeUiState, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = LexiTheme.dimens.screenPaddingHorizontal),
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap24, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(DesignRes.string.app_name))
        LexiButton(
            text = stringResource(Res.string.home_play_a1_classic),
            onClick = { onIntent(HomeIntent.StartGame) },
        )
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    LexiTheme {
        HomeScreen(state = HomeUiState(), onIntent = {})
    }
}
