package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.constant.PaymentPointStatus;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "payment_point", schema = "point_schema")
public class PaymentPoint {

    @Id
    @Column(name = "payment_point_id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "pg_order_id", nullable = false, unique = true)
    private UUID pgOrderId;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "payment_key", nullable = false, length = 50)
    private String paymentKey;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentPointStatus status;

    @Column(name = "fail_message")
    private String failMessage;

    @Column(name = "refund_message")
    private String refundMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "requested_at")
    private OffsetDateTime requestedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "refunded_at")
    private OffsetDateTime refundedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    private PaymentPoint(UUID memberId, UUID pgOrderId, long amount){
        this.id = UUID.randomUUID();
        this.memberId = memberId;
        this.pgOrderId = pgOrderId;
        this.amount = amount;
        this.requestedAt = OffsetDateTime.now();
        this.status = PaymentPointStatus.REQUESTED;
    }

    public static PaymentPoint create(UUID memberId, UUID orderId, long amount){
        return new PaymentPoint(memberId, orderId, amount);
    }

    public void markConfirmed(String method, OffsetDateTime approvedAt, String paymentKey) {
        this.status = PaymentPointStatus.COMPLETED;
        this.paymentMethod = method;
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.failMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = PaymentPointStatus.FAILED;
        this.failMessage = errorMessage;
    }

    public void markRefunded(OffsetDateTime refundedAt, String cancelReason) {
        this.status = PaymentPointStatus.REFUNDED;
        this.refundedAt = refundedAt;
        this.refundMessage = cancelReason;
    }

    public void validate(UUID memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new CustomException(CustomErrorCode.PAYMENT_OWNER_MISMATCH);
        }
    }
}
