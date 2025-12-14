package store._0982.order.application.sellerbalance.dto;

import store._0982.order.domain.settlement.SellerBalance;

import java.util.UUID;

public record SellerBalanceInfo(
        UUID sellerBalanceId,
        UUID memberId,
        Long balance
) {
    public static SellerBalanceInfo from(SellerBalance balance) {
        return new SellerBalanceInfo(
                balance.getBalanceId(),
                balance.getMemberId(),
                balance.getSettlementBalance());
    }
}
