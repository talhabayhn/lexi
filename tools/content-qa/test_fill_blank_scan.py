import unittest

import fill_blank_scan as s


def row(**kw):
    base = dict(id="x", answer="BRIDGE", rule="STARTS", clue_tr="köprü", example_en="They walked across the old bridge today.",
                example_tr="Bugün eski köprüden yürüdüler.", level="A1")
    base.update(kw)
    return base


class Rules(unittest.TestCase):
    def test_clean_card_passes(self):
        self.assertEqual(s.evaluate(row()), [])

    def test_r1_contains(self):
        self.assertIn("R1", s.evaluate(row(rule="CONTAINS")))

    def test_r2_short3(self):
        self.assertIn("R2", s.evaluate(row(example_en="Wipe the bridge.")))

    def test_r3_short4(self):
        r = s.evaluate(row(example_en="The bridge is closed."))
        self.assertIn("R3", r)
        self.assertNotIn("R2", r)

    def test_r4_short_answer(self):
        self.assertIn("R4", s.evaluate(row(answer="CAT", example_en="The cat sat on the warm mat.")))

    def test_r5_poor_context(self):
        self.assertIn("R5", s.evaluate(row(example_en="It is a bridge.")))
        self.assertNotIn("R5", s.evaluate(row(example_en="The old bridge collapsed.")))

    def test_r6_no_tr_match(self):
        self.assertIn("R6", s.evaluate(row(example_tr="Bugün eve gittiler.")))
        self.assertNotIn("R6", s.evaluate(row(example_tr="Bugün eski köprüyü geçtiler.")))
        self.assertNotIn("R6", s.evaluate(row(clue_tr="iki şehri bağlayan yapı", example_tr="Bugün eve gittiler.")))

    def test_r6_turkish_inflection_tolerance(self):
        def r6(clue, tr):
            return "R6" in s.evaluate(row(clue_tr=clue, example_tr=tr, example_en="Ali covered his bridge as he yawned."))
        self.assertFalse(r6("ağız", "Esnerken ağzını eliyle kapattı."))   # consonant drop
        self.assertFalse(r6("boyun", "Yumuşak yastık ağrıyan boynu destekler."))
        self.assertFalse(r6("yüzmek", "Ördekler gölette yüzebilir."))     # shared 3-letter prefix
        self.assertFalse(r6("tuz", "Patates bir tutam tuzla daha güzel."))
        self.assertTrue(r6("ağız", "Esnerken elini kapattı derhal."))     # still catches a missing word
        self.assertTrue(r6("kedi", "Esnerken elini kapattı derhal."))

    def test_r6_length_ratio(self):
        self.assertIn("R6", s.evaluate(row(example_tr="Köprü.")))

    def test_r6_diacritic_insensitive(self):
        self.assertNotIn("R6", s.evaluate(row(clue_tr="çiçek", example_tr="Bahçede bir Cicek acti bugün")))

    def test_r7_generic_frame(self):
        rows = [row(id=f"c{i}", answer=a, example_en=f"This is my {a.lower()}.") for i, a in
                enumerate(["BAG", "BOX", "CAR", "PEN", "CUP"])]
        for r in rows:
            r["rules"] = []
        s.apply_frames(rows)
        self.assertTrue(all("R7" in r["rules"] for r in rows))
        rows[0]["rules"] = []
        other = [row(id="o", example_en="This is my bridge.")]
        other[0]["rules"] = []
        s.apply_frames(other)
        self.assertEqual(other[0]["rules"], [])

    def test_r8_answer_absent_and_whole_word(self):
        self.assertIn("R8", s.evaluate(row(example_en="They crossed the bridges together slowly.")))
        self.assertEqual(s.blank("Cross the Bridge now", "BRIDGE"), "Cross the ___ now")

    def test_r9_leak_and_exempt(self):
        def rows():
            rs = [row(id="a", answer="BRIDGE", example_en="The old bridge crosses a very wide river."),
                  row(id="b", answer="VERY", example_en="It is very cold."),
                  row(id="c", answer="RIVER", example_en="The river is wide and calm."),
                  row(id="d", answer="BRIDGE", level="B1", example_en="A bridge near a river.")]
            for r in rs:
                r["rules"] = []
            return rs
        rs = rows()
        s.apply_leaks(rs, set())
        self.assertEqual([r["id"] for r in rs if "R9" in r["rules"]], ["a", "d"][:1] + [])
        rs = rows()
        s.apply_leaks(rs, {"very", "river"})
        self.assertFalse(any("R9" in r["rules"] for r in rs))

    def test_actions(self):
        self.assertEqual(s.action([]), "keep")
        self.assertEqual(s.action(["R3"]), "rewrite")
        self.assertEqual(s.action(["R3", "R4"]), "exclude")


if __name__ == "__main__":
    unittest.main()
