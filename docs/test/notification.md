# Notification 서비스 테스트 문서

## 테스트 실행 방법

```bash
# Notification 테스트는 member 모듈에 포함됨
./gradlew :member:test --tests "*Notification*"

# 특정 테스트 클래스 실행
./gradlew :member:test --tests "NotificationServiceTest"
./gradlew :member:test --tests "NotificationCreatorTest"
```

## 테스트 구조

```
member/src/test/java/
└── store/_0982/member/
    ├── domain/notification/
    │   └── NotificationTest                    # 도메인 로직 단위 테스트
    ├── application/notification/
    │   ├── NotificationServiceTest             # 서비스 계층 테스트
    │   ├── NotificationCreatorTest             # 알림 생성 로직 테스트
    │   ├── OrderEventListenerTest              # 주문 이벤트 리스너 테스트
    │   ├── PointEventListenerTest              # 포인트 이벤트 리스너 테스트
    │   ├── SettlementEventListenerTest         # 정산 이벤트 리스너 테스트
    │   └── GroupPurchaseEventListenerTest      # 공동구매 이벤트 리스너 테스트
    ├── infrastructure/notification/
    │   └── NotificationAdapterTest             # 영속성 어댑터 테스트
    └── presentation/notification/
        └── NotificationControllerTest          # 컨트롤러 계층 테스트
```

## 테스트 커버리지

- **Domain Layer**: 알림 상태 변경, 권한 검증 로직
- **Application Layer**: 알림 읽기/생성 비즈니스 로직, Kafka 이벤트 리스너
- **Infrastructure Layer**: DB 조회 및 영속성 처리
- **Presentation Layer**: API 엔드포인트 및 요청/응답 검증
- **Event Integration**: Kafka 이벤트 수신 및 알림 생성 E2E 테스트

## 테스트 데이터 관리

**H2 인메모리 DB 사용**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## 주요 테스트 케이스

### 1. 타인의 알림 접근 차단
```java
@Test
@DisplayName("다른 회원의 알림을 읽으려고 하면 예외가 발생한다")
void read_fail_whenNotOwnNotification()
```
- 알림의 memberId와 요청자의 memberId 불일치 시 예외
- 개인정보 보호 필수 요구사항

### 2. 실패 상태 알림 읽기 불가
```java
@Test
@DisplayName("실패 상태의 알림은 읽을 수 없다")
void read_fail()
```
- FAILED 상태 알림은 읽음 처리 불가
- 비즈니스 규칙 준수

### 3. Kafka 이벤트 기반 알림 생성
```java
@Test
@DisplayName("주문 생성 이벤트를 수신하면 주문 예약 알림이 생성된다")
void handleOrderCreatedEvent()
```
- EmbeddedKafka를 통한 실제 이벤트 발행/수신 검증
- 주문, 포인트, 정산, 공동구매 4가지 이벤트 소스 테스트
- 이벤트 타입에 맞는 알림 타입과 ReferenceType 매핑
- Awaitility로 비동기 이벤트 처리 대기

### 4. 일괄 읽음 처리
```java
@Test
@DisplayName("읽지 않은 알림을 모두 읽음 처리한다")
void readAll_success()
```
- 특정 회원의 미읽은 알림 전체를 READ 상태로 변경
- 사용자 편의성 기능

## 사용한 테스트 도구/라이브러리

| 도구                  | 용도                   |
|---------------------|----------------------|
| **JUnit 5**         | 테스트 프레임워크            |
| **AssertJ**         | 유창한 assertion API    |
| **Mockito**         | Mock 객체 생성 및 검증      |
| **@SpringBootTest** | 통합 테스트 (전체 컨텍스트 로드)  |
| **H2 Database**     | 인메모리 테스트 DB          |
| **EmbeddedKafka**   | Kafka 이벤트 리스너 통합 테스트 |
| **KafkaTemplate**   | 테스트용 Kafka 메시지 발행    |
| **Awaitility**      | 비동기 이벤트 처리 대기 및 검증   |
