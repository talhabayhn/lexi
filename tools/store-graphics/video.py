#!/usr/bin/env python3
"""Store videos from the app's real footage: App Store app previews and the Google Play (YouTube) promo video.

    pip install pillow numpy      # + ffmpeg on PATH
    python3 tools/store-graphics/video.py

Needs the gameplay footage from StoreFootageCapture (feature/game/build/store-footage/<lang>/: frame_*.png,
splash_*.png, timeline.txt), which only CI records: Actions -> store-graphics (docs/RELEASE.md §9). Every pixel of
the phone area is a real app frame or Roborazzi screenshot; sounds are the app's own WAVs at the moments the screen
played them.

Outputs (docs/store/graphics):
  appstore/preview/<lang>-886x1920.mp4   App Store app preview, iPhone 6.9" slot (886 x 1920 portrait, 30 fps,
                                         H.264 High, AAC stereo, ~24 s; Apple: 15-30 s, <= 500 MB, up to 3)
  play/promo-video-<lang>-1920x1080.mp4  Google Play preview video: upload to YouTube (public or unlisted, ads off)
                                         and paste the link in Play Console -> Main store listing -> Video
"""
from __future__ import annotations

import subprocess
import wave
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

from build import (BLUE, CREAM, FOOTAGE, INK, OUT, RED, ROOT, YELLOW, headline_band, logo_tiles,
                   phone_layout, portrait_background, resolve)

FPS = 30
FOOTAGE_STEP = 0.033  # StoreFootageCapture.STEP_MS
SOUNDS = ROOT / "shared" / "src" / "commonMain" / "composeResources" / "files" / "sounds"
RATE = 48_000
STILL_S = 2.4
SLIDE_S = 0.22  # motion.md #12: horizontal slide 24 dp, no fade, 220 ms
SPLASH_HOLD_S = 1.8

SCENES = {
    "tr": {
        "game": "Türkçe ipucu,\nİngilizce kelime",
        "end": "Hemen\noyna",
        "stills": [
            ("feature/home/HomeScreen.png", YELLOW, "Her gün yeni\nGünün Çarkı"),
            ("feature/levels/LevelSelect.png", BLUE, "A1'den ana dile\naltı seviye"),
            ("feature/game/ResultScreen.png", RED, "Her oyundan sonra\ntekrar listesi"),
            ("feature/notebook/Notebook.png", INK, "Aralıklı tekrarlı\nkelime defteri"),
            ("feature/stats/Stats.png", YELLOW, "İlerlemeni\nadım adım gör"),
        ],
    },
    "en": {
        "game": "Turkish clue,\nEnglish word",
        "end": "Play\nnow",
        "stills": [
            ("feature/levels/LevelSelectNatPracticeEn.png", BLUE, "Six levels,\nA1 to native"),
            ("feature/game/ResultScreenLongTextEnglish.png", RED, "Review every\nword you missed"),
            ("feature/notebook/NotebookWordSheetEn.png", INK, "A notebook with\nspaced repetition"),
            ("feature/stats/StatsEn.png", YELLOW, "Watch your\nprogress grow"),
            ("feature/game/JokerSheetEnglish.png", BLUE, "Stuck?\nUse a joker"),
        ],
    },
}


def ease_out(t: float) -> float:
    return 1 - (1 - t) ** 3


class Layout:
    """Where the phone screen sits on the canvas, and the background (headline, accents) behind it."""

    def __init__(self, size: tuple[int, int]):
        self.size = size
        self.landscape = size[0] > size[1]
        self.cache: dict[tuple, Image.Image] = {}

    def slot(self) -> tuple[int, int, int, int]:
        w, h = self.size
        if not self.landscape:
            _, x, top, sw, sh = phone_layout(self.size, (780, 1688))
            return x, top, sw, sh
        sh = h - 120
        sw = int(sh * 780 / 1688)
        return int(w * 0.68) - sw // 2, 60, sw, sh

    def background(self, band, headline: str) -> Image.Image:
        key = (band, headline)
        if key not in self.cache:
            self.cache[key] = self._landscape(band, headline) if self.landscape else portrait_background(
                self.size, band, headline)
        return self.cache[key]

    def _landscape(self, band, headline: str) -> Image.Image:
        w, h = self.size
        u = h / 1080
        img = Image.new("RGB", self.size, CREAM)
        d = ImageDraw.Draw(img)
        x, top, sw, sh = self.slot()
        d.ellipse((x + sw - int(120 * u), int(120 * u), x + sw + int(260 * u), int(500 * u)),
                  fill=YELLOW if band != YELLOW else BLUE)
        d.rectangle((x - int(200 * u), h - int(330 * u), x + int(40 * u), h - int(90 * u)),
                    fill=BLUE if band != BLUE else RED)
        panel = int(w * 0.42)
        headline_band(img, (0, 0, panel, h), band, headline, u * 1.1)
        d.rectangle((panel, 0, panel + int(8 * u), h), fill=INK)
        if band != INK:  # the ink "I" tile would vanish on an ink panel
            logo_tiles(img, int(72 * u), h - int(150 * u), int(64 * u), int(8 * u))
        border, shadow = max(4, int(8 * u)), int(18 * u)
        d.rectangle((x + shadow, top + shadow, x + sw + shadow, top + sh + shadow), fill=INK)
        d.rectangle((x - border, top - border, x + sw + border - 1, top + sh + border - 1), fill=INK)
        return img


