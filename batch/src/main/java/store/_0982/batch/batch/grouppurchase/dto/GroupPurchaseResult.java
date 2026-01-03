package store._0982.batch.batch.grouppurchase.dto;

import store._0982.batch.domain.grouppurchase.GroupPurchase;

public record GroupPurchaseResult(
        GroupPurchase groupPurchase,
        boolean success
) {
}
