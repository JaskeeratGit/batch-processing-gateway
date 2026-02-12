from __future__ import annotations

import argparse
import datetime as dt
import json
import os
import re
import shlex
import shutil
import subprocess
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Tuple
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

from prompt_constructor import RepairLoopContext  # prompt_constructor.py must be in same dir / PYTHONPATH


# =========================
# Regex / constants
# =========================

_OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"

_PACKAGE_RE = re.compile(r"^\s*package\s+([a-zA-Z0-9_.]+)\s*;\s*$", re.MULTILINE)
_PUBLIC_CLASS_RE = re.compile(r"\bpublic\s+(?:final\s+)?class\s+([A-Za-z_][A-Za-z0-9_]*)\b")
_CODE_FENCE_RE = re.compile(r"```(?:java|[a-zA-Z0-9_-]+)?\s*([\s\S]*?)```", re.MULTILINE)

_ABS_PATH_RE = re.compile(r"(/[^ \n\t:]+)+/([^/\s:]+\.java)")
_WIN_ABS_PATH_RE = re.compile(r"([A-Za-z]:\\[^ \n\t:]+\\([^\\\s:]+\.java))")
_ANSI_RE = re.compile(r"\x1b\[[0-9;]*m")

_COMPILATION_HEADER_RE = re.compile(r"COMPILATION ERROR", re.IGNORECASE)
_JAVAC_ERR_RE = re.compile(
    r"(cannot find symbol|package .* does not exist|incompatible types|method .* cannot be applied|"
    r"symbol:\s+method|symbol:\s+class|symbol:\s+variable|cannot access|reference to .* is ambiguous)",
    re.IGNORECASE,
)
_JAVA_LOC_RE = re.compile(r"\.java:\[\d+,\d+\]")

_SUREFIRE_FAIL_RE = re.compile(r"(There are test failures|Failed tests:|Tests run:)", re.IGNORECASE)
_EXCEPTION_LINE_RE = re.compile(
    r"([A-Za-z0-9_$.]+Exception|AssertionFailedError|AssertionError|NoSuchMethodError|NoClassDefFoundError|"
    r"NullPointerException|IllegalArgumentException|IllegalStateException)"
)

_TIMEOUT_RE = re.compile(r"(timed out|timeout)", re.IGNORECASE)
_LISTING_LINE_RE = re.compile(r"^\s*(?:[\w.-]+/)*[\w.-]+\.(?:java|log)\s*$")


# =========================
# Small helpers
# =========================

def read_text(p: Path) -> str:
    return p.read_text(encoding="utf-8", errors="replace")


def write_text(p: Path, s: str) -> None:
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(s, encoding="utf-8")


def read_json(p: Path) -> Any:
    return json.loads(read_text(p))


def now_utc_iso() -> str:
    return dt.datetime.utcnow().replace(microsecond=0).isoformat() + "Z"


def safe_stem(s: str) -> str:
    s2 = re.sub(r"[^A-Za-z0-9_.-]+", "_", s).strip("_")
    return s2 or "output"


def parse_java_package(java_src: str) -> Optional[str]:
    m = _PACKAGE_RE.search(java_src or "")
    return m.group(1).strip() if m else None


def infer_pkg_and_public_class(java_src: str) -> Tuple[Optional[str], Optional[str]]:
    pkg = parse_java_package(java_src)
    cls = None
    m2 = _PUBLIC_CLASS_RE.search(java_src)
    if m2:
        cls = m2.group(1).strip()
    return pkg, cls


def _depath(s: str) -> str:
    s = _ABS_PATH_RE.sub(r"\2", s)
    s = _WIN_ABS_PATH_RE.sub(r"\2", s)
    return s


# =========================
# Broken map handling
# =========================

def coerce_record_ids(obj: Any) -> List[str]:
    if isinstance(obj, dict):
        return [str(k) for k in obj.keys()]
    if isinstance(obj, list):
        ids: List[str] = []
        for i, item in enumerate(obj):
            if not isinstance(item, dict):
                continue
            rid = item.get("id") or item.get("test_id") or item.get("path") or item.get("file") or item.get("filename")
            ids.append(str(rid) if rid is not None else str(i))
        return ids
    raise ValueError("broken_tests_mapped.json must be a dict or list")


