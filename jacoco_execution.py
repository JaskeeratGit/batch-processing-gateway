from __future__ import annotations

import argparse
import json
import re
import shutil
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import List


PKG_RE = re.compile(r"^\s*package\s+([a-zA-Z_][\w\.]*)\s*;", re.MULTILINE)
PUBLIC_CLASS_RE = re.compile(r"^\s*public\s+(?:final\s+|abstract\s+)?class\s+([A-Za-z_]\w*)\b", re.MULTILINE)
ANY_CLASS_RE = re.compile(r"^\s*(?:public\s+)?(?:final\s+|abstract\s+)?class\s+([A-Za-z_]\w*)\b", re.MULTILINE)


@dataclass(frozen=True)
class JavaTestInfo:
    src_path: Path
    package: str          # "" if default package
    class_name: str
    fqn: str              # package.class_name or class_name
    rel_java_path: Path   # com/foo/BarTest.java or BarTest.java


def run(cmd: List[str], cwd: Path, *, check: bool = True) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, cwd=str(cwd), stdout=sys.stdout, stderr=sys.stderr, check=check)


def read_text(p: Path) -> str:
    return p.read_text(encoding="utf-8", errors="replace")


def parse_java_test_info(src_path: Path) -> JavaTestInfo:
    text = read_text(src_path)

    m_pkg = PKG_RE.search(text)
    pkg = m_pkg.group(1) if m_pkg else ""

    m_pub = PUBLIC_CLASS_RE.search(text)
    m_any = ANY_CLASS_RE.search(text)
    class_name = (m_pub.group(1) if m_pub else (m_any.group(1) if m_any else src_path.stem))

    fqn = f"{pkg}.{class_name}" if pkg else class_name
    rel_dir = Path(*pkg.split(".")) if pkg else Path()
    rel_java_path = rel_dir / f"{class_name}.java"

    return JavaTestInfo(
        src_path=src_path,
        package=pkg,
        class_name=class_name,
        fqn=fqn,
        rel_java_path=rel_java_path,
    )


def safe_slug(s: str) -> str:
    return re.sub(r"[^A-Za-z0-9_.-]+", "_", s)


def ensure_dir(p: Path) -> None:
    p.mkdir(parents=True, exist_ok=True)


def copy_into_test_dir(test: JavaTestInfo, test_src_root: Path) -> Path:
    dest = test_src_root / test.rel_java_path
    ensure_dir(dest.parent)
    shutil.copy2(test.src_path, dest)
    return dest


def cleanup_inserted_test(test: JavaTestInfo, test_src_root: Path, project_root: Path) -> None:
    inserted = test_src_root / test.rel_java_path
    if inserted.exists():
        inserted.unlink()

    test_classes_root = project_root / "target" / "test-classes"
    class_dir = test_classes_root / Path(*test.package.split(".")) if test.package else test_classes_root
    if class_dir.exists():
        for cls in class_dir.glob(f"{test.class_name}*.class"):
            try:
                cls.unlink()
            except OSError:
                pass


def jacoco_merge(jacococli_jar: Path, exec_files: List[Path], merged_exec: Path, project_root: Path) -> None:
    if not exec_files:
        raise RuntimeError("No JaCoCo .exec files were produced; nothing to merge.")
    ensure_dir(merged_exec.parent)
    cmd = ["java", "-jar", str(jacococli_jar), "merge", *map(str, exec_files), "--destfile", str(merged_exec)]
    run(cmd, cwd=project_root)


