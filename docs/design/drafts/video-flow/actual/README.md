# Video flow — rendered screens (phase 2)

Linux CI renders (Roborazzi goldens recorded by `screenshots-record` on `redesign/video-flow`), copied here to compare
with the drafts (`../home_v5.png`, `../cats_v3.png`). The source of truth stays the goldens in each module's
`src/androidHostTest/screenshots`; refresh these copies when the goldens change.

| File | Golden |
|---|---|
| `01-home-tr.png` | `feature/home/.../HomeScreen.png` (390 × 844) |
| `02-home-320x568-font200.png` | `HomeScreen_w320h568_fontScale200.png` |
| `03-picker-top.png` | `feature/levels/.../CategoryPicker.png` |
| `04-picker-exam-section.png` | `CategoryPickerNat.png` (NAT: Karışık, then the exam packs) |
| `05-dialog-klasik.png` | `PlayDialogClassic.png` |
| `06-dialog-320x568-font200.png` | `PlayDialog_w320h568_fontScale200.png` |

`flow-android.mp4` (phase 3): screen recording of the release-like `benchmarkRelease` build on an Android emulator
(API 35, arm64, host GPU; `adb shell screenrecord --size 540x1200 --bit-rate 2000000`, no re-encode): Home → play
card → "Pick your wheel" (grid entrance) → Mixed card lift + Y flip into the dialog → Practice / Classic → close →
reopen → PLAY letter morph → board entrance → letters and passes (#17). Compare with `../prototype/` and the
prototype video. An emulator renders through the host GPU: timing feel is right, smoothness is not a phone's.
