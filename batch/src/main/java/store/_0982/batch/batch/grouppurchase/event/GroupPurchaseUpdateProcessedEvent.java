package store._0982.batch.batch.grouppurchase.event;

import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultWithProductInfo;

public record GroupPurchaseUpdateProcessedEvent(
        GroupPurchaseResultWithProductInfo updateItem
) {

}
