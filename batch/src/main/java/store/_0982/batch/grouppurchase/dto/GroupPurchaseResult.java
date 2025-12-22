package store._0982.batch.grouppurchase.dto;

import store._0982.commerce.domain.grouppurchase.GroupPurchase;

public record GroupPurchaseResult(
        GroupPurchase groupPurchase,
        boolean success
) {
}
