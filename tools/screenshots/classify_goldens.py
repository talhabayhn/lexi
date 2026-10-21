#!/usr/bin/env python3
"""Classify re-recorded Roborazzi goldens against the committed ones (docs/CONVENTIONS.md, screenshots).

Run after `./gradlew recordRoborazziAndroidHostTest` in a clean checkout. For every changed golden under
`*/src/androidHostTest/screenshots` (git pathspec `*` also matches `/`, so nested modules count) it compares the
committed PNG (git HEAD) with the new one:

- RENDERING: same size, no flat area changed color and after a box blur no visible region changed. Glyph shape,
  hinting and antialiasing differences (e.g. Windows vs Linux font rendering) are thin edges next to ink: they never
  touch areas that are flat in both images, and the blur smooths them away.
- SUSPECT: a new or deleted golden, a different size (layout), a flat area with another color (palette or token
  change, missing shadow), or a region that still differs after the blur (moved or missing element, text that
  wrapped differently). These must be reviewed by a person.

Writes a Markdown report (stdout, and $GITHUB_STEP_SUMMARY when set), `<out>/suspects.txt` (one path per line) and
`<out>/<module>/<name>_{old,new,diff}.png` for review. Exit code 0; the workflow decides what to commit.
Needs Pillow.
"""

import argparse
import os
import subprocess
import sys
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageChops, ImageFilter

# Blur radius (px) that smooths glyph-edge differences; region changes survive it.
BLUR_RADIUS = 3
# Per-channel delta (0-255) of the blurred images that counts as a visible regional change.
REGION_DELTA = 48
# Share of pixels allowed above REGION_DELTA before a golden is SUSPECT (0.05 %).
REGION_SHARE = 0.0005
# Per-channel delta of the raw images that counts a pixel as changed (for the report only).
PIXEL_DELTA = 24
# A pixel is flat when the luminance range of its FLAT_WINDOW x FLAT_WINDOW neighbourhood is at most FLAT_RANGE.
FLAT_WINDOW = 7
FLAT_RANGE = 6
# Per-channel delta that counts as a color change in a pixel flat in both images, and the share allowed (0.05 %).
COLOR_DELTA = 6
COLOR_SHARE = 0.0005

GOLDEN_GLOB = "*/src/androidHostTest/screenshots/*.png"


def git(*args: str) -> bytes:
    return subprocess.run(["git", *args], check=True, capture_output=True).stdout


def changed_goldens() -> list[tuple[str, str]]:
    """(status, path) of goldens that differ from HEAD: M modified, A new (untracked), D deleted."""
    out = git("status", "--porcelain", "-z", "--untracked-files=all", "--", GOLDEN_GLOB).decode()
    result = []
    for entry in filter(None, out.split("\0")):
        status, path = entry[:2].strip(), entry[3:]
        result.append(("A" if status == "??" else status[0], path))
    return sorted(result, key=lambda item: item[1])


def share_above(image: Image.Image, delta: int) -> float:
    histogram = image.convert("L").point(lambda v: 255 if v > delta else 0).histogram()
    return histogram[255] / (image.width * image.height)


def flat(image: Image.Image) -> Image.Image:
    """255 where the neighbourhood is flat (no edge or glyph within FLAT_WINDOW // 2 px), else 0."""
    luminance = image.convert("L")
    spread = ImageChops.subtract(
        luminance.filter(ImageFilter.MaxFilter(FLAT_WINDOW)), luminance.filter(ImageFilter.MinFilter(FLAT_WINDOW))
    )
    return spread.point(lambda v: 255 if v <= FLAT_RANGE else 0)


def review_target(path: str, out: Path) -> Path:
    """`<out>/<module>/<name>`, the prefix of the review images of [path]."""
    parts = Path(path).parts
    target = out / "-".join(parts[: parts.index("src")]) / Path(path).stem
    target.parent.mkdir(parents=True, exist_ok=True)
    return target


def classify(status: str, path: str, out: Path) -> tuple[str, str]:
    target = review_target(path, out)
    old = Image.open(BytesIO(git("show", f"HEAD:{path}"))).convert("RGB") if status != "A" else None
    new = Image.open(path).convert("RGB") if status != "D" else None
    if old is not None:
        old.save(f"{target}_old.png")
    if new is not None:
        new.save(f"{target}_new.png")
    if old is None:
        return "SUSPECT", "new golden"
    if new is None:
        return "SUSPECT", "deleted golden"
    if old.size != new.size:
        return "SUSPECT", f"size {old.size[0]}x{old.size[1]} -> {new.size[0]}x{new.size[1]}"
    raw = ImageChops.difference(old, new)
    # Max channel delta per pixel, so a pure color shift in one channel counts too.
    raw_max = ImageChops.lighter(ImageChops.lighter(*raw.split()[:2]), raw.split()[2])
    blurred = ImageChops.difference(
        old.filter(ImageFilter.BoxBlur(BLUR_RADIUS)), new.filter(ImageFilter.BoxBlur(BLUR_RADIUS))
    )
    blurred_max = ImageChops.lighter(ImageChops.lighter(*blurred.split()[:2]), blurred.split()[2])
    pixels = share_above(raw_max, PIXEL_DELTA)
    region = share_above(blurred_max, REGION_DELTA)
    both_flat = ImageChops.multiply(flat(old), flat(new))
    recolored = ImageChops.multiply(raw_max.point(lambda v: 255 if v > COLOR_DELTA else 0), both_flat)
    color = recolored.histogram()[255] / (old.width * old.height)

    raw_max.point(lambda v: 255 if v > PIXEL_DELTA else 0).save(f"{target}_diff.png")

    detail = f"changed pixels {pixels:.2%}, recolored flat {color:.3%}, regional {region:.3%}"
    suspect = color > COLOR_SHARE or region > REGION_SHARE
    return ("SUSPECT" if suspect else "RENDERING"), detail


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", default="build/golden-review", help="directory for the report images")
    args = parser.parse_args()
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)

    rows, suspects = [], []
    for status, path in changed_goldens():
        verdict, detail = classify(status, path, out)
        rows.append(f"| {verdict} | `{path}` | {detail} |")
        if verdict == "SUSPECT":
            suspects.append(path)

    (out / "suspects.txt").write_text("".join(f"{path}\n" for path in suspects))
    report = "\n".join(
        [
            "## Roborazzi golden review",
            "",
            f"{len(rows)} changed goldens, {len(suspects)} suspect (not font/antialiasing only).",
            "",
            "| Verdict | Golden | Detail |",
            "|---|---|---|",
            *rows,
            "",
        ]
    )
    print(report)
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as handle:
            handle.write(report)
    return 0


if __name__ == "__main__":
    sys.exit(main())