def compose(layout: Layout, band, headline: str, screen: Image.Image, prev: Image.Image | None = None,
            slide: float = 1.0) -> Image.Image:
    """One video frame: background + screen in the slot; during a transition the new screen slides in 24 dp."""
    img = layout.background(band, headline).copy()
    x, top, sw, sh = layout.slot()
    offset = int((1 - ease_out(slide)) * 24 * sw / 390)
    frame = screen.resize((sw, sh), Image.LANCZOS)
    if offset:
        img.paste(frame.crop((0, 0, sw - offset, sh)), (x + offset, top))
        if prev is not None:  # the old screen stays under the gap (no fade, motion.md #12)
            img.paste(prev.resize((sw, sh), Image.LANCZOS).crop((0, 0, offset, sh)), (x, top))
    else:
        img.paste(frame, (x, top))
    return img


def frames(lang: str, layout: Layout):
    """Yields (image, [(seconds, sound)]) per output frame for the whole video."""
    footage = FOOTAGE / lang
    game = sorted(footage.glob("frame_*.png"))
    splash = sorted(footage.glob("splash_*.png"))
    if not game or not splash:
        raise SystemExit(f"no footage in {footage}: run the store-graphics workflow (docs/RELEASE.md §9)")
    scene = SCENES[lang]
    events = []
    for line in (footage / "timeline.txt").read_text().split("\n"):
        if line.strip():
            ms, sound = line.split()
            events.append((int(ms) / 1000, sound))

    # 1. Gameplay footage (real frames, ≈30 fps).
    game_s = len(game) * FOOTAGE_STEP
    t = 0.0
    n = int(game_s * FPS)
    for i in range(n):
        src = Image.open(game[min(len(game) - 1, int(i / FPS / FOOTAGE_STEP))]).convert("RGB")
        yield compose(layout, RED, scene["game"], src), []
    t += n / FPS
    yield None, [(t0, s) for t0, s in events]
    prev = Image.open(game[-1]).convert("RGB")

    # 2. Stills with the app's screen transition.
    for name, band, headline in scene["stills"]:
        src = Image.open(resolve(name, None, lang)).convert("RGB")
        steps = int(STILL_S * FPS)
        yield None, [(t, "tap")]
        for i in range(steps):
            yield compose(layout, band, headline, src, prev, min(1.0, i / FPS / SLIDE_S)), []
        t += steps / FPS
        prev = src

    # 3. The app's splash, then hold.
    for i in range(int((len(splash) * FOOTAGE_STEP + SPLASH_HOLD_S) * FPS)):
        src = Image.open(splash[min(len(splash) - 1, int(i / FPS / FOOTAGE_STEP))]).convert("RGB")
        yield compose(layout, INK, scene["end"], src), []


def load_wav(name: str) -> np.ndarray:
    with wave.open(str(SOUNDS / f"{name}.wav")) as w:
        data = np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16).astype(np.float32) / 32768
        if w.getnchannels() == 2:
            data = data.reshape(-1, 2).mean(axis=1)
        rate = w.getframerate()
    n = int(len(data) * RATE / rate)
    return np.interp(np.linspace(0, len(data) - 1, n), np.arange(len(data)), data)


def mix(events: list[tuple[float, str]], seconds: float) -> np.ndarray:
    track = np.zeros(int(seconds * RATE) + RATE, dtype=np.float32)
    cache: dict[str, np.ndarray] = {}
    for at, name in events:
        clip = cache.setdefault(name, load_wav(name))
        start = int(at * RATE)
        end = min(len(track), start + len(clip))
        track[start:end] += clip[: end - start] * 0.8
    track = np.clip(track[: int(seconds * RATE)], -1, 1)
    return (np.stack([track, track], axis=1) * 32767).astype(np.int16)


def render(lang: str, size: tuple[int, int], out: Path, tmp: Path) -> None:
    layout = Layout(size)
    out.parent.mkdir(parents=True, exist_ok=True)
    video = tmp / f"{out.stem}.h264.mp4"
    ff = subprocess.Popen(
        ["ffmpeg", "-y", "-loglevel", "error", "-f", "rawvideo", "-pix_fmt", "rgb24", "-s", f"{size[0]}x{size[1]}",
         "-r", str(FPS), "-i", "-", "-c:v", "libx264", "-profile:v", "high", "-level", "4.0", "-pix_fmt", "yuv420p",
         "-crf", "18", "-preset", "slow", "-r", str(FPS), str(video)],
        stdin=subprocess.PIPE,
    )
    events, count = [], 0
    for image, sounds in frames(lang, layout):
        events += sounds
        if image is not None:
            ff.stdin.write(image.tobytes())
            count += 1
    ff.stdin.close()
    if ff.wait() != 0:
        raise SystemExit("ffmpeg failed")
    seconds = count / FPS
    audio = tmp / f"{out.stem}.wav"
    with wave.open(str(audio), "wb") as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(mix(events, seconds).tobytes())
    subprocess.run(
        ["ffmpeg", "-y", "-loglevel", "error", "-i", str(video), "-i", str(audio), "-c:v", "copy", "-c:a", "aac",
         "-b:a", "256k", "-ar", str(RATE), "-ac", "2", "-shortest", "-movflags", "+faststart", str(out)],
        check=True,
    )
    print(out.relative_to(ROOT), size, f"{seconds:.1f}s")


def main() -> None:
    tmp = ROOT / "build" / "store-video"
    tmp.mkdir(parents=True, exist_ok=True)
    for lang in ("tr", "en"):
        render(lang, (886, 1920), OUT / "appstore" / "preview" / f"{lang}-886x1920.mp4", tmp)
        render(lang, (1920, 1080), OUT / "play" / f"promo-video-{lang}-1920x1080.mp4", tmp)


if __name__ == "__main__":
    main()
