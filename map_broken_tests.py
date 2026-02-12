from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple, Union

# --------------------------
# Path parsing (class/method)
# --------------------------

CLASS_RE = re.compile(r"^class(\d+)$")
METHOD_RE = re.compile(r"^method(\d+)$")

ValueT = Union[Tuple[int, int], List[Tuple[int, int]]]


def find_class_method_from_path(p: Path) -> Optional[Tuple[int, int]]:
    class_num = None
    method_num = None
    for part in p.parts:
        m = CLASS_RE.match(part)
        if m:
            class_num = int(m.group(1))
        m = METHOD_RE.match(part)
        if m:
            method_num = int(m.group(1))
    if class_num is None or method_num is None:
        return None
    return (class_num, method_num)


def build_index(sorted_root: Path, key_without_ext: bool, duplicate: str) -> Dict[str, ValueT]:
    index: Dict[str, ValueT] = {}

    for java_file in sorted_root.rglob("*.java"):
        cm = find_class_method_from_path(java_file)
        if cm is None:
            continue

        key = java_file.stem if key_without_ext else java_file.name

        if key not in index:
            index[key] = cm
            continue

        existing = index[key]

        if duplicate == "first":
            continue

        if duplicate == "list":
            if isinstance(existing, tuple):
                index[key] = [existing, cm]
            else:
                existing.append(cm)
            continue

        if duplicate == "error":
            if isinstance(existing, tuple):
                if existing != cm:
                    raise ValueError(f"Duplicate key with different mapping: {key}: {existing} vs {cm} ({java_file})")
            else:
                if cm not in existing:
                    raise ValueError(f"Duplicate key with different mapping: {key}: {existing} vs {cm} ({java_file})")
            continue

        raise ValueError(f"Unknown duplicate policy: {duplicate}")

    return index


# --------------------------
# Error message normalization
# --------------------------

# Match absolute file paths with optional :[line,col] suffix
# Examples:
#   /Users/.../src/test/java/.../Foo.java:[36,15]
#   /home/.../Foo.java:55
_ABS_PATH_WITH_LOC_RE = re.compile(
    r"(?P<path>(?:[A-Za-z]:)?[\\/](?:[^ \t\n\r:]+[\\/])+(?:[^ \t\n\r:]+))(?P<loc>:\[\d+,\d+\]|:\d+)?"
)

# Keep everything from src/... onwards when possible (handles /.../src/test/java/...).
_SRC_PREFIX_RE = re.compile(r"(?:^|[\\/])(src[\\/].+)$")


def normalize_error_message(msg: Optional[str]) -> Optional[str]:
    if not msg:
        return msg

    def repl(m: re.Match) -> str:
        full_path = m.group("path")
        loc = m.group("loc") or ""

        # Convert windows backslashes for matching, but keep output with '/'
        p = full_path.replace("\\", "/")

        msrc = _SRC_PREFIX_RE.search(p)
        if msrc:
            # Keep src/... part
            return msrc.group(1).replace("\\", "/") + loc

        # Otherwise, collapse to basename
        base = p.split("/")[-1]
        return base + loc

    # Apply to whole message (may contain many file paths)
    return _ABS_PATH_WITH_LOC_RE.sub(repl, msg)


# --------------------------
# Log parsing (compile vs runtime)
# --------------------------

LOG_TS_RE = re.compile(r"_(\d{8})_(\d{6})$")  # ..._YYYYMMDD_HHMMSS.log

_BOILERPLATE_ERROR_PATTERNS = [
    re.compile(r"^\[ERROR\] Failed to execute goal\b"),
    re.compile(r"^\[ERROR\] -> \[Help "),
    re.compile(r"^\[ERROR\] To see the full stack trace\b"),
    re.compile(r"^\[ERROR\] Re-run Maven\b"),
    re.compile(r"^\[ERROR\] For more information\b"),
    re.compile(r"^\[ERROR\] \[Help "),
]

_STOP_PATTERNS = [
    re.compile(r"^\[INFO\] BUILD FAILURE\b"),
    re.compile(r"^\[INFO\] BUILD SUCCESS\b"),
    re.compile(r"^\[INFO\] Total time:"),
    re.compile(r"^\[INFO\] Finished at:"),
    re.compile(r"^\[INFO\] ------------------------------------------------------------------------"),
    re.compile(r"^\[ERROR\] -> \[Help "),
]

