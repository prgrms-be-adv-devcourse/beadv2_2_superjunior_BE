# Implementation Plan - 보너스 포인트 차감 및 환불 기능 구현

## Phase 1: 도메인 모델 및 리포지토리 보완 [checkpoint: 70536a0]
- [x] Task: `BonusEarning` 엔티티 조회 메서드 추가
    - [x] Sub-task: 유효기간(`expiresAt`) 오름차순으로 사용 가능한(`ACTIVE`, `PARTIALLY_USED`) 보너스 목록 조회 쿼리 메서드 작성 (`BonusEarningRepository`). [checkpoint: f88f7db]
- [x] Task: `BonusDeduction` 관련 리포지토리 기능 점검
    - [x] Sub-task: 환불 처리를 위해 트랜잭션 ID 또는 주문 ID로 차감 내역을 조회하는 기능 확인. [checkpoint: 70536a0]

## Phase 2: 비즈니스 로직 구현 (차감) [checkpoint: 70536a0]
- [x] Task: `BonusDeductionService` 구현
    - [x] Sub-task: 차감 요청 금액만큼 유효기간이 임박한 순서대로 `BonusEarning`을 차감하는 로직 작성. [checkpoint: 70536a0]
    - [x] Sub-task: 각 차감 건에 대해 `BonusDeduction` 엔티티 생성 및 저장. [checkpoint: 70536a0]
    - [x] Sub-task: `BonusEarning`의 상태(`PARTIALLY_USED`, `FULLY_USED`) 및 잔액(`remainingAmount`) 업데이트. [checkpoint: 70536a0]
- [x] Task: 단위 테스트 작성 (차감)
    - [x] Sub-task: 다양한 유효기간을 가진 보너스 포인트가 있을 때 순서대로 차감되는지 검증. [checkpoint: 70536a0]
    - [x] Sub-task: 보유 포인트보다 많은 금액 차감 시 예외 발생 검증. [checkpoint: 70536a0]

## Phase 3: 비즈니스 로직 구현 (환불) [checkpoint: 70536a0]
- [x] Task: `BonusRefundService` 구현
    - [x] Sub-task: 환불 요청(주문 취소) 시 원본 트랜잭션의 `BonusDeduction` 내역 조회. [checkpoint: 70536a0]
    - [x] Sub-task: 각 `BonusDeduction`에 연결된 `BonusEarning`의 잔액 복구. [checkpoint: 70536a0]
    - [x] Sub-task: 만료된 보너스 포인트에 대한 예외 처리 (재적립 불가 또는 만료 상태로 복구). [checkpoint: 70536a0]
- [x] Task: 단위 테스트 작성 (환불)
    - [x] Sub-task: 정상 환불 시 `BonusEarning` 잔액 및 상태 복구 검증. [checkpoint: 70536a0]
    - [x] Sub-task: 만료된 포인트 환불 시나리오 검증. [checkpoint: 70536a0]

## Phase 4: 통합 및 검증 [checkpoint: 70536a0]
- [x] Task: `PointTransactionUpdater` 통합
    - [x] Sub-task: 기존 `PointTransactionUpdater`의 `deductPoints` 및 `returnPoints` 메서드에서 `BonusDeductionService`와 `BonusRefundService`를 호출하도록 수정. [checkpoint: 70536a0]
- [x] Task: 통합 테스트 작성
    - [x] Sub-task: 적립 -> 차감 -> 환불 전체 시나리오에 대한 통합 테스트 수행. [checkpoint: 70536a0]
- [x] Task: Conductor - User Manual Verification '통합 및 검증' (Protocol in workflow.md) [checkpoint: 70536a0]
