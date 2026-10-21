#!/usr/bin/env python3
"""Classify re-recorded Roborazzi goldens against the committed ones (docs/CONVENTIONS.md, screenshots).

Run after `./gradlew recordRoborazziAndroidHostTest` in a clean checkout. For every changed golden under
`*/src/androidHostTest/screenshots` (and `*/*/...`) it compares the committed PNG (git HEAD) with the new one:

- RENDERING: same size, and after a box blur no visible region changed. Glyph shape, hinting and antialiasing
  differences (e.g. Windows vs Linux font rendering) are thin edges that the blur smooths away.
- SUSPECT: a new or deleted golden, a different size (layout), or a region that still differs after the blur
  (color change, moved or missing element, text that wrapped differently). These must be reviewed by a person.

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

GOLDEN_GLOBS = ("*/src/androidHostTest/screenshots/*.png", "*/*/src/androidHostTest/screenshots/*.png")


def git(*args: str) -> bytes:
    return subprocess.run(["git", *args], check=True, capture_output=True).stdout


def changed_goldens() -> list[tuple[str, str]]:
    """(status, path) of goldens that differ from HEAD: M modified, A new (untracked), D deleted."""
    out = git("status", "--porcelain", "--untracked-files=all", "--", *GOLDEN_GLOBS).decode()
    result = []
    for line in out.splitlines():
        status, path = line[:2].strip(), line[3:]
        result.append(("A" if status == "??" else status[0], path))
    return sorted(result, key=lambda item: item[1])


def share_above(image: Image.Image, delta: int) -> float:
    histogram = image.convert("L").point(lambda v: 255 if v > delta else 0).histogram()
    return histogram[255] / (image.width * image.height)


def classify(path: str, out: Path) -> tuple[str, str]:
    old = Image.open(BytesIO(git("show", f"HEAD:{path}"))).convert("RGB")
    new = Image.open(path).convert("RGB")
    if old.size != new.size:
        return "SUSPECT", f"size {old.size[0]}x{old.size[1]} → {new.size[0]}x{new.size[1]}"
    raw = ImageChops.difference(old, new)
    # Max channel delta per pixel, so a pure color shift in one channel counts too.
    raw_max = ImageChops.lighter(ImageChops.lighter(*raw.split()[:2]), raw.split()[2])
    blurred = ImageChops.difference(
        old.filter(ImageFilter.BoxBlur(BLUR_RADIUS)), new.filter(ImageFilter.BoxBlur(BLUR_RADIUS))
    )
    blurred_max = ImageChops.lighter(ImageChops.lighter(*blurred.split()[:2]), blurred.split()[2])
    pixels = share_above(raw_max, PIXEL_DELTA)
    region = share_above(blurred_max, REGION_DELTA)

    parts = Path(path).parts
    target = out / "-".join(parts[: parts.index("src")]) / Path(path).stem
    target.parent.mkdir(parents=True, exist_ok=True)
    old.save(f"{target}_old.png")
    new.save(f"{target}_new.png")
    raw_max.point(lambda v: 255 if v > PIXEL_DELTA else 0).save(f"{target}_diff.png")

    detail = f"changed pixels {pixels:.2%}, regional {region:.3%}"
    return ("SUSPECT" if region > REGION_SHARE else "RENDERING"), detail


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--out", default="build/golden-review", help="directory for the report images")
    args = parser.parse_args()
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)

    rows, suspects = [], []
    for status, path in changed_goldens():
        if status == "M":
            verdict, detail = classify(path, out)
        else:
            verdict, detail = "SUSPECT", "new golden" if status == "A" else "deleted golden"
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
