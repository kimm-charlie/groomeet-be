# Test Rules Detail

## 테스트 유형

### API 통합 테스트
- 어노테이션: `@ControllerIntegrationTest`
- 상속: `BaseIntegrationTest`
- 실제 DB 사용, 전체 컨텍스트 로드
- Provider 패턴으로 테스트 데이터 생성

### RestDocs 테스트
- 어노테이션: `@RestDocsTest`
- 상속: `BaseRestDocsTest`
- `@MockitoBean`으로 Facade 모킹
- API 문서화 생성

**모든 API에 두 유형 모두 필수**

## DisplayName 규칙

- 형식: `@DisplayName("{API 기능}가 가능하다 (특이케이스)")`
- 같은 API의 테스트는 **앞부분(기능 설명)이 동일**해야 한다
- 특이케이스는 괄호 안에 간결하게 표현한다
- 메서드명은 `@DisplayName`과 동일한 내용을 `_`로 구성하되 괄호는 제외한다

```java
// ✅ 올바른 예 — 같은 API, 앞부분 통일
@Test
@DisplayName("포트폴리오 조회가 가능하다 (아무것도 없는 경우)")
void 포트폴리오_조회가_가능하다_아무것도_없는_경우() { ... }

@Test
@DisplayName("포트폴리오 조회가 가능하다 (3건인 경우)")
void 포트폴리오_조회가_가능하다_3건인_경우() { ... }

@Test
@DisplayName("포트폴리오 조회가 가능하다 (권한 없음)")
void 포트폴리오_조회가_가능하다_권한_없음() { ... }

// ❌ 잘못된 예 — 앞부분이 제각각
@Test
void 포트폴리오_조회_성공() { ... }

@Test
void 빈_포트폴리오_목록_반환() { ... }
```

## 테스트 구조 (Given-When-Then)

응답 바디 검증은 **MvcResult → DTO 역직렬화** 방식을 사용한다.
`jsonPath`는 필드명 변경 시 컴파일 오류가 나지 않아 변경에 취약하므로 사용하지 않는다.
예외 응답 검증(status/message/code)은 예외이며 `jsonPath`를 사용한다.

```java
@Test
void 테스트_이름() throws Exception {
    // given
    var request = ...;

    // when
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/..."))
        .andExpect(status().isOk())
        .andReturn();

    entityManager.flush();
    entityManager.clear();

    // then
    SomeResponse response = objectMapper.readValue(
        result.getResponse().getContentAsString(), SomeResponse.class);
    assertThat(response.getSomeField()).isEqualTo(expectedValue);
}
```

## Provider 패턴

### 역할별 Provider
- `ProviderForAdmin`: 관리자 테스트 데이터
- `Provider`: 일반 테스트 데이터

### 사용법
```java
// BaseIntegrationTest에서 Provider 주입
bannerProvider.save(banner);
memberProvider.save(member);
```

### 금지사항
- Repository를 직접 주입하지 않는다
- Provider를 통해서만 테스트 데이터를 생성한다

## Mock 검증

```java
// verify 패턴
verify(facade).findAll();
verify(facade, times(1)).save(any());
verify(facade, never()).delete(any());

// ArgumentCaptor 패턴
ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
verify(facade).save(captor.capture());
assertThat(captor.getValue().getName()).isEqualTo("expected");
```

## Enum 사용

- 하드코딩된 문자열 대신 Enum 값을 사용한다
- 테스트에서도 동일한 Enum을 참조한다

```java
// ❌ 잘못된 예
.andExpect(jsonPath("$.status").value("ACTIVE"));

// ✅ 올바른 예
.andExpect(jsonPath("$.status").value(Status.ACTIVE.name()));
```

## 예외 검증

- status, message, code를 **모두** 검증한다
- Constants와 Exception Enum의 getter를 사용한다

```java
// 예외 검증 예시
.andExpect(status().isUnauthorized())
.andExpect(jsonPath("$.message").value(AuthException.UNAUTHORIZED.getErrorMessage()))
.andExpect(jsonPath("$.code").value(AuthException.UNAUTHORIZED.getCode()));
```

## 필수 테스트 케이스

- 성공 케이스: 정상 동작 확인
- 실패 케이스: 예외 발생 및 올바른 에러 응답 확인
- 경계 조건: 빈 리스트, null 값, 최대/최소 값 등
