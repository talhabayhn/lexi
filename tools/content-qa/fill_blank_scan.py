#!/usr/bin/env python3
"""Fill-the-blank example scan (docs/design/fill-blank-proposal.md §10). Stdlib only, read-only on content/.

Usage (repo root): python3 tools/content-qa/fill_blank_scan.py [--content content/cards]
    [--report docs/content-review/FILL-BLANK-SCAN.md] [--csv docs/content-review/FILL-BLANK-REVIEW.csv]
Rules: R1 contains, R2 short-3, R3 short-4, R4 short-answer, R5 poor-context, R6 no-tr-match, R7 generic-frame,
plus R8 answer-absent (the answer does not appear as a whole word in example_en, so it cannot be blanked),
R9 leak (example_en contains another same-level answer as a whole word, unless listed in content/leak-exempt.txt).
"""
import argparse
import csv
import re
import unicodedata
from collections import Counter, defaultdict
from pathlib import Path

LEVELS = ["A1", "A2", "B1", "B2", "C1", "NAT"]
RULES = ["R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9"]
NAMES = {"R1": "contains", "R2": "short-3", "R3": "short-4", "R4": "short-answer", "R5": "poor-context",
         "R6": "no-tr-match", "R7": "generic-frame", "R8": "answer-absent", "R9": "leak"}
FRAME_MIN = 5
# Function words: articles, pronouns, be/have/do, modals, prepositions, conjunctions, common fillers.
FUNCTION_WORDS = set("""a an the i you he she it we they me him her us them my your his its our their this that these those
am is are was were be been being do does did have has had will would can could shall should may might must not no
in on at to of for from with by about into over under up down out off and or but so if then than too very there here
what who whom whose which when where why how some any all each every one two three""".split())
# Unconditional: R1/R4 are excluded by the mode's own rules (§3); everything else is a rewrite candidate.
EXCLUDE_RULES = {"R1", "R4"}


def fold(s):
    s = s.replace("ı", "i").replace("İ", "I")
    return "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c)).lower()


def words(s):
    return re.findall(r"[A-Za-z']+", s)


def blank(example, answer):
    """Example with the answer replaced by ___ (whole word, case-insensitive); None when absent."""
    pat = re.compile(r"(?<![A-Za-z'])" + re.escape(answer) + r"(?![A-Za-z'])", re.IGNORECASE)
    if not pat.search(example):
        return None
    return pat.sub("___", example)


VOWELS = set("aeiou")


def drop_last_vowel(w):
    """Turkish vowel drop on suffixing (agiz -> agz-ini, boyun -> boyn-u): remove the last vowel that is followed by a
    consonant, only for words of >= 4 letters ending in a consonant."""
    if len(w) < 4 or w[-1] in VOWELS:
        return w
    for i in range(len(w) - 2, 0, -1):
        if w[i] in VOWELS:
            return w[:i] + w[i + 1:]
    return w


def stems(s):
    """Folded lowercase words (>= 3 letters) of s."""
    return {fold(w) for w in re.findall(r"[^\W\d_]+", s) if len(w) >= 3}


def stem_match(a, b):
    """Turkish inflection tolerant word match: shared 4-letter prefix, a shared 3-letter prefix (yuzmek ~ yuzebilir),
    or either of those after the clue word loses its last vowel (agiz ~ agzini, boyun ~ boynu)."""
    for c in {a, drop_last_vowel(a)}:
        if c[:4] == b[:4] or c[:3] == b[:3]:
            return True
    return False


def no_tr_match(clue_tr, example_tr, example_en=""):
    """R6: translation may not show the word. Stem check only for gloss-style clues (<= 2 words); longer clues are
    definitions, whose words rarely occur in the translation. Plus a length-ratio check against example_en."""
    n_tr, n_en = len(example_tr.split()), len(example_en.split())
    if n_en and not 0.4 <= n_tr / n_en <= 2.5:
        return True
    clue = stems(clue_tr)
    tr = stems(example_tr)
    return 0 < len(clue_tr.split()) <= 2 and bool(clue) and not any(stem_match(c, w) for c in clue for w in tr)


def poor_context(example, answer):
    ws = words(example)
    if len(ws) > 5:
        return False
    ans = {w.lower() for w in words(answer)}
    content = [w for w in ws if w.lower() not in FUNCTION_WORDS and w.lower() not in ans]
    return len(content) <= 1


def frame_key(blanked):
    return " ".join(w.lower() for w in re.findall(r"___|[A-Za-z']+", blanked))


def evaluate(row):
    """Per-card rules R1–R6, R8 (R7 needs the whole level, see apply_frames)."""
    ans, ex = row["answer"].strip(), row["example_en"].strip()
    hit = []
    if row["rule"].strip().upper() == "CONTAINS":
        hit.append("R1")
    n = len(words(ex))
    if n <= 3:
        hit.append("R2")
    if n == 4:
        hit.append("R3")
    if len(ans.replace(" ", "")) <= 3:
        hit.append("R4")
    if poor_context(ex, ans):
        hit.append("R5")
    if no_tr_match(row["clue_tr"], row["example_tr"], ex):
        hit.append("R6")
    if blank(ex, ans) is None:
        hit.append("R8")
    return hit


