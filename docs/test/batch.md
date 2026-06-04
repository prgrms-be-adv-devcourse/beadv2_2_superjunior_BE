# Batch 서비스 테스트 문서

## 테스트 실행 방법

```bash
# 전체 테스트 실행
./gradlew :batch:test

# 특정 테스트 클래스 실행
./gradlew :batch:test --tests "SellerBalanceServiceTest"
./gradlew :batch:test --tests "SellerPayoutServiceTest"
./gradlew :batch:test --tests "SellerPayoutProcessorTest"

# 통합 테스트 실행
./gradlew :batch:test --tests "SellerPayoutJobIntegrationTest"
./gradlew :batch:test --tests "RetryFailedSellerPayoutJobIntegrationTest"
./gradlew :batch:test --tests "SettlementJobIntegrationTest"
./gradlew :batch:test --tests "GroupPurchaseJobIntegrationTest"
```

## 테스트 구조

```
batch/src/test/java/
├── BatchApplicationTests              # Spring Boot 기본 테스트 + 통합 테스트 공통 베이스
├── config/
│   └── BatchTestConfig                # JobLauncherTestUtils 빈 설정
├── domain/
│   └── grouppurchase/
│       └── GroupPurchaseTest          # 공동구매 도메인 로직 단위 테스트
├── application/
│   ├── sellerbalance/
│   │   └── SellerBalanceServiceTest   # 판매자 잔액 차감 서비스 단위 테스트
│   └── sellerpayout/
│       └── SellerPayoutServiceTest    # 판매자 지급 실패 저장 서비스 단위 테스트
└── batch/
    ├── grouppurchase/
    │   ├── GroupPurchaseJobIntegrationTest           # 공동구매 Job 통합 테스트
    │   ├── listener/
    │   │   └── GroupPurchaseJobListenerTest          # Job 리스너 단위 테스트
    │   ├── processor/
    │   │   ├── OpenGroupPurchaseProcessorTest        # SCHEDULED → OPEN 프로세서 단위 테스트
    │   │   └── UpdateStatusClosedGroupPurchaseProcessorTest  # OPEN → SUCCESS/FAILED 프로세서 단위 테스트
    │   └── writer/
    │       ├── OpenGroupPurchaseWriterTest           # 공동구매 오픈 Writer 단위 테스트
    │       └── UpdateStatusClosedGroupPurchaseWriterTest     # 공동구매 종료 Writer 단위 테스트
    ├── sellerpayout/
    │   ├── SellerPayoutJobIntegrationTest            # 판매자 지급 Job 통합 테스트
    │   ├── RetryFailedSellerPayoutJobIntegrationTest # 지급 실패 재시도 Job 통합 테스트
    │   ├── processor/
    │   │   ├── SellerPayoutProcessorTest             # 지급 대상 프로세서 단위 테스트
    │   │   ├── RetryFailedSellerPayoutProcessorTest  # 재시도 프로세서 단위 테스트
    │   │   └── LowBalanceNotificationProcessorTest   # 잔액 부족 알림 프로세서 단위 테스트
    │   └── writer/
    │       ├── SellerPayoutWriterTest                # 지급 처리 Writer 단위 테스트
    │       ├── RetryFailedSellerPayoutWriterTest     # 재시도 Writer 단위 테스트
    │       └── LowBalanceNotificationWriterTest      # 잔액 부족 알림 Writer 단위 테스트
    ├── settlement/
    │   ├── SettlementJobIntegrationTest              # 정산 Job 통합 테스트
    │   ├── listener/
    │   │   ├── SettlementJobListenerTest             # Job 리스너 단위 테스트
    │   │   └── SettlementStepListenerTest            # Step 리스너 단위 테스트
    │   └── writer/
    │       └── SettlementWriterTest                  # 정산 Writer 단위 테스트
    └── recommendation/
        └── processor/
            └── VectorUtilTest                        # 벡터 유틸리티 단위 테스트
```

## 테스트 커버리지

- **Domain Layer**: 공동구매 도메인 상태 전이 및 비즈니스 로직 검증
- **Application Layer**: SellerBalanceService, SellerPayoutService 서비스 계층 로직 검증
- **Batch Layer (단위)**: Processor, Writer 계층의 아이템 변환/처리 로직 검증 (Mockito)
- **Batch Layer (통합)**: 실제 PostgreSQL 컨테이너로 Job/Step 전체 흐름 검증 (Testcontainers)

## 테스트 데이터 관리

**Testcontainers PostgreSQL 사용**
```java
// BatchApplicationTests.java
static final PostgreSQLContainer<?> POSTGRES;

static {
    POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("schema.sql");
    POSTGRES.start();
}
```
- 실제 PostgreSQL 15 컨테이너로 통합 테스트
- `schema.sql`로 테스트 스키마 초기화
- 프로덕션과 동일한 DB 환경에서 Job/Step 동작 검증

**BatchApplicationTests (통합 테스트 베이스)**
- 모든 통합 테스트가 상속하는 공통 베이스 클래스
- `@SpringBootTest`, `@Testcontainers`, `@ActiveProfiles("test")` 설정
- KafkaTemplate, ElasticsearchClient, ElasticsearchOperations는 `@MockitoBean`으로 대체
- `@DirtiesContext(classMode = AFTER_CLASS)`로 컨텍스트 격리

**BatchTestConfig**
- `JobLauncherTestUtils` 빈 등록
- 통합 테스트에서 `launchJob()`, `launchStep()`으로 개별 Job/Step 단위 실행 가능

## 주요 테스트 케이스

### 1. 정산 Job (SettlementJobIntegrationTest)

