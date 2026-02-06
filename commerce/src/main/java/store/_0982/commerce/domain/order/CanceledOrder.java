package store._0982.commerce.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.order.PaymentMethod;
import store._0982.common.exception.CustomException;
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

    @Column(name = "original_paid_amount", nullable = false)
    private Long originalPaidAmount;    // 취소 전 실제 결제액

    @Column(name = "cancel_fee_amount", nullable = false)
    private Long cancelFeeAmount;       // 취소 수수료

    @Column(name = "shipping_fee_amount", nullable = false)
    private Long shippingFeeAmount;     // 배송비

    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount;          // 최종 환불액

    @Column(name = "policy_id", nullable = false, length = 60)
    private String policyId;            // 취소 정책

    @Column(name = "policy_snapshot", columnDefinition = "text")
    private String policySnapshot;      // 취소 정책 스냅샷 - JSON 문자열

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CancelStatus status;

    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    private CancelReason reason;

    @Column(name = "detail_reason")
    private String detailReason;

    @Column(name = "idempotency_key", unique = true, nullable = false, updatable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt; // 취소 요청 시간

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt; // 환불 완료 시간

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public void markCompleted() {
        this.returnedAt = OffsetDateTime.now();
        this.status = CancelStatus.COMPLETED;
    }

    public void markApproved() {
        if (this.status != CancelStatus.PENDING) {
            throw new CustomException(CustomErrorCode.INVALID_CANCEL_STATUS);
        }
        this.status = CancelStatus.APPROVED;
    }

    public void markRejected() {
        if (this.status != CancelStatus.PENDING) {
            throw new CustomException(CustomErrorCode.INVALID_CANCEL_STATUS);
        }
        this.status = CancelStatus.REJECTED;
    }

    public static CanceledOrder createCanceledOrder(
            UUID orderId,
            UUID memberId,
            long originalPaidAmount,
            long cancelFeeAmount,
            long shippingFeeAmount,
            long refundAmount,
            String policyId,
            String policySnapshot,
            CancelStatus status,
            CancelReason reason,
            String detailReason,
            String idempotencyKey,
            PaymentMethod paymentMethod) {
        return new CanceledOrder(
                orderId,
                memberId,
                originalPaidAmount,
                cancelFeeAmount,
                shippingFeeAmount,
                refundAmount,
                policyId,
                policySnapshot,
                status,
                reason,
                detailReason,
                idempotencyKey,
                paymentMethod);
    }

    private CanceledOrder(
            UUID orderId,
            UUID memberId,
            long originalPaidAmount,
            long cancelFeeAmount,
            long shippingFeeAmount,
            long refundAmount,
            String policyId,
            String policySnapshot,
            CancelStatus status,
            CancelReason reason,
            String detailReason,
            String idempotencyKey,
            PaymentMethod paymentMethod) {
        this.canceledOrderId = UUID.randomUUID();
        this.orderId = orderId;
        this.memberId = memberId;
        this.originalPaidAmount = originalPaidAmount;
        this.cancelFeeAmount = cancelFeeAmount;
        this.shippingFeeAmount = shippingFeeAmount;
        this.refundAmount = refundAmount;
        this.policyId = policyId;
        this.policySnapshot = policySnapshot;
        this.status = status;
        this.reason = reason;
        this.detailReason = detailReason;
        this.idempotencyKey = idempotencyKey;
        this.paymentMethod = paymentMethod;
        this.canceledAt = OffsetDateTime.now();
    }

    public OrderCanceledEvent toEvent(String productName, OrderCanceledEvent.PaymentMethod method) {
        return new OrderCanceledEvent(
                this.memberId,
                this.orderId,
                productName,
                this.detailReason,
                method,
                this.refundAmount
        );
    }
}
