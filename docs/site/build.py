#!/usr/bin/env python3
"""Builds the Lexi website (landing, privacy, terms, support; TR + EN) into a static folder.

Usage:  python3 docs/site/build.py [--out _site]

Legal pages are generated from docs/store/privacy-policy-*.md and terms-*.md on every build, so the
site never drifts from the store drafts. DRAFT banners and [PLACEHOLDERS] are kept on purpose until
the owner finalises the texts (docs/RELEASE.md). Standard library only; deployed by .github/workflows/pages.yml.
"""
from __future__ import annotations

import argparse
import html
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
STORE = ROOT / "docs" / "store"
FONTS = ROOT / "docs" / "design" / "fonts"
ICON = ROOT / "docs" / "store" / "graphics" / "play" / "icon-512.png"

# Brand name lives here once for the site (app: AppInfo.NAME + string resources).
NAME = "Lexi"

T = {
    "en": {
        "html_lang": "en",
        "tagline": "English vocabulary, A to Z",
        "hero": "Learn English words with a fun A–Z wheel.",
        "lead": "Read a Turkish clue, find the English word for the current letter and type it with the "
        "letter keys. Six levels, from A1 to native.",
        "features": [
            ("Six levels", "A1, A2, B1, B2, C1 and Native (idioms, phrasal verbs)."),
            ("Game modes", "Classic, Practice, Hardcore, Daily Wheel, Review Wheel and a same-device duel."),
            ("Learn, not just play", "Review list after every game, word notebook with spaced repetition, stats."),
            ("Works offline", "All words are stored on your device. No account needed."),
        ],
        "soon": "Coming soon to Google Play and the App Store.",
        "nav": {"home": "Home", "privacy": "Privacy", "terms": "Terms", "support": "Support"},
        "other": "Türkçe",
        "support_title": "Support",
        "support": [
            ("Contact", "Questions, feedback or a wrong clue? Write to **[CONTACT EMAIL]**. "
             "Please include your device model and app version (Settings)."),
            ("Restore purchases", "On a new or reinstalled device open **Settings → Restore purchases**, signed in "
             "to the same store account. Purchases belong to your store account; Lexi has no accounts."),
            ("Manage or cancel a subscription", "Subscriptions are managed by the store: Google Play → Payments & "
             "subscriptions, or iPhone Settings → your name → Subscriptions."),
            ("Your data", "Progress, notebook and settings are stored only on your device. Deleting the app or "
             "its data removes them. See the [privacy policy](../privacy/)."),
        ],
        "draft": "DRAFT — this page is not final.",
    },
    "tr": {
        "html_lang": "tr",
        "tagline": "A'dan Z'ye İngilizce",
        "hero": "A–Z çarkıyla İngilizce kelime öğren.",
        "lead": "Türkçe ipucunu oku, o harfin İngilizce kelimesini bul ve harf tuşlarıyla yaz. "
        "A1'den ana dil seviyesine altı seviye.",
        "features": [
            ("Altı seviye", "A1, A2, B1, B2, C1 ve Native (deyimler, phrasal verb'ler)."),
            ("Oyun modları", "Klasik, Alıştırma, Hardcore, Günün Çarkı, Tekrar Çarkı ve aynı cihazda düello."),
            ("Sadece oynama, öğren", "Her oyundan sonra tekrar listesi, aralıklı tekrarlı kelime defteri, istatistikler."),
            ("Çevrimdışı çalışır", "Tüm kelimeler cihazında saklanır. Hesap gerekmez."),
        ],
        "soon": "Yakında Google Play ve App Store'da.",
        "nav": {"home": "Ana sayfa", "privacy": "Gizlilik", "terms": "Koşullar", "support": "Destek"},
        "other": "English",
        "support_title": "Destek",
        "support": [
            ("İletişim", "Soru, öneri ya da hatalı bir ipucu mu? **[CONTACT EMAIL]** adresine yazın. "
             "Lütfen cihaz modelinizi ve uygulama sürümünü (Ayarlar) ekleyin."),
            ("Satın alımları geri yükleme", "Yeni ya da yeniden kurulan cihazda, aynı mağaza hesabıyla "
             "**Ayarlar → Satın alımları geri yükle**'yi açın. Satın alımlar mağaza hesabınıza bağlıdır; Lexi'de hesap yoktur."),
            ("Aboneliği yönetme ya da iptal", "Abonelikleri mağaza yönetir: Google Play → Ödemeler ve abonelikler "
             "ya da iPhone Ayarlar → adınız → Abonelikler."),
            ("Verileriniz", "İlerleme, defter ve ayarlar yalnızca cihazınızda saklanır. Uygulamayı ya da verilerini "
             "silmek bunları kaldırır. Bkz. [gizlilik politikası](../privacy/)."),
        ],
        "draft": "TASLAK — bu sayfa nihai değil.",
    },
}

