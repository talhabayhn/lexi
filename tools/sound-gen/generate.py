#!/usr/bin/env python3
"""Lexi sound set generator: warm, minimal sounds for a word-puzzle game, built from real recordings.

Concept: a Bauhaus letter board. Frequent UI sounds are real wood (woodblock, slit drum, claves played softly,
pitched down and darkened); answers, rewards and the timer are short C major pentatonic gestures on one real
acoustic instrument. Sources are CC0 recordings from VCSL (see fetch_samples.py), vendored as short tuned excerpts
in tools/sound-gen/samples/, so this script runs offline and deterministically (seeded; only dependency: numpy):

    python3 tools/sound-gen/generate.py                # render A-D, previews, install set A into the app
    python3 tools/sound-gen/generate.py --install C    # install another candidate set
    python3 tools/sound-gen/generate.py --install none # render only

Outputs
  tools/sound-gen/out/<set>/*.wav                       candidate sets (git-ignored build output)
  docs/design/sound-previews/<set>/*.ogg + _reel.ogg    listening copies (WAV if ffmpeg is missing)
  shared/src/commonMain/composeResources/files/sounds/  the installed set (mono 16-bit 22.05 kHz WAV)

Processing is light on purpose: pitch by resampling (instrument notes at most a few semitones), velocity as level + brightness,
optional damping, rolled chords and human timing, gentle EQ/low-pass, a small wooden room (synthetic IR), a
natural release and peak normalisation. See docs/design/sound.md for the sound list and intent.
"""
import argparse
import shutil
import subprocess
import sys
import wave
import zlib
from pathlib import Path

import numpy as np

SR = 44100  # internal render rate
OUT_SR = 22050  # shipped rate (matches SoundPool / AVAudioPlayer assets)
HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
SAMPLE_DIR = HERE / "samples"
OUT_DIR = HERE / "out"
PREVIEW_DIR = ROOT / "docs/design/sound-previews"
APP_DIR = ROOT / "shared/src/commonMain/composeResources/files/sounds"

PC = {"C": 0, "C#": 1, "D": 2, "D#": 3, "E": 4, "F": 5, "F#": 6, "G": 7, "G#": 8, "A": 9, "A#": 10, "B": 11}
PENTATONIC = {0, 2, 4, 7, 9}  # C D E G A: every overlap stays consonant


def midi(note):
    return 12 * (int(note[-1]) + 1) + PC[note[:-1]]


# ---------------------------------------------------------------- dsp

def biquad(x, kind, fc, q=0.7071, gain_db=0.0):
    """RBJ biquad (causal): 'lp' | 'hp' | 'peak'."""
    w0 = 2 * np.pi * fc / SR
    alpha = np.sin(w0) / (2 * q)
    c = np.cos(w0)
    if kind == "lp":
        b, a = [(1 - c) / 2, 1 - c, (1 - c) / 2], [1 + alpha, -2 * c, 1 - alpha]
    elif kind == "hp":
        b, a = [(1 + c) / 2, -(1 + c), (1 + c) / 2], [1 + alpha, -2 * c, 1 - alpha]
    else:
        g = 10 ** (gain_db / 40)
        b, a = [1 + alpha * g, -2 * c, 1 - alpha * g], [1 + alpha / g, -2 * c, 1 - alpha / g]
    b0, b1, b2 = (v / a[0] for v in b)
    a1, a2 = a[1] / a[0], a[2] / a[0]
    y = np.zeros_like(x)
    x1 = x2 = y1 = y2 = 0.0
    for i, xi in enumerate(x.tolist()):
        yi = b0 * xi + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2, x1, y2, y1 = x1, xi, y1, yi
        y[i] = yi
    return y


