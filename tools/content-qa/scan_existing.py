#!/usr/bin/env python3
"""Deterministic second-pass scan of content/cards. Usage: scan_existing.py [out_dir]
Writes flags.json (flagged cards) and summary data; read-only on content/."""
import csv, glob, json, os, re, sys, random, difflib, collections
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
LEVELS = ["A1", "A2", "B1", "B2", "C1", "NAT"]
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "docs", "content-review")

cards = []
for lv in LEVELS:
    for f in sorted(glob.glob(f"{ROOT}/content/cards/{lv}/*.csv")):
        with open(f, newline="", encoding="utf-8") as fh:
            for r in csv.DictReader(fh):
                r["level"] = lv; r["file"] = os.path.basename(f); cards.append(r)

flags = collections.defaultdict(list)  # id -> [(check, detail)]
counts = collections.defaultdict(lambda: collections.Counter())
def flag(c, check, detail=""):
    flags[c["id"]].append((check, detail)); counts[check][c["level"]] += 1

def words(s): return re.findall(r"[a-z']+", s.lower())
all_examples = [(c, c["example_en"].lower()) for c in cards]
by_ans = collections.defaultdict(list)
for c in cards: by_ans[c["answer"].upper()].append(c)

# frequency proxy: word frequency inside the corpus' own examples
freq = collections.Counter(w for c in cards for w in words(c["example_en"]))
RARE_OK = set()
TR_FIX = {"icin":"için","degil":"değil","cok":"çok","gunes":"güneş","guzel":"güzel","oglen":"öğlen",
          "ogrenci":"öğrenci","ogretmen":"öğretmen","soyle":"söyle","sey":"şey","yapmak":"yapmak",
          "turkce":"Türkçe","ingilizce":"İngilizce","kucuk":"küçük","buyuk":"büyük","dusunmek":"düşünmek",
          "calismak":"çalışmak","gorum":"görüm","uzgun":"üzgün","yemek":"yemek","gece":"gece",
          "cocuk":"çocuk","sehir":"şehir","okul":"okul","guc":"güç","ucak":"uçak","kopek":"köpek"}
TR_FIX = {k:v for k,v in TR_FIX.items() if k != v}

for c in cards:
    a = c["answer"]; al = a.lower(); ex = c["example_en"]; clue = c["clue_tr"]; ext = c["example_tr"]
    if not re.fullmatch(r"[A-Z]+(?:[ '\-][A-Z]+)*", a): flag(c, "answer_charset", a)
    L = c["letter"].upper()
    if c["rule"] == "STARTS" and not a.startswith(L): flag(c, "letter_rule_mismatch", f"{L}/{c['rule']}")
    if c["rule"] == "CONTAINS" and L not in a: flag(c, "letter_rule_mismatch", f"{L}/{c['rule']}")
    # answer leak: whole answer (or stem >=4 letters) in clue or example_tr
    stem = al[:max(4, len(al) - 2)] if len(al) >= 5 else al
    if re.search(r"\b" + re.escape(al) + r"\b", clue.lower()) or (len(al) >= 4 and stem in clue.lower()):
        flag(c, "answer_in_clue", clue)
    # answer must appear in own example (inflections tolerated via stem)
    ws = words(ex)
    if not any(w.startswith(stem[:max(3, len(stem)-1)]) or al in w for w in ws) and " " not in al:
        flag(c, "answer_not_in_own_example", ex)
    # clue/example lengths
    if len(clue) < 2: flag(c, "clue_too_short", clue)
    if len(clue) > 60: flag(c, "clue_too_long", clue)
    if len(ex.split()) < 3: flag(c, "example_too_short", ex)
    if len(ex.split()) > 18: flag(c, "example_too_long", ex)
    if not ext or len(ext.split()) < 2: flag(c, "example_tr_missing_or_short", ext)
    # Turkish typo patterns
    for fld in ("clue_tr", "example_tr"):
        t = c[fld]
        if "  " in t or t != t.strip(): flag(c, "tr_whitespace", fld)
        if re.search(r"\bi̇|İ[a-zçğıöşü]*[A-ZÇĞİÖŞÜ]{2}", t): flag(c, "tr_casing", fld)
        if re.search(r"\bI[a-zçğıöşü]{2,}", t) and fld == "example_tr" and not re.search(r"\bI[sS]", t):
            flag(c, "tr_casing_dotless_I", t)
        if re.search(r" [,.!?;:]|[,.;:!?][^\s\d\"'”)\]….,!?]", t): flag(c, "tr_punct_spacing", fld)
        for w in re.findall(r"[A-Za-zÇĞİıÖŞÜçğöşü]+", t):
            if w.lower() in TR_FIX: flag(c, "tr_missing_diacritics", f"{w}->{TR_FIX[w.lower()]}")
    # level: rare words in A1/A2 examples (word appears only once in whole corpus, long)
    if c["level"] in ("A1", "A2"):
        rare = [w for w in ws if len(w) >= 9 and freq[w] == 1 and w != al]
        if len(rare) >= 2 or (c["level"] == "A1" and len(rare) >= 1 and len(ex.split()) > 10):
            flag(c, "rare_words_low_level", ",".join(rare))
    try: d = int(c["difficulty"])
    except ValueError: d = None; flag(c, "bad_difficulty", c["difficulty"])

