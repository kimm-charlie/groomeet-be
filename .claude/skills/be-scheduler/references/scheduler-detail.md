# Scheduler Detail

## 레이어 구조

```
Scheduler → Facade → Service → QueryService/CommandService → Repository
```

## 위치

- `com.motd.be.common.scheduler`

## 클래스 구조

```java
@Slf4j
@Component
@Profile({"prod-blue", "prod-green", "dev-blue", "dev-green"})
@RequiredArgsConstructor
public class ServiceEstimateReminderScheduler {

    private final ServiceEstimateReminderFacade facade;

    @Scheduled(cron = "${scheduler.service-estimate-reminder.cron}")
    public void sendReminder() {
        try {
            facade.sendReminder();
        } catch (Exception e) {
            log.error("스케줄러 실행 중 오류 발생", e);
        }
    }
}
```

## 네이밍 규칙

- 클래스: `{feature}Scheduler`
- 메서드: 동사+목적어 패턴 (예: `sendReminder`, `updateStatus`)

## 필수 요구사항

1. **Profile 어노테이션**: `@Profile({"prod-blue", "prod-green", "dev-blue", "dev-green"})` — 환경별 활성화
2. **외부 cron 설정**: `@Scheduled(cron = "${scheduler.key.cron}")` — properties에서 관리
3. **try-catch 예외 처리**: 스케줄러 실패가 전체 애플리케이션에 영향을 주지 않도록
4. **중복 실행 방지**: `{feature}SentAt` 필드를 사용하여 이미 처리된 항목 건너뛰기

## 테스트 케이스

- `be-test-rules` 스킬 참조
- 필수 테스트:
  - 성공 케이스
  - 중복 방지 케이스 (이미 처리된 항목)
  - 미충족 조건 케이스 (조건에 맞지 않아 건너뛰는 경우)
