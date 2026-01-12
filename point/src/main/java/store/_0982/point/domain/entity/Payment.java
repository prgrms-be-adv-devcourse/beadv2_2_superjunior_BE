package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.constant.PaymentStatus;
import store._0982.point.exception.CustomErrorCode;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "payment", schema = "payment_schema")
public class Payment {

    private static final int REFUND_PERIOD_DAYS = 14;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "payment_key", nullable = false, unique = true)
    private String paymentKey;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

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

    private Payment(UUID memberId, UUID orderId, long amount) {
        this.id = UUID.randomUUID();
        this.memberId = memberId;
        this.orderId = orderId;
        this.amount = amount;
        this.requestedAt = OffsetDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public static Payment create(UUID memberId, UUID orderId, long amount) {
        return new Payment(memberId, orderId, amount);
    }

    public void markConfirmed(String method, OffsetDateTime approvedAt, String paymentKey) {
        this.status = PaymentStatus.COMPLETED;
        this.paymentMethod = method;
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.failMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = PaymentStatus.FAILED;
        this.failMessage = errorMessage;
    }

    public void markRefundPending() {
        this.status = PaymentStatus.REFUND_PENDING;
    }

    public void markRefunded(OffsetDateTime refundedAt, String cancelReason) {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = refundedAt;
        this.refundMessage = cancelReason;
    }

    public void validateCompletable(UUID memberId) {
        validateOwner(memberId);
        if (this.status != PaymentStatus.PENDING) {
            throw new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT);
        }
    }

    public void validateFailable(UUID memberId) {
        validateOwner(memberId);
        if (this.status != PaymentStatus.PENDING) {
            throw new CustomException(CustomErrorCode.CANNOT_HANDLE_FAILURE);
        }
    }

    public void validateRefundable(UUID memberId) {
        validateOwner(memberId);
        if (this.status == PaymentStatus.REFUNDED) {
            throw new CustomException(CustomErrorCode.ALREADY_REFUNDED_PAYMENT);
        }
        if (this.status != PaymentStatus.COMPLETED) {
            throw new CustomException(CustomErrorCode.NOT_COMPLETED_PAYMENT);
        }
        validateRefundTerms();
    }

    public void validateOwner(UUID memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new CustomException(CustomErrorCode.PAYMENT_OWNER_MISMATCH);
        }
    }

    // TODO: 기간을 세분화해서 부분 환불 비율을 결정하면 좋을 것 같다.
    private void validateRefundTerms() {
        if (approvedAt == null) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }

        // 결제일이 7일 이내일 경우 환불 가능
        if (Duration.between(approvedAt, OffsetDateTime.now()).toDays() > REFUND_PERIOD_DAYS) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }
    }
}
