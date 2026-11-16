#!/usr/bin/env python3
"""Level-consistency audit of content/cards (read-only; writes only the two report files).

Usage (repo root): python3 tools/content-qa/level_audit.py
Writes docs/content-review/LEVEL-AUDIT.md and docs/content-review/LEVEL-AUDIT.csv. Suggests only, applies nothing.

The word lists below are reviewer judgement (no Oxford/Cambridge data was used or copied).
"""
import csv
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LEVELS = ["A1", "A2", "B1", "B2", "C1", "NAT"]
OUT_MD = ROOT / "docs/content-review/LEVEL-AUDIT.md"
OUT_CSV = ROOT / "docs/content-review/LEVEL-AUDIT.csv"
HARD_LETTERS = set("QXZJ")
EASY_MIN = 8  # letters with fewer easy cards than this "run out"

# --- reviewer judgement lists -------------------------------------------------------------------------------------
# Answers I am confident are usually taught at B1 / B2 (checked against A2 and B1 cards only).
KNOWN_B1 = set("""appointment argue article average chance competition conversation departure distance education
enemy equipment especially explore explode fashion foreign gallery habit illness immediately improve injury invent
journalist law lawyer personal prison quality salary vehicle tax vote warn unless otherwise organize nervous although
actually except express expert degree equal exchange""".upper().split())
KNOWN_B2 = set("""citizen expense flood hurricane ignore judge oxygen require sketch antique quilt acquire adjust assume
awkward biological claim complex consequence convenient convince criticize demand deny desperate distant emphasize
enthusiasm entire evidence exception expand extreme facility finance fortune frequent generation guarantee heritage
highlight identify identity immigrant impress incident individual infection influence insist inspire insurance
investigate investment issue justice launch loan logical loyalty luxury majority massive maximum military motivate
objective occupation opponent outline particular pension permit policy poverty predict prejudice prevent priority
privacy profit property qualification quote reflect refund regret reject relief requirement resident sequence
shortage species struggle summarize survey suspect symbol tackle technique tension theory threat trade unique urban
vary victim violence wealth weapon witness worth""".upper().split())

# A1 words that are not beginner vocabulary (my judgement) -> why.
A1_NOT_BEGINNER = {
    "DOZEN": "uncommon measure word",
    "LORRY": "British-only, rare for beginners",
    "QUARTER": "fraction / time idiom",
    "POUND": "ambiguous (money / weight), abstract for beginners",
    "SURNAME": "formal word, 'last name' is taught first",
    "ISLAND": "irregular spelling, mid-frequency",
    "EXTRA": "abstract adjective",
    "EXIT": "sign vocabulary, not core",
    "FAMOUS": "abstract adjective",
    "HOBBY": "abstract noun",
    "HEALTHY": "abstract adjective",
    "ALPHABET": "metalanguage",
    "EXERCISE": "abstract (noun/verb)",
    "EXAMPLE": "abstract noun",
    "LANGUAGE": "abstract noun",
    "HISTORY": "abstract noun",
    "PROBLEM": "abstract noun",
    "IDEA": "abstract noun",
    "PRICE": "abstract noun",
    "NEWS": "uncountable abstract noun",
    "TOURIST": "mid-frequency",
    "PRIZE": "mid-frequency",
    "SIZE": "abstract noun",
    "SUIT": "ambiguous (clothes / verb)",
    "MIX": "mid-frequency verb",
    "FIX": "mid-frequency verb",
}
# Function words tagged pos=phrase in A1: fine for A1, only listed so the owner sees them.
# Overrides for the A1 difficulty>=3 cards (reviewer judgement): (action, note).
A1_D3_OVERRIDE = {
    "CRAZY": ("keep", "common adjective, d3 is top of the A1 scale"),
    "DOZEN": ("move to a higher level", "A2/B1 word"),
    "DOWNSTAIRS": ("lower difficulty", "taught with upstairs/house vocabulary, d2 is enough"),
    "UPSTAIRS": ("lower difficulty", "taught with downstairs/house vocabulary, d2 is enough"),
    "INTERESTING": ("lower difficulty", "high-frequency adjective, long but easy"),
    "QUARTER": ("keep", "time telling (quarter past), but needs the clue to stay concrete"),
    "LORRY": ("move to a higher level", "British-only; 'truck' is the beginner word"),
    "PLAYGROUND": ("lower difficulty", "concrete school noun"),
    "ISLAND": ("keep", "silent S makes it hard to spell"),
    "VEGETABLE": ("lower difficulty", "core food word, only the spelling is long"),
    "DAUGHTER": ("lower difficulty", "family word taught with son; only the spelling is hard"),
    "KNIFE": ("lower difficulty", "core table word; silent K is a spelling issue only"),
}
# Overrides for A2 difficulty>=4 words that are not in the B1/B2 lists.
A2_D4_OVERRIDE = {
    "SAXOPHONE": ("lower difficulty", "concrete music word, hard spelling only"),
    "ZIGZAG": ("keep", "Z coverage exception"),
    "QUILT": ("move to a higher level", "rare household word"),
    "ANTIQUE": ("move to a higher level", "B2 adjective/noun"),
}