def read_wav(path):
    with wave.open(str(path)) as w:
        x = np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16) / 32768.0
        rate = w.getframerate()
    return np.interp(np.arange(len(x) * SR // rate) * rate / SR, np.arange(len(x)), x)  # content < 10.5 kHz


def repitch(x, ratio, n):
    """Play `x` `ratio` times faster (ratio 2 = one octave up) into `n` samples."""
    t = np.arange(n) * ratio
    y = np.interp(t, np.arange(len(x)), x, right=0.0)
    return y


def load(folder):
    return {p.stem: read_wav(p) for p in sorted((SAMPLE_DIR / folder).glob("*.wav"))}


# ---------------------------------------------------------------- instruments

class Sampler:
    """Pitched instrument from tuned excerpts: nearest sample, resampled to the note."""

    def __init__(self, folder):
        self.samples = {midi(k): v for k, v in load(folder).items()}

    def __call__(self, note, vel, n, rng, mute=1.0, cents=0.0):
        m = midi(note)
        src = min(self.samples, key=lambda s: (abs(s - m), s))
        ratio = 2 ** ((m - src) / 12 + cents / 1200)
        y = repitch(self.samples[src], ratio, n)
        y = biquad(y, "lp", 1400 + 5000 * vel**1.2)  # softer touch = darker, like a real instrument
        if mute < 1.0:  # damped: a hand or felt stops the ring
            t = np.arange(n) / SR
            y *= np.exp(-t / (0.35 * mute))
        return y * vel**1.3


WOOD = None


def wood(name, vel, n, semitones=0.0, lp=4000.0, decay=None):
    """Real wood (woodblock, slit drum, claves) pitched down and darkened into a soft tactile knock."""
    y = repitch(WOOD[name], 2 ** (semitones / 12), n)
    y = biquad(biquad(y, "lp", lp), "lp", lp)
    if decay:
        y *= np.exp(-np.arange(n) / SR / decay)
    return y * vel


SETS = {
    "A": ("a-piano", "piano", {"lp": 6500, "eq": [(250, 1.5, 0.8)], "rt_scale": 1.0}),
    "B": ("b-kalimba", "kalimba", {"lp": 6500, "eq": [(240, 2.0, 1.0)], "rt_scale": 1.0}),
    "C": ("c-marimba", "marimba", {"lp": 6000, "eq": [(200, 1.5, 0.9)], "rt_scale": 1.0}),
    "D": ("d-vibraphone", "vibes", {"lp": 6000, "eq": [(250, 1.0, 0.9)], "rt_scale": 1.1}),
}


# ---------------------------------------------------------------- room + mastering

def room_ir(rt, rng):
    """Small wooden room: smoothed early cluster + darkening exponential tail, -60 dB at rt."""
    n = int(SR * rt)
    t = np.arange(n) / SR
    tail = rng.standard_normal(n) * np.exp(-6.9 * t / rt) * (1 - np.exp(-t / 0.004))
    tail = biquad(biquad(tail, "lp", 2800), "hp", 160)
    early = np.zeros(n)
    for ms in rng.uniform(4, 28, 9):
        i = int(SR * ms / 1000)
        if i < n:
            early[i] += rng.uniform(-1, 1) * np.exp(-ms / 20)
    early = biquad(early, "lp", 3000)
    ir = tail / np.max(np.abs(tail)) + 0.8 * early / (np.max(np.abs(early)) + 1e-12)
    return ir / np.sqrt(np.sum(ir**2))


def master(x, spec, rt, wet_amount, rng):
    for fc, g, q in spec["eq"]:
        x = biquad(x, "peak", fc, q, g)
    x = biquad(biquad(x, "lp", spec["lp"]), "lp", spec["lp"])
    x = biquad(x, "hp", 90)
    ir = room_ir(rt * spec["rt_scale"], rng)
    size = len(x) + len(ir)
    wet = np.fft.irfft(np.fft.rfft(x, size) * np.fft.rfft(ir, size))[: len(x)]
    wet *= np.sqrt(np.mean(x**2)) / (np.sqrt(np.mean(wet**2)) + 1e-12)
    y = (1 - wet_amount) * x + wet_amount * wet
    fade = max(int(SR * 0.015), int(len(y) * 0.35))  # natural release, never a cut
    y[-fade:] *= (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fade))) ** 1.5
    y[: int(SR * 0.002)] *= np.linspace(0, 1, int(SR * 0.002))
    pad = len(y) + 2048  # zero-phase anti-alias taper 8.5-10.5 kHz, then decimate by 2
    spec_y = np.fft.rfft(y, pad) * np.clip((10500 - np.fft.rfftfreq(pad, 1 / SR)) / 2000, 0, 1)
    y = np.fft.irfft(spec_y, pad)[: len(y)][::2]
    return y - np.mean(y)


