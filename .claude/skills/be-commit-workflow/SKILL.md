---
name: be-commit-workflow
description: >
  커밋 메시지 규칙과 전략. 브랜치 번호 추출, 타입, 커밋 단위를 포함한다.
  This skill should be used when committing code changes or writing commit messages.
---

# Commit Workflow

## Overview
커밋 메시지 형식, 커밋 시점, 전략을 정의한다.

## 핵심 규칙
- 형식: `#{브랜치번호} {type}: {설명}`
- 브랜치번호는 현재 브랜치명에서 추출 (예: `feature/#532-...` → `#532`)
- type: `feature`, `refactor`, `chore`, `test`
- task 완료 + 빌드 성공 시 자동 커밋

- 상세 규칙은 `references/commit-workflow-detail.md`를 확인한다.
