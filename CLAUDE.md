# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

0909(공구공구)는 Spring Boot 3.5.8 기반의 마이크로서비스 이커머스 플랫폼입니다. 여러 판매자가 공동구매 상품을 등록하고 소비자가 참여할 수 있는 서비스를 제공합니다.

**기술 스택:**
- Java 17, Spring Boot 3.5.8, Spring Cloud 2025.0.0
- PostgreSQL 15, Redis 7, Elasticsearch 8.18
- Apache Kafka 7.5 (KRaft 모드)

## 빌드 및 실행 명령어

### 로컬 개발 환경

#### 인프라 서비스 실행 (Docker Compose)
```bash
# PostgreSQL, Redis, Kafka, Elasticsearch, Logstash 실행
docker-compose up -d

# 인프라 종료
docker-compose down
```

**참고:** Docker Compose는 로컬 개발 전용입니다. 서버 배포는 Kubernetes를 사용합니다.

### 프로젝트 빌드
```bash
# 전체 빌드 (테스트 포함)
./gradlew build

# 테스트 제외 빌드
./gradlew build -x test

# 특정 모듈만 빌드
./gradlew :member:build
./gradlew :commerce:build -x test
```

### 서비스 실행 (순서 중요)
```bash
# 1. Gateway 실행 (포트 8000)
./gradlew :gateway:bootRun

# 2. 비즈니스 서비스 실행 (순서 무관)
./gradlew :member:bootRun          # 포트 8083
./gradlew :commerce:bootRun        # 포트 8087
./gradlew :point:bootRun           # 포트 8086
./gradlew :elastic-search:bootRun  # 포트 8082
./gradlew :recommendation:bootRun  # 포트 8088
```

### 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :member:test
./gradlew :commerce:test

# 특정 테스트 클래스만 실행
./gradlew :member:test --tests "MemberServiceTest"

# 특정 테스트 메서드만 실행
./gradlew :member:test --tests "MemberServiceTest.testSignUp"
```

### 서비스 접근 (로컬)
- API Gateway: http://localhost:8000
- Swagger UI: http://localhost:8000/swagger-ui.html
- Elasticsearch: http://localhost:9200

### 서버 배포 (Kubernetes)

#### 배포 구조

Kubernetes 매니페스트는 `docs/k8s/` 디렉토리에 구성되어 있습니다:

```
docs/k8s/
├── infra/              # 인프라 서비스 (StatefulSet)
│   ├── kafka.yml           # Kafka (메인)
│   ├── kafka-batch.yml     # Kafka (배치 전용)
│   ├── postgres.yml        # PostgreSQL (메인)
│   ├── postgres-batch.yml  # PostgreSQL (배치 전용)
│   ├── redis.yml           # Redis
│   └── elastic-search.yml  # Elasticsearch
├── service/            # 마이크로서비스 (Deployment)
│   ├── gateway.yml         # API Gateway
│   ├── member.yml          # Member 서비스
│   ├── commerce.yml        # Commerce 서비스
│   ├── point.yml           # Point 서비스
│   ├── search.yml          # Elastic Search 서비스
│   └── ai.yml              # Recommendation 서비스
├── job/                # Job (배치 작업)
│   ├── group-purchase-job.yml          # 공구 상태 업데이트
│   ├── group-purchase-reindex-job.yml  # 공구 재인덱싱
│   ├── settlement-job.yml              # 정산
│   ├── seller-payout-job.yml           # 판매자 지급
│   ├── retry-seller-payout-job.yml     # 판매자 지급 재시도
│   └── vector-refresh-job.yml          # 벡터 갱신
├── monitoring/         # 모니터링 서비스
│   ├── prometheus.yml      # 메트릭 수집
│   ├── grafana.yml         # 대시보드
│   ├── kibana.yml          # 로그 분석
│   └── fluent-bit.yml      # 로그 수집
├── secret/             # Secret (민감 정보)
│   ├── commerce-secret.yml
│   ├── elasticsearch-secret.yml
│   ├── grafana-secret.yml
│   ├── kibana-secret.yml
│   └── postgres-secret.yml
└── cert/               # TLS 인증서 (cert-manager + Let's Encrypt)
    └── tls.yml         # Ingress 설정 (0982.store)
```

#### 배포 명령어

```bash
# 1. 인프라 서비스 배포
kubectl apply -f docs/k8s/infra/

# 2. 마이크로서비스 배포
kubectl apply -f docs/k8s/service/

# 3. Job 배포
kubectl apply -f docs/k8s/job/

# 4. 모니터링 배포
kubectl apply -f docs/k8s/monitoring/

# 5. TLS/Ingress 설정 (cert-manager 필요)
kubectl apply -f docs/k8s/cert/
```

#### 배포 확인

```bash
# Pod 상태 확인
kubectl get pods

