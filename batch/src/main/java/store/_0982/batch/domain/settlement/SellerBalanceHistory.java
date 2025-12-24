package store._0982.batch.domain.settlement;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seller_balance_history", schema = "order_schema")
public class SellerBalanceHistory {

    @Id
    @Column(name = "history_id", nullable = false)
    private UUID historyId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "settlement_id")
    private UUID settlementId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private BalanceHistoryStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public SellerBalanceHistory(
            UUID memberId,
            UUID settlementId,
            Long amount,
            BalanceHistoryStatus status
    ) {
        this.historyId = UUID.randomUUID();
        this.memberId = memberId;
        this.settlementId = settlementId;
        this.amount = amount;
        this.status = status;
    }

}
