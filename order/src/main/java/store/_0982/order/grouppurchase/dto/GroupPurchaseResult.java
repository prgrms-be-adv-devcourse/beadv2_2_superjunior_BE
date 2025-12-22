package store._0982.order.grouppurchase.dto;

import store._0982.product.domain.grouppurchase.GroupPurchase;

public record GroupPurchaseResult(
        GroupPurchase groupPurchase,
        boolean success
) {
}
