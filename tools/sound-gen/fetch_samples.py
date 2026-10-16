#!/usr/bin/env python3
"""Extracts the short source excerpts in tools/sound-gen/samples/ from the VCSL sample library (CC0 1.0).

VCSL (Versilian Community Sample Library, https://github.com/sgossner/VCSL) is public domain (CC0): no
royalties, no credit, no special terms. Only the excerpts are vendored, so generate.py runs offline.

    python3 tools/sound-gen/fetch_samples.py            # sparse-clones VCSL (pinned commit) into tools/sound-gen/out/
    python3 tools/sound-gen/fetch_samples.py --vcsl DIR # use an existing checkout

Each excerpt: mono mix, onset-aligned, trimmed with a smooth fade, tuned to the exact equal-tempered pitch of its
note (measured, so the C major pentatonic stays in tune), saved as mono 16-bit 22.05 kHz WAV.
"""
import argparse
import subprocess
import sys
import wave
from pathlib import Path

import numpy as np

VCSL_URL = "https://github.com/sgossner/VCSL"
VCSL_COMMIT = "c1ea7bcc3c7309650ab0da9d15c9cd1fbc4a4c7e"
HERE = Path(__file__).resolve().parent
SAMPLES = HERE / "samples"
RATE = 22050

PIANO = "Chordophones/Zithers/Upright Piano, Yamaha/"
KALIMBA = "Idiophones/Plucked Idiophones/Kalimba, Tanzania/"
MARIMBA = "Idiophones/Struck Idiophones/Marimba/"
VIBES = "Idiophones/Struck Idiophones/Vibraphone/"
WOOD = "Idiophones/Struck Idiophones/Woodblock/"
SLIT = "Idiophones/Struck Idiophones/Slit Drum/"
CLAVES = "Idiophones/Struck Idiophones/Claves/"

# target file: (VCSL file, sounding note or None for unpitched, seconds kept). VCSL names notes one octave lower
# than scientific pitch (its "C4" sounds at 523 Hz), so the sounding note is one octave above the file name.
EXCERPTS = {
    "piano/C4.wav": (PIANO + "Sustains/Upright1_Sus_C3_vl1_rr1.wav", "C4", 1.6),
    "piano/G4.wav": (PIANO + "Sustains/Upright1_Sus_G3_vl1_rr1.wav", "G4", 1.6),
    "piano/C5.wav": (PIANO + "Sustains/Upright1_Sus_C4_vl1_rr1.wav", "C5", 1.6),
    "piano/G5.wav": (PIANO + "Sustains/Upright1_Sus_G4_vl1_rr1.wav", "G5", 1.5),
    "piano/C6.wav": (PIANO + "Sustains/Upright1_Sus_C5_vl1_rr1.wav", "C6", 1.4),
    "kalimba/C#4.wav": (KALIMBA + "MBira3_pluck_Main_C#3_k16_50_100_rr2.wav", "C#4", 1.4),
    "kalimba/F4.wav": (KALIMBA + "MBira3_pluck_Main_F3_k17_50_100_rr2.wav", "F4", 1.4),
    "kalimba/G4.wav": (KALIMBA + "MBira3_pluck_Main_G3_k6_50_100_rr2.wav", "G4", 1.4),
    "kalimba/A#4.wav": (KALIMBA + "MBira3_pluck_Main_A#3_k18_50_100_rr2.wav", "A#4", 1.4),
    "kalimba/C#5.wav": (KALIMBA + "MBira3_pluck_Main_C#4_k5_50_100_rr2.wav", "C#5", 1.4),
    "kalimba/D5.wav": (KALIMBA + "MBira3_pluck_Main_D4_k19_50_100_rr2.wav", "D5", 1.4),
    "kalimba/F5.wav": (KALIMBA + "MBira3_pluck_Main_F4_k21_50_100_rr2.wav", "F5", 1.3),
    "kalimba/G5.wav": (KALIMBA + "MBira3_pluck_Main_G4_k22_50_100_rr2.wav", "G5", 1.3),
    "kalimba/C#6.wav": (KALIMBA + "MBira3_pluck_Main_C#5_k24_50_100_rr2.wav", "C#6", 1.2),
    "marimba/B3.wav": (MARIMBA + "Marimba_hit_Outrigger_B2_soft_01.wav", "B3", 1.4),
    "marimba/F4.wav": (MARIMBA + "Marimba_hit_Outrigger_F3_soft_01.wav", "F4", 1.4),
    "marimba/C5.wav": (MARIMBA + "Marimba_hit_Outrigger_C4_soft_01.wav", "C5", 1.3),
    "marimba/G5.wav": (MARIMBA + "Marimba_hit_Outrigger_G4_soft_01.wav", "G5", 1.2),
    "marimba/B5.wav": (MARIMBA + "Marimba_hit_Outrigger_B4_soft_01.wav", "B5", 1.1),
    "vibes/C4.wav": (VIBES + "Soft Mallets/Vibes_soft_C3_v1_rr2_Main.wav", "C4", 1.6),
    "vibes/E4.wav": (VIBES + "Soft Mallets/Vibes_soft_E3_v1_rr2_Main.wav", "E4", 1.6),
    "vibes/G4.wav": (VIBES + "Soft Mallets/Vibes_soft_G3_v1_rr1_Main.wav", "G4", 1.6),
    "vibes/B4.wav": (VIBES + "Soft Mallets/Vibes_soft_B3_v1_rr1_Main.wav", "B4", 1.6),
    "vibes/D5.wav": (VIBES + "Soft Mallets/Vibes_soft_D4_v1_rr1_Main.wav", "D5", 1.6),
    "vibes/F5.wav": (VIBES + "Soft Mallets/Vibes_soft_F4_v1_rr1_Main.wav", "F5", 1.5),
    "vibes/A5.wav": (VIBES + "Soft Mallets/Vibes_soft_A4_v1_rr1_Main.wav", "A5", 1.5),
    "wood/block_1.wav": (WOOD + "wood_click_pp_rr1.wav", None, 0.25),
    "wood/block_2.wav": (WOOD + "wood_click_pp_rr2.wav", None, 0.25),
    "wood/block_3.wav": (WOOD + "wood_click_pp_rr3.wav", None, 0.25),
    "wood/block_soft.wav": (WOOD + "wood_click3_vl1.wav", None, 0.3),
    "wood/slit_hi.wav": (SLIT + "LogDrumHi_MedM_v1_rr1_Sum.wav", None, 0.5),
    "wood/slit_lo.wav": (SLIT + "LogDrumLo_MedM_v1_rr1_Sum.wav", None, 0.5),
    "wood/claves.wav": (CLAVES + "Legacy/claves_pp1.wav", None, 0.15),
}

