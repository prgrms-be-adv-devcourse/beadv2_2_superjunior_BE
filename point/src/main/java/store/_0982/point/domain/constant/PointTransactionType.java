package store._0982.point.domain.constant;

public enum PointTransactionType {
    CHARGE,     // 포인트 충전
    USE,        // 포인트 사용 (상품 구매)
    REFUND,     // 포인트 환불 (사용 취소)
    EARN_BONUS, // 보너스 적립
    EXPIRE      // 포인트 소멸
}