def get_record_meta(broken_obj: Any, record_id: str) -> Dict[str, Any]:
    if isinstance(broken_obj, dict):
        meta = broken_obj.get(record_id) or {}
        return meta if isinstance(meta, dict) else {}
    if isinstance(broken_obj, list):
        for item in broken_obj:
            if not isinstance(item, dict):
                continue
            rid = item.get("id") or item.get("test_id") or item.get("path") or item.get("file") or item.get("filename")
            if rid is not None and str(rid) == record_id:
                return item
        return {}
    return {}


# =========================
# Unit test discovery
# =========================

def index_java_files(root: Path) -> Dict[str, List[Path]]:
    idx: Dict[str, List[Path]] = {}
    for p in root.rglob("*.java"):
        idx.setdefault(p.name, []).append(p)
    for k in list(idx.keys()):
        idx[k] = sorted(idx[k])
    return idx


def load_unit_test_from_dir(
    *,
    broken_tests_dir: Path,
    java_index: Dict[str, List[Path]],
    record_id: str,
) -> Tuple[str, Path]:
    rid_name = Path(record_id).name
    if not rid_name.endswith(".java"):
        rid_name += ".java"

    matches = java_index.get(rid_name, [])
    if not matches:
        candidate = broken_tests_dir / record_id
        if candidate.exists() and candidate.is_file():
            matches = [candidate]

    if not matches:
        raise FileNotFoundError(f"Could not locate {rid_name} under {broken_tests_dir}")

    chosen = matches[0]
    return read_text(chosen), chosen


# =========================
# Infer focal identifiers from filename
# Format: <ClassName>_<methodName>_<methodnumber>_<...>_Test_<testName>.java
# =========================

def parse_filename_tokens(record_id: str) -> Tuple[Optional[str], Optional[str], Optional[str], Optional[str]]:
    stem = Path(record_id).name
    if stem.endswith(".java"):
        stem = stem[:-5]
    parts = stem.split("_")
    if len(parts) < 5:
        return None, None, None, None
    return parts[0] or None, parts[1] or None, parts[2] or None, parts[3] or None


# =========================
# classMapping.json handling
# =========================

def load_class_mapping(class_map_path: Path) -> Dict[str, Any]:
    obj = read_json(class_map_path)
    if not isinstance(obj, dict):
        raise ValueError("classMapping.json must be a dict {class_key: {...}}")
    return obj


def infer_class_key(
    *,
    class_map: Dict[str, Any],
    class_name: str,
    package_name: Optional[str],
) -> Optional[str]:
    candidates: List[Tuple[str, str]] = []
    for ck, entry in class_map.items():
        if not isinstance(entry, dict):
            continue
        if str(entry.get("className") or "") != class_name:
            continue
        pkg = str(entry.get("packageName") or "")
        if pkg:
            candidates.append((str(ck), pkg))

    if not candidates:
        return None

    if package_name:
        exact = [ck for ck, pkg in candidates if pkg == package_name]
        if exact:
            return exact[0]
        prefix = [ck for ck, pkg in candidates if pkg.startswith(package_name)]
        if prefix:
            return prefix[0]

    candidates.sort(key=lambda x: (len(x[1]), x[1], x[0]))
    return candidates[0][0]


# =========================
# method_file_stem inference for numbered jsons
# =========================

def _method_json_matches_method_name(method_json: Dict[str, Any], method_name: str) -> bool:
    mn = method_json.get("methodName")
    if isinstance(mn, str) and mn == method_name:
        return True
    sig = method_json.get("methodSignature")
    if isinstance(sig, str) and sig.strip().startswith(method_name + "("):
        return True
    brief = method_json.get("brief")
    if isinstance(brief, str) and (method_name + "(") in brief:
        return True
    full = method_json.get("full_method_info")
    if isinstance(full, str) and (method_name + "(") in full:
        return True
    src = method_json.get("sourceCode")
    if isinstance(src, str) and (method_name + "(") in src:
        return True
    return False


def infer_method_file_stem_numbered(
    *,
    class_info_dir: Path,
    package_name: str,
    class_name: str,
    method_name: str,
    method_index_token: Optional[str],
) -> Optional[str]:
    class_dir = class_info_dir.joinpath(*package_name.split("."), class_name)
    if not class_dir.exists():
        return None

    if method_index_token and str(method_index_token).isdigit():
        p = class_dir / f"{method_index_token}.json"
        if p.exists():
            try:
                mj = read_json(p)
                if isinstance(mj, dict) and _method_json_matches_method_name(mj, method_name):
                    return p.stem
            except Exception:
                pass

    json_files = sorted([p for p in class_dir.glob("*.json") if p.name != "class.json"])
    for p in json_files:
        if not p.stem.isdigit():
            continue
        try:
            mj = read_json(p)
        except Exception:
            continue
        if isinstance(mj, dict) and _method_json_matches_method_name(mj, method_name):
            return p.stem

    return None


