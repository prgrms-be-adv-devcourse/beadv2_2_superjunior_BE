package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.constant.BonusEarningType;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "bonus_earning", schema = "payment_schema")
public class BonusEarning {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private BonusEarningType type;

    @Column(name = "policy_id", updatable = false)
    private UUID policyId;

    @Column(name = "order_id", updatable = false)
    private UUID orderId;

    @Column(name = "reference_id", updatable = false)
    private UUID referenceId;

    @Column(name = "description", updatable = false)
    private String description;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private OffsetDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BonusEarningStatus status;

    @CreationTimestamp
    @Column(name = "earned_at", nullable = false, updatable = false)
    private OffsetDateTime earnedAt;

    public static BonusEarning earned(UUID memberId, long amount, BonusEarningType type,
                                       OffsetDateTime expiresAt, UUID policyId, String description) {
        return BonusEarning.builder()
                .memberId(memberId)
                .amount(amount)
                .type(type)
                .status(BonusEarningStatus.ACTIVE)
                .expiresAt(expiresAt)
                .policyId(policyId)
                .description(description)
                .build();
    }

    public static BonusEarning fromOrder(UUID memberId, long amount, UUID orderId,
                                          OffsetDateTime expiresAt, UUID policyId, String description) {
        return BonusEarning.builder()
                .memberId(memberId)
                .amount(amount)
                .type(BonusEarningType.PURCHASE_REWARD)
                .status(BonusEarningStatus.ACTIVE)
                .expiresAt(expiresAt)
                .policyId(policyId)
                .orderId(orderId)
                .description(description)
                .build();
    }

    public static BonusEarning refunded(UUID memberId, long amount, UUID originalOrderId,
                                         OffsetDateTime expiresAt, String description) {
        return BonusEarning.builder()
                .memberId(memberId)
                .amount(amount)
                .type(BonusEarningType.REFUND)
                .status(BonusEarningStatus.ACTIVE)
                .expiresAt(expiresAt)
                .referenceId(originalOrderId)
                .description(description)
                .build();
    }

    public void markPartiallyUsed() {
        validateActive();
        this.status = BonusEarningStatus.PARTIALLY_USED;
    }

    public void markFullyUsed() {
        validateActive();
        this.status = BonusEarningStatus.FULLY_USED;
    }

    public void markExpired() {
        validateActive();
        this.status = BonusEarningStatus.EXPIRED;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    private void validateActive() {
        if (this.status != BonusEarningStatus.ACTIVE) {
            throw new CustomException(CustomErrorCode.INVALID_BONUS_STATUS);
        }
    }
}
