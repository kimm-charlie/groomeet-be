---
name: be-java-spring
description: >
  Java, Spring Boot, Lombok, JPA, Entity 코딩 표준. 트랜잭션, 예외 처리, DB 마이그레이션 규칙을 포함한다.
  This skill should be used when writing or modifying entities, JPA code, or DB migration files.
---

# Java / Spring

## Overview
Java, Spring Boot, Lombok, JPA 사용 시 따라야 할 코딩 표준을 정의한다.

## 핵심 규칙
- Lombok: `@RequiredArgsConstructor`, `@Builder`, `@Getter` 적극 사용
- 트랜잭션: 클래스 레벨 `readOnly = true`, 변경 메서드에 `@Transactional`
- 예외: `CustomRuntimeException` + Enum 기반 예외 코드
- Entity 수정 시 SQL 마이그레이션 파일 필수: `V{version}__{description}.sql`
- DB는 MySQL (MariaDB 아님)

- 상세 규칙은 `references/java-spring-detail.md`를 확인한다.