def method_json_exists(
    class_info_dir: Path,
    package_name: str,
    class_name: str,
    stem: str,
) -> bool:
    if not stem:
        return False
    class_dir = class_info_dir.joinpath(*package_name.split("."), class_name)
    return (class_dir / f"{stem}.json").exists()


# =========================
# OpenRouter call (NOW RETURNS TOKEN USAGE TOO)
# =========================

def openrouter_chat(
    *,
    api_key: str,
    model: str,
    prompt: str,
    temperature: float = 0.0,
    max_tokens: Optional[int] = None,
    site_url: Optional[str] = None,
    app_title: Optional[str] = None,
    timeout_s: int = 180,
) -> Tuple[str, Dict[str, int]]:
    payload: Dict[str, Any] = {
        "model": model,
        "messages": [{"role": "user", "content": prompt}],
        "temperature": temperature,
    }
    if max_tokens is not None:
        payload["max_tokens"] = int(max_tokens)

    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}
    if site_url:
        headers["HTTP-Referer"] = site_url
    if app_title:
        headers["X-Title"] = app_title

    req = Request(_OPENROUTER_URL, data=json.dumps(payload).encode("utf-8"), headers=headers, method="POST")

    try:
        with urlopen(req, timeout=timeout_s) as resp:
            body = resp.read().decode("utf-8", errors="replace")
    except HTTPError as e:
        err_body = ""
        try:
            err_body = e.read().decode("utf-8", errors="replace")
        except Exception:
            pass
        raise RuntimeError(f"OpenRouter HTTPError {e.code}: {err_body or str(e)}") from e
    except URLError as e:
        raise RuntimeError(f"OpenRouter URLError: {e}") from e

    j = json.loads(body)
    choices = j.get("choices") or []
    if not choices:
        raise RuntimeError(f"OpenRouter response missing choices: {body[:2000]}")

    msg = (choices[0].get("message") or {})
    content = msg.get("content")

    if isinstance(content, str):
        text_out = content.strip()
    elif isinstance(content, list):
        parts: List[str] = []
        for part in content:
            if isinstance(part, dict) and isinstance(part.get("text"), str):
                parts.append(part["text"])
            elif isinstance(part, str):
                parts.append(part)
        text_out = "\n".join(parts).strip()
    else:
        text = choices[0].get("text")
        if isinstance(text, str):
            text_out = text.strip()
        else:
            raise RuntimeError(f"OpenRouter response content not found: {body[:2000]}")

    usage = j.get("usage") or {}
    if not isinstance(usage, dict):
        usage = {}

    prompt_tokens = int(usage.get("prompt_tokens") or 0)
    completion_tokens = int(usage.get("completion_tokens") or 0)

    total_tokens = usage.get("total_tokens")
    if total_tokens is None:
        total_tokens = prompt_tokens + completion_tokens
    total_tokens = int(total_tokens or 0)

    reasoning_tokens = usage.get("reasoning_tokens")
    if reasoning_tokens is None and isinstance(usage.get("completion_tokens_details"), dict):
        reasoning_tokens = usage["completion_tokens_details"].get("reasoning_tokens")
    reasoning_tokens = int(reasoning_tokens or 0)

    usage_out = {
        "input_tokens": prompt_tokens,
        "output_tokens": completion_tokens,
        "reasoning_tokens": reasoning_tokens,
        "total_tokens": total_tokens,
    }

    return text_out, usage_out


# =========================
# Extract / normalize LLM Java output
# =========================

def extract_java_from_llm_response(text: str) -> str:
    t = text.strip()

    if "<<Generation Begin>>" in t and "<<Generation Over>>" in t:
        inner = t.split("<<Generation Begin>>", 1)[1].split("<<Generation Over>>", 1)[0].strip()
        if inner:
            return inner

    fences = _CODE_FENCE_RE.findall(t)
    if fences:
        blocks = sorted((b.strip() for b in fences if b and b.strip()), key=len, reverse=True)
        if blocks:
            return blocks[0]

    return t


