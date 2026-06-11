---
name: be-api-rules
description: >
  API 설계 및 구현 가이드라인. Spec-first 접근, 엔드포인트 설계, DTO 규칙, Controller 규칙, 유효성 검증을 포함한다.
  This skill should be used when creating or modifying APIs, DTOs, controllers, or API spec documents.
---

# API Rules

## Overview
API 설계부터 구현까지의 규칙을 정의한다. Spec-first 접근을 따르며, DTO/Controller/검증 규칙을 포함한다.

## 핵심 규칙
- Spec-first: API 스펙 문서를 먼저 작성하고 FE와 공유한 후 구현
- DTO에 `@Setter` 금지, `@AllArgsConstructor` 사용
- Controller에 비즈니스 로직 금지
- 유효성 검증에 `@Validated` 사용 (`@Valid` 아님)
- API 작성 시 RestDocs 테스트 필수

- 상세 규칙은 `references/api-rules-detail.md`를 확인한다.
