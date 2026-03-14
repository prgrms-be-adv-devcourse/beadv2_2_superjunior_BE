# Commerce 서비스 테스트 문서

## 테스트 실행 방법

```bash
# 전체 테스트 실행
./gradlew :commerce:test

# 특정 테스트 클래스 실행
./gradlew :commerce:test --tests "ProductServiceTest"
./gradlew :commerce:test --tests "OrderQueryServiceTest"

# 동시성 테스트 실행
./gradlew :commerce:test --tests "OrderConcurrencyIntegrationTest"
./gradlew :commerce:test --tests "OrderCancelConcurrencyCorrectnessTest"
./gradlew :commerce:test --tests "ParticipateServiceConcurrencyTest"
```

## 테스트 구조

```
commerce/src/test/java/
├── CommerceApplicationTests   # Spring Boot 기본 테스트
├── domain/                    # 도메인 로직 단위 테스트
│   └── OrderTest
├── application/               # 서비스 계층 테스트
│   ├── product/
│   │   └── ProductServiceTest
│   ├── order/
│   │   ├── OrderQueryServiceTest
│   │   ├── OrderCancellationPolicyResolverTest
│   │   └── CanceledOrderServiceTest
│   ├── grouppurchase/
│   │   ├── GroupPurchaseServiceTest
│   │   └── ParticipateServiceConcurrencyTest  # 동시성 테스트
│   ├── sellerbalance/
│   │   └── SellerBalanceServiceTest
│   └── settlement/
│       └── OrderSettlementServiceTest
├── presentation/              # 컨트롤러 계층 테스트
│   ├── product/
│   ├── order/
│   ├── grouppurchase/
│   └── sellerbalance/
├── integration/               # 통합 테스트
│   ├── product/
│   │   └── ProductIntegrationTest
│   ├── order/
│   │   ├── AbstractOrderCancelConcurrencySupport  # 주문 취소 동시성 테스트 공통 로직
│   │   ├── OrderGroupPurchaseIntegrationTest
│   │   ├── OrderConcurrencyIntegrationTest
│   │   ├── OrderCancelConcurrencyCorrectnessTest  # 동시성 정확성 테스트
│   │   ├── OrderCancelConcurrencyBenchmarkTest     # 동시성 성능 테스트
│   │   ├── PartialRetryRollbackIntegrationTest
│   │   ├── CanceledOrderServiceIntegrationTest
│   │   └── testsupport/                           # 재시도 테스트 지원 서비스
│   │       ├── DecreaseQuantityTxService
│   │       ├── RetryCancelService
│   │       └── RetryQuantityService
│   ├── grouppurchase/
│   │   └── GrouppurchaseIntegrationTest
│   └── sellerbalance/
│       ├── SellerBalanceIntegrationTest
│       └── InternalSellerBalanceIntegrationTest
└── support/                   # 테스트 지원 클래스
    ├── BaseIntegrationTest
    ├── BaseConcurrencyTest
    ├── PostgreSQLContainerInitializer
    └── concurrency/
        ├── BenchmarkStats     # 벤치마크 통계 유틸리티
        └── ConcurrencyResult  # 동시성 테스트 결과 저장
```

## 테스트 커버리지

- **Domain Layer**: 주문 도메인 비즈니스 로직 검증
- **Application Layer**: 서비스 계층 로직 및 트랜잭션 처리
- **Presentation Layer**: API 엔드포인트 및 요청/응답 검증 (MockMvc 사용)
- **Integration Layer**: 서비스 간 통합 및 이벤트 처리 검증
- **Concurrency**: 동시성 제어 및 락 성능 검증 (벤치마크 포함)

## 테스트 데이터 관리

**Testcontainers PostgreSQL 사용**
```java
// PostgreSQLContainerInitializer.java
private static final PostgreSQLContainer<?> POSTGRESQL =
    new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg15"))
        .withDatabaseName("commerce_test")
        .withUsername("test")
        .withPassword("test")
        .withCommand("postgres -c max_connections=200");
```
- 실제 PostgreSQL 컨테이너로 테스트
- pgvector 지원 (벡터 검색 기능)
- 프로덕션과 동일한 DB 환경에서 테스트

**Flyway 스키마 관리**
```yaml
spring:
  flyway:
    enabled: true
    default-schema: product_schema
    schemas:
      - product_schema
      - order_schema
      - settlement_schema
    create-schemas: true
  jpa:
    hibernate:
      ddl-auto: none  # Flyway로 스키마 관리
```

**BaseIntegrationTest**
- 통합 테스트를 위한 공통 설정
- `@ContextConfiguration(initializers = PostgreSQLContainerInitializer.class)`
- @SpringBootTest, @RecordApplicationEvents 설정

**BaseConcurrencyTest**
- 동시성 테스트를 위한 ExecutorService 설정
- CountDownLatch를 활용한 동시 실행 제어