CSS = """
@font-face{font-family:Jost;font-weight:500;src:url(Jost-Medium.ttf) format("truetype");font-display:swap}
@font-face{font-family:Jost;font-weight:600;src:url(Jost-SemiBold.ttf) format("truetype");font-display:swap}
@font-face{font-family:Jost;font-weight:700;src:url(Jost-Bold.ttf) format("truetype");font-display:swap}
@font-face{font-family:Jost;font-weight:800;src:url(Jost-ExtraBold.ttf) format("truetype");font-display:swap}
:root{--cream:#F2EBDD;--ink:#111111;--red:#D9281C;--blue:#1F4FBF;--yellow:#F2C200;--white:#FFFFFF}
*{box-sizing:border-box}
html{color-scheme:light}
body{margin:0;background:var(--cream);color:var(--ink);font-family:Jost,system-ui,sans-serif;font-weight:500;
  font-size:17px;line-height:1.55}
a{color:var(--ink);text-decoration-thickness:2px;text-underline-offset:3px}a:hover{color:var(--blue)}
.wrap{max-width:880px;margin:0 auto;padding:0 16px}
header{border-bottom:3px solid var(--ink);background:var(--cream)}
header .wrap{display:flex;align-items:center;gap:14px;padding-top:14px;padding-bottom:14px;flex-wrap:wrap}
.mark{display:flex;gap:4px}.mark i{display:block;width:20px;height:20px}
.sq{background:var(--red)}.ci{background:var(--yellow);border-radius:50%}.lf{background:var(--blue);border-radius:50% 0 50% 0}
.bk{background:var(--ink)}
.brand{font-weight:800;font-size:30px;letter-spacing:.02em;text-decoration:none;line-height:1}
nav{margin-left:auto;display:flex;gap:16px;flex-wrap:wrap;font-weight:700;font-size:15px}
nav a{text-decoration:none}nav a[aria-current]{border-bottom:3px solid var(--red)}
.lang{border:2px solid var(--ink);padding:2px 10px;background:var(--white)}
.draft{background:var(--yellow);border:3px solid var(--ink);padding:12px 16px;margin:24px 0;font-weight:600}
main{padding:8px 0 56px}
.hero{display:grid;grid-template-columns:1fr auto;gap:32px;align-items:center;padding:40px 0 24px}
.hero h1{font-size:48px;line-height:1.08;font-weight:800;margin:0 0 16px}
.hero p{font-size:20px;margin:0;max-width:520px}
.logo{display:grid;grid-template-columns:repeat(2,88px);gap:10px}
.logo span{width:88px;height:88px;border:4px solid var(--ink);display:flex;align-items:center;justify-content:center;
  font-weight:800;font-size:56px;line-height:1}
.logo .sq,.logo .lf{color:var(--white)}.logo .bk{color:var(--cream);border-color:var(--ink)}
.grid{display:grid;grid-template-columns:repeat(2,1fr);gap:16px;margin:24px 0}
.card{background:var(--white);border:3px solid var(--ink);padding:18px 20px}
.card h3{margin:0 0 6px;font-size:20px;font-weight:700}.card p{margin:0}
.card:nth-child(1){box-shadow:6px 6px 0 var(--red)}.card:nth-child(2){box-shadow:6px 6px 0 var(--yellow)}
.card:nth-child(3){box-shadow:6px 6px 0 var(--blue)}.card:nth-child(4){box-shadow:6px 6px 0 var(--ink)}
.soon{display:inline-block;background:var(--ink);color:var(--cream);font-weight:700;padding:12px 18px;margin-top:16px}
.doc h1{font-size:36px;line-height:1.15;font-weight:800;margin:32px 0 12px}
.doc h2{font-size:22px;font-weight:700;margin:32px 0 8px;padding-top:12px;border-top:3px solid var(--ink)}
.doc h3{font-size:18px;font-weight:700;margin:20px 0 6px}
.doc table{border-collapse:collapse;width:100%;font-size:15px;background:var(--white)}
.doc th,.doc td{border:2px solid var(--ink);padding:8px;text-align:left;vertical-align:top}
.doc th{background:var(--ink);color:var(--cream)}
.tbl{overflow-x:auto;margin:12px 0}
code{font-family:ui-monospace,monospace;font-size:.9em;background:var(--white);border:1px solid var(--ink);padding:0 4px}
footer{border-top:3px solid var(--ink);padding:20px 0 32px;font-size:14px}
footer .wrap{display:flex;gap:16px;flex-wrap:wrap}
@media (max-width:640px){
  .hero{grid-template-columns:1fr;padding-top:24px}.hero h1{font-size:36px}
  .logo{grid-template-columns:repeat(4,64px);gap:8px}.logo span{width:64px;height:64px;font-size:40px;border-width:3px}
  .grid{grid-template-columns:1fr}nav{margin-left:0;width:100%}.doc h1{font-size:28px}
}
"""


