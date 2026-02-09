package store._0982.common.domain.order;

public enum CancelStatus {
    REQUESTED,   // 취소 요청

    PENDING,     // 판매자 승인 대기
    APPROVED,    // 승인
    REJECTED,    // 거부

    COMPLETED    // 환불 완료
}