def jacoco_report(
    jacococli_jar: Path,
    merged_exec: Path,
    classes_dir: Path,
    sources_dir: Path,
    html_out: Path,
    project_root: Path,
) -> None:
    ensure_dir(html_out)
    cmd = [
        "java", "-jar", str(jacococli_jar), "report", str(merged_exec),
        "--classfiles", str(classes_dir),
        "--sourcefiles", str(sources_dir),
        "--html", str(html_out),
    ]
    run(cmd, cwd=project_root)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--project-root", required=True, type=Path, help="Root dir containing pom.xml")
    ap.add_argument("--tests", required=True, type=Path, help="JSON file: list of POSIX paths to .java tests")
    ap.add_argument("--test-src-root", default=Path("src/test/java"), type=Path, help="Relative to project-root")
    ap.add_argument("--mvn", default="mvn", help="Maven executable (e.g., mvn or ./mvnw)")
    ap.add_argument(
        "--mvn-args",
        action="append",
        default=[],
        help="Extra Maven args; repeatable. Example: --mvn-args -Dcheckstyle.skip=true",
    )
    ap.add_argument("--jacococli-jar", required=True, type=Path, help="Path to org.jacoco.cli-*-nodeps.jar")
    ap.add_argument("--runs-dir", default=Path("target/jacoco-runs"), type=Path, help="Relative to project-root")
    ap.add_argument("--merged-exec", default=Path("target/jacoco-merged.exec"), type=Path, help="Relative to project-root")
    ap.add_argument("--html-report", default=Path("target/jacoco-merged-report"), type=Path, help="Relative to project-root")
    ap.add_argument("--classes-dir", default=Path("target/classes"), type=Path, help="Relative to project-root")
    ap.add_argument("--sources-dir", default=Path("src/main/java"), type=Path, help="Relative to project-root")
    ap.add_argument("--skip-failing-tests", action="store_true", help="Continue even if a test fails to compile/run")
    args = ap.parse_args()

    project_root = args.project_root.resolve()
    tests_json = args.tests.resolve()
    test_src_root = (project_root / args.test_src_root).resolve()

    jacococli_jar = args.jacococli_jar.resolve()
    if not jacococli_jar.exists():
        raise FileNotFoundError(f"jacococli jar not found: {jacococli_jar}")

    runs_dir = (project_root / args.runs_dir).resolve()
    ensure_dir(runs_dir)

    merged_exec = (project_root / args.merged_exec).resolve()
    html_report = (project_root / args.html_report).resolve()
    classes_dir = (project_root / args.classes_dir).resolve()
    sources_dir = (project_root / args.sources_dir).resolve()

    test_paths_raw = json.loads(read_text(tests_json))
    if not isinstance(test_paths_raw, list):
        raise ValueError("tests JSON must be a list of POSIX paths (strings).")

    test_paths = [Path(p).expanduser().resolve() for p in test_paths_raw]
    exec_files: List[Path] = []

    for idx, src in enumerate(test_paths, start=1):
        if not src.exists():
            print(f"[SKIP] missing: {src}", file=sys.stderr)
            continue

        test = parse_java_test_info(src)
        inserted_path = copy_into_test_dir(test, test_src_root)

        # include idx to avoid collisions when same FQN appears multiple times in your JSON
        exec_out = runs_dir / f"{idx:04d}_{safe_slug(test.fqn)}.exec"

        try:
            cmd = [
                args.mvn,
                *args.mvn_args,
                "-Dtest=" + test.fqn,
                "test",
                "-Djacoco.destFile=" + str(exec_out),
                "-Djacoco.append=false",
            ]
            run(cmd, cwd=project_root)

            if exec_out.exists():
                exec_files.append(exec_out)
            else:
                print(f"[WARN] no exec produced for {test.fqn} (expected {exec_out})", file=sys.stderr)

        except subprocess.CalledProcessError:
            print(f"[FAIL] {test.fqn} (#{idx})", file=sys.stderr)
            if not args.skip_failing_tests:
                cleanup_inserted_test(test, test_src_root, project_root)
                raise
        finally:
            cleanup_inserted_test(test, test_src_root, project_root)

            # best-effort: remove empty dirs created under src/test/java
            try:
                d = inserted_path.parent
                while d != test_src_root and d.exists() and not any(d.iterdir()):
                    d.rmdir()
                    d = d.parent
            except OSError:
                pass

    jacoco_merge(jacococli_jar, exec_files, merged_exec, project_root)
    jacoco_report(jacococli_jar, merged_exec, classes_dir, sources_dir, html_report, project_root)

    print(f"Merged exec: {merged_exec}")
    print(f"HTML report: {html_report / 'index.html'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
