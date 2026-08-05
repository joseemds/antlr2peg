#!/usr/bin/env python3
"""Apply random string mutations (deletion, duplication, keyword insertion) to a file."""

from __future__ import annotations

import argparse
import json
import random
import re
import sys
from pathlib import Path
from typing import List, Optional


def delete_random_sequence(s: str) -> str:
    """Return s with a random contiguous sequence deleted."""
    if len(s) <= 1:
        return ""
    pos_start = random.randint(0, len(s) - 2)
    pos_end = random.randint(pos_start + 1, len(s) - 1)
    return s[:pos_start] + s[pos_end:]


def duplicate_random_sequence(s: str) -> str:
    """Return s with a random contiguous sequence duplicated."""
    if s == "":
        return s
    if len(s) == 1:
        return s + s
    pos_start = random.randint(0, len(s) - 2)
    pos_end = random.randint(pos_start + 1, len(s) - 1)
    seq = s[pos_start:pos_end]
    return s[:pos_end] + seq + s[pos_end:]


def insert_keyword(s: str, tokens: List[str]) -> str:
    """Return s with a random token inserted at a random position."""
    if not tokens:
        return s
    new_seq = random.choice(tokens)
    pos = random.randint(0, len(s))
    return s[:pos] + new_seq + s[pos:]


def mutate(s: str, tokens: List[str]) -> str:
    """Return s with one random mutation applied."""
    mutators = [delete_random_sequence, duplicate_random_sequence]
    if tokens:
        mutators.append(lambda x: insert_keyword(x, tokens))
    mutator = random.choice(mutators)
    return mutator(s)


def load_tokens(tokens_path: Optional[str]) -> List[str]:
    """Load a JSON list of tokens from disk, or return [] if no path given."""
    if not tokens_path:
        return []
    with open(tokens_path, "r") as f:
        return json.load(f)


def extract_tokens_from_grammar(grammar_text: str) -> List[str]:
    """Extract quoted literal tokens from a .g4 grammar's contents.

    Ported as-is from the standalone tokens.json generator script:
    same regex, same (remove-while-iterating) filter loop, unmodified.
    """
    my_list = re.findall(r"'([^ '][^ ']+)'", grammar_text)

    for e in my_list:
        if '\\' in e:
            my_list.remove(e)

    return my_list


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Apply one or more random mutations (deletion, duplication, "
        "keyword insertion) to the contents of a file."
    )
    parser.add_argument("path", help="Path to the input file to mutate.")
    parser.add_argument(
        "-o",
        "--output",
        help="Path to write the mutated output. If omitted, the result is "
        "printed to stdout (unless -i/--in-place is given).",
    )
    parser.add_argument(
        "-i",
        "--in-place",
        action="store_true",
        help="Rewrite the input file in place. Ignored if -o/--output is given.",
    )
    token_source = parser.add_mutually_exclusive_group()
    token_source.add_argument(
        "-t",
        "--tokens",
        help="Path to a JSON file containing a list of keyword strings to use "
        "for the insertion mutation. If omitted, keyword insertion is skipped.",
    )
    token_source.add_argument(
        "-g",
        "--grammar",
        help="Path to a .g4 grammar file. Quoted literal tokens are extracted "
        "from it on the fly (same extraction logic as the standalone "
        "tokens.json generator) and used for the insertion mutation.",
    )
    parser.add_argument(
        "--save-tokens",
        help="If set alongside -g/--grammar, also write the extracted token "
        "list to this path as JSON (for reuse with -t later).",
    )
    parser.add_argument(
        "-s",
        "--seed",
        help="Random seed to use (any string/number). If omitted, a random "
        "seed is generated and reported on stderr for reproducibility.",
    )
    parser.add_argument(
        "--min-mutations",
        type=int,
        default=1,
        help="Minimum number of mutations to apply (default: 1).",
    )
    parser.add_argument(
        "--max-mutations",
        type=int,
        default=3,
        help="Maximum number of mutations to apply (default: 3).",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    if args.min_mutations < 0 or args.max_mutations < args.min_mutations:
        print(
            "Error: --max-mutations must be >= --min-mutations, both >= 0",
            file=sys.stderr,
        )
        sys.exit(1)

    seed = args.seed if args.seed is not None else random.randrange(sys.maxsize)
    random.seed(seed)
    print(f"Using seed: {seed}", file=sys.stderr)

    in_path = Path(args.path)
    try:
        original = in_path.read_text()
    except OSError as e:
        print(f"Error reading {args.path}: {e}", file=sys.stderr)
        sys.exit(1)

    try:
        if args.grammar:
            grammar_text = Path(args.grammar).read_text()
            tokens = extract_tokens_from_grammar(grammar_text)
        else:
            tokens = load_tokens(args.tokens)
    except (OSError, json.JSONDecodeError) as e:
        source = args.grammar or args.tokens
        print(f"Error reading {source}: {e}", file=sys.stderr)
        sys.exit(1)

    print(f"Loaded {len(tokens)} token(s)", file=sys.stderr)

    if args.save_tokens:
        Path(args.save_tokens).write_text(json.dumps(tokens))

    n_mutations = random.randint(args.min_mutations, args.max_mutations)
    mutated = original
    for _ in range(n_mutations):
        mutated = mutate(mutated, tokens)

    if args.output:
        Path(args.output).write_text(mutated)
    elif args.in_place:
        in_path.write_text(mutated)
    else:
        sys.stdout.write(mutated)


if __name__ == "__main__":
    main()