_SUREFIRE_ANCHORS = [
    re.compile(r"^\[ERROR\]\s*Tests run:\s*\d+,"),
    re.compile(r"^\[ERROR\]\s*There are test failures\."),
    re.compile(r"^\[ERROR\]\s*Failures:\s*$"),
    re.compile(r"^\[ERROR\]\s*Errors:\s*$"),
    re.compile(r"<<<\s*(FAILURE|ERROR)!\s*$"),
]

_GENERIC_EXCEPTION_RE = re.compile(
    r"^(?:\[ERROR\]\s*)?"
    r"(?P<ex>(?:[a-zA-Z_]\w*(?:\.[a-zA-Z_]\w*)*)"
    r"(?:Exception|Error|AssertionError))"
    r"(?:\b|:)"
)
_EXCLUDE_EXCEPTIONS = {
    "org.apache.maven.lifecycle.LifecycleExecutionException",
    "org.apache.maven.plugin.MojoFailureException",
    "org.apache.maven.plugin.MojoExecutionException",
    "org.codehaus.plexus.component.repository.exception.ComponentLookupException",
}


def _is_boilerplate(line: str) -> bool:
    return any(p.search(line) for p in _BOILERPLATE_ERROR_PATTERNS)


def _is_stop(line: str) -> bool:
    return any(p.search(line) for p in _STOP_PATTERNS)


def _is_stack_continuation(line: str) -> bool:
    s = line.lstrip()
    return (
        s.startswith("at ")
        or s.startswith("\tat ")
        or s.startswith("Caused by:")
        or s.startswith("Suppressed:")
        or s.startswith("... ")
    )


def _find_first_idx(lines: List[str], predicate) -> Optional[int]:
    for i, ln in enumerate(lines):
        if predicate(ln):
            return i
    return None


def _extract_block_after_idx(lines: List[str], start_idx: int) -> Optional[str]:
    collected: List[str] = []
    started = False

    for ln in lines[start_idx:]:
        if _is_stop(ln):
            break

        if ln.startswith("[INFO]"):
            if started and "--------" in ln:
                break
            continue

        if ln.startswith("[ERROR]"):
            if _is_boilerplate(ln):
                if started:
                    break
                continue
            msg = ln[len("[ERROR]"):].lstrip()
            if msg:
                collected.append(msg)
                started = True
            continue

        if started and (_is_stack_continuation(ln) or ln.startswith(" ") or ln.startswith("\t")):
            collected.append(ln.strip())
            continue

        if started and not ln.strip():
            break

    return "\n".join(collected).strip() if collected else None


def extract_error_from_maven_log(text: str) -> Tuple[Optional[str], Optional[str]]:
    """
    Returns (error_type, error_message)
      error_type ∈ {"compilation", "runtime"} (or None if not found)
    """
    lines = text.splitlines()

    comp_idx = _find_first_idx(lines, lambda ln: "[ERROR] COMPILATION ERROR" in ln)
    surefire_idx = _find_first_idx(lines, lambda ln: any(p.search(ln) for p in _SUREFIRE_ANCHORS))

    def is_generic_exception_line(ln: str) -> bool:
        m = _GENERIC_EXCEPTION_RE.match(ln.strip())
        if not m:
            return False
        ex = m.group("ex")
        return ex not in _EXCLUDE_EXCEPTIONS

    generic_idx = _find_first_idx(lines, is_generic_exception_line)

    candidates: List[Tuple[int, str]] = []
    if comp_idx is not None:
        candidates.append((comp_idx, "compilation"))
    if surefire_idx is not None:
        candidates.append((surefire_idx, "runtime"))
    if generic_idx is not None:
        candidates.append((generic_idx, "runtime"))

    if not candidates:
        first_err = _find_first_idx(lines, lambda ln: ln.startswith("[ERROR]") and not _is_boilerplate(ln))
        if first_err is None:
            return None, None
        msg = _extract_block_after_idx(lines, first_err)
        msg = normalize_error_message(msg)
        return ("runtime", msg) if msg else (None, None)

    start_idx, etype = min(candidates, key=lambda x: x[0])

    if etype == "compilation":
        msg = _extract_block_after_idx(lines, start_idx + 1)
        msg = normalize_error_message(msg)
        return ("compilation", msg) if msg else ("compilation", None)

    msg = _extract_block_after_idx(lines, start_idx)
    msg = normalize_error_message(msg)
    return ("runtime", msg) if msg else ("runtime", None)


