# Point 서비스 테스트 문서

## 테스트 실행 방법

```bash
# 전체 테스트 실행
./gradlew :point:test

# 특정 테스트 클래스 실행
./gradlew :point:test --tests "MemberPointServiceTest"

# 동시성 테스트 실행
./gradlew :point:test --tests "MemberPointServiceConcurrencyTest"
```

## 테스트 구조

```
point/src/test/java/
├── domain/                    # 도메인 로직 단위 테스트
│   ├── MemberPointTest
│   ├── MemberPointHistoryTest
│   └── PaymentPointTest
├── application/               # 서비스 계층 테스트
│   ├── MemberPointServiceTest
│   ├── PaymentPointServiceTest
│   ├── RefundServiceTest
│   ├── TossPaymentServiceTest
│   └── MemberPointServiceConcurrencyTest  # 동시성 테스트
├── presentation/              # 컨트롤러 계층 테스트
│   ├── MemberPointControllerTest
│   └── PaymentPointControllerTest
└── client/                    # 외부 API 클라이언트 테스트
    └── TossPaymentClientTest
```

## 테스트 커버리지

### 현재 커버리지: 76%

- **Domain Layer**: 비즈니스 로직 검증 (잔액 부족, 환불 불가 조건 등)
- **Application Layer**: 서비스 계층 로직 및 트랜잭션 처리
- **Presentation Layer**: API 엔드포인트 및 요청/응답 검증
- **Concurrency**: 멱등성 및 동시성 제어 검증

## 테스트 데이터 관리

**H2 인메모리 DB 사용**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;INIT=CREATE SCHEMA IF NOT EXISTS point_schema
  jpa:
    hibernate:
      ddl-auto: create-drop
```

**FixtureMonkey 활용**
- 동시성 테스트에서 다양한 멱등성 키 생성
- 테스트 데이터의 일관성 유지

## 주요 테스트 케이스

### 1. 포인트 부족 시 차감 불가
- 잔액보다 큰 금액 차감 시도 시 예외 발생
- 데이터 무결성 보장

### 2. 중복 요청 멱등성 보장
```java
@Test
@DisplayName("네트워크 장애에 의한 중복 차감 요청을 중복 처리하지 않는다")
void concurrent_deduct_idempotent()
```
- 같은 멱등성 키로 동시에 10개 요청
- DB에는 1개만 저장되는지 검증
- 실제 네트워크 재시도 상황 재현

### 3. 사용자 중복 클릭 방지
```java
@Test
@DisplayName("사용자 실수에 의한 중복 차감 요청을 중복 처리하지 않는다")
void concurrent_deduct_duplicate()
```
- 다른 멱등성 키, 같은 주문 ID로 동시 요청
- unique constraint로 중복 차감 방지

### 4. 환불 기간 검증
- 결제 승인 후 7일 이내만 환불 가능
- 기간 초과 시 환불 불가 예외 발생

### 5. 환불 권한 검증
- 본인의 결제만 환불 가능
- 타인의 결제 환불 시도 시 예외 발생

### 6. 이중 환불 방지
- 이미 환불된 결제는 재환불 불가
- 기존 환불 정보 반환

## 사용한 테스트 도구/라이브러리

| 도구                  | 용도                |
|---------------------|-------------------|
| **JUnit 5**         | 테스트 프레임워크         |
| **AssertJ**         | 유창한 assertion API |
| **Mockito**         | Mock 객체 생성 및 검증   |
| **H2 Database**     | 인메모리 테스트 DB       |
| **FixtureMonkey**   | 테스트 데이터 생성        |
| **EmbeddedKafka**   | 카프카 이벤트 테스트       |
| **ExecutorService** | 동시성 테스트 멀티스레드 실행  |
| **CountDownLatch**  | 동시 실행 동기화         |