def load(level):
    rows = []
    for path in sorted((ROOT / "content/cards" / level).glob("*.csv")):
        with path.open(encoding="utf-8", newline="") as f:
            for row in csv.DictReader(f):
                row["level"] = level
                row["difficulty"] = int(row["difficulty"])
                row["len"] = len(row["answer"].replace(" ", ""))
                rows.append(row)
    return rows


def exception_tags(c):
    tags = []
    if c["rule"] == "CONTAINS":
        tags.append("CONTAINS")
    if c["letter"] in HARD_LETTERS:
        tags.append("hard letter " + c["letter"])
    return tags


def audit_card(c):
    """Returns (reasons, action) for one card; reasons empty = not flagged."""
    lvl, ans, d, n = c["level"], c["answer"], c["difficulty"], c["len"]
    signals, judgement = [], []
    if lvl == "A1":
        if n >= 9:
            signals.append(f"length {n} >= 9")
        if d >= 3:
            signals.append(f"difficulty {d} >= 3")
        if " " in ans:
            judgement.append("multi-word")
        if c["pos"] in ("idiom", "phrasal"):
            judgement.append(f"pos {c['pos']}")
        elif c["pos"] == "phrase" and " " not in ans:
            judgement.append("function word tagged phrase")
        if ans in A1_NOT_BEGINNER:
            judgement.append("not beginner: " + A1_NOT_BEGINNER[ans])
    elif lvl == "A2":
        if n >= 11:
            signals.append(f"length {n} >= 11")
        if d >= 4:
            signals.append(f"difficulty {d} >= 4")
        if ans in KNOWN_B1:
            judgement.append("reviewer judgement: B1 word")
        if ans in KNOWN_B2:
            judgement.append("reviewer judgement: B2 word")
    elif lvl == "B1":
        if d >= 5:
            signals.append(f"difficulty {d} >= 5")
        if ans in KNOWN_B2:
            judgement.append("reviewer judgement: B2 word")
    exc = exception_tags(c)
    if not signals and not judgement:
        return [], "", exc
    reasons = signals + judgement
    # --- suggested action (suggest only) ---
    action = "keep"
    if lvl == "A1":
        if ans in A1_D3_OVERRIDE and d >= 3:
            action, note = A1_D3_OVERRIDE[ans]
            reasons.append(note)
        elif ans in A1_NOT_BEGINNER and ans in ("DOZEN", "LORRY", "SURNAME", "EXTRA", "EXIT", "NEWS", "ALPHABET"):
            action = "move to a higher level"
        elif d >= 3:
            action = "lower difficulty"
    elif lvl == "A2":
        if ans in A2_D4_OVERRIDE and d >= 4:
            action, note = A2_D4_OVERRIDE[ans]
            reasons.append(note)
        elif ans in KNOWN_B2 or ans in KNOWN_B1:
            action = "move to a higher level"
        elif d >= 4:
            action = "lower difficulty"
    elif lvl == "B1":
        if ans in KNOWN_B2:
            action = "move to a higher level"
        elif d >= 5:
            action = "lower difficulty"
    if exc:
        # CONTAINS / Q X Z J cards are deliberate coverage exceptions: a signal alone never makes them a problem
        reasons.append("coverage exception: " + ", ".join(exc))
        if not judgement and ans not in A1_D3_OVERRIDE and ans not in A2_D4_OVERRIDE:
            action = "keep"
    return reasons, action, exc


def md_table(headers, rows):
    out = ["| " + " | ".join(headers) + " |", "|" + "|".join("---" for _ in headers) + "|"]
    out += ["| " + " | ".join(str(x) for x in r) + " |" for r in rows]
    return "\n".join(out)


def esc(text):
    return text.replace("|", "/")


