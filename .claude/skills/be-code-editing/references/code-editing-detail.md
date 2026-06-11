# Code Editing Detail

## import 규칙

- import 문의 순서, 그룹핑을 불필요하게 정렬/변경하지 않는다
- 새로운 import는 필요한 위치에 추가한다
- 컴파일 에러 해결을 위한 import 변경은 허용한다
- 사용하지 않는 import를 무분별하게 자동 제거하지 않는다

## 스타일 보존

- 기존 코드의 스타일, 들여쓰기, 간격을 그대로 유지한다
- 주변 코드와 일관된 패턴을 따른다
- 불필요한 포맷팅 변경을 하지 않는다

## 주석

- 필요한 경우에만 간결한 한국어 주석을 작성한다
- 자명한 코드에 주석을 달지 않는다
- 기존 주석 스타일을 따른다

## 정렬

- 정렬이 필요한 경우 가능하면 DTO에서 처리한다
- Service 레이어에서 정렬하지 않는다

## JPQL 쿼리

- Enum 값을 JPQL에 문자열로 하드코딩하지 않는다 (예: `cs.status = 'APPROVED'`)
- 반드시 파라미터 바인딩으로 Enum 값을 넘긴다 (예: `cs.status = :status` + `@Param("status") SomeStatus status`)
- 호출하는 Service에서 Enum 상수를 전달한다

## Entity 변환

- DTO → Entity: DTO의 `toEntity()` 메서드 사용
- Entity → DTO: Entity의 `from()` 또는 `of()` static 메서드, 또는 DTO의 `from()` 사용
- 직접 생성자 호출보다 팩토리 메서드를 선호한다
