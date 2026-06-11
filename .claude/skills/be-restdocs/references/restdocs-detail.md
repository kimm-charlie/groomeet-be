# RestDocs Detail

## 테스트 패턴

```java
@RestDocsTest
public class BannerForDirectorRestDocsTest extends BaseRestDocsTest {

    @MockitoBean
    private BannerFacadeForDirector facade;

    @Test
    void 배너_전체_조회() throws Exception {
        // given
        given(facade.findAll()).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/directors/banners")
                .cookie(...))
            .andExpect(status().isOk())
            .andDo(document("operation-id",
                getRequestPreProcessor(),
                getResponsePreProcessor(),
                requestCookies(...),
                responseFields(...),
                resource(builder()
                    .tag("🎨 Category")
                    .summary("Summary")
                    .description("Description")
                    .build())
            ));
    }
}
```

## 핵심 구성요소

### 어노테이션
- `@RestDocsTest`: 커스텀 테스트 어노테이션
- `BaseRestDocsTest` 상속 필수

### MockMvc 설정
- `MockMvcRestDocumentationWrapper.document()` 사용
- `getRequestPreProcessor()` / `getResponsePreProcessor()` 헬퍼 사용

### 필드 문서화
- `requestFields(...)`: 요청 바디 필드
- `responseFields(...)`: 응답 바디 필드
- `pathParameters(...)`: URL 경로 파라미터
- `queryParameters(...)`: 쿼리 스트링 파라미터
- `requestHeaders(...)`: 요청 헤더
- `requestCookies(...)`: 쿠키

### 필드 타입
- `JsonFieldType.STRING`, `NUMBER`, `BOOLEAN`, `ARRAY`, `OBJECT`, `NULL`

## Utils Helper (필수)

- `rest_docs/Utils.java` 헬퍼를 반드시 사용한다
- 날짜/시간/Enum 필드에 `.attributes()` 활용
- 커스텀 포맷 지정 시 Utils 메서드 사용

## Resource 문서화

```java
resource(ResourceSnippetParameters.builder()
    .tag("태그명")
    .summary("API 요약")
    .description("API 설명")
    .build())
```

## Asciidoc 파일

- 위치: `src/docs/asciidoc/{role}/{domain}/`
- `include::` 방식만 사용한다 (`operation::` 금지)
- Admin 템플릿: request, cookies, parameters, response, field descriptions 섹션 포함
- **도메인 adoc 파일 생성 후 반드시 index 파일에 등록한다**
  - member API → `src/docs/asciidoc/index-member.adoc`
  - director API → `src/docs/asciidoc/index-director.adoc`
  - admin API → `src/docs/asciidoc/index-admin.adoc`
