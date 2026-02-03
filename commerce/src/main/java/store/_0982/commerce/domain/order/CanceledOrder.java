package store._0982.commerce.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.kafka.dto.OrderCanceledEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "\"canceled_order\"", schema = "order_schema")
public class CanceledOrder {

    @Id
    @Column(name = "cancel_order_id", nullable = false, updatable = false, unique = true)
    private UUID canceledOrderId;

    @Column(name = "order_id", nullable = false, updatable = false, unique = true)
    private UUID orderId;

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "returned_amount", nullable = false)
    private Long returnedAmount;

    @Column(name = "fee", nullable = false)
    private Long fee;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CancelStatus status;

    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    private CancelReason reason;

    @Column(name = "detail_reason")
    private String detailReason;

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "cancel_requested_at")
    private OffsetDateTime canceledAt; // 취소 요청 시간

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt; // 환불 완료 시간

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public static CanceledOrder createCanceledOrder(
            UUID orderId,
            UUID memberId,
            Long returnedAmount,
            Long fee,
            CancelReason reason,
            String detailReason,
            String idempotencyKey,
            PaymentMethod paymentMethod) {
        return new CanceledOrder(
                orderId,
                memberId,
                returnedAmount,
                fee,
                reason,
                detailReason,
                idempotencyKey,
                paymentMethod);
    }

    private CanceledOrder(
            UUID orderId,
            UUID memberId,
            Long returnedAmount,
            Long fee,
            CancelReason reason,
            String detailReason,
            String idempotencyKey,
            PaymentMethod paymentMethod) {
        this.canceledOrderId = UUID.randomUUID();
        this.orderId = orderId;
        this.memberId = memberId;
        this.returnedAmount = returnedAmount;
        this.fee = fee;
        this.status = CancelStatus.REQUESTED;
        this.reason = reason;
        this.detailReason = detailReason;
        this.idempotencyKey = idempotencyKey;
        this.paymentMethod = paymentMethod;
        this.canceledAt = OffsetDateTime.now();
    }

    public OrderCanceledEvent toEvent(String productName, String cancelReason, OrderCanceledEvent.PaymentMethod method, Long amount) {
        return new OrderCanceledEvent(
                this.memberId,
                this.orderId,
                productName,
                cancelReason,
                method,
                amount
        );
    }
}