PC = {"C": 0, "C#": 1, "D": 2, "D#": 3, "E": 4, "F": 5, "F#": 6, "G": 7, "G#": 8, "A": 9, "A#": 10, "B": 11}


def hz(note):
    name, octave = note[:-1], int(note[-1])
    return 440.0 * 2 ** ((12 * (octave + 1) + PC[name] - 69) / 12)


def read_wav(path):
    with wave.open(str(path)) as w:
        ch, width, rate, n = w.getnchannels(), w.getsampwidth(), w.getframerate(), w.getnframes()
        raw = np.frombuffer(w.readframes(n), dtype=np.uint8)
    if width == 3:
        b = raw.reshape(-1, 3).astype(np.int32)
        x = (b[:, 0] | (b[:, 1] << 8) | (b[:, 2] << 16)).astype(np.int32)
        x = np.where(x >= 1 << 23, x - (1 << 24), x) / float(1 << 23)
    else:
        x = np.frombuffer(raw.tobytes(), dtype=np.int16) / 32768.0
    return x.reshape(-1, ch).mean(axis=1), rate


def resample(x, src, dst):
    """Band-limited resampling: FFT low-pass below the new Nyquist, then interpolation."""
    if src > dst:
        pad = len(x) + 4096
        spec = np.fft.rfft(x, pad)
        f = np.fft.rfftfreq(pad, 1 / src)
        spec *= np.clip((0.47 * dst - f) / (0.04 * dst), 0, 1)
        x = np.fft.irfft(spec, pad)[: len(x)]
    t = np.arange(int(len(x) * dst / src)) * src / dst
    return np.interp(t, np.arange(len(x)), x)


def measured_pitch(x, rate, nominal):
    """Strongest spectral peak within +-70 cents of the nominal fundamental (parabolic interpolation)."""
    seg = x[int(0.03 * rate): int(0.53 * rate)]
    n = 1 << 18
    spec = np.abs(np.fft.rfft(seg * np.hanning(len(seg)), n))
    f = np.fft.rfftfreq(n, 1 / rate)
    band = np.where((f > nominal * 2 ** (-70 / 1200)) & (f < nominal * 2 ** (70 / 1200)))[0]
    i = band[np.argmax(spec[band])]
    a, b, c = np.log(spec[i - 1: i + 2] + 1e-12)
    return f[i] + 0.5 * (a - c) / (a - 2 * b + c) * (f[1] - f[0])


def excerpt(src, note, seconds):
    x, rate = read_wav(src)
    x = x - np.mean(x[: int(0.002 * rate)])
    peak = np.max(np.abs(x))
    onset = max(0, int(np.argmax(np.abs(x) > peak * 0.02)) - int(0.002 * rate))
    x = x[onset: onset + int(seconds * rate)]
    cents = 0.0
    if note:
        actual = measured_pitch(x, rate, hz(note))
        cents = 1200 * np.log2(hz(note) / actual)
        rate_in = rate * actual / hz(note)  # play slower/faster so the note lands exactly on pitch
    else:
        rate_in = rate
    y = resample(x, rate_in, RATE)
    fade = int(len(y) * 0.25)
    y[-fade:] *= (0.5 + 0.5 * np.cos(np.linspace(0, np.pi, fade))) ** 1.5
    y[: 20] *= np.linspace(0, 1, 20)
    y = y / (np.max(np.abs(y)) + 1e-12) * 0.89
    return np.round(y * 32767).astype(np.int16), cents


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--vcsl", type=Path, help="existing VCSL checkout")
    args = ap.parse_args()
    vcsl = args.vcsl
    if vcsl is None:
        vcsl = HERE / "out" / "vcsl"
        if not vcsl.exists():
            subprocess.run(["git", "clone", "--filter=blob:none", "--no-checkout", VCSL_URL, str(vcsl)], check=True)
        paths = sorted({src for src, _, _ in EXCERPTS.values()})
        subprocess.run(["git", "-C", str(vcsl), "checkout", VCSL_COMMIT, "--", *paths], check=True)
    for target, (src, note, seconds) in EXCERPTS.items():
        pcm, cents = excerpt(vcsl / src, note, seconds)
        out = SAMPLES / target
        out.parent.mkdir(parents=True, exist_ok=True)
        with wave.open(str(out), "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(RATE)
            w.writeframes(pcm.tobytes())
        print(f"{target:20} {len(pcm) / RATE:4.2f} s  tuned {cents:+5.1f} cents  <- {src.split('/')[-1]}")


if __name__ == "__main__":
    sys.exit(main())
