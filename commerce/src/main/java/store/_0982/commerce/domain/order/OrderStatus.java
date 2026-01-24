package store._0982.commerce.domain.order;

import java.util.List;

public enum OrderStatus {
    PENDING,                // 주문 생성 (결제 대기)

    PAYMENT_COMPLETED,      // 결제 완료
    PAYMENT_FAILED,         // 결제 실패

    ORDER_FAILED,           // 주문 실패

    GROUP_PURCHASE_SUCCESS, // 공동 구매 성공
    GROUP_PURCHASE_FAIL,    // 공동 구매 실패

    CANCEL_REQUESTED,       // 취소 요청
    CANCELLED,              // 취소 완료

    REVERSE_REQUESTED,      // 번복 요청
    REVERSED,               // 번복 완료

    REFUND_REQUESTED,       // 반품 요청
    REFUNDED,               // 반품 완료

    PURCHASE_CONFIRMED;     // 구매 확정

    public static List<OrderStatus> participantStatuses(){
        return List.of(
                PAYMENT_COMPLETED,
                GROUP_PURCHASE_SUCCESS,
                GROUP_PURCHASE_FAIL
        );
    }

}
