#!/usr/bin/env python3
"""Store graphics for Google Play and the App Store, regenerated from the app's real screens.

    pip install pillow            # once
    python3 tools/store-graphics/build.py              # writes docs/store/graphics/**
    python3 tools/store-graphics/build.py --screens-dir DIR   # prefer PNGs from DIR (e.g. `gh run download`)

Re-run after any UI change: first refresh the Roborazzi screenshots (`./gradlew recordRoborazziAndroidHostTest`),
then this script. Output is deterministic, so unchanged screens produce unchanged files.

Sources (never invented UI):
  * Roborazzi screenshots committed under feature/*/src/androidHostTest/screenshots (rendered from the app's
    Compose code at 390x844 dp @2x). With --screens-dir, a file of the same name there wins.
  * docs/design/screens/02-home.png for Home (1:1 with the app; the Home Roborazzi shot is captured mid
    entrance animation, so the design PNG is the cleaner source).
  * iosApp/.../AppIcon.appiconset/app-icon-1024.png: the shipped app icon (Play icon is a resize of it).

Required sizes, verified 2026-10 (Play Console help "Add preview assets", App Store Connect "Screenshot
specifications"):
  Google Play
    - App icon: 512 x 512, 32-bit PNG, <= 1 MB (Play applies the round-rect mask; no own rounding/shadow).
    - Feature graphic: 1024 x 500, JPEG or 24-bit PNG (no alpha). Required to publish.
    - Phone screenshots: 2-8, PNG/JPEG no alpha, each side 320-3840 px, long side <= 2x short side;
      1080 x 1920 (9:16) is used here (meets the >= 1080 px recommendation for promotion).
  App Store (iPhone only: TARGETED_DEVICE_FAMILY = 1, so no iPad set is needed)
    - 6.9" display: 1320 x 2868 portrait (primary; Apple scales it down for smaller iPhones).
    - 6.5" display: 1284 x 2778 portrait (only needed if 6.9" is not provided; generated as a fallback).
    - 1-10 per localization, PNG/JPEG, RGB, no alpha.
    - App icon 1024 x 1024 comes from the asset catalog in the build, not from this script.
"""
from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs" / "store" / "graphics"
FONTS = ROOT / "docs" / "design" / "fonts"
APP_ICON = ROOT / "iosApp" / "iosApp" / "Assets.xcassets" / "AppIcon.appiconset" / "app-icon-1024.png"

# Brand tokens (docs/design/tokens.md).
CREAM = (0xF2, 0xEB, 0xDD)
INK = (0x11, 0x11, 0x11)
RED = (0xD9, 0x28, 0x1C)
BLUE = (0x1F, 0x4F, 0xBF)
YELLOW = (0xF2, 0xC2, 0x00)
WHITE = (0xFF, 0xFF, 0xFF)

SIZES = {
    "play/phone": (1080, 1920),
    "appstore/iphone-6.9": (1320, 2868),
    "appstore/iphone-6.5": (1284, 2778),
}

# (source screenshot, band colour, headline TR, headline EN). Source "design:" = docs/design/screens/<name>.
SCREENS = {
    "tr": [
        ("feature/game/GameScreen.png", RED, "Türkçe ipucu,\nİngilizce kelime", None),
        ("feature/levels/LevelSelect.png", BLUE, "A1'den ana dile\naltı seviye", None),
        ("feature/game/ResultScreen.png", YELLOW, "Her oyundan sonra\ntekrar listesi", None),
        ("feature/notebook/Notebook.png", INK, "Aralıklı tekrarlı\nkelime defteri", None),
        ("feature/stats/Stats.png", BLUE, "İlerlemeni\nadım adım gör", None),
        ("design:02-home.png", RED, "Her gün yeni\nGünün Çarkı", None),
    ],
    "en": [
        # Clues are Turkish by design (the app teaches English to Turkish speakers), so the game screen is shared.
        ("feature/game/GameScreen.png", RED, None, "Turkish clue,\nEnglish word"),
        ("feature/levels/LevelSelectNatPracticeEn.png", BLUE, None, "Six levels,\nA1 to native"),
        ("feature/game/ResultScreenLongTextEnglish.png", YELLOW, None, "Review every\nword you missed"),
        ("feature/notebook/NotebookWordSheetEn.png", INK, None, "A notebook with\nspaced repetition"),
        ("feature/stats/StatsEn.png", BLUE, None, "Watch your\nprogress grow"),
        ("feature/game/JokerSheetEnglish.png", RED, None, "Stuck? Use\na joker"),
    ],
}

