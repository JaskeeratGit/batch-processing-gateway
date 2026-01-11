from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Dict, List, Literal, Optional


PromptMethod = Literal["zero_shot", "few_shot", "cot"]


@dataclass
class RepairLoopContext:
    # -------- config --------
    class_map_path: Path
    class_info_dir: Path

    # -------- run context --------
    unit_test: str = ""
    error_message: str = ""
    error_type: str = ""  # optional (for COT template)
    prompt_method: PromptMethod = "zero_shot"

    # -------- focal identifiers --------
    method_sig: str = ""
    class_name: str = ""
    focal_fqcn: str = ""  # package + className

    # -------- focal method info (explicit) --------
    fm_source_code: str = ""          # method-only source (preferred)
    fm_full_method_info: str = ""     # optional comment+method if you want it

    # -------- focal class info (explicit) --------
    focal_class_skeleton: str = ""    # imports + classSignature + compact fields + ctor stubs + method stubs
    focal_class_method_sigs: List[str] = field(default_factory=list)

    # -------- dependent info (explicit) --------
    dependent_method_sigs: Dict[str, List[str]] = field(default_factory=dict)        # fqcn -> [sig,...] (cleaned)
    dependent_class_skeletons: Dict[str, str] = field(default_factory=dict)          # fqcn -> skeleton (only)
    dependent_selected_method_sigs: Dict[str, List[str]] = field(default_factory=dict)  # fqcn -> dependent sigs

    # -------- internals --------
    _class_map: Dict[str, Any] = field(default_factory=dict, init=False, repr=False)

    # -------- regex/helpers --------
    _field_decl_re = re.compile(r"^(.+?)(=.+)?;$")
    _ws_re = re.compile(r"\s+")
    _angle_re = re.compile(r"[<>]")

    def __post_init__(self) -> None:
        self.class_map_path = Path(self.class_map_path)
        self.class_info_dir = Path(self.class_info_dir)
        self._class_map = self._read_json(self.class_map_path)

    # =========================
    # IO
    # =========================

    @staticmethod
    def _read_json(path: Path) -> Dict[str, Any]:
        with path.open("r", encoding="utf-8") as f:
            return json.load(f)

    # =========================
    # Setters
    # =========================

    def set_run_context(
        self,
        *,
        unit_test: Optional[str] = None,
        error_message: Optional[str] = None,
        error_type: Optional[str] = None,
    ) -> None:
        if unit_test is not None:
            self.unit_test = unit_test
        if error_message is not None:
            self.error_message = error_message
        if error_type is not None:
            self.error_type = error_type

    def set_prompt_method(self, prompt_method: PromptMethod) -> None:
        self.prompt_method = prompt_method

    # =========================
    # Path resolution
    # =========================

    def class_data_dir_from_key(self, class_key: str) -> Path:
        entry = self._class_map.get(class_key)
        if not entry:
            raise KeyError(f"class_key not found in class map: {class_key}")

        classname = entry.get("className")
        pkg = entry.get("packageName")
        if not classname or not pkg:
            raise ValueError(f"Missing className/packageName for class_key={class_key}")

        return self.class_info_dir.joinpath(*str(pkg).split("."), str(classname))

    # =========================
    # Population
    # =========================

    def populate_focal_from_files(self, *, class_key: str, method_file_stem: str) -> None:
        class_entry = self._class_map.get(class_key)
        if not class_entry:
            raise KeyError(f"class_key not found in class map: {class_key}")

        self.class_name = str(class_entry.get("className") or self.class_name)
        pkg = str(class_entry.get("packageName") or "")
        self.focal_fqcn = f"{pkg}.{self.class_name}" if pkg else self.class_name

        class_dir = self.class_data_dir_from_key(class_key)

        # ---- focal class.json ----
        class_json_path = class_dir / "class.json"
        if not class_json_path.exists():
            raise FileNotFoundError(f"Missing class.json: {class_json_path}")
        class_json = self._read_json(class_json_path)

        self.focal_class_method_sigs = list(class_json.get("methodsBrief") or [])
        self.focal_class_skeleton = self._build_class_skeleton(
            class_json=class_json,
            only_methods=None,
        )

        # ---- focal method json ----
        method_json_path = class_dir / f"{method_file_stem}.json"
        if not method_json_path.exists():
            raise FileNotFoundError(f"Missing method json: {method_json_path}")
        method_json = self._read_json(method_json_path)

        self.method_sig = str(method_json.get("methodSignature") or self.method_sig)

        self.fm_source_code = str(method_json.get("sourceCode") or "")
        self.fm_full_method_info = str(method_json.get("full_method_info") or "")
        if not self.fm_source_code and self.fm_full_method_info:
            self.fm_source_code = self.fm_full_method_info

        # ---- dependent methods ----
        raw_dep = method_json.get("dependentMethods") or {}
        dep_norm = self._normalize_dependent_methods(raw_dep)
        dep_clean = self._postprocess_deps(dep_norm)

        self.dependent_method_sigs = dep_clean
        self.dependent_selected_method_sigs = dict(dep_clean)

        # ---- dependent skeletons ----
        self.dependent_class_skeletons = self._load_dependent_class_skeletons(dep_clean)

    @staticmethod
    def _normalize_dependent_methods(dep: Any) -> Dict[str, List[str]]:
        if not isinstance(dep, dict):
            return {}
        out: Dict[str, List[str]] = {}
        for fqcn, sigs in dep.items():
            fqcn_s = str(fqcn)
            if sigs is None:
                out[fqcn_s] = []
            elif isinstance(sigs, list):
                out[fqcn_s] = [str(s) for s in sigs]
            else:
                out[fqcn_s] = [str(sigs)]
        return out

    def _postprocess_deps(self, deps: Dict[str, List[str]]) -> Dict[str, List[str]]:
        out: Dict[str, List[str]] = {}
        for fqcn, sigs in deps.items():
            if fqcn == self.focal_fqcn:
                continue
            if fqcn.startswith(("java.", "javax.", "sun.")):
                continue

            cleaned: List[str] = []
            seen: set[str] = set()
            for s in sigs or []:
                cs = self._clean_sig(s)
                if not cs or cs in seen:
                    continue
                seen.add(cs)
                cleaned.append(cs)

            if cleaned:
                out[fqcn] = cleaned
        return out

    def _clean_sig(self, sig: str) -> str:
        s = sig.strip()
        s = self._ws_re.sub(" ", s)
        s = self._angle_re.sub("", s)
        return s

    def _load_dependent_class_skeletons(self, deps: Dict[str, List[str]]) -> Dict[str, str]:
        out: Dict[str, str] = {}
        for fqcn, needed_sigs in deps.items():
            dep_class_json = self._try_read_class_json_for_fqcn(fqcn)
            if not dep_class_json:
                out[fqcn] = ""
                continue

            out[fqcn] = self._build_class_skeleton(
                class_json=dep_class_json,
                only_methods=set(needed_sigs) if needed_sigs else set(),
            )
        return out

    def _try_read_class_json_for_fqcn(self, fqcn: str) -> Optional[Dict[str, Any]]:
        p1 = self.class_info_dir.joinpath(*fqcn.split(".")) / "class.json"
        if p1.exists():
            return self._read_json(p1)

        if "." in fqcn:
            outer = fqcn.rsplit(".", 1)[0]
            p2 = self.class_info_dir.joinpath(*outer.split(".")) / "class.json"
            if p2.exists():
                return self._read_json(p2)

        return None

    def _compact_field(self, field_line: str, max_len: int = 140) -> str:
        s = " ".join(field_line.strip().split())
        m = self._field_decl_re.match(s)
        if m:
            decl = (m.group(1) or "").strip()
            has_init = bool(m.group(2))
            if has_init:
                return f"{decl} = ...;"
            return s if len(s) <= max_len else s[:max_len] + " ...;"
        if "=" in s:
            left = s.split("=", 1)[0].strip()
            return f"{left} = ...;"
        return s if len(s) <= max_len else s[:max_len] + " ...;"

    def _build_class_skeleton(self, *, class_json: Dict[str, Any], only_methods: Optional[set[str]]) -> str:
        imports: List[str] = list(class_json.get("imports") or [])
        class_sig: str = str(class_json.get("classSignature") or f"class {class_json.get('className','Unknown')}")
        fields_raw: List[str] = list(class_json.get("fields") or [])
        ctor_brief: List[str] = list(class_json.get("constructorBrief") or [])
        methods_brief: List[str] = list(class_json.get("methodsBrief") or [])

        fields = [self._compact_field(x) for x in fields_raw]

        if only_methods is not None:
            filtered: List[str] = []
            for mb in methods_brief:
                mb_s = mb.strip()
                for sig in only_methods:
                    name = sig.split("(", 1)[0].strip()
                    if not name:
                        continue
                    if f" {name}(" in mb_s or mb_s.startswith(name + "("):
                        filtered.append(mb)
                        break
            methods_brief = filtered

        lines: List[str] = []
        if imports:
            lines.extend(imports)
        lines.append(class_sig + " {")
        if fields:
            lines.extend(fields)
        if ctor_brief:
            lines.extend(ctor_brief)
        if methods_brief:
            lines.extend(methods_brief)
        lines.append("}")
        return "\n".join(lines)

    # =========================
    # Prompt helpers
    # =========================

    def _dependent_block_for_zero_shot(self) -> str:
        # Match the COT dependency presentation.
        return self._dependent_block_for_cot()

    def _dependent_block_for_few_shot(self) -> str:
        # Match the COT dependency presentation.
        return self._dependent_block_for_cot()

    def _dependent_block_for_cot(self) -> str:
        """
        Render dependency info in the same style used by the COT template:
          - For each dependent class: its skeleton
          - For each dependent class: the dependent method signatures (if any)
        """
        blocks: List[str] = []

        # class deps (skeletons)
        for fqcn, skel in self.dependent_class_skeletons.items():
            if not skel:
                continue
            blocks.append(
                f"The brief information of dependent class `{fqcn}` is\n"
                "```[java]\n"
                f"{skel}\n"
                "```"
            )

        # method deps (signatures)
        for fqcn, sigs in self.dependent_method_sigs.items():
            if not sigs:
                continue
            blocks.append(
                f"The brief information of dependent class `{fqcn}` is\n"
                "```[java]\n"
                + "\n".join(sigs)
                + "\n```"
            )

        return "\n\n".join(blocks)

    def _dependency_section_headered(self, dep_block: str) -> str:
        if not dep_block:
            return ""
        return (
            "\n\nTo help you correctly fix the unit test file, we provide the brief information about the dependency\n\n"
            f"{dep_block}\n"
        )

    # =========================
    # Prompt rendering
    # =========================

    def render_prompt(self) -> str:
        if self.prompt_method == "zero_shot":
            dep_block = self._dependent_block_for_zero_shot()
            dep_section = self._dependency_section_headered(dep_block)

            return (
                "I need you to fix an error in a unit test, an error occurred while compiling and executing\n\n"
                "The unit test is:\n\n"
                f"{self.unit_test}\n\n"
                "The error chatMessage is:\n\n"
                f"{self.error_message}\n\n"
                f"The unit test is testing the method `{self.method_sig}` in the class `{self.class_name}`.\n\n"
                "Focal method source code:\n\n"
                f"{self.fm_source_code}\n\n"
                "Focal class skeleton (imports + fields + method signatures):\n\n"
                f"{self.focal_class_skeleton}"
                f"{dep_section}\n"
                "Please fix the error and return the whole fixed unit test. "
                "You can use Junit 5 and Mockito 3. Adhere to Java 8 language style. No explanation is needed."
            )

        if self.prompt_method == "few_shot":
            dep_block = self._dependent_block_for_few_shot()
            dep_section = self._dependency_section_headered(dep_block)

            return (
                "I need you to fix an error in a unit test. An error occurred while compiling or executing.\n\n"
                "Below are a few examples of unit tests with their error messages and their correct fixes.\n\n"
                "========================\n"
                "EXAMPLE 1\n"
                "========================\n"
                "Original Unit Test:\n\n"
                "```java\n"
                "\"public class Options_getOptionGroup_9_0_Test {\n"
                "    static class OptionGroup {\n"
                "        private final String id;\n"
                "        OptionGroup(String id) {\n"
                "            this.id = id;\n"
                "        }\n"
                "        String getId() {\n"
                "            return id;\n"
                "        }\n"
                "    }\n"
                "    \n"
                "    testGetOptionGroupReturnsNullWhenMissing() throws Exception {\n"
                "    Options options = new Options();\n"
                "    Option opt = new Option(\"\"b\"\", \"\"desc\"\");\n"
                "    OptionGroup result = options.getOptionGroup(opt);\n"
                "    assertNull(result, \"\"Expected null when no OptionGroup is associated with the option key\"\");\n"
                "    \n"
                "}\n"
                "```\n\n"
                "Error chatMessage:\n"
                " Error in Options_getOptionGroup_9_0_Test: line 46 : incompatible types: org.apache.commons.cli.OptionGroup cannot be converted to org.apache.commons.cli.Options_getOptionGroup_9_0_Test.OptionGroup,\n"
                " Error in Options_getOptionGroup_9_0_Test: line 55 : incompatible types: org.apache.commons.cli.OptionGroup cannot be converted to org.apache.commons.cli.Options_getOptionGroup_9_0_Test.OptionGroup\n\n"
                "Fixed Unit Test:\n\n"
                "```java\n"
                "\"public class Options_getOptionGroup_9_0_Test {\n"
                "   \n"
                "   testGetOptionGroupReturnsNullWhenMissing() throws Exception {\n"
                "    Options options = new Options();\n"
                "    Option opt = new Option(\"\"b\"\", \"\"desc\"\");\n"
                "    OptionGroup result = options.getOptionGroup(opt);\n"
                "    assertNull(result, \"\"Expected null when no OptionGroup is associated with the option key\"\");\n"
                "}\n"
                "}\n"
                "\n"
                "```\n\n"
                "========================\n"
                "EXAMPLE 2\n"
                "========================\n"
                "Original Unit Test:\n\n"
                "```java\n"
                "public class CommandLine_getParsedOptionValues_46_0_Test {\n"
                "\n"
                "    @Test\n"
                "    void testNullOptionGroupReturnsDefault() throws Exception {\n"
                "        // Arrange\n"
                "        CommandLine cmd = new CommandLine();\n"
                "        Supplier<String[]> supplier = () -> new String[] { \"default1\", \"default2\" };\n"
                "        // Act\n"
                "        String[] result = cmd.getParsedOptionValues(null, supplier);\n"
                "        // Assert\n"
                "        assertArrayEquals(supplier.get(), result);\n"
                "    }\n"
                "}\n"
                "```\n\n"
                "Error chatMessage:\n"
                "Error in CommandLine_getParsedOptionValues_46_0_Test: line 30 : reference to getParsedOptionValues is ambiguous\n"
                "  both method <T>getParsedOptionValues(org.apache.commons.cli.OptionGroup,java.util.function.Supplier<T[]>) in org.apache.commons.cli.CommandLine and method <T>getParsedOptionValues(java.lang.String,java.util.function.Supplier<T[]>) in org.apache.commons.cli.CommandLine match\n\n"
                "Fixed Unit Test:\n\n"
                "```java\n"
                "public class CommandLine_getParsedOptionValues_46_0_Test {\n"
                "\n"
                "    @Test\n"
                "    void testNullOptionGroupReturnsDefault() throws Exception {\n"
                "        // Arrange\n"
                "        CommandLine cmd = new CommandLine();\n"
                "        Supplier<String[]> supplier = () -> new String[] { \"default1\", \"default2\" };\n"
                "        // Act\n"
                "        String[] result = cmd.getParsedOptionValues((OptionGroup) null, supplier);\n"
                "        // Assert\n"
                "        assertArrayEquals(supplier.get(), result);\n"
                "    }\n"
                "}\n"
                "```\n\n"
                "========================\n"

                "Now, fix the following.\n\n"
                "The unit test is:\n\n"
                f"{self.unit_test}\n\n"
                "The error chatMessage is:\n\n"
                f"{self.error_message}\n\n"
                f"The unit test is testing the method `{self.method_sig}` in the class `{self.class_name}`.\n\n"
                "Focal method source code:\n\n"
                f"{self.fm_source_code}\n\n"
                "Focal class skeleton (imports + fields + method signatures):\n\n"
                f"{self.focal_class_skeleton}"
                f"{dep_section}\n"
                "Please fix the error and return the whole fixed unit test. You can use Junit 5, and Mockito 3. "
                "Adhere to Java 8 language style. No explanation is needed.\n"
                "Return only the fixed unit test."
            )

        if self.prompt_method == "cot":
            dep_block = self._dependent_block_for_cot()
            dep_section = (dep_block + "\n\n") if dep_block else ""

            method_identifier = self.method_sig.split("(", 1)[0].strip() if self.method_sig else ""

            return (
                "Hello. You are a talented Java programmer. Here you're going to help the user fix the unit test file with error.\n"
                "Here is the information of the method-to-test:\n\n"
                f"The focal method is `{self.method_sig}` in the focal class `{self.class_name}`, and their information is\n"
                "```[java]\n"
                f"{self.fm_source_code}\n"
                "```\n\n"
                "To help you correctly fix the unit test file, we provide the brief information about the dependency\n\n"
                f"{dep_section}"
                "# Unit Test to Fix\n\n"
                "Here's the unit test that needs fixing.\n\n"
                "```java\n"
                f"{self.unit_test}\n"
                "```\n\n"
                "The error encountered when running this unit test is:\n\n"
                "```\n"
                f"{self.error_type}\n"
                f"{self.error_message}\n"
                "```\n\n"
                "# Procedures for Fixing the Unit Test:\n"
                "Let's proceed step by step:\n\n"
                "1. Pick out the statements that the errors occur\n"
                "2. Reason about the causes of the errors\n"
                "3. Reason about solutions on how to fix the errors\n"
                "3. Provide the complete fixed unit test, utilizing JUnit 5 and Mockito 3 if required.\n\n"
                "# Requirements and Considerations for the Unit Test Fix:\n"
                "- Ensure the unit tests are executable without compile errors, runtime errors, or timeouts.\n"
                "- Aim for high coverage scores, covering as many instructions and branches of the method under test as possible.\n"
                "- Avoid modifying the method under test.\n"
                "- Generate the entire unit test file, including package declaration and imports.\n"
                "- Ensure correct testing of the method under test:\n"
                f"    - The method under test is defined in {self.class_name}.\n"
                f"    - The method under test is {self.class_name}.{method_identifier}.\n"
                "- Utilize correct tools and adhere to Java 8 language style:\n"
                "    - You can use JUnit 5 and Mockito 3.\n"
                "    - The language style should follow Java 8 conventions.\n\n"
                "# Output Format\n"
                "To facilitate generating the desired unit test, follow these instructions:\n\n"
                "<<Generation Begin>>\n"
                "{complete unit test}\n"
                "<<Generation Over>>\n\n"
                "Please proceed with generating the fixed unit test.\n"
                "Ensure all generations are provided in a single response."
            )

        raise ValueError(f"Unknown prompt_method: {self.prompt_method}")

    # =========================
    # Convenience
    # =========================

    def populate_all(
        self,
        *,
        class_key: str,
        method_file_stem: str,
        unit_test: Optional[str] = None,
        error_message: Optional[str] = None,
        error_type: Optional[str] = None,
        prompt_method: Optional[PromptMethod] = None,
    ) -> None:
        if unit_test is not None or error_message is not None or error_type is not None:
            self.set_run_context(unit_test=unit_test, error_message=error_message, error_type=error_type)
        if prompt_method is not None:
            self.prompt_method = prompt_method
        self.populate_focal_from_files(class_key=class_key, method_file_stem=method_file_stem)
