package store._0982.batch.domain.sellerbalance;

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
@Table(name = "seller_balance_history", schema = "settlement_schema")
public class SellerBalanceHistory {

    @Id
    @Column(name = "history_id", nullable = false)
    private UUID historyId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "settlement_id")
    private UUID settlementId;

    @Column(name = "order_settlement_id", unique = true)
    private UUID orderSettlementId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private SellerBalanceHistoryStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static SellerBalanceHistory createCreditHistory(
            UUID sellerId,
            UUID orderSettlementId,
            Long amount
    ) {
        return new SellerBalanceHistory(
                sellerId,
                null,
                orderSettlementId,
                amount,
                SellerBalanceHistoryStatus.CREDIT
        );
    }

    public static SellerBalanceHistory createDebitHistory(
            UUID sellerId,
            UUID settlementId,
            Long amount
    ) {
        return new SellerBalanceHistory(
                sellerId,
                settlementId,
                null,
                amount,
                SellerBalanceHistoryStatus.DEBIT
        );
    }

    private SellerBalanceHistory(
            UUID memberId,
            UUID settlementId,
            UUID orderSettlementId,
            Long amount,
            SellerBalanceHistoryStatus status
    ) {
        this.historyId = UUID.randomUUID();
        this.memberId = memberId;
        this.settlementId = settlementId;
        this.orderSettlementId = orderSettlementId;
        this.amount = amount;
        this.status = status;
    }
}