FEATURE = {
    "tr": ("A'dan Z'ye İngilizce", "Türkçe ipucu, İngilizce kelime. A1'den ana dile."),
    "en": ("English, A to Z", "Turkish clues, English words. A1 to native."),
}


def font(weight: str, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(FONTS / f"Jost-{weight}.ttf"), size)


def source(name: str, screens_dir: Path | None) -> Image.Image:
    if name.startswith("design:"):
        path = ROOT / "docs" / "design" / "screens" / name.removeprefix("design:")
    else:
        module, file = name.removeprefix("feature/").split("/")
        path = ROOT / "feature" / module / "src" / "androidHostTest" / "screenshots" / file
        if screens_dir is not None:
            override = next(iter(sorted(screens_dir.rglob(file))), None)
            path = override or path
    if not path.exists():
        raise SystemExit(f"missing source screen: {path}")
    return Image.open(path).convert("RGB")


def fit_text(draw: ImageDraw.ImageDraw, text: str, max_w: int, max_h: int, start: int) -> ImageFont.FreeTypeFont:
    size = start
    while size > 12:
        f = font("ExtraBold", size)
        box = draw.multiline_textbbox((0, 0), text, font=f, spacing=int(size * 0.12))
        if box[2] - box[0] <= max_w and box[3] - box[1] <= max_h:
            return f
        size -= 2
    return font("ExtraBold", size)


