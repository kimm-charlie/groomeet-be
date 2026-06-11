---
name: be-architecture
description: >
  레이어 구조, 책임 분리, 의존성 규칙, 트랜잭션 관리 가이드라인.
  This skill should be used when creating or modifying services, facades, or when making architectural decisions about layer structure.
---

# Architecture

## Overview
레이어 구조와 각 레이어의 책임, 의존성 방향, 트랜잭션 관리 규칙을 정의한다.

## 핵심 규칙
- 레이어: Controller → Facade → Service → QueryService/CommandService → Repository
- Facade: 오케스트레이션 + 트랜잭션 관리 (다른 도메인 서비스 조합)
- Service: 도메인 비즈니스 로직 (검증, 엔티티 생성, 상태 변경)
- QueryService/CommandService: 단순 영속성 조작
- 역방향 참조 금지
- 클래스 네이밍은 패키지명(도메인)을 따른다 (예: `consulting_request` 패키지 → `ConsultingRequestFacade`)
- Service에 불필요한 `@Transactional(readOnly = true)` 금지

- 상세 규칙은 `references/architecture-detail.md`를 확인한다.
