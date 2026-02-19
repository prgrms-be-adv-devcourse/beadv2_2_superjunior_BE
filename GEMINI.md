# 0909 (공구공구) - Gemini CLI Guide

이 가이드는 Gemini CLI가 이 프로젝트에서 효율적으로 작업할 수 있도록 프로젝트 구조, 기술 스택, 개발 컨벤션 및 주요 정보를 요약합니다.

## 🚀 프로젝트 개요
- **이름**: 0909 (공구공구) / `beadv2_2_superjunior_BE`
- **설명**: 마이크로서비스 아키텍처(MSA) 기반의 대규모 트래픽 대응 공동구매 이커머스 플랫폼.
- **주요 특징**:
  - 선착순 및 수량 조건 기반의 공동구매 로직 (Optimistic Locking)
  - Kafka를 활용한 이벤트 기반 아키텍처 (EDA)
  - Elasticsearch + Nori 형태소 분석기를 통한 통합 검색
  - Redis를 활용한 분산 락 및 캐싱
  - Spring AI(OpenAI)와 pgvector를 활용한 개인화 추천 시스템

## 🛠 기술 스택
- **언어 및 프레임워크**: Java 17, Spring Boot 3.5.8, Spring Cloud 2025.0.0
- **데이터베이스**:
  - **Main**: PostgreSQL 15 (pgvector 확장 사용)
  - **Cache/Session**: Redis 7
  - **Search**: Elasticsearch 8.18.8 (Nori Plugin 포함)
- **메시지 브로커**: Apache Kafka 7.5 (KRaft mode)
- **모니터링 & 로깅**: Logstash, Logback
- **인프라**: Docker Compose (로컬), Kubernetes (운영 - Traefik Ingress)
- **CI/CD**: GitHub Actions

## 📁 모듈 구조 및 네트워크 포트
모든 서비스는 `store._0982.{module_name}` 패키지 구조를 따릅니다.

| 모듈명 | 포트 | 설명 |
| :--- | :--- | :--- |
| `common` | - | 공통 DTO, 예외 처리, Kafka 설정, AOP 로깅 등 (Library) |
| `gateway` | 8000 | API Gateway, JWT 인증 필터, 라우팅 |
| `member` | 8083 | 회원 관리, Auth(OAuth2/JWT), 실시간 알림 |
| `commerce` | 8087 | 상품, 공구, 주문, 장바구니, 재고 관리 (핵심 도메인) |
| `point` | 8086 | 포인트 충전/결제 (Toss Payments 연동), 결제 이력 |
| `elastic-search` | 8082 | 상품/공구 통합 검색 및 인덱싱 |
| `recommendation` | 8088 | AI 기반 추천 서비스 (Spring AI, OpenAI Embedding) |
| `batch` | - | 대량 데이터 처리, 정산(판매자/월별), 벡터 재생성 |
| `dummy-data` | - | 테스트용 대량 더미 데이터 생성 도구 |

## 💻 주요 명령어 (Local Development)

### 빌드 및 테스트
```bash
# 전체 빌드 (테스트 제외)
./gradlew build -x test

# 특정 모듈 빌드 (예: member)
./gradlew :member:build

# 전체 테스트 실행
./gradlew test
```

### 인프라 및 서비스 실행
1. **인프라 실행 (Docker)**: `docker-compose up -d`
2. **개별 서비스 실행**: `./gradlew :{module_name}:bootRun`
   - 순서 권장: `gateway` -> `member` -> 기타 비즈니스 서비스

## ☁️ 운영 환경 (Kubernetes)
배포 관련 설정은 `docs/k8s/` 하위에 위치합니다.
- **Ingress**: Traefik 기반 `0982.store` 도메인 라우팅
- **StatefulSets**: PostgreSQL, Redis, Kafka, Elasticsearch
- **CronJobs**:
  - `group-purchase-cronjob`: 공동구매 상태 업데이트 (매시 0분)
  - `seller-balance-cronjob`: 판매자 정산 (매일 02:30)
  - `vector-refresh-cronjob`: 유저 추천 벡터 갱신 (매일 03:13)

## 📝 개발 컨벤션 및 가이드라인

### 1. 코드 스타일 & 아키텍처
- **DDD (Domain-Driven Design)**: 비즈니스 로직은 최대한 도메인 모델 내에 캡슐화합니다.
- **Async/Event-Driven**: 서비스 간 강결합을 피하기 위해 Kafka 이벤트를 적극 활용합니다.
- **Concurrency Control**: 재고 등 동시성 이슈가 중요한 곳은 JPA `@Version` 또는 Redis Distributed Lock을 사용합니다.

### 2. 테스트 작성 (JUnit 5, AssertJ)
- **Given-When-Then** 패턴을 준수합니다.
- `@DisplayName`에 한국어로 명확한 테스트 의도를 기재합니다.

### 3. 주의 사항
- `common` 모듈은 모든 서비스의 의존성이므로 수정 시 신중해야 합니다.
- API Gateway를 통한 접근 시 JWT 토큰이 필요하며, 로컬 개발 시 `application.yml`의 환경 변수 및 URL 설정을 확인하십시오.

---
*이 문서는 프로젝트의 변화에 따라 수시로 갱신됩니다.*
