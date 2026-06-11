---
name: be-code-editing
description: >
  기존 코드 수정 시 따라야 할 규칙. import 변경 금지, 스타일 보존, 주석, 정렬, Entity 변환 규칙을 포함한다.
  This skill should be used when modifying existing Java files or refactoring code.
---

# Code Editing

## Overview
기존 코드를 수정할 때 기존 스타일을 훼손하지 않기 위한 규칙을 정의한다.

## 핵심 규칙
- import 문의 순서/그룹핑/와일드카드 절대 수정 금지
- 기존 코드 스타일, 들여쓰기, 간격 유지
- 간결한 한국어 주석만 필요 시 작성
- Entity 변환은 DTO의 `toEntity()` 또는 Entity의 `from()`/`of()` 사용

- 상세 규칙은 `references/code-editing-detail.md`를 확인한다.
