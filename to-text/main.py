#!/usr/bin/env python3
import argparse
import os
import sys
import glob
from pathlib import Path
import datetime
import time

def read_patterns(pattern_file: Path):
    patterns = []
    if not pattern_file.exists():
        return patterns
    with pattern_file.open('r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            patterns.append(line)
    return patterns

def expand_patterns(patterns, context: Path):
    included = []
    excluded = []
    seen = set()
    for pat in patterns:
        if pat.startswith('!'):
            excluded.append(pat[1:])
            continue
        # Expand pattern relative to context
        glob_pattern = str(context / pat)
        matches = glob.glob(glob_pattern, recursive=True)
        matches = [Path(p) for p in matches if Path(p).is_file()]
        matches.sort()
        for m in matches:
            try:
                rel = str(m.relative_to(context))
            except Exception:
                rel = str(m)
            if rel not in seen:
                seen.add(rel)
                included.append(m)
    # Apply excludes
    if excluded:
        excl_set = set()
        for pat in excluded:
            glob_pattern = str(context / pat)
            for p in glob.glob(glob_pattern, recursive=True):
                if Path(p).is_file():
                    try:
                        excl_set.add(str(Path(p).relative_to(context)))
                    except Exception:
                        excl_set.add(str(Path(p)))
        included = [p for p in included if str(p.relative_to(context)) not in excl_set]
    return included

def is_text_file(path: Path, blocksize=512):
    try:
        with path.open('rb') as f:
            data = f.read(blocksize)
            if b'\x00' in data:
                return False
            # If it decodes with utf-8 it's probably text
            try:
                data.decode('utf-8')
                return True
            except Exception:
                # fallback: if all bytes are printable-ish or newline/space, treat as text
                if all((32 <= b <= 126) or b in (9, 10, 13) for b in data):
                    return True
                return False
    except Exception:
        return False

def read_text_file(path: Path, encoding='utf-8'):
    try:
        with path.open('r', encoding=encoding, errors='strict') as f:
            return f.read()
    except Exception:
        # try forgiving read
        try:
            with path.open('r', encoding='utf-8', errors='replace') as f:
                return f.read()
        except Exception:
            try:
                with path.open('r', encoding='latin-1', errors='replace') as f:
                    return f.read()
            except Exception:
                return None

def write_header(out, relpath, path: Path):
    size = path.stat().st_size
    mtime = datetime.datetime.fromtimestamp(path.stat().st_mtime).isoformat()
    out.write(f"\n==== FILE: {relpath} ====\n")
    out.write(f"Path: {relpath}\n")
    out.write(f"Size: {size} bytes\n")
    out.write(f"Modified: {mtime}\n")
    out.write("---- CONTENT START ----\n")

def write_footer(out):
    out.write("\n---- CONTENT END ----\n")

def main():
    parser = argparse.ArgumentParser(description='Collect files matched by patterns into a single txt.')
    parser.add_argument('--context', '-c', default='.', help='Context folder to apply patterns (default: current dir).')
    parser.add_argument('--pattern', '-p', default='to-text.pattern', help='Pattern file path relative to context (default: to-text.pattern).')
    parser.add_argument('--output', '-o', default='to_text_output.txt', help='Output txt file path.')
    parser.add_argument('--encoding', default='utf-8', help='Preferred reading encoding (default: utf-8).')
    parser.add_argument('--include-binary', action='store_true', help='Include binary files by inserting <binary omitted> placeholder.')
    parser.add_argument('--append', action='store_true', help='Append to existing output file instead of overwrite.')
    parser.add_argument('--verbose', '-v', action='store_true', help='Show progress.')
    args = parser.parse_args()

    context = Path(args.context).resolve()
    if not context.exists():
        print(f"Context path does not exist: {context}", file=sys.stderr)
        sys.exit(1)

    # --pattern 인자로 지정된 경로 우선 사용:
    # 1) args.pattern이 절대경로 또는 cwd 기준 존재하면 사용
    # 2) 없으면 context/<pattern> 사용
    # 3) 없으면 에러
    cand = Path(args.pattern)
    pattern_file = None
    if cand.exists() and cand.is_file():
        pattern_file = cand.resolve()
    else:
        maybe = context / args.pattern
        if maybe.exists() and maybe.is_file():
            pattern_file = maybe.resolve()
    if pattern_file is None:
        print(f"Pattern file not found: {args.pattern}", file=sys.stderr)
        print("Searched: cwd/<pattern> (or absolute), then context/<pattern>", file=sys.stderr)
        sys.exit(1)
 
    patterns = read_patterns(pattern_file)
    if args.verbose:
        print(f"Using context: {context}")
        print(f"Using pattern file: {pattern_file}")
        print(f"Patterns ({len(patterns)}): {patterns}")

    if not patterns:
        print("No patterns found; exiting.", file=sys.stderr)
        sys.exit(1)

    matches = expand_patterns(patterns, context)
    if args.verbose:
        print(f"Found {len(matches)} files (before filtering).")

    mode = 'a' if args.append else 'w'
    written = 0
    try:
        with open(args.output, mode, encoding='utf-8') as out:
            for p in matches:
                try:
                    rel = str(p.relative_to(context))
                except Exception:
                    rel = str(p)
                if not p.exists():
                    continue
                if not is_text_file(p):
                    if args.include_binary:
                        write_header(out, rel, p)
                        out.write("<binary file omitted>\n")
                        write_footer(out)
                        written += 1
                    else:
                        if args.verbose:
                            print(f"Skipping binary file: {rel}")
                        continue
                else:
                    content = read_text_file(p, encoding=args.encoding)
                    if content is None:
                        if args.verbose:
                            print(f"Failed reading file (skipped): {rel}")
                        continue
                    write_header(out, rel, p)
                    out.write(content)
                    write_footer(out)
                    written += 1
                if args.verbose:
                    print(f"Added: {rel}")
    except Exception as e:
        print(f"Failed writing output: {e}", file=sys.stderr)
        sys.exit(1)

    if args.verbose:
        print(f"Total files written: {written}")
    else:
        print(f"Done. Wrote {written} files to {args.output}")

if __name__ == '__main__':
    main()