# =========================
# Project placement + restore
# =========================

@dataclass
class TempFileSwap:
    target_path: Path
    backup_path: Optional[Path]

    def restore(self) -> None:
        try:
            if self.backup_path and self.backup_path.exists():
                self.target_path.parent.mkdir(parents=True, exist_ok=True)
                shutil.move(str(self.backup_path), str(self.target_path))
            else:
                if self.target_path.exists():
                    self.target_path.unlink()
        except Exception:
            pass


def place_test_in_project(
    *,
    src_test_java: Path,
    java_src: str,
    fallback_package: Optional[str],
    fallback_classname: str,
) -> Tuple[Path, str, TempFileSwap]:
    pkg, cls = infer_pkg_and_public_class(java_src)
    pkg = pkg or fallback_package or ""
    cls = cls or fallback_classname

    normalized = java_src
    if pkg and not _PACKAGE_RE.search(java_src):
        normalized = f"package {pkg};\n\n{java_src.lstrip()}"

    rel_dir = Path(*pkg.split(".")) if pkg else Path()
    out_dir = src_test_java / rel_dir
    out_path = out_dir / f"{cls}.java"
    out_dir.mkdir(parents=True, exist_ok=True)

    backup_path: Optional[Path] = None
    if out_path.exists():
        backup_path = out_path.with_suffix(out_path.suffix + ".bak_repairloop")
        try:
            if backup_path.exists():
                backup_path.unlink()
        except Exception:
            pass
        shutil.copy2(str(out_path), str(backup_path))

    write_text(out_path, normalized.rstrip() + "\n")
    return out_path, normalized, TempFileSwap(target_path=out_path, backup_path=backup_path)


# =========================
# Maven run + error summarization
# =========================

def _strip_noise_lines(lines: Iterable[str]) -> List[str]:
    out: List[str] = []
    for ln in lines:
        s = _ANSI_RE.sub("", ln.rstrip("\n"))
        if not s.strip():
            continue
        if s.startswith("Downloading from") or s.startswith("Downloaded from"):
            continue
        out.append(s)
    return out


def _is_listing_line(s: str) -> bool:
    ss = s.strip()
    if not ss:
        return False
    if "[ERROR]" in ss or "COMPILATION ERROR" in ss or "There are test failures" in ss:
        return False
    if _JAVA_LOC_RE.search(ss) or _JAVAC_ERR_RE.search(ss) or _EXCEPTION_LINE_RE.search(ss):
        return False
    return bool(_LISTING_LINE_RE.match(ss))


def summarize_maven_failure(
    stdout: str,
    stderr: str,
    *,
    test_class: str,
    placed_java_filename: str,
) -> Tuple[str, str, bool]:
    combined = (stdout or "") + "\n" + (stderr or "")
    combined = _ANSI_RE.sub("", combined)
    combined = _depath(combined)

    lines = _strip_noise_lines(combined.splitlines())
    filtered = [ln for ln in lines if not _is_listing_line(ln)]

    if _TIMEOUT_RE.search(combined):
        tail = filtered[-60:] if filtered else lines[-60:]
        return "timeout", "\n".join(tail)[:4000], False

    comp_loc_lines = [
        ln for ln in filtered
        if "[ERROR]" in ln and (_JAVA_LOC_RE.search(ln) or _JAVAC_ERR_RE.search(ln))
    ]
    if comp_loc_lines:
        first = comp_loc_lines[0]
        i0 = next((i for i, ln in enumerate(filtered) if ln == first), 0)
        block = filtered[max(0, i0 - 5): min(len(filtered), i0 + 40)]
        msg = "\n".join(block)[:4000]
        is_unrel = (test_class not in msg) and (placed_java_filename not in msg)
        return ("unrelated" if is_unrel else "compile"), msg, is_unrel

    comp_header = next((i for i, ln in enumerate(filtered) if _COMPILATION_HEADER_RE.search(ln)), None)
    if comp_header is not None:
        block: List[str] = []
        for ln in filtered[comp_header: comp_header + 160]:
            if "Re-run Maven using" in ln:
                break
            if "[ERROR]" in ln or _JAVA_LOC_RE.search(ln) or _JAVAC_ERR_RE.search(ln) or ln.strip().startswith(("symbol:", "location:")):
                block.append(ln)
        if not block:
            block = filtered[comp_header: comp_header + 80]
        msg = "\n".join(block[:80])[:4000]
        is_unrel = (test_class not in msg) and (placed_java_filename not in msg)
        return ("unrelated" if is_unrel else "compile"), msg, is_unrel

    if _SUREFIRE_FAIL_RE.search(combined):
        hit_idx = [i for i, ln in enumerate(filtered) if test_class in ln or placed_java_filename in ln]
        if hit_idx:
            i0 = hit_idx[0]
            block = filtered[max(0, i0 - 15): min(len(filtered), i0 + 80)]
            return "test", "\n".join(block)[:4000], False

        ex_lines = [ln for ln in filtered if _EXCEPTION_LINE_RE.search(ln) or ln.strip().startswith("at ")]
        tail = (ex_lines[-80:] if ex_lines else filtered[-80:] if filtered else lines[-80:])
        msg = "\n".join(tail)[:4000]
        is_unrel = (test_class not in msg) and (placed_java_filename not in msg)
        return ("unrelated" if is_unrel else "test"), msg, is_unrel

    err_lines = [ln for ln in filtered if "[ERROR]" in ln or _EXCEPTION_LINE_RE.search(ln)]
    if err_lines:
        tail = err_lines[-80:]
        msg = "\n".join(tail)[:4000]
        is_unrel = (test_class not in msg) and (placed_java_filename not in msg)
        return ("unrelated" if is_unrel else "other"), msg, is_unrel

    tail = filtered[-60:] if filtered else lines[-60:]
    msg = "\n".join(tail)[:4000]
    is_unrel = (test_class not in msg) and (placed_java_filename not in msg)
    return ("unrelated" if is_unrel else "other"), msg, is_unrel