# 서비스 확인
kubectl get svc

# Job 확인
kubectl get jobs

# 특정 서비스 로그 확인
kubectl logs -f deployment/gateway
kubectl logs -f deployment/member

# Job 실행 이력
kubectl get jobs
```

#### 주요 설정

**Docker 이미지:**
- 레지스트리: Docker Hub (`minbros/`)
- 태그: `latest`
- 이미지 예시: `minbros/gateway:latest`, `minbros/member:latest`

**리소스 제한:**
- Gateway: CPU 200m-500m, Memory 384Mi-512Mi
- Member/Commerce/Point: 각 서비스별 설정
- Kafka: Memory 256Mi-512Mi, Storage 5Gi (PVC)

**Health Check:**
- Readiness Probe: `/actuator/health` (initialDelay: 30s)
- Liveness Probe: `/actuator/health` (initialDelay: 60s)

**Secret 관리:**
- `gateway-secret`, `member-secret`, `commerce-secret` 등 각 서비스별 Secret 필요
- JWT_SECRET, DB 비밀번호 등 민감 정보 포함

**Ingress (TLS):**
- 도메인: `0982.store`
- TLS: Let's Encrypt (cert-manager)
- Ingress Controller: Traefik
- HTTPS 리다이렉트 자동 활성화

#### 서버 접근
- Production: https://0982.store
- Swagger UI: https://0982.store/swagger-ui.html

## 아키텍처

### 서비스 구성

| 서비스 | 포트 | 역할 | 주요 도메인 |
|--------|------|------|-------------|
| **gateway** | 8000 | JWT 인증, 라우팅, 권한 관리 | - |
| **member** | 8083 | 회원/판매자 관리, 알림 | Member, Seller, Notification |
| **commerce** | 8087 | 상품/주문/공동구매 관리 | Product, Order, GroupPurchase, Cart, SellerBalance |
| **point** | 8086 | 포인트 충전/결제 (Toss Payments) | PgPayment, BonusPolicy |
| **elastic-search** | 8082 | 상품 검색 인덱싱 (벡터 검색) | GroupPurchaseDocument |
| **batch** | - | 일일/월별 정산 배치 | Settlement, SellerPayout, GroupPurchase |
| **recommendation** | 8088 | 상품 벡터화, AI 추천 | - |

### 모듈 목록

settings.gradle에 정의된 10개 모듈:
- `common` - 공통 라이브러리
- `gateway` - API Gateway
- `member` - 회원/판매자 관리
- `commerce` - 상품/주문/공동구매
- `point` - 포인트/결제
- `elastic-search` - 검색 서비스
- `batch` - 배치 작업
- `recommendation` - AI 추천
- `dummy-data` - 더미 데이터 생성
- `conductor` - 추가 모듈

### 레이어 아키텍처 (Hexagonal Architecture)

각 서비스는 다음 레이어로 구성됩니다:

```
presentation/      - Controller, DTO (요청/응답)
application/       - Service, Facade, Event Listener
domain/            - Entity, Repository Interface, VO, Constant
infrastructure/    - JPA Repository, Kafka Publisher, Feign Client, Adapter
```

**예시 (member 서비스):**
```
member/src/main/java/store/_0982/member/
├── presentation/
│   └── MemberController.java
├── application/
│   └── MemberService.java
├── domain/
│   ├── Member.java
│   └── MemberRepository.java
└── infrastructure/
    ├── MemberJpaRepository.java
    ├── MemberRoleRedisCache.java
    └── PointFeignClient.java