def build_log_error_map(broken_root: Path, key_without_ext: bool) -> Dict[str, Dict[str, Optional[str]]]:
    out: Dict[str, Dict[str, Optional[str]]] = {}

    for log_file in broken_root.rglob("*.log"):
        if "_logs" not in {p.name for p in log_file.parents}:
            continue

        stem = log_file.stem
        stem2 = LOG_TS_RE.sub("", stem)
        key = stem2 if key_without_ext else (stem2 + ".java")

        try:
            txt = log_file.read_text(encoding="utf-8", errors="replace")
        except Exception:
            continue

        etype, msg = extract_error_from_maven_log(txt)
        if etype or msg:
            out.setdefault(key, {"error_type": etype, "error_message": msg})

    return out


# --------------------------
# Output mapping
# --------------------------

def build_broken_mapping(
    broken_root: Path,
    index: Dict[str, ValueT],
    log_info: Dict[str, Dict[str, Optional[str]]],
    key_without_ext: bool,
) -> Dict[str, Any]:
    out: Dict[str, Any] = {}

    for java_file in broken_root.rglob("*.java"):
        if "_logs" in {p.name for p in java_file.parents}:
            continue

        key = java_file.stem if key_without_ext else java_file.name

        cm = index.get(key)
        li = log_info.get(key, {})
        err_type = li.get("error_type")
        err_msg = li.get("error_message")

        if isinstance(cm, tuple):
            class_num, method_num = cm
        elif isinstance(cm, list):
            class_num = [c for c, _m in cm]
            method_num = [m for _c, m in cm]
        else:
            class_num, method_num = None, None

        out[key] = {
            "class_number": class_num,
            "method_number": method_num,
            "error_type": err_type,
            "error_message": err_msg,
        }

    return out


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--broken-root", type=Path, required=True, help="Path to broken_tests root.")
    ap.add_argument("--sorted-root", type=Path, required=True, help="Path to round0_testmethods_cli root.")
    ap.add_argument("--key-without-ext", action="store_true",
                    help="Use filename without .java as key (recommended; matches log stems).")
    ap.add_argument("--duplicate", choices=["first", "list", "error"], default="first",
                    help="How to handle duplicate filenames in sorted-root.")
    ap.add_argument("--out", type=Path, default=Path("broken_test_map_with_errors.json"),
                    help="Output JSON file path.")
    args = ap.parse_args()

    index = build_index(args.sorted_root, key_without_ext=args.key_without_ext, duplicate=args.duplicate)
    log_info = build_log_error_map(args.broken_root, key_without_ext=args.key_without_ext)
    broken_map = build_broken_mapping(args.broken_root, index, log_info, key_without_ext=args.key_without_ext)

    args.out.parent.mkdir(parents=True, exist_ok=True)
    with args.out.open("w", encoding="utf-8") as f:
        json.dump(broken_map, f, indent=2, sort_keys=True)

    total_tests = sum(
        1 for p in args.broken_root.rglob("*.java")
        if "_logs" not in {q.name for q in p.parents}
    )
    with_err = sum(1 for v in broken_map.values() if v.get("error_message"))
    with_cm = sum(
        1 for v in broken_map.values()
        if v.get("class_number") is not None and v.get("method_number") is not None
    )
    comp = sum(1 for v in broken_map.values() if v.get("error_type") == "compilation")
    run = sum(1 for v in broken_map.values() if v.get("error_type") == "runtime")

    print(f"Wrote {len(broken_map)} records to: {args.out}")
    print(f"Broken tests discovered: {total_tests}")
    print(f"Have class/method mapping: {with_cm}")
    print(f"Have extracted error message: {with_err}")
    print(f"Error type counts: compilation={comp}, runtime={run}")


if __name__ == "__main__":
    main()