def build_mvn_cmd(
    *,
    mvn_bin: str,
    test_class: str,
    mvn_append: str,
) -> List[str]:
    cmd = [mvn_bin, "test", f"-Dtest={test_class}"]
    if mvn_append.strip():
        cmd.extend(shlex.split(mvn_append))
    return cmd


def run_maven(project_root: Path, cmd: List[str], timeout_s: int) -> Tuple[int, str, str]:
    p = subprocess.run(
        cmd,
        cwd=str(project_root),
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        timeout=timeout_s,
    )
    return p.returncode, p.stdout or "", p.stderr or ""


# =========================
# Output records
# =========================

def append_jsonl(path: Path, obj: Dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("a", encoding="utf-8") as f:
        f.write(json.dumps(obj, ensure_ascii=False) + "\n")


# =========================
# Main loop
# =========================

def main() -> int:
    ap = argparse.ArgumentParser(description="Iterative LLM repair loop with mvn test validation")

    ap.add_argument("--project-root", required=True, type=Path, help="Maven project root (contains pom.xml)")
    ap.add_argument("--broken-map", required=True, type=Path, help="broken_tests_mapped.json")
    ap.add_argument("--broken-tests-dir", required=True, type=Path, help="dir containing broken .java tests (recursive)")
    ap.add_argument("--class-map", required=True, type=Path, help="classMapping.json")
    ap.add_argument("--class-info-dir", required=True, type=Path, help="class-info root")

    ap.add_argument("--model", required=True, help="OpenRouter model id (e.g., openai/gpt-5-mini)")
    ap.add_argument("--api-key", default=os.getenv("OPENROUTER_API_KEY", ""), help="or set OPENROUTER_API_KEY")
    ap.add_argument("--site-url", default=os.getenv("OPENROUTER_SITE_URL", ""), help="optional HTTP-Referer")
    ap.add_argument("--app-title", default=os.getenv("OPENROUTER_APP_TITLE", ""), help="optional X-Title")
    ap.add_argument("--temperature", type=float, default=0.0)
    ap.add_argument("--max-tokens", type=int, default=None)
    ap.add_argument("--sleep-s", type=float, default=0.0, help="sleep between LLM calls")

    ap.add_argument("--prompt-method", choices=["zero_shot", "few_shot", "cot"], default="zero_shot")

    ap.add_argument("--max-rounds", type=int, default=3, help="max repair rounds per test (including round 0)")
    ap.add_argument("--max-records", type=int, default=None, help="limit number of tests processed (debug)")

    ap.add_argument("--mvn-bin", default="mvn", help="maven executable")
    ap.add_argument("--mvn-append", default="", help="string appended to default mvn cmd, e.g. '-Drat.skip=true'")
    ap.add_argument("--mvn-timeout-s", type=int, default=600, help="timeout per mvn invocation")

    ap.add_argument("--out-dir", required=True, type=Path, help="output directory")
    ap.add_argument("--keep-passing-in-project", action="store_true", help="keep passing test file in src/test/java")
    ap.add_argument(
        "--stop-on-unrelated-failure",
        action="store_true",
        help="if mvn fails but error doesn't mention the placed test, stop looping this test",
    )

    args = ap.parse_args()

    if not args.api_key:
        print("ERROR: missing OpenRouter API key (set OPENROUTER_API_KEY or pass --api-key).", file=sys.stderr)
        return 2
    if not (args.project_root / "pom.xml").exists():
        print(f"ERROR: --project-root does not contain pom.xml: {args.project_root}", file=sys.stderr)
        return 2
    if not args.broken_tests_dir.exists():
        print(f"ERROR: --broken-tests-dir not found: {args.broken_tests_dir}", file=sys.stderr)
        return 2

    broken_obj = read_json(args.broken_map)
    record_ids = coerce_record_ids(broken_obj)
    if args.max_records is not None:
        record_ids = record_ids[: max(0, args.max_records)]

    class_map = load_class_mapping(args.class_map)
    java_index = index_java_files(args.broken_tests_dir)

    args.out_dir.mkdir(parents=True, exist_ok=True)
    records_jsonl = args.out_dir / "repair_records.jsonl"

    src_test_java = args.project_root / "src" / "test" / "java"

    for record_id in record_ids:
        meta = get_record_meta(broken_obj, record_id)

        # INITIAL ERROR FROM MAP
        initial_error_message = str(meta.get("error_message") or meta.get("errorMessage") or "")
        initial_error_type = str(meta.get("error_type") or meta.get("errorType") or "")

        try:
            original_unit_test, original_path = load_unit_test_from_dir(
                broken_tests_dir=args.broken_tests_dir,
                java_index=java_index,
                record_id=record_id,
            )
        except Exception as e:
            append_jsonl(records_jsonl, {
                "record_id": record_id,
                "round": None,
                "status": "skip_load_input",
                "llm_time_s": None,
                "input_tokens": 0,
                "output_tokens": 0,
                "reasoning_tokens": 0,
                "total_tokens": 0,
                "error_type": "other",
                "error_message": str(e)[:2000],
                "timestamp_utc": now_utc_iso(),
            })
            continue

        pkg = parse_java_package(original_unit_test)
        fn_class_name, method_name, fn_method_index_token, _variant = parse_filename_tokens(record_id)
        if not pkg or not fn_class_name or not method_name:
            append_jsonl(records_jsonl, {
                "record_id": record_id,
                "round": None,
                "status": "skip_bad_filename_or_package",
                "llm_time_s": None,
                "input_tokens": 0,
                "output_tokens": 0,
                "reasoning_tokens": 0,
                "total_tokens": 0,
                "error_type": "other",
                "error_message": f"pkg={pkg} cls={fn_class_name} method={method_name}",
                "timestamp_utc": now_utc_iso(),
            })
            continue

        class_number = meta.get("class_number") or meta.get("classNumber")
        method_number = meta.get("method_number") or meta.get("methodNumber")

        class_key: Optional[str] = None
        if class_number is not None:
            ck = f"class{int(class_number)}"
            if ck in class_map:
                class_key = ck
        if not class_key:
            class_key = infer_class_key(class_map=class_map, class_name=fn_class_name, package_name=pkg)
        if not class_key:
            append_jsonl(records_jsonl, {
                "record_id": record_id,
                "round": None,
                "status": "skip_no_class_key",
                "llm_time_s": None,
                "input_tokens": 0,
                "output_tokens": 0,
                "reasoning_tokens": 0,
                "total_tokens": 0,
                "error_type": "other",
                "error_message": f"Could not find class_key for {pkg}.{fn_class_name}",
                "timestamp_utc": now_utc_iso(),
            })
            continue

        entry = class_map.get(class_key)
        focal_pkg = str(entry.get("packageName") or "") if isinstance(entry, dict) else ""
        focal_cls = str(entry.get("className") or "") if isinstance(entry, dict) else ""
        if not focal_pkg or not focal_cls:
            append_jsonl(records_jsonl, {
                "record_id": record_id,
                "round": None,
                "status": "skip_bad_class_map_entry",
                "llm_time_s": None,
                "input_tokens": 0,
                "output_tokens": 0,
                "reasoning_tokens": 0,
                "total_tokens": 0,
                "error_type": "other",
                "error_message": f"class_key={class_key} entry_missing packageName/className",
                "timestamp_utc": now_utc_iso(),
            })
            continue

        method_file_stem: Optional[str] = None
        if method_number is not None:
            ms = str(int(method_number))
            if method_json_exists(args.class_info_dir, focal_pkg, focal_cls, ms):
                method_file_stem = ms
        if not method_file_stem:
            method_file_stem = infer_method_file_stem_numbered(
                class_info_dir=args.class_info_dir,
                package_name=focal_pkg,
                class_name=focal_cls,
                method_name=method_name,
                method_index_token=fn_method_index_token,
            )
        if not method_file_stem:
            append_jsonl(records_jsonl, {
                "record_id": record_id,
                "round": None,
                "status": "skip_no_method_json",
                "llm_time_s": None,
                "input_tokens": 0,
                "output_tokens": 0,
                "reasoning_tokens": 0,
                "total_tokens": 0,
                "error_type": "other",
                "error_message": f"Could not find method json for {focal_pkg}.{focal_cls}.{method_name}",
                "timestamp_utc": now_utc_iso(),
            })
            continue

        fixed_context = {
            "record_id": record_id,
            "input_unit_test_path": str(original_path),
            "package": pkg,
            "class_key": class_key,
            "focal_packageName": focal_pkg,
            "focal_className": focal_cls,
            "method_name": method_name,
            "method_index_token": fn_method_index_token,
            "method_file_stem": method_file_stem,
            "prompt_method": args.prompt_method,
            "model": args.model,
            "mvn_append": args.mvn_append,
            "initial_error_type": initial_error_type,
            "initial_error_message": initial_error_message,
        }

        current_unit_test = original_unit_test
        current_error_type = initial_error_type
        current_error_message = initial_error_message

        max_rounds = max(1, int(args.max_rounds))
        for round_num in range(0, max_rounds):
            try:
                ctx = RepairLoopContext(class_map_path=args.class_map, class_info_dir=args.class_info_dir)
                ctx.populate_all(
                    class_key=class_key,
                    method_file_stem=method_file_stem,
                    unit_test=current_unit_test,
                    error_message=current_error_message,
                    error_type=current_error_type,
                    prompt_method=args.prompt_method,  # type: ignore[arg-type]
                )
                prompt = ctx.render_prompt()
            except Exception as e:
                append_jsonl(records_jsonl, {
                    **fixed_context,
                    "round": round_num,
                    "status": "skip_prompt_construction",
                    "llm_time_s": None,
                    "input_tokens": 0,
                    "output_tokens": 0,
                    "reasoning_tokens": 0,
                    "total_tokens": 0,
                    "error_type": "other",
                    "error_message": str(e)[:2000],
                    "timestamp_utc": now_utc_iso(),
                })
                break

            llm_t0 = time.time()
            usage_out = {"input_tokens": 0, "output_tokens": 0, "reasoning_tokens": 0, "total_tokens": 0}
            try:
                llm_raw, usage_out = openrouter_chat(
                    api_key=args.api_key,
                    model=args.model,
                    prompt=prompt,
                    temperature=args.temperature,
                    max_tokens=args.max_tokens,
                    site_url=args.site_url or None,
                    app_title=args.app_title or None,
                )
            except Exception as e:
                llm_time_s = time.time() - llm_t0
                append_jsonl(records_jsonl, {
                    **fixed_context,
                    "round": round_num,
                    "status": "fail_llm_call",
                    "llm_time_s": round(llm_time_s, 6),
                    "input_tokens": usage_out["input_tokens"],
                    "output_tokens": usage_out["output_tokens"],
                    "reasoning_tokens": usage_out["reasoning_tokens"],
                    "total_tokens": usage_out["total_tokens"],
                    "error_type": "other",
                    "error_message": str(e)[:2000],
                    "timestamp_utc": now_utc_iso(),
                })
                break
            llm_time_s = time.time() - llm_t0

            generated_java = extract_java_from_llm_response(llm_raw)
            _out_pkg, out_cls = infer_pkg_and_public_class(generated_java)
            out_cls_name = out_cls or Path(record_id).stem

            per_round_dir = args.out_dir / safe_stem(Path(record_id).stem) / f"round_{round_num}"
            write_text(per_round_dir / "prompt.txt", prompt)
            write_text(per_round_dir / "llm_response.txt", llm_raw)
            write_text(per_round_dir / "usage.json", json.dumps(usage_out, indent=2))
            write_text(per_round_dir / f"{safe_stem(out_cls_name)}.java", generated_java.rstrip() + "\n")

            swap: Optional[TempFileSwap] = None
            placed_path: Optional[Path] = None
            normalized_java: Optional[str] = None
            cmd: List[str] = []

            try:
                placed_path, normalized_java, swap = place_test_in_project(
                    src_test_java=src_test_java,
                    java_src=generated_java,
                    fallback_package=pkg,
                    fallback_classname=out_cls_name,
                )

                test_class = placed_path.stem
                cmd = build_mvn_cmd(mvn_bin=args.mvn_bin, test_class=test_class, mvn_append=args.mvn_append)

                rc, out, err = run_maven(args.project_root, cmd, args.mvn_timeout_s)

                write_text(per_round_dir / "mvn_stdout.txt", out)
                write_text(per_round_dir / "mvn_stderr.txt", err)

                if rc == 0:
                    append_jsonl(records_jsonl, {
                        **fixed_context,
                        "round": round_num,
                        "status": "pass",
                        "llm_time_s": round(llm_time_s, 6),
                        "input_tokens": usage_out["input_tokens"],
                        "output_tokens": usage_out["output_tokens"],
                        "reasoning_tokens": usage_out["reasoning_tokens"],
                        "total_tokens": usage_out["total_tokens"],
                        "error_type": "",
                        "error_message": "",
                        "prompt": prompt,
                        "response": llm_raw,
                        "placed_test_path": str(placed_path),
                        "mvn_cmd": cmd,
                        "timestamp_utc": now_utc_iso(),
                    })
                    if not args.keep_passing_in_project and swap is not None:
                        swap.restore()
                    break

                err_type, err_msg, is_unrelated = summarize_maven_failure(
                    out,
                    err,
                    test_class=test_class,
                    placed_java_filename=placed_path.name,
                )

                status = "fail_unrelated" if is_unrelated else "fail"
                append_jsonl(records_jsonl, {
                    **fixed_context,
                    "round": round_num,
                    "status": status,
                    "llm_time_s": round(llm_time_s, 6),
                    "input_tokens": usage_out["input_tokens"],
                    "output_tokens": usage_out["output_tokens"],
                    "reasoning_tokens": usage_out["reasoning_tokens"],
                    "total_tokens": usage_out["total_tokens"],
                    "error_type": err_type,
                    "error_message": err_msg,
                    "prompt": prompt,
                    "response": llm_raw,
                    "placed_test_path": str(placed_path),
                    "mvn_cmd": cmd,
                    "timestamp_utc": now_utc_iso(),
                })

                if is_unrelated and args.stop_on_unrelated_failure:
                    break

                current_error_type = err_type
                current_error_message = err_msg
                current_unit_test = normalized_java or generated_java

            except subprocess.TimeoutExpired:
                write_text(per_round_dir / "mvn_stdout.txt", "")
                write_text(per_round_dir / "mvn_stderr.txt", "mvn test timed out\n")
                err_type = "timeout"
                err_msg = "mvn test timed out"
                append_jsonl(records_jsonl, {
                    **fixed_context,
                    "round": round_num,
                    "status": "fail",
                    "llm_time_s": round(llm_time_s, 6),
                    "input_tokens": usage_out["input_tokens"],
                    "output_tokens": usage_out["output_tokens"],
                    "reasoning_tokens": usage_out["reasoning_tokens"],
                    "total_tokens": usage_out["total_tokens"],
                    "error_type": err_type,
                    "error_message": err_msg,
                    "prompt": prompt,
                    "response": llm_raw,
                    "placed_test_path": str(placed_path) if placed_path else "",
                    "mvn_cmd": cmd,
                    "timestamp_utc": now_utc_iso(),
                })
                current_error_type = err_type
                current_error_message = err_msg
                current_unit_test = normalized_java or generated_java

            finally:
                if swap is not None:
                    swap.restore()

            if args.sleep_s > 0:
                time.sleep(args.sleep_s)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
