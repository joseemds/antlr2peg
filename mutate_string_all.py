#!/usr/bin/env python3
"""Mutate previously-generated grammarinator test files.

Walks the same `grammars` / `results` metadata used by the test-generation
script, locates each grammar's already-generated tests on disk, extracts
tokens from that grammar's .g4 file (via extract_tokens_from_grammar), and
applies random mutations (deletion / duplication / keyword insertion) to
every test file. Reuses mutate_string.py directly so the mutation logic
lives in exactly one place.
"""

from __future__ import annotations

import argparse
import importlib.util
import json
import random
import sys
from pathlib import Path
from typing import List, Optional


# Grammar names to always skip, same convention as the generation script.
SKIPS: List[str] = []


def load_json(path: Path) -> object:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        raise SystemExit(f"File not found: {path}")
    except json.JSONDecodeError as exc:
        raise SystemExit(f"Invalid JSON in {path}: {exc}")


def load_mutate_string(module_path: Path):
    """Import mutate_string.py as a module, without running its __main__."""
    spec = importlib.util.spec_from_file_location("mutate_string", module_path)
    if spec is None or spec.loader is None:
        raise SystemExit(f"Could not load mutate_string.py from {module_path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description="Mutate all previously-generated grammarinator test files, "
        "using tokens extracted from each grammar's .g4 file."
    )
    p.add_argument("grammars", type=Path, help="Path to the grammars JSON (same file used for generation).")
    p.add_argument("results", type=Path, help="Path to the results JSON (same file used for generation).")
    p.add_argument(
        "--corpus", type=Path, default=Path("/tmp/grammars"),
        help="Directory containing the .g4 grammar files (default: /tmp/grammars).",
    )
    p.add_argument(
        "--tests-dir", "--out", dest="tests_dir", type=Path, default=Path("/tmp/test_all"),
        help="Root output directory used when generating tests (default: /tmp/test_all).",
    )
    p.add_argument(
        "--gen-dir", type=Path, default=Path("gen"),
        help="Sub-directory (per grammar) that generated tests live in (default: gen).",
    )
    p.add_argument(
        "--mutate-string-path", type=Path,
        default=Path(__file__).resolve().parent / "mutate_string.py",
        help="Path to mutate_string.py, reused for the mutation and token-extraction logic.",
    )

    out_mode = p.add_mutually_exclusive_group()
    out_mode.add_argument(
        "-i", "--in-place", action="store_true",
        help="Overwrite the generated test files directly.",
    )
    out_mode.add_argument(
        "--output-dir", type=Path,
        help="Write mutated tests here, mirroring the tests-dir structure. Originals are left untouched.",
    )

    p.add_argument("--min-mutations", type=int, default=1, help="Minimum mutations per test (default: 1).")
    p.add_argument("--max-mutations", type=int, default=3, help="Maximum mutations per test (default: 3).")
    p.add_argument(
        "--seed", type=int, default=None,
        help="Random seed. If omitted, a random seed is generated and reported on stderr.",
    )
    return p


def find_grammar_files(entry: dict, corpus: Path) -> tuple[Path, Optional[Path]]:
    grammar_parser = corpus / entry["parser"]
    lexer = corpus / entry["lexer"] if entry.get("lexer") else None
    return grammar_parser, lexer


def mutated_path(test_file: Path, output_dir: Optional[Path], tests_dir: Path, in_place: bool) -> Path:
    if in_place:
        return test_file
    if output_dir is not None:
        rel = test_file.relative_to(tests_dir)
        dest = output_dir / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        return dest
    # Default: write a sibling file, e.g. test_3.gen.json -> test_3.mut.json,
    # leaving the originally-generated test untouched.
    if ".gen." in test_file.name:
        return test_file.with_name(test_file.name.replace(".gen.", ".mut."))
    return test_file.with_name(test_file.stem + ".mut" + test_file.suffix)


def main() -> None:
    args = build_parser().parse_args()

    if args.min_mutations < 0 or args.max_mutations < args.min_mutations:
        raise SystemExit("--max-mutations must be >= --min-mutations, both >= 0")

    ms = load_mutate_string(args.mutate_string_path)

    seed = args.seed if args.seed is not None else random.randrange(sys.maxsize)
    random.seed(seed)
    print(f"Using seed: {seed}", file=sys.stderr)

    grammars_data: list = load_json(args.grammars)
    results_data: dict = load_json(args.results)

    total_files = 0
    total_grammars = 0

    for antlr_grammar in grammars_data:
        name = antlr_grammar["name"]
        if name in SKIPS:
            continue

        if name not in results_data:
            print(f"[SKIP] '{name}' not in results file", file=sys.stderr)
            continue

        entry = results_data[name]
        if entry.get("status") != "success":
            print(f"[SKIP] '{name}' status={entry.get('status')!r}", file=sys.stderr)
            continue

        grammar_parser, lexer = find_grammar_files(antlr_grammar, args.corpus)

        base_dir = args.tests_dir / grammar_parser.stem / args.gen_dir
        if not base_dir.is_dir():
            print(f"[SKIP] '{name}': no generated tests at {base_dir}", file=sys.stderr)
            continue

        extension = name.lower()
        test_files = sorted(base_dir.glob(f"test_*.gen.{extension}"))
        if not test_files:
            print(f"[SKIP] '{name}': no files matching test_*.gen.{extension} in {base_dir}", file=sys.stderr)
            continue

        try:
            grammar_text = grammar_parser.read_text()
            if lexer is not None:
                grammar_text += "\n" + lexer.read_text()
            tokens = ms.extract_tokens_from_grammar(grammar_text)
        except OSError as exc:
            print(f"[FAIL] '{name}': could not read grammar: {exc}", file=sys.stderr)
            continue

        print(f"[{name}] {len(test_files)} test(s), {len(tokens)} token(s)", file=sys.stderr)

        for test_file in test_files:
            try:
                original = test_file.read_text()
            except OSError as exc:
                print(f"  [FAIL] {test_file}: {exc}", file=sys.stderr)
                continue

            n_mutations = random.randint(args.min_mutations, args.max_mutations)
            mutated = original
            for _ in range(n_mutations):
                mutated = ms.mutate(mutated, tokens)

            dest = mutated_path(test_file, args.output_dir, args.tests_dir, args.in_place)
            dest.write_text(mutated)
            total_files += 1

        total_grammars += 1

    print("\n=== Summary ===", file=sys.stderr)
    print(f"Grammars processed: {total_grammars}", file=sys.stderr)
    print(f"Files mutated:      {total_files}", file=sys.stderr)


if __name__ == "__main__":
    main()
