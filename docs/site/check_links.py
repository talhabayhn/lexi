#!/usr/bin/env python3
"""Builds the Lexi site and checks every internal link, EN<->TR switch and invented domain.

Usage:  python3 docs/site/check_links.py [--out DIR]

Standard library only. Exits 1 when any href/src does not resolve to a file in the built output.
"""
from __future__ import annotations

import argparse
import sys
import tempfile
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import unquote, urldefrag, urlparse

import build

PAGES = ("", "privacy/", "terms/", "support/")
ALLOWED_HOSTS = {"talhabayhn.github.io"}


class Refs(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.refs: list[tuple[str, str]] = []  # (tag.attr, url)
        self.lang_link: str | None = None

    def handle_starttag(self, tag: str, attrs: list) -> None:
        d = dict(attrs)
        for attr in ("href", "src"):
            if d.get(attr):
                self.refs.append((f"{tag}.{attr}", d[attr]))
        if tag == "a" and "lang" in (d.get("class") or "").split():
            self.lang_link = d.get("href")


def resolve(site: Path, page: Path, url: str) -> Path | None:
    target = (page.parent / unquote(urldefrag(url)[0])).resolve()
    if site not in target.parents and target != site:
        return None
    return target / "index.html" if target.is_dir() else target


def check(site: Path) -> list[str]:
    site = site.resolve()
    errors: list[str] = []
    count = 0
    for lang_dir in ("", "tr/"):
        for p in PAGES:
            rel = lang_dir + p
            page = site / rel / "index.html"
            if not page.is_file():
                errors.append(f"missing page: /{rel}")
                continue
            parser = Refs()
            parser.feed(page.read_text(encoding="utf-8"))
            for kind, url in parser.refs:
                u = urlparse(url)
                if u.scheme in ("mailto", "tel"):
                    continue
                if u.scheme or u.netloc:
                    if u.netloc not in ALLOWED_HOSTS:
                        errors.append(f"/{rel}: {kind} external host not allowed: {url}")
                    continue
                if not u.path:
                    continue  # pure #fragment
                count += 1
                found = resolve(site, page, url)
                if found is None or not found.is_file():
                    errors.append(f"/{rel}: {kind} -> {url} does not resolve")
            # The language switch must land on the same page in the other language.
            other = ("" if lang_dir else "tr/") + p
            want = (site / other / "index.html").resolve()
            got = resolve(site, page, parser.lang_link) if parser.lang_link else None
            if got != want:
                errors.append(f"/{rel}: language switch {parser.lang_link!r} should reach /{other}")
    print(f"checked {count} internal links on {2 * len(PAGES)} pages")
    return errors


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--out", type=Path, default=None)
    args = ap.parse_args()
    out = args.out or Path(tempfile.mkdtemp(prefix="lexi_site_"))
    build.build(out.resolve())
    errs = check(out)
    for e in errs:
        print("ERROR", e)
    print("FAILED" if errs else "OK: all links resolve; EN<->TR switches verified on every page")
    sys.exit(1 if errs else 0)
