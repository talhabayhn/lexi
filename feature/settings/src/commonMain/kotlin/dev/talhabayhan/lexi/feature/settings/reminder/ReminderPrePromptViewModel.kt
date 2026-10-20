package dev.talhabayhan.lexi.feature.settings.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.talhabayhan.lexi.core.domain.reminder.AnswerReminderPrePrompt
import dev.talhabayhan.lexi.core.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The reminder pre-prompt. Showing it counts as asked (it is marked shown on entry), so leaving it with the system back
 * gesture never brings it back; an answer goes through [AnswerReminderPrePrompt].
 */
internal class ReminderPrePromptViewModel(
    private val settings: SettingsRepository,
    private val answer: AnswerReminderPrePrompt,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ReminderPrePromptUiState())
    val state: StateFlow<ReminderPrePromptUiState> = mutableState.asStateFlow()

    private val effectChannel = Channel<ReminderPrePromptEffect>(Channel.BUFFERED)
    val effects: Flow<ReminderPrePromptEffect> = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                settings.setReminderPrePromptShown()
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // Best effort: the answer writes the flag again.
            }
        }
    }

    fun onIntent(intent: ReminderPrePromptIntent) {
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true) }
        viewModelScope.launch {
            try {
                answer(accepted = intent == ReminderPrePromptIntent.Accept)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // A failed write or OS call leaves the reminder off; the screen closes either way.
            }
            effectChannel.trySend(ReminderPrePromptEffect.Done)
        }
    }
}
