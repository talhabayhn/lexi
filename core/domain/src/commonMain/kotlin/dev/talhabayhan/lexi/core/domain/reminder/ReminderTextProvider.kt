package dev.talhabayhan.lexi.core.domain.reminder

/** Title and body of a reminder in the app's UI language. */
public data class ReminderText(val title: String, val body: String)

/**
 * Resolves `<variant.key>_title` / `_body` (`docs/copy/COPY.md` §1) from string resources, filling `{n}` with [count].
 * Implemented in the app layer (Compose resources); tests use `FakeReminderTextProvider`.
 */
public interface ReminderTextProvider {
    public suspend fun text(variant: ReminderVariant, count: Int?): ReminderText
}
