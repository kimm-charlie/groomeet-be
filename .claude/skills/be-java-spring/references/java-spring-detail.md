# Java / Spring Detail

## Lombok 사용

- `@RequiredArgsConstructor`: 생성자 주입에 사용
- `@Builder`: 빌더 패턴
- `@Getter`: getter 자동 생성
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: Entity용 보호된 기본 생성자
- `@NoArgsConstructor(access = AccessLevel.PRIVATE)`: DTO용 비공개 기본 생성자
- `@Slf4j`: 로깅

## 트랜잭션

- 클래스 레벨: `@Transactional(readOnly = true)` (QueryService)
- 메서드 레벨: `@Transactional` (변경 작업)
- 불필요한 트랜잭션 어노테이션을 붙이지 않는다

## 예외 처리

- `CustomRuntimeException`에 Enum 기반 예외 코드를 사용한다
- 예외 Enum은 `CustomException` 인터페이스를 구현한다
- 필드: `HttpStatus status`, `String message`, `String code`
- 구현 메서드: `getHttpStatus()`, `getErrorMessage()`, `getName()`, `getCode()`
- 예외 종류: `AuthException`, `MemberException`, `FileException` 등

### 예외 사용 예시
```java
throw new CustomRuntimeException(AuthException.UNAUTHORIZED);
```

## JPA Entity 규칙

### 어노테이션
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@DynamicInsert
@DynamicUpdate
```

### 필드 어노테이션
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`: auto-increment
- `@Column(nullable = false, length = X)`: DB 제약 조건
- `@Column(columnDefinition = "boolean default false")`: 기본값
- `@Enumerated(EnumType.STRING)`: Enum 저장
- `@OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "...")`: 관계 매핑
- `@Column(updatable = false)`: 불변 필드
- `@CreationTimestamp`: 자동 생성 시간

### Entity 규칙
- 필드와 어노테이션 사이에 빈 줄을 넣지 않는다
- protected 기본 생성자 사용
- update 메서드를 public으로 제공: `updateInfo(...)`
- 편의 메서드 제공: `getFormattedCreatedAt()`

## Entity 수정 시

- SQL 마이그레이션 파일을 반드시 추가한다
- 파일명: `V{version}__{description}.sql`
- Flyway 위치: `src/main/resources/db/migration/`

### 마이그레이션 예시
```sql
-- V119__add_forbidden_word_table.sql
CREATE TABLE forbidden_word (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   word VARCHAR(100) NOT NULL UNIQUE,
   is_active BOOLEAN DEFAULT TRUE,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## MySQL 주의사항

- DB는 MySQL을 사용한다 (MariaDB 아님)
- `DROP COLUMN IF EXISTS`는 MySQL에서 지원하지 않는다
- MySQL 문법에 맞게 작성한다
