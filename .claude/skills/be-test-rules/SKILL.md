---
name: be-test-rules
description: >
  테스트 작성 규칙. API 통합 테스트, RestDocs 테스트, Provider 패턴, 검증 방법을 포함한다.
  This skill should be used when writing any test code including integration tests and RestDocs tests.
---

# Test Rules

## Overview
API 통합 테스트와 RestDocs 테스트의 작성 규칙, Provider 패턴, 검증 방법을 정의한다.

## 핵심 규칙
- 모든 API에 통합 테스트 + RestDocs 테스트 필수
- Given-When-Then 패턴 사용
- Provider 패턴: `ProviderForAdmin`, `Provider` (Repository 직접 주입 금지)
- Enum 값 사용, 하드코딩 금지
- 최소 성공/실패/경계 조건 케이스

- 상세 규칙은 `references/test-rules-detail.md`를 확인한다.
