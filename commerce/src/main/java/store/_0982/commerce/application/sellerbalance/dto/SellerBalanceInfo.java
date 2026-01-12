package store._0982.commerce.application.sellerbalance.dto;

import store._0982.commerce.domain.sellerbalance.SellerBalance;

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
