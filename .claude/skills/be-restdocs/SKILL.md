---
name: be-restdocs
description: >
  REST Documentation 작성 패턴. MockMvc, 필드 문서화, Asciidoc 파일 규칙을 포함한다.
  This skill should be used when writing RestDocs tests or creating API documentation.
---

# RestDocs

## Overview
REST API 문서화를 위한 테스트 패턴과 Asciidoc 파일 작성 규칙을 정의한다.

## 핵심 규칙
- `MockMvc` + `MockMvcRestDocumentationWrapper.document()` 사용
- `rest_docs/Utils.java` 헬퍼 필수 사용
- Asciidoc 위치: `src/docs/asciidoc/{role}/{domain}/`
- `include::` 방식만 사용 (`operation::` 금지)

- 상세 규칙은 `references/restdocs-detail.md`를 확인한다.