def write_review_sheet(lvl, items):
    """Numbered, mobile-readable review sheet of the flagged cards of one level, grouped by reason."""
    groups = defaultdict(list)
    for c, reasons, action, exc in items:
        if any("B2 word" in r for r in reasons):
            key = "Looks like B2 (reviewer judgement)"
        elif any("B1 word" in r for r in reasons):
            key = "Looks like B1 (reviewer judgement)"
        else:
            key = "Mechanical signal only (length / difficulty)"
        groups[key].append((c, reasons, action, exc))
    out = [f"# {lvl} level review sheet", "",
           f"{len(items)} flagged cards, suggestions only (nothing applied). Reasons marked reviewer judgement are my "
           "own knowledge, not Oxford/Cambridge data. Action = keep / lower difficulty / move to a higher level / drop.", ""]
    n = 0
    for key in sorted(groups):
        rows = sorted(groups[key], key=lambda t: (t[0]["difficulty"], t[0]["answer"]))
        out += [f"## {key}: {len(rows)}", ""]
        table = []
        for c, reasons, action, exc in rows:
            n += 1
            tags = f" ({c['rule']})" if c["rule"] == "CONTAINS" else ""
            why = "; ".join(r for r in reasons if not r.startswith("reviewer judgement"))
            table.append([n, f"**{c['answer'].lower()}** {c['letter']}{tags}<br>d{c['difficulty']} `{c['id']}`",
                          f"{esc(c['clue_tr'])}<br>_{esc(c['example_en'])}_",
                          f"**{action}**" + (f"<br>{esc(why)}" if why else "")])
        out += [md_table(["#", "Card", "Clue / example", "Suggest"], table), ""]
    (ROOT / f"docs/content-review/LEVEL-REVIEW-{lvl}.md").write_text("\n".join(out), encoding="utf-8")


