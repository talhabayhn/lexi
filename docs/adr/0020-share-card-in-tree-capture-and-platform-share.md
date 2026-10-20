# ADR 0020: Share card — in-tree capture and platform share

- Status: Accepted
- Date: 2026-10-20

## Context
M6 shares a result card as a PNG through the system share sheet. The card is Compose UI, so it has to be rendered to
a bitmap, then handed to Android's chooser or iOS's `UIActivityViewController` behind the domain `ShareService`.

## Decision
- Capture: in-tree Compose graphics-layer capture of the card composable (no screenshot or capture library). The
  PNG bytes go to `ShareService.sharePng(png, text)`.
- Android: `AndroidShareService` writes one overwritten file `cacheDir/share/lexi-share.png` and shares a
  `FileProvider` URI (`${applicationId}.share`) via `ACTION_SEND` + chooser. The provider is not exported and its
  `share_paths.xml` declares only `<cache-path path="share/">`, so nothing else in the cache is exposed.
- iOS: `IosShareService` presents a `UIActivityViewController` (image + text) from the top-most view controller of
  the key window; on iPad it anchors the popover to the view centre.
- Dependency: no new library. `androidx.core:core-ktx` (already in the catalog and transitively present) is declared
  as an explicit edge in `:core:platform` androidMain for `FileProvider`.

## Consequences
- The file stays in the app cache until the OS evicts it; the next share overwrites it.
- Sharing needs no storage permission on either platform.
- The share result only says whether the sheet was shown, not whether the user completed the share.
