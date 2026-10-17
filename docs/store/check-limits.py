#!/usr/bin/env python3
"""Verify store field limits: python3 docs/store/check-limits.py"""
import re, sys, pathlib
LIM = {"Title": 30, "Short description": 80, "Full description": 4000, "Name": 30, "Subtitle": 30,
       "Promotional text": 170, "Keywords": 100, "Başlık": 30, "Kısa açıklama": 80, "Tam açıklama": 4000,
       "Ad": 30, "Alt başlık": 30, "Tanıtım metni": 170, "Anahtar kelimeler": 100}
bad = 0
for f in ("listing-en.md", "listing-tr.md"):
    t = (pathlib.Path(__file__).parent / f).read_text()
    for m in re.finditer(r"\*\*([^*(]+?) \(≤(\d+)[^)]*\)\*\*\n```\n(.*?)\n```", t, re.S):
        name, lim, body = m.group(1), int(m.group(2)), m.group(3)
        n = len(body)
        ok = n <= lim
        bad += not ok
        print(f"{'OK ' if ok else 'BAD'} {f:14} {name:20} {n:5}/{lim}")
sys.exit(1 if bad else 0)
