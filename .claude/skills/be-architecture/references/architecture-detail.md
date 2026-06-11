# Architecture Detail

## 레이어 구조

```
Controller → Facade → Service → QueryService/CommandService → Repository
                           ↘ Validator
```

## 각 레이어 책임과 어노테이션

### Controller
- 어노테이션: `@RestController`, `@RequestMapping`
- HTTP 라우팅만 담당, 비즈니스 로직 금지
- Facade를 주입받아 위임

### Facade
- 어노테이션: `@Service` 또는 `@Component` (기존 코드의 컨벤션을 따른다)
- 여러 도메인의 Service를 조합하는 **오케스트레이션** 계층
- 트랜잭션 범위 관리
- 비즈니스 로직을 직접 수행하지 않고 Service에 위임
- **조건 분기, 유효성 검증 등 비즈니스 로직을 Facade에 두지 않는다** — 반드시 Service에 위임
- 네이밍: `{Domain}Facade` (패키지명을 따른다)

### Service
- 어노테이션: `@Service`
- 해당 도메인의 **비즈니스 로직** 담당 (검증, 엔티티 생성, 상태 변경 등)
- 네이밍: `{Domain}Service` (패키지명을 따른다)
- QueryService/CommandService를 주입받아 영속성 조작을 위임
- 비즈니스 검증이 복잡하면 Validator를 주입받아 사용

### QueryService / CommandService
- 어노테이션: `@Service`
- **단순 영속성 조작만** 담당 (저장, 조회, 삭제)
- 비즈니스 로직을 포함하지 않는다
- 분리 기준: `{Entity}QueryService{Role}` + `{Entity}CommandService{Role}`
- QueryService: `@Transactional(readOnly = true)` 클래스 레벨
- CommandService: `@Transactional` 클래스 레벨

### Validator
- 어노테이션: `@Component`, `@RequiredArgsConstructor`
- 도메인별 유효성 검증 로직을 전담한다
- Service 또는 Facade에서 주입받아 사용한다
- Service에 검증 로직을 private 메서드로 두지 않고, 반드시 Validator 클래스로 분리한다
- **검증 로직은 해당 데이터를 소유한 도메인의 Validator에 위치**한다
  - 예: 초대 코드 사용 이력 검증 → `CodeUsageHistoryValidator` (code_usage_history 도메인)
  - 예: 컨설팅 요청 중복 검증 → `ConsultingRequestValidator` (consulting_request 도메인)
- 데이터 조회가 필요한 검증은 같은 도메인의 QueryService를 주입받아 처리한다
- **검증에 사용하는 상수(최대 개수, 길이 제한 등)는 `ValidationConstants`에 정의**한다 (Validator 내부 private static final 금지)
- 네이밍: `{Domain}Validator` (예: `ConsultingRequestValidator`)
- 위치: `module/{role}/{domain}/validator/`

### Repository
- JPA Repository 인터페이스
- Service에서 직접 참조

## 패키지 구조

```
module/{role}/{domain}/
├── controller/
├── facade/
├── service/
├── dto/
│   ├── request/
│   └── response/
├── entity/
├── repository/
└── validator/ (필요 시)
```

- `module/` 하위에 도메인 기반 구성
- 역할 구분: `admin/`, `director/`, `member/`

## 네이밍 규칙

- 클래스명은 **패키지명(도메인)을 따른다**
- Facade: `{Domain}Facade` (예: `consulting_request` 패키지 → `ConsultingRequestFacade`)
- Service: `{Domain}Service` (예: `consulting_request` 패키지 → `ConsultingRequestService`)
- 도메인과 무관한 기능 접미사를 붙이지 않는다 (예: `ConsultingEligibilityFacade` X → `ConsultingRequestFacade` O)

## 의존성 규칙

- 역방향 참조 금지: 하위 레이어가 상위 레이어를 참조하지 않는다
- Service가 다른 도메인의 Service를 직접 참조하지 않는다 (Facade에서 조합)
- Service는 같은 도메인의 QueryService/CommandService를 주입받아 사용한다

## 쿼리 작성 규칙

- JPQL을 우선 사용한다
- 필터링은 QueryService/Repository에서 처리, Service 레이어에서 하지 않는다

## 트랜잭션 관리

- Facade 레벨에서 트랜잭션 범위를 관리한다
- QueryService에 `@Transactional(readOnly = true)` 클래스 레벨
- CommandService에 `@Transactional` 클래스 레벨
- Service에 불필요한 `@Transactional(readOnly = true)`를 붙이지 않는다
