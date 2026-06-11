#!/usr/bin/env python3
"""
Exception Codes Markdown Generator
=================================

이 스크립트는 exception enum 파일을 파싱하여 exception-codes.md 문서를 자동 생성합니다.

실행 방법:
---------
1. 수동 실행 (프로젝트 루트에서):
   $ python3 scripts/generate_exception_markdown.py

2. Gradle build 실행 (자동):
   $ ./gradlew clean build

   → generateExceptionMarkdown task가 자동으로 실행됩니다

3. 실행 조건:
   - Python 3.6 이상 필요
   - 예외 enum 파일이 src/main/java/com/motd/be/exception/exceptions/ 에 존재해야 함

사용 시나리오:
-------------
- 새로운 Exception enum 추가 후 문서 업데이트할 때
- 예외 코드/메시지 수정 후 문서 재생성할 때
"""

import re
from datetime import datetime
from pathlib import Path

STATUS_CODE_MAP = {
    "BAD_REQUEST": 400,
    "FORBIDDEN": 403,
    "NOT_FOUND": 404,
    "UNAUTHORIZED": 401,
    "INTERNAL_SERVER_ERROR": 500,
    "SERVICE_UNAVAILABLE": 503,
}


def remove_comments(text):
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    text = re.sub(r"//.*", "", text)
    return text


def unescape_java_string(value):
    value = value.replace("\\\\", "\\")
    value = value.replace("\\\"", "\"")
    value = value.replace("\\n", "\n")
    value = value.replace("\\r", "\r")
    value = value.replace("\\t", "\t")
    return value


def parse_string_literal(raw):
    raw = raw.strip()
    if len(raw) >= 2 and raw[0] == '"' and raw[-1] == '"':
        return unescape_java_string(raw[1:-1])
    return raw


def split_top_level(text, separator):
    parts = []
    buf = []
    depth = 0
    in_string = False
    escape = False

    for ch in text:
        if in_string:
            buf.append(ch)
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            continue

        if ch == '"':
            in_string = True
            buf.append(ch)
            continue

        if ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1

        if ch == separator and depth == 0:
            part = ''.join(buf).strip()
            if part:
                parts.append(part)
            buf = []
            continue

        buf.append(ch)

    last = ''.join(buf).strip()
    if last:
        parts.append(last)
    return parts


def extract_enum_block(text):
    match = re.search(r"public\s+enum\s+\w+", text)
    if not match:
        return ""

    start = text.find("{", match.end())
    if start == -1:
        return ""

    i = start + 1
    depth = 0
    in_string = False
    escape = False

    while i < len(text):
        ch = text[i]

        if in_string:
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            i += 1
            continue

        if ch == '"':
            in_string = True
        elif ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1
        elif ch == ';' and depth == 0:
            return text[start + 1 : i]

        i += 1

    return ""


def parse_enum_constants(block):
    entries = split_top_level(block, ',')
    constants = []

    for entry in entries:
        if not entry:
            continue
        name_end = entry.find('(')
        if name_end == -1:
            name = entry.strip()
            args = []
        else:
            name = entry[:name_end].strip()
            args_raw = entry[name_end + 1 :].strip()
            if args_raw.endswith(')'):
                args_raw = args_raw[:-1]
            args = [arg.strip() for arg in split_top_level(args_raw, ',')]
        if name:
            constants.append((name, args))

    return constants


def parse_field_order(text):
    fields = []
    for match in re.finditer(r"private\s+final\s+[\w<>?.]+\s+(\w+)\s*;", text):
        fields.append(match.group(1))
    return fields


def http_status_to_text(raw):
    raw = raw.strip()
    if raw.startswith("HttpStatus."):
        status_name = raw.split(".", 1)[1].strip()
    else:
        status_name = raw

    code = STATUS_CODE_MAP.get(status_name)
    if code is None:
        return f"UNKNOWN {status_name}"
    return f"{code} {status_name}"


def sanitize_cell(value):
    if value is None or value == "":
        return "-"
    text = str(value).replace("\n", "<br>")
    return text.replace("|", "\\|")


def parse_exception_file(path):
    raw_text = path.read_text(encoding="utf-8")
    text = remove_comments(raw_text)

    enum_match = re.search(r"public\s+enum\s+(\w+)", text)
    if not enum_match:
        return None

    enum_name = enum_match.group(1)
    block = extract_enum_block(text)
    constants = parse_enum_constants(block)
    field_order = parse_field_order(text)

    entries = []
    for name, args in constants:
        values = {}
        for idx, field in enumerate(field_order):
            if idx < len(args):
                values[field] = args[idx]

        status = None
        code = None
        message = None
        description = None

        for field, value in values.items():
            field_lower = field.lower()
            if "status" in field_lower:
                status = http_status_to_text(value)
            elif "code" in field_lower:
                code = parse_string_literal(value)
            elif "message" in field_lower:
                message = parse_string_literal(value)
            elif "description" in field_lower:
                description = parse_string_literal(value)

        entries.append(
            {
                "name": name,
                "status": status,
                "code": code,
                "message": message,
                "description": description,
            }
        )

    return {
        "enum": enum_name,
        "file": path.name,
        "entries": entries,
    }


def generate_markdown(exception_dir):
    sections = []

    for path in sorted(exception_dir.glob("*.java")):
        parsed = parse_exception_file(path)
        if not parsed:
            continue

        lines = []
        lines.append(f"## {parsed['enum']}")
        lines.append("")
        lines.append(f"Source file: `exceptions/{parsed['file']}`")
        lines.append("")
        lines.append("| Code | HTTP Status | Name | Message | Description |")
        lines.append("| --- | --- | --- | --- | --- |")

        for entry in parsed["entries"]:
            code = sanitize_cell(entry["code"])
            status = sanitize_cell(entry["status"])
            name = sanitize_cell(entry["name"])
            message = sanitize_cell(entry["message"])
            description = sanitize_cell(entry["description"])
            lines.append(f"| {code} | {status} | {name} | {message} | {description} |")

        sections.append("\n".join(lines))

    date_str = datetime.now().strftime("%Y-%m-%d")
    header = [
        "# Exception Codes",
        "",
        "Source: `src/main/java/com/motd/be/exception/exceptions`",
        f"Generated: {date_str}",
        "",
    ]

    return "\n\n".join(["\n".join(header)] + sections).rstrip() + "\n"


def write_if_changed(path, content):
    if path.exists():
        current = path.read_text(encoding="utf-8")
        if current == content:
            print("No changes in exception-codes.md")
            return

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    print(f"Updated {path}")


def main():
    project_root = Path(__file__).resolve().parent.parent
    exception_dir = project_root / "src" / "main" / "java" / "com" / "motd" / "be" / "exception" / "exceptions"
    output_file = project_root / "docs" / "exception-codes.md"

    if not exception_dir.exists():
        raise SystemExit(f"Exception directory not found: {exception_dir}")

    markdown = generate_markdown(exception_dir)
    write_if_changed(output_file, markdown)


if __name__ == "__main__":
    main()
