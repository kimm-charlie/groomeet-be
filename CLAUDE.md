# AI 코딩 가이드라인

> AGENTS.md는 이 파일의 심링크입니다. CLAUDE.md만 수정하세요.

---

## 필수 Workflow

코드 변경 요청 시 반드시 다음 순서를 따른다:

1. **스킬 호출** — 아래 매핑 테이블에서 해당 스킬을 호출한다
2. **적용 규칙 설명** — 어떤 규칙을 적용할지 간단히 설명한다
3. **코드 작성** — 규칙을 따라 코드를 작성한다
4. **테스트/RestDocs 반영** — 아래 기준에 따라 반드시 포함한다:
   - API 추가/수정 시 → 테스트 + RestDocs 신규 작성 (`/be-test-rules`, `/be-restdocs` 스킬 따름)
   - 리팩토링 시 → 영향받는 기존 테스트/RestDocs가 있으면 반드시 함께 수정
5. **빌드 확인** — 기능 구현 완료 시 `./gradlew clean build`로 검증한다

스킬을 읽지 않고 코드를 작성하지 않는다.

---

## 작업별 스킬 매핑

| 작업 유형 | 호출 스킬 |
|----------|----------|
| API 추가/수정, DTO, Controller | `/be-api-rules` → 완료 후 `/be-test-rules` + `/be-restdocs` 필수 |
| 레이어링, Service, Facade | `/be-architecture` |
| 테스트 작성 | `/be-test-rules` |
| RestDocs 작성 | `/be-restdocs` |
| 기존 코드 수정, 리팩토링 | `/be-code-editing` → 영향받는 테스트/RestDocs 있으면 `/be-test-rules` + `/be-restdocs` 함께 호출 |
| 스케줄러 | `/be-scheduler` |
| Entity, JPA, DB 마이그레이션 | `/be-java-spring` |
| 프로젝트 구조, 빌드 명령 | `/be-project-structure` |
| API 변경 후 FE 전달 | `/be-fe-handoff` |
| 커밋 | `/be-commit-workflow` |
| 코드 리뷰 (수정 없이 점검) | `/be-code-review` |

작업이 여러 유형에 해당하면 관련 스킬을 모두 호출한다.
규칙이 충돌하면 더 구체적인 스킬이 우선이며, 모호하면 질문한다.

---

## 빌드 확인

모든 기능 구현 완료 후:

```bash
./gradlew clean build
```

빌드가 성공할 때까지 작업이 완료된 것이 아니다.
