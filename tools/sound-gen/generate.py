#!/usr/bin/env python3
"""Lexi sound set generator: warm, tactile, minimal sounds for a word-puzzle game (C major pentatonic).

Concept: a Bauhaus letter board — wooden letter tiles, paper cards and one warm acoustic instrument.
Frequent UI sounds are tactile (tile on a wooden board, a paper card turning); rewards and the timer are short
melodic gestures on the set's instrument. Same file names in every set, so any set can be shipped.

One command renders everything (deterministic, seeded; only dependency: numpy):

    python3 tools/sound-gen/generate.py                # render A/B/C, previews, install set A into the app
    python3 tools/sound-gen/generate.py --install C    # install another candidate set
    python3 tools/sound-gen/generate.py --install none # render only

Outputs
  tools/sound-gen/out/{a-felt-piano,b-kalimba,c-marimba}/*.wav   candidate sets (git-ignored build output)
  docs/design/sound-previews/<set>/*.ogg + _reel.ogg               listening copies (WAV if ffmpeg is missing)
  shared/src/commonMain/composeResources/files/sounds/             the installed set (mono 16-bit 22.05 kHz WAV)

Everything is physically modelled, never raw oscillators: stiff-string piano partials with three detuned unison
strings, hammer strike position and felt filtering; a plucked kalimba tine with release glide and box resonance;
a rosewood bar with yarn mallet and tube resonator; a wooden tile as an inharmonic block + board thump; paper as
band-swept noise with crinkle grains. Then body EQ, gentle low-pass, soft asymmetric saturation and a small wooden
room from a synthetic impulse response. See docs/design/sound.md for the sound list and intent.
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

NAMES = {"C": 0, "D": 2, "E": 4, "G": 7, "A": 9}  # C major pentatonic only: every overlap stays consonant


def hz(note):
    pc, octave = NAMES[note[0]], int(note[1:])
    return 440.0 * 2 ** ((12 * (octave + 1) + pc - 69) / 12)


# ---------------------------------------------------------------- dsp primitives

def biquad(x, kind, fc, q=0.7071, gain_db=0.0):
    """RBJ biquad (causal): 'lp' | 'hp' | 'bp' | 'peak'."""
    w0 = 2 * np.pi * fc / SR
    alpha = np.sin(w0) / (2 * q)
    c = np.cos(w0)
    if kind == "lp":
        b, a = [(1 - c) / 2, 1 - c, (1 - c) / 2], [1 + alpha, -2 * c, 1 - alpha]
    elif kind == "hp":
        b, a = [(1 + c) / 2, -(1 + c), (1 + c) / 2], [1 + alpha, -2 * c, 1 - alpha]
    elif kind == "bp":
        b, a = [alpha, 0.0, -alpha], [1 + alpha, -2 * c, 1 - alpha]
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


def ramp(n, ms):
    """Raised-cosine attack envelope (no clicks)."""
    k = min(n, max(2, int(SR * ms / 1000)))
    env = np.ones(n)
    env[:k] = 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, k))
    return env


def burst(n, rng, kind, fc, decay_ms, q=0.7071, rise_ms=0.8):
    """Filtered noise with a soft rise and exponential fall (mallet, finger, hammer, contact)."""
    m = min(n, int(SR * max(0.03, decay_ms * 6 / 1000)))
    t = np.arange(m) / SR
    x = biquad(rng.standard_normal(m) * np.exp(-t / (decay_ms / 1000)) * ramp(m, rise_ms), kind, fc, q)
    out = np.zeros(n)
    out[:m] = x / (np.max(np.abs(x)) + 1e-12)
    return out


def decaying(f, amp, tau, t, rng, glide=0.0):
    """One damped mode; `glide` adds a small settling pitch drop at the start (real strikes/plucks do this)."""
    phase = 2 * np.pi * f * (t + glide * 0.015 * (1 - np.exp(-t / 0.015)))
    return amp * np.exp(-t / tau) * np.sin(phase + rng.uniform(0, 2 * np.pi))


# ---------------------------------------------------------------- instruments

def felt_piano(f, vel, n, rng, mute=1.0):
    """(A) Felt upright: stiff-string partials, hammer at 1/7.5 of the string, felt low-pass, 3 detuned unison
    strings (slow beating + two-stage decay), hammer/key thump."""
    t = np.arange(n) / SR
    tau1 = float(np.clip(1.5 * (262 / f) ** 0.7, 0.45, 2.4)) * mute
    b_inh = float(np.clip(0.00018 * (f / 262), 0.00008, 0.0005))
    fc = 650 + 1300 * vel**1.5
    x = np.zeros(n)
    for k in range(1, 15):
        fk = k * f * np.sqrt(1 + b_inh * k * k)
        if fk > 0.4 * SR:
            break
        amp = abs(np.sin(np.pi * k / 7.5)) * k**-1.2 / (1 + (fk / fc) ** 2.5)
        tk = tau1 / (1 + 0.3 * (k - 1))
        for cents, share, slow in ((-0.8, 0.34, 0.85), (0.1, 0.33, 1.0), (0.9, 0.33, 1.2)):
            fs = fk * 2 ** ((cents + rng.uniform(-0.3, 0.3)) / 1200)
            prompt = np.exp(-t / (tk * 0.22))
            env = 0.55 * prompt + 0.45 * np.exp(-t / (tk * slow))
            x += share * amp * env * np.sin(2 * np.pi * fs * t + rng.uniform(0, 2 * np.pi))
    x /= np.max(np.abs(x)) + 1e-12
    x += 0.05 * vel * burst(n, rng, "lp", 1100, 2.0) + 0.06 * burst(n, rng, "bp", 140, 12.0, 1.0)
    return x * ramp(n, 3.0)


def kalimba(f, vel, n, rng, mute=1.0):
    """(B) Kalimba tine: modes 1 : 5.93 : 13.4, release glide, thumb-flesh pluck, slight bridge beating."""
    t = np.arange(n) / SR
    tau = float(np.clip(1.0 * (523 / f) ** 0.5, 0.35, 1.6)) * mute
    x = decaying(f, 1.0, tau, t, rng, glide=0.004)
    x += decaying(f * 1.0008, 0.18, tau * 1.25, t, rng)
    x += decaying(f * 5.93, 0.16 * vel, tau * 0.12, t, rng)
    x += decaying(f * 13.4, 0.035 * vel, tau * 0.05, t, rng)
    x += 0.05 * vel * burst(n, rng, "lp", 800, 3.0, rise_ms=1.5)
    return x * ramp(n, 5.0)


def marimba(f, vel, n, rng, mute=1.0):
    """(C) Rosewood bar, yarn mallet: tuned modes 1 : 3.93 : 9.3, resonator tube swelling on the fundamental."""
    t = np.arange(n) / SR
    tau = float(np.clip(0.6 * (262 / f) ** 0.8, 0.16, 0.95)) * mute
    x = decaying(f, 1.0, tau, t, rng, glide=0.002)
    x += decaying(f * 3.93, 0.12 * vel**1.5, tau * 0.18, t, rng)
    x += decaying(f * 9.3, 0.03 * vel, tau * 0.06, t, rng)
    tube = decaying(f * 1.0008, 0.5, tau * 1.5, t, rng) * (1 - np.exp(-t / 0.008))
    x += tube + 0.04 * vel * burst(n, rng, "lp", 700, 4.0, rise_ms=2.0)
    return x * ramp(n, 7.0)


# ---------------------------------------------------------------- tactile layer (same in every set)

def tile(n, rng, vel=0.5, size=1.0):
    """Wooden letter tile on a wooden board: inharmonic block modes + contact noise + short warm board thump."""
    t = np.arange(n) / SR
    f0 = rng.uniform(1100, 1300) / size
    x = np.zeros(n)
    for ratio, amp, tau_ms in ((1, 1, 16), (1.56, 0.6, 11), (2.18, 0.35, 8), (2.71, 0.22, 6), (3.38, 0.12, 4)):
        x += decaying(f0 * ratio * rng.uniform(0.97, 1.03), amp, tau_ms * size / 1000, t, rng)
    for fb, amp, tau_ms in ((165, 0.5, 30), (247, 0.35, 22), (392, 0.2, 15)):
        x += decaying(fb * rng.uniform(0.95, 1.05), amp, tau_ms / 1000, t, rng)
    x += 0.35 * burst(n, rng, "bp", 1500 + 2500 * vel, 1.2, 0.8)
    x = biquad(x, "lp", 1800 + 3000 * vel)  # a soft finger is darker than a firm one
    return vel * x * ramp(n, 3.0)


def paper(n, rng, dur, forward=True, vel=0.5):
    """Paper card turning / sliding: noise swept between two bands, swell then fall, sparse crinkle grains."""
    m = min(n, int(SR * dur))
    t = np.arange(m) / SR
    env = np.sin(np.pi * np.clip(t / dur, 0, 1)) ** 1.5 * np.exp(-t / (dur * 0.8))
    mix = t / dur if forward else 1 - t / dur
    noise = rng.standard_normal(m)
    x = (1 - mix) * biquad(noise, "bp", 700, 0.8) + mix * biquad(noise, "bp", 1800, 0.9)
    grains = (rng.random(m) < 140 / SR) * rng.uniform(-1, 1, m)
    x = x + 0.5 * biquad(grains, "bp", 2500, 2.0)
    x = biquad(x * env, "lp", 3800)
    out = np.zeros(n)
    out[:m] = x / (np.max(np.abs(x)) + 1e-12) * vel
    return out


SETS = {
    "A": ("a-felt-piano", felt_piano, {"lp": 5200, "eq": [(220, 3.0, 0.8), (2600, -2.0, 1.0)]}),
    "B": ("b-kalimba", kalimba, {"lp": 5500, "eq": [(230, 4.0, 1.2), (900, 1.5, 1.0)]}),
    "C": ("c-marimba", marimba, {"lp": 5000, "eq": [(180, 2.5, 0.9)]}),
}


# ---------------------------------------------------------------- room + mastering

def room_ir(rt, rng):
    """Small wooden room: diffuse early cluster (smoothed) + darkening exponential tail, -60 dB at rt."""
    n = int(SR * rt)
    t = np.arange(n) / SR
    tail = rng.standard_normal(n) * np.exp(-6.9 * t / rt) * (1 - np.exp(-t / 0.004))
    tail = biquad(biquad(tail, "lp", 2600), "hp", 160)
    early = np.zeros(n)
    for ms in rng.uniform(4, 28, 9):
        i = int(SR * ms / 1000)
        if i < n:
            early[i] += rng.uniform(-1, 1) * np.exp(-ms / 20)
    early = biquad(early, "lp", 3000)
    ir = tail / np.max(np.abs(tail)) + 0.8 * early / (np.max(np.abs(early)) + 1e-12)
    return ir / np.sqrt(np.sum(ir**2))


def master(x, spec, rt, wet_amount, rng):
    """Body EQ, low-pass, rumble cut, soft asymmetric saturation, wooden room, natural release, 22.05 kHz."""
    for fc, g, q in spec["eq"]:
        x = biquad(x, "peak", fc, q, g)
    x = biquad(biquad(x, "lp", spec["lp"]), "lp", spec["lp"])
    x = biquad(x, "hp", 100)
    x = x / (np.max(np.abs(x)) + 1e-12) * 0.8
    x = np.tanh(1.3 * x + 0.1 * x * x) / np.tanh(1.3)  # mild even harmonics, rounded peaks
    ir = room_ir(rt, rng)
    size = len(x) + len(ir)
    wet = np.fft.irfft(np.fft.rfft(x, size) * np.fft.rfft(ir, size))[: len(x)]
    wet *= np.sqrt(np.mean(x**2)) / (np.sqrt(np.mean(wet**2)) + 1e-12)
    y = (1 - wet_amount) * x + wet_amount * wet
    fade = max(int(SR * 0.015), int(len(y) * 0.35))
    y[-fade:] *= (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fade))) ** 1.5
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
    """One rendering: places instrument notes, tiles and paper on a timeline with light human timing."""

    def __init__(self, instrument, dur, rng, variant):
        self.instrument, self.rng, self.variant = instrument, rng, variant
        self.x = np.zeros(int(SR * dur))

    def _at(self, t0):
        start = int(SR * (t0 + (self.rng.uniform(0, 0.003) if t0 else 0)))
        return start, len(self.x) - start

    def _vel(self, vel):
        return float(np.clip(vel * self.rng.uniform(0.9, 1.1) if self.variant else vel, 0.05, 1.0))

    def note(self, t0, notes, vel, mute=1.0):
        chord = notes.split()
        for i, nn in enumerate(chord):  # chords are rolled like a hand, never a block hit
            start, n = self._at(t0 + 0.018 * i)
            if n > 0:
                f = hz(nn) * (2 ** (self.rng.uniform(-5, 5) / 1200) if self.variant else 1.0)
                self.x[start:] += self.instrument(f, self._vel(vel), n, self.rng, mute) * (0.75 if i else 1.0)
        return self

    def tile(self, t0, vel, size=1.0, bounce=False):
        start, n = self._at(t0)
        if n > 0:
            self.x[start:] += 0.9 * tile(n, self.rng, self._vel(vel), size)
        if bounce:  # a set-down tile rocks once on the board
            b = start + int(SR * self.rng.uniform(0.014, 0.02))
            if b < len(self.x):
                self.x[b:] += 0.9 * tile(len(self.x) - b, self.rng, self._vel(vel) * 0.3, size)
        return self

    def paper(self, t0, dur, forward=True, vel=0.5):
        start, n = self._at(t0)
        if n > 0:
            self.x[start:] += paper(n, self.rng, dur, forward, self._vel(vel))
        return self


# name: (duration_s, peak_dBFS, room rt_s, reverb wet, frequent?, score(take))
SOUNDS = {
    # --- existing files (referenced by SoundEffect; names and format kept, durations close)
    "tap": (0.07, -19, 0.12, 0.08, True, lambda k: k.tile(0, 0.35)),
    "place": (0.09, -20, 0.14, 0.08, True, lambda k: k.tile(0, 0.5, bounce=True)),
    "correct": (0.28, -9, 0.3, 0.12, False, lambda k: k.note(0, "G4", 0.55, 0.6).note(0.085, "C5", 0.7, 0.8)),
    "wrong": (0.30, -14, 0.25, 0.1, False,
              lambda k: k.tile(0, 0.35, size=1.6).note(0.01, "E4", 0.3, 0.2).note(0.12, "C4", 0.3, 0.25)),
    "pass": (0.26, -15, 0.2, 0.1, False, lambda k: k.paper(0, 0.2, True, 0.6).tile(0.17, 0.2, size=1.2)),
    "combo": (0.40, -9, 0.4, 0.12, False,
              lambda k: k.note(0, "G4", 0.5, 0.6).note(0.055, "C5", 0.55, 0.6).note(0.11, "E5", 0.6, 0.7)
              .note(0.165, "G5", 0.7, 0.9)),
    "tick": (0.12, -18, 0.15, 0.1, True, lambda k: k.note(0, "G4", 0.35, 0.22).tile(0, 0.15, size=1.8)),
    "win": (0.90, -8, 0.7, 0.13, False,
            lambda k: k.note(0, "C5", 0.5).note(0.11, "E5", 0.55).note(0.22, "G5", 0.6)
            .note(0.36, "C4 G4 E5", 0.6, 1.2)),
    "lose": (0.80, -12, 0.6, 0.12, False,
             lambda k: k.note(0, "A4", 0.4, 0.8).note(0.15, "G4", 0.38, 0.8).note(0.3, "E4", 0.36, 0.8)
             .note(0.45, "C4 G4", 0.38)),
    # --- new files (ready to wire, see docs/design/sound.md)
    "button": (0.09, -17, 0.15, 0.08, False, lambda k: k.tile(0, 0.45, size=1.4)),
    "backspace": (0.09, -21, 0.12, 0.08, True, lambda k: k.paper(0, 0.035, False, 0.5).tile(0.025, 0.25, size=1.1)),
    "timer_tick": (0.09, -30, 0.15, 0.1, True, lambda k: k.note(0, "A4", 0.2, 0.15)),
    "warning_1": (0.14, -19, 0.18, 0.1, False, lambda k: k.note(0, "G4", 0.3, 0.25).tile(0, 0.12, size=1.8)),
    "warning_2": (0.14, -17, 0.18, 0.1, False, lambda k: k.note(0, "A4", 0.4, 0.25).tile(0, 0.15, size=1.8)),
    "warning_3": (0.18, -15, 0.18, 0.1, False,
                  lambda k: k.note(0, "C5", 0.45, 0.25).note(0.085, "C5", 0.3, 0.25).tile(0, 0.18, size=1.8)),
    "time_up": (0.70, -12, 0.6, 0.12, False, lambda k: k.note(0, "G4", 0.45, 0.9).note(0.16, "C4 G4", 0.4)),
    "joker": (0.38, -13, 0.35, 0.12, False,
              lambda k: k.paper(0, 0.1, True, 0.3).note(0.04, "E5", 0.4, 0.6).note(0.08, "G5", 0.45, 0.6)
              .note(0.12, "A5", 0.5, 0.8)),
    "gift": (0.65, -11, 0.55, 0.12, False,
             lambda k: k.paper(0, 0.14, True, 0.45).note(0.09, "C5", 0.45).note(0.15, "E5", 0.5)
             .note(0.21, "G5", 0.55, 1.1)),
    "countdown": (0.22, -14, 0.25, 0.1, False, lambda k: k.note(0, "C5", 0.5, 0.4)),
    "countdown_go": (0.38, -11, 0.35, 0.12, False, lambda k: k.note(0, "G4 C5 E5", 0.6, 0.8)),
    "best": (0.85, -8, 0.7, 0.13, False,
             lambda k: k.note(0, "C5", 0.5, 0.7).note(0.07, "E5", 0.5, 0.7).note(0.14, "G5", 0.55, 0.7)
             .note(0.21, "A5", 0.6, 0.8).note(0.32, "C4 G4 E5 C6", 0.65, 1.2)),
    "dialog_open": (0.18, -22, 0.2, 0.1, False, lambda k: k.paper(0, 0.12, True, 0.5)),
    "dialog_close": (0.18, -23, 0.2, 0.1, False, lambda k: k.paper(0, 0.1, False, 0.5).tile(0.08, 0.15, size=1.3)),
    "toast": (0.25, -21, 0.25, 0.12, False, lambda k: k.note(0, "E5", 0.3, 0.5)),
    "notebook_add": (0.24, -18, 0.22, 0.1, False,
                     lambda k: k.paper(0, 0.09, True, 0.45).note(0.06, "E5", 0.4, 0.4).note(0.11, "A5", 0.45, 0.5)),
    "notebook_remove": (0.22, -20, 0.2, 0.1, False,
                        lambda k: k.paper(0, 0.09, False, 0.45).note(0.07, "A4", 0.3, 0.25)),
    "daily_complete": (1.00, -8, 0.7, 0.13, False,
                       lambda k: k.note(0, "G4", 0.45).note(0.09, "C5", 0.5).note(0.18, "E5", 0.55)
                       .note(0.27, "G5", 0.6).note(0.4, "C4 G4 E5 C6", 0.65, 1.3)),
}
VARIANTS = 3


def render(set_key, name, variant=0):
    _, instrument, spec = SETS[set_key]
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
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--install", default="A", choices=["A", "B", "C", "none"], help="set copied into the app")
    ap.add_argument("--no-previews", action="store_true")
    args = ap.parse_args()

    ffmpeg = shutil.which("ffmpeg")
    if OUT_DIR.exists():
        shutil.rmtree(OUT_DIR)
    if not args.no_previews and PREVIEW_DIR.exists():
        shutil.rmtree(PREVIEW_DIR)
    for key, (label, _, _) in SETS.items():
        out = OUT_DIR / label
        mains, variants = [], []
        for name, (*_, frequent, _score) in SOUNDS.items():
            for v in range(VARIANTS + 1 if frequent else 1):
                fname = f"{name}.wav" if v == 0 else f"{name}_{v}.wav"
                pcm = render(key, name, v)
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
