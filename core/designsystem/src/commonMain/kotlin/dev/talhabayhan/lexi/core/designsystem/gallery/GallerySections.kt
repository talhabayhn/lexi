package dev.talhabayhan.lexi.core.designsystem.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.talhabayhan.lexi.core.designsystem.component.ActionCircleButton
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlot
import dev.talhabayhan.lexi.core.designsystem.component.AnswerSlotState
import dev.talhabayhan.lexi.core.designsystem.component.ConfirmDialog
import dev.talhabayhan.lexi.core.designsystem.component.IconButtonDefaults
import dev.talhabayhan.lexi.core.designsystem.component.LetterKey
import dev.talhabayhan.lexi.core.designsystem.component.LetterKeyState
import dev.talhabayhan.lexi.core.designsystem.component.LevelBadge
import dev.talhabayhan.lexi.core.designsystem.component.LevelBadgeStyle
import dev.talhabayhan.lexi.core.designsystem.component.LexiButton
import dev.talhabayhan.lexi.core.designsystem.component.LexiButtonVariant
import dev.talhabayhan.lexi.core.designsystem.component.LexiSwitch
import dev.talhabayhan.lexi.core.designsystem.component.LexiTextLink
import dev.talhabayhan.lexi.core.designsystem.component.LocalForcePressed
import dev.talhabayhan.lexi.core.designsystem.component.NavSquareButton
import dev.talhabayhan.lexi.core.designsystem.component.ProgressBar
import dev.talhabayhan.lexi.core.designsystem.component.Segment
import dev.talhabayhan.lexi.core.designsystem.component.SegmentedControl
import dev.talhabayhan.lexi.core.designsystem.component.Toast
import dev.talhabayhan.lexi.core.designsystem.component.ToastStyle
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetter
import dev.talhabayhan.lexi.core.designsystem.component.WheelLetterState
import dev.talhabayhan.lexi.core.designsystem.icon.LexiIcons
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.designsystem.theme.resized
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
    SectionLabel("SEGMENT VE ANAHTAR", topMargin = LexiTheme.dimens.gap8)
    SegmentedControl(
        segments = listOf(Segment("Seçili"), Segment("Seçenek"), Segment("Seçenek")),
        selectedIndex = 0,
        onSelect = {},
        modifier = Modifier.fillMaxWidth(),
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap24),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LexiSwitch(checked = true, onCheckedChange = {})
        LexiSwitch(checked = false, onCheckedChange = {})
        ProgressBar(
            progress = GallerySpec.DEMO_PROGRESS,
            height = GallerySpec.demoProgressHeight,
            borderWidth = LexiTheme.borders.regular,
            fillColor = LexiTheme.colors.yellow,
            fillEdge = true,
            modifier = Modifier.weight(1f),
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
    SectionLabel("CEVAP KUTUSU", topMargin = LexiTheme.dimens.gap6)
    Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14), verticalAlignment = Alignment.Bottom) {
        val slotStyle = LexiTheme.typography.slotLetter.resized(GallerySpec.demoSlotLetterSize)
        Captioned("Dolu") { AnswerSlot('K', AnswerSlotState.Filled, textStyle = slotStyle) }
        Captioned("Aktif") { AnswerSlot(null, AnswerSlotState.Active) }
        Captioned("Boş") { AnswerSlot(null, AnswerSlotState.Empty) }
        Captioned("Yanlış") { AnswerSlot('S', AnswerSlotState.Wrong, textStyle = slotStyle) }
        Captioned("Joker ile") { AnswerSlot('N', AnswerSlotState.JokerFilled, textStyle = slotStyle) }
    }
    SectionLabel("HARF TUŞU", topMargin = LexiTheme.dimens.gap6)
    Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap14), verticalAlignment = Alignment.Bottom) {
        val key = Modifier.width(GallerySpec.demoKeyWidth)
        Captioned("Normal") { LetterKey('E', onClick = {}, modifier = key) }
        Captioned("Basılı") {
            CompositionLocalProvider(LocalForcePressed provides true) { LetterKey('E', onClick = {}, modifier = key) }
        }
        Captioned("Kullanıldı") { LetterKey('E', onClick = {}, modifier = key, state = LetterKeyState.Used) }
        Captioned("Joker sildi") {
            LetterKey('E', onClick = {}, modifier = key, state = LetterKeyState.RemovedByJoker)
        }
    }
    SectionLabel("SEVİYE ROZETLERİ", topMargin = LexiTheme.dimens.gap6)
    Row(horizontalArrangement = Arrangement.spacedBy(LexiTheme.dimens.gap8)) {
        LevelBadge("A1", LevelBadgeStyle.A1)
        LevelBadge("A2", LevelBadgeStyle.A2)
        LevelBadge("B1", LevelBadgeStyle.B1)
        LevelBadge("B2", LevelBadgeStyle.B2)
        LevelBadge("C1", LevelBadgeStyle.C1)
        LevelBadge("NAT", LevelBadgeStyle.Native)
    }
}

@Composable
internal fun ColumnScope.GalleryColumnThree() {
    SectionLabel("BİLDİRİMLER (TOAST)")
    Toast("Doğru! KITCHEN", trailing = "+10", modifier = Modifier.fillMaxWidth())
    Toast(
        "\"egg\" kelime defterine eklendi",
        style = ToastStyle.Info,
        actionLabel = "Geri al",
        modifier = Modifier.fillMaxWidth(),
    )
    SectionLabel("ONAY PENCERESİ", topMargin = LexiTheme.dimens.gap6)
    ConfirmDialog(
        title = "Çarkı bırakmak istiyor musun?",
        message = "Bu çarktaki puanın kaydedilmez. Pas geçtiğin kelimeler yine de defterine eklenir.",
        confirmLabel = "Bırak",
        dismissLabel = "Devam et",
        onConfirm = {},
        onDismiss = {},
        modifier = Modifier.fillMaxWidth(),
    )
}
