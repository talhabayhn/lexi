package dev.talhabayhan.lexi.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.talhabayhan.lexi.core.designsystem.component.EmptyState
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.feature.home.generated.resources.Res
import dev.talhabayhan.lexi.feature.home.generated.resources.home_back
import dev.talhabayhan.lexi.feature.home.generated.resources.home_exams_soon
import dev.talhabayhan.lexi.feature.home.generated.resources.home_tile_exams
import org.jetbrains.compose.resources.stringResource

/**
 * `10-exam-packs` header (back button, "Sınav paketleri") over an [EmptyState]: the packs themselves arrive in M7.
 * DESIGN-DEBT: the design only shows the finished page. Lives in `feature:home` until the exams module is wired.
 */
@Composable
internal fun ExamPacksScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val dimens = LexiTheme.dimens
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LexiTheme.colors.cream)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(
                start = dimens.screenPaddingHorizontal,
                top = dimens.screenPaddingTop,
                end = dimens.screenPaddingHorizontal,
                bottom = dimens.screenPaddingBottomList,
            ),
        verticalArrangement = Arrangement.spacedBy(dimens.gap14),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.gap12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavSquareButton(
                icon = LexiIcons.Back,
                contentDescription = stringResource(Res.string.home_back),
                onClick = onBack,
            )
            Text(stringResource(Res.string.home_tile_exams), style = LexiTheme.typography.title)
        }
        EmptyState(stringResource(Res.string.home_exams_soon), Modifier.fillMaxWidth())
    }
}

// design sample, not user-facing
@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ExamPacksScreenPreview() {
    LexiTheme(reducedMotion = false) { ExamPacksScreen(onBack = {}) }
}
