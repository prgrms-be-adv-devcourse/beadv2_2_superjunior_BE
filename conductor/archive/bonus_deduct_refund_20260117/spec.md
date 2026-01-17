# Track Specification: 보너스 포인트 차감 및 환불 기능 구현

## Overview
이 트랙은 `point` 모듈 내에서 보너스 포인트의 **차감(Deduction)**과 **환불(Refund)** 로직을 구현하는 것을 목표로 합니다. 기존에 구현된 적립(`Earning`) 기능과 연계하여, 사용자가 포인트를 사용할 때 유효기간이 임박한 보너스 포인트부터 차감되도록 하고, 주문 취소 시 사용된 보너스 포인트가 올바르게 환불(복구)되도록 합니다.

## Goals
1.  **보너스 차감 로직 구현:**
    -   포인트 사용 요청 시 유효기간이 가장 짧게 남은 보너스 포인트부터 우선 차감 (FIFO에 가까운 만료 우선 전략).
    -   `BonusDeduction` 엔티티를 통해 차감 내역을 상세 기록.
    -   `BonusEarning` 엔티티의 상태(`PARTIALLY_USED`, `FULLY_USED`) 업데이트.
2.  **보너스 환불 로직 구현:**
    -   주문 취소 시 해당 주문에서 사용된 보너스 포인트 내역(`BonusDeduction`)을 조회하여 환불 처리.
    -   환불 시 원래의 유효기간을 유지하되, 이미 만료된 포인트는 환불하지 않거나 정책에 따라 처리(기본: 만료된 포인트는 소멸 처리).
    -   `BonusEarning` 엔티티의 상태 복구(잔액 증가).

## Key Features
-   **유효기간 기반 우선 차감:** `expiresAt` 기준으로 오름차순 정렬하여 차감.
-   **차감 이력 추적:** 어떤 적립 건(`BonusEarning`)에서 얼마가 차감되었는지 `BonusDeduction`에 기록.
-   **환불 유효성 검증:** 환불 시점에 유효기간이 지난 보너스 포인트에 대한 처리 로직.

## Tech Stack
-   **Backend:** Java 17, Spring Boot 3.5.8, Spring Data JPA
-   **Database:** PostgreSQL (BonusEarning, BonusDeduction 테이블)

## Constraints
-   동시성 이슈를 고려하여 포인트 차감 및 상태 변경 시 락(Lock) 또는 낙관적 락(`@Version`)을 고려해야 함. (기존 PointBalance가 처리하는지 확인 필요)
-   `point` 모듈 내부 로직으로 구현하며, 외부 API 연동은 최소화.
