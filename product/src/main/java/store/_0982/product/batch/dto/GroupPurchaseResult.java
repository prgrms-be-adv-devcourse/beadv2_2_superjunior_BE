package store._0982.product.batch.dto;

import store._0982.product.domain.GroupPurchase;

public record GroupPurchaseResult(
        GroupPurchase groupPurchase,
        boolean success
) {
}
