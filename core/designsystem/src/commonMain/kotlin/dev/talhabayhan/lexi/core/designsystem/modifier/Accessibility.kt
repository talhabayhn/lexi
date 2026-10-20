package dev.talhabayhan.lexi.core.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

/** Marks an element (a screen, sheet or dialog title) as an accessibility heading for TalkBack/VoiceOver navigation. */
public fun Modifier.lexiHeading(): Modifier = semantics { heading() }
