"""
test_runner.py

Run from project root.

For each *.java file under an input directory:
  1) Determine package -> destination under src/test/java (or --test-root)
  2) Copy test into project
  3) Run tests (optionally targeted)
  4) If pass -> copy test + log into verified_tests/<package-path>/
     else     -> copy test + log into broken_tests/<package-path>/
  5) Restore src/test/java from verified_tests after each file (canonical set)

Also appends a JSONL record per file to verify_results.jsonl.
"""

from __future__ import annotations

import argparse
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
from typing import Optional, Tuple


PACKAGE_RE = re.compile(r'^\s*package\s+([a-zA-Z_][\w\.]*)\s*;\s*$', re.MULTILINE)
PUBLIC_CLASS_RE = re.compile(r'^\s*public\s+(?:final\s+|abstract\s+)?class\s+([A-Za-z_]\w*)\b', re.MULTILINE)
ANY_CLASS_RE = re.compile(r'^\s*(?:public\s+)?(?:final\s+|abstract\s+)?class\s+([A-Za-z_]\w*)\b', re.MULTILINE)


@dataclass(frozen=True)
class JavaInfo:
    package: Optional[str]
    class_name: Optional[str]
    rel_dir: Path
    file_name: str


def read_text(p: Path) -> str:
    return p.read_text(encoding="utf-8", errors="replace")


def parse_java_info(java_file: Path, rename_to_class: bool) -> JavaInfo:
    txt = read_text(java_file)

    m_pkg = PACKAGE_RE.search(txt)
    pkg = m_pkg.group(1) if m_pkg else None
    rel_dir = Path(*pkg.split(".")) if pkg else Path()

    m_cls = PUBLIC_CLASS_RE.search(txt) or ANY_CLASS_RE.search(txt)
    cls = m_cls.group(1) if m_cls else None

    if rename_to_class and cls:
        file_name = f"{cls}.java"
    else:
        file_name = java_file.name

    return JavaInfo(package=pkg, class_name=cls, rel_dir=rel_dir, file_name=file_name)


def ensure_dir(p: Path) -> None:
    p.mkdir(parents=True, exist_ok=True)


def merge_copytree(src: Path, dst: Path) -> None:
    if not src.exists():
        return
    for root, dirs, files in os.walk(src):
        root_p = Path(root)
        rel = root_p.relative_to(src)
        target_root = dst / rel
        ensure_dir(target_root)

        for d in dirs:
            ensure_dir(target_root / d)

        for f in files:
            s = root_p / f
            t = target_root / f
            if not t.exists():
                shutil.copy2(s, t)
            else:
                try:
                    if s.stat().st_mtime > t.stat().st_mtime:
                        shutil.copy2(s, t)
                except OSError:
                    shutil.copy2(s, t)


def wipe_dir(p: Path) -> None:
    if p.exists():
        shutil.rmtree(p)


def detect_default_cmd(project_root: Path) -> Tuple[list[str], str]:
    mvnw = project_root / "mvnw"
    gradlew = project_root / "gradlew"
    pom = project_root / "pom.xml"

    if mvnw.exists():
        return [str(mvnw), "-q", "test"], "maven"
    if pom.exists():
        return ["mvn", "-q", "test"], "maven"
    if gradlew.exists():
        return [str(gradlew), "test"], "gradle"

    raise SystemExit("Could not detect build tool. Provide --cmd explicitly.")


def build_targeted_cmd(base_cmd: list[str], tool: str, class_name: Optional[str]) -> list[str]:
    if not class_name:
        return base_cmd
    if tool == "maven":
        return base_cmd + [f"-Dtest={class_name}"]
    if tool == "gradle":
        return base_cmd + ["--tests", f"*{class_name}"]
    return base_cmd


def run_cmd(cmd: list[str], cwd: Path) -> Tuple[int, str]:
    proc = subprocess.run(
        cmd,
        cwd=str(cwd),
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        check=False,
    )
    return proc.returncode, proc.stdout


def write_log(log_text: str, dest_dir: Path, base_name: str) -> Path:
    ensure_dir(dest_dir)
    log_path = dest_dir / f"{base_name}.log"
    log_path.write_text(log_text, encoding="utf-8", errors="replace")
    return log_path


def append_jsonl(entry: dict, path: Path) -> None:
    ensure_dir(path.parent)
    with path.open("a", encoding="utf-8") as f:
        f.write(json.dumps(entry, ensure_ascii=False) + "\n")


