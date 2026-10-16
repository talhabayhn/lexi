#!/usr/bin/env python3
"""Lexi sound set generator: calm, warm, minimal UI sounds in C major pentatonic.

One command renders everything (deterministic, seeded; only dependency: numpy):

    python3 tools/sound-gen/generate.py                # render A/B/C, previews, install set A into the app
    python3 tools/sound-gen/generate.py --install C    # install another candidate set
    python3 tools/sound-gen/generate.py --install none # render only

Outputs
  tools/sound-gen/out/{a-kalimba,b-glass,c-felt}/*.wav   candidate sets (git-ignored build output)
  docs/design/sound-previews/<set>/*.ogg + _reel.ogg       listening copies (WAV if ffmpeg is missing)
  shared/src/commonMain/composeResources/files/sounds/     the installed set (mono 16-bit 22.05 kHz WAV)

Synthesis is physically informed, never raw oscillators: modal/additive partials with inharmonic ratios and
individual exponential decays, a soft noise mallet/hammer transient, 3-8 ms raised-cosine attacks, random
detune/phase per partial, gentle saturation, a 4th-order low-pass (little energy above ~8 kHz) and a short
small-room reverb from a synthetic impulse response. See docs/design/sound.md for the sound list and intent.
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
ROOT = Path(__file__).resolve().parents[2]
OUT_DIR = Path(__file__).resolve().parent / "out"
PREVIEW_DIR = ROOT / "docs/design/sound-previews"
APP_DIR = ROOT / "shared/src/commonMain/composeResources/files/sounds"

PENTATONIC = {0, 2, 4, 7, 9}  # C D E G A
NAMES = {"C": 0, "D": 2, "E": 4, "G": 7, "A": 9}


def hz(note):
    """'C5' -> frequency. Only C major pentatonic notes exist, so every combination stays consonant."""
    pc, octave = NAMES[note[0]], int(note[1:])
    assert pc in PENTATONIC
    return 440.0 * 2 ** ((12 * (octave + 1) + pc - 69) / 12)


# ---------------------------------------------------------------- dsp primitives

def biquad(x, kind, fc, q=0.7071):
    """RBJ biquad (causal), kind = 'lp' | 'hp' | 'bp'."""
    w0 = 2 * np.pi * fc / SR
    alpha = np.sin(w0) / (2 * q)
    c = np.cos(w0)
    if kind == "lp":
        b = [(1 - c) / 2, 1 - c, (1 - c) / 2]
    elif kind == "hp":
        b = [(1 + c) / 2, -(1 + c), (1 + c) / 2]
    else:
        b = [alpha, 0.0, -alpha]
    a0, a1, a2 = 1 + alpha, -2 * c, 1 - alpha
    b0, b1, b2 = (v / a0 for v in b)
    a1, a2 = a1 / a0, a2 / a0
    y = np.zeros_like(x)
    x1 = x2 = y1 = y2 = 0.0
    for i, xi in enumerate(x.tolist()):
        yi = b0 * xi + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2, x1, y2, y1 = x1, xi, y1, yi
        y[i] = yi
    return y


def ramp(n, ms):
    """Raised-cosine attack envelope of `ms` (no clicks)."""
    k = max(2, int(SR * ms / 1000))
    env = np.ones(n)
    k = min(k, n)
    env[:k] = 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, k))
    return env


def modes(f0, partials, n, rng, detune_cents=3.0):
    """Sum of decaying sine modes: partials = [(ratio, amp, tau_s), ...]."""
    t = np.arange(n) / SR
    out = np.zeros(n)
    for ratio, amp, tau in partials:
        f = f0 * ratio * 2 ** (rng.uniform(-detune_cents, detune_cents) / 1200)
        if f > 0.42 * SR or amp <= 0:
            continue
        out += amp * np.exp(-t / tau) * np.sin(2 * np.pi * f * t + rng.uniform(0, 2 * np.pi))
    return out


def noise_burst(n, rng, fc, level, decay_ms, kind="lp", q=0.7071):
    """Soft mallet / hammer transient: filtered noise, 1 ms rise, fast exponential fall."""
    m = min(n, int(SR * 0.04))
    t = np.arange(m) / SR
    burst = rng.standard_normal(m) * np.exp(-t / (decay_ms / 1000)) * ramp(m, 1.0)
    burst = biquad(burst, kind, fc, q)
    burst /= np.max(np.abs(burst)) + 1e-12
    out = np.zeros(n)
    out[:m] = burst * level
    return out


# ---------------------------------------------------------------- instruments (one per candidate set)

def marimba(f, vel, n, rng, mute=1.0):
    """(A) Wooden bar: 1 : 3.98 : 9.2 bar modes, tube resonance on the fundamental, felt-wrapped mallet."""
    tau = float(np.clip(0.42 * (523 / f) ** 0.6, 0.10, 0.9)) * mute
    p = [
        (1.0, 1.0, tau),
        (1.0015, 0.25, tau * 1.4),  # resonator tube, slightly detuned: slow warm beating
        (3.98, 0.22 * vel, tau * 0.22),
        (9.2, 0.05 * vel, tau * 0.08),
    ]
    x = modes(f, p, n, rng)
    x += noise_burst(n, rng, 900 + 1600 * vel, 0.06 * vel, 3.5, "bp", 0.9)
    return x * ramp(n, 4.0)


def glass(f, vel, n, rng, mute=1.0):
    """(B) Soft glass / small bell: free-bar inharmonic ratios, each mode doubled with a ~0.7 Hz beat."""
    tau = float(np.clip(0.75 * (523 / f) ** 0.5, 0.2, 1.4)) * mute
    p = []
    for ratio, amp, k in ((1.0, 1.0, 1.0), (2.756, 0.20 * vel, 0.45), (5.404, 0.06 * vel, 0.22), (8.933, 0.02, 0.12)):
        p.append((ratio, amp, tau * k))
        p.append((ratio * (1 + 0.7 / (f * ratio)), amp * 0.5, tau * k * 1.1))
    x = modes(f, p, n, rng, detune_cents=1.5)
    x += noise_burst(n, rng, 2600, 0.025 * vel, 2.0)
    return x * ramp(n, 6.0)


def felt(f, vel, n, rng, mute=1.0):
    """(C) Felt piano / soft pluck: stiff-string partials (inharmonicity B), felt-damped highs, double decay."""
    tau = float(np.clip(0.9 * (262 / f) ** 0.5, 0.2, 1.3)) * mute
    b_inh = 0.00035
    fc = 900 + 1500 * vel
    t = np.arange(n) / SR
    x = np.zeros(n)
    for k in range(1, 11):
        fk = k * f * np.sqrt(1 + b_inh * k * k) * 2 ** (rng.uniform(-2, 2) / 1200)
        if fk > 0.42 * SR:
            break
        amp = k ** -1.6 / (1 + (fk / fc) ** 2)
        tk = tau / (1 + 0.45 * (k - 1))
        env = 0.65 * np.exp(-t / (tk * 0.28)) + 0.35 * np.exp(-t / tk)
        x += amp * env * np.sin(2 * np.pi * fk * t + rng.uniform(0, 2 * np.pi))
    x /= np.max(np.abs(x)) + 1e-12
    x += noise_burst(n, rng, 500, 0.07 * vel, 6.0)  # felt hammer thump
    return x * ramp(n, 5.0)


SETS = {
    "A": ("a-kalimba", marimba, {"lp": 6500, "rt": 1.0, "wet": 0.11}),
    "B": ("b-glass", glass, {"lp": 7000, "rt": 1.1, "wet": 0.13}),
    "C": ("c-felt", felt, {"lp": 6000, "rt": 1.0, "wet": 0.10}),
}


# ---------------------------------------------------------------- room + mastering

def room_ir(rt, rng):
    """Small-room impulse response: a few early reflections + exponentially decaying, darkening noise tail."""
    n = int(SR * rt)
    t = np.arange(n) / SR
    tail = rng.standard_normal(n) * np.exp(-6.9 * t / rt)  # -60 dB at rt
    tail = biquad(biquad(tail, "lp", 3200), "hp", 200)
    ir = np.zeros(n)
    pre = int(SR * 0.006)
    ir[pre:] = tail[: n - pre]
    for ms, g in ((7, 0.5), (11, -0.35), (17, 0.28), (23, -0.2), (31, 0.14)):
        i = int(SR * ms / 1000)
        if i < n:
            ir[i] += g * np.max(np.abs(tail))
    return ir / np.sqrt(np.sum(ir**2))


def master(x, spec, rng):
    """Low-pass, DC/rumble cut, soft saturation, reverb, end fade, decimate to 22.05 kHz."""
    x = biquad(biquad(x, "lp", spec["lp"]), "lp", spec["lp"])
    x = biquad(x, "hp", 110)
    x = x / (np.max(np.abs(x)) + 1e-12) * 0.8
    x = np.tanh(1.4 * x) / np.tanh(1.4)  # subtle saturation (warmth, rounds the peaks)
    ir = room_ir(spec["rt"], rng)
    wet = np.fft.irfft(np.fft.rfft(x, len(x) + len(ir)) * np.fft.rfft(ir, len(x) + len(ir)))[: len(x)]
    wet *= np.sqrt(np.mean(x**2)) / (np.sqrt(np.mean(wet**2)) + 1e-12)
    w = spec["wet"]
    y = (1 - w) * x + w * wet
    fade = max(int(SR * 0.015), int(len(y) * 0.35))  # long, natural release: the tail never gets cut short
    y[-fade:] *= (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fade))) ** 1.5
    # anti-alias (zero-phase, smooth taper 8.5-10.5 kHz) then decimate by 2
    pad = len(y) + 2048
    spec_y = np.fft.rfft(y, pad)
    f = np.fft.rfftfreq(pad, 1 / SR)
    spec_y *= np.clip((10500 - f) / 2000, 0, 1)
    y = np.fft.irfft(spec_y, pad)[: len(y)][::2]
    return y - np.mean(y)


def to_int16(y, peak_db, rng):
    y = y / (np.max(np.abs(y)) + 1e-12) * 10 ** (peak_db / 20)
    tpdf = (rng.uniform(-0.5, 0.5, len(y)) + rng.uniform(-0.5, 0.5, len(y))) / 32768
    return np.clip(np.round((y + tpdf) * 32767), -32768, 32767).astype(np.int16)


# ---------------------------------------------------------------- sound list
# name: (duration_s, peak_dBFS, reverb rt (s, IR length ~ tail), [(time_s, note | (notes...), velocity, mute)])
# mute < 1 shortens every decay (muted / damped strike). Frequent sounds: 40-120 ms, quiet, short tail.

FREQUENT = {"tap", "place", "backspace", "timer_tick", "tick"}
VARIANTS = 3

SOUNDS = {
    # --- existing files (referenced by SoundEffect; names, format and approx. durations kept)
    "tap": (0.07, -19, 0.12, [(0, "G5", 0.35, 0.18)]),
    "place": (0.08, -21, 0.12, [(0, "D5", 0.3, 0.2)]),
    "correct": (0.28, -8, 0.28, [(0, "G5", 0.65, 0.5), (0.09, "C6", 0.8, 0.6)]),
    "wrong": (0.30, -13, 0.25, [(0, "E4", 0.4, 0.22), (0.11, "C4", 0.35, 0.28)]),
    "pass": (0.26, -14, 0.25, [(0, "C5", 0.3, 0.3), (0.045, "D5", 0.3, 0.3), (0.09, "E5", 0.35, 0.4)]),
    "combo": (0.40, -8, 0.40, [(0, "C5", 0.5, 0.5), (0.05, "E5", 0.55, 0.5), (0.10, "G5", 0.6, 0.6), (0.15, "C6", 0.75, 0.8)]),
    "tick": (0.12, -18, 0.15, [(0, "A4", 0.35, 0.25)]),
    "win": (0.90, -7, 0.70, [(0, "C5", 0.5, 1), (0.10, "E5", 0.55, 1), (0.20, "G5", 0.6, 1), (0.32, ("C6", "E5", "G5"), 0.7, 1.2)]),
    "lose": (0.80, -11, 0.60, [(0, "A4", 0.4, 0.8), (0.14, "G4", 0.38, 0.8), (0.28, "E4", 0.36, 0.8), (0.42, ("C4", "G4"), 0.38, 1)]),
    # --- new files (ready to wire, see docs/design/sound.md)
    "button": (0.09, -17, 0.15, [(0, "C5", 0.4, 0.25)]),
    "backspace": (0.07, -21, 0.12, [(0, "E5", 0.25, 0.15)]),
    "timer_tick": (0.09, -28, 0.15, [(0, "A5", 0.2, 0.2)]),
    "warning_1": (0.14, -19, 0.18, [(0, "G4", 0.3, 0.3)]),
    "warning_2": (0.14, -17, 0.18, [(0, "A4", 0.4, 0.3)]),
    "warning_3": (0.18, -15, 0.18, [(0, "C5", 0.45, 0.28), (0.085, "C5", 0.3, 0.28)]),
    "time_up": (0.70, -11, 0.60, [(0, ("C5", "G4"), 0.5, 1.0), (0.16, "C4", 0.4, 1.0)]),
    "joker": (0.38, -12, 0.35, [(0, "G5", 0.4, 0.5), (0.035, "A5", 0.45, 0.5), (0.07, "D6", 0.5, 0.8)]),
    "gift": (0.65, -10, 0.55, [(0, "E5", 0.45, 0.8), (0.06, "G5", 0.5, 0.8), (0.12, "C6", 0.55, 0.9), (0.18, "E6", 0.5, 1.1)]),
    "countdown": (0.22, -14, 0.25, [(0, "C5", 0.5, 0.45)]),
    "countdown_go": (0.38, -11, 0.35, [(0, ("G5", "C6"), 0.6, 0.8)]),
    "best": (0.85, -7, 0.70, [(0, "C5", 0.5, 0.7), (0.07, "E5", 0.5, 0.7), (0.14, "G5", 0.55, 0.7), (0.21, "A5", 0.6, 0.8), (0.30, ("C6", "G5", "E5"), 0.75, 1.2)]),
    "dialog_open": (0.18, -20, 0.2, [(0, "D5", 0.3, 0.3), (0.04, "G5", 0.3, 0.35)]),
    "dialog_close": (0.18, -21, 0.2, [(0, "G5", 0.28, 0.3), (0.04, "D5", 0.28, 0.3)]),
    "toast": (0.25, -20, 0.25, [(0, "A5", 0.3, 0.5)]),
    "notebook_add": (0.20, -17, 0.22, [(0, "E5", 0.4, 0.35), (0.05, "A5", 0.45, 0.45)]),
    "notebook_remove": (0.18, -19, 0.2, [(0, "A5", 0.3, 0.25), (0.05, "E5", 0.3, 0.22)]),
    "daily_complete": (1.00, -7, 0.70, [(0, "G4", 0.45, 1), (0.09, "C5", 0.5, 1), (0.18, "E5", 0.55, 1), (0.27, "G5", 0.6, 1), (0.38, ("C6", "E5", "G4"), 0.7, 1.3)]),
}


def render(set_key, name, variant=0):
    label, instrument, spec = SETS[set_key]
    dur, peak_db, rt, notes = SOUNDS[name]
    rng = np.random.default_rng(zlib.crc32(f"{set_key}/{name}/{variant}".encode()))
    n = int(SR * dur)
    x = np.zeros(n)
    for t0, note, vel, mute in notes:
        chord = note if isinstance(note, tuple) else (note,)
        if variant:  # subtle round-robin: a few cents, +-10 % velocity, fresh mallet noise
            vel = float(np.clip(vel * rng.uniform(0.9, 1.1), 0.05, 1.0))
        for i, nn in enumerate(chord):
            f = hz(nn) * (2 ** (rng.uniform(-6, 6) / 1200) if variant else 1.0)
            start = int(SR * t0) + int(SR * 0.012) * i  # chords are softly rolled, never a block hit
            if start >= n:
                continue
            x[start:] += instrument(f, vel, n - start, rng, mute) * (0.8 if i else 1.0)
    room = dict(spec, rt=rt)
    y = master(x, room, rng)
    return to_int16(y, peak_db, rng)


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
    f = np.fft.rfftfreq(len(y), 1 / OUT_SR)
    hi = spec[f > 8000].sum() / (spec.sum() + 1e-12)
    assert abs(y[0]) < 0.01 and abs(y[-1]) < 0.01, f"{name}: click at an edge"
    assert hi < 0.01, f"{name}: {hi:.2%} energy above 8 kHz"
    return hi


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--install", default="A", choices=["A", "B", "C", "none"], help="set copied into the app")
    ap.add_argument("--no-previews", action="store_true")
    args = ap.parse_args()

    ffmpeg = shutil.which("ffmpeg")
    for key, (label, _, _) in SETS.items():
        out = OUT_DIR / label
        if out.exists():
            shutil.rmtree(out)
        files = []
        for name in SOUNDS:
            variants = [0] + (list(range(1, VARIANTS + 1)) if name in FREQUENT else [])
            for v in variants:
                fname = f"{name}.wav" if v == 0 else f"{name}_{v}.wav"
                pcm = render(key, name, v)
                hi = check(f"{label}/{fname}", pcm)
                write_wav(out / fname, pcm)
                files.append(out / fname)
                if v == 0:
                    print(f"{label:10} {name:16} {len(pcm) / OUT_SR * 1000:5.0f} ms  >8k {hi:.3%}")
        if not args.no_previews:
            write_previews(label, files, ffmpeg)

    if args.install != "none":
        label = SETS[args.install][0]
        for src in sorted((OUT_DIR / label).glob("*.wav")):
            shutil.copyfile(src, APP_DIR / src.name)
        print(f"installed set {args.install} ({label}) into {APP_DIR.relative_to(ROOT)}")


def write_previews(label, files, ffmpeg):
    dest = PREVIEW_DIR / label
    if dest.exists():
        shutil.rmtree(dest)
    dest.mkdir(parents=True)
    gap = np.zeros(int(OUT_SR * 0.45), dtype=np.int16)
    reel = []
    for f in files:
        with wave.open(str(f)) as w:
            reel += [np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16), gap]
    reel_path = dest / "_reel.wav"
    write_wav(reel_path, np.concatenate(reel))
    if not ffmpeg:
        for f in files:
            shutil.copyfile(f, dest / f.name)
        return
    for src in files + [reel_path]:
        target = dest / (src.stem + ".ogg")
        subprocess.run(
            [ffmpeg, "-loglevel", "error", "-y", "-i", str(src), "-map_metadata", "-1", "-fflags", "+bitexact",
             "-flags:a", "+bitexact", "-c:a", "libvorbis", "-q:a", "4", str(target)],
            check=True,
        )
    reel_path.unlink()


if __name__ == "__main__":
    sys.exit(main())
