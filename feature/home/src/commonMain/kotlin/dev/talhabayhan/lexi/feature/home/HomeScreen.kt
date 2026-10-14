package dev.talhabayhan.lexi.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.generated.resources.Res
import dev.talhabayhan.lexi.core.designsystem.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

/** Placeholder (M0): shows the app name. Replaced by the `02-home` design in M4. */
@Composable
internal fun HomeScreen(state: HomeUiState, onIntent: (HomeIntent) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(Res.string.app_name))
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(state = HomeUiState(), onIntent = {})
}
