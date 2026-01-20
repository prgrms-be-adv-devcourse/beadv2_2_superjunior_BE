package store._0982.batch.application.sellerbalance.event;

import store._0982.batch.domain.sellerbalance.SellerBalance;

public record SellerBalanceCompleted(
        SellerBalance sellerBalance,
        Long amount
) {
}
