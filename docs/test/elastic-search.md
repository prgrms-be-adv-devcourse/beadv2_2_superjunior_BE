# elastic-search 테스트 가이드

## 1. 테스트 실행 방법
- 전체 테스트: `./gradlew :elastic-search:test`
- 특정 테스트 클래스: `./gradlew :elastic-search:test --tests "*ProductSearchServiceTest"`
- 스모크 테스트(Elasticsearch Testcontainers): Docker 실행 필요
  - 예: `./gradlew :elastic-search:test --tests "*SearchSmokeTest"`
- 프로파일
  - `kafka`: Embedded Kafka 기반 이벤트 리스너 테스트
  - `search`: Testcontainers Elasticsearch 기반 스모크 테스트

## 2. 테스트 구조
- presentation: `@WebMvcTest` + MockMvc로 컨트롤러 요청/응답 검증
- application: Mockito 기반 서비스 단위 테스트
- infrastructure: QueryFactory 단위 테스트(쿼리 구성 로직 확인)
- smokesearch: Testcontainers Elasticsearch 연동 스모크 테스트
- context: 기본 애플리케이션 컨텍스트 로드 확인

## 3. 테스트 커버리지
- 인덱스 생성/삭제 동작 (상품, 공동구매)
- 검색 API 동작 (keyword, category, sellerId/상태 필터, 페이지네이션)
- QueryFactory 쿼리 분기 (match_all vs keyword, 필터 조합)
- Kafka 이벤트 소비 및 문서 저장/삭제 (상품, 공동구매)
- 필수 파라미터 누락 시 예외 처리 (sellerId null)
- 실제 Elasticsearch 연동 스모크 검증
- JaCoCo 커버리지 결과는 검색 서비스의 핵심 로직
  (QueryFactory, SearchService, EventListener)에 집중
- JaCoCo 리포트: `elastic-search/build/reports/jacoco/test/html/index.html`

## 4. 테스트 데이터 관리 (테스트 데이터용 DB 초기화 전략)
- 스모크 테스트는 매 테스트마다 인덱스 삭제/생성 후 문서 저장
- `ElasticsearchOperations`로 테스트 문서 삽입 후 `refresh()` 수행
- Kafka 이벤트 테스트는 Repository를 MockitoBean으로 대체해 영속 저장 없이 검증
- 단위 테스트는 모든 외부 의존성을 mock 처리

## 5. 주요 테스트 케이스 설명
- 상품 인덱스 생성/삭제 API 정상 응답
- 상품 검색: keyword+sellerId+category 조합, keyword 빈 값 처리
- sellerId 헤더 누락 시 인증/요청 실패 처리
- 공동구매 인덱스 생성/삭제 API 정상 응답
- 공동구매 전체 검색 및 판매자별 검색
- 상품/공동구매 Kafka 이벤트 수신 시 문서 저장/삭제 로직
- 쿼리 팩토리의 조건별 쿼리 문자열 구성 검증
- Elasticsearch 연동 시 실제 검색 결과 필터링 검증

## 6. 사용한 테스트 도구 / 라이브러리
- JUnit 5, AssertJ
- Mockito, Spring Boot Test
- MockMvc (`@WebMvcTest`)
- Embedded Kafka (`@EmbeddedKafka`)
- Testcontainers (Elasticsearch 8.11.1 + nori plugin)
- Spring Data Elasticsearch
- JaCoCo
