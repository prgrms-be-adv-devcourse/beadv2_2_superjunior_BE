package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.PaymentRules;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.vo.PaymentMethodDetail;
import store._0982.point.exception.CustomErrorCode;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "pg_payment", schema = "payment_schema")
public class PgPayment {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Embedded
    private PaymentMethodDetail paymentMethodDetail;

    @Column(name = "payment_key", unique = true)
    private String paymentKey;

    private String transactionKey;

    @Column(nullable = false)
    private long amount;

    private Long refundedAmount;

    private String groupPurchaseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PgPaymentStatus status;

    @Column(length = 2048)
    private String receiptUrl;

    @CreationTimestamp
    @ColumnDefault("now()")
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

    // TODO: 아직 미구현
    @Column(nullable = false)
    private boolean isPartialCancelable;

    @Version
    @Column(nullable = false)
    @ColumnDefault("0")
    private Long version;

    public static PgPayment create(UUID memberId, UUID orderId, long amount, String groupPurchaseName) {
        return PgPayment.builder()
                .memberId(memberId)
                .orderId(orderId)
                .amount(amount)
                .groupPurchaseName(groupPurchaseName)
                .requestedAt(OffsetDateTime.now())  // TODO: 요청 시각도 따로 정보를 받아야 할 것 같다
                .status(PgPaymentStatus.PENDING)
                .build();
    }

    public void markConfirmed(PaymentMethod method, OffsetDateTime approvedAt, String paymentKey) {
        this.status = PgPaymentStatus.COMPLETED;
        this.paymentMethod = method;
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
    }

    public void markConfirmed(PaymentMethod method, PaymentMethodDetail paymentMethodDetail, OffsetDateTime approvedAt, String paymentKey) {
        markConfirmed(method, approvedAt, paymentKey);
        this.paymentMethodDetail = paymentMethodDetail;
    }

    public void markFailed(String paymentKey) {
        this.paymentKey = paymentKey;
        this.status = PgPaymentStatus.FAILED;
    }

    public void markRefunded(OffsetDateTime refundedAt) {
        this.status = PgPaymentStatus.REFUNDED;
        this.refundedAt = refundedAt;
        this.refundedAmount = amount;
    }

    public void applyRefund(long newRefundAmount, OffsetDateTime refundedAt) {
        long currentRefunded = (refundedAmount == null) ? 0L : refundedAmount;
        long totalRefunded = currentRefunded + newRefundAmount;

        if (totalRefunded > amount) {
            throw new CustomException(CustomErrorCode.INVALID_REFUND_AMOUNT);
        }

        if (totalRefunded == amount) {
            markRefunded(refundedAt);
            return;
        }

        this.status = PgPaymentStatus.PARTIALLY_REFUNDED;
        this.refundedAt = refundedAt;
        this.refundedAmount = totalRefunded;
    }

    public void validateCompletable(UUID memberId) {
        validateOwner(memberId);
        if (this.status != PgPaymentStatus.PENDING) {
            throw new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT);
        }
    }

    public void validateFailable(UUID memberId) {
        validateOwner(memberId);
        if (this.status != PgPaymentStatus.PENDING) {
            throw new CustomException(CustomErrorCode.CANNOT_HANDLE_FAILURE);
        }
    }

    public void validateRefundable(UUID memberId, PaymentRules rules) {
        validateOwner(memberId);
        if (this.status == PgPaymentStatus.REFUNDED) {
            throw new CustomException(CustomErrorCode.ALREADY_REFUNDED_PAYMENT);
        }
        if (this.status != PgPaymentStatus.COMPLETED && this.status != PgPaymentStatus.PARTIALLY_REFUNDED) {
            throw new CustomException(CustomErrorCode.NOT_COMPLETED_PAYMENT);
        }
        validateRefundTerms(rules);
    }

    public void validateOwner(UUID memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new CustomException(CustomErrorCode.PAYMENT_OWNER_MISMATCH);
        }
    }

    private void validateRefundTerms(PaymentRules rules) {
        if (approvedAt == null) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }

        // 결제일이 설정된 환불 기간 이내일 경우 환불 가능
        if (Duration.between(approvedAt, OffsetDateTime.now()).toDays() > rules.getRefundDays()) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
