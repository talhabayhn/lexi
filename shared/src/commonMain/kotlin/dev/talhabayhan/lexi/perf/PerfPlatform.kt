package dev.talhabayhan.lexi.perf

/** "android" or "ios", printed in the tour summary. */
internal expect val perfPlatformName: String

/** Stores the full tour report where the test session can fetch it (iOS: `Documents/lexi-perf.json`;
 * Android: logcat only). */
internal expect fun writePerfReport(json: String)
