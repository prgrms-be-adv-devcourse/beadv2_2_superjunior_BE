# 0909 (공구공구) - Gemini Guide

이 파일은 Gemini CLI 에이전트가 이 프로젝트에서 효율적으로 작업하기 위한 가이드라인을 제공합니다.

## 🚀 프로젝트 개요
- **이름**: 0909 (공구공구) / `group-purchase`
- **설명**: 마이크로서비스 아키텍처(MSA) 기반의 공동구매 이커머스 플랫폼.
- **주요 특징**: 선착순/수량 조건 공동구매, 이벤트 기반 아키텍처(Kafka), 실시간 알림, 통합 검색(Elasticsearch), AI 활용(pgvector).

## 🛠 기술 스택
- **Backend**: Java 17, Spring Boot 3.5.8, Spring Cloud 2025.0.0
- **Database**: PostgreSQL 15 (pgvector), Redis 7, Elasticsearch 8.18 (Nori plugin)
- **Message Broker**: Apache Kafka 7.5 (KRaft mode)
- **Infrastructure**: Docker, Kubernetes (Traefik Ingress), GitHub Actions
- **Logging**: Logstash, Logback

## 📁 모듈 구조 및 패키지
모든 비즈니스 로직은 `store._0982.{module_name}` 패키지 하위에 위치합니다.
`settings.gradle`에 정의된 모듈은 다음과 같습니다:

- `common`: 공통 DTO, 예외 처리, Kafka 설정, AOP 로깅, OpenFeign 설정 (라이브러리 모듈)
- `gateway`: API Gateway (Port 8000) - 라우팅 및 JWT 인증 필터
- `member`: 회원 서비스 (Port 8083) - Auth, JWT, 사용자 관리
- `commerce`: 핵심 비즈니스 서비스 (Port 8087) - 상품, 공동구매, 주문, 장바구니
- `point`: 포인트/결제 서비스 (Port 8086) - Toss Payments 연동
- `elastic-search`: 검색 서비스 (Port 8082) - 상품/공구 인덱싱 및 검색
- `batch`: 배치 작업 - 정산 및 대량 데이터 처리
- `ai`: AI 서비스 (Port 8088) - 추천 및 지능형 기능 (OpenAI Embedding)

## 💻 주요 명령어

### 빌드 및 테스트
```bash
# 전체 빌드 (테스트 제외)
./gradlew build -x test

# 특정 모듈 빌드
./gradlew :{module_name}:build

# 전체 테스트 실행
./gradlew test
```

### 서비스 실행 (로컬 개발)
로컬 개발 환경에서는 **Docker Compose**를 사용하여 인프라를 구성합니다.

1. **Infrastructure**: `docker-compose up -d` (PostgreSQL, Redis, Kafka, Elasticsearch, Logstash)
2. **Gateway**: `./gradlew :gateway:bootRun`
3. **Business Services**: 각 모듈별 `bootRun`

## ☁️ 배포 환경 (Production - Kubernetes)
서버 배포는 **Kubernetes (K8s)** 환경에서 운영되며, 관련 설정은 `docs/k8s` 디렉토리를 따릅니다.

### 1. Ingress & Network
- **Ingress Controller**: Traefik
- **SSL/TLS**: Cert-Manager (Let's Encrypt), `https-redirect` Middleware 적용
- **Domain**: `0982.store` -> `gateway-service` (Port 8000)

### 2. Infrastructure (StatefulSets)
인프라 서비스는 주로 `ip-10-0-0-244` 노드에 배치됩니다.
- **PostgreSQL**: `postgres-service` (Port 5432), `pgvector/pgvector:pg15`
- **Redis**: `redis-service` (Port 6379), `redis:7-alpine`
- **Kafka**: `kafka-service` (Port 9092), `confluentinc/cp-kafka:7.5.0` (KRaft Mode)
- **Elasticsearch**: `elasticsearch-service` (Port 9200), `elasticsearch:8.18.8` (Nori Plugin 설치됨)

### 3. Applications (Deployments)
비즈니스 애플리케이션은 주로 `ip-10-0-0-5` 노드에 배치됩니다.
- **gateway**: `minbros/gateway`, Port 8000
- **member**: `minbros/member`, Port 8083
- **commerce**: `minbros/commerce`, Port 8087
- **point**: `minbros/point`, Port 8086
- **search**: `minbros/search`, Port 8082 (Infra 노드 배치)
- **ai**: `minbros/ai`, Port 8088 (Infra 노드 배치)

### 4. Batch Jobs (CronJobs)
배치 작업은 `minbros/batch` 이미지를 사용합니다.
- **group-purchase-cronjob**: 매시 0분 (공구 상태 업데이트)
- **monthly-settlement-cronjob**: 매월 1일 00:30 (월별 정산)
- **seller-balance-cronjob**: 매일 02:30 (판매자 정산)
- **vector-refresh-cronjob**: 매일 03:13 (유저 벡터 재생성)
- **retry-settlement-cronjob**: 매월 2일 00:30 (실패 정산 재시도)

## 📝 개발 컨벤션

### 코드 스타일 및 아키텍처
- **Domain-Driven Design (DDD)**: 도메인 중심 로직 구성.
- **Event-Driven**: Kafka를 통한 서비스 간 비동기 데이터 동기화.
- **Optimistic Locking**: 동시성 제어가 필요한 경우(예: 재고) JPA `@Version` 사용.

### 테스트 컨벤션
- JUnit 5 및 AssertJ 사용.
- `@DisplayName`을 사용하여 한국어로 테스트 목적 명시.
- 테스트 구조: `given` (준비) -> `when` (실행) -> `then` (검증).

## ⚠️ 주의 사항
- 로컬(`docker-compose`)과 운영(`k8s`) 환경의 설정 차이를 인지해야 합니다. (예: DB 호스트명, 포트 포워딩 등)
- `common` 모듈 수정 시 모든 서비스에 영향을 미치므로 주의가 필요합니다.