# answer appears in other cards' examples (informational: ambiguity risk for same-letter wheel clues) -> skipped as noisy;
# instead flag answer appearing in OTHER cards' example_tr/clue is meaningless; keep within-example check above.

# duplicate answers
dups = {}
for a, lst in by_ans.items():
    if len(lst) > 1:
        within = len({c["level"] for c in lst}) < len(lst)
        dups[a] = lst
        for c in lst:
            flag(c, "dup_answer_within_level" if within else "dup_answer_across_levels",
                 a + ": " + ",".join(x["id"] for x in lst))

# same / similar clues
byclue = collections.defaultdict(list)
for c in cards: byclue[re.sub(r"\W+", " ", c["clue_tr"].lower()).strip()].append(c)
for k, lst in byclue.items():
    if len(lst) > 1 and len({c["answer"] for c in lst}) > 1:
        for c in lst: flag(c, "same_clue_diff_answer", k + " -> " + ",".join(x["answer"] for x in lst))
# high similarity within same starting letter (what a wheel player would face) — token-bucket for speed
bucket = collections.defaultdict(list)
for c in cards:
    for w in set(re.findall(r"\w{4,}", c["clue_tr"].lower())): bucket[w].append(c)
seen = set()
for w, lst in bucket.items():
    if len(lst) > 40: continue
    for i in range(len(lst)):
        for j in range(i + 1, len(lst)):
            x, y = lst[i], lst[j]
            key = tuple(sorted((x["id"], y["id"])))
            if key in seen or x["answer"] == y["answer"]: continue
            seen.add(key)
            if len(x["clue_tr"]) > 8 and difflib.SequenceMatcher(None, x["clue_tr"].lower(), y["clue_tr"].lower()).ratio() >= 0.88:
                for a, b in ((x, y), (y, x)): flag(a, "similar_clue", f"{a['clue_tr']} ~ {b['clue_tr']} ({b['answer']})")

# level vs tier: corpus frequency of answer in examples (weak proxy; no offline wordlist)
# skipped: no offline frequency list available.

os.makedirs(OUT, exist_ok=True)
json.dump({i: v for i, v in flags.items()}, open(os.path.join(OUT, "second-pass-flags.json"), "w"), ensure_ascii=False, indent=0)
checks = sorted(counts)
tot = collections.Counter(c["level"] for c in cards)
lines = ["# Second-pass scan summary", "", f"Cards: " + ", ".join(f"{l} {tot[l]}" for l in LEVELS) + f" (total {len(cards)}). Flagged cards: {len(flags)}.", "",
         "Level-vs-frequency check skipped (no offline frequency list); `rare_words_low_level` is a corpus-frequency proxy.", "",
         "| check | " + " | ".join(LEVELS) + " | total |", "|---|" + "---|" * (len(LEVELS) + 1)]
for ck in checks:
    lines.append(f"| {ck} | " + " | ".join(str(counts[ck][l]) for l in LEVELS) + f" | {sum(counts[ck].values())} |")
open(os.path.join(OUT, "SECOND-PASS-summary.md"), "w").write("\n".join(lines) + "\n")

# review set: flagged cards + seeded 40/level sample -> scratch file
rng = random.Random(42); sample = set()
for l in LEVELS:
    ids = sorted(c["id"] for c in cards if c["level"] == l)
    sample |= set(rng.sample(ids, min(40, len(ids))))
rev = [c for c in cards if c["id"] in sample or c["id"] in flags]
with open(os.path.join(OUT, ".review-set.tsv"), "w", encoding="utf-8") as f:
    for c in rev:
        fl = ";".join(k for k, _ in flags.get(c["id"], []))
        f.write("\t".join([c["id"], c["answer"], c["clue_tr"], c["example_en"], c["example_tr"], fl]) + "\n")
print(len(cards), len(flags), len(rev))
