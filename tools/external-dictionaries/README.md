# External dictionaries

Builds tab-separated dictionaries (`reading<TAB>word<TAB>part of speech`) that
can be imported from the system user dictionary screen.

```bash
python3 build_dictionaries.py --src <dir> --out <dir>
```

`<dir>` is expected to contain `src_sudachi/{core,notcore}_lex.zip`,
`src_post/ken_all.zip` and `src_skk/SKK-JISYO.*`.

| Output | Source | License |
|---|---|---|
| `sudachi-general.tsv` | SudachiDict | Apache-2.0 |
| `place-names.tsv` | Japan Post postal code data | no copyright claimed |
| `skk-personal-only.tsv` | skk-dev/dict | GPL: personal use only, do not commit |

`hand_written/` holds small word lists written by hand (IT terms and
subculture terms). They contain no third-party dictionary data.

The converted dictionary files are not stored in this repository.
