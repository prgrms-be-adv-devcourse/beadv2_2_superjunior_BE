# 0909 (공구공구)

판매자가 공동구매 상품을 등록하고, 소비자가 참여할 수 있는 마이크로서비스 기반 이커머스 플랫폼입니다.

<br>

## 🛠 기술 스택

### 백엔드
- **Java 17**
- **Spring Boot 3.5.8**
- **Spring Cloud**
- **Spring Data JPA**
- **Spring Batch**

### 데이터베이스
- **PostgreSQL 15**
- **Redis 7**
- **Elasticsearch 8.18**

### 메시지 큐
- **Apache Kafka 7.5** (KRaft 모드)

<br>

## 🏗 시스템 아키텍처

### 아키텍처 다이어그램

<img width="5796" height="3312" alt="archi" src="https://github.com/user-attachments/assets/af210ade-93db-4272-8cba-c8f49b8ad246" />


### 서비스 구성

#### Infrastructure Services

| 서비스 | 포트 | 역할 |
|--------|------|------|
| **API Gateway** | 8000 | 라우팅, JWT 인증, 사용자 컨텍스트 주입 |

#### Business Services

| 서비스 | 포트 | 역할 |
|--------|------|------|
| **Member** | 8083 | 회원 인증/인가, JWT 토큰 발급, 알림 관리 |
| **Commerce** | 8087 | 상품 및 공동구매 관리, 주문 처리 |
| **Point** | 8086 | 포인트 충전/결제 (Toss Payments 연동) |
| **Elastic Search** | 8082 | 상품/공동구매 검색 인덱싱 |
| **Recommendation** | 8088 | 상품 벡터화, AI 추천 |
| **Batch** | - | 일일/월별 정산 배치 |

#### Shared Module
- **Common**:  공통 DTO, Kafka 설정, 예외 처리, 로깅 Aspect

<br>

## 📁 프로젝트 구조

```
beadv2_2_superjunior_BE/
├── common/                 # 공통 모듈 (DTO, Kafka, Exception, AOP)
├── gateway/                # API 게이트웨이
├── member/                 # 회원 서비스
├── commerce/               # 상품/주문 서비스
│   ├── domain/
│   │   ├── product/
│   │   ├── grouppurchase/
│   │   ├── order/
│   │   ├── cart/
│   │   └── sellerbalance/
├── point/                  # 포인트/결제 서비스
├── elastic-search/         # 검색 서비스
├── recommendation/         # AI 추천 서비스
├── batch/                  # 배치 작업 (정산)
├── dummy-data/             # 더미 데이터 생성
├── docker/                 # Docker 설정 파일
│   ├── postgres/init/
│   └── logstash/
├── docs/                   # 문서
├── docker-compose.yml      # 인프라 구성
├── build.gradle            # 루트 빌드 설정
└── settings.gradle         # 모듈 설정
```

<br>

## 🚀 실행 방법

### 사전 요구사항

- Java 17 이상
- Docker & Docker Compose
- Gradle 8.x 이상

### 1. 인프라 서비스 실행

```bash
# PostgreSQL, Redis, Kafka, Elasticsearch, Logstash 실행
docker-compose up -d
```

### 2. 서비스 빌드

```bash
# 전체 프로젝트 빌드
./gradlew build

# 테스트 제외 빌드
./gradlew build -x test
```

### 3. 서비스 실행 (순서 중요)

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

### 4. 서비스 접근

| 서비스 | URL |
|--------|-----|
| **API Gateway** | http://localhost:8000 |
| **Elasticsearch** | http://localhost:9200 |
| **Swagger UI** | http://localhost:8000/swagger-ui.html |

<br>

## 📚 주요 기능

### 1. 회원 및 인증
- JWT 기반 인증 (Access Token / Refresh Token)
- 역할 기반 권한 관리 (소비자, 판매자, 관리자)
- 이메일 인증을 통한 회원가입
- 판매자 등록 및 전환

### 2. 공동구매
- 판매자가 상품을 등록하고 공동구매 생성
- 소비자는 공동구매에 참여하여 할인된 가격으로 구매
- 공동구매 찜 기능으로 관심 상품 저장
- Spring Batch를 통한 공동구매 상태 자동 관리

### 3. 주문 및 결제
- 장바구니를 통한 다중 상품 주문
- 포인트 또는 PG 결제 (Toss Payments) 선택
- 주문 취소 요청 및 판매자 승인 프로세스
- 구매 확정 후 자동 정산

### 4. 실시간 검색
- Elasticsearch 기반 공동구매 전문 검색
- 한글 형태소 분석 (Nori Analyzer)
- 키워드, 카테고리, 상태별 필터링
- Kafka 이벤트를 통한 실시간 인덱싱

### 5. AI 추천
- 벡터 유사도 기반 상품 추천
- 사용자 행동 패턴 분석을 통한 맞춤형 추천
- 키워드 및 카테고리 기반 연관 상품 제안

### 6. 실시간 알림
- Kafka 이벤트 기반 비동기 알림 시스템
- 주문, 결제, 정산 등 주요 이벤트 알림
- 읽음/미읽음 상태 관리

### 7. 자동 정산
- Spring Batch를 통한 일일/월별 자동 정산
- 판매자 지급 처리 및 실패 시 재시도
- 판매자 잔액 실시간 조회


## ✅ API 명세

API 명세를 확인하려면 API 명세서를 참고하세요.

## 🧪 테스트

테스트 가이드를 확인하려면 테스트 가이드 문서를 참고하세요.

``` 
beadv2_2_superjunior_BE/
├── docs         
│   ├── test/       
│   │   ├── commerce.md
│   │   ├── elastic-search.md   
│   │   ├── notification.md     
└── └── └── point.md
```

- [commerce.md](https://github.com/prgrms-be-adv-devcourse/beadv2_2_superjunior_BE/blob/dev/docs/test/commerce.md)
- [elastic-search.md](https://github.com/prgrms-be-adv-devcourse/beadv2_2_superjunior_BE/blob/dev/docs/test/elastic-search.md)
- [notification.md](https://github.com/prgrms-be-adv-devcourse/beadv2_2_superjunior_BE/blob/dev/docs/test/notification.md) 
- [point.md](https://github.com/prgrms-be-adv-devcourse/beadv2_2_superjunior_BE/blob/dev/docs/test/point.md)

## 💬 기술적 고민

기술적 고민 사항을 확인하려면 기술적 고민 문서를 참고하세요.



## ❗ 트러블 슈팅

트러블 슈팅을 확인하려면 트러블 슈팅 문서를 참고하세요.
