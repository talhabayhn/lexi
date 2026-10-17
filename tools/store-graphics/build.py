#!/usr/bin/env python3
"""Store graphics for Google Play and the App Store, regenerated from the app's real screens.

    pip install pillow numpy
    python3 tools/store-graphics/build.py                    # images -> docs/store/graphics/**
    python3 tools/store-graphics/video.py                    # videos (needs ffmpeg + footage, see video.py)
    python3 tools/store-graphics/build.py --screens-dir DIR  # prefer PNGs from DIR (e.g. `gh run download`)

The normal way is CI: Actions -> store-graphics -> Run workflow. It re-records every Roborazzi screenshot with the
current theme, captures the gameplay footage (StoreFootageCapture), runs both scripts and uploads everything as the
`store-graphics` artifact (docs/RELEASE.md §9). Output is deterministic for the same inputs.

Sources (never invented UI):
  * Roborazzi screenshots under feature/*/src/androidHostTest/screenshots, rendered from the app's Compose code at
    390x844 dp @2x with LexiTheme (so they carry the current palette once re-recorded).
  * build/store-footage/<lang>/frame_*.png from StoreFootageCapture, and feature/*/build/store-screens/en/*.png from
    the StoreScreensCapture tests: the English set uses the English UI with English clues and meanings (owner's
    decision 2026-10-17: English listings show no Turkish text; the app itself has Turkish clues only for now).
  * The launcher icon geometry (androidApp/src/main/res/drawable/ic_launcher_*.xml, design 13-logo-and-icon
    option 1) in the current LexiColors, drawn here so the store icon always matches the app.

Required sizes, verified 2026-10 (Play Console help "Add preview assets", App Store Connect "Screenshot
specifications" / "App preview specifications"):
  Google Play
    - App icon: 512 x 512, 32-bit PNG, <= 1 MB, full square (Play applies the mask; no own rounding/shadow).
    - Feature graphic: 1024 x 500, JPEG or 24-bit PNG (no alpha). Required to publish; also the video cover.
    - Phone screenshots: 2-8, PNG/JPEG no alpha, each side 320-3840 px, long side <= 2x short side;
      1080 x 1920 (9:16) here (meets the >= 1080 px recommendation for promotion).
    - Preview video: a YouTube URL only (public or unlisted, ads off) -> play/promo-video-*.mp4 to upload there.
  App Store (iPhone only: TARGETED_DEVICE_FAMILY = 1, so no iPad set is needed)
    - 6.9" display: 1320 x 2868 portrait (primary; Apple scales it down for smaller iPhones).
    - 6.5" display: 1284 x 2778 portrait (only needed if 6.9" is missing; generated as a fallback).
    - 1-10 per localization, PNG/JPEG, RGB, no alpha.
    - App preview: 886 x 1920 portrait, 15-30 s, <= 30 fps, H.264 .mp4, <= 500 MB, up to 3 per localization.
    - App icon 1024 x 1024 comes from the build's asset catalog; appstore/app-icon-1024.png is the same art.
"""
from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs" / "store" / "graphics"
FONTS = ROOT / "docs" / "design" / "fonts"
FOOTAGE = ROOT / "feature" / "game" / "build" / "store-footage"

# LexiColors (core/designsystem .../theme/LexiColors.kt, docs/design/tokens.md). Keep in sync when the theme changes.
CREAM = (0xF7, 0xF4, 0xEE)
INK = (0x1F, 0x24, 0x2D)
RED = (0xE0, 0x53, 0x4C)
BLUE = (0x3D, 0x68, 0xB2)
YELLOW = (0xE2, 0x9E, 0x2E)
WHITE = (0xFF, 0xFF, 0xFF)

SIZES = {
    "play/phone": (1080, 1920),
    "appstore/iphone-6.9": (1320, 2868),
    "appstore/iphone-6.5": (1284, 2778),
}

