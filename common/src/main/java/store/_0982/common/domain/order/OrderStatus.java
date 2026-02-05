package store._0982.common.domain.order;

import java.util.List;

public enum OrderStatus {
    PENDING,                // 주문 생성 (결제 대기)
    EXPIRED,                // 주문 만료 (결제 시간 초과)

    PAYMENT_COMPLETED,      // 결제 완료
    PAYMENT_FAILED,         // 결제 실패

    CANCELLED,               // 주문 취소
    CONFIRMED;               // 구매 확정

    public static List<OrderStatus> participantStatuses(){
        return List.of(
                PAYMENT_COMPLETED
        );
    }
}