def inline(text: str) -> str:
    """Escapes HTML, then applies **bold**, `code` and [link](url)."""
    out = html.escape(text, quote=False)
    out = re.sub(r"`([^`]+)`", r"<code>\1</code>", out)
    out = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", out)
    out = re.sub(r"\[([^\]]+)\]\(([^)\s]+)\)", r'<a href="\2">\1</a>', out)
    return out


def markdown(md: str) -> str:
    """Small Markdown subset used by the store drafts: headings, paragraphs, lists, tables, blockquotes."""
    blocks: list[str] = []
    lines = md.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i].rstrip()
        if not line.strip():
            i += 1
            continue
        if m := re.match(r"(#{1,4})\s+(.*)", line):
            level = len(m.group(1))
            blocks.append(f"<h{level}>{inline(m.group(2))}</h{level}>")
            i += 1
        elif line.startswith(">"):
            quote = []
            while i < len(lines) and lines[i].startswith(">"):
                quote.append(lines[i].lstrip(">").strip())
                i += 1
            blocks.append(f'<div class="draft" role="note">{inline(" ".join(quote))}</div>')
        elif line.startswith("|"):
            rows = []
            while i < len(lines) and lines[i].startswith("|"):
                cells = [c.strip() for c in lines[i].strip().strip("|").split("|")]
                if not all(re.fullmatch(r":?-{2,}:?", c) for c in cells):
                    rows.append(cells)
                i += 1
            head, *body = rows
            th = "".join(f"<th>{inline(c)}</th>" for c in head)
            tb = "".join("<tr>" + "".join(f"<td>{inline(c)}</td>" for c in r) + "</tr>" for r in body)
            blocks.append(f'<div class="tbl"><table><thead><tr>{th}</tr></thead><tbody>{tb}</tbody></table></div>')
        elif re.match(r"\s*([-*]|\d+\.)\s+", line):
            ordered = bool(re.match(r"\s*\d+\.", line))
            items: list[str] = []
            while i < len(lines) and lines[i].strip():
                if m := re.match(r"\s*(?:[-*]|\d+\.)\s+(.*)", lines[i]):
                    items.append(m.group(1))
                elif items:
                    items[-1] += " " + lines[i].strip()
                i += 1
            tag = "ol" if ordered else "ul"
            blocks.append(f"<{tag}>" + "".join(f"<li>{inline(x)}</li>" for x in items) + f"</{tag}>")
        else:
            para = []
            while i < len(lines) and lines[i].strip() and not re.match(r"(#|>|\||\s*[-*]\s|\s*\d+\.\s)", lines[i]):
                para.append(lines[i].strip())
                i += 1
            blocks.append(f"<p>{inline(' '.join(para))}</p>")
    return "\n".join(blocks)


