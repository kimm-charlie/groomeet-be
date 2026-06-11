# API Rules Detail

## Spec-first 접근

- API 스펙 문서를 먼저 작성하고 프론트엔드와 공유한 후 구현을 시작한다.
- 스펙 문서 없이 바로 코드를 작성하지 않는다.

## 엔드포인트 규칙

- base path: `/api`
- 역할별 경로 (실제 컨벤션):
  - Admin: `/api/admin/` (단수)
  - Director: `/api/directors/` (복수)
  - Member: `/api/members/` (복수)
- 기존 네이밍 패턴을 따른다.

## 패키지/파일 구조

- 역할 기반 패턴: `*ForAdmin`, `*ForDirector`, `*ForPublic`
- 역할이 없는 경우 (member/public): 접미사 없이 사용
- 위치: `module/{role}/{domain}/controller/`, `module/{role}/{domain}/dto/`

## DTO 규칙

### 네이밍
- Response: `{Entity}{Action}ResponseFor{Role}` (예: `BannerFindAllResponseForDirector`)
- Request: `{Entity}{Action}RequestFor{Role}` (예: `ServiceEstimateSaveAdditionalRequestForDirector`)
- Public/Member: 역할 접미사 없이 (예: `BannerResponse`)

### 어노테이션
```java
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
```

- `@Setter` 금지
- `@NoArgsConstructor(access = AccessLevel.PRIVATE)` 사용 (직접 인스턴스화 방지)
- `@AllArgsConstructor`는 `@Builder`와 함께 사용

### Static Factory 메서드
- `from(Entity entity)` — Entity를 DTO로 변환
- `of(...)` — 여러 파라미터로 DTO 생성
- `toEntityName()` — DTO를 Entity로 변환

### DTO 재사용 및 구성
- 응답 DTO 안에 여러 static inner class를 중첩하지 않는다
- 다른 도메인에서도 재사용 가능한 DTO는 해당 도메인의 `dto/response/` 디렉토리에 독립 클래스로 존재한다 (예: `FileResponse`, `MemberResponse`)
- 기존 도메인 DTO를 먼저 확인하고, 있으면 재사용한다
- 해당 API에서만 사용하는 구조라면 해당 도메인 `dto/response/`에 별도 클래스로 생성한다

## Controller 규칙

### 어노테이션
```java
@RestController
@RequestMapping("/api/{role}/{resource}")
@RequiredArgsConstructor
```

### 보안
- `@PreAuthorize("hasAnyRole('DIRECTOR')")` — 역할 기반 접근 제어
- `@AuthenticationPrincipal` — 인증된 사용자 정보 주입

### 구조
- Facade를 주입받아 사용 (비즈니스 로직 금지)
- 반환 타입: `ResponseEntity<T>` with `HttpStatus`

## 날짜/시간 포맷

- 응답 DTO의 `createdAt` 등 날짜/시간 필드는 반드시 `DateFormatUtils`로 포맷하여 `String`으로 내려준다
- `LocalDateTime`, `LocalDate`를 그대로 응답에 넣지 않는다
- `DateFormatUtils`에 정의된 메서드만 사용:
  - `formatToDateString(LocalDate)` → `"yyyy.MM.dd"`
  - `formatToDateString(LocalDateTime)` → `"yyyy.MM.dd HH:mm"`
  - `formatToTimeString(LocalTime)` → `"HH:mm"`
  - `parseToLocalDateTime(String)` → `LocalDateTime`
- `Constants`에 정의된 상수를 사용한다.

## 유효성 검증

- Controller 파라미터에 `@Validated`를 사용한다 (`@Valid` 아님).
- DTO 내부 중첩 객체에도 `@Valid`를 사용하지 않는다. `@Validated`로 통일한다.
- 검증 메시지는 `ValidationMessages`에 정의된 상수를 사용한다.
- 검증 규칙은 `ValidationConstants`를 참조한다.

## DTO 필드 스타일

- 필드 사이에 빈 줄을 넣지 않는다. 어노테이션과 필드를 붙여서 연속으로 작성한다.
```java
// ✅ 올바른 예
@NotNull(message = SOME_REQUIRED)
private Boolean someField;
@NotNull(message = OTHER_REQUIRED)
private Boolean otherField;
@NotEmpty(message = LIST_REQUIRED)
private List<SomeRequest> items;

// ❌ 잘못된 예 — 필드 사이 빈 줄
@NotNull(message = SOME_REQUIRED)
private Boolean someField;

@NotNull(message = OTHER_REQUIRED)
private Boolean otherField;

@NotEmpty(message = LIST_REQUIRED)
private List<SomeRequest> items;
```