def iter_java_files(input_dir: Path, recursive: bool) -> list[Path]:
    if recursive:
        return sorted([p for p in input_dir.rglob("*.java") if p.is_file()])
    return sorted([p for p in input_dir.glob("*.java") if p.is_file()])


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("input_dir", help="Directory containing Java test files to verify")
    ap.add_argument("--recursive", action="store_true", help="Recurse into subdirectories")
    ap.add_argument("--test-root", default="src/test/java")
    ap.add_argument("--verified-dir", default="verified_tests")
    ap.add_argument("--broken-dir", default="broken_tests")
    ap.add_argument("--cmd", default=None)
    ap.add_argument("--targeted", action="store_true")
    ap.add_argument("--rename-to-class", action="store_true")
    ap.add_argument("--stop-on-first-fail", action="store_true")
    args = ap.parse_args()

    project_root = Path.cwd()
    input_dir = Path(args.input_dir).expanduser().resolve()
    if not input_dir.exists() or not input_dir.is_dir():
        raise SystemExit(f"Input directory not found or not a directory: {input_dir}")

    test_root = (project_root / args.test_root).resolve()
    verified_dir = (project_root / args.verified_dir).resolve()
    broken_dir = (project_root / args.broken_dir).resolve()

    ensure_dir(test_root)
    ensure_dir(verified_dir)
    ensure_dir(broken_dir)

    # Merge-backup current test_root into verified_tests (as requested).
    merge_copytree(test_root, verified_dir)

    # Temporary safety backup
    tmp_backup = project_root / f".tmp_test_backup_{int(time.time())}"
    wipe_dir(tmp_backup)
    merge_copytree(test_root, tmp_backup)

    # Determine command once.
    if args.cmd:
        base_cmd = shlex.split(args.cmd)
        tool = "custom"
    else:
        base_cmd, tool = detect_default_cmd(project_root)

    files = iter_java_files(input_dir, args.recursive)
    if not files:
        print("No .java files found.", file=sys.stderr)
        return 2

    passed_ct = 0
    failed_ct = 0

    for java_file in files:
        java_info = parse_java_info(java_file, rename_to_class=args.rename_to_class)

        dest_in_project = test_root / java_info.rel_dir / java_info.file_name
        ensure_dir(dest_in_project.parent)

        # Put file into project test directory.
        shutil.copy2(java_file, dest_in_project)

        cmd = base_cmd
        if args.targeted:
            cmd = build_targeted_cmd(base_cmd, tool, java_info.class_name)

        rc, out = run_cmd(cmd, project_root)
        passed = (rc == 0)

        stamp = time.strftime("%Y%m%d_%H%M%S")
        pkg_path = java_info.rel_dir
        base_name = f"{java_info.class_name or dest_in_project.stem}_{stamp}"

        bucket = (verified_dir if passed else broken_dir) / pkg_path
        ensure_dir(bucket)

        stored_test = bucket / java_info.file_name
        shutil.copy2(dest_in_project, stored_test)
        log_path = write_log(out, bucket / "_logs", base_name)

        record = {
            "timestamp": stamp,
            "input_file": str(java_file),
            "package": java_info.package,
            "class_name": java_info.class_name,
            "project_dest": str(dest_in_project.relative_to(project_root)),
            "command": cmd,
            "returncode": rc,
            "passed": passed,
            "stored_test": str(stored_test.relative_to(project_root)),
            "log": str(log_path.relative_to(project_root)),
        }
        append_jsonl(record, project_root / "verify_results.jsonl")

        # Restore project tests from verified_tests (canonical) after each file.
        try:
            wipe_dir(test_root)
            ensure_dir(test_root)
            merge_copytree(verified_dir, test_root)
        except Exception as e:
            wipe_dir(test_root)
            ensure_dir(test_root)
            merge_copytree(tmp_backup, test_root)
            print(f"[WARN] Restore from verified_tests failed ({e}); restored from temp backup.", file=sys.stderr)

        if passed:
            passed_ct += 1
            print(f"PASS: {java_file}")
        else:
            failed_ct += 1
            print(f"FAIL: {java_file}")
            if args.stop_on_first_fail:
                break

    # Cleanup temp backup.
    wipe_dir(tmp_backup)

    print(f"\nSummary: passed={passed_ct}, failed={failed_ct}, total={passed_ct + failed_ct}")
    return 0 if failed_ct == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