def page(lang: str, key: str, title: str, body: str, depth: int) -> str:
    t = T[lang]
    up = "../" * depth
    # Site root is EN; TR lives under /tr/. Links stay relative so the site works under /<repo>/ on Pages.
    lang_root = up if lang == "en" else up + "tr/"
    other_root = up + "tr/" if lang == "en" else up
    sub = "" if key == "home" else key + "/"
    nav = "".join(
        f'<a href="{lang_root}{"" if k == "home" else k + "/"}"{" aria-current=page" if k == key else ""}>{v}</a>'
        for k, v in t["nav"].items()
    )
    full_title = f"{NAME} — {t['tagline']}" if key == "home" else f"{title} · {NAME}"
    return f"""<!doctype html>
<html lang="{t['html_lang']}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(full_title)}</title>
<meta name="description" content="{html.escape(t['hero'])}">
<meta name="robots" content="noindex">
<link rel="alternate" hreflang="en" href="{up}{sub}">
<link rel="alternate" hreflang="tr" href="{up}tr/{sub}">
<link rel="icon" href="{up}assets/icon.png">
<link rel="stylesheet" href="{up}assets/site.css">
</head>
<body>
<header><div class="wrap">
<span class="mark" aria-hidden="true"><i class="sq"></i><i class="ci"></i><i class="lf"></i></span>
<a class="brand" href="{lang_root}">lexi</a>
<nav>{nav}<a class="lang" href="{other_root}{sub}" hreflang="{'tr' if lang == 'en' else 'en'}">{t['other']}</a></nav>
</div></header>
<main><div class="wrap">
{body}
</div></main>
<footer><div class="wrap"><span>© 2026 [DEVELOPER NAME]</span><span>{NAME} · {t['tagline']}</span></div></footer>
</body>
</html>
"""


def landing(lang: str) -> str:
    t = T[lang]
    cards = "".join(f'<div class="card"><h3>{inline(h)}</h3><p>{inline(p)}</p></div>' for h, p in t["features"])
    return f"""<div class="draft" role="note">{t['draft']}</div>
<section class="hero">
<div><h1>{inline(t['hero'])}</h1><p>{inline(t['lead'])}</p><span class="soon">{t['soon']}</span></div>
<div class="logo" role="img" aria-label="Lexi"><span class="sq">L</span><span class="ci">E</span><span class="lf">X</span><span class="bk">I</span></div>
</section>
<section class="grid">{cards}</section>"""


def support(lang: str) -> str:
    t = T[lang]
    items = "".join(f"<h2>{inline(h)}</h2><p>{inline(p)}</p>" for h, p in t["support"])
    return f'<div class="doc"><div class="draft" role="note">{t["draft"]}</div><h1>{t["support_title"]}</h1>{items}</div>'


def build(out: Path) -> None:
    if out.exists():
        shutil.rmtree(out)
    assets = out / "assets"
    assets.mkdir(parents=True)
    (assets / "site.css").write_text(CSS, encoding="utf-8")
    for font in FONTS.glob("Jost-*.ttf"):
        shutil.copy(font, assets / font.name)
    shutil.copy(FONTS / "OFL.txt", assets / "Jost-OFL.txt")
    if ICON.exists():
        shutil.copy(ICON, assets / "icon.png")
    (out / ".nojekyll").write_text("", encoding="utf-8")

    for lang in ("en", "tr"):
        base = out if lang == "en" else out / "tr"
        depth0 = 0 if lang == "en" else 1
        pages = {
            "home": ("", landing(lang)),
            "privacy": ("privacy", None),
            "terms": ("terms", None),
            "support": (T[lang]["support_title"], support(lang)),
        }
        for key, (title, body) in pages.items():
            if body is None:
                md = (STORE / f"{'privacy-policy' if key == 'privacy' else 'terms'}-{lang}.md").read_text(encoding="utf-8")
                title = re.search(r"^#\s+(.*)$", md, re.M).group(1)
                body = f'<article class="doc">{markdown(md)}</article>'
            target = base if key == "home" else base / key
            target.mkdir(parents=True, exist_ok=True)
            depth = depth0 + (0 if key == "home" else 1)
            (target / "index.html").write_text(page(lang, key, title, body, depth), encoding="utf-8")
    print(f"site written to {out}")


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--out", type=Path, default=ROOT / "_site")
    build(ap.parse_args().out.resolve())
