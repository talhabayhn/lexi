package dev.talhabayhan.lexi.core.designsystem.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.talhabayhan.lexi.core.designsystem.component.ActionCircleButton
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.LexiTextLink
import dev.talhabayhan.lexi.core.designsystem.component.LocalForcePressed
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetter
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.withCssLineHeight

// design sample, not user-facing: every text in this file is the 16-components canvas copy.

@Composable
private fun SectionLabel(text: String, topMargin: Dp = 0.dp) {
    Text(text, style = LexiTheme.typography.label12, modifier = Modifier.padding(top = topMargin))
}

@Composable
internal fun ColumnScope.GalleryColumnOne() {
    val button16 = LexiTheme.typography.button16
    SectionLabel("BUTONLAR")
    Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12)) {
        LexiButton("Birincil", onClick = {}, textStyle = button16.copy(letterSpacing = 0.06.em))
        LexiButton("İkincil", onClick = {}, variant = LexiButtonVariant.Secondary)
        LexiButton("PAS", onClick = {
        }, variant = LexiButtonVariant.Pass, textStyle = button16.copy(letterSpacing = 0.1.em))
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalForcePressed provides true) {
            LexiButton("Basılı", onClick = {})
        }
        LexiButton("Pasif", onClick = {}, enabled = false, modifier = Modifier.padding(start = 5.dp))
        LexiTextLink("Metin bağlantı", onClick = {})
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavSquareButton(LexiIcons.Back, "Kare ikon buton", onClick = {}, size = GallerySpec.demoButtonSize)
        ActionCircleButton(
            LexiIcons.Bulb,
            "Daire ikon buton",
            onClick = {},
            size = GallerySpec.demoButtonSize,
            iconSize = IconButtonDefaults.IconSize,
        )
        Text(
            "Navigasyon = kare,\noyun aksiyonu = daire",
            style = LexiTheme.typography.caption13.withCssLineHeight(GallerySpec.NOTE_LINE_HEIGHT),
        )
    }
}

@Composable
private fun Captioned(caption: String, content: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap6),
    ) {
        content()
        Text(caption, style = LexiTheme.typography.caption12SemiBold)
    }
}

@Composable
internal fun ColumnScope.GalleryColumnTwo() {
    SectionLabel("ÇARK HARFİ DURUMLARI")
    Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14)) {
        val captions = listOf("Bekliyor", "Sırada", "Doğru", "Yanlış", "Pas")
        WheelLetterState.entries.forEachIndexed { i, state ->
            Captioned(captions[i]) {
                WheelLetter(
                    letter = 'A' + i,
                    state = state,
                    size = GallerySpec.demoChipSize,
                    textStyle = LexiTheme.typography.heading18,
                    scaleCurrent = false,
                )
            }
        }
    }
}

@Composable
internal fun ColumnScope.GalleryColumnThree() {
}
