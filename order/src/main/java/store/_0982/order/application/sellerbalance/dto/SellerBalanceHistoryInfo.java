package store._0982.order.application.sellerbalance.dto;

import store._0982.order.domain.settlement.BalanceHistoryStatus;
import store._0982.order.domain.settlement.SellerBalanceHistory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SellerBalanceHistoryInfo(
        UUID sellerBalanceHistoryId,
        UUID memberId,
        UUID settlementId,
        Long amount,
        BalanceHistoryStatus status,
        OffsetDateTime createdAt
) {
    public static SellerBalanceHistoryInfo from(SellerBalanceHistory history) {
        return new SellerBalanceHistoryInfo(
                history.getHistoryId(),
                history.getMemberId(),
                history.getSettlementId(),
                history.getAmount(),
                history.getStatus(),
                history.getCreatedAt()
        );
    }
}
