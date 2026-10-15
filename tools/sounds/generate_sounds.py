#!/usr/bin/env python3
"""Generates Lexi's placeholder sound effects (stdlib only, no third-party audio, so no licensing).

Output: mono 16-bit 22.05 kHz WAV files in shared/src/commonMain/composeResources/files/sounds/.
Usage: python tools/sounds/generate_sounds.py [output-dir]
Replace any file with a final asset of the same name and format; nothing else needs to change.
"""
import math
import random
import struct
import sys
import wave
from pathlib import Path

RATE = 22050
PEAK = 0.7
FADE_MS = 6

DEFAULT_OUT = Path(__file__).resolve().parents[2] / "shared/src/commonMain/composeResources/files/sounds"


def tone(freq, ms, shape="sine", decay=0.0):
    n = int(RATE * ms / 1000)
    out = []
    for i in range(n):
        t = i / RATE
        ph = (freq * t) % 1.0
        if shape == "sine":
            v = math.sin(2 * math.pi * ph)
        elif shape == "square":
            v = 1.0 if ph < 0.5 else -1.0
        elif shape == "saw":
            v = 2 * ph - 1
        else:
            v = math.sin(2 * math.pi * ph)
        out.append(v * math.exp(-decay * t))
    return out


def sweep(f0, f1, ms, shape="sine", decay=0.0):
    n = int(RATE * ms / 1000)
    out = []
    phase = 0.0
    for i in range(n):
        f = f0 + (f1 - f0) * i / max(1, n - 1)
        phase += f / RATE
        ph = phase % 1.0
        v = math.sin(2 * math.pi * ph) if shape == "sine" else (1.0 if ph < 0.5 else -1.0)
        out.append(v * math.exp(-decay * i / RATE))
    return out


def seq(*parts):
    r = []
    for p in parts:
        r.extend(p)
    return r


def mix(*parts):
    n = max(len(p) for p in parts)
    return [sum(p[i] for p in parts if i < len(p)) for i in range(n)]


def finish(samples):
    fade = int(RATE * FADE_MS / 1000)
    s = list(samples)
    for i in range(min(fade, len(s))):
        g = i / fade
        s[i] *= g
        s[-1 - i] *= g
    peak = max(abs(v) for v in s) or 1.0
    return [v / peak * PEAK for v in s]


def sounds():
    rnd = random.Random(7)
    noise = lambda ms: [rnd.uniform(-1, 1) for _ in range(int(RATE * ms / 1000))]
    return {
        "tap": tone(1400, 35, decay=60),
        "place": mix(tone(520, 70, decay=35), tone(1040, 70, decay=50)),
        "correct": seq(tone(660, 110, decay=8), tone(990, 170, decay=10)),
        "wrong": sweep(220, 110, 260, shape="square", decay=3),
        "pass": [a * 0.8 + b * 0.2 for a, b in zip(sweep(300, 900, 240, decay=4), noise(240))],
        "combo": seq(tone(523, 70), tone(659, 70), tone(784, 70), tone(1047, 90, decay=8)),
        "tick": tone(1800, 25, decay=90),
        "win": seq(tone(523, 130, decay=3), tone(659, 130, decay=3), tone(784, 130, decay=3), tone(1047, 380, decay=4)),
        "lose": seq(tone(392, 160, decay=3), tone(330, 160, decay=3), tone(262, 400, decay=4)),
    }


def write(path, samples):
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(b"".join(struct.pack("<h", int(v * 32767)) for v in finish(samples)))


def main():
    out = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_OUT
    out.mkdir(parents=True, exist_ok=True)
    for name, samples in sounds().items():
        write(out / f"{name}.wav", samples)
        print(f"{name}.wav {len(samples) * 1000 // RATE} ms")


if __name__ == "__main__":
    main()