def main():
    cards = {lvl: load(lvl) for lvl in LEVELS}
    md = ["# Level audit", "",
          "Read-only audit of `content/cards` (exam-pack-only cards excluded). Signals are simple and mechanical; "
          "lists marked *reviewer judgement* are my own knowledge, not Oxford/Cambridge data. Nothing was changed.", ""]

    # a) distribution
    md += ["## 1. Difficulty per level", ""]
    rows = []
    for lvl in LEVELS:
        cnt = Counter(c["difficulty"] for c in cards[lvl])
        rows.append([lvl, len(cards[lvl])] + [cnt.get(i, 0) for i in range(1, 6)])
    md += [md_table(["Level", "Cards", "d1", "d2", "d3", "d4", "d5"], rows), ""]

    md += ["## 2. A1 / A2 easy cards per letter", "",
           "Counts by wheel letter (CONTAINS cards count under their letter).", ""]
    rows = []
    for letter in map(chr, range(65, 91)):
        r = [letter]
        for lvl in ("A1", "A2"):
            ls = [c for c in cards[lvl] if c["letter"] == letter]
            r += [sum(c["difficulty"] == 1 for c in ls), sum(c["difficulty"] == 2 for c in ls), len(ls)]
        rows.append(r)
    md += [md_table(["Letter", "A1 d1", "A1 d2", "A1 all", "A2 d1", "A2 d2", "A2 all"], rows), ""]

    # b, c, d) flagged cards
    flagged = {}
    for lvl in ("A1", "A2", "B1"):
        flagged[lvl] = []
        for c in cards[lvl]:
            reasons, action, exc = audit_card(c)
            if reasons:
                flagged[lvl].append((c, reasons, action, exc))

    def signal_rows(lvl, deliberate):
        rs = []
        for c, reasons, action, exc in flagged[lvl]:
            sig = [r for r in reasons if "length" in r or r.startswith("difficulty")]
            if not sig or bool(exc) != deliberate:
                continue
            rs.append([c["answer"].lower(), c["letter"], c["rule"], c["difficulty"], "; ".join(sig)])
        return sorted(rs)

    md += ["## 3. Too hard for the level (simple signals)", "",
           "Signals: A1 length >= 9 or d >= 3; A2 length >= 11 or d >= 4; B1 d = 5. Length counts letters, not spaces. "
           "Deliberate coverage exceptions (CONTAINS, Q/X/Z/J) are listed separately.", ""]
    for lvl in ("A1", "A2", "B1"):
        regular, deliberate = signal_rows(lvl, False), signal_rows(lvl, True)
        md += [f"### {lvl}: {len(regular)} regular, {len(deliberate)} coverage exceptions", ""]
        if regular:
            md += [md_table(["Answer", "L", "Rule", "d", "Signal"], regular), ""]
        else:
            md += ["_none_", ""]
        if deliberate:
            md += [f"Coverage exceptions ({lvl}):", "", md_table(["Answer", "L", "Rule", "d", "Signal"], deliberate), ""]

    # c) A2 / B1 vs B1/B2 lists
    md += ["## 4. A2 and B1 words that look like a higher level (reviewer judgement)", ""]
    for lvl in ("A2", "B1"):
        rs = sorted([c["answer"].lower(), c["letter"], c["rule"], c["difficulty"],
                     next(r for r in reasons if "reviewer judgement" in r).split(": ")[1]]
                    for c, reasons, action, exc in flagged[lvl] if any("reviewer judgement" in r for r in reasons))
        md += [f"### {lvl}: {len(rs)} cards", ""]
        md += [md_table(["Answer", "L", "Rule", "d", "Looks like"], rs) if rs else "_none_", ""]

    # d) A1 not beginner
    md += ["## 5. A1 answers that are not beginner words (reviewer judgement)", ""]
    rs = sorted([c["answer"].lower(), c["letter"], c["pos"], c["difficulty"],
                 "; ".join(r for r in reasons if r.startswith(("multi-word", "pos ", "function", "not beginner")))]
                for c, reasons, action, exc in flagged["A1"]
                if any(r.startswith(("multi-word", "pos ", "function", "not beginner")) for r in reasons))
    md += [md_table(["Answer", "L", "Pos", "d", "Why"], rs), ""]

    # e) easy-first A1
    md += ["## 6. A1 easy-first: cards left per letter", "",
           f"A letter runs out when it has fewer than {EASY_MIN} cards. 'STARTS' counts only STARTS cards "
           "(the wheel prefers them).", ""]
    rows, out_d1, out_d1s, out_d2 = [], [], [], []
    for letter in map(chr, range(65, 91)):
        ls = [c for c in cards["A1"] if c["letter"] == letter]
        d1 = [c for c in ls if c["difficulty"] == 1]
        d1s = [c for c in d1 if c["rule"] == "STARTS"]
        d12 = [c for c in ls if c["difficulty"] <= 2]
        rows.append([letter, len(d1), len(d1s), len(d12), len(ls)])
        if len(d1) < EASY_MIN:
            out_d1.append(f"{letter}({len(d1)})")
        if len(d1s) < EASY_MIN:
            out_d1s.append(f"{letter}({len(d1s)})")
        if len(d12) < EASY_MIN:
            out_d2.append(f"{letter}({len(d12)})")
    md += [md_table(["Letter", "d1", "d1 STARTS", "d<=2", "All"], rows), "",
           f"- d1 only, runs out: {', '.join(out_d1) or 'none'}",
           f"- d1 STARTS only, runs out: {', '.join(out_d1s) or 'none'}",
           f"- d<=2, runs out: {', '.join(out_d2) or 'none'}", ""]

    # engine facts (static, verified by reading the code)
    md += ["## 7. Engine card selection (facts)", "",
           "- `difficulty` is validated (1-5), stored in content.db and mapped onto `Card`, but no selection code reads it.",
           "- `WheelGenerator.generate` filters the pool by `level` (and category); the category fallback also stays in "
           "the same level; a letter with no card is SKIPPED, never filled from another level.",
           "- Pick = candidates sorted by id -> not recently seen -> STARTS preferred (`preferStarts`) -> never-seen, "
           "else least-recently-seen -> one seeded `nextInt`.",
           "- Cross-level by design: Daily (`WheelGenerator.daily`, A1x6 A2x5 B1x5 B2x4 C1x3 NAT x3), review wheel "
           "(due notebook cards of any level), exam packs (any level), level test (adaptive, moves up/down a level).",
           "- Solo wheels and Duel (`wheelPool(level)`) are single-level.",
           "- So an easy-first selection needs a new difficulty rule in `pickFresh`/`preferStarts`; today d1..d3 cards "
           "of a letter are equally likely.", ""]

    OUT_MD.write_text("\n".join(md), encoding="utf-8")

    with OUT_CSV.open("w", encoding="utf-8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["id", "level", "answer", "letter", "rule", "difficulty", "reason", "suggested_action"])
        for lvl in ("A1", "A2", "B1"):
            for c, reasons, action, exc in flagged[lvl]:
                w.writerow([c["id"], lvl, c["answer"], c["letter"], c["rule"], c["difficulty"],
                            "; ".join(reasons), action])
    for lvl in ("A2", "B1"):
        write_review_sheet(lvl, flagged[lvl])
    n = {lvl: len(flagged[lvl]) for lvl in flagged}
    print("flagged", n, "| A1 d1 out:", out_d1, "| d1 STARTS out:", out_d1s, "| d<=2 out:", out_d2)


if __name__ == "__main__":
    main()
