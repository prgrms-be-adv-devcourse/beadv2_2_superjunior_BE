package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "bonus_deduction",
    schema = "payment_schema",
    indexes = {
        @Index(name = "idx_bonus_deduction_earning", columnList = "bonus_earning_id"),
        @Index(name = "idx_bonus_deduction_transaction", columnList = "transaction_id")
    }
)
public class BonusDeduction {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "bonus_earning_id", nullable = false, updatable = false)
    private UUID bonusEarningId;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @CreationTimestamp
    @Column(name = "deducted_at", nullable = false, updatable = false)
    private OffsetDateTime deductedAt;

    public static BonusDeduction create(UUID bonusEarningId, UUID transactionId, long amount) {
        return BonusDeduction.builder()
                .bonusEarningId(bonusEarningId)
                .transactionId(transactionId)
                .amount(amount)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
