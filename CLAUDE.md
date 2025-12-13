# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Cloud microservices application for a group purchase e-commerce platform. The system consists of 9 services with event-driven architecture using Kafka for inter-service communication.

## Build and Development Commands

### Building the Project

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :member:build
./gradlew :product:build
./gradlew :order:build
./gradlew :point:build
./gradlew :notification:build
./gradlew :elastic-search:build
./gradlew :gateway:build
./gradlew :config:build
./gradlew :discovery:build
./gradlew :common:build

# Build without tests
./gradlew build -x test

# Clean and build
./gradlew clean build
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :member:test
./gradlew :notification:test
./gradlew :point:test

# Run specific test class
./gradlew :notification:test --tests "NotificationTest"

# Run tests with detailed output
./gradlew test --info

# Run tests continuously (watch mode)
./gradlew test --continuous
```

### Running Services

```bash
# Start infrastructure dependencies
docker-compose up -d

# Run specific service (from service directory)
./gradlew :config:bootRun
./gradlew :discovery:bootRun
./gradlew :gateway:bootRun
./gradlew :member:bootRun
./gradlew :product:bootRun
./gradlew :order:bootRun
./gradlew :point:bootRun
./gradlew :notification:bootRun
./gradlew :elastic-search:bootRun
```

### Service Startup Order

1. Start infrastructure: `docker-compose up -d` (PostgreSQL, Redis, Kafka, Elasticsearch, Logstash)
2. Start Config Server (port 8888)
3. Start Discovery Server (port 8761)
4. Start Gateway (port 8000)
5. Start business services in any order

## Architecture Overview

### Microservices

**Infrastructure Services:**
- **config** (port 8888): Spring Cloud Config Server for centralized configuration
- **discovery** (port 8761): Netflix Eureka service registry
- **gateway** (port 8000): Spring Cloud Gateway with JWT authentication

**Business Services:**
- **member** (port 8083): Authentication, user management, JWT token generation
- **product** (port 8087): Product catalog and group purchase management
- **order** (port 8085): Order processing and settlement automation
- **point** (port 8086): Point recharge/payment via Toss Payments API
- **notification** (port 8084): Notification management via Kafka events
- **elastic-search** (port 8082): Product and group purchase search indexing

**Shared Module:**
- **common**: Shared DTOs, Kafka configurations, exception handling, logging aspects

### Communication Patterns

**Synchronous (REST):**
- All external requests route through Gateway (port 8000)
- Feign clients for inter-service communication:
  - Order → Product: Settlement queries
  - Product → Member: Profile lookups via Gateway

**Asynchronous (Kafka):**
- Event-driven communication between services
- Topics defined in `common/kafka/config/KafkaTopics.java`:
  - `order.created`, `order.changed`
  - `point.recharged`, `point.changed`
  - `product.upserted`, `product.deleted`
  - `group-purchase.added`, `group-purchase.changed`
  - `settlement.daily.completed`, `settlement.daily.failed`
  - `settlement.monthly.completed`, `settlement.monthly.failed`

### Data Storage

**PostgreSQL (port 5433):**
- Schema-based isolation per service:
  - `member_schema`: Member service
  - `point_schema`: Point service
  - `notification_schema`: Notification service
  - `product` schema: Product and GroupPurchase tables
  - `order` schema: Order tables

**Redis (port 6379):**
- Used by Member service for session management and caching

**Elasticsearch (port 9200):**
- Product and group purchase search indices
- Korean text search using Nori analyzer
- Real-time sync via Kafka events

**Kafka (port 9092):**
- KRaft mode (no Zookeeper)
- Event streaming between services

### Authentication Flow

1. User authenticates via Member service
2. JWT access token and refresh token issued as HTTP-only cookies
3. Gateway validates JWT on each request
4. Gateway injects user context headers for downstream services:
   - `X-Member-Id`
   - `X-Member-Email`
   - `X-Member-Role`
5. Services read headers to identify authenticated user

### Event-Driven Architecture

**Publishing Events:**
- Events published AFTER database commit
- Message key = entity ID (for partition ordering)
- Domain entities have `toEvent()` methods
- Example: Product service publishes to `product.upserted` after creating/updating products

**Consuming Events:**
- `@KafkaListener` annotations on event handlers
- `@RetryableTopic` for automatic retry on failure
- Consumer groups isolate service instances
- Example: Notification service listens to order, point, and settlement events

### Key Domain Relationships

- **GroupPurchase** references **Product** via `productId`
- **Order** references **Member**, **Seller**, and **GroupPurchase**
- **Notification** references entities via `referenceType` and `referenceId`
- **MemberPoint** tracks point balance per member
- **PaymentPoint** tracks individual payment transactions

### Optimistic Locking

Group purchase participation uses optimistic locking with version field to handle concurrent updates:
```java
@Version
private Long version;
```

### Settlement Process

- Daily settlement runs at 1:00 AM Asia/Seoul timezone
- Monthly settlement runs separately
- Settlement jobs mark group purchases as settled
- Results published to Kafka for notifications

### Testing Conventions

- Use JUnit 5 and AssertJ for assertions
- `@DisplayName` annotations describe test cases in Korean
- Domain tests focus on business logic without Spring context
- Service tests use mocking for dependencies
- Example test structure:
  ```java
  @Test
  @DisplayName("알림을 읽음 상태로 변경할 수 있다")
  void read_success() {
      // given, when, then
  }
  ```

### Configuration Management

- Centralized configuration in Config Server
- Environment variables in `.env` file (not committed to git):
  - Database credentials
  - JWT secret key
  - Toss Payments API key
- Service-specific configs in `config/repo/` directory

### Logging

- Custom `@ServiceLog` annotation for AOP-based logging
- Logstash integration (port 5000) for log aggregation
- Logs sent to Elasticsearch via Logstash

### Error Handling

- Custom exception hierarchy per service
- `@RestControllerAdvice` for global exception handling
- Base exception handler in common module
- Consistent error response format across services

## Important Notes

- The common module is a shared library (jar), not a standalone service
- Config and Discovery services exclude Spring Cloud Config and Eureka Client dependencies
- All services use Java 17
- Spring Cloud version: 2025.0.0
- Spring Boot version: 3.5.8
- Database DDL strategy varies by service (update, validate, none)
- Kafka auto-create topics is disabled - topics must be created manually