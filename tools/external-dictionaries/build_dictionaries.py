#!/usr/bin/env python3
"""外部辞書を、システムユーザー辞書の取り込み形式(読み<TAB>単語<TAB>品詞)へ変換する。

出力は配布可否で分ける。
- distributable: Apache-2.0 / パブリックドメイン相当の元データだけから作る。
- personal:      GPL の元データを含む。MIT のこのリポジトリには入れず、手元で使う。
"""
import argparse
import csv
import io
import re
import sys
import unicodedata
import zipfile
from pathlib import Path

HIRAGANA = re.compile(r"[ぁ-ゔー]+")
KATAKANA_TO_HIRAGANA = {c: chr(c - 0x60) for c in range(0x30A1, 0x30F7)}


def to_hiragana(text: str) -> str:
    return text.translate(KATAKANA_TO_HIRAGANA).translate(str.maketrans("ゐゑ", "いえ"))


def sudachi_entries(csv_zip: Path, name: str, min_reading: int, include_person_name: bool):
    with zipfile.ZipFile(csv_zip) as z, z.open(f"{name}.csv") as f:
        for row in csv.reader(io.TextIOWrapper(f, encoding="utf-8")):
            if len(row) < 13:
                continue
            pos1, pos2, pos3, reading, surface = row[5], row[6], row[7], row[11], row[12]
            if pos1 != "名詞":
                continue
            if pos2 == "固有名詞" and pos3 == "地名":
                continue
            if pos2 == "固有名詞" and pos3 == "人名" and not include_person_name:
                continue
            reading = to_hiragana(reading.translate(str.maketrans("", "", "=・")))
            if len(reading) < min_reading or len(surface) < 2:
                continue
            if not HIRAGANA.fullmatch(reading):
                continue
            if len(surface) > 20 or len(reading) > 30:
                continue
            tag = "固有名詞" if pos2 == "固有名詞" else "名詞"
            yield reading, surface, tag, int(row[3])


def post_entries(ken_all_zip: Path):
    with zipfile.ZipFile(ken_all_zip) as z:
        member = next(n for n in z.namelist() if n.lower().endswith(".csv"))
        with z.open(member) as f:
            for row in csv.reader(io.TextIOWrapper(f, encoding="cp932")):
                yield from _post_rows(row)


def _post_rows(row):
    pref_kana, city_kana, town_kana = row[3], row[4], row[5]
    pref, city, town = row[6], row[7], row[8]
    for kana, surface in ((pref_kana, pref), (city_kana, city), (town_kana, town)):
        if not surface or surface.startswith("以下に掲載がない場合"):
            continue
        surface = re.sub(r"[（(].*", "", surface)
        kana = to_hiragana(unicodedata.normalize("NFKC", re.sub(r"[（(].*", "", kana)))
        if not surface or not HIRAGANA.fullmatch(kana) or len(kana) < 2 or len(surface) < 2:
            continue
        yield kana, surface, "地名", 0


def skk_entries(path: Path, tag: str, min_reading: int):
    raw = path.read_bytes()
    for encoding in ("euc_jp", "utf-8"):
        try:
            text = raw.decode(encoding)
            break
        except UnicodeDecodeError:
            continue
    else:
        return
    for line in text.splitlines():
        if not line or line.startswith(";"):
            continue
        reading, _, rest = line.partition(" ")
        if not rest.startswith("/"):
            continue
        reading = to_hiragana(reading)
        if not HIRAGANA.fullmatch(reading) or len(reading) < min_reading:
            continue
        for cand in rest.strip("/").split("/"):
            surface = cand.split(";")[0]
            if len(surface) < 2 or surface.startswith("(") or "\\" in surface:
                continue
            yield reading, surface, tag, 0


def write(path: Path, entries):
    seen = {}
    for reading, surface, tag, cost in entries:
        key = (reading, surface)
        if key not in seen or cost < seen[key][1]:
            seen[key] = (tag, cost)
    rows = sorted((r, s, t) for (r, s), (t, _) in seen.items())
    path.write_text("".join(f"{r}\t{s}\t{t}\n" for r, s, t in rows), encoding="utf-8")
    return len(rows)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", type=Path, required=True)
    ap.add_argument("--out", type=Path, required=True)
    ap.add_argument("--max-sudachi", type=int, default=30000)
    args = ap.parse_args()
    args.out.mkdir(parents=True, exist_ok=True)

    sudachi = []
    for name in ("core_lex", "notcore_lex"):
        z = args.src / "src_sudachi" / f"{name}.zip"
        if z.exists():
            sudachi += list(sudachi_entries(z, name, min_reading=3, include_person_name=False))
    sudachi.sort(key=lambda e: e[3])
    sudachi = sudachi[: args.max_sudachi]

    post = list(post_entries(args.src / "src_post" / "ken_all.zip"))

    n1 = write(args.out / "sudachi-general.tsv", sudachi)
    n2 = write(args.out / "place-names.tsv", post)
    print(f"distributable: sudachi={n1} place-names={n2}")

    skk = []
    for fname, tag, minr in (
        ("SKK-JISYO.jinmei", "人名", 2),
        ("SKK-JISYO.propernoun", "固有名詞", 3),
        ("SKK-JISYO.geo", "地名", 2),
        ("SKK-JISYO.station", "地名", 2),
    ):
        p = args.src / "src_skk" / fname
        if p.exists():
            skk += list(skk_entries(p, tag, minr))
    n3 = write(args.out / "skk-personal-only.tsv", skk)
    print(f"personal-only: skk={n3}")


if __name__ == "__main__":
    sys.exit(main())
