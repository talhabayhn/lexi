#!/usr/bin/env python3
"""Regenerates the 1200x630 share-preview images from the Play feature graphics (no new artwork).

Usage:  python3 docs/site/make_og.py      (needs Pillow; the PNGs are committed, the site build does not need it)
"""
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "docs" / "store" / "graphics" / "play"
OUT = ROOT / "docs" / "site" / "assets"
W, H = 1200, 630

for lang in ("en", "tr"):
    img = Image.open(SRC / f"feature-graphic-{lang}.png").convert("RGB")
    img = img.resize((W, round(img.height * W / img.width)), Image.LANCZOS)
    pad = (H - img.height) // 2  # the graphic is flat colour at its top/bottom edge: extend it
    canvas = Image.new("RGB", (W, H))
    canvas.paste(img.crop((0, 0, W, 1)).resize((W, pad + 1)), (0, 0))
    canvas.paste(img.crop((0, img.height - 1, W, img.height)).resize((W, H - pad - img.height + 1)), (0, pad + img.height))
    canvas.paste(img, (0, pad))
    canvas.save(OUT / f"og-{lang}.png", optimize=True)
    print("wrote", OUT / f"og-{lang}.png")
