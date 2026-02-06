package store._0982.batch.application.sellerpayout.event;

import store._0982.common.domain.sellerpayout.SellerPayout;

public record SellerPayoutFailedEvent(
        SellerPayout sellerPayout,
        String failureReason
) {
}