```

### Common 모듈 (공통 라이브러리)

**위치:** `common/src/main/java/store/_0982/common/`

Common 모듈은 모든 서비스에서 공유하는 기능을 제공합니다:

1. **Kafka 설정 (`kafka/`)**
   - `KafkaCommonConfigs`: Producer/Consumer Factory 제공
   - `KafkaTopics`: 14개 토픽 상수
   - 이벤트 DTO: `OrderCreatedEvent`, `PaymentChangedEvent` 등 (BaseEvent 확장)
   - Producer 전략:
     - `defaultProducerFactory()`: 안정성 우선 (acks=all, idempotence=true) - 주문, 결제
     - `fastProducerFactory()`: 성능 우선 (acks=0, idempotence=false) - 알림, 로그

2. **예외 처리 (`exception/`)**
   - `CustomException`: 커스텀 예외 기본 클래스
   - `ErrorCode`: 에러 코드 인터페이스
   - `BaseExceptionHandler`: 전역 예외 처리

3. **AOP 로깅 (`log/`)**
   - `@ControllerLog`: HTTP 요청/응답 로깅
   - `@ServiceLog`: 서비스 메서드 실행 시간 및 에러 추적

4. **인증 (`auth/`)**
   - `Role`: GUEST, CONSUMER, SELLER, ADMIN

5. **공통 DTO (`dto/`)**
   - `ResponseDto<T>`, `PageResponse<T>`

6. **도메인 공유 (`domain/`)**
   - grouppurchase, order, product, sellerbalance, sellerpayout, settlement, vector

### Gateway 인증 및 라우팅

**JWT 인증 흐름:**
```
1. AccessTokenAuthenticationWebFilter → 쿠키에서 accessToken 추출
2. GatewayJwtProvider → JWT 파싱 및 Member 객체 변환
3. JwtReactiveAuthenticationManager → MemberAuthenticationToken 생성
4. RouteAuthorizationManager → DB에서 엔드포인트별 권한 조회
5. 권한 확인 후 대상 서비스로 라우팅
```

**라우팅 규칙 (`gateway/src/main/resources/application.yml`):**
- `/api/members/**`, `/api/notifications/**` → Member 서비스
- `/auth/**`, `/oauth2/**` → Member 서비스 (인증)
- `/api/orders/**`, `/api/carts/**`, `/api/balances/**`, `/api/products/**`, `/api/purchases/**` → Commerce 서비스
- `/api/points/**`, `/api/payments/**` → Point 서비스
- `/api/searches/**` → Elastic Search 서비스
- `/api/recommendation/**` → Recommendation 서비스
- `/webhooks/**` → Point 서비스 (Toss IP 화이트리스트)

**보안:**
- JWT Secret: 환경변수 `JWT_SECRET` 필요 (`.env` 파일)
- 공개 경로: `/auth/**`, `/webhooks/**`, `/actuator/**`, Swagger
- 게스트 사용자: 토큰 없으면 자동 게스트 토큰 생성

### 서비스 간 통신

#### 1. Feign Client (동기 통신)

**예시:**
- Member → Point: `PointFeignClient.postPointBalance()` (판매자 등록 시)
- Commerce → Member: `MemberClient.getProfile()` (주문 시 회원 정보 조회)
- Point → Commerce: `CommerceServiceClient.getOrder()` (결제 시 주문 정보 조회)
- Elastic Search → Recommendation: `RecommendationClient` (벡터 검색)

#### 2. Kafka 이벤트 (비동기 통신)

**주요 토픽:**
- `ORDER_CREATED`, `ORDER_CANCELED`, `ORDER_CHANGED`, `ORDER_CONFIRMED` (주문)
- `PAYMENT_CHANGED`, `POINT_CHANGED` (결제/포인트)
- `PRODUCT_UPSERTED`, `PRODUCT_EMBEDDING_COMPLETED` (상품)
- `GROUP_PURCHASE_CHANGED`, `GROUP_PURCHASE_FAILED` (공동구매)
- `MEMBER_DELETED`, `MEMBER_LOGGED_IN` (회원)
- `SELLER_PAYOUT_DONE` (판매자 지급, 구 SETTLEMENT_DONE은 Deprecated)

**이벤트 발행 패턴:**
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleEvent(DomainEvent event) {
    kafkaTemplate.send(TOPIC_NAME, event);
}
```

**이벤트 구독 패턴:**
```java
@KafkaListener(topics = KafkaTopics.ORDER_CREATED)
public void onOrderCreated(OrderCreatedEvent event) {
    // 비즈니스 로직 처리
}
```

#### 3. Internal API

각 서비스는 다른 서비스 전용 내부 API를 제공합니다:
- `/internal/members/{id}/role` (Member)
- `/internal/seller-accounts` (Commerce)
- `/internal/points` (Point)

### 데이터베이스 스키마

PostgreSQL에 서비스별 스키마로 분리:
- `member_schema` - Member 서비스
- `commerce_schema` - Commerce 서비스
- `point_schema` - Point 서비스
- `search_schema` - Elastic Search 서비스
- `batch_schema` - Batch 서비스 (Spring Batch 메타데이터 포함)

**초기화 스크립트:** `docker/postgres/init/` 디렉토리

**Batch DB:** 배치 전용 PostgreSQL 인스턴스 사용 (K8s: `postgres-batch.yml`)
- `batch_schema`, `order_schema`, `product_schema`, `recommendation_schema`, `settlement_schema` 연결

### 모니터링 스택

| 도구 | 역할 |
|------|------|
| **Prometheus** | 메트릭 수집 (`/actuator/metrics`, `/actuator/prometheus`) |
| **Grafana** | 메트릭 대시보드 |
| **Kibana** | 로그 분석 (Elasticsearch 기반) |
| **Fluent-bit** | 로그 수집 및 전달 |
| **Logstash** | 로그 처리 파이프라인 (로컬: Docker Compose) |

## 개발 가이드

### 새로운 이벤트 추가 시

1. **Common 모듈에 이벤트 정의**
   ```java
   // common/src/main/java/store/_0982/common/kafka/dto/
   public record NewEvent(
       UUID entityId,
       String data
   ) implements BaseEvent {}
   ```

2. **토픽 상수 추가**
   ```java
   // common/src/main/java/store/_0982/common/kafka/KafkaTopics.java
   public static final String NEW_EVENT = "new-event";
   ```

3. **발행자 구현**
   ```java
   @TransactionalEventListener(phase = AFTER_COMMIT)
   public void publishEvent(NewEvent event) {
       kafkaTemplate.send(KafkaTopics.NEW_EVENT, event);
   }
   ```

4. **구독자 구현**
   ```java
   @KafkaListener(topics = KafkaTopics.NEW_EVENT)
   public void consumeEvent(NewEvent event) {
       // 처리 로직
   }
   ```

### 새로운 엔드포인트 추가 시

1. Controller에 엔드포인트 추가
2. `@ControllerLog` 어노테이션으로 로깅 활성화
3. Gateway에 라우팅 규칙 추가 (`gateway/src/main/resources/application.yml`)
4. DB `gateway_route` 테이블에 권한 설정 추가

### 테스트 작성 가이드

- **단위 테스트:** Service 레이어는 Mockito로 Repository 모킹
- **통합 테스트:** `@SpringBootTest`로 전체 컨텍스트 로드
- **Kafka 테스트:** `@EmbeddedKafka` 사용
- **테스트 DB:** 각 서비스의 `src/test/resources/application.yml`에 H2 설정

### Soft Delete 패턴

모든 엔티티는 `deletedAt` 컬럼으로 논리적 삭제를 구현합니다:

```java
@SQLRestriction("deleted_at IS NULL")
public class Member {
    private LocalDateTime deletedAt;

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
```

### 환경변수 설정

프로젝트 루트에 `.env` 파일 생성:
```
JWT_SECRET=your-secret-key-here
TOSS_PAYMENTS_SECRET_KEY=your-toss-secret
```

## 주의사항

### 로컬 개발

1. **Kafka 토픽 생성:** 자동 생성이 비활성화되어 있으므로, 새 토픽은 수동으로 생성 필요
   ```bash
   # 로컬 환경
   docker exec -it group-purchase-kafka kafka-topics --create \
     --bootstrap-server localhost:9092 \
     --topic new-topic \
     --partitions 3 \
     --replication-factor 1
   ```

2. **환경변수:** 프로젝트 루트에 `.env` 파일 필수 (JWT_SECRET, TOSS_PAYMENTS_SECRET_KEY 등)

3. **PostgreSQL 포트:** 로컬 Docker는 5433 포트 사용 (기본 5432와 충돌 방지)

### Kubernetes 배포

1. **Secret 생성:** 배포 전 각 서비스별 Secret 생성 필요
   ```bash
   kubectl create secret generic gateway-secret \
     --from-literal=JWT_SECRET=your-secret-key \
     --from-literal=SPRING_DATASOURCE_PASSWORD=your-db-password
   ```

2. **Kafka 토픽 생성 (K8s):** Pod 내에서 토픽 생성
   ```bash
   kubectl exec -it kafka-0 -- kafka-topics --create \
     --bootstrap-server localhost:9092 \
     --topic new-topic \
     --partitions 3 \
     --replication-factor 1
   ```

3. **PVC 확인:** Kafka는 5Gi PVC를 사용하므로, StorageClass `local-path` 필요

4. **이미지 업데이트:** 새 이미지 배포 시 Pod 재시작
   ```bash
   kubectl rollout restart deployment/gateway
   kubectl rollout restart deployment/member
   ```

5. **nodeSelector 주의:** 일부 서비스는 특정 노드에 배포되도록 설정됨

6. **배치 DB 분리:** Batch 서비스는 전용 PostgreSQL 인스턴스(`postgres-batch.yml`)에 연결됨

### 공통

1. **트랜잭션 이벤트:** 반드시 `@TransactionalEventListener(phase = AFTER_COMMIT)` 사용하여 트랜잭션 커밋 후 이벤트 발행

2. **Feign Client 타임아웃:** 기본 타임아웃이 짧으므로, 필요시 설정 조정

3. **Elasticsearch 리인덱싱:** 데이터 동기화 필요 시 `/api/searches/reindex` 엔드포인트 호출

4. **Soft Delete 쿼리:** `@SQLRestriction` 적용 여부 확인 (삭제된 데이터 조회 방지)

5. **Gateway 라우트 권한:** DB 기반이므로 새 엔드포인트 추가 시 `gateway_route` 테이블 업데이트 필수

6. **Kafka 토픽 네이밍:** `SETTLEMENT_DONE` → `SELLER_PAYOUT_DONE` 으로 변경됨. 신규 개발 시 `SELLER_PAYOUT_DONE` 사용