# (source, band colour, headline). "feature/<module>/<file>" = Roborazzi shot, "footage:<frame>" = footage frame,
# "footage:combo" = the footage frame at the combo burst
# (falls back to the second entry when the footage was not captured).
SCREENS = {
    "tr": [
        ("feature/game/GameScreen.png", RED, "Türkçe ipucu,\nİngilizce kelime"),
        ("feature/game/GameMotionComboBurst.png", BLUE, "Seri yap,\npuanı katla"),
        ("feature/home/HomeScreen.png", YELLOW, "Her gün yeni\nGünün Çarkı"),
        ("feature/levels/LevelSelect.png", INK, "A1'den ana dile\naltı seviye"),
        ("feature/game/ResultScreen.png", RED, "Her oyundan sonra\ntekrar listesi"),
        ("feature/notebook/Notebook.png", BLUE, "Aralıklı tekrarlı\nkelime defteri"),
        ("feature/stats/Stats.png", YELLOW, "İlerlemeni\nadım adım gör"),
        ("feature/game/JokerSheet.png", INK, "Takıldın mı?\nJoker kullan"),
    ],
    "en": [
        # English UI with English clues/meanings (StoreScreensCapture): English listings show no Turkish text.
        ("store:game/Game.png", RED, "Find the word,\nletter by letter"),
        ("footage:combo", BLUE, "Build streaks,\nscore more"),
        ("store:home/Home.png", YELLOW, "A new Daily Wheel\nevery day"),
        ("feature/levels/LevelSelectNatPracticeEn.png", INK, "Six levels,\nA1 to native"),
        ("store:game/Result.png", RED, "Review every\nword you missed"),
        ("store:notebook/Notebook.png", BLUE, "A notebook with\nspaced repetition"),
        ("feature/stats/StatsEn.png", YELLOW, "Watch your\nprogress grow"),
        ("store:game/JokerSheet.png", INK, "Stuck?\nUse a joker"),
    ],
}

FEATURE = {
    "tr": ("A'dan Z'ye İngilizce", "Türkçe ipucu, İngilizce kelime. A1'den ana dile."),
    "en": ("From A to Z in English", "Learn English words with an A–Z wheel. A1 to native."),
}


def font(weight: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONTS / f"Jost-{weight}.ttf"), size)


def resolve(name: str, screens_dir: Path | None, lang: str = "tr") -> Path:
    """Path of a source screen; `a|b` tries a first."""
    for option in name.split("|"):
        if option == "footage:combo":
            path = combo_frame(lang)
        elif option.startswith("footage:"):
            path = FOOTAGE / lang / option.removeprefix("footage:")
        elif option.startswith("store:"):
            module, file = option.removeprefix("store:").split("/")
            path = ROOT / "feature" / module / "build" / "store-screens" / lang / file
        else:
            module, file = option.removeprefix("feature/").split("/")
            path = ROOT / "feature" / module / "src" / "androidHostTest" / "screenshots" / file
            if screens_dir is not None:
                path = next(iter(sorted(screens_dir.rglob(file))), path)
        if path.exists():
            return path
    raise SystemExit(f"missing source screen: {name}")


def combo_frame(lang: str) -> Path:
    """Footage frame with the combo burst at its middle keyframe (timeline.txt "combo" + 210 ms, motion.md #7)."""
    timeline = FOOTAGE / lang / "timeline.txt"
    if not timeline.exists():
        return FOOTAGE / lang / "missing"
    ms = next(int(line.split()[0]) for line in timeline.read_text().splitlines() if line.endswith(" combo"))
    return FOOTAGE / lang / f"frame_{(ms + 210) // 33:04d}.png"


def source(name: str, screens_dir: Path | None, lang: str = "tr") -> Image.Image:
    return Image.open(resolve(name, screens_dir, lang)).convert("RGB")


def app_icon(size: int) -> Image.Image:
    """Launcher icon (design 180 px units): cream, red left half, ink-ringed yellow disc, "L" outline from Jost."""
    ss = 4
    s = size * ss / 180
    img = Image.new("RGB", (size * ss, size * ss), CREAM)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 90 * s - 1, size * ss), fill=RED)
    d.ellipse((40 * s, 40 * s, 140 * s, 140 * s), fill=INK)
    d.ellipse((45 * s, 45 * s, 135 * s, 135 * s), fill=YELLOW)
    ell = [(76.976, 67.2), (76.976, 112), (107.504, 112), (107.504, 101.44), (89.392, 101.44), (89.392, 67.2)]
    d.polygon([(x * s, y * s) for x, y in ell], fill=INK)
    return img.resize((size, size), Image.LANCZOS)


