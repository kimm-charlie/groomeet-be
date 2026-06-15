# Groomeet — 뷰티 디렉터 매칭 플랫폼 (Backend)

뷰티 디렉터(헤어·메이크업 전문가)와 소비자를 매칭하는 플랫폼 **Groomeet**의 백엔드입니다.
실시간 채팅 · 견적 · 위치 기반 매칭 · LLM 기반 추천을 제공합니다.

> 서비스는 출시 전 단계에서 정리되었고, 이 레포는 포트폴리오 공개용 스냅샷입니다.
> 각 설계의 배경·대안·검증은 deep-dive 자료에 정리했고, 이 README는 **코드의 어디에 있는지**만 안내합니다.

## 기술 스택

Java 17 · Spring Boot 3.5 · MySQL · Redis · WebSocket/STOMP · JPA/QueryDSL · AWS S3 · Firebase

## 핵심 설계 (코드 맵)

### 1. LLM 기반 디렉터 추천 / 요청서 생성

AI 응답을 구조화 JSON으로 강제·파싱 + 멀티턴 대화(텍스트+이미지) 영속으로 백엔드가 추천 흐름을 제어. 정보가 부족하면 추가 질문, 충분하면 디렉터 추천을 반환하는 분기 구조. 제공자는 `AiChatProvider`로 추상화해 모델 교체에 대비.

| 포인트 | 코드 |
|--------|------|
| 대화방/메시지 영속 + 추천·요청서 파이프라인 | `module/member/prompt` |
| AI 제공자 추상화 + 구현 | `shared/ai/provider` (`AiChatProvider`/`SionicAiChatProvider`) |
| 프롬프트 구성·추천 결과 파싱 | `shared/ai/builder`, `shared/ai/dto` |

### 2. WebSocket/STOMP 실시간 채팅 + 다중 서버 확장

`SimpleBroker`는 단일 서버 안에서만 동작하는 한계 → 기존 Redis Pub/Sub으로 서버 간 메시지 전파(새 인프라 추가 없이). 연결 생존은 STOMP heartbeat(5초) + 스케줄러 기반 Redis 세션 TTL(20초) 갱신 2층으로 관리해, 서버가 비정상 종료돼도 좀비 세션이 자동 정리됨.

흐름: `사용자 → STOMP → 서버 → PUBLISH chat:message → Redis → 전 서버 SUBSCRIBE → 각 서버가 로컬 구독자에게 전달`

| 포인트 | 코드 |
|--------|------|
| STOMP/브로커 설정 | `common/config/WebSocketConfigurer` |
| Redis Pub/Sub 발행·구독 | `redis/domain/config/RedisPubSubConfig`, `redis/domain/brocker/ChatMessageSubscriber` |
| 세션·구독 TTL 관리 | `common/manager/WebSocketSessionManager`, `common/scheduler/WebSocketPingScheduler`, `redis/domain/repository/RedisChat*Repository` |
| 연결/해제 이벤트 | `common/event_listener/WebSocketEventListener` |

## 레이어 구조

```
Controller / Scheduler / EventListener
        → Facade
        → Service → QueryService / CommandService
        → Repository
```
