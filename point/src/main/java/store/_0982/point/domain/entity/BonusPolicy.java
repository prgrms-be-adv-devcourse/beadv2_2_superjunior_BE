package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import store._0982.point.domain.constant.BonusPolicyType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.OptionalLong;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Check(
        constraints = "(reward_rate IS NOT NULL AND fixed_amount IS NULL AND max_reward_amount IS NOT NULL) " +
                "OR (reward_rate IS NULL AND fixed_amount IS NOT NULL) "
)
@Table(name = "bonus_policy", schema = "payment_schema")
public class BonusPolicy {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BonusPolicyType type;

    @Column(name = "reward_rate")
    private BigDecimal rewardRate;      // 적립 비율

    @Column(name = "fixed_amount")
    private Long fixedAmount;           // 비율 or 고정 금액 중 하나로 적용 가능

    @Column(name = "min_purchase_amount")
    private Long minPurchaseAmount;     // 최소 구매 금액 (구매 적립 시)

    @Column(name = "max_reward_amount")
    private Long maxRewardAmount;       // 최대 적립 한도

    @Column(name = "valid_from")
    private OffsetDateTime validFrom;   // 정책의 유효 기간

    @Column(name = "valid_until")
    private OffsetDateTime validUntil;

    @Column(name = "expiration_days", nullable = false)
    private int expirationDays;

    @Column(name = "target_group_purchase_id")
    private UUID targetGroupPurchaseId;     // null이면 전체 적용

    @Column(name = "target_category")
    private String targetCategory;          // 특정 카테고리만 적용

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public OptionalLong calculateBonusAmount(long paidAmount) {
        if (rewardRate != null) {
            long earning = BigDecimal.valueOf(paidAmount)
                    .multiply(rewardRate)
                    .longValue();
            return OptionalLong.of(Math.min(earning, maxRewardAmount));
        }
        if (fixedAmount != null) {
            return OptionalLong.of(fixedAmount);
        }
        return OptionalLong.empty();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