**settlementStep**
- 미정산 항목(`settled_at IS NULL`)을 읽어 판매자 잔액 증가 및 `settled_at` 갱신
- 이미 정산된 항목은 재처리하지 않음
- 동일 판매자의 여러 항목 합산 처리
- 판매자 잔액이 없으면 새로 생성
- 정산 처리 건별로 `SellerBalanceHistory(CREDIT)` 생성
- `settlement_amount`가 음수이면 Step FAILED

**전체 Job**
- 미정산/기정산 항목 혼재 시 미정산 항목만 처리
- Step 실패 시 Job도 FAILED

### 2. 판매자 지급 Job (SellerPayoutJobIntegrationTest)

**sellerPayoutStep** (잔액 ≥ 30,000원 대상)
- 계좌 정보가 있으면 COMPLETED 처리 후 잔액 0으로 초기화
- COMPLETED 처리 후 `SellerBalanceHistory(DEBIT)` 생성
- 계좌 정보가 없으면 FAILED 처리 및 `SellerPayoutFailure` 생성
- 잔액이 최소 송금액(30,000원) 미만 또는 0원이면 처리하지 않음

**lowBalanceNotificationStep** (0원 초과 30,000원 미만 대상)
- 잔액 부족 판매자를 DEFERRED 처리
- 잔액이 최소 송금액 이상이거나 0원이면 처리하지 않음

**전체 Job**
- 잔액 구간에 따라 sellerPayoutStep / lowBalanceNotificationStep에 자동 라우팅

### 3. 판매자 지급 재시도 Job (RetryFailedSellerPayoutJobIntegrationTest)

**retryFailedSellerPayoutStep**
- 계좌 정보가 복구된 실패 건: COMPLETED 처리 및 `SellerPayoutFailure` 삭제
- 계좌 정보가 여전히 없는 실패 건: `retryCount` 증가
- `retryCount ≥ MAX_RETRY(5)`인 건: 처리하지 않음
- 이미 COMPLETED인 건: 필터링(processor에서 null 반환)

### 4. 공동구매 Job (GroupPurchaseJobIntegrationTest)

**openGroupPurchaseStep**
- 시작 시간이 지난 `SCHEDULED` → `OPEN` 변경
- 시작 시간 미도래 또는 이미 `OPEN`인 건은 처리하지 않음

**updateStatusClosedGroupPurchaseStep**
- 종료된 공동구매 중 최소 수량 달성 → `SUCCESS`
- 최소 수량 미달 → `FAILED`
- 정확히 최소 수량과 같으면 `SUCCESS`
- 종료 시간 미도래인 건은 처리하지 않음

### 5. SellerBalanceService 단위 테스트 (SellerBalanceServiceTest)

- 판매자 잔액이 송금액만큼 차감되고 `SellerBalance` 저장
- 일부 차감 후 나머지 잔액 유지
- DEBIT 상태의 `SellerBalanceHistory` 저장 (memberId, sellerPayoutId, amount 검증)
- 판매자 잔액이 없으면 `SELLER_NOT_FOUND` 예외 발생, 저장 호출 없음

### 6. SellerPayoutService 단위 테스트 (SellerPayoutServiceTest)

- `SellerPayoutFailure` 생성 및 저장 (sellerId, sellerPayoutId, 기간, reason 검증)
- `retryCount` 0으로 초기화
- 빈 문자열 및 긴 실패 사유 저장

### 7. Processor 단위 테스트

**SellerPayoutProcessor / LowBalanceNotificationProcessor**
- `SellerBalanceDto` → PENDING 상태 `SellerPayout` 생성
- 지난 달 시작일/마지막일로 period 설정
- 계좌 정보는 null (Writer에서 설정)
- 호출마다 독립적인 sellerPayoutId 생성

**RetryFailedSellerPayoutProcessor**
- FAILED/PENDING/DEFERRED 상태 → 재시도 대상으로 반환
- 존재하지 않는 payout → null 반환 (필터링)
- 이미 COMPLETED → null 반환 (필터링)

### 8. Writer 단위 테스트

**SellerPayoutWriter**
- 유효 계좌: COMPLETED 처리, `SellerBalanceService.clearBalance()` 호출, 이벤트 발행
- 계좌 없음/null/공백: FAILED 처리, 이벤트 미발행
- 실패가 없으면 `sellerPayoutFailureRepository.saveAll` 미호출

**RetryFailedSellerPayoutWriter**
- 재시도 성공: COMPLETED 처리, `SellerPayoutFailure` 삭제, CompletedEvent 발행
- 재시도 실패: `retryCount` 증가, FailedEvent 발행, clearBalance 미호출
- 일부 성공/일부 실패 혼재 처리

**LowBalanceNotificationWriter**
- DEFERRED 상태 변경 후 `saveAll` 호출 (순서 검증)
- payout별 DeferredEvent 발행
- 여러 payout을 한 번의 `saveAll`로 저장

## 사용한 테스트 도구/라이브러리

| 도구 | 용도 |
|------|------|
| **JUnit 5** | 테스트 프레임워크 |
| **AssertJ** | 유창한 assertion API |
| **Mockito** | Mock 객체 생성 및 검증 (Repository, KafkaTemplate 등) |
| **Testcontainers** | PostgreSQL 컨테이너 통합 테스트 환경 |
| **postgres:15-alpine** | 배치 전용 PostgreSQL 테스트 이미지 |
| **Spring Batch Test** | `JobLauncherTestUtils`로 Job/Step 단위 실행 |
| **ArgumentCaptor** | Writer에서 저장된 엔티티 내용 검증 |
