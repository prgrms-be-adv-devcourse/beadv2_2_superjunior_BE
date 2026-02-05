package store._0982.batch.batch.grouppurchase.event;

import store._0982.common.domain.grouppurchase.GroupPurchase;

public record GroupPurchaseFailedProcessedEvent(
        GroupPurchase groupPurchase,
        String reason
) {
}
