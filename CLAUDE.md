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
# PostgreSQL, Redis, Kafka, Elasticsearch 실행
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
./gradlew :ai:bootRun              # 포트 8088
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
│   ├── kafka.yml       # Kafka (5Gi PVC)
│   ├── postgres.yml    # PostgreSQL
│   ├── redis.yml       # Redis
│   └── elastic-search.yml  # Elasticsearch
├── service/            # 마이크로서비스 (Deployment)
│   ├── gateway.yml     # API Gateway
│   ├── member.yml      # Member 서비스
│   ├── commerce.yml    # Commerce 서비스
│   ├── point.yml       # Point 서비스
│   ├── search.yml      # Elastic Search 서비스
│   └── ai.yml          # AI 서비스
├── job/                # CronJob (배치 작업)
│   ├── group-purchase-cronjob.yml      # 공구 상태 업데이트 (매시간)
│   ├── seller-balance-cronjob.yml      # 판매자 잔액 업데이트 (매일 02:30)
│   ├── monthly-settlement-cronjob.yml  # 월별 정산 (매월 1일 00:30)
│   ├── retry-settlement-cronjob.yml    # 정산 재시도 (매월 2일 00:30)
│   ├── vector-refresh-cronjob.yml      # 벡터 갱신 (매일 03:13)
│   └── kustomization.yml
└── cert/               # TLS 인증서 (cert-manager + Let's Encrypt)
    └── tls.yml         # Ingress 설정 (0982.store)
```

#### 배포 명령어

```bash
# 1. 인프라 서비스 배포
kubectl apply -f docs/k8s/infra/

# 2. 마이크로서비스 배포
kubectl apply -f docs/k8s/service/

# 3. CronJob 배포 (Kustomize 사용)
kubectl apply -k docs/k8s/job/

# 4. TLS/Ingress 설정 (cert-manager 필요)
kubectl apply -f docs/k8s/cert/

# 전체 한번에 배포
kubectl apply -f docs/k8s/infra/ -f docs/k8s/service/ -k docs/k8s/job/ -f docs/k8s/cert/
```

#### 배포 확인

```bash
# Pod 상태 확인
kubectl get pods

# 서비스 확인
kubectl get svc

# CronJob 확인
kubectl get cronjobs

# 특정 서비스 로그 확인
kubectl logs -f deployment/gateway
kubectl logs -f deployment/member

# CronJob 실행 이력
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

**CronJob 스케줄 (Asia/Seoul):**
- `group-purchase-cronjob`: 매시간 (`0 * * * *`)
- `seller-balance-cronjob`: 매일 02:30 (`30 2 * * *`)
- `monthly-settlement-cronjob`: 매월 1일 00:30 (`30 0 1 * *`)
- `retry-settlement-cronjob`: 매월 2일 00:30 (`30 0 2 * *`)
- `vector-refresh-cronjob`: 매일 03:13 (`13 3 * * *`)

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
| **commerce** | 8087 | 상품/주문/공동구매 관리 | Product, Order, GroupPurchase, Cart |
| **point** | 8086 | 포인트 충전/결제 (Toss Payments) | PgPayment, BonusPolicy |
| **elastic-search** | 8082 | 상품 검색 인덱싱 (벡터 검색) | GroupPurchaseDocument |
| **batch** | - | 일일/월별 정산 배치 | Settlement |
| **ai** | 8088 | 상품 벡터화, AI 기능 | - |

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
   - `KafkaTopics`: 14개 토픽 상수 (ORDER_CREATED, PAYMENT_CHANGED 등)
   - 이벤트 DTO: `OrderCreatedEvent`, `PaymentChangedEvent` 등 (BaseEvent 확장)
   - Producer 전략:
     - `defaultProducerFactory()`: 안정성 우선 (acks=all, idempotence=true)
     - `fastProducerFactory()`: 성능 우선 (acks=0, idempotence=false)

2. **예외 처리 (`exception/`)**
   - `CustomException`: 커스텀 예외 기본 클래스
   - `ErrorCode`: 에러 코드 인터페이스
   - `BaseExceptionHandler`: 전역 예외 처리

3. **AOP 로깅 (`log/`)**
   - `@ControllerLog`: HTTP 요청/응답 로깅
   - `@ServiceLog`: 서비스 메서드 실행 시간 및 에러 추적

4. **인증 (`auth/`)**
   - `Role`: GUEST, CONSUMER, SELLER, ADMIN
   - `RequireRole`: 권한 검증 어노테이션 (Deprecated - Gateway에서 처리)

5. **공통 DTO (`dto/`)**
   - `ResponseDto<T>`, `PageResponse<T>`

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
- `/api/members/**` → Member 서비스
- `/api/orders/**, /api/carts/**` → Commerce 서비스
- `/api/points/**, /api/payments/**` → Point 서비스
- `/api/searches/**` → Elastic Search 서비스
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

#### 2. Kafka 이벤트 (비동기 통신)

**주요 토픽:**
- `ORDER_CREATED`, `ORDER_CANCELED`, `ORDER_CONFIRMED` (주문)
- `PAYMENT_CHANGED`, `POINT_CHANGED` (결제/포인트)
- `PRODUCT_UPSERTED`, `PRODUCT_EMBEDDING_COMPLETED` (상품)
- `GROUP_PURCHASE_CHANGED`, `GROUP_PURCHASE_FAILED` (공동구매)
- `MEMBER_DELETED`, `MEMBER_LOGGED_IN` (회원)
- `SETTLEMENT_DONE` (정산)

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

**초기화 스크립트:** `docker/postgres/init/` 디렉토리

## 개발 가이드

### 새로운 이벤트 추가 시

1. **Common 모듈에 이벤트 정의**
   ```java
   // common/src/main/java/store/_0982/common/kafka/event/
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

4. **CronJob 수동 실행:** 테스트를 위해 CronJob을 즉시 실행
   ```bash
   kubectl create job --from=cronjob/group-purchase-cronjob test-job-1
   ```

5. **이미지 업데이트:** 새 이미지 배포 시 Pod 재시작
   ```bash
   kubectl rollout restart deployment/gateway
   kubectl rollout restart deployment/member
   ```

6. **nodeSelector 주의:** 일부 서비스는 특정 노드에 배포되도록 설정됨 (gateway: ip-10-0-0-5, kafka: ip-10-0-0-244)

### 공통

1. **트랜잭션 이벤트:** 반드시 `@TransactionalEventListener(phase = AFTER_COMMIT)` 사용하여 트랜잭션 커밋 후 이벤트 발행

2. **Feign Client 타임아웃:** 기본 타임아웃이 짧으므로, 필요시 설정 조정

3. **Elasticsearch 리인덱싱:** 데이터 동기화 필요 시 `/api/searches/reindex` 엔드포인트 호출

4. **Soft Delete 쿼리:** `@SQLRestriction` 적용 여부 확인 (삭제된 데이터 조회 방지)

5. **Gateway 라우트 권한:** DB 기반이므로 새 엔드포인트 추가 시 `gateway_route` 테이블 업데이트 필수

6. **CronJob 동시 실행 방지:** `concurrencyPolicy: Forbid` 설정으로 중복 실행 방지됨
