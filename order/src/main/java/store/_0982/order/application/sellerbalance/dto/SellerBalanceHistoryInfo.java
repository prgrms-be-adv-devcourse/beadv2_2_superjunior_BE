package store._0982.order.application.sellerbalance.dto;

import store._0982.order.domain.settlement.BalanceHistoryStatus;
import store._0982.order.domain.settlement.SellerBalanceHistory;

import java.util.UUID;

public record SellerBalanceHistoryInfo(
        UUID sellerBalanceHistoryId,
        UUID memberId,
        UUID settlementId,
        Long amount,
        BalanceHistoryStatus status
) {
    public static SellerBalanceHistoryInfo from(SellerBalanceHistory history) {
        return new SellerBalanceHistoryInfo(
                history.getHistoryId(),
                history.getMemberId(),
                history.getSettlementId(),
                history.getAmount(),
                history.getStatus()
        );
    }
}
