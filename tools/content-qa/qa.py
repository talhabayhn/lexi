#!/usr/bin/env python3
"""Python-only exam pack QA (docs/content/EXAM_PACKS.md). Mirrors the content-compiler rules that can be
reproduced without Gradle; the compiler stays the source of truth.

Usage (repo root): python3 tools/content-qa/qa.py [--content content] [--packs content/exam-packs] [--min 5]
Exit 0 = no errors, 1 = errors (printed as file:line: error: message).
"""
import argparse
import csv
import re
import sys
import unicodedata
from collections import Counter, defaultdict
from pathlib import Path

LEVELS = ["A1", "A2", "B1", "B2", "C1", "NAT"]
CARD_COLUMNS = "id,answer,letter,rule,pos,clue_tr,example_en,example_tr,difficulty,tags,category".split(",")
PACK_COLUMNS = ["id", "name", "description", "is_pro", "sort"]
UNIT_COLUMNS = ["card_id", "unit"]
POS = {"noun", "verb", "adj", "adv", "phrase", "idiom", "phrasal"}
SLUG = re.compile(r"[a-z0-9]+(-[a-z0-9]+)*")
TAG = re.compile(r"[a-z0-9]+([-:][a-z0-9]+)*")
ANSWER = re.compile(r"[A-Z]+( [A-Z]+)*")

errors, warnings = [], []


def err(src, msg):
    errors.append(f"{src}: error: {msg}")


def warn(src, msg):
    warnings.append(f"{src}: warning: {msg}")


def norm(text):
    text = unicodedata.normalize("NFD", text.lower())
    text = "".join(c for c in text if unicodedata.category(c) != "Mn").replace("ı", "i")
    return re.sub(r"\s+", " ", text)


def pattern(phrase, whole=True):
    words = r"\s+".join(re.escape(w) for w in norm(phrase).strip().split(" "))
    return re.compile(r"(?<![^\W_])" + words + (r"(?![^\W_])" if whole else ""))


def read_table(path, columns):
    with open(path, encoding="utf-8-sig", newline="") as f:
        rows = list(csv.reader(f))
    if not rows:
        err(path, "empty file")
        return []
    header = rows[0]
    if sorted(header) != sorted(columns):
        err(f"{path}:1", f"header must be exactly {','.join(columns)}")
        return []
    out = []
    for i, row in enumerate(rows[1:], start=2):
        if len(row) != len(header):
            err(f"{path}:{i}", f"row has {len(row)} values, header has {len(header)}")
            continue
        rec = dict(zip(header, row))
        for k, v in rec.items():
            if v != v.strip() or any(ord(c) < 32 for c in v):
                err(f"{path}:{i}", f"{k}: leading/trailing whitespace or control chars")
        rec["_src"] = f"{path}:{i}"
        out.append(rec)
    return out


def load_blocklist(path):
    terms = {"en": [], "tr": []}
    section = None
    if not path.is_file():
        return terms
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        if line in ("[en]", "[tr]"):
            section = line[1:-1]
        elif section:
            terms[section].append(line)
    return terms


def check_card(c, level, categories, blocklist):
    src = c["_src"]
    cid, answer, letter, rule = c["id"], c["answer"], c["letter"], c["rule"]
    if not SLUG.fullmatch(cid) or not cid.startswith(level.lower() + "-"):
        err(src, f"id '{cid}' must be a slug starting with '{level.lower()}-'")
    letters = len(answer.replace(" ", ""))
    ok = bool(ANSWER.fullmatch(answer)) and 2 <= letters <= 14
    if not ok:
        err(src, f"answer '{answer}' must be A-Z words with single spaces, 2-14 letters")
    if len(letter) != 1 or not "A" <= letter <= "Z":
        err(src, f"letter '{letter}' invalid")
    elif rule == "STARTS":
        if not answer.startswith(letter):
            err(src, f"'{answer}' does not start with {letter}")
    elif rule == "CONTAINS":
        if answer.startswith(letter) or letter not in answer:
            err(src, f"'{answer}' CONTAINS {letter} invalid")
    else:
        err(src, f"rule '{rule}' invalid")
    if c["pos"] not in POS:
        err(src, f"pos '{c['pos']}' invalid")
    clue = c["clue_tr"]
    if not clue or len(clue) > 60:
        err(src, f"clue_tr length {len(clue)} (1-60)")
    if ok and pattern(answer, whole=letters < 4).search(norm(clue)):
        err(src, f"clue_tr contains the answer '{answer}'")
    if ok and not pattern(answer).search(norm(c["example_en"])):
        err(src, f"example_en must contain '{answer}'")
    if not c["example_tr"]:
        err(src, "example_tr empty")
    if c["difficulty"] not in list("12345"):
        err(src, f"difficulty '{c['difficulty']}' invalid")
    for t in filter(None, c["tags"].split("|")):
        if not TAG.fullmatch(t):
            err(src, f"tag '{t}' invalid")
    cat = categories.get(c["category"])
    if c["category"] == "mixed" or cat is None:
        err(src, f"category '{c['category']}' invalid")
    elif level not in cat:
        warn(src, f"category '{c['category']}' not listed for {level}")
    for t in blocklist["en"]:
        for field in ("answer", "example_en"):
            if pattern(t).search(norm(c[field])):
                err(src, f"blocklisted '{t}' in {field}")
    for t in blocklist["tr"]:
        for field in ("clue_tr", "example_tr"):
            if re.search(r"(?<!\w)" + re.escape(t.lower()) + r"(?!\w)", c[field].lower()):
                err(src, f"blocklisted '{t}' in {field}")


