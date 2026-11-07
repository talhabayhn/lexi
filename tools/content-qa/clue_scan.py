#!/usr/bin/env python3
"""Turkish clue (clue_tr) quality scan (docs/CONTENT.md "Clue standard (TR)"). Stdlib only, read-only on content/.

Usage (repo root): python3 tools/content-qa/clue_scan.py [--content content/cards] [--csv docs/content-review/CLUE-SCAN.csv]
Rules: C1 single-word clue (likely bare translation), C2 clue longer than 8 words, C3 rambling clue without a clear
head noun (noun/adj cards: generic or verbal last word in a multi-phrase clue), C4 translation-like clue (2-3 words) / example_tr mismatch (no
content word of the clue shares a stem with example_tr; heuristic, expect false positives on definitions).
CSV columns: id, level, answer, pos, clue_tr, example_tr, rules, proposed_rewrite (left empty for the owner).
"""
import argparse
import csv
import re
import unicodedata
from collections import Counter
from pathlib import Path

LEVELS = ["A1", "A2", "B1", "B2", "C1", "NAT"]
RULES = ["C1", "C2", "C3", "C4"]
GENERIC_HEADS = {"parça", "şey", "şeyler", "yer", "kısım", "durum", "tür", "çeşit", "madde", "alet", "eşya", "nesne"}
VERBAL_END = re.compile(r"(en|an|ken|arak|erek|ıp|ip|up|üp|mak|mek|diği|dığı|duğu|düğü|tiği|tığı)$")
# Owner decision: numbers, ordinals, colours, days and months keep a 1:1 translation (excluded from C1).
TRANSLATION_OK = set("""sıfır bir iki üç dört beş altı yedi sekiz dokuz on yirmi otuz kırk elli altmış yetmiş seksen doksan
yüz bin milyon birinci ikinci üçüncü dördüncü beşinci altıncı yedinci sekizinci dokuzuncu onuncu
kırmızı mavi sarı yeşil siyah beyaz pembe mor turuncu gri kahverengi
pazartesi salı çarşamba perşembe cuma cumartesi pazar
ocak şubat mart nisan mayıs haziran temmuz ağustos eylül ekim kasım aralık""".split())
TR_STOP = set("bir ve ile için de da bu şu o çok daha en gibi olan olarak kadar ya veya her bazı kendi".split())


def fold(s):
    s = s.replace("ı", "i").replace("İ", "I")
    return "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c)).lower()


def tr_words(s):
    return re.findall(r"[A-Za-zÇĞİÖŞÜçğıöşü]+", s)


def stem(w):
    return fold(w)[:3]


def hits(row):
    clue = row["clue_tr"]
    ws = tr_words(clue)
    out = []
    if len(ws) == 1 and ws[0].lower() not in TRANSLATION_OK:
        out.append("C1")
    if len(ws) > 8:
        out.append("C2")
    if len(ws) >= 4 and row["pos"] in ("noun", "adj"):
        last = ws[-1].lower()
        if last in GENERIC_HEADS or (VERBAL_END.search(last) and row["pos"] == "noun"):
            out.append("C3")
    if 2 <= len(ws) <= 3:
        ex = {stem(w) for w in tr_words(row["example_tr"]) if len(w) >= 3}
        content = [w for w in ws if w.lower() not in TR_STOP and len(w) >= 3]
        if content and not any(stem(w) in ex for w in content):
            out.append("C4")
    return out


def review_md(csv_path, md_path):
    """Render a review CSV (id, current_clue, proposed_clue) as a mobile-readable markdown table grouped by letter."""
    with open(csv_path, encoding="utf-8", newline="") as fh:
        rows = list(csv.DictReader(fh))
    for r in rows:
        r["answer"] = r["id"].split("-", 1)[1].rsplit("-", 1)[0] if re.search(r"-\d+$", r["id"]) else r["id"].split("-", 1)[1]
    rows.sort(key=lambda r: (r["answer"][0], r["answer"]))
    out = [f"# {Path(csv_path).stem} review ({len(rows)} clues)", "",
           f"Generated from `{csv_path}` by `clue_scan.py --review-md`; edit the CSV, not this file.", ""]
    letter, n = None, 0
    for r in rows:
        n += 1
        if r["answer"][0] != letter:
            letter = r["answer"][0]
            out += ["", f"## {letter.upper()}", "", "| # | answer | current | proposed |", "|---|---|---|---|"]
        out.append(f"| {n} | {r['answer']} | {r['current_clue']} | {r['proposed_clue']} |")
    Path(md_path).write_text("\n".join(out) + "\n", encoding="utf-8")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--review-md", nargs=2, metavar=("CSV", "MD"), help="render a review CSV to markdown and exit")
    ap.add_argument("--content", default="content/cards")
    ap.add_argument("--csv", default="docs/content-review/CLUE-SCAN.csv")
    a = ap.parse_args()
    if a.review_md:
        review_md(*a.review_md)
        return
    rows = []
    for lv in LEVELS:
        for f in sorted((Path(a.content) / lv).glob("*.csv")):
            with open(f, encoding="utf-8-sig", newline="") as fh:
                for r in csv.DictReader(fh):
                    r["level"] = lv
                    rows.append(r)
    cnt = {lv: Counter() for lv in LEVELS}
    total = Counter()
    flagged = []
    for r in rows:
        total[r["level"]] += 1
        h = hits(r)
        for x in h:
            cnt[r["level"]][x] += 1
        if h:
            flagged.append((r, h))
    Path(a.csv).parent.mkdir(parents=True, exist_ok=True)
    with open(a.csv, "w", encoding="utf-8", newline="") as fh:
        w = csv.writer(fh)
        w.writerow(["id", "level", "answer", "pos", "clue_tr", "example_tr", "rules", "proposed_rewrite"])
        for r, h in flagged:
            w.writerow([r["id"], r["level"], r["answer"], r["pos"], r["clue_tr"], r["example_tr"], "|".join(h), ""])
    print("level  cards  flagged  " + "  ".join(RULES))
    for lv in LEVELS:
        n = sum(1 for r, _ in flagged if r["level"] == lv)
        print(f"{lv:<5} {total[lv]:>6} {n:>8}  " + "  ".join(f"{cnt[lv][k]:>3}" for k in RULES))


if __name__ == "__main__":
    main()
