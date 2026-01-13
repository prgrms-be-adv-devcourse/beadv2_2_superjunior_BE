package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import store._0982.point.domain.constant.PointPaymentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "point_payment",
        schema = "payment_schema",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "point_payment_uc",
                        columnNames = {"order_id", "status"}
                )
        }
)
public class PointPayment {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, updatable = false)
    private PointPaymentStatus status;

    @Column(name = "paid_amount", nullable = false, updatable = false)
    private long paidAmount;

    @Column(name = "bonus_amount", nullable = false, updatable = false)
    private long bonusAmount;

    @CreationTimestamp
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "idempotency_key", nullable = false, updatable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "order_id", updatable = false)
    private UUID orderId;

    @Column(name = "cancel_reason")
    private String cancelReason;

    public static PointPayment charged(UUID memberId, UUID idempotencyKey, long paidAmount, long bonusAmount) {
        return PointPayment.builder()
                .memberId(memberId)
                .idempotencyKey(idempotencyKey)
                .status(PointPaymentStatus.CHARGED)
                .paidAmount(paidAmount)
                .bonusAmount(bonusAmount)
                .build();
    }

    public static PointPayment used(UUID memberId, UUID orderId, UUID idempotencyKey, long paidAmount, long bonusAmount) {
        return PointPayment.builder()
                .memberId(memberId)
                .idempotencyKey(idempotencyKey)
                .status(PointPaymentStatus.USED)
                .paidAmount(paidAmount)
                .bonusAmount(bonusAmount)
                .orderId(orderId)
                .build();
    }

    public static PointPayment returned(UUID memberId, UUID orderId, UUID idempotencyKey, long paidAmount, long bonusAmount, String cancelReason) {
        return PointPayment.builder()
                .memberId(memberId)
                .idempotencyKey(idempotencyKey)
                .status(PointPaymentStatus.RETURNED)
                .paidAmount(paidAmount)
                .bonusAmount(bonusAmount)
                .orderId(orderId)
                .cancelReason(cancelReason)
                .build();
    }

    public long getAmount() {
        return paidAmount + bonusAmount;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