def apply_frames(rows):
    """R7: blanked frame shared by >= FRAME_MIN cards of the same level. Mutates row['rules']."""
    counts = Counter()
    keys = {}
    for r in rows:
        b = blank(r["example_en"].strip(), r["answer"].strip())
        if b is not None:
            keys[r["id"]] = (r["level"], frame_key(b))
            counts[keys[r["id"]]] += 1
    for r in rows:
        k = keys.get(r["id"])
        if k and counts[k] >= FRAME_MIN:
            r["rules"].append("R7")


def load_exempt(path):
    p = Path(path)
    if not p.is_file():
        return set()
    lines = p.read_text(encoding="utf-8-sig").splitlines()
    return {l.split("#")[0].strip().lower() for l in lines if l.split("#")[0].strip()}


def apply_leaks(rows, exempt):
    """R9: example_en contains another same-level answer (whole word, diacritic-insensitive), except exempt ones."""
    by_level = defaultdict(list)
    for r in rows:
        by_level[r["level"]].append(r)
    for lr in by_level.values():
        pats = [(o, re.compile(r"(?<![^\W_])" + r"\s+".join(re.escape(w) for w in fold(o["answer"]).split()) + r"(?![^\W_])"))
                for o in lr if o["answer"].strip().lower() not in exempt]
        for r in lr:
            ex = fold(r["example_en"])
            if any(o is not r and o["answer"] != r["answer"] and p.search(ex) for o, p in pats):
                r["rules"].append("R9")


def action(rules):
    if not rules:
        return "keep"
    return "exclude" if EXCLUDE_RULES & set(rules) else "rewrite"


def load(content, exempt=frozenset()):
    rows = []
    for path in sorted(Path(content).rglob("*.csv")):
        with open(path, newline="", encoding="utf-8") as f:
            for i, r in enumerate(csv.DictReader(f), start=2):
                if not r.get("id"):
                    continue
                r["level"] = path.parent.name
                r["file"] = f"{path.as_posix()}:{i}"
                r["category"] = r.get("category") or path.stem
                r["rules"] = evaluate(r)
                rows.append(r)
    apply_frames(rows)
    apply_leaks(rows, exempt)
    for r in rows:
        r["rules"].sort(key=RULES.index)
        r["action"] = action(r["rules"])
    return rows


def write_csv(rows, path):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f, lineterminator="\n")
        w.writerow(["id", "level", "sentence", "translation", "failing_rules", "suggested_action"])
        for r in rows:
            if r["rules"]:
                w.writerow([r["id"], r["level"], r["example_en"], r["example_tr"], " ".join(r["rules"]), r["action"]])


def write_report(rows, path):
    total = len(rows)
    flagged = [r for r in rows if r["rules"]]
    lines = ["# Fill-the-blank example scan", "",
             "Generated by `python3 tools/content-qa/fill_blank_scan.py` (read-only; no card is modified). "
             f"{total} cards, {len(flagged)} flagged ({100 * len(flagged) // max(total, 1)} %). Rules overlap.", ""]
    lv = lambda l: [r for r in rows if r["level"] == l]
    lines += ["## Rule × level", "", "| Rule | " + " | ".join(LEVELS) + " | Total |", "|---|" + "---|" * (len(LEVELS) + 1)]
    for rule in RULES:
        c = [sum(rule in r["rules"] for r in lv(l)) for l in LEVELS]
        lines.append(f"| {rule} {NAMES[rule]} | " + " | ".join(map(str, c)) + f" | {sum(c)} |")
    lines += ["", "## Verdict × level", "", "| Action | " + " | ".join(LEVELS) + " | Total |", "|---|" + "---|" * (len(LEVELS) + 1)]
    for a in ["keep", "rewrite", "exclude"]:
        c = [sum(r["action"] == a for r in lv(l)) for l in LEVELS]
        lines.append(f"| {a} | " + " | ".join(map(str, c)) + f" | {sum(c)} |")
    lines += ["", "`exclude` = R1 (CONTAINS) or R4 (answer ≤ 3 letters), left out of the mode by design (§3). "
              "`rewrite` = fixable by a new example sentence. `keep` = no rule hit.", "",
              "## Per category (flagged / total, top 25 by flagged)", "", "| Level/category | Flagged | Total |", "|---|---|---|"]
    cat_t, cat_f = Counter(), Counter()
    for r in rows:
        k = f"{r['level']}/{r['category']}"
        cat_t[k] += 1
        cat_f[k] += bool(r["rules"])
    for k, n in sorted(cat_f.items(), key=lambda kv: (-kv[1], kv[0]))[:25]:
        lines.append(f"| {k} | {n} | {cat_t[k]} |")
    lines += ["", "## Example rows (up to 3 per rule)", ""]
    for rule in RULES:
        hits = [r for r in rows if rule in r["rules"]][:3]
        lines.append(f"### {rule} {NAMES[rule]}")
        for r in hits:
            lines.append(f"- `{r['file']}` {r['id']}: \"{r['example_en']}\" / \"{r['example_tr']}\"")
        lines.append("")
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text("\n".join(lines), encoding="utf-8")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--content", default="content/cards")
    ap.add_argument("--report", default="docs/content-review/FILL-BLANK-SCAN.md")
    ap.add_argument("--csv", default="docs/content-review/FILL-BLANK-REVIEW.csv")
    ap.add_argument("--exempt", default="content/leak-exempt.txt")
    a = ap.parse_args()
    rows = load(a.content, load_exempt(a.exempt))
    write_csv(rows, a.csv)
    write_report(rows, a.report)
    print(f"{len(rows)} cards, {sum(bool(r['rules']) for r in rows)} flagged")


if __name__ == "__main__":
    main()