def fit_text(draw: ImageDraw.ImageDraw, text: str, max_w: int, max_h: int, start: int) -> ImageFont.FreeTypeFont:
    size = start
    while size > 12:
        f = font("ExtraBold", size)
        box = draw.multiline_textbbox((0, 0), text, font=f, spacing=int(size * 0.12))
        if box[2] - box[0] <= max_w and box[3] - box[1] <= max_h:
            return f
        size -= 2
    return font("ExtraBold", size)


def on_color(band: tuple[int, int, int]) -> tuple[int, int, int]:
    """tokens.md: text on yellow/white/cream is ink, on red/blue/ink white."""
    return INK if band in (YELLOW, WHITE, CREAM) else WHITE


def headline_band(img: Image.Image, box: tuple[int, int, int, int], band, text: str, u: float) -> None:
    d = ImageDraw.Draw(img)
    x0, y0, x1, y1 = box
    d.rectangle(box, fill=band)
    pad = int(72 * u)
    f = fit_text(d, text, x1 - x0 - 2 * pad, y1 - y0 - 2 * pad, int(104 * u))
    spacing = int(f.size * 0.12)
    tb = d.multiline_textbbox((0, 0), text, font=f, spacing=spacing)
    d.multiline_text((x0 + pad, y0 + (y1 - y0 - (tb[3] - tb[1])) // 2 - tb[1]), text, font=f, fill=on_color(band),
                     spacing=spacing)


def phone_layout(size: tuple[int, int], src_size: tuple[int, int]) -> tuple[int, int, int, int, int]:
    """(band_h, x, top, screen_w, screen_h) of the portrait frame; shared with video.py."""
    w, h = size
    u = w / 1080
    band_h = int(h * 0.22)
    top = band_h + int(64 * u)
    screen_h = h - top - int(64 * u)
    screen_w = int(screen_h * src_size[0] / src_size[1])
    if screen_w > int(w * 0.80):
        screen_w = int(w * 0.80)
        screen_h = int(screen_w * src_size[1] / src_size[0])
    return band_h, (w - screen_w) // 2, top, screen_w, screen_h


def portrait_background(size: tuple[int, int], band, headline: str, src_size=(780, 1688)) -> Image.Image:
    """Cream canvas, headline band and Bauhaus accents (circle, square) behind the screen slot."""
    w, h = size
    u = w / 1080
    img = Image.new("RGB", size, CREAM)
    d = ImageDraw.Draw(img)
    band_h, x, top, screen_w, screen_h = phone_layout(size, src_size)
    r = int(170 * u)
    d.ellipse((w - r - int(40 * u), band_h + int(120 * u), w + r - int(40 * u), band_h + int(120 * u) + 2 * r),
              fill=YELLOW if band != YELLOW else BLUE)
    d.rectangle((-int(60 * u), h - int(520 * u), int(150 * u), h - int(310 * u)), fill=BLUE if band != BLUE else RED)
    headline_band(img, (0, 0, w, band_h), band, headline, u)
    d.rectangle((0, band_h, w, band_h + int(8 * u)), fill=INK)
    border = max(4, int(8 * u))
    shadow = int(18 * u)
    d.rectangle((x + shadow, top + shadow, x + screen_w + shadow, top + screen_h + shadow), fill=INK)
    d.rectangle((x - border, top - border, x + screen_w + border - 1, top + screen_h + border - 1), fill=INK)
    return img


def screenshot(src: Image.Image, size: tuple[int, int], band, headline: str) -> Image.Image:
    img = portrait_background(size, band, headline, src.size)
    _, x, top, screen_w, screen_h = phone_layout(size, src.size)
    img.paste(src.resize((screen_w, screen_h), Image.LANCZOS), (x, top))
    return img


def logo_tiles(img: Image.Image, x0: int, y0: int, tile: int, gap: int) -> None:
    """Wordmark L E X I (13-logo-and-icon): square, circle, leaf, ink block."""
    d = ImageDraw.Draw(img)
    border = max(3, tile // 24)
    shapes = [("sq", RED, WHITE, "L"), ("ci", YELLOW, INK, "E"), ("lf", BLUE, WHITE, "X"), ("bk", INK, CREAM, "I")]
    letter_font = font("ExtraBold", int(tile * 0.69))
    for i, (shape, fill, fg, ch) in enumerate(shapes):
        x = x0 + i * (tile + gap)
        box = (x, y0, x + tile - 1, y0 + tile - 1)
        if shape == "ci":
            d.ellipse(box, fill=fill, outline=INK, width=border)
        elif shape == "lf":
            big = tile * 4
            mask = Image.new("L", (big, big), 0)
            ImageDraw.Draw(mask).rounded_rectangle((0, 0, big - 1, big - 1), radius=big // 2, fill=255,
                                                   corners=(True, False, True, False))
            img.paste(INK, (x, y0), mask.resize((tile, tile), Image.LANCZOS))
            inner = tile - 2 * border
            img.paste(fill, (x + border, y0 + border), mask.resize((inner, inner), Image.LANCZOS))
        elif shape == "bk":
            d.rectangle(box, fill=fill)
        else:
            d.rectangle(box, fill=fill, outline=INK, width=border)
        tb = d.textbbox((0, 0), ch, font=letter_font)
        d.text((x + (tile - (tb[2] - tb[0])) / 2 - tb[0], y0 + (tile - (tb[3] - tb[1])) / 2 - tb[1]), ch,
               font=letter_font, fill=fg)


def feature_graphic(lang: str) -> Image.Image:
    w, h = 1024, 500
    img = Image.new("RGB", (w, h), CREAM)
    d = ImageDraw.Draw(img)
    # Left part echoes the app icon: red field with the yellow "L" disc on the seam.
    d.rectangle((0, 0, 299, h), fill=RED)
    img.paste(app_icon(260), (170, 120))
    logo_tiles(img, 490, 92, 96, 12)
    title, sub = FEATURE[lang]
    d.text((490, 236), title.upper(), font=fit_text(d, title.upper(), w - 530, 50, 34), fill=INK)
    sub_font = fit_text(d, sub, w - 530, 120, 30)
    d.multiline_text((490, 296), wrap(d, sub, sub_font, w - 530), font=sub_font, fill=INK, spacing=8)
    d.rectangle((490, 420, 610, 432), fill=INK)
    return img


def wrap(d: ImageDraw.ImageDraw, text: str, f: ImageFont.FreeTypeFont, max_w: int) -> str:
    lines, cur = [], ""
    for word in text.split():
        trial = f"{cur} {word}".strip()
        if d.textlength(trial, font=f) <= max_w:
            cur = trial
        else:
            lines.append(cur)
            cur = word
    return "\n".join([*lines, cur])


def save(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, optimize=True)
    print(path.relative_to(ROOT), img.size)


def main() -> None:
    ap = argparse.ArgumentParser(description="Generate store images into docs/store/graphics")
    ap.add_argument("--screens-dir", type=Path, help="folder with fresher Roborazzi PNGs (same file names)")
    args = ap.parse_args()

    save(app_icon(512), OUT / "play" / "icon-512.png")
    save(app_icon(1024), OUT / "appstore" / "app-icon-1024.png")
    # The iOS app icon is the same art, so iOS and Android show the same icon in the current palette.
    save(app_icon(1024), ROOT / "iosApp" / "iosApp" / "Assets.xcassets" / "AppIcon.appiconset" / "app-icon-1024.png")
    for lang in ("tr", "en"):
        save(feature_graphic(lang), OUT / "play" / f"feature-graphic-{lang}.png")
        for i, (name, band, headline) in enumerate(SCREENS[lang], start=1):
            src = source(name, args.screens_dir, lang)
            for folder, size in SIZES.items():
                save(screenshot(src, size, band, headline), OUT / folder / f"{lang}-{i:02d}.png")


if __name__ == "__main__":
    main()
