package store._0982.batch.application.settlement.event;

import store._0982.common.domain.settlement.SellerPayout;

public record SellerPayoutCompletedEvent(
        SellerPayout sellerPayout
) {
}