## 주요 테스트 케이스

### 1. 상품 관리
- 상품 생성, 수정, 삭제 검증
- 상품 조회 및 검색 기능 테스트

### 2. 주문 취소 동시성 제어 (OrderCancelConcurrencyCorrectnessTest)
**테스트 1: 서로 다른 주문 10개 동시 취소**
- 메서드: `cancel_10_orders_same_groupPurchase_all_success_and_quantity_zero()`
- 공동구매 수량 50 → 10개 주문 동시 취소 → 수량 0 검증
- 모든 주문이 성공적으로 취소되는지 검증
- 재고 복구 정확성 보장

**테스트 2: 동일 주문 10번 동시 취소 (멱등성)**
- 메서드: `cancel_same_order_10_times_only_one_canceled_order()`
- 같은 주문을 10번 동시 취소해도 CanceledOrder는 1개만 생성
- 멱등성 보장 검증

### 3. 주문 취소 성능 벤치마크 (OrderCancelConcurrencyBenchmarkTest)
**메서드**: `benchmark_cancel_100_orders_load_32()`
- 32개 스레드로 100개 주문 동시 취소 처리
- warmup 3회, 실제 측정 10회 반복
- 평균 처리 시간, latency 통계 측정
- @Tag("benchmark")로 일반 테스트와 분리
- 프로덕션 환경에서의 락 전략 결정 근거 제공

### 4. 공동구매 참여 동시성 제어 (ParticipateServiceConcurrencyTest)
**메서드**: `participate_concurrency_shouldAllowExactlyMaxParticipants()`
- 100명이 동시에 참여할 때 정확히 100명만 성공
- 비관적 락을 통한 동시성 제어
- 재고 초과 구매 방지
- `GROUP_PURCHASE_IS_REACHED` 예외 처리 검증

### 5. Spring Retry의 Rollback-Only 오염 검증 (PartialRetryRollbackIntegrationTest)
**메서드**: `rollbackOnlyPollutionProof()`
- @Retryable(maxAttempts=4) 설정으로 4번 재시도
- rollback-only 마킹된 트랜잭션의 재시도 동작 검증
- UnexpectedRollbackException 발생 확인
- 재시도 카운트 검증으로 실제 4번 시도했는지 확인

### 6. 판매자 정산 잔액 관리
- 판매자 잔액 조회 및 정산 처리
- 정산 금액 계산 로직 검증
- 판매자 지급 처리 테스트

### 7. 주문 취소 정책 (OrderCancellationPolicyResolverTest)
- Void, Reversal, Refund 정책 선택 로직 검증
- 구매자 귀책 사유: void 기간 → Void 정책
- 구매자 귀책 사유: reversed 기간 → Reversal 정책
- 구매자 귀책 사유: returned 기간 → Refund 정책
- 판매자 귀책 사유: 항상 Void 정책
- 정책 ID로 정책 조회 기능 테스트

### 8. Kafka 이벤트 발행
- 주문 생성/취소/확정 이벤트 발행 검증
- 상품 변경 이벤트 발행 검증
- 공동구매 상태 변경 이벤트 발행 검증

### 9. 테스트 지원 클래스
**AbstractOrderCancelConcurrencySupport**
- 주문 취소 동시성 테스트의 공통 설정 및 헬퍼 메서드 제공
- OrderCancelConcurrencyCorrectnessTest와 OrderCancelConcurrencyBenchmarkTest의 부모 클래스
- 테스트 데이터 생성 및 동시성 실행 로직 추상화

**testsupport 패키지 (integration/order/testsupport/)**
- `DecreaseQuantityTxService`: 재고 감소 트랜잭션 테스트 서비스
- `RetryCancelService`: 재시도 가능한 주문 취소 서비스
- `RetryQuantityService`: 재시도 가능한 수량 변경 서비스
- Spring Retry의 롤백 동작 및 트랜잭션 오염 검증용

**support/concurrency 패키지**
- `BenchmarkStats`: 벤치마크 통계 계산 (평균, 중앙값, percentile 등)
- `ConcurrencyResult`: 동시성 테스트 실행 결과 집계 및 분석

## 사용한 테스트 도구/라이브러리

| 도구                          | 용도                              |
|-----------------------------|---------------------------------|
| **JUnit 5**                 | 테스트 프레임워크                       |
| **AssertJ**                 | 유창한 assertion API               |
| **Mockito**                 | Mock 객체 생성 및 검증 (KafkaTemplate) |
| **Testcontainers**          | PostgreSQL 컨테이너 테스트 환경          |
| **pgvector/pgvector:pg15**  | 벡터 검색 지원 PostgreSQL 이미지        |
| **CountDownLatch**          | 동시 실행 동기화                       |
| **BenchmarkStats**          | 벤치마크 통계 계산 (평균, latency)       |
| **ConcurrencyResult**       | 동시성 테스트 결과 집계                  |
