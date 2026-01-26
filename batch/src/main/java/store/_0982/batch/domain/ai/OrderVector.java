package store._0982.batch.domain.ai;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class OrderVector extends ProductVector {

    private UUID orderId;
    private OrderStatus status;
    private int quantity;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public OrderVector(
            UUID memberId,
            UUID productId,
            float[] vector,
            UUID orderId,
            OrderStatus status,
            int quantity,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            String description
    ) {
        super(memberId, productId, vector, description);
        this.orderId = orderId;
        this.status = status;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public enum OrderStatus {
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
