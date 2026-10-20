# ADR 0021: Local reminders as planned one-shot notifications

- Status: Accepted
- Date: 2026-10-20

## Context
M7 adds the daily reminder (`11-settings`, `COPY.md` §1). The rules: at most 1 a day, only after the player opts in
through the pre-prompt, never inside quiet hours, the text depends on player state (daily played, streak, due
notebook cards, days since last play), and the 3/7/14-day comeback series stops after day 14. v1.0 has no server
(ADR 0005), so all notifications are local. iOS cannot run app code when a local notification fires, so the text
has to be known when the notification is scheduled. `ARCHITECTURE.md` §7 already lists `ReminderScheduler`.

## Decision
- **Plan ahead, pure.** `ReminderPlanner` (`core:domain`, pure, no RNG, no clock) builds up to 15 entries, from today
  to today+14, as `PlannedReminder(epochDay, minuteOfDay, variant, count)`. It assumes the player won't play again.
  Messages rotate by `epochDay` and the same message key never appears on two days in a row. `notif_goal` is left
  out because it needs data at fire time.
- **Re-plan on change.** `RescheduleReminders` (domain use case, started app-scoped in `shared`) combines settings,
  streak days, sessions and SRS progress, debounces them, resolves the texts through `ReminderTextProvider` (Compose
  resources in `shared`, current app language) and calls `ReminderScheduler.replaceAll(...)`. It also re-runs on
  app start/resume (day change). Reminder off or permission missing ⇒ `cancelAll()`.
- **Interfaces in domain**: `ReminderScheduler`, `NotificationPermission` (`GRANTED | CAN_ASK | BLOCKED`, `request()`,
  `openSystemSettings()`) and `ReminderTextProvider`; implementations in `core:platform` / `shared`.
- **Android**: AlarmManager `setAndAllowWhileIdle(RTC_WAKEUP)`, which is inexact, so the app needs no
  `SCHEDULE_EXACT_ALARM`. The plan is stored as local date + minute + title + body in private SharedPreferences.
  `ReminderAlarmReceiver` posts the notification on the "reminders" channel. `ReminderRescheduleReceiver` re-arms the
  alarms from the stored plan on `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED` and `MY_PACKAGE_REPLACED`.
  `POST_NOTIFICATIONS` is a runtime request on API 33+ (ActivityResult launcher registered in `MainActivity.onCreate`).
- **iOS**: `UNUserNotificationCenter` from Kotlin/Native (no Swift bridge), with non-repeating
  `UNCalendarNotificationTrigger`s in the local time zone and ids prefixed `lexi.reminder.`.
- **Quiet hours** (22:00–08:00) can't be hit: the time list only offers 08:00–22:00 in 30-min steps. A configurable
  quiet-hours row waits for a design.
- **No new dependency**: only framework APIs plus `core-ktx` / `activity`, which are already in the catalog.

## Alternatives considered
- **WorkManager** (periodic or one-time workers that build the text at fire time). Rejected: it is a new dependency
  plus App Startup init, it only helps Android (iOS still needs pre-planned texts, so we would keep two models), and
  its initial delays are relative durations, so a time zone change or clock adjustment shifts a 20:00 reminder until
  the app is opened again.
- **Repeating daily trigger with static text** (AlarmManager `setInexactRepeating` / iOS repeating trigger). Rejected:
  it can't skip days already played, honour the comeback series or vary the message.
- **Exact alarms** (`setExactAndAllowWhileIdle`). Rejected: they need `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`
  (a Play policy declaration), and a reminder doesn't need to be exact to the minute.
- **Push from a server.** Rejected: no backend in v1.0 (ADR 0005), and the privacy policy promises local-only
  notifications.

## Consequences
- The texts are fixed at scheduling time. A language change triggers a re-plan, and a plan only goes stale if the
  app isn't opened for more than 14 days, which is when the comeback series ends anyway.
- OEM battery savers may delay or drop inexact alarms. We accept this.
- At most 15 iOS pending requests, well under the 64 limit.
- The planner is pure and unit-tested on `jvmTest`. Platform schedulers get Robolectric tests on Android and a manual
  check on iOS (needs a Mac).
- The engine and its determinism (ADR 0002) are untouched.
