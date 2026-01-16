package store._0982.ai.feign.commerce.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderInfo(
        UUID orderId,
        OrderStatus status,
        int quantity,
        Long price,
        Long totalAmount,
        OffsetDateTime createdAt
) {
    private enum OrderStatus {
        PENDING,                // 주문 생성 (결제 대기)
        PAYMENT_COMPLETED,      // 결제 완료
        ORDER_FAILED,           // 주문 실패
        PAYMENT_FAILED,         // 결제 실패
        CANCELLED,              // 주문 취소
        GROUP_PURCHASE_SUCCESS, // 공동 구매 성공
        GROUP_PURCHASE_FAIL,    // 공동 구매 실패
        REVERSED,               // 결제 취소
        RETURNED                // 환불 완료
    }
}