def to_int16(y, peak_db, rng):
    y = y / (np.max(np.abs(y)) + 1e-12) * 10 ** (peak_db / 20)
    tpdf = (rng.uniform(-0.5, 0.5, len(y)) + rng.uniform(-0.5, 0.5, len(y))) / 32768
    return np.clip(np.round((y + tpdf) * 32767), -32768, 32767).astype(np.int16)


# ---------------------------------------------------------------- sound list

class Take:
    """One rendering: places instrument notes and wood on a timeline with light human timing."""

    def __init__(self, instrument, dur, rng, variant):
        self.instrument, self.rng, self.variant = instrument, rng, variant
        self.x = np.zeros(int(SR * dur))

    def _at(self, t0):
        start = int(SR * (t0 + (self.rng.uniform(0, 0.003) if t0 else 0)))
        return start, len(self.x) - start

    def _vel(self, vel):
        return float(np.clip(vel * self.rng.uniform(0.9, 1.1) if self.variant else vel, 0.05, 1.0))

    def note(self, t0, notes, vel, mute=1.0):
        for i, nn in enumerate(notes.split()):  # chords are rolled like a hand, never a block hit
            assert midi(nn) % 12 in PENTATONIC, nn
            start, n = self._at(t0 + 0.018 * i)
            if n > 0:
                cents = self.rng.uniform(-4, 4) if self.variant else 0.0
                self.x[start:] += self.instrument(nn, self._vel(vel), n, self.rng, mute, cents) * (0.8 if i else 1.0)
        return self

    def wood(self, t0, name, vel, semitones=0.0, lp=4000.0, decay=None):
        if name == "block":  # round robin over the three real woodblock strokes
            name = f"block_{self.variant % 3 + 1}"
        start, n = self._at(t0)
        if n > 0:
            shift = semitones + (self.rng.uniform(-0.4, 0.4) if self.variant else 0.0)
            self.x[start:] += wood(name, self._vel(vel), n, shift, lp, decay)
        return self


