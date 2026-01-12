# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Cloud microservices application for a group purchase e-commerce platform (공구공구). The system consists of 7 active services with event-driven architecture using Kafka for inter-service communication.

## Build and Development Commands

### Building the Project

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :member:build
./gradlew :commerce:build
./gradlew :point:build
./gradlew :batch:build
./gradlew :elastic-search:build
./gradlew :gateway:build
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
./gradlew :commerce:test
./gradlew :point:test
./gradlew :batch:test

# Run specific test class
./gradlew :point:test --tests "MemberPointServiceConcurrencyTest"

# Run tests with detailed output
./gradlew test --info

# Run tests continuously (watch mode)
./gradlew test --continuous

# Run tests without daemon (for CI/CD or troubleshooting)
./gradlew :point:test --no-daemon
```

### Running Services

```bash
# Start infrastructure dependencies
docker-compose up -d

# Run specific service
./gradlew :discovery:bootRun
./gradlew :gateway:bootRun
./gradlew :member:bootRun
./gradlew :commerce:bootRun
./gradlew :point:bootRun
./gradlew :batch:bootRun
./gradlew :elastic-search:bootRun
```

### Service Startup Order

1. Start infrastructure: `docker-compose up -d` (PostgreSQL, Redis, Kafka, Elasticsearch, Logstash)
2. Start Discovery Server (port 8761)
3. Start Gateway (port 8000)
4. Start business services in any order (member, commerce, point, batch, elastic-search)

## Architecture Overview

### Microservices

**Infrastructure Services:**
- **discovery** (port 8761): Netflix Eureka service registry
- **gateway** (port 8000): Spring Cloud Gateway with JWT authentication and internal service authentication

**Business Services:**
- **member** (port 8083): Authentication, user management, JWT token generation, notification management
- **commerce** (port 8087): Product catalog, group purchase management, order processing, cart, seller balance
- **point** (port 8086): Point recharge/payment via Toss Payments API, point usage/refund with concurrency control
- **batch** (port 8085): Daily/monthly settlement automation, group purchase status management
- **elastic-search** (port 8082): Product and group purchase search indexing with Nori analyzer

**Shared Module:**
- **common**: Shared DTOs, Kafka configurations, exception handling, logging aspects

**Notes:**
- **config** service is disabled (commented out in settings.gradle)
- **notification** functionality is integrated into **member** service
- **product** and **order** services are merged into **commerce** service

### Communication Patterns

**Synchronous (REST):**
- All external requests route through Gateway (port 8000)
- Feign clients for inter-service communication:
  - Point → Commerce: Order information queries via `OrderServiceClient`
  - Batch → Member, Commerce, Point: Settlement data retrieval
  - All inter-service calls route through Gateway

**Asynchronous (Kafka):**
- Event-driven communication between services
- Topics defined in `common/src/main/java/store/_0982/common/kafka/KafkaTopics.java`:
  - `order.created`, `order.changed`
  - `point.recharged`, `point.changed`
  - `product.upserted`, `product.deleted`
  - `group-purchase.created`, `group-purchase.changed`, `group-purchase.update`
  - `group-purchase.added` (Deprecated - will be removed)
  - `settlement.daily.completed`, `settlement.daily.failed`
  - `settlement.monthly.completed`, `settlement.monthly.failed`

### Data Storage

**PostgreSQL (port 5433):**
- Schema-based isolation per service:
  - `member_schema`: Member service (users, sellers, admins)
  - `notification_schema`: Notification service (integrated in member)
  - `point_schema`: Point service (member points, payment transactions)
  - `product` schema: Commerce service (products, group purchases)
  - `order` schema: Commerce service (orders, carts)
  - `batch_order_schema`: Batch service (Spring Batch metadata for order processing)
  - `batch_product_schema`: Batch service (Spring Batch metadata for settlement)

**Redis (port 6379):**
- Used by Member service for session management and caching

**Elasticsearch (port 9200):**
- Product and group purchase search indices
- Korean text search using Nori analyzer
- Real-time sync via Kafka events
- Custom Docker image built with Nori plugin

**Kafka (port 9092):**
- KRaft mode (no Zookeeper)
- Event streaming between services
- Auto-create topics is disabled - topics must be created manually

### Authentication Flow

1. User authenticates via Member service
2. JWT access token and refresh token issued as HTTP-only cookies
3. Gateway validates JWT on each request
4. Gateway injects user context headers for downstream services:
   - `X-Member-Id`
   - `X-Member-Email`
   - `X-Member-Role`
5. Services read headers to identify authenticated user
6. Internal service endpoints (`/internal/**`) use separate token authentication

### Event-Driven Architecture

**Publishing Events:**
- Events published AFTER database commit
- Message key = entity ID (for partition ordering)
- Domain entities have `toEvent()` methods
- Example: Commerce service publishes to `product.upserted` after creating/updating products

**Consuming Events:**
- `@KafkaListener` annotations on event handlers
- `@RetryableTopic` for automatic retry on failure
- Consumer groups isolate service instances
- Examples:
  - Member service listens to order, point, settlement, and group purchase events for notifications
  - Elastic-search service listens to product and group purchase events for indexing

### Key Domain Relationships

- **GroupPurchase** references **Product** via `productId`
- **Order** references **Member**, **Seller**, and **GroupPurchase**
- **Notification** references entities via `referenceType` and `referenceId`
- **MemberPoint** tracks point balance per member
- **PaymentPoint** tracks individual payment/refund transactions
- **Cart** references **Member** and **GroupPurchase**
- **SellerBalance** tracks seller earnings and withdrawals

### Concurrency Control

**Optimistic Locking:**
- Group purchase participation uses `@Version` field to handle concurrent updates:
  ```java
  @Version
  private Long version;
  ```

**Idempotency Key Pattern:**
- Point service uses idempotency keys to prevent duplicate requests
- Checked before processing point usage/refund operations
- Example: `existsByIdempotencyKey()` in `MemberPointService.java:111`

**Database Constraints:**
- Unique constraints on critical fields (e.g., order_id + status)
- `DataIntegrityViolationException` handling for graceful failure
- `saveAndFlush()` used to detect constraint violations immediately

### Settlement Process

- Daily settlement runs at 1:00 AM Asia/Seoul timezone
- Monthly settlement runs separately
- Batch service marks group purchases as settled
- Results published to Kafka for notifications
- Spring Batch framework for job management

### Testing Conventions

**Test Structure:**
- Use JUnit 5 and AssertJ for assertions
- `@DisplayName` annotations describe test cases in Korean
- Given-When-Then pattern for test organization
- Example:
  ```java
  @Test
  @DisplayName("알림을 읽음 상태로 변경할 수 있다")
  void read_success() {
      // given, when, then
  }
  ```

**Test Types:**
- Domain tests: Business logic without Spring context
- Service tests: Mocking for dependencies
- Integration tests: TestContainers for database
- Concurrency tests: Multi-threaded scenarios

**Test Infrastructure (Point Service):**
- **BaseIntegrationTest**: Base class for integration tests
  - TestContainers with PostgreSQL 15-alpine
  - EmbeddedKafka integration
  - Located at: `point/src/test/java/store/_0982/point/support/BaseIntegrationTest.java`

- **BaseConcurrencyTest**: Base class for concurrency tests
  - ExecutorService and CountDownLatch utilities
  - Helper methods: `runSynchronizedTask()`, `runSynchronizedTasks()`
  - Default 10 threads for concurrent execution
  - Located at: `point/src/test/java/store/_0982/point/support/BaseConcurrencyTest.java`

**Concurrency Test Examples:**
- `MemberPointServiceConcurrencyTest.java`: Point deduction/refund concurrency
- `PaymentPointServiceConcurrencyTest.java`: Point recharge concurrency
- `RefundServiceConcurrencyTest.java`: Refund concurrency

**Recent Testing Improvements:**
- Migrated from H2 to TestContainers (PostgreSQL) for production parity
- Introduced BaseConcurrencyTest framework for systematic concurrency testing
- Removed test-only methods from production code
- Added read-only transaction checks for query methods

### Configuration Management

- No centralized Config Server (disabled)
- Environment variables in `.env` file (not committed to git):
  - Database credentials
  - JWT secret key
  - Toss Payments API key
  - Internal token secret
- Service-specific configs in `src/main/resources/application.yml`

### Logging

- Custom `@ServiceLog` and `@ControllerLog` annotations for AOP-based logging
- Logstash integration (port 5000) for log aggregation
- Logs sent to Elasticsearch via Logstash
- OpenTelemetry for distributed tracing (OTLP exporter)
- W3C trace context propagation
- Sampling rate: 100%

### Monitoring

**Actuator Endpoints** (all services):
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

**Distributed Tracing:**
- OpenTelemetry integration
- OTLP exporter configured
- Trace context propagation across services

### Error Handling

- Custom exception hierarchy per service
- `@RestControllerAdvice` for global exception handling
- Base exception handler in common module
- Consistent error response format across services
- HTTP status codes follow REST conventions

### Swagger/OpenAPI

- Swagger UI integrated via Gateway
- All service APIs documented
- Accessible through Gateway routes

## Important Notes

### Module Configuration
- The **common** module is a shared library (JAR), not a standalone service
- **Discovery** service excludes Eureka Client dependency (it's the server)
- **Config** service is disabled and not in use
- **Notification** is not a separate service - it's part of **member** service

### Technology Stack
- Java 17 (toolchain)
- Spring Boot 3.5.8
- Spring Cloud 2025.0.0
- Gradle 8.x
- PostgreSQL 15-alpine
- Kafka 7.5.0 (KRaft mode)
- Redis 7-alpine
- Elasticsearch 8.18.8 (custom build with Nori plugin)

### Database DDL Strategy
- Member: `validate`
- Point: `none` (production), `create` (test)
- Commerce: `none`
- Batch: `none`
- Migration scripts should be managed manually

### Kafka Configuration
- Auto-create topics is disabled
- Topics must be created manually or via init scripts
- KRaft mode (no Zookeeper dependency)
- Replication factor: 1 (single broker)

### Docker Infrastructure
- All services use custom Docker network: `group-purchase-network`
- PostgreSQL exposed on host port 5433 (container port 5432)
- Health checks configured for all infrastructure services
- Elasticsearch built from custom Dockerfile with Nori plugin

### Best Practices
- Always use TestContainers for integration tests (not H2)
- Use `@Version` for optimistic locking on concurrent updates
- Implement idempotency keys for critical operations
- Publish Kafka events AFTER database commits
- Use Feign clients for inter-service REST communication
- Route all inter-service calls through Gateway
- Handle `DataIntegrityViolationException` for constraint violations
- Write concurrency tests for critical business logic

### File Locations
- Kafka topics: `common/src/main/java/store/_0982/common/kafka/KafkaTopics.java`
- Test support classes: `{service}/src/test/java/store/_0982/{service}/support/`
- Application configs: `{service}/src/main/resources/application.yml`
- Docker init scripts: `docker/postgres/init/`
- Logstash pipeline: `docker/logstash/pipeline/`