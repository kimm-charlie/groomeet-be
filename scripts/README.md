# Scripts Directory

This directory contains build automation scripts for the MOTD project.

## Exception Documentation Generator

### Files
- `generate_exception_markdown.py` - Python script that generates markdown from exception enums
- `generate_exception_html.py` - Python script that generates HTML documentation from exception markdown
- `exception-template.html` - HTML template used for generating exception documentation

### How It Works

**Automatic (Recommended)**:
```bash
./gradlew clean build
```
The `generateExceptionDocs` task automatically runs during build and:
1. Generates `docs/exception-codes.md` from exception enums
2. Generates HTML to `build/docs/exception-codes.html`
3. `copyDocs` task copies it to `src/main/resources/static/docs/`
4. Available at `/docs/exception-codes.html` in your application

**Manual Execution**:
```bash
# From project root
python3 scripts/generate_exception_markdown.py
python3 scripts/generate_exception_html.py
```

### Workflow

```
exception enums  →  generate_exception_markdown.py  →  exception-codes.md  →  generate_exception_html.py  →  build/docs/exception-codes.html  →  static/docs/exception-codes.html
(source)               (generated)                       (source)                  (generated)                     (generated)                         (served)
```

### Requirements
- Python 3.6+
- Source files: `src/main/java/com/motd/be/exception/exceptions/*.java`

### Adding New Exceptions

1. Update your Exception enums in Java
2. Run `./gradlew build` or manually run the scripts
3. Markdown and HTML documentation are automatically generated

### Gradle Task Details

**Task Names**: `generateExceptionMarkdown`, `generateExceptionDocs`

**Dependencies**:
- `ensureDocsDir` (creates output directories)

**Integrated Into**:
- `build` task - runs automatically during full build
- `copyDocs` task - copies generated HTML to static resources

**Python Detection**:
The task automatically finds Python 3 in this order:
1. `python3.14`
2. `python3.13`
3. `python3`
4. `/opt/homebrew/bin/python3` (brew Python)
5. `/usr/bin/python3` (system Python)
