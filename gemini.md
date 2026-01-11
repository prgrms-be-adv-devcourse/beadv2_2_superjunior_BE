# 0909 (공구공구) - Gemini Guide

이 파일은 Gemini CLI 에이전트가 이 프로젝트에서 효율적으로 작업하기 위한 가이드라인을 제공합니다.

## 🚀 프로젝트 개요
- **이름**: 0909 (공구공구)
- **설명**: 마이크로서비스 아키텍처(MSA) 기반의 공동구매 이커머스 플랫폼.
- **주요 특징**: 선착순/수량 조건 공동구매, 이벤트 기반 아키텍처(Kafka), 실시간 알림, 통합 검색.

## 🛠 기술 스택
- **Backend**: Java 17, Spring Boot 3.5.8, Spring Cloud (Eureka, Gateway, Config)
- **Database**: PostgreSQL (Main), Redis (Session/Cache), Elasticsearch (Search)
- **Message Broker**: Apache Kafka (Event-driven)
- **Infrastructure**: Docker, Kubernetes, GitHub Actions

## 📁 모듈 구조 및 패키지
모든 비즈니스 로직은 `store._0982.{module_name}` 패키지 하위에 위치합니다.

- `common`: 공통 DTO, 예외 처리, Kafka 설정, AOP 로깅 (라이브러리 모듈)
- `discovery`: Eureka Server (서비스 등록/발견)
- `gateway`: API Gateway (라우팅, JWT 인증 필터)
- `config`: Spring Cloud Config Server (중앙 설정 관리)
- `member`: 회원 서비스 (Auth, JWT, 사용자 관리)
- `commerce`: 핵심 비즈니스 서비스 (상품, 공동구매, 주문, 장바구니, 정산)
- `point`: 결제 및 포인트 서비스 (Toss Payments 연동)
- `elastic-search`: 검색 서비스 (상품/공구 인덱싱 및 검색)
- `batch`: 배치 작업 (정산 및 대량 데이터 처리)

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

### 서비스 실행 (순서 권장)
1. **Infrastructure**: `docker-compose up -d`
2. **Config/Discovery**: `./gradlew :config:bootRun`, `./gradlew :discovery:bootRun`
3. **Gateway**: `./gradlew :gateway:bootRun`
4. **Business Services**: 각 모듈별 `bootRun`

## 📝 개발 컨벤션

### 코드 스타일 및 아키텍처
- **Domain-Driven Design (DDD)** 원칙을 부분적으로 채택하여 도메인 중심의 로직 구성.
- **Event-Driven**: 서비스 간 데이터 동기화는 Kafka 이벤트를 통해 비동기로 처리.
- **Optimistic Locking**: 공동구매 인원 제한 등 동시성 제어가 필요한 경우 JPA `@Version` 사용.

### 테스트 컨벤션
- JUnit 5 및 AssertJ 사용.
- `@DisplayName`을 사용하여 한국어로 테스트 목적 명시.
- 테스트 구조: `given` (준비) -> `when` (실행) -> `then` (검증).

### 보안 및 인증
- JWT 기반 인증을 사용하며, Gateway에서 인증 후 사용자 정보를 헤더(`X-Member-Id`, `X-Member-Role` 등)에 주입.
- 서비스 내부에서는 이 헤더를 통해 사용자 컨텍스트를 파악.

## ⚠️ 주의 사항
- `common` 모듈 수정 시 모든 서비스에 영향을 미치므로 신중히 변경할 것.
- 새로운 Kafka 토픽 추가 시 `common` 모듈의 `KafkaTopics` 정의를 확인하거나 추가할 것.
- DB 스키마는 서비스별로 분리되어 있으므로(PostgreSQL Schema) 연결 설정을 주의 깊게 확인할 것.