def load_cards(root, categories, blocklist):
    cards = []
    if not root.is_dir():
        return cards
    for level in LEVELS:
        for f in sorted((root / level).glob("*.csv")) if (root / level).is_dir() else []:
            for c in read_table(f, CARD_COLUMNS):
                c["_level"] = level
                check_card(c, level, categories, blocklist)
                cards.append(c)
    return cards


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--content", default="content")
    ap.add_argument("--packs", default="content/exam-packs")
    ap.add_argument("--min", type=int, default=5)
    ap.add_argument("--quiet", action="store_true", help="only errors and the summary table")
    a = ap.parse_args()
    content, packs_dir = Path(a.content), Path(a.packs)

    categories = {r["id"]: r["levels"].split("|") for r in read_table(content / "categories.csv",
                  "id,name_tr,name_en,levels,sort,is_pro".split(","))}
    blocklist = load_blocklist(content / "blocklist.txt")
    base = load_cards(content / "cards", categories, blocklist)
    n_base_errors = len(errors)
    pack_cards = load_cards(packs_dir / "cards", categories, blocklist)
    for c in pack_cards:
        c["_pack"] = True
    cards = base + pack_cards
    by_id = {}
    for c in cards:
        if c["id"] in by_id:
            err(c["_src"], f"duplicate id '{c['id']}' (also {by_id[c['id']]['_src']})")
        by_id.setdefault(c["id"], c)
    seen = {}
    for c in cards:
        key = (c["_level"], c["answer"])
        if key in seen:
            err(c["_src"], f"duplicate answer '{c['answer']}' in {c['_level']} (also {seen[key]['_src']})")
        seen.setdefault(key, c)

    # Example leaks, per level, only where a pack card is involved (base-only leaks are the compiler's job).
    by_level = defaultdict(list)
    for c in cards:
        by_level[c["_level"]].append(c)
    for level, lc in by_level.items():
        pats = [(o, pattern(o["answer"])) for o in lc]
        for c in lc:
            ex = norm(c["example_en"])
            for o, p in pats:
                if o is c or o["answer"] == c["answer"]:
                    continue
                if (c.get("_pack") or o.get("_pack")) and p.search(ex):
                    err(c["_src"], f"example_en of '{c['id']}' contains the answer '{o['answer']}' of '{o['id']}'")

    packs = read_table(packs_dir / "packs.csv", PACK_COLUMNS) if (packs_dir / "packs.csv").is_file() else []
    used = set()
    table = {}
    for p in packs:
        uf = packs_dir / "units" / f"{p['id']}.csv"
        if not uf.is_file():
            err(p["_src"], f"pack '{p['id']}' has no units file")
            continue
        refs = read_table(uf, UNIT_COLUMNS)
        ids, answers, letters = set(), {}, Counter()
        for r in refs:
            cid = r["card_id"]
            if cid not in by_id:
                err(r["_src"], f"unknown card '{cid}'")
                continue
            if cid in ids:
                err(r["_src"], f"duplicate card '{cid}' in pack '{p['id']}'")
            ids.add(cid)
            used.add(cid)
            c = by_id[cid]
            if c["answer"] in answers:
                warn(r["_src"], f"answer '{c['answer']}' twice in pack '{p['id']}' ({answers[c['answer']]}, {cid})")
            answers[c["answer"]] = cid
            letters[c["letter"]] += 1
        table[p["id"]] = (letters, len(ids), sum(1 for i in ids if by_id[i].get("_pack")))
        for L in map(chr, range(65, 91)):
            if letters[L] < a.min:
                err(uf, f"pack '{p['id']}' letter {L} has {letters[L]} card(s), minimum {a.min}")
    for c in pack_cards:
        if c["id"] not in used:
            warn(c["_src"], f"pack card '{c['id']}' is not used by any pack")

    out = errors[n_base_errors:] if a.quiet else errors
    if n_base_errors and a.quiet:
        print(f"({n_base_errors} errors in base content hidden)")
    for line in ([] if a.quiet else warnings) + out:
        print(line)
    for pid, (letters, total, new) in table.items():
        print(f"{pid}: {total} cards ({new} pack-only, {total - new} referenced) | " +
              " ".join(f"{L}{letters[L]}" for L in map(chr, range(65, 91))))
    print(f"{len(errors)} error(s), {len(warnings)} warning(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
