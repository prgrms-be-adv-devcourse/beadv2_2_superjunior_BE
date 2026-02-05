# 기술 스택 (Technology Stack)

이 문서는 프로젝트에서 사용되는 모든 핵심 기술과 도구를 정의합니다.

## 1. 백엔드 (Backend)
- **언어:** Java 17
- **프레임워크:** Spring Boot 3.5.8
- **MSA 인프라:** Spring Cloud (Gateway, Eureka)
- **데이터 접근:** Spring Data JPA
- **배치 작업:** Spring Batch

## 2. 데이터 및 메시징 (Data & Messaging)
- **주 데이터베이스:** PostgreSQL 15
- **캐시 및 세션:** Redis 7
- **검색 엔진:** Elasticsearch 8.18
- **메시지 브로커:** Apache Kafka

## 3. 로깅 및 모니터링 (Logging & Monitoring)
- **로그 수집:** Fluent Bit (도입 예정)
- **로그 저장:** Elasticsearch

## 4. 인프라 및 배포 (Infrastructure & Deployment)
- **오케스트레이션:** k3s (운영 및 스테이징)
- **로컬 개발 환경:** Docker, Docker Compose
- **빌드 도구:** Gradle 8.x
