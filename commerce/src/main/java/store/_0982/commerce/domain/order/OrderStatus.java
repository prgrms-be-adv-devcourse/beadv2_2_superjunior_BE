package store._0982.commerce.domain.order;

public enum OrderStatus {
    IN_PROGRESS,
    // 생성, 주문 성공
    PENDING, // 주문 생성됨, 외부 작업 전
    CONFIRMED, // 결제 + 참여 성공

    // 공동구매 종료 기준
    SUCCESS, // 공동구매 성공
    FAILED, // 공공구매 실패

    // 주문 실패 시 보상
    CANCELLED // 주문 실패로 취소됨
}