# name: (duration_s, peak_dBFS, room rt_s, reverb wet, frequent?, score(take))
SOUNDS = {
    # --- existing files (referenced by SoundEffect; names and format kept, durations close)
    "tap": (0.07, -19, 0.12, 0.08, True, lambda k: k.wood(0, "block", 0.6, -5, 3000)),
    "place": (0.09, -20, 0.14, 0.08, True,
              lambda k: k.wood(0, "block_soft", 0.6, -4, 3000).wood(0, "slit_hi", 0.15, -3, 2500, 0.05)),
    "correct": (0.28, -9, 0.3, 0.12, False, lambda k: k.note(0, "G4", 0.55).note(0.085, "C5", 0.65)),
    "wrong": (0.30, -14, 0.25, 0.1, False,
              lambda k: k.wood(0, "slit_lo", 0.5, -2, 1800, 0.08).note(0.01, "E4", 0.35, 0.35)
              .note(0.12, "C4", 0.35, 0.4)),
    "pass": (0.26, -15, 0.2, 0.1, False,
             lambda k: k.wood(0, "slit_hi", 0.35, -2, 2500, 0.06).note(0.03, "D5", 0.35, 0.4)
             .note(0.1, "G4", 0.35, 0.45)),
    "combo": (0.40, -9, 0.4, 0.12, False,
              lambda k: k.note(0, "G4", 0.5).note(0.055, "C5", 0.55).note(0.11, "E5", 0.6).note(0.165, "G5", 0.65)),
    "tick": (0.12, -18, 0.15, 0.1, True,
             lambda k: k.note(0, "G4", 0.35, 0.25).wood(0, "slit_lo", 0.25, -3, 1500, 0.05)),
    "win": (0.90, -8, 0.7, 0.13, False,
            lambda k: k.note(0, "C5", 0.5).note(0.11, "E5", 0.55).note(0.22, "G5", 0.6)
            .note(0.36, "C4 G4 E5", 0.6)),
    "lose": (0.80, -12, 0.6, 0.12, False,
             lambda k: k.note(0, "A4", 0.4).note(0.15, "G4", 0.38).note(0.3, "E4", 0.36).note(0.45, "C4 G4", 0.38)),
    # --- new files (ready to wire, see docs/design/sound.md)
    "button": (0.09, -17, 0.15, 0.08, False, lambda k: k.wood(0, "slit_hi", 0.6, -2, 3000, 0.05)),
    "backspace": (0.09, -21, 0.12, 0.08, True, lambda k: k.wood(0, "claves", 0.5, -9, 2200)),
    "timer_tick": (0.09, -30, 0.15, 0.1, True, lambda k: k.note(0, "A4", 0.25, 0.2)),
    "warning_1": (0.14, -19, 0.18, 0.1, False,
                  lambda k: k.note(0, "G4", 0.3, 0.3).wood(0, "slit_lo", 0.2, -3, 1500, 0.05)),
    "warning_2": (0.14, -17, 0.18, 0.1, False,
                  lambda k: k.note(0, "A4", 0.4, 0.3).wood(0, "slit_lo", 0.25, -3, 1500, 0.05)),
    "warning_3": (0.18, -15, 0.18, 0.1, False,
                  lambda k: k.note(0, "C5", 0.45, 0.3).note(0.085, "C5", 0.3, 0.3)
                  .wood(0, "slit_lo", 0.3, -3, 1500, 0.05)),
    "time_up": (0.70, -12, 0.6, 0.12, False, lambda k: k.note(0, "G4", 0.45).note(0.16, "C4 G4", 0.4)),
    "joker": (0.38, -13, 0.35, 0.12, False,
              lambda k: k.note(0, "E5", 0.4).note(0.045, "G5", 0.45).note(0.09, "A5", 0.5)),
    "gift": (0.65, -11, 0.55, 0.12, False,
             lambda k: k.note(0, "C5", 0.45).note(0.07, "E5", 0.5).note(0.14, "G5", 0.55).note(0.21, "C6", 0.5)),
    "countdown": (0.22, -14, 0.25, 0.1, False, lambda k: k.note(0, "C5", 0.5, 0.5)),
    "countdown_go": (0.38, -11, 0.35, 0.12, False, lambda k: k.note(0, "G4 C5 E5", 0.6)),
    "best": (0.85, -8, 0.7, 0.13, False,
             lambda k: k.note(0, "C5", 0.5).note(0.07, "E5", 0.5).note(0.14, "G5", 0.55).note(0.21, "A5", 0.6)
             .note(0.32, "C4 G4 E5 C6", 0.6)),
    "dialog_open": (0.18, -22, 0.2, 0.1, False, lambda k: k.wood(0, "slit_hi", 0.4, 0, 2500, 0.05)),
    "dialog_close": (0.18, -23, 0.2, 0.1, False, lambda k: k.wood(0, "slit_lo", 0.4, 0, 2200, 0.05)),
    "toast": (0.25, -21, 0.25, 0.12, False, lambda k: k.note(0, "E5", 0.35)),
    "notebook_add": (0.24, -18, 0.22, 0.1, False,
                     lambda k: k.wood(0, "block_soft", 0.3, -4, 2800).note(0.05, "E5", 0.4).note(0.1, "A5", 0.45)),
    "notebook_remove": (0.22, -20, 0.2, 0.1, False,
                        lambda k: k.wood(0, "block_soft", 0.3, -6, 2500).note(0.05, "A4", 0.35, 0.35)),
    "daily_complete": (1.00, -8, 0.7, 0.13, False,
                       lambda k: k.note(0, "G4", 0.45).note(0.09, "C5", 0.5).note(0.18, "E5", 0.55)
                       .note(0.27, "G5", 0.6).note(0.4, "C4 G4 E5 C6", 0.6)),
}
VARIANTS = 3