def screenshot(src: Image.Image, size: tuple[int, int], band: tuple[int, int, int], headline: str) -> Image.Image:
    w, h = size
    u = w / 1080  # layout unit, so every size keeps the same proportions
    img = Image.new("RGB", size, CREAM)
    d = ImageDraw.Draw(img)

    band_h = int(h * 0.22)
    d.rectangle((0, 0, w, band_h), fill=band)
    d.rectangle((0, band_h, w, band_h + int(8 * u)), fill=INK)
    on = INK if band == YELLOW else WHITE
    pad = int(72 * u)
    f = fit_text(d, headline, w - 2 * pad, band_h - 2 * pad, int(104 * u))
    spacing = int(f.size * 0.12)
    box = d.multiline_textbbox((0, 0), headline, font=f, spacing=spacing)
    d.multiline_text((pad, (band_h - (box[3] - box[1])) // 2 - box[1]), headline, font=f, fill=on, spacing=spacing)

    # Bauhaus accents behind the phone, in the brand's shape language (circle, square, leaf).
    r = int(170 * u)
    d.ellipse((w - r - int(40 * u), band_h + int(120 * u), w + r - int(40 * u), band_h + int(120 * u) + 2 * r), fill=YELLOW)
    d.rectangle((-int(60 * u), h - int(520 * u), int(150 * u), h - int(310 * u)), fill=BLUE if band != BLUE else RED)

    top = band_h + int(64 * u)
    screen_h = h - top - int(64 * u)
    screen_w = int(screen_h * src.width / src.height)
    if screen_w > int(w * 0.80):
        screen_w = int(w * 0.80)
        screen_h = int(screen_w * src.height / src.width)
    x = (w - screen_w) // 2
    border = max(4, int(8 * u))
    shadow = int(18 * u)
    d.rectangle((x + shadow, top + shadow, x + screen_w + shadow, top + screen_h + shadow), fill=INK)
    d.rectangle((x - border, top - border, x + screen_w + border - 1, top + screen_h + border - 1), fill=INK)
    img.paste(src.resize((screen_w, screen_h), Image.LANCZOS), (x, top))
    return img


def feature_graphic(lang: str) -> Image.Image:
    w, h = 1024, 500
    img = Image.new("RGB", (w, h), CREAM)
    d = ImageDraw.Draw(img)
    # Left half echoes the app icon: red field with the yellow "L" disc on the seam.
    d.rectangle((0, 0, 299, h), fill=RED)
    icon = Image.open(APP_ICON).convert("RGB").resize((260, 260), Image.LANCZOS)
    img.paste(icon, (170, 120))

    # Wordmark tiles L E X I (logo, docs/design/screens/13-logo-and-icon).
    tile, gap, x0, y0 = 96, 12, 490, 92
    shapes = [("sq", RED, WHITE, "L"), ("ci", YELLOW, INK, "E"), ("lf", BLUE, WHITE, "X"), ("bk", INK, CREAM, "I")]
    letter_font = font("ExtraBold", 66)
    for i, (shape, fill, fg, ch) in enumerate(shapes):
        x = x0 + i * (tile + gap)
        box = (x, y0, x + tile, y0 + tile)
        if shape == "ci":
            d.ellipse(box, fill=fill, outline=INK, width=4)
        elif shape == "lf":
            leaf = Image.new("L", (tile * 4, tile * 4), 0)
            ld = ImageDraw.Draw(leaf)
            ld.rounded_rectangle((0, 0, tile * 4 - 1, tile * 4 - 1), radius=tile * 2, fill=255,
                                 corners=(True, False, True, False))
            leaf = leaf.resize((tile, tile), Image.LANCZOS)
            img.paste(INK, (x, y0), leaf)
            inner = leaf.resize((tile - 8, tile - 8), Image.LANCZOS)
            img.paste(fill, (x + 4, y0 + 4), inner)
        elif shape == "bk":
            d.rectangle(box, fill=fill)
        else:
            d.rectangle(box, fill=fill, outline=INK, width=4)
        tb = d.textbbox((0, 0), ch, font=letter_font)
        d.text((x + (tile - (tb[2] - tb[0])) / 2 - tb[0], y0 + (tile - (tb[3] - tb[1])) / 2 - tb[1]), ch,
               font=letter_font, fill=fg)

    title, sub = FEATURE[lang]
    d.text((x0, 236), title.upper(), font=font("Bold", 34), fill=INK)
    sub_font = fit_text(d, sub, w - x0 - 40, 120, 30)
    d.multiline_text((x0, 296), _wrap(d, sub, sub_font, w - x0 - 40), font=sub_font, fill=INK, spacing=8)
    d.rectangle((x0, 420, x0 + 120, 432), fill=INK)
    return img


def _wrap(d: ImageDraw.ImageDraw, text: str, f: ImageFont.FreeTypeFont, max_w: int) -> str:
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
    ap = argparse.ArgumentParser(description="Generate store graphics into docs/store/graphics")
    ap.add_argument("--screens-dir", type=Path, help="folder with fresher Roborazzi PNGs (same file names)")
    args = ap.parse_args()

    save(Image.open(APP_ICON).convert("RGB").resize((512, 512), Image.LANCZOS), OUT / "play" / "icon-512.png")
    for lang in ("tr", "en"):
        save(feature_graphic(lang), OUT / "play" / f"feature-graphic-{lang}.png")
        for i, (name, band, tr, en) in enumerate(SCREENS[lang], start=1):
            src = source(name, args.screens_dir)
            for folder, size in SIZES.items():
                save(screenshot(src, size, band, tr or en), OUT / folder / f"{lang}-{i:02d}.png")


if __name__ == "__main__":
    main()
