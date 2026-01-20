package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "point_transaction",
        schema = "payment_schema",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_point_transaction",
                        columnNames = {"order_id", "status"}
                )
        }
)
public class PointTransaction {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, updatable = false)
    private PointTransactionStatus status;

    @Embedded
    @AttributeOverride(name = "paidPoint", column = @Column(name = "paid_amount", nullable = false, updatable = false))
    @AttributeOverride(name = "bonusPoint", column = @Column(name = "bonus_amount", nullable = false, updatable = false))
    private PointAmount pointAmount;

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

    public static PointTransaction charged(UUID memberId, UUID idempotencyKey, PointAmount amount) {
        return PointTransaction.builder()
                .memberId(memberId)
                .idempotencyKey(idempotencyKey)
                .status(PointTransactionStatus.CHARGED)
                .pointAmount(amount)
                .build();
    }

    // 보너스 적립의 경우에는 orderId가 있을 수도 있고, 없을 수도 있음
    public static PointTransaction bonusEarned(UUID memberId, UUID orderId, UUID idempotencyKey, PointAmount amount) {
        return PointTransaction.builder()
                .memberId(memberId)
                .orderId(orderId)
                .idempotencyKey(idempotencyKey)
                .status(PointTransactionStatus.BONUS_EARNED)
                .pointAmount(amount)
                .build();
    }

    public static PointTransaction used(UUID memberId, UUID orderId, UUID idempotencyKey, PointAmount amount) {
        return PointTransaction.builder()
                .memberId(memberId)
                .idempotencyKey(idempotencyKey)
                .status(PointTransactionStatus.USED)
                .pointAmount(amount)
                .orderId(orderId)
                .build();
    }

    public static PointTransaction transferred(UUID memberId, UUID idempotencyKey, PointAmount amount) {
        return PointTransaction.builder()
                .memberId(memberId)
                .idempotencyKey(idempotencyKey)
                .status(PointTransactionStatus.TRANSFERRED)
                .pointAmount(amount)
                .build();
    }

    public static PointTransaction returned(UUID memberId, UUID orderId, UUID idempotencyKey, PointAmount amount, String cancelReason) {
        return PointTransaction.builder()
                .memberId(memberId)
                .idempotencyKey(idempotencyKey)
                .status(PointTransactionStatus.RETURNED)
                .pointAmount(amount)
                .orderId(orderId)
                .cancelReason(cancelReason)
                .build();
    }

    public PointAmount calculateRefund(long amount) {
        return pointAmount.calculateRefund(amount);
    }

    public long getPaidAmount() {
        return pointAmount.getPaidPoint();
    }

    public long getBonusAmount() {
        return pointAmount.getBonusPoint();
    }

    public long getTotalAmount() {
        return pointAmount.getTotal();
    }

    public void validateOwner(UUID memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new CustomException(CustomErrorCode.PAYMENT_OWNER_MISMATCH);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