def render(set_key, name, instrument, variant=0):
    spec = SETS[set_key][2]
    dur, peak_db, rt, wet, _, score = SOUNDS[name]
    rng = np.random.default_rng(zlib.crc32(f"{set_key}/{name}/{variant}".encode()))
    take = score(Take(instrument, dur, rng, variant))
    return to_int16(master(take.x, spec, rt, wet, rng), peak_db, rng)


def write_wav(path, pcm):
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(OUT_SR)
        w.writeframes(pcm.tobytes())


def check(name, pcm):
    """Guard rails: no click at the start/end, almost no energy above 8 kHz."""
    y = pcm.astype(np.float64) / 32768
    spec = np.abs(np.fft.rfft(y)) ** 2
    hi = spec[np.fft.rfftfreq(len(y), 1 / OUT_SR) > 8000].sum() / (spec.sum() + 1e-12)
    assert abs(y[0]) < 0.01 and abs(y[-1]) < 0.01, f"{name}: click at an edge"
    assert hi < 0.01, f"{name}: {hi:.2%} energy above 8 kHz"
    return hi


def main():
    global WOOD
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--install", default="A", choices=[*SETS, "none"], help="set copied into the app")
    ap.add_argument("--no-previews", action="store_true")
    args = ap.parse_args()

    WOOD = load("wood")
    ffmpeg = shutil.which("ffmpeg")
    if OUT_DIR.exists():
        for old in OUT_DIR.iterdir():
            if old.name != "vcsl":  # keep a fetch_samples.py checkout
                shutil.rmtree(old)
    if not args.no_previews and PREVIEW_DIR.exists():
        shutil.rmtree(PREVIEW_DIR)
    for key, (label, folder, _) in SETS.items():
        instrument = Sampler(folder)
        out = OUT_DIR / label
        mains, variants = [], []
        for name, (*_, frequent, _score) in SOUNDS.items():
            for v in range(VARIANTS + 1 if frequent else 1):
                fname = f"{name}.wav" if v == 0 else f"{name}_{v}.wav"
                pcm = render(key, name, instrument, v)
                hi = check(f"{label}/{fname}", pcm)
                write_wav(out / fname, pcm)
                (variants if v else mains).append(out / fname)
                if v == 0:
                    print(f"{label:13} {name:16} {len(pcm) / OUT_SR * 1000:5.0f} ms  >8k {hi:.3%}")
        if not args.no_previews:
            write_previews(label, mains, variants, ffmpeg)

    if args.install != "none":
        label = SETS[args.install][0]
        for old in APP_DIR.glob("*.wav"):
            old.unlink()
        for src in sorted((OUT_DIR / label).glob("*.wav")):
            shutil.copyfile(src, APP_DIR / src.name)
        print(f"installed set {args.install} ({label}) into {APP_DIR.relative_to(ROOT)}")


def write_previews(label, mains, variants, ffmpeg):
    """Per-sound listening copies + `_reel` (every main sound in list order, 0.45 s apart)."""
    dest = PREVIEW_DIR / label
    dest.mkdir(parents=True)
    gap = np.zeros(int(OUT_SR * 0.45), dtype=np.int16)
    reel = []
    for f in mains:
        with wave.open(str(f)) as w:
            reel += [np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16), gap]
    reel_path = dest / "_reel.wav"
    write_wav(reel_path, np.concatenate(reel))
    if not ffmpeg:
        for f in mains + variants:
            shutil.copyfile(f, dest / f.name)
        return
    for src in mains + variants + [reel_path]:
        subprocess.run(
            [ffmpeg, "-loglevel", "error", "-y", "-i", str(src), "-map_metadata", "-1", "-fflags", "+bitexact",
             "-flags:a", "+bitexact", "-c:a", "libvorbis", "-q:a", "4", str(dest / (src.stem + ".ogg"))],
            check=True,
        )
    reel_path.unlink()


if __name__ == "__main__":
    sys.exit(main())
