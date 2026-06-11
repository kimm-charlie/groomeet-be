# Project Structure Detail

## 디렉토리 레이아웃

```
src/main/java/com/motd/be/
├── common/
│   ├── constants/         # DefaultConstants, ValidationMessages, PageSizeConstants, TimePolicy, ValidationConstants
│   ├── utils/             # DateFormatUtils, Utils, CookieUtils
│   ├── scheduler/         # 스케줄러
│   ├── exception/         # 예외 핸들러
│   ├── filter/            # 보안 필터
│   ├── interceptor/
│   └── config/
├── module/
│   ├── admin/             # 관리자 모듈
│   │   └── {domain}/
│   │       ├── controller/
│   │       ├── facade/
│   │       ├── service/
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   └── response/
│   │       ├── entity/
│   │       ├── repository/
│   │       └── validator/
│   ├── director/          # 디렉터 모듈
│   │   └── {domain}/...
│   └── member/            # 멤버/퍼블릭 모듈
│       └── {domain}/...
├── shared/                # 공유 서비스 (AWS, Firebase, AI 등)
└── exception/             # 글로벌 예외 처리
```

## 리소스

```
src/main/resources/
├── db/migration/          # Flyway SQL 마이그레이션
├── local/                 # 로컬 환경 설정
├── dev/                   # 개발 환경 설정
└── prod/                  # 운영 환경 설정
```

## 테스트

```
src/test/java/com/motd/be/
├── annotation/            # @RestDocsTest, @ControllerIntegrationTest
├── BaseIntegrationTest.java
├── BaseRestDocsTest.java
└── module/                # 테스트 코드 (본 코드와 동일 구조)
```

## 빌드 명령어

```bash
# 전체 빌드 (테스트 포함)
./gradlew clean build

# 테스트만
./gradlew test

# 로컬 실행
./gradlew bootRun

# 특정 테스트 실행
./gradlew test --tests "com.motd.be.module.*.ClassName"
